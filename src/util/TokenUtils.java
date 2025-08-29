package util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

public class TokenUtils {

    private static final String CHARACTERS = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final int TOKEN_LENGTH = 64;

    public static String generateToken() {
        SecureRandom random = new SecureRandom();
        StringBuilder sb = new StringBuilder(TOKEN_LENGTH);
        for (int i = 0; i < TOKEN_LENGTH; i++) {
            sb.append(CHARACTERS.charAt(random.nextInt(CHARACTERS.length())));
        }
        return sb.toString();
    }

    /** Use minutes for verification  links (e.g., 60 min) */
    public static Timestamp generateExpiry(int hours) {
        return Timestamp.from(Instant.now().plus(hours, ChronoUnit.HOURS));
    }
    
    
    /** Use minutes for reset links (e.g., 20 min) */
    public static Timestamp generateExpiryMinutes(int minutes) {
        return Timestamp.from(Instant.now().plus(minutes, ChronoUnit.MINUTES));
    }

    /** SHA-256 hex for storing token securely */
    public static String sha256Hex(String rawToken) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest(rawToken.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(digest.length * 2);
            for (byte b : digest) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (Exception e) {
            throw new RuntimeException("Unable to hash reset token", e);
        }
    }

}
