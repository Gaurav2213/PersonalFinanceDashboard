package util;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;

import model.Transaction;
import model.ValidationResult;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class Utils {

    /**
     * Parses a JSON request body into an object of the specified class.
     */
    public static <T> T parseRequestBody(InputStream is, Class<T> clazz) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(is, clazz);
    }

    /**
     * Parses a JSON request body into a generic object (like List<Transaction>) using TypeReference.
     */
    public static <T> T parseRequestBodyList(InputStream is, TypeReference<T> typeRef) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(is, typeRef);
    }

    /**
     * Sends a simple JSON response with the given HTTP status code and message.
     */
    public static void sendResponse(HttpExchange exchange, int statusCode, String message) throws IOException {
        byte[] responseBytes = message.getBytes(StandardCharsets.UTF_8);

        exchange.getResponseHeaders().set("Content-Type", "application/json");
        exchange.sendResponseHeaders(statusCode, responseBytes.length);

        OutputStream os = exchange.getResponseBody();
        os.write(responseBytes);
        os.close();
    }
    
    
    private static final int MAX_BATCH_SIZE = 100;

    //validate transaction batch size 
    public static ValidationResult validateTransactionBatchSize(List<Transaction> transactions) {
        if (transactions == null || transactions.isEmpty()) {
            return new ValidationResult(false, "Transaction list cannot be empty");
        }

        if (transactions.size() > MAX_BATCH_SIZE) {
            return new ValidationResult(false, "Batch size exceeds limit of " + MAX_BATCH_SIZE + " transactions.");
        }

        return new ValidationResult(true, "Valid batch size");
    }
}
