package controller;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import dao.UserDAO;
import model.EmailRequest;
import model.User;
import util.EmailService;
import util.TokenUtils;
import util.Utils;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONObject;

public class ResendVerificationHandler implements HttpHandler {

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
            Utils.sendResponse(exchange, 405, "Method Not Allowed");
            return;
        }

        EmailRequest req = Utils.parseRequestBody(exchange.getRequestBody(), EmailRequest.class);
        String email = req.getEmail() != null ? req.getEmail().trim() : "";

        if (email.isEmpty()) {
            Utils.sendResponse(exchange, 400, "Missing or invalid email.");
            return;
        }

        User user = UserDAO.getUserByEmail(email);
        if (user == null) {
            Utils.sendResponse(exchange, 404, "User not found.");
            return;
        }

        if (user.isVerified()) {
            Utils.sendResponse(exchange, 409, "Email is already verified.");
            return;
        }

        // ✅ Generate new verification token + expiry
        String newToken = TokenUtils.generateToken();
        Timestamp newExpiry = TokenUtils.generateExpiry(1); // expires in 1 hour

        boolean updated = UserDAO.updateVerificationToken(user.getId(), newToken, newExpiry);
        if (!updated) {
            Utils.sendResponse(exchange, 500, "Failed to update verification token.");
            return;
        }

        boolean emailSent = EmailService.sendVerificationEmail(user.getEmail(), user.getName(), newToken);
        if (!emailSent) {
            Utils.sendResponse(exchange, 500, "Failed to send verification email.");
            return;
        }

        Map<String, Object> res = new HashMap<>();
        res.put("success", true);
        res.put("message", "Verification email resent successfully.");

        Utils.sendJsonResponse(exchange, res, 200);
    }
}
