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

    private static final long THRESHOLD_SECONDS = 5 * 60 * 1000; // 5 minutes

    @Override
    public void handle(HttpExchange ex) throws IOException {
        if (!"POST".equalsIgnoreCase(ex.getRequestMethod())) {
            Utils.sendResponse(ex, 405, "Method Not Allowed");
            return;
        }

        String token =  AuthGuard.extractBearerToken(ex);
        if (token == null) {
            Utils.sendResponse(ex, 401, "Missing bearer token");
            return;
        }

        // (optional) reject if blacklisted
        if (SessionManager.isBlacklisted(token)) {
            Utils.sendResponse(ex, 401, "Token revoked");
            return;
        }

        try {
            long secs = JWTUtils.secondsUntilExpiry(token);
            if (secs == 0) {
                Utils.sendResponse(ex, 401, "Token expired");
                return;
            }
            
            //Optional: only allow refresh when close to expiry
             if (!JWTUtils.isExpiringSoon(token, THRESHOLD_SECONDS)) {
                 Utils.sendResponse(ex, 400, "Token not eligible for refresh yet");
                 return;
           }
             
         // blacklist the old token so only the newest stays valid
        	String newToken = JWTUtils.reissueIfValid(token);
        	
        	String jti  = JWTUtils.getJti(token);
        	long expirInMiliOldToken = JWTUtils.getExpirationMillis(token);
        	
            SessionManager.blacklist(jti,expirInMiliOldToken);

            long newSecs = JWTUtils.secondsUntilExpiry(newToken);

            Map<String, Object> body = new HashMap<>();
            body.put("success", true);
            body.put("token", newToken);
            body.put("expiresInSeconds", newSecs);

            System.out.println(body);
            Utils.sendJsonResponse(ex,  body,200);

        } catch (Exception e) {
            Utils.sendResponse(ex, 401, "Invalid token");
        }
    }
}
