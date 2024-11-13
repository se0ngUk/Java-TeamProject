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
import java.util.Timer;
import java.util.TimerTask;

public class LoginServer {
    private static final UserManager userManager = new UserManager(); // UserManager 인스턴스 생성

    public static void startServer() throws IOException {
        try {
            HttpServer server = HttpServer.create(new InetSocketAddress("localhost", 5050), 0);
            server.createContext("/login", new LoginHandler());
            server.createContext("/signup", new SignupHandler());
            server.createContext("/style.css", new StaticFileHandler("C:/Users/dlcjs/Desktop/공학대 수업/code/Javateam/src/templates/style.css"));
            server.setExecutor(null);
            server.start();
            System.out.println("서버가 localhost에서 실행 중입니다.");

            Timer timer = new Timer();
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    System.out.print(".");
                }
            }, 0, 3000);

        } catch (IOException e) {
            System.out.println("서버 시작 중 오류가 발생했습니다.");
            throw e;
        }
    }

    static class SignupHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("POST".equals(exchange.getRequestMethod())) {
                String formData = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
                Map<String, String> params = parseFormData(formData);
    
                String id = params.get("username");
                String password = params.get("password");
                String name = params.get("name");
                String ssn = params.get("ssn");
                String address = params.get("address"); // 주소 필드 추가
    
                if (id == null || password == null || name == null || ssn == null || address == null) {
                    String response = "<html><body><h2>회원가입 실패: 모든 필드를 입력하세요.</h2></body></html>";
                    sendResponse(exchange, response);
                } else if (userManager.isUsernameTaken(id)) {
                    String response = "<html><body><h2>회원가입 실패: 이미 존재하는 사용자입니다.</h2></body></html>";
                    sendResponse(exchange, response);
                } else {
                    userManager.registerUser(id, password, name, ssn, address);
                    exchange.getResponseHeaders().set("Location", "/login");
                    exchange.sendResponseHeaders(302, -1);
                    exchange.close();
                }
            } else {
                String form = loadHtml("signup.html");
                sendResponse(exchange, form);
            }
        }
    }
    
    static class StaticFileHandler implements HttpHandler {
        private final String filePath;

        public StaticFileHandler(String filePath) {
            this.filePath = filePath;
        }

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            byte[] fileBytes = Files.readAllBytes(Paths.get(filePath));
            exchange.getResponseHeaders().set("Content-Type", "text/css");
            exchange.sendResponseHeaders(200, fileBytes.length);
            OutputStream os = exchange.getResponseBody();
            os.write(fileBytes);
            os.close();
        }
    }

    private static String loadHtml(String fileName) throws IOException {
        String absolutePath = "C:/Users/dlcjs/Desktop/공학대 수업/code/Javateam/src/templates/" + fileName;
        try {
            return new String(Files.readAllBytes(Paths.get(absolutePath)), StandardCharsets.UTF_8);
        } catch (IOException e) {
            System.out.println("파일을 찾을 수 없습니다: " + absolutePath);
            throw e;
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

    private static void sendResponse(HttpExchange exchange, String response) throws IOException {
        exchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
        exchange.sendResponseHeaders(200, response.getBytes(StandardCharsets.UTF_8).length);
        OutputStream os = exchange.getResponseBody();
        os.write(response.getBytes(StandardCharsets.UTF_8));
        os.close();
    }
}

