package controller;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import util.AuthGuard;
import util.JWTUtils;
import util.SessionManager;
import util.Utils;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class RefreshTokenHandler implements HttpHandler {
	// In seconds (for refresh threshold check)
    private static final long THRESHOLD_SECONDS =  5 * 60 ; // 5 minutes in second (⚠️ see note below)

    @Override
    public void handle(HttpExchange ex) throws IOException {
        System.out.println("🔁 RefreshTokenHandler invoked");

        if (!"POST".equalsIgnoreCase(ex.getRequestMethod())) {
            System.out.println("❌ Invalid method: " + ex.getRequestMethod());
            Utils.sendResponse(ex, 405, "Method Not Allowed");
            return;
        }

        String token = AuthGuard.extractBearerToken(ex);
        if (token == null) {
            System.out.println("❌ Missing bearer token");
            Utils.sendResponse(ex, 401, "Missing bearer token");
            return;
        }

        try {
            System.out.println("🔐 Token received: " + token);

            String jti = JWTUtils.getJti(token);
            System.out.println("📌 Extracted JTI: " + jti);

            if (SessionManager.isBlacklisted(jti)) {
                System.out.println("⛔ Token is blacklisted");
                Utils.sendResponse(ex, 401, "Token revoked");
                return;
            }

            long secs = JWTUtils.secondsUntilExpiry(token);
            System.out.println("⏱️ Seconds until expiry: " + secs);

            if (secs == 0) {
                System.out.println("⛔ Token already expired");
                Utils.sendResponse(ex, 401, "Token expired");
                return;
            }

            // Optional: only allow refresh near expiry
            System.out.println("⚙️ Checking expiring soon (threshold: " + THRESHOLD_SECONDS + "ms)");
            if (!JWTUtils.isExpiringSoon(token, THRESHOLD_SECONDS)) {
                System.out.println("⛔ Token not expiring soon");
                Utils.sendResponse(ex, 400, "Token not eligible for refresh yet");
                return;
            }

            System.out.println("🔁 Reissuing new token...");
            String newToken = JWTUtils.reissueIfValid(token);
            long newSecs = JWTUtils.secondsUntilExpiry(newToken);

            long oldExp = JWTUtils.getExpirationMillis(token);
            System.out.println("🛡️ Blacklisting old token (JTI: " + jti + ") until: " + oldExp);
            SessionManager.blacklist(jti, oldExp);

            Map<String, Object> body = new HashMap<>();
            body.put("success", true);
            body.put("token", newToken);
            body.put("expiresInSeconds", newSecs);

            System.out.println("✅ Refresh successful: " + body);
            Utils.sendJsonResponse(ex, body, 200);

        } catch (Exception e) {
            System.out.println("❌ Exception during token refresh");
            e.printStackTrace();  // ✅ Full stacktrace
            Utils.sendResponse(ex, 401, "Invalid token");
        }
    }
}
