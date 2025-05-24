package controller.batch;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import model.BatchDeleteRequest;
import model.ValidationResult;
import service.TransactionService;
import util.Utils;

import java.io.IOException;

public class DeleteTransactionsBatchHandler implements HttpHandler {

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if (!exchange.getRequestMethod().equalsIgnoreCase("DELETE")) {
            Utils.sendResponse(exchange, 405, "Method Not Allowed");
            return;
        }

        try {
            // ✅ Use reusable utility method to parse JSON request body
            BatchDeleteRequest request = Utils.parseRequestBody(exchange.getRequestBody(), BatchDeleteRequest.class);

            ValidationResult result = TransactionService.deleteTransactionsBatch(
                request.getUserId(),
                request.getTransactionIds()
            );

            Utils.sendResponse(exchange, result.isValid() ? 200 : 400, result.getMessage());

        } catch (Exception e) {
            e.printStackTrace();
            Utils.sendResponse(exchange, 500, "Internal Server Error: " + e.getMessage());
        }
    }
}
