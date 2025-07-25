package controller.analytics;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import model.AnalyticsResponse;
import model.SpendingDistribution;
import service.AnalyticsService;
import util.Utils;

import java.io.IOException;
import java.util.Map;

public class SpendingDistributionHandler implements HttpHandler {

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
            Utils.sendResponse(exchange, 405, "Method Not Allowed");
            return;
        }

        // ✅ Parse userId from query params
        Map<String, String> queryParams = Utils.parseQueryParams(exchange.getRequestURI().getQuery());
        int userId;

        try {
            userId = Integer.parseInt(queryParams.getOrDefault("userId", "0").trim());
        } catch (NumberFormatException e) {
            Utils.sendResponse(exchange, 400, "Invalid or missing userId.");
            return;
        }

        // ✅ Call service
        AnalyticsResponse<SpendingDistribution> response = AnalyticsService.getSpendingDistribution(userId);

        if (!response.isSuccess()) {
            Utils.sendResponse(exchange, 404, response.getMessage());
        } else {
            Utils.sendJsonResponse(exchange, response, 200);
        }
    }
}
