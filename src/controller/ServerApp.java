package controller;

import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

public class ServerApp {
    public static void main(String[] args) throws IOException {
        // Create server on port 8000
        HttpServer server = HttpServer.create(new InetSocketAddress(8000), 0);

        // Register test endpoint
        server.createContext("/test", new TestHandler());
      
        server.createContext("/register", new RegisterHandler());

        server.createContext("/login", new LoginHandler());
        
        server.createContext("/transaction/add",new AddTransactionHandler());

        server.createContext("/transaction/all", new GetAllTransactionsHandler());
        
        server.createContext("/transaction/category", new GetTransactionsByCategoryHandler());
        server.createContext("/transaction/update", new UpdateTransactionHandler());

        server.createContext("/transaction/delete", new DeleteTransactionHandler());


        // Use fixed thread pool
        server.setExecutor(Executors.newFixedThreadPool(10));

        // Start server
        server.start();
        System.out.println("🚀 Server is running on port 8000");
    }

    // Custom test handler
    static class TestHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String response = "✅ Hello! This is a response from your test endpoint.";
            exchange.sendResponseHeaders(200, response.length());
            OutputStream os = exchange.getResponseBody();
            os.write(response.getBytes());
            os.close();
        }
    }
}
