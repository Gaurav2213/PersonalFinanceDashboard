package controller.budget;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import model.BudgetResponse;
import service.BudgetService;
import util.Utils;

import java.io.IOException;
import java.util.Map;

public class DeleteBudgetHandler implements HttpHandler {

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        System.out.println("Received DELETE request for /budget/delete");

        if (!"DELETE".equalsIgnoreCase(exchange.getRequestMethod())) {
            Utils.sendResponse(exchange, 405, "Method Not Allowed");
            return;
        }

        Map<String, String> queryParams = Utils.parseQueryParams(exchange.getRequestURI().getQuery());
        System.out.println("Query params: " + queryParams);

      //  int userId;
        String category = queryParams.getOrDefault("category", "").trim();
       

//        try {
//            userId = Integer.parseInt(queryParams.getOrDefault("userId", "0"));
//        } catch (NumberFormatException e) {
//            Utils.sendResponse(exchange, 400, "Invalid or missing userId");
//            return;
//        }
        int userId = (int) exchange.getAttribute("authUserId");

        // ✅ Call service
        BudgetResponse<String> response = BudgetService.deleteBudget(userId, category);
        System.out.println("Service response: " + response.getMessage());

        int statusCode = response.isSuccess() ? 200 : 404;
        Utils.sendResponse(exchange, statusCode, response.getMessage());

        System.out.println("DELETE completed\n");
    }
}
