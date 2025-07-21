package controller.analytics;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import model.AnalyticsResponse;
import model.OverspendWarning;
import service.AnalyticsService;
import util.Utils;
import java.io.IOException;
import java.util.List;
import java.util.Map;

public class OverspendWarningHandler implements HttpHandler {

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        System.out.println("[OverspendWarningHandler] Request: " + exchange.getRequestURI());

        // ✅ Allow only GET requests
        if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
            Utils.sendResponse(exchange, 405, "Method Not Allowed");
            return;
        }

        // ✅ Parse query parameters
        Map<String, String> queryParams = Utils.parseQueryParams(exchange.getRequestURI().getQuery());

        int userId;
        try {
            userId = Integer.parseInt(queryParams.getOrDefault("userId", "0").trim());
        } catch (NumberFormatException e) {
            Utils.sendResponse(exchange, 400, "Invalid or missing userId");
            return;
        }

        // ✅ Call service
        AnalyticsResponse<List<OverspendWarning>> response = AnalyticsService.getOverspendWarnings(userId);

        if (!response.isSuccess()) {
            Utils.sendResponse(exchange, 404, response.getMessage());
        } else {
            Utils.sendJsonResponse(exchange, response, 200);
        }

        System.out.println("[OverspendWarningHandler] Completed.\n");
    }
}
