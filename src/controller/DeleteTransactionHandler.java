package controller;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import model.TransactionResponse;
import service.TransactionService;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;

/**
 * Handler for deleting a transaction.
 * Accepts DELETE requests with JSON body containing transactionId and userId.
 */
public class DeleteTransactionHandler implements HttpHandler {

    private final TransactionService transactionService = new TransactionService();

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        // Allow only DELETE method
        if (!exchange.getRequestMethod().equalsIgnoreCase("DELETE")) {
            String error = "405 Method Not Allowed";
            exchange.sendResponseHeaders(405, error.length());
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(error.getBytes());
            }
            return;
        }

        // Parse request body (expected JSON with transactionId and userId)
        BufferedReader reader = new BufferedReader(new InputStreamReader(exchange.getRequestBody()));
        StringBuilder jsonBuilder = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            jsonBuilder.append(line);
        }

        JSONObject requestJson = new JSONObject(jsonBuilder.toString());
        int transactionId = requestJson.getInt("transactionId");
        int userId = requestJson.getInt("userId");

        // Call service to delete
        TransactionResponse result = transactionService.deleteTransaction(transactionId, userId);

        // Build and send response
        JSONObject responseJson = new JSONObject();
        responseJson.put("success", result.isSuccess());
        responseJson.put("message", result.getMessage());
        byte[] responseBytes = responseJson.toString().getBytes();
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        exchange.sendResponseHeaders(200, responseBytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(responseBytes);
        }
    }
}
