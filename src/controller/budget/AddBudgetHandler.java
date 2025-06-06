package controller.budget;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import model.Budget;
import model.BudgetResponse;
import service.BudgetService;
import util.Utils;

import java.io.IOException;

public class AddBudgetHandler implements HttpHandler {

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        System.out.println("Received request: " + exchange.getRequestURI());

        // ✅ Allow only POST requests
        if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
            Utils.sendResponse(exchange, 405, "Method Not Allowed");
            return;
        }

        // ✅ Parse JSON request body to Budget object
        Budget budget;
        try {
            budget = Utils.parseRequestBody(exchange.getRequestBody(), Budget.class);
        } catch (Exception e) {
            System.out.println("Failed to parse budget: " + e.getMessage());
            Utils.sendResponse(exchange, 400, "Invalid request body format");
            return;
        }

        // ✅ Call service layer
        BudgetResponse<Budget> response = BudgetService.addBudget(budget);
        System.out.println("Service response: success=" + response.isSuccess() + ", message=" + response.getMessage());

        // ✅ Send final response
        int statusCode = response.isSuccess() ? 201 : 400;
        Utils.sendJsonResponse(exchange, response, statusCode);
    }
}
