package controller;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import model.AuthResponse;
import model.User;
import model.ValidationResult;
import service.UserService;
import util.Utils;

import java.io.IOException;
import java.util.Map;

public class RegisterHandler implements HttpHandler {

    private final UserService userService = new UserService();

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if (!exchange.getRequestMethod().equalsIgnoreCase("POST")) {
            Utils.sendResponse(exchange, 405, "Method Not Allowed");
            return;
        }

        try {
            Map<String, Object> body = Utils.parseRequestBody(exchange.getRequestBody(), Map.class);
            String name     = body == null ? null : (String) body.get("name");
            String email    = body == null ? null : (String) body.get("email");
            String password = body == null ? null : (String) body.get("password");

            User user = new User(name, email, password);
            ValidationResult result = userService.register(user);

            AuthResponse<Void> response = new AuthResponse<>(result.isValid(), result.getMessage());

            int status;
            if (result.isValid()) {
                status = 200;
            } else if (result.getMessage() != null && result.getMessage().toLowerCase().contains("already registered")) {
                status = 409;
            } else {
                status = 400;
            }

            Utils.sendJsonResponse(exchange, response, status);

        } catch (Exception e) {
            e.printStackTrace();
            Utils.sendResponse(exchange, 500, "Internal Server Error: " + e.getMessage());
        }
    }
}
