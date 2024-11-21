import java.io.IOException;
import connectDatabase.*;
import java.sql.*;

public class Main {
    public static void main(String[] args) {
        try {
            // LoginServer 시작
            LoginServer.startServer();
            // 데이터베이스 연결
            MainConnect.connect();

        } catch (Exception e) {
            System.out.println("서버 시작 중 오류가 발생했습니다: " + e);
        }
    }
}
