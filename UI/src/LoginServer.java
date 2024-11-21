//서버를 실행시키기 위한 자바 파일입니다.
//현재 로그인은 서버가 켜져있는동안 배열에 임시적으로 저장하고 따로 데이터베이스에 저장하지는 않습니다.
//이후에 브라우저를 킨뒤에 localhost:5050/signup을 치시면 회원가입창으로 접속이 가능합니다.(5050은 포트번호입니다.)
//서버 종료방법은 터미널에서 ctrl+c를 입력하시면 종료가 됩니다.
// LoginServer.java - 서버 실행 및 회원가입/로그인/대시보드 기능 담당
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.sql.*;

public class LoginServer {
    private static final UserManager userManager = new UserManager();

    public static void startServer() throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress("localhost", 5050), 0);
        server.createContext("/signup", new SignupHandler());
        server.createContext("/login", new LoginHandler());
        //server.createContext("/dashboard", new Dashboard.DashboardHandler());
        server.createContext("/dashboard",new DynamicDashboardHandler());
        server.createContext("/style.css", new CssHandler());
        server.createContext("/dashboardcss.css", new Dashboard.DashboardCssHandler());
        server.createContext("/path/", new ImageHandler());
        server.setExecutor(null);
        server.start();
        System.out.println("서버가 localhost에서 실행 중입니다.");
    }

    public static class SignupHandler implements HttpHandler {
        String url = "jdbc:mysql://localhost:3306/jtp";
        String user = "root";
        String password = "root";

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("POST".equals(exchange.getRequestMethod())) {
                String formData = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
                Map<String, String> params = parseFormData(formData);
                String id = params.get("username");
                String userpassword = params.get("password");
                 String name = params.get("fullName");
                String ssn = params.get("ssnpin");
                String address = params.get("address");

                if (id == null || id.isEmpty() || userpassword == null || userpassword.isEmpty() ||
                    name == null || name.isEmpty() || ssn == null || ssn.isEmpty() || 
                    address == null || address.isEmpty()) {
                    sendResponse(exchange, "<html><body><h2>회원가입 실패: 모든 필드를 입력하세요.</h2></body></html>");
                } else if (userManager.isUseridTaken(id)) {
                    sendResponse(exchange, "<html><body><h2>회원가입 실패: 이미 존재하는 아이디입니다.</h2></body></html>");
                } else if(userManager.isUserSSNTaken(ssn)){
                    sendResponse(exchange, "<html><body><h2>회원가입 실패: 이미 존재하는 사용자입니다.</h2></body></html>");
                }
                else {
                        try{
                            Class.forName("com.mysql.cj.jdbc.Driver");
                            Connection conn = DriverManager.getConnection(url, user, password);
                            Statement stmt = conn.createStatement();
                            String Register = "INSERT INTO user(userid, passwd, username, RRN, address) VALUES(?, ?, ?, ?, ?)";
                            PreparedStatement pstmt = conn.prepareStatement(Register);
                            pstmt.setString(1, id);
                            pstmt.setString(2, userpassword);
                            pstmt.setString(3, name);
                            pstmt.setString(4, ssn);
                            pstmt.setString(5, address);
                            pstmt.executeUpdate();
                        }
                        catch(Exception e){}
                        exchange.getResponseHeaders().set("Location", "/login");
                        exchange.sendResponseHeaders(302, -1);
                }
            }
            else {
                sendResponse(exchange, loadHtml("signup.html"));
            }
        }
    }

    static class LoginHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("POST".equals(exchange.getRequestMethod())) {
                String formData = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
                Map<String, String> params = parseFormData(formData);

                String id = params.get("username");
                String userpassword = params.get("password");

                if (userManager.authenticateUser(id, userpassword)) {
                    try{
                    System.out.println("로그인 성공: " + id);
                        exchange.getResponseHeaders().set("Location", "/dashboard");
                        exchange.sendResponseHeaders(302, -1);
                    }
                    catch(Exception e){System.out.println(e);}
                }
                else {
                    sendResponse(exchange, "<html><body><h2>로그인 실패: 잘못된 ID 또는 비밀번호입니다.</h2></body></html>");
                }
            } else {
                sendResponse(exchange, loadHtml("login.html"));
            }
        }
    }

    static class DynamicDashboardHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            UserManager.User loggedInUser = userManager.getLoggedInUser();
    
            // 책 목록을 HTML로 변환
            StringBuilder booksHtml = new StringBuilder();
            for (UserManager.Book book : userManager.getAllBooks()) {
                booksHtml.append("<tr>")
                         .append("<td>").append(book.getBookname()).append("</td>")
                         .append("<td>").append(book.getPublisher()).append("</td>")
                         .append("<td>").append(book.getOnlineprice()).append("</td>")
                         .append("<td>").append(book.getOnlineinv()).append("</td>")
                         .append("</tr>");
            }
    
            // 대시보드 HTML 생성
            String dashboardHtml = loadHtml("dashboard.html")
                    .replace("{{username}}", loggedInUser.id)
                    .replace("{{name}}", loggedInUser.name)
                    .replace("{{address}}", loggedInUser.address)
                    .replace("{{searchResults}}", booksHtml.toString()); // {{searchResults}} 대체
    
            exchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
            exchange.sendResponseHeaders(200, dashboardHtml.getBytes(StandardCharsets.UTF_8).length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(dashboardHtml.getBytes(StandardCharsets.UTF_8));
            }
        }
    }
    

    static class CssHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            byte[] css = Files.readAllBytes(Paths.get("./UI/src/styles/style.css"));
            exchange.getResponseHeaders().set("Content-Type", "text/css; charset=UTF-8");
            exchange.sendResponseHeaders(200, css.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(css);
            }
        }
    }

    static class DashboardCssHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            byte[] css = Files.readAllBytes(Paths.get("./UI/src/tempates/dashboardcss.css"));
            exchange.getResponseHeaders().set("Content-Type", "text/css; charset=UTF-8");
            exchange.sendResponseHeaders(200, css.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(css);
            }
        }
    }

    private static Map<String, String> parseFormData(String formData) {
        Map<String, String> params = new HashMap<>();
        for (String pair : formData.split("&")) {
            String[] keyValue = pair.split("=");
            if (keyValue.length > 1) {
                params.put(keyValue[0], java.net.URLDecoder.decode(keyValue[1], StandardCharsets.UTF_8));
            }
        }
        return params;
    }

    private static String loadHtml(String fileName) throws IOException {
        return new String(Files.readAllBytes(Paths.get("./UI/src/templates/" + fileName)), StandardCharsets.UTF_8);
    }

    private static void sendResponse(HttpExchange exchange, String response) throws IOException {
        exchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
        exchange.sendResponseHeaders(200, response.getBytes(StandardCharsets.UTF_8).length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(response.getBytes(StandardCharsets.UTF_8));
        }
    }
        // 새로운 핸들러 추가
        static class ImageHandler implements HttpHandler {
            @Override
            public void handle(HttpExchange exchange) throws IOException {
                // 요청된 파일의 경로를 얻음
                String filePath = "./UI/src/templates" + exchange.getRequestURI().getPath(); // /path/ 경로 포함
                try {
                    byte[] fileBytes = Files.readAllBytes(Paths.get(filePath)); // 파일 읽기
                    String contentType = getContentType(filePath); // 파일에 맞는 Content-Type 결정
                    exchange.getResponseHeaders().set("Content-Type", contentType);
                    exchange.sendResponseHeaders(200, fileBytes.length);
                    try (OutputStream os = exchange.getResponseBody()) {
                        os.write(fileBytes); // 파일 내용 전송
                    }
                } catch (IOException e) {
                    exchange.sendResponseHeaders(404, -1); // 파일을 찾지 못했을 경우 404 응답
                }
            }
    
            private String getContentType(String filePath) {
                if (filePath.endsWith(".png")) return "image/png";
                else if (filePath.endsWith(".jpg") || filePath.endsWith(".jpeg")) return "image/jpeg";
                else if (filePath.endsWith(".gif")) return "image/gif";
                else return "application/octet-stream"; // 기본 Content-Type
            }
        }
    

    public static void main(String[] args) throws IOException {
        startServer();
    }
}
