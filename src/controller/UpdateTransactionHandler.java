package controller;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import model.Transaction;
import model.TransactionResponse;
import service.TransactionService;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.sql.Date;

public class UpdateTransactionHandler implements HttpHandler {

    private final TransactionService transactionService = new TransactionService();

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        // Allow only PUT method
        if (!exchange.getRequestMethod().equalsIgnoreCase("PUT")) {
            String error = "405 Method Not Allowed";
            exchange.sendResponseHeaders(405, error.length());
            OutputStream os = exchange.getResponseBody();
            os.write(error.getBytes());
            os.close();
            return;
        }

        // Read JSON request body
        BufferedReader reader = new BufferedReader(new InputStreamReader(exchange.getRequestBody()));
        StringBuilder jsonBuilder = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            jsonBuilder.append(line);
        }

        JSONObject requestJson = new JSONObject(jsonBuilder.toString());

        // Extract and map fields from request
        int transactionId = requestJson.getInt("id");
        int userId = requestJson.getInt("userId");
        String type = requestJson.getString("type");
        double amount = requestJson.getDouble("amount");
        String category = requestJson.getString("category");
        String description = requestJson.getString("description");
        Date date = Date.valueOf(requestJson.getString("date"));

        Transaction transaction = new Transaction(transactionId, userId, type, amount, category, description, date);

        // Call service
        TransactionResponse result = transactionService.updateTransaction(transaction);

        // Build response
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        JSONObject responseJson = new JSONObject();
        responseJson.put("success", result.isSuccess());
        responseJson.put("message", result.getMessage());

        byte[] responseBytes = responseJson.toString().getBytes();
        exchange.sendResponseHeaders(200, responseBytes.length);
        OutputStream os = exchange.getResponseBody();
        os.write(responseBytes);
        os.close();
    }
}
