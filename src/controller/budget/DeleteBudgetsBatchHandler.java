package controller.budget;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import model.BudgetResponse;
import model.BatchDeleteBudgetRequest;
import service.BudgetService;
import util.Utils;

import java.io.IOException;

public class DeleteBudgetsBatchHandler implements HttpHandler {

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if (!"DELETE".equalsIgnoreCase(exchange.getRequestMethod())) {
            Utils.sendResponse(exchange, 405, "Method Not Allowed");
            return;
        }

        try {
            // ✅ Parse JSON body to Java object
            BatchDeleteBudgetRequest request = Utils.parseRequestBody(
                exchange.getRequestBody(),
                BatchDeleteBudgetRequest.class
            );
            int userId = (int) exchange.getAttribute("authUserId");
            BudgetResponse<?> response = BudgetService.deleteBudgetsBatch(
                userId,
                request.getCategories()
            );

            Utils.sendJsonResponse(exchange, response, response.isSuccess() ? 200 : 400);

        } catch (Exception e) {
            e.printStackTrace();
            Utils.sendResponse(exchange, 500, "Internal Server Error: " + e.getMessage());
        }
    }
}
