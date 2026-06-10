package controller;

import java.io.IOException;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import model.AuthResponse;
import service.UserService;
import util.Utils;
import util.AuthGuard;

public class LogoutHandler implements HttpHandler {
    private final UserService userService = new UserService();

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
            Utils.sendResponse(exchange, 405,
                "{\"success\":false,\"message\":\"Method Not Allowed\",\"data\":null}");
            return;
        }

        // Guarded.protect() already verified token is valid — safe to extract directly
        String token = AuthGuard.extractBearerToken(exchange);
        AuthResponse<Object> resp = userService.logout(token);
        Utils.sendJsonResponse(exchange, resp, resp.isSuccess() ? 200 : 401);
    }
}
