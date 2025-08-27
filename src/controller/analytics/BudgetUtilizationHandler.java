package controller.analytics;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import model.AnalyticsResponse;
import model.BudgetUtilization;
import service.AnalyticsService;
import util.Utils;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public class BudgetUtilizationHandler implements HttpHandler {

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        System.out.println("🔔 Received request: " + exchange.getRequestURI());

        // ✅ Allow only GET requests
        if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
            System.out.println("❌ Invalid request method: " + exchange.getRequestMethod());
            Utils.sendResponse(exchange, 405, "Method Not Allowed");
            return;
        }

//        // ✅ Parse query parameters
//        Map<String, String> queryParams = Utils.parseQueryParams(exchange.getRequestURI().getQuery());
//        System.out.println("📦 Query parameters: " + queryParams);
//
//        int userId;
//        try {
//            userId = Integer.parseInt(queryParams.getOrDefault("userId", "0").trim());
//            System.out.println("🔍 Parsed userId: " + userId);
//        } catch (NumberFormatException e) {
//            System.out.println("❌ Invalid or missing userId");
//            Utils.sendResponse(exchange, 400, "Invalid or missing userId");
//            return;
//        }
        int userId =    (int) exchange.getAttribute("authUserId");
        // ✅ Call service layer
        AnalyticsResponse<List<BudgetUtilization>> response = AnalyticsService.getBudgetUtilization(userId);
        System.out.println("📊 Service response: success=" + response.isSuccess() + ", message=" + response.getMessage());

        // ✅ Send response
        if (!response.isSuccess()) {
            Utils.sendResponse(exchange, 404, response.getMessage());
        } else {
            Utils.sendJsonResponse(exchange, response, 200);
        }

        System.out.println("✅ Request processing completed.\n");
    }
}
