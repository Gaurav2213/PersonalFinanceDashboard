package controller.analytics;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import model.AnalyticsResponse;
import model.DateCumulativeSpending;
import service.AnalyticsService;
import util.Utils;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public class PrefixSumSpendingHandler implements HttpHandler {

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        System.out.println(" [PrefixSumSpendingHandler] Request: " + exchange.getRequestURI());

        if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
            Utils.sendResponse(exchange, 405, "Method Not Allowed");
            return;
        }

        Map<String, String> queryParams = Utils.parseQueryParams(exchange.getRequestURI().getQuery());

        int userId;
        String type = queryParams.getOrDefault("type", "daily").trim().toLowerCase(); // default = daily

        try {
            userId = Integer.parseInt(queryParams.getOrDefault("userId", "0"));
        } catch (NumberFormatException e) {
            Utils.sendResponse(exchange, 400, "Invalid or missing userId");
            return;
        }

        try {
        //  Call the service method
        AnalyticsResponse<List<DateCumulativeSpending>> response =
                AnalyticsService.getPrefixSumSpending(userId, type);

        //  Send response
        if (!response.isSuccess()) {
            Utils.sendResponse(exchange, 404, response.getMessage());
        } else {
            Utils.sendJsonResponse(exchange, response, 200);
        }
        }catch(Exception e)
        { 
        	 e.printStackTrace();
        	    Utils.sendResponse(exchange, 500, "Server error: " + e.getMessage());
        }

        System.out.println("✅ PrefixSumSpendingHandler completed.\n");
    }
}

