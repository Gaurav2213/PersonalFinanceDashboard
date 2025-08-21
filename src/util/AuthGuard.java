// util/AuthGuard.java
package util;

import com.sun.net.httpserver.HttpExchange;
import io.jsonwebtoken.Claims;

import java.io.IOException;

public class AuthGuard {

    // Extract token from "Authorization: Bearer <token>"
    public static String extractBearerToken(HttpExchange exchange) {
        String h = exchange.getRequestHeaders().getFirst("Authorization");
        if (h == null || !h.startsWith("Bearer ")) return null;
        return h.substring("Bearer ".length()).trim();
    }

    // Verify token (and not blacklisted). Returns claims if valid, else null.
    public static Claims verify(HttpExchange exchange) {
        String token = extractBearerToken(exchange);
        if (token == null) return null;
        if (SessionManager.isBlacklisted(token)) return null;

        try {
            Claims claims = JWTUtils.parseClaims(token);  // your 0.11.5 utils
            if (JWTUtils.isExpired(claims)) return null;
            return claims;
        } catch (Exception e) {
            return null;
        }
    }

    public static void unauthorized(HttpExchange exchange) throws IOException {
        Utils.sendResponse(exchange, 401, "{\"success\":false,\"message\":\"Unauthorized\",\"data\":null}");
    }
}
