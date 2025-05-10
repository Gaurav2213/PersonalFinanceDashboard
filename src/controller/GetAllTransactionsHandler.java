package controller;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import model.Transaction;
import service.TransactionService;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.stream.Collectors;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Handler to fetch all transactions for a given userId
 * Example: GET /transaction/all?userId=1
 */
public class GetAllTransactionsHandler implements HttpHandler {

    private final TransactionService transactionService = new TransactionService();

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if (!exchange.getRequestMethod().equalsIgnoreCase("GET")) {
            String error = "405 Method Not Allowed";
            exchange.sendResponseHeaders(405, error.length());
            OutputStream os = exchange.getResponseBody();
            os.write(error.getBytes());
            os.close();
            return;
        }

        // Parse query param: userId
        String query = exchange.getRequestURI().getQuery();
        int userId = -1;
        if (query != null && query.contains("userId=")) {
            try {
                userId = Integer.parseInt(query.split("userId=")[1]);
            } catch (NumberFormatException e) {
                userId = -1;
            }
        }

        if (userId <= 0) {
            String error = "Invalid or missing userId";
            exchange.sendResponseHeaders(400, error.length());
            OutputStream os = exchange.getResponseBody();
            os.write(error.getBytes());
            os.close();
            return;
        }

        // Fetch transactions
        List<Transaction> transactions = transactionService.getTransactionsByUser(userId);

        // Convert to JSON array
        JSONArray jsonArray = new JSONArray(
            transactions.stream().map(t -> {
                JSONObject obj = new JSONObject();
                obj.put("id", t.getId());
                obj.put("type", t.getType());
                obj.put("amount", t.getAmount());
                obj.put("category", t.getCategory());
                obj.put("description", t.getDescription());
                obj.put("date", t.getDate().toString());
                return obj;
            }).collect(Collectors.toList())
        );

        // Send response
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        byte[] responseBytes = jsonArray.toString().getBytes();
        exchange.sendResponseHeaders(200, responseBytes.length);
        OutputStream os = exchange.getResponseBody();
        os.write(responseBytes);
        os.close();
    }
}
