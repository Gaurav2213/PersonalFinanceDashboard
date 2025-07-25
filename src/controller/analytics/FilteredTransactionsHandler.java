package controller.analytics;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import model.AnalyticsResponse;
import model.Transaction;
import service.AnalyticsService;
import util.Utils;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public class FilteredTransactionsHandler implements HttpHandler {

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
            Utils.sendResponse(exchange, 405, "Method Not Allowed");
            return;
        }

        Map<String, String> queryParams = Utils.parseQueryParams(exchange.getRequestURI().getQuery());

        try {
            int userId = Integer.parseInt(queryParams.getOrDefault("userId", "0").trim());

            double minAmount = Double.parseDouble(queryParams.getOrDefault("min", "0").trim());
            double maxAmount = Double.parseDouble(queryParams.getOrDefault("max", String.valueOf(Double.MAX_VALUE)).trim());

            String keyword = queryParams.getOrDefault("keyword", "").trim();
            String type = queryParams.getOrDefault("type", "").trim();  // "income" or "expense" or ""

            AnalyticsResponse<List<Transaction>> response =
                    AnalyticsService.filterTransactions(userId, minAmount, maxAmount, keyword, type);

            if (!response.isSuccess()) {
                Utils.sendResponse(exchange, 404, response.getMessage());
            } else {
                Utils.sendJsonResponse(exchange, response, 200);
            }

        } catch (NumberFormatException e) {
            Utils.sendResponse(exchange, 400, "Invalid User Id or amount .");
        } catch (Exception e) {
            e.printStackTrace();
            Utils.sendResponse(exchange, 500, "Internal Server Error: " + e.getMessage());
        }
    }
}
