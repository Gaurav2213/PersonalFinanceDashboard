package controller;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import model.User;
import model.ValidationResult;
import service.UserService;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;

import org.json.*;

/**
 * Handler class to process HTTP POST requests for user registration.
 * Expects JSON input with 'name', 'email', and 'password'.
 * Returns a structured JSON response with success status and message.
 */
public class RegisterHandler implements HttpHandler {

    // Initialize the UserService to handle registration logic
    private final UserService userService = new UserService();

    /**
     * Main handler method called when /register endpoint receives a request.
     */
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        
        // Step 1: Only allow POST method
        if (!exchange.getRequestMethod().equalsIgnoreCase("POST")) {
            String error = "405 Method Not Allowed";
            exchange.sendResponseHeaders(405, error.length());
            OutputStream os = exchange.getResponseBody();
            os.write(error.getBytes());
            os.close();
            return;
        }

        // Step 2: Read the raw JSON input from the request body
        BufferedReader reader = new BufferedReader(new InputStreamReader(exchange.getRequestBody()));
        StringBuilder jsonBuilder = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            jsonBuilder.append(line); // Append each line to build the full JSON string
        }

        // Step 3: Parse the JSON request into usable variables
        JSONObject requestJson = new JSONObject(jsonBuilder.toString());
        String name = requestJson.getString("name");
        String email = requestJson.getString("email");
        String password = requestJson.getString("password");

        // Step 4: Create a User object and pass it to the service layer for registration
        User user = new User(name, email, password);
        ValidationResult result = userService.register(user); // Returns success flag and message

        // Step 5: Build the response JSON with validation result
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        JSONObject responseJson = new JSONObject();
        responseJson.put("success", result.isValid());
        responseJson.put("message", result.getMessage());

        // Step 6: Send the JSON response back to the client
        byte[] responseBytes = responseJson.toString().getBytes();
        exchange.sendResponseHeaders(200, responseBytes.length); // HTTP 200 OK
        OutputStream os = exchange.getResponseBody();
        os.write(responseBytes); // Write the response
        os.close(); // Always close the output stream
    }
}
