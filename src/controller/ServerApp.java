package controller;

import com.sun.net.httpserver.HttpServer;

import controller.analytics.BudgetUtilizationHandler;
import controller.analytics.CategoryTrendHandler;
import controller.analytics.FilteredTransactionsHandler;
import controller.analytics.MaxMinSpendingDaysHandler;
import controller.analytics.OverspendWarningHandler;
import controller.analytics.PrefixSumSpendingHandler;
import controller.analytics.RecurringTransactionHandler;
import controller.analytics.SpendingDistributionHandler;
import controller.analytics.SpendingSummaryHandler;
import controller.analytics.TopCategoriesHandler;
import controller.batch.AddTransactionsBatchHandler;
import controller.batch.DeleteTransactionsBatchHandler;
import controller.batch.UpdateTransactionsBatchHandler;
import controller.budget.AddBudgetHandler;
import controller.budget.AddBudgetsBatchHandler;
import controller.budget.DeleteBudgetHandler;
import controller.budget.DeleteBudgetsBatchHandler;
import controller.budget.GetBudgetsByUserHandler;
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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import util.Guarded;

public class ServerApp {
	private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger("probe");


    public static void main(String[] args) throws IOException {
    	log.info("splunk_probe");
    	try {
    	    Class.forName("io.jsonwebtoken.Claims");
    	    System.out.println("✅ jjwt-api is on the runtime classpath!");
    	} catch (ClassNotFoundException e) {
    	    System.out.println("❌ jjwt-api NOT on the runtime classpath!");
    	    e.printStackTrace();
    	}
    	System.out.println(System.getProperty("java.class.path"));

        // Create server on port 8000
        HttpServer server = HttpServer.create(new InetSocketAddress(8085), 0);

        // Register test end point (PUBLIC)
        server.createContext("/test", new TestHandler());

        //*****************users controller mapping (PUBLIC)
        server.createContext("/login",            Guarded.open(ex -> new LoginHandler().handle(ex)));
        server.createContext("/register",         Guarded.open(ex -> new RegisterHandler().handle(ex)));
        
        //************************* verification email (PUBLIC)
        server.createContext("/verify-email",   Guarded.open(ex -> new VerifyEmailHandler().handle(ex)));
        server.createContext("/auth/resend-verification",Guarded.open(ex ->  new ResendVerificationHandler().handle(ex)));

        
      //*****************users  token session extension (PUBLIC)
        server.createContext("/auth/refresh", Guarded.open(ex -> new RefreshTokenHandler().handle(ex)));
        
        //*****************users  forgot and reset password  (PUBLIC)
        server.createContext("/auth/forgot-password",Guarded.open(ex -> new controller.ForgotPasswordHandler().handle(ex)));
        server.createContext("/auth/reset-password", Guarded.open(ex -> new controller.ResetPasswordHandler().handle(ex)));
 
        
        
        //*****************users controller mapping (PROTECTED)
        server.createContext("/logout",
        	    Guarded.protect((exchange, claims) -> new LogoutHandler().handle(exchange)));

        //******************single operation mapping (PROTECTED)
        server.createContext("/transaction/add",
            Guarded.protect((exchange, claims) -> new AddTransactionHandler().handle(exchange)));
        server.createContext("/transaction/all",
            Guarded.protect((exchange, claims) -> new GetAllTransactionsHandler().handle(exchange)));
        server.createContext("/transaction/category",
            Guarded.protect((exchange, claims) -> new GetTransactionsByCategoryHandler().handle(exchange)));
        server.createContext("/transaction/update",
            Guarded.protect((exchange, claims) -> new UpdateTransactionHandler().handle(exchange)));
        server.createContext("/transaction/delete",
            Guarded.protect((exchange, claims) -> new DeleteTransactionHandler().handle(exchange)));

        //*********************batch operation mapping (PROTECTED)
        server.createContext("/transactions/batch-add",
            Guarded.protect((exchange, claims) -> new AddTransactionsBatchHandler().handle(exchange)));
        server.createContext("/transactions/batch-update",
            Guarded.protect((exchange, claims) -> new UpdateTransactionsBatchHandler().handle(exchange)));
        server.createContext("/transactions/batch-delete",
            Guarded.protect((exchange, claims) -> new DeleteTransactionsBatchHandler().handle(exchange)));

        //************* analytics service mapping (PROTECTED)
        server.createContext("/analytics/top-categories",
            Guarded.protect((exchange, claims) -> new TopCategoriesHandler().handle(exchange)));
        server.createContext("/analytics/summary",
            Guarded.protect((exchange, claims) -> new SpendingSummaryHandler().handle(exchange)));
        server.createContext("/analytics/budget-utilization",
            Guarded.protect((exchange, claims) -> new BudgetUtilizationHandler().handle(exchange)));
        server.createContext("/analytics/prefix-sum",
            Guarded.protect((exchange, claims) -> new PrefixSumSpendingHandler().handle(exchange)));
        server.createContext("/analytics/max-min-days",
            Guarded.protect((exchange, claims) -> new MaxMinSpendingDaysHandler().handle(exchange)));
        server.createContext("/analytics/category-trend",
            Guarded.protect((exchange, claims) -> new CategoryTrendHandler().handle(exchange)));
        server.createContext("/analytics/recurring",
            Guarded.protect((exchange, claims) -> new RecurringTransactionHandler().handle(exchange)));
        server.createContext("/analytics/overspend-warnings",
            Guarded.protect((exchange, claims) -> new OverspendWarningHandler().handle(exchange)));
        server.createContext("/analytics/filter",
            Guarded.protect((exchange, claims) -> new FilteredTransactionsHandler().handle(exchange)));
        server.createContext("/analytics/spending-distribution",
            Guarded.protect((exchange, claims) -> new SpendingDistributionHandler().handle(exchange)));

        //**********************common categories validation mapping (PROTECTED)
        server.createContext("/analytics/categories",
            Guarded.protect((exchange, claims) -> new ValidCategoriesHandler().handle(exchange)));

        //*******************budget service mapping (PROTECTED)
        server.createContext("/budget/add",
            Guarded.protect((exchange, claims) -> new AddBudgetHandler().handle(exchange)));
        server.createContext("/budget/update",
            Guarded.protect((exchange, claims) -> new UpdateBudgetHandler().handle(exchange)));
        server.createContext("/budget/delete",
            Guarded.protect((exchange, claims) -> new DeleteBudgetHandler().handle(exchange)));
        server.createContext("/budget/user",
            Guarded.protect((exchange, claims) -> new GetBudgetsByUserHandler().handle(exchange)));
        server.createContext("/budget/all",
            Guarded.protect((exchange, claims) -> new GetBudgetsHandler().handle(exchange)));

        //*******************budget batch functionality (PROTECTED)
        server.createContext("/budget/batch-add",
            Guarded.protect((exchange, claims) -> new AddBudgetsBatchHandler().handle(exchange)));
        server.createContext("/budget/batch-update",
            Guarded.protect((exchange, claims) -> new UpdateBudgetsBatchHandler().handle(exchange)));
        server.createContext("/budget/batch-delete",
            Guarded.protect((exchange, claims) -> new DeleteBudgetsBatchHandler().handle(exchange)));

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
