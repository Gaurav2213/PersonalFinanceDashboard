package controller;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import model.AuthResponse;
import org.json.JSONObject;
import util.AuthGuard;
import util.Utils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class ResetPasswordHandler implements HttpHandler {

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
            Utils.sendResponse(exchange, 405, "Method Not Allowed");
            return;
        }

        String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8).trim();
        String token = "";
        String newPassword = "";

        if (!body.isBlank()) {
            JSONObject json = new JSONObject(body);
            token = json.optString("token", "").trim();
            newPassword = json.optString("newPassword", "").trim();
        }

        if (token.isEmpty() || newPassword.isEmpty()) {
            Utils.sendJsonResponse(exchange, new AuthResponse<>(false, "Token and newPassword are required."), 400);
            return;
        }

        AuthResponse<Void> res = AuthGuard.resetPassword(token, newPassword);
        Utils.sendJsonResponse(exchange, res, res.isSuccess() ? 200 : 400);
    }
}
