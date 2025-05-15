package controller;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import model.TransactionResponse;
import service.TransactionService;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.io.OutputStream;
import java.util.stream.Collectors;

/**
 * Handler to fetch transactions for a user filtered by category
 * Example: GET /transaction/category?userId=1&category=food
 */
public class GetTransactionsByCategoryHandler implements HttpHandler {

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

        // Extract query params
        String query = exchange.getRequestURI().getQuery();
        int userId = -1;
        String category = null;

        if (query != null) {
            String[] params = query.split("&");
            for (String param : params) {
                if (param.startsWith("userId=")) {
                    try {
                        userId = Integer.parseInt(param.split("=")[1]);
                    } catch (NumberFormatException e) {
                        userId = -1;
                    }
                } else if (param.startsWith("category=")) {
                    category = param.split("=")[1];
                }
            }
        }

        // Call service layer for validation and data
        TransactionResponse response = transactionService.getTransactionsByCategory(userId, category);

        // Build JSON response
        JSONObject responseJson = new JSONObject();
        responseJson.put("success", response.isSuccess());
        responseJson.put("message", response.getMessage());

        if (response.getTransactions() != null) {
            JSONArray jsonArray = new JSONArray(
                response.getTransactions().stream().map(t -> {
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
            responseJson.put("transactions", jsonArray);
        }

        byte[] responseBytes = responseJson.toString().getBytes();
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        exchange.sendResponseHeaders(200, responseBytes.length);

        OutputStream os = exchange.getResponseBody();
        os.write(responseBytes);
        os.close();
    }
}
