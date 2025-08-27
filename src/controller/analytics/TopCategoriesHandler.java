package controller.analytics;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import model.AnalyticsResponse;
import model.CategoryTotal;
import model.ValidationResult;
import service.AnalyticsService;
import util.Utils;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public class TopCategoriesHandler implements HttpHandler {

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
            Utils.sendResponse(exchange, 405, "Method Not Allowed");
            return;
        }

        Map<String, String> queryParams = Utils.parseQueryParams(exchange.getRequestURI().getQuery());

        int userId;
        int limit;

        try {
         //   userId = Integer.parseInt(queryParams.getOrDefault("userId", "0"));
        	 userId = (int) exchange.getAttribute("authUserId");
            limit = Integer.parseInt(queryParams.getOrDefault("limit", "5")); // default top 5
        } catch (NumberFormatException e) {
            Utils.sendResponse(exchange, 400, "Invalid query parameter format.");
            return;
        }

        // ✅ Centralized validation of userId, limit, and transactions
        ValidationResult validation = AnalyticsService.validateUserAnalyticsAccess(userId, limit);
        if (!validation.isValid()) {
            Utils.sendResponse(exchange, 400, validation.getMessage());
            return;
        }

        // ✅ Fetch top categories and send structured response
        AnalyticsResponse<List<CategoryTotal>> response = AnalyticsService.getTopSpendingCategories(userId, limit);

        if (!response.isSuccess()) {
            Utils.sendResponse(exchange, 404, response.getMessage());
            return;
        }
        Utils.sendJsonResponse(exchange, response, 200);
    }
}
