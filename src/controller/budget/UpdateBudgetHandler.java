package controller.budget;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import model.Budget;
import model.BudgetResponse;
import service.BudgetService;
import util.Utils;

import java.io.IOException;

public class UpdateBudgetHandler implements HttpHandler {

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        System.out.println(" Received request to update budget");

        // Allow only PUT requests
        if (!"PUT".equalsIgnoreCase(exchange.getRequestMethod())) {
            System.out.println(" Invalid method: " + exchange.getRequestMethod());
            Utils.sendResponse(exchange, 405, "Method Not Allowed");
            return;
        }

        // Parse JSON input
        Budget budget = Utils.parseRequestBody(exchange.getRequestBody(), Budget.class);
        System.out.println(" Parsed budget input: " + budget);

        // Call service
        BudgetResponse<String> response = BudgetService.updateBudget(budget);
        System.out.println("📊 Service result: " + response.getMessage());

        // Send appropriate response
        int statusCode = response.isSuccess() ? 200 : 400;
        Utils.sendResponse(exchange, statusCode, response.getMessage());
    }
}
