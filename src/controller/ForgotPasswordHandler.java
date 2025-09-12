package controller;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.json.JSONObject;

import util.AuthGuard;
import util.Utils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public class ForgotPasswordHandler implements HttpHandler {

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        // Allow only POST
        if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
            Utils.sendResponse(exchange, 405, "Method Not Allowed");
            return;
        }

        String email = "";

        // 1) Try to read JSON body: { "email": "user@example.com" }
        try {
            byte[] bytes = exchange.getRequestBody().readAllBytes();
            String body = new String(bytes, StandardCharsets.UTF_8).trim();
            if (!body.isBlank()) {
                JSONObject json = new JSONObject(body);
                email = json.optString("email", "").trim();
            }
        } catch (Exception ignored) {
            // we'll fall back to query params next
        }

        // 2) Fallback: support /auth/forgot-password?email=...
        if (email.isEmpty()) {
            Map<String, String> query = Utils.parseQueryParams(exchange.getRequestURI().getQuery());
            email = query.getOrDefault("email", "").trim();
        }

        // 3) Basic input check (don’t enumerate! always generic response)
        if (email.isEmpty()) {
            Utils.sendResponse(exchange, 400, "Email is required.");
            return;
        }

        // 4) Issue token + send email (method is idempotent/generic externally)
        try {
            // Optional: add simple rate limit here if you have one
            AuthGuard.requestPasswordReset(email);
        } catch (Exception e) {
            // Log internally; generic success to caller to avoid enumeration
            e.printStackTrace();
        }

        // 5) Generic success response (same for existing/non-existing users)
        Utils.sendJsonResponse(
                exchange,
                Map.of("message", "If an account exists for that email, we’ve sent a reset link."),
                200
        );
    }
}
