package controller.budget;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import model.Budget;
import model.BudgetResponse;
import service.BudgetService;
import util.Utils;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public class GetBudgetsHandler implements HttpHandler {

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        System.out.println("📥 Request received: " + exchange.getRequestURI());

        // ✅ Allow only GET requests
        if (!exchange.getRequestMethod().equalsIgnoreCase("GET")) {
            Utils.sendResponse(exchange, 405, "Method Not Allowed");
            return;
        }

        // ✅ Extract and validate query parameters
        Map<String, String> queryParams = Utils.parseQueryParams(exchange.getRequestURI().getQuery());
        int userId;
        try {
            userId = Integer.parseInt(queryParams.getOrDefault("userId", "0").trim());
        } catch (NumberFormatException e) {
            Utils.sendResponse(exchange, 400, "Invalid or missing userId");
            return;
        }

        // ✅ Call service
        BudgetResponse<List<Budget>> response = BudgetService.getBudgetsByUser(userId);
        System.out.println("✅ Service result: success=" + response.isSuccess() + ", message=" + response.getMessage());

        // ✅ Send response
        if (!response.isSuccess()) {
            Utils.sendResponse(exchange, 404, response.getMessage());
        } else {
            Utils.sendJsonResponse(exchange, response, 200);
        }

        System.out.println("📤 Response sent.\n");
    }
}
