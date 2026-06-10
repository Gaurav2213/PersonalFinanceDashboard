package controller;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import model.AuthResponse;
import org.json.JSONObject;
import util.AuthGuard;
import util.Utils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public class ForgotPasswordHandler implements HttpHandler {

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
            Utils.sendResponse(exchange, 405, "Method Not Allowed");
            return;
        }

        String email = "";

        try {
            byte[] bytes = exchange.getRequestBody().readAllBytes();
            String body = new String(bytes, StandardCharsets.UTF_8).trim();
            if (!body.isBlank()) {
                JSONObject json = new JSONObject(body);
                email = json.optString("email", "").trim();
            }
        } catch (Exception ignored) {
            // fall back to query params
        }

        if (email.isEmpty()) {
            Map<String, String> query = Utils.parseQueryParams(exchange.getRequestURI().getQuery());
            email = query.getOrDefault("email", "").trim();
        }

        if (email.isEmpty()) {
            Utils.sendJsonResponse(exchange, new AuthResponse<>(false, "Email is required."), 400);
            return;
        }

        try {
            AuthGuard.requestPasswordReset(email);
        } catch (Exception e) {
            e.printStackTrace();
        }

        Utils.sendJsonResponse(
            exchange,
            new AuthResponse<>(true, "If an account exists for that email, we've sent a reset link."),
            200
        );
    }
}
