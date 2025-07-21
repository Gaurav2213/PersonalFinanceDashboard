package controller.analytics;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import model.AnalyticsResponse;
import model.CategoryTrend;
import service.AnalyticsService;
import util.Utils;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public class CategoryTrendHandler implements HttpHandler {

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        System.out.println("[CategoryTrendHandler] Request: " + exchange.getRequestURI());

        // ✅ Only allow GET requests
        if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
            Utils.sendResponse(exchange, 405, "Method Not Allowed");
            return;
        }

        try {
            // ✅ Parse query parameters
            Map<String, String> queryParams = Utils.parseQueryParams(exchange.getRequestURI().getQuery());
            int userId = Integer.parseInt(queryParams.getOrDefault("userId", "0").trim());

            // ✅ Call service
            AnalyticsResponse<List<CategoryTrend>> response =
                    AnalyticsService.getCategoryTrendOverTime(userId);

            // ✅ Respond to client
            if (!response.isSuccess()) {
                Utils.sendResponse(exchange, 404, response.getMessage());
            } else {
                Utils.sendJsonResponse(exchange, response, 200);
            }

        } catch (NumberFormatException e) {
            Utils.sendResponse(exchange, 400, "Invalid userId format");
        } catch (Exception e) {
            e.printStackTrace();
            Utils.sendResponse(exchange, 500, "Internal Server Error: " + e.getMessage());
        }
    }
}
