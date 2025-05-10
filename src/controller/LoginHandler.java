package controller;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import model.ValidationResult;
import service.UserService;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;

import org.json.JSONObject;

/**
 * Handler to process user login via POST /login
 * Accepts email and password in JSON format
 * Returns structured feedback using ValidationResult
 */
public class LoginHandler implements HttpHandler {

    private final UserService userService = new UserService();

    @Override
    public void handle(HttpExchange exchange) throws IOException {

        // Step 1: Allow only POST requests
        if (!exchange.getRequestMethod().equalsIgnoreCase("POST")) {
            String error = "405 Method Not Allowed";
            exchange.sendResponseHeaders(405, error.length());
            OutputStream os = exchange.getResponseBody();
            os.write(error.getBytes());
            os.close();
            return;
        }

        // Step 2: Read and parse JSON request body
        BufferedReader reader = new BufferedReader(new InputStreamReader(exchange.getRequestBody()));
        StringBuilder jsonBuilder = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            jsonBuilder.append(line);
        }

        JSONObject requestJson = new JSONObject(jsonBuilder.toString());
        String email = requestJson.getString("email");
        String password = requestJson.getString("password");

        // Step 3: Authenticate via service layer
        ValidationResult result = userService.loginWithValidation(email, password);

        // Step 4: Build and return JSON response
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
