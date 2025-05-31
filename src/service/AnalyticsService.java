package service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import dao.BudgetDAO;
import dao.TransactionDAO;
import model.AnalyticsResponse;
import model.BudgetUtilization;
import model.CategoryTotal;
import model.TimePeriodSpending;
import model.Transaction;
import model.ValidationResult;

public class AnalyticsService {
	  //validate the user id and check for the existence of transaction and limit validation related to user id 
	public static ValidationResult validateUserAnalyticsAccess(int userId ,int limit) {
		
	    ValidationResult userValidation = new TransactionService().validateUserId(userId);
	    if (!userValidation.isValid()) return userValidation;
	    
	    // Validate limit
	    if (limit <= 0) {
	        return new ValidationResult(false, "Limit must be greater than 0.");
	    }


	    List<Transaction> transactions = TransactionDAO.getTransactionsByUserId(userId);
	    if (transactions.isEmpty()) {
	        return new ValidationResult(false, "No transactions found for this user.");
	    }

	    return new ValidationResult(true, "valid");
	}

	//overload the validateUserAnalyticalTransaction
	public static ValidationResult validateUserAnalyticsAccess(int userId) {
	    ValidationResult userValidation = new TransactionService().validateUserId(userId);
	    if (!userValidation.isValid()) return userValidation;

	    List<Transaction> transactions = TransactionDAO.getTransactionsByUserId(userId);
	    if (transactions.isEmpty()) {
	        return new ValidationResult(false, "No transactions found for this user.");
	    }

	    return new ValidationResult(true, "valid");
	}
	
	
	//helper method to validate the transaction id and yser id and mapping total expense based on category and validate expense transaction existence
	public static AnalyticsResponse<Map<String, Double>> getExpenseTotalsByCategory(int userId) {
	    // ✅ Validate user & transactions
	    ValidationResult validation = validateUserAnalyticsAccess(userId);
	    if (!validation.isValid()) {
	        return new AnalyticsResponse<Map<String, Double>>(false, validation.getMessage(), new HashMap<>());
	    }

	    List<Transaction> transactions = TransactionDAO.getTransactionsByUserId(userId);

	    Map<String, Double> totals = new HashMap<>();
	    for (Transaction tx : transactions) {
	        if ("expense".equalsIgnoreCase(tx.getType())) {
	            String category = tx.getCategory().toLowerCase();
	            totals.put(category, totals.getOrDefault(category, 0.0) + tx.getAmount());
	        }
	    }

	    if (totals.isEmpty()) {
	        return new AnalyticsResponse<Map<String, Double>>(false, "No expense transactions found for this user.", new HashMap<>());
	    }

	    return new AnalyticsResponse<Map<String, Double>>(true, "Expense totals by category retrieved.", totals);
	}


	
	 

    /**
     * Get top N spending categories by total expense amount
     */
	public static AnalyticsResponse<List<CategoryTotal>> getTopSpendingCategories(int userId, int limit) {
	    AnalyticsResponse<Map<String, Double>> expenseResponse = getExpenseTotalsByCategory(userId);

	    if (!expenseResponse.isSuccess()) {
	        return new AnalyticsResponse<>(false, expenseResponse.getMessage(), new ArrayList<>());
	    }

	    Map<String, Double> categoryTotals = expenseResponse.getData();

	    List<CategoryTotal> topCategories = categoryTotals.entrySet().stream()
	        .map(entry -> new CategoryTotal(entry.getKey(), entry.getValue()))
	        .sorted((a, b) -> Double.compare(b.getTotal(), a.getTotal()))
	        .limit(limit)
	        .collect(Collectors.toList());

	    return new AnalyticsResponse<>(true, "Top spending categories retrieved successfully.", topCategories);
	}

	
	
	//get spending summary based on monthly or yearly per category
	public static AnalyticsResponse<List<TimePeriodSpending>> getTimePeriodSpendingSummary(int userId, String type) {
	    // ✅ Validate user and transaction existence
	    ValidationResult validation = AnalyticsService.validateUserAnalyticsAccess(userId);
	    if (!validation.isValid()) {
	        return new AnalyticsResponse<List<TimePeriodSpending>>(false, validation.getMessage(), new ArrayList<>());
	    }

	    // ✅ Validate the 'type'
	    if (!type.equalsIgnoreCase("monthly") && !type.equalsIgnoreCase("yearly")) {
	        return new AnalyticsResponse<List<TimePeriodSpending>>(false, "Invalid type. Use 'monthly' or 'yearly'.", new ArrayList<>());
	    }

	    // ✅ Fetch all transactions
	    List<Transaction> transactions = TransactionDAO.getTransactionsByUserId(userId);

	    // ✅ Filter only expense transactions
	    List<Transaction> expenses = transactions.stream()
	            .filter(tx -> "expense".equalsIgnoreCase(tx.getType()))
	            .collect(Collectors.toList());

	    if (expenses.isEmpty()) {
	        return new AnalyticsResponse<List<TimePeriodSpending>>(false, "No expense transactions found for this user.", new ArrayList<>());
	    }

	    // ✅ Group by period (month or year)
	    Map<String, Double> grouped = new HashMap<>();
	    for (Transaction tx : expenses) {
	        java.sql.Date date = tx.getDate();
	        String period;

	        if ("monthly".equalsIgnoreCase(type)) {
	            period = date.toLocalDate().getYear() + "-" + String.format("%02d", date.toLocalDate().getMonthValue());
	        } else {
	            period = String.valueOf(date.toLocalDate().getYear());
	        }

	        grouped.put(period, grouped.getOrDefault(period, 0.0) + tx.getAmount());
	    }

	    // ✅ Convert to list
	    List<TimePeriodSpending> results = grouped.entrySet().stream()
	            .map(e -> new TimePeriodSpending(e.getKey(), e.getValue()))
	            .sorted((a, b) -> b.getPeriod().compareTo(a.getPeriod()))
	            .collect(Collectors.toList());

	    String successMsg = "Spending summary (" + type + ") retrieved successfully.";
	    return new AnalyticsResponse<List<TimePeriodSpending>>(true, successMsg, results);
	}

	
	 // Calculates how much of the user's budget has been utilized for each category.
	public static AnalyticsResponse<List<BudgetUtilization>> getBudgetUtilization(int userId) {
	    AnalyticsResponse<Map<String, Double>> expenseResponse = getExpenseTotalsByCategory(userId);

	    if (!expenseResponse.isSuccess()) {
	        return new AnalyticsResponse<>(false, expenseResponse.getMessage(), new ArrayList<>());
	    }

	    //get total spent corresponding to category  of the user 
	    Map<String, Double> categorySpending = expenseResponse.getData();

	    //get total budget of the user based on category 
	    Map<String, Double> userBudgets = BudgetDAO.getBudgetsByUserId(userId);
	    if (userBudgets.isEmpty()) {
	        return new AnalyticsResponse<>(false, "No budgets set for this user.", new ArrayList<>());
	    }

	    List<BudgetUtilization> result = new ArrayList<>();
	    for (Map.Entry<String, Double> entry : userBudgets.entrySet()) {
	        String category = entry.getKey();
	        double budget = entry.getValue();
	        double spent = categorySpending.getOrDefault(category, 0.0);
	        double percentUsed = (budget == 0) ? 0.0 : (spent / budget) * 100;

	        result.add(new BudgetUtilization(category, budget, spent, percentUsed));
	    }

	    return new AnalyticsResponse<>(true, "Budget utilization summary retrieved.", result);
	}


}
