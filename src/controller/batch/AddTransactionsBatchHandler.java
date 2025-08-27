package controller.batch;

import java.io.IOException;
import java.util.List;

import com.fasterxml.jackson.core.type.TypeReference;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import model.Transaction;
import model.ValidationResult;
import service.TransactionService;
import util.Utils;

public class AddTransactionsBatchHandler implements HttpHandler {

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if (!exchange.getRequestMethod().equalsIgnoreCase("POST")) {
            Utils.sendResponse(exchange, 405, "Method Not Allowed");
            return;
        }

        try {
            List<Transaction> transactions = Utils.parseRequestBodyList(
                exchange.getRequestBody(), new TypeReference<List<Transaction>>() {}
            );
            
            int userId = (int) exchange.getAttribute("authUserId");
            ValidationResult result = TransactionService.addTransactionsBatch(transactions ,  userId);
            Utils.sendResponse(exchange, result.isValid() ? 200 : 400, result.getMessage());

        } catch (com.fasterxml.jackson.databind.exc.InvalidFormatException e) {
            // Specific error for badly formatted date (or enum/type)
            if (e.getMessage().contains("java.sql.Date")) {
                Utils.sendResponse(exchange, 400, "Invalid date format. Please use 'yyyy-MM-dd'.");
            } else {
                Utils.sendResponse(exchange, 400, "Invalid value format in JSON: " + e.getOriginalMessage());
            }
        } catch (Exception e) {
            e.printStackTrace();  // Still useful for debugging
            Utils.sendResponse(exchange, 500, "Internal Server Error: " + e.getMessage());
        }
    }
}
