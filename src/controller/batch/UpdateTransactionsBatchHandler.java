package controller.batch;

import com.fasterxml.jackson.core.type.TypeReference;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import model.Transaction;
import model.ValidationResult;
import service.TransactionService;
import util.Utils;

import java.io.IOException;
import java.util.List;
public class UpdateTransactionsBatchHandler implements HttpHandler {
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if (!exchange.getRequestMethod().equalsIgnoreCase("PUT")) {
            Utils.sendResponse(exchange, 405, "Method Not Allowed");
            return;
        }

        try {
            List<Transaction> transactions = Utils.parseRequestBodyList(
                exchange.getRequestBody(), new TypeReference<List<Transaction>>() {}
            );

            ValidationResult result = TransactionService.updateTransactionsBatch(transactions);
            Utils.sendResponse(exchange, result.isValid() ? 200 : 400, result.getMessage());

        } catch (Exception e) {
            e.printStackTrace();
            Utils.sendResponse(exchange, 500, "Internal Server Error: " + e.getMessage());
        }
    }
}