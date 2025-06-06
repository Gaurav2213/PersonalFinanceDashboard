package controller.common;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import util.Utils;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class ValidCategoriesHandler implements HttpHandler {

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
            Utils.sendResponse(exchange, 405, "Method Not Allowed");
            return;
        }

        List<String> categories = Arrays.asList(
            "food", "transport", "utilities", "shopping",
            "health", "salary", "entertainment", "other"
        );

        Utils.sendJsonResponse(exchange, categories, 200);
    }
}
