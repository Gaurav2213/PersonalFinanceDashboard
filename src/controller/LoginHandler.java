package controller;

import java.io.IOException;
import java.util.Map;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import model.AuthResponse;
import model.LoginResponse;
import service.UserService;
import util.Utils;

public class LoginHandler implements HttpHandler {

    private final UserService userService = new UserService();

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
            Utils.sendResponse(exchange, 405, "Method Not Allowed");
            return;
        }

        try {
        	
            Map<String, Object> body = Utils.parseRequestBody(exchange.getRequestBody(), Map.class);
            String email = body == null ? null : (String) body.get("email");
            String password = body == null ? null : (String) body.get("password");

            AuthResponse<LoginResponse> response = userService.loginUser(email, password);
            Utils.sendJsonResponse(exchange, response, response.isSuccess() ? 200 : 401);

        } catch (Exception e) {
            e.printStackTrace();
            Utils.sendResponse(exchange, 500, "Internal Server Error: " + e.getMessage());
        }
    }
}
