package controller;

import com.sun.net.httpserver.HttpServer;

import controller.analytics.BudgetUtilizationHandler;
import controller.analytics.CategoryTrendHandler;
import controller.analytics.MaxMinSpendingDaysHandler;
import controller.analytics.OverspendWarningHandler;
import controller.analytics.PrefixSumSpendingHandler;
import controller.analytics.RecurringTransactionHandler;
import controller.analytics.SpendingSummaryHandler;
import controller.analytics.TopCategoriesHandler;
import controller.batch.AddTransactionsBatchHandler;
import controller.batch.DeleteTransactionsBatchHandler;
import controller.batch.GetBudgetsByUserHandler;
import controller.batch.UpdateTransactionsBatchHandler;
import controller.budget.AddBudgetHandler;
import controller.budget.AddBudgetsBatchHandler;
import controller.budget.DeleteBudgetHandler;
import controller.budget.DeleteBudgetsBatchHandler;
import controller.budget.GetBudgetsHandler;
import controller.budget.UpdateBudgetHandler;
import controller.budget.UpdateBudgetsBatchHandler;
import controller.common.ValidCategoriesHandler;

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

        // Register test end point
        server.createContext("/test", new TestHandler());
        
        
        //*****************users controller mapping
        server.createContext("/register", new RegisterHandler());
        server.createContext("/login", new LoginHandler());
        
        
        //******************single operation mapping
        server.createContext("/transaction/add",new AddTransactionHandler());
        server.createContext("/transaction/all", new GetAllTransactionsHandler());
        server.createContext("/transaction/category", new GetTransactionsByCategoryHandler());
        server.createContext("/transaction/update", new UpdateTransactionHandler());
        server.createContext("/transaction/delete", new DeleteTransactionHandler());
        
        
        //*********************batch operation mapping 
        server.createContext("/transactions/batch-add", new AddTransactionsBatchHandler());
        server.createContext("/transactions/batch-update", new UpdateTransactionsBatchHandler());
        server.createContext("/transactions/batch-delete", new DeleteTransactionsBatchHandler());
        

        //************* analytics service mapping
        server.createContext("/analytics/top-categories", new TopCategoriesHandler());
        server.createContext("/analytics/summary", new SpendingSummaryHandler());
        server.createContext("/analytics/budget-utilization", new BudgetUtilizationHandler());
        server.createContext("/analytics/prefix-sum", new PrefixSumSpendingHandler());
        server.createContext("/analytics/max-min-days", new MaxMinSpendingDaysHandler());
        server.createContext("/analytics/category-trend", new CategoryTrendHandler());
        server.createContext("/analytics/recurring", new RecurringTransactionHandler());
        server.createContext("/analytics/overspend-warnings", new OverspendWarningHandler());


        
        
        //**********************common categories validation mapping
        server.createContext("/analytics/categories", new ValidCategoriesHandler());
        
        //*******************budget service mapping 
        server.createContext("/budget/add", new AddBudgetHandler());
        server.createContext("/budget/update", new UpdateBudgetHandler());
        server.createContext("/budget/delete", new DeleteBudgetHandler());
        server.createContext("/budget/user", new GetBudgetsByUserHandler());
        server.createContext("/budget/all", new GetBudgetsHandler());
        
        //*******************budget batch functionality 
        server.createContext("/budget/batch-add", new AddBudgetsBatchHandler());
        server.createContext("/budget/batch-update", new UpdateBudgetsBatchHandler());
       
        server.createContext("/budget/batch-delete", new DeleteBudgetsBatchHandler());

       



        // Use fixed thread pool
        server.setExecutor(Executors.newFixedThreadPool(10));
        
        
    


        // Start server
        server.start();
        System.out.println("Server is running on port 8000");
    }

    // Custom test handler
    static class TestHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String response = " Hello! This is a response from your test endpoint.";
            exchange.sendResponseHeaders(200, response.length());
            OutputStream os = exchange.getResponseBody();
            os.write(response.getBytes());
            os.close();
        }
    }
}
