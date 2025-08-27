package controller.analytics;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import model.AnalyticsResponse;
import model.DateCumulativeSpending;
import service.AnalyticsService;
import util.Utils;

import java.io.IOException;
import java.util.Map;

public class MaxMinSpendingDaysHandler implements HttpHandler {
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
            Utils.sendResponse(exchange, 405, "Method Not Allowed");
            return;
        }

//        int userId;
//        try {
//            Map<String, String> queryParams = Utils.parseQueryParams(exchange.getRequestURI().getQuery());
//            userId = Integer.parseInt(queryParams.getOrDefault("userId", "0").trim());
//        } catch (Exception e) {
//            Utils.sendResponse(exchange, 400, "Invalid or missing userId");
//            return;
//        }
        int userId = (int) exchange.getAttribute("authUserId");

        AnalyticsResponse<Map<String, DateCumulativeSpending>> response = AnalyticsService.getMaxMinSpendingDays(userId);
        Utils.sendJsonResponse(exchange, response, response.isSuccess() ? 200 : 404);
    }
}

