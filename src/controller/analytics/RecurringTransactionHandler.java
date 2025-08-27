package controller.analytics;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import model.AnalyticsResponse;
import model.RecurringTransaction;
import service.AnalyticsService;
import util.Utils;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public class RecurringTransactionHandler implements HttpHandler {

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        System.out.println("[RecurringTransactionHandler] Request: " + exchange.getRequestURI());

        // ✅ Only allow GET
        if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
            Utils.sendResponse(exchange, 405, "Method Not Allowed");
            return;
        }

        // ✅ Parse query parameters
      //  Map<String, String> queryParams = Utils.parseQueryParams(exchange.getRequestURI().getQuery());
//
//        int userId;
//        try {
//            userId = Integer.parseInt(queryParams.getOrDefault("userId", "0").trim());
//        } catch (NumberFormatException e) {
//            Utils.sendResponse(exchange, 400, "Invalid or missing userId");
//            return;
//        }
        int userId = (int) exchange.getAttribute("authUserId");

        // ✅ Call service
        AnalyticsResponse<List<RecurringTransaction>> response =
                AnalyticsService.detectRecurringTransactions(userId);

        // ✅ Return response
        if (!response.isSuccess()) {
            Utils.sendResponse(exchange, 404, response.getMessage());
        } else {
            Utils.sendJsonResponse(exchange, response, 200);
        }

        System.out.println("✅ RecurringTransactionHandler completed.\n");
    }
}
