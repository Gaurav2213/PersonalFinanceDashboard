package util;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.Headers;

import model.Transaction;
import model.ValidationResult;


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
    
    // * Parses a query string into a Map of key-value pairs.
    public static Map<String, String> parseQueryParams(String query) {
        Map<String, String> queryParams = new HashMap<>();
        if (query == null || query.isEmpty()) return queryParams;

        String[] pairs = query.split("&");
        for (String pair : pairs) {
            String[] keyValue = pair.split("=");
            if (keyValue.length == 2) {
                queryParams.put(
                    URLDecoder.decode(keyValue[0], StandardCharsets.UTF_8),
                    URLDecoder.decode(keyValue[1], StandardCharsets.UTF_8)
                );
            }
        }
        return queryParams;
    }
    
    /**
     * Serializes any Java object (e.g., DTO, List, Map) into JSON and sends it as an HTTP response.
     * This should be used when returning complex data structures like lists or objects.
     */
    
    public static void sendJsonResponse(HttpExchange exchange, Object data, int statusCode) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        String json = mapper.writeValueAsString(data);  //  Serializes your List<> or DTO

        byte[] responseBytes = json.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        exchange.sendResponseHeaders(statusCode, responseBytes.length);

        OutputStream os = exchange.getResponseBody();
        os.write(responseBytes);
        os.close();
    }
    
    public static void addCorsHeaders(Headers headers) {
        headers.add("Access-Control-Allow-Origin",  "*"); // TODO: restrict to your frontend domain in prod
        headers.add("Access-Control-Allow-Headers", "Content-Type, Authorization");
        headers.add("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        headers.add("Access-Control-Expose-Headers", "Content-Type");
      }


}