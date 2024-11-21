package userManage;

import java.sql.*;
import java.util.*;

public class UserMenu {
    // 사용자 메뉴 화면
    public static void userMenuScreen(String userID, Connection conn) {
        Scanner scanner = new Scanner(System.in);
        try {
            PreparedStatement ps = conn.prepareStatement("SELECT * FROM users WHERE userID = ?");
            ps.setString(1, userID);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                String userid = rs.getString("userid");
                String password = rs.getString("passwd");
                String name = rs.getString("username");
                String SSN = rs.getString("RRN");
                String address = rs.getString("address");
                while (true) {
                    System.out.println("==== " + userid + "의 메뉴 ====");
                    System.out.println("1. 도서 검색");
                    System.out.println("2. 도서 구매");
                    System.out.println("3. 중고 도서 구매");
                    System.out.println("4. 중고 도서 등록");
                    System.out.println("5. 메세지");
                    System.out.println("6. 로그아웃");
                    System.out.println("==============================");

                    System.out.print("메뉴를 선택하세요: ");
                    int choice;
                    try {
                        choice = Integer.parseInt(scanner.nextLine());
                    } catch (NumberFormatException e) {
                        System.out.println("숫자를 입력해주세요.");
                        continue;
                    }

                    switch (choice) {
                        case 1:
                            serchBookName(conn);
                            break;
                        case 2:
                            praseBook(address, conn);
                            break;
                        case 3:
                            praceUsedBook(userid, conn);
                            break;
                        case 4:
                            enrollUsedBook(userid, conn);
                            break;
                        case 5:
                            conversationMenu(userid, conn);
                            break;
                        case 6:
                            System.out.println("로그아웃 되었습니다.");
                            return;
                        default:
                            System.out.println("잘못된 선택입니다. 다시 시도하세요.");
                    }
                }
            }
        }
        catch(Exception e){}
    }

    // 도서 검색 메소드
    public static void serchBookName(Connection conn) {
        try {
            Scanner scanner = new Scanner(System.in);
            System.out.print("검색할 책 이름을 입력하세요: ");
            String bookName = scanner.nextLine();

            String query = "SELECT ONLINEINV, ONLINEPRICE, COUNT(BOOK_SELLER.USEDINV) AS USEDINV, AVG(BOOK_SELLER.USEDPRICE) AS USEDPRICE " +
                    "FROM BOOK LEFT JOIN BOOK_SELLER ON BOOK.BOOKID = BOOK_SELLER.BOOK_BOOKID " +
                    "WHERE BOOK.BOOKNAME = ? GROUP BY BOOK.BOOKID, BOOK.ONLINEINV, BOOK.ONLINEPRICE";
            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setString(1, bookName);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                int onlineInv = rs.getInt("ONLINEINV");
                double onlinePrice = rs.getDouble("ONLINEPRICE");
                int usedInv = rs.getInt("USEDINV");
                double usedPrice = rs.getDouble("USEDPRICE");

                System.out.println("==== 책 검색 ====");
                System.out.println("검색한 책: " + bookName);
                System.out.println("신제품 수량: " + onlineInv);
                System.out.println("신제품 가격: " + onlinePrice);
                System.out.println("중고 수량: " + usedInv);
                System.out.println("중고 가격: " + (usedPrice > 0 ? usedPrice : "N/A"));
            }
            else {
                System.out.println("해당 책이 없습니다.");
            }
        }
        catch (Exception e) {System.out.println(e);}
    }

    // 도서 구매 메소드
    public static void praseBook(String address, Connection conn) {
        String userAddress = address;
        try {
            Scanner scanner = new Scanner(System.in);
            System.out.print("구입할 도서를 입력하세요: ");
            String bookName = scanner.nextLine();

            String query = "SELECT ONLINEPRICE, ONLINEINV FROM BOOK WHERE BOOKNAME = ?";
            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setString(1, bookName);
            ResultSet rs = pstmt.executeQuery();
            if(rs.next()) {
                int onlineInv = rs.getInt("ONLINEINV");
                double onlinePrice = rs.getDouble("ONLINEPRICE");
                if(onlineInv == 0){
                    System.out.println("책 수량이 없습니다.");
                    return;
                }
                else {
                    System.out.println("책 가격은 " + onlinePrice + "원입니다.");
                }
                System.out.print("도서를 구매하시겠습니까? (Y/N): ");
                String decision = scanner.nextLine();
                if(decision.equals("Y")) {
                    String updateQuery = "UPDATE BOOK SET ONLINEINV = ONLINEINV - 1 WHERE BOOKNAME = ?";
                    pstmt = conn.prepareStatement(updateQuery);
                    pstmt.setString(1, bookName);
                    pstmt.executeUpdate();
                    System.out.println("책 이름: " + bookName);
                    System.out.println("책 가격: " + onlinePrice + "원");
                    System.out.println("주소: " + userAddress);
                    System.out.println("배송을 시작합니다.");
                }
                else {
                    System.out.println("구매가 취소되었습니다.");
                }
            }
            else {
                System.out.println("해당 책이 없습니다.");
            }
        }
        catch(Exception e) {System.out.println(e);}
    }

    // 중고 도서 등록 메소드
    public static void enrollUsedBook(String userid, Connection conn) {
        try {
            String sql = "SELECT * FROM user WHERE userid = ?";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, userid);
            ResultSet rs = pstmt.executeQuery();
            if(rs.next()) {
                String password = rs.getString("passwd");
                String name = rs.getString("username");
                String SSN = rs.getString("RRN");
                String address = rs.getString("address");
                Scanner scanner = new Scanner(System.in);
                System.out.print("등록할 책의 제목을 입력해주세요: ");
                String bookName = scanner.nextLine();
                System.out.print("등록할 책의 가격을 입력해주세요: ");
                int usedPrice = scanner.nextInt();
                System.out.println("책 제목: " + bookName);
                System.out.println("책 가격: " + usedPrice + "원");
                System.out.println("등록할 사용자: " + name);
                System.out.print("등록하시겠습니까? (Y/N): ");
                String decision = scanner.nextLine();
                if (decision.equalsIgnoreCase("Y")) {
                    String insertQuery = "INSERT INTO BOOK_SELLER (USER_USERID, BOOK_BOOKID, USEDPRICE, USEDINV) " +
                            "VALUES (?, (SELECT BOOKID FROM BOOK WHERE BOOKNAME = ?), ?, 1)";
                    PreparedStatement pstmt2 = conn.prepareStatement(insertQuery);
                    pstmt2.setString(1, userid);
                    pstmt2.setString(2, bookName);
                    pstmt2.setInt(3, usedPrice);
                    int rowsAffected = pstmt2.executeUpdate();
                    if (rowsAffected > 0) {
                        System.out.println("중고 도서가 등록되었습니다.");
                    } else {
                        System.out.println("중고 도서 등록에 실패했습니다. 책이 존재하는지 확인해주세요.");
                    }
                }
                else {
                    System.out.println("중고 도서 등록이 취소되었습니다.");
                }
            }
        }
        catch(Exception e){}
    }

    // 중고 도서 구매 메소드
    public static void praceUsedBook(String userid, Connection conn) {
        try {
            Scanner scanner = new Scanner(System.in);
            System.out.print("구입할 중고 도서의 제목을 입력해주세요: ");
            String bookName = scanner.nextLine();
            String query = "SELECT BOOK_SELLER.USER_USERID, BOOK_SELLER.USEDPRICE " +
                    "FROM BOOK_SELLER JOIN BOOK ON BOOK_SELLER.BOOK_BOOKID = BOOK.BOOKID " +
                    "WHERE BOOK.BOOKNAME = ? ORDER BY BOOK_SELLER.USEDPRICE ASC FETCH FIRST 10 ROWS ONLY";
            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setString(1, bookName);
            ResultSet rs = pstmt.executeQuery();
            System.out.println(bookName + "의 판매자 목록입니다:");
            boolean hasSellers = false;
            while(rs.next()) {
                hasSellers = true;
                String sellerId = rs.getString("USER_USERID");
                double usedPrice = rs.getDouble("USEDPRICE");
                System.out.println(sellerId + " 님 가격: " + usedPrice + "원");
            }
            if (!hasSellers) {
                System.out.println("중고 도서 판매자가 없습니다.");
                return;
            }
            System.out.print("메세지를 보낼 판매자의 아이디를 입력해주세요: ");
            String receiverId = scanner.nextLine();
            String checkSellerQuery = "SELECT COUNT(*) FROM BOOK_SELLER WHERE USER_USERID = ? AND BOOK_BOOKID = (SELECT BOOKID FROM BOOK WHERE BOOKNAME = ?)";
            PreparedStatement checkpstmt = conn.prepareStatement(checkSellerQuery);
            checkpstmt.setString(1, receiverId);
            checkpstmt.setString(2, bookName);
            ResultSet checkRs = checkpstmt.executeQuery();
            if(checkRs.next() && checkRs.getInt(1) > 0) {
                System.out.print("메세지를 입력해주세요: ");
                String messageContent = scanner.nextLine();
                String insertMessage = "INSERT INTO CONVERSATION (SENDER_USERID, RECEIVER_USERID, MESSAGE_CONTENT, SENT_TIME) VALUES (?, ?, ?, SYSDATE)";
                PreparedStatement insertpstmt = conn.prepareStatement(insertMessage);
                insertpstmt.setString(1, userid);
                insertpstmt.setString(2, receiverId);
                insertpstmt.setString(3, messageContent);
                insertpstmt.executeUpdate();
                System.out.println("메세지를 전송했습니다.");
            }
            else{
                System.out.println("해당 판매자가 존재하지 않거나 중고 도서를 판매하지 않습니다.");
            }
        } catch(Exception e){}
    }

    // 대화 메뉴 메소드
    public static void conversationMenu(String userid, Connection conn) {
        try {
            PreparedStatement pstmt = conn.prepareStatement(
                    "SELECT DISTINCT CASE WHEN SENDER_USERID = ? THEN RECEIVER_USERID ELSE SENDER_USERID END AS PARTNER_ID " +
                            "FROM CONVERSATION WHERE SENDER_USERID = ? OR RECEIVER_USERID = ?");
            pstmt.setString(1, userid);
            pstmt.setString(2, userid);
            pstmt.setString(3, userid);
            ResultSet rs = pstmt.executeQuery();
            System.out.println("========= 대화 =========");
            int count = 0;
            while(rs.next()) {
                String partnerId = rs.getString("PARTNER_ID");
                System.out.println((++count) + ". " + partnerId + " 님과의 대화방");
            }
            System.out.println("=======================");
            if (count == 0) {
                System.out.println("대화 기록이 없습니다.");
                return;
            }
            Scanner scanner = new Scanner(System.in);
            System.out.print("대화할 상대의 유저 아이디를 입력해주세요: ");
            String selectedPartnerId = scanner.nextLine();
            conversation(userid, conn, selectedPartnerId);
        }
        catch(Exception e){}
    }

    // 대화 메소드
    public static void conversation(String userid, Connection conn, String partnerId) {
        try {
            PreparedStatement pstmt = conn.prepareStatement(
                    "SELECT SENDER_USERID, MESSAGE_CONTENT, SENT_TIME " +
                            "FROM CONVERSATION WHERE (SENDER_USERID = ? AND RECEIVER_USERID = ?) " +
                            "OR (SENDER_USERID = ? AND RECEIVER_USERID = ?) ORDER BY SENT_TIME");
            pstmt.setString(1, userid);
            pstmt.setString(2, partnerId);
            pstmt.setString(3, partnerId);
            pstmt.setString(4, userid);
            while(true){
                ResultSet rs = pstmt.executeQuery();
                System.out.println("==== " + partnerId + "님과의 대화 ====");
                while (rs.next()) {
                    String sender = rs.getString("SENDER_USERID");
                    String message = rs.getString("MESSAGE_CONTENT");
                    System.out.println(sender + ": " + message);
                }
                System.out.println("===========================");
                Scanner scanner = new Scanner(System.in);
                System.out.print("MESSAGE (종료하려면 'EXIT' 입력): ");
                String messageContent = scanner.nextLine();
                if ("EXIT".equalsIgnoreCase(messageContent)) {
                    System.out.println("대화를 종료합니다.");
                    break;
                }
                PreparedStatement insertStmt = conn.prepareStatement("INSERT INTO CONVERSATION (SENDER_USERID, RECEIVER_USERID, MESSAGE_CONTENT, SENT_TIME) VALUES (?, ?, ?, SYSTIMESTAMP)");
                insertStmt.setString(1, userid);
                insertStmt.setString(2, partnerId);
                insertStmt.setString(3, messageContent);
                insertStmt.executeUpdate();
            }
        }
        catch(Exception e){}
    }
}


