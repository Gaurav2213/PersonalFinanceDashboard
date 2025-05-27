package controller.analytics;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import model.CategoryTotal;
import model.ValidationResult;
import service.AnalyticsService;
import util.Utils;

import java.io.IOException;
import java.io.OutputStream;
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
            userId = Integer.parseInt(queryParams.getOrDefault("userId", "0"));
            limit = Integer.parseInt(queryParams.getOrDefault("limit", "5")); // default top 5
        } catch (NumberFormatException e) {
            Utils.sendResponse(exchange, 400, "Invalid query parameter format.");
            return;
        }
        
      //  Validate user + data existence using helper
        ValidationResult validation = AnalyticsService.validateUserAnalyticsAccess(userId ,limit);
        if (!validation.isValid()) {
            Utils.sendResponse(exchange, 404, validation.getMessage());
            return;
        }
         //Get transactions & calculate top categories
        List<CategoryTotal> topCategories = AnalyticsService.getTopSpendingCategories(userId, limit);
        // no transaction found 
        if (topCategories.isEmpty()) {
            Utils.sendResponse(exchange, 200, "No expense transactions found for this user.");
            return;
        }

       // Send response
        Utils.sendJsonResponse(exchange, topCategories, 200);
    }
}
