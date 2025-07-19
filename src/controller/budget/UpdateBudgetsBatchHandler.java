package controller.budget;

import com.fasterxml.jackson.core.type.TypeReference;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import model.Budget;
import model.BudgetResponse;
import service.BudgetService;
import util.Utils;

import java.io.IOException;
import java.util.List;

public class UpdateBudgetsBatchHandler implements HttpHandler {

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if (!"PUT".equalsIgnoreCase(exchange.getRequestMethod())) {
            Utils.sendResponse(exchange, 405, "Method Not Allowed");
            return;
        }

        try {
            //  Parse JSON array of budgets
            List<Budget> budgets = Utils.parseRequestBodyList(
                exchange.getRequestBody(), new TypeReference<List<Budget>>() {}
            );

            //  Call service
            BudgetResponse<List<Budget>> response = BudgetService.updateBudgetsBatch(budgets);

            //  Send response
            Utils.sendJsonResponse(exchange, response, response.isSuccess() ? 200 : 400);

        } catch (Exception e) {
            e.printStackTrace();
            Utils.sendResponse(exchange, 500, "Internal Server Error: " + e.getMessage());
        }
    }
}
