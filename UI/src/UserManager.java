import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserManager {
    private User loggedInUser = null;

    public User getLoggedInUser() {
        return loggedInUser;
    }

    // 데이터베이스 연결 정보
    private final String dbUrl = "jdbc:mysql://localhost:3306/jtp";
    private final String dbUser = "root";
    private final String dbPassword = "root";

    public UserManager() {
        // 생성자에서 추가 작업 필요 시 추가 가능
    }

    public List<Book> getAllBooks() {
        List<Book> books = new ArrayList<>();
        String query = "SELECT bookname, publisher, onlineprice, onlineinv FROM book";
        try (Connection conn = DriverManager.getConnection(dbUrl, dbUser, dbPassword);
             PreparedStatement pstmt = conn.prepareStatement(query);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                books.add(new Book(
                        rs.getString("bookname"),
                        rs.getString("publisher"),
                        rs.getInt("onlineprice"),
                        rs.getInt("onlineinv")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return books;
    }

    public List<Book> searchBooks(String keyword) {
        List<Book> results = new ArrayList<>();
        String query = "SELECT bookname, publisher, onlineprice, onlineinv FROM book WHERE bookname LIKE ?";
        try (Connection conn = DriverManager.getConnection(dbUrl, dbUser, dbPassword);
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, "%" + keyword + "%");
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    results.add(new Book(
                            rs.getString("bookname"),
                            rs.getString("publisher"),
                            rs.getInt("onlineinv"),
                            rs.getInt("onlineprice")
                    ));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return results;
    }

    public boolean authenticateUser(String id, String password) {
        String query = "SELECT * FROM user WHERE userid = ?";
        try (Connection conn = DriverManager.getConnection(dbUrl, dbUser, dbPassword);
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next() && rs.getString("passwd").equals(password)) {
                    loggedInUser = new User(
                            rs.getString("userid"),
                            rs.getString("passwd"),
                            rs.getString("username"),
                            rs.getString("RRN"),
                            rs.getString("address")
                    );
                    return true;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean isUseridTaken(String id) {
        String query = "SELECT COUNT(*) FROM user WHERE userid = ?";
        try (Connection conn = DriverManager.getConnection(dbUrl, dbUser, dbPassword);
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next() && rs.getInt(1) > 0) {
                    return true;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean isUserSSNTaken(String SSN) {
        String query = "SELECT COUNT(*) FROM user WHERE RRN = ?";
        try (Connection conn = DriverManager.getConnection(dbUrl, dbUser, dbPassword);
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, SSN);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next() && rs.getInt(1) > 0) {
                    return true;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // 내부 클래스 - 사용자 정보
    public static class User {
        String id, password, name, ssn, address;

        public User(String id, String password, String name, String ssn, String address) {
            this.id = id;
            this.password = password;
            this.name = name;
            this.ssn = ssn;
            this.address = address;
        }
    }

    // 내부 클래스 - 책 정보
    public static class Book {
        private final String bookname, publisher;
        private final int onlineprice, onlineinv;

        public Book(String bookname, String publisher, int onlineprice, int onlineinv) {
            this.bookname = bookname;
            this.publisher = publisher;
            this.onlineprice = onlineprice;
            this.onlineinv = onlineinv;
        }

        public String getBookname() {
            return bookname;
        }

        public String getPublisher() {
            return publisher;
        }

        public int getOnlineprice() {
            return onlineprice;
        }

        public int getOnlineinv() {
            return onlineinv;
        }
    }
}
