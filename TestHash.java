import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class TestHash {
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String hash = "$2a$10$6ZxRWzLjuh5AcWwv6YAM/uGS4duWTT.4TWNlcchwzIPm54lALgHY6";
        System.out.println("Matches password123? " + encoder.matches("password123", hash));
    }
}
