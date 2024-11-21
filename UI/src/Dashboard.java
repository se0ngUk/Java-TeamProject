import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.sql.*;

public class Dashboard {
    private static final String BASE_PATH = "./UI/src/templates/";
    private static final UserManager userManager = new UserManager();
    
    // 대시보드 메인 핸들러
    public static class DashboardHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            // 로그인된 사용자 정보 확인
            UserManager.User loggedInUser = userManager.getLoggedInUser();

            // URL 쿼리에서 검색 키워드 추출
            String query = exchange.getRequestURI().getQuery();
            String keyword = (query != null && query.contains("keyword"))
                    ? URLDecoder.decode(query.split("=")[1], StandardCharsets.UTF_8)
                    : "";

            System.out.println("[DEBUG] 검색 키워드: " + keyword);

            // UserManager에서 책 데이터 검색
            List<UserManager.Book> books = userManager.searchBooks(keyword);

            // HTML 템플릿 로드 및 사용자 데이터 삽입
            String dashboardHtml = loadHtml("dashboard.html")
                    .replace("{{username}}", loggedInUser.id)
                    .replace("{{name}}", loggedInUser.name)
                    .replace("{{address}}", loggedInUser.address);

            // 검색 결과를 테이블 형식으로 삽입
            StringBuilder resultsBuilder = new StringBuilder();
           // UserManager.Book 명시적 사용
                for (UserManager.Book book : books) {
                    resultsBuilder.append("<tr>")
                        .append("<td>").append(book.getBookname()).append("</td>")
                        .append("<td>").append(book.getPublisher()).append("</td>")
                        .append("<td>").append(book.getOnlineprice()).append("</td>")
                        .append("<td>").append(book.getOnlineinv()).append("</td>")
                        .append("</tr>");
                }

            if (books.isEmpty()) {
                resultsBuilder.append("<tr><td colspan='4'>검색 결과가 없습니다.</td></tr>");
            }
            dashboardHtml = dashboardHtml.replace("{{searchResults}}", resultsBuilder.toString());
            

            // 디버그: 검색 결과 출력
            System.out.println("[DEBUG] 검색 결과: " + resultsBuilder);

            // HTML 응답 전송
            exchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
            exchange.sendResponseHeaders(200, dashboardHtml.getBytes(StandardCharsets.UTF_8).length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(dashboardHtml.getBytes(StandardCharsets.UTF_8));
            }
        }

        // HTML 파일 로드 메서드
        private String loadHtml(String fileName) throws IOException {
            return new String(Files.readAllBytes(Paths.get(BASE_PATH + fileName)), StandardCharsets.UTF_8);
        }
    }
    public static class StaticFileHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            // 요청된 파일 경로를 가져옴
            String filePath = "./UI/src/templates/" + exchange.getRequestURI().getPath();
            try {
                byte[] fileBytes = Files.readAllBytes(Paths.get(filePath));
                String contentType = getContentType(filePath); // 파일 확장자에 따른 Content-Type 결정
                exchange.getResponseHeaders().set("Content-Type", contentType);
                exchange.sendResponseHeaders(200, fileBytes.length);
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(fileBytes);
                }
            } catch (IOException e) {
                // 파일이 없는 경우 404 처리
                exchange.sendResponseHeaders(404, -1);
            }
        }
    
        private String getContentType(String filePath) {
            if (filePath.endsWith(".png")) return "image/png";
            if (filePath.endsWith(".jpg") || filePath.endsWith(".jpeg")) return "image/jpeg";
            if (filePath.endsWith(".css")) return "text/css";
            return "application/octet-stream";
        }
    }
    

    // CSS 핸들러
    public static class DashboardCssHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            byte[] css = Files.readAllBytes(Paths.get("./UI/src/styles/dashboardcss.css"));
            exchange.getResponseHeaders().set("Content-Type", "text/css; charset=UTF-8");
            exchange.sendResponseHeaders(200, css.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(css);
            }
        }
    }

    // 이미지 핸들러
    public static class ImageHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String imagePath = "./UI/src/templates/" + exchange.getRequestURI().getPath().replace("/images/", "");
            byte[] imageBytes = Files.readAllBytes(Paths.get(imagePath));
            exchange.getResponseHeaders().set("Content-Type", "image/png");
            exchange.sendResponseHeaders(200, imageBytes.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(imageBytes);
            }
        }
    }
    
}
