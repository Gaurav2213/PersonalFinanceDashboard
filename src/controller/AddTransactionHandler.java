package controller;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import model.Transaction;
import model.ValidationResult;
import service.TransactionService;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.sql.Date;

import org.json.JSONObject;

/**
 * Handler to process adding a new transaction via POST /transaction/add
 * Accepts JSON body with: userId, type, amount, category, description, date
 */
public class AddTransactionHandler implements HttpHandler {

    private final TransactionService transactionService = new TransactionService();

    @Override
    public void handle(HttpExchange exchange) throws IOException {

        // Allow only POST requests
        if (!exchange.getRequestMethod().equalsIgnoreCase("POST")) {
            String error = "405 Method Not Allowed";
            exchange.sendResponseHeaders(405, error.length());
            OutputStream os = exchange.getResponseBody();
            os.write(error.getBytes());
            os.close();
            return;
        }

        // Read and build JSON request body
        BufferedReader reader = new BufferedReader(new InputStreamReader(exchange.getRequestBody()));
        StringBuilder jsonBuilder = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            jsonBuilder.append(line);
        }

        // Parse input fields
        JSONObject requestJson = new JSONObject(jsonBuilder.toString());
        int userId = (int) exchange.getAttribute("authUserId");
        String type = requestJson.getString("type");
        double amount = requestJson.getDouble("amount");
        String category = requestJson.getString("category");
        String description = requestJson.optString("description", "");
        Date date = Date.valueOf(requestJson.getString("date"));

        // Construct transaction object
        Transaction transaction = new Transaction(0, userId, type, amount, category, description, date);

        // Validate and save
        ValidationResult result = transactionService.addTransaction(transaction);

        // Send JSON response
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        JSONObject responseJson = new JSONObject();
        responseJson.put("success", result.isValid());
        responseJson.put("message", result.getMessage());

        byte[] responseBytes = responseJson.toString().getBytes();
        exchange.sendResponseHeaders(200, responseBytes.length);
        OutputStream os = exchange.getResponseBody();
        os.write(responseBytes);
        os.close();
    }
}
