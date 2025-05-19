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
        // Allow only POST requests
        if (!exchange.getRequestMethod().equalsIgnoreCase("POST")) {
            Utils.sendResponse(exchange, 405, "Method Not Allowed");
            return;
        }

        // Parse request body to List<Transaction>
        List<Transaction> transactions = Utils.parseRequestBodyList(
            exchange.getRequestBody(), new TypeReference<List<Transaction>>() {}
        );

        // Call service method to process batch add
        ValidationResult result = TransactionService.addTransactionsBatch(transactions);

        // Return success or error message
        Utils.sendResponse(exchange, result.isValid() ? 200 : 400, result.getMessage());
    }
}
