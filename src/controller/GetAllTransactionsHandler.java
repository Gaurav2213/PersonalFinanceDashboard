package controller;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import model.Transaction;
import model.TransactionResponse;
import model.ValidationResult;
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

    
        int userId = (int) exchange.getAttribute("authUserId");

      
//fetching the transaction   
        TransactionResponse txResult = transactionService.getTransactionsByUser(userId);

        JSONObject responseJson = new JSONObject();
        responseJson.put("success", txResult.isSuccess());
        responseJson.put("message", txResult.getMessage());

        //validate if the transaction is existing or not then only create an json array object 
        if (txResult.isSuccess()) {
            JSONArray txArray = new JSONArray(
                txResult.getTransactions().stream().map(t -> {
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
            responseJson.put("transactions", txArray);
        }

     //   send response 
        byte[] responseBytes = responseJson.toString().getBytes();
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        exchange.sendResponseHeaders(200, responseBytes.length);
        OutputStream os = exchange.getResponseBody();
        os.write(responseBytes);
        os.close();

    }
}
