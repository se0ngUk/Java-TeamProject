import java.util.HashMap;
import java.util.regex.Pattern;

class User {
    String id;
    String password;
    String name;
    String ssn;

    public User(String id, String password, String name, String ssn) {
        this.id = id;
        this.password = password;
        this.name = name;
        this.ssn = ssn;
    }
}

public class UserManager {
    private HashMap<String, User> userDatabase = new HashMap<>();
    private static final String SSN_PATTERN = "\\d{6}-\\d{7}";

    // 사용자 등록 메서드 (데이터베이스 사용)
    public void registerUser(String id, String password, String name, String ssn) {
        if (Pattern.matches(SSN_PATTERN, ssn)) {
            userDatabase.put(id, new User(id, password, name, ssn));
            System.out.println("회원가입이 완료되었습니다.");
        } else {
            System.out.println("잘못된 주민번호 형식입니다.");
        }
    }

    // 사용자 인증 메서드
    public boolean authenticateUser(String id, String password) {
        User user = userDatabase.get(id);
        return user != null && user.password.equals(password);
    }

    // 사용자명 중복 확인 메서드
    public boolean isUsernameTaken(String id) {
        return userDatabase.containsKey(id);
    }
}
