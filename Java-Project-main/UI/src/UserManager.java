import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

public class UserManager {
    private final Map<String, User> userDatabase = new HashMap<>();
    private static final String SSN_PATTERN = "\\d{6}-\\d{7}";

    public void registerUser(String id, String password, String name, String ssn, String address) {
        if (Pattern.matches(SSN_PATTERN, ssn)) {
            userDatabase.put(id, new User(id, password, name, ssn, address));
            System.out.println("회원가입이 완료되었습니다.");
        } else {
            System.out.println("잘못된 주민번호 형식입니다.");
        }
    }

    public boolean authenticateUser(String id, String password) {
        User user = userDatabase.get(id);
        return user != null && user.password.equals(password);
    }

    public boolean isUsernameTaken(String id) {
        return userDatabase.containsKey(id);
    }

    static class User {
        String id;
        String password;
        String name;
        String ssn;
        String address; // 주소 필드 추가

        public User(String id, String password, String name, String ssn, String address) {
            this.id = id;
            this.password = password;
            this.name = name;
            this.ssn = ssn;
            this.address = address; // 주소 초기화
        }
    }
}
