package controller;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import io.jsonwebtoken.Claims;
import model.AuthResponse;
import model.LoginResponse;
import util.AuthGuard;
import util.JWTUtils;
import util.SessionManager;
import util.Utils;

import java.io.IOException;

public class RefreshTokenHandler implements HttpHandler {

    // Only refresh tokens expiring within this window (5 minutes)
    private static final long THRESHOLD_SECONDS = 5 * 60;

    @Override
    public void handle(HttpExchange ex) throws IOException {
        if (!"POST".equalsIgnoreCase(ex.getRequestMethod())) {
            Utils.sendResponse(ex, 405, "Method Not Allowed");
            return;
        }

        // Step 1: Extract Bearer token from Authorization header
        String token = AuthGuard.extractBearerToken(ex);
        if (token == null) {
            Utils.sendJsonResponse(ex, new AuthResponse<>(false, "Missing Authorization header"), 401);
            return;
        }

        try {
            // Step 2: Extract JTI (unique token ID) and check if it has been blacklisted (logged out)
            String jti = JWTUtils.getJti(token);
            if (SessionManager.isBlacklisted(jti)) {
                Utils.sendJsonResponse(ex, new AuthResponse<>(false, "Token has been revoked"), 401);
                return;
            }

            // Step 3: Reject already-expired tokens — cannot refresh what is already dead
            if (JWTUtils.secondsUntilExpiry(token) == 0) {
                Utils.sendJsonResponse(ex, new AuthResponse<>(false, "Token has expired"), 401);
                return;
            }

            // Step 4: Only allow refresh if token is expiring within the threshold window
            if (!JWTUtils.isExpiringSoon(token, THRESHOLD_SECONDS)) {
                Utils.sendJsonResponse(ex, new AuthResponse<>(false, "Token not eligible for refresh yet"), 400);
                return;
            }

            // Step 5: Issue a new token, then blacklist the old one so it cannot be reused
            String newToken = JWTUtils.reissueIfValid(token);
            SessionManager.blacklist(jti, JWTUtils.getExpirationMillis(token));

            // Step 6: Parse claims from new token to build the response (sub = userId, email claim)
            Claims claims = JWTUtils.parseClaims(newToken);
            int userId = Integer.parseInt(claims.getSubject());
            String email = claims.get("email", String.class);

            // Step 7: Return new token wrapped in AuthResponse — frontend replaces old token in localStorage
            LoginResponse loginResponse = new LoginResponse(userId, email, newToken);
            Utils.sendJsonResponse(ex, new AuthResponse<>(true, "Token refreshed", loginResponse), 200);

        } catch (Exception e) {
            e.printStackTrace();
            Utils.sendJsonResponse(ex, new AuthResponse<>(false, "Invalid token"), 401);
        }
    }
}
