package controller.analytics;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import model.AnalyticsResponse;
import model.TimePeriodSpending;
import service.AnalyticsService;
import util.Utils;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public class SpendingSummaryHandler implements HttpHandler {

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
            Utils.sendResponse(exchange, 405, "Method Not Allowed");
            return;
        }

        Map<String, String> queryParams = Utils.parseQueryParams(exchange.getRequestURI().getQuery());

        int userId;
        String type = queryParams.getOrDefault("type", "monthly"); // default to monthly if not passed

        try {
            userId = Integer.parseInt(queryParams.getOrDefault("userId", "0"));
        } catch (NumberFormatException e) {
            Utils.sendResponse(exchange, 400, "Invalid userId format.");
            return;
        }

        // 🎯 Call the service method
        AnalyticsResponse<List<TimePeriodSpending>> response =
                AnalyticsService.getTimePeriodSpendingSummary(userId, type);

        if (!response.isSuccess()) {
            Utils.sendResponse(exchange, 404, response.getMessage());
        } else {
            Utils.sendJsonResponse(exchange, response, 200);
        }
    }
}
