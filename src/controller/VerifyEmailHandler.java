package controller;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import dao.UserDAO;
import model.User;
import util.Utils;

import java.io.IOException;
import java.io.OutputStream;
import java.sql.Timestamp;
import java.util.Map;

import org.json.JSONObject;

public class VerifyEmailHandler implements HttpHandler {

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        // ✅ Only allow GET
        if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
            Utils.sendResponse(exchange, 405, "Method Not Allowed");
            return;
        }

        // ✅ Parse token from query string
        String query = exchange.getRequestURI().getQuery();
        Map<String, String> params = Utils.parseQueryParams(query);

        if (!params.containsKey("token") || params.get("token").trim().isEmpty()) {
            Utils.sendResponse(exchange, 400, "Missing or invalid token.");
            return;
        }

        String token = params.get("token");

        // ✅ Fetch user by token
        User user = UserDAO.getUserByVerificationToken(token);
        if (user == null) {
            Utils.sendResponse(exchange, 404, "Invalid or expired verification token.");
            return;
        }

        // ✅ Check expiry
        Timestamp now = new Timestamp(System.currentTimeMillis());
        if (user.getEmailVerificationTokenExpires() == null || now.after(user.getEmailVerificationTokenExpires())) {
            Utils.sendResponse(exchange, 410, "Verification link has expired.");
            return;
        }

        // ✅ Mark user as verified
        boolean updated = UserDAO.markUserAsVerified(user.getId());
        if (updated) {
            Utils.sendResponse(exchange, 200, "Email verified successfully. You can now log in.");
        } else {
            Utils.sendResponse(exchange, 500, "Failed to verify email. Please try again.");
        }
    }
}
