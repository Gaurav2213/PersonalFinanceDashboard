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

public class AddBudgetsBatchHandler implements HttpHandler {

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
            Utils.sendResponse(exchange, 405, "Method Not Allowed");
            return;
        }

        try {
            List<Budget> budgets = Utils.parseRequestBodyList(
                exchange.getRequestBody(),
                new TypeReference<List<Budget>>() {}
            );

            int userId = (int) exchange.getAttribute("authUserId");
            BudgetResponse<List<Budget>> response = BudgetService.addBudgetsBatch(budgets, userId);

            Utils.sendJsonResponse(exchange, response, response.isSuccess() ? 200 : 400);

        } catch (com.fasterxml.jackson.databind.exc.InvalidFormatException e) {
            if (e.getMessage().contains("java.sql.Date")) {
                Utils.sendResponse(exchange, 400, "Invalid date format. Use 'yyyy-MM-dd'.");
            } else {
                Utils.sendResponse(exchange, 400, "Invalid value format in JSON: " + e.getOriginalMessage());
            }
        } catch (Exception e) {
            e.printStackTrace();
            Utils.sendResponse(exchange, 500, "Internal Server Error: " + e.getMessage());
        }
    }
}
