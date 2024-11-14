import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

public class Dashboard {
    private static final UserManager userManager = new UserManager();

    static class DashboardHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String userId = exchange.getPrincipal().getName(); // 현재 로그인한 사용자 ID 가져오기
            UserManager.User user = userManager.retrieveUserFromDatabase(userId);

            if (user != null) {
                String response = "<html><body><h2>환영합니다, " + user.name + "님!</h2>" +"</p></body></html>";
                sendResponse(exchange, response);
            } else {
                String response = "<html><body><h2>사용자 정보를 불러올 수 없습니다.</h2></body></html>";
                sendResponse(exchange, response);
            }
        }

        private void sendResponse(HttpExchange exchange, String response) throws IOException {
            exchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
            exchange.sendResponseHeaders(200, response.getBytes(StandardCharsets.UTF_8).length);
            OutputStream os = exchange.getResponseBody();
            os.write(response.getBytes(StandardCharsets.UTF_8));
            os.close();
        }
    }
}
