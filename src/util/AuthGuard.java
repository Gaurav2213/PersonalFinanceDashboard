// util/AuthGuard.java
package util;
import com.sun.net.httpserver.HttpExchange;

import dao.UserDAO;
import io.jsonwebtoken.Claims;
import model.AnalyticsResponse;
import model.User;
import model.ValidationResult;
import service.UserService;

import java.io.IOException;

public class AuthGuard {
	private static final int RESET_TTL_MINUTES = 20;
    private static final String FRONTEND_BASE_URL = "http://localhost:3000";

    public static String extractBearerToken(HttpExchange exchange) {
        String h = exchange.getRequestHeaders().getFirst("Authorization");
        if (h == null || !h.startsWith("Bearer ")) return null;
        return h.substring("Bearer ".length()).trim();
    }

    // Returns claims if valid and not blacklisted, else null
    public static Claims verify(HttpExchange exchange) {
        String token = extractBearerToken(exchange);
        if (token == null) return null;

        try {
            // IMPORTANT: parseClaims must VERIFY signature + exp internally
            Claims claims = JWTUtils.parseClaims(token);
            if (JWTUtils.isExpired(claims)) return null;

            String jti = claims.getId(); // standard claim for token id
            if (SessionManager.isBlacklisted(jti)) return null;


            return claims;
        } catch (Exception e) {
            return null;
        }
    }

    public static void unauthorized(HttpExchange exchange) throws IOException {
        Utils.sendResponse(exchange, 401, "{\"success\":false,\"message\":\"Unauthorized\",\"data\":null}");
    }
    
    
    public static void requestPasswordReset(String email) {
        try {
            User user = UserDAO.getUserByEmail(email);
            // Always behave the same externally to avoid user enumeration
            if (user == null) return;

            String raw = TokenUtils.generateToken();                 // email this
            String hash = TokenUtils.sha256Hex(raw);                 // store this
            var exp = TokenUtils.generateExpiryMinutes(RESET_TTL_MINUTES);

            UserDAO.setResetToken(user.getId(), hash, exp);

            String link = FRONTEND_BASE_URL + "/reset-password?token=" + raw;

            // Dev-only: log token so you can test via Postman before UI is ready
            System.out.println("[DEV] Password reset link for " + email + ": " + link);

            EmailService.sendVerificationEmail(user.getEmail(), user.getName(), link);
        } catch (Exception e) {
            // Log internally; never leak details to client
            e.printStackTrace();
        }
    }
    
    public static AnalyticsResponse<Void> resetPassword(String rawToken, String newPassword) {
        if (rawToken == null || rawToken.isBlank()) {
            return new AnalyticsResponse<>(false, "token is required");
        }

        ValidationResult vr = UserService.validatePassword(newPassword);
        if (!vr.isValid()) {
            return new AnalyticsResponse<>(false, vr.getMessage());
        }

        String tokenHash = TokenUtils.sha256Hex(rawToken);

        try {
            Integer userId = UserDAO.findUserIdByValidResetHash(tokenHash);
            if (userId == null) {
                return new AnalyticsResponse<>(false, "Invalid or expired reset link");
            }

            String pwdHash = PasswordUtils.hashPassword(newPassword);
            UserDAO.updatePassword(userId, pwdHash);
            UserDAO.clearResetToken(userId);
           

            return new AnalyticsResponse<>(true,"Password has been reset successfully"); // success only
        } catch (Exception e) {
            e.printStackTrace();
            return new AnalyticsResponse<>(false, "Something went wrong. Please try again");
        }
    }

    
}
