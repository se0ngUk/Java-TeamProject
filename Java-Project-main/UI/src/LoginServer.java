//서버를 실행시키기 위한 자바 파일입니다.
//현재 로그인은 서버가 켜져있는동안 배열에 임시적으로 저장하고 따로 데이터베이스에 저장하지는 않습니다.
//이후에 브라우저를 킨뒤에 localhost:5050/signup을 치시면 회원가입창으로 접속이 가능합니다.(5050은 포트번호입니다.)
//서버 종료방법은 터미널에서 ctrl+c를 입력하시면 종료가 됩니다.
// LoginServer.java - 서버 실행 및 회원가입/로그인 기능 담당
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

public class LoginServer {
    private static final UserManager userManager = new UserManager();
    private static final String BASE_PATH = "C:/Users/dlcjs/Desktop/공학대 수업/code/Javateam/src/templates/";
    //html파일이 들어있는 폴더를 절대경로로 넣어주시면됩니다.


    // 서버 시작 메서드
    public static void startServer() throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress("localhost", 5050), 0);
        server.createContext("/signup", new SignupHandler());
        server.createContext("/login", new LoginHandler());
        server.createContext("/style.css", new CssHandler());
        server.setExecutor(null);
        server.start();
        System.out.println("서버가 localhost에서 실행 중입니다.");
    }

    // CSS 파일 핸들러 html의 디자인을 받기위해 사용하는 클래스입니다.
    static class CssHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            byte[] css = Files.readAllBytes(Paths.get(BASE_PATH + "style.css")); //BASE_PATH는 절대경로로 사용자마자 설정을 추가로 해야합니다.
            exchange.getResponseHeaders().set("Content-Type", "text/css; charset=UTF-8");
            exchange.sendResponseHeaders(200, css.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(css);
            }
        }
    }

    //signup.html 파일 받아 처리하는 클래스입니다.
    // 회원가입 핸들러
    static class SignupHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("POST".equals(exchange.getRequestMethod())) {
                String formData = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
                Map<String, String> params = parseFormData(formData);
                
                //회원가입창에서 받은 데이터를 할당합니다.
                String id = params.get("username");
                String password = params.get("password");
                String name = params.get("fullName");
                String ssn = params.get("ssnpin");
                String address = params.get("adress");

                // 필드 유효성 검사
                String missingField = null;
                if (id == null || id.isEmpty()) missingField = "ID";
                else if (password == null || password.isEmpty()) missingField = "Password";
                else if (name == null || name.isEmpty()) missingField = "이름";
                else if (ssn == null || ssn.isEmpty()) missingField = "주민번호";
                else if (address == null || address.isEmpty()) missingField = "주소";

                if (missingField != null) {
                    sendResponse(exchange, "<html><body><h2>회원가입 실패: " + missingField + " 필드를 입력하세요.</h2></body></html>");
                } else if (userManager.isUsernameTaken(id)) {
                    sendResponse(exchange, "<html><body><h2>회원가입 실패: 이미 존재하는 사용자입니다.</h2></body></html>");
                } else {
                    userManager.registerUser(id, password, name, ssn, address);
                    exchange.getResponseHeaders().set("Location", "/login");
                    exchange.sendResponseHeaders(302, -1);
                }
            } else {
                sendResponse(exchange, loadHtml("signup.html"));
            }
        }
    }


    //Login.html 데이터 받는 클래스입니다.
    // 로그인 핸들러
    static class LoginHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("POST".equals(exchange.getRequestMethod())) { //데이터전송 방식 post
                String formData = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
                Map<String, String> params = parseFormData(formData);
                
                //로그인 html에서 가져온 ID,비밀번호를 가져온뒤에 현재 저장된
                String id = params.get("username");
                String password = params.get("password");

                if (userManager.authenticateUser(id, password)) {
                    sendResponse(exchange, "<html><body><h2>로그인 성공! 환영합니다, " + id + "님.</h2></body></html>");
                } else {
                    sendResponse(exchange, "<html><body><h2>로그인 실패: 잘못된 ID 또는 비밀번호입니다.</h2></body></html>");
                }
            } else {
                sendResponse(exchange, loadHtml("login.html"));
            }
        }
    }

    private static Map<String, String> parseFormData(String formData) {
        System.out.println("Raw form data: " + formData); // 폼 데이터 확인용 출력
        Map<String, String> params = new HashMap<>();
        //아래 for는 신경안쓰셔도됩니다. 디버깅하다 만들어진 똥입니다. 정상적으로 작동은 합니다.
        for (String pair : formData.split("&")) {
            String[] keyValue = pair.split("=");
            if (keyValue.length > 1) {
                String key = keyValue[0];
                String value = java.net.URLDecoder.decode(keyValue[1], StandardCharsets.UTF_8);
                params.put(key, value);
            } else {
                System.out.println("Warning: Missing value for key: " + keyValue[0]);
            }
        }
        return params;
    }
    

    // 응답 전송 메서드
    private static void sendResponse(HttpExchange exchange, String response) throws IOException {
        exchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
        exchange.sendResponseHeaders(200, response.getBytes(StandardCharsets.UTF_8).length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(response.getBytes(StandardCharsets.UTF_8));
        }
    }

    // HTML 파일 로드 메서드
    private static String loadHtml(String fileName) throws IOException {
        return new String(Files.readAllBytes(Paths.get(BASE_PATH + fileName)), StandardCharsets.UTF_8);
    }
}
