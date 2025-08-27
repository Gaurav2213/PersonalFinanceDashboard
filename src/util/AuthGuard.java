// util/AuthGuard.java
package util;
import com.sun.net.httpserver.HttpExchange;
import io.jsonwebtoken.Claims;
import java.io.IOException;

public class AuthGuard {

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
}
