package service;

import java.time.LocalDate;
import java.time.temporal.WeekFields;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

import dao.BudgetDAO;
import dao.TransactionDAO;
import model.AnalyticsResponse;
import model.BudgetUtilization;
import model.CategoryTotal;
import model.CategoryTrend;
import model.DateCumulativeSpending;
import model.OverspendWarning;
import model.RecurringTransaction;
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

	

	public static AnalyticsResponse<List<DateCumulativeSpending>> getPrefixSumSpending(int userId, String type) {
	    // ✅ Validate user and transactions
	    ValidationResult validation = validateUserAnalyticsAccess(userId);
	    if (!validation.isValid()) {
	        return new AnalyticsResponse<>(false, validation.getMessage(), new ArrayList<>());
	    }

	    // ✅ Validate type
	    if (!type.equalsIgnoreCase("daily") && !type.equalsIgnoreCase("weekly")) {
	        return new AnalyticsResponse<>(false, "Invalid type. Use 'daily' or 'weekly'.", new ArrayList<>());
	    }

	    // ✅ Fetch transactions (already filtered for expense + sorted by date ASC)
	    List<Transaction> expenses = TransactionDAO.getExpenseTransactionsByUser(userId);
	    
	    System.out.println("Expense transactions for user " + userId + ":");
	    expenses.forEach(tx -> System.out.println(tx.getUserId() + " | " + tx.getDate() + " | " + tx.getAmount()));

	    if (expenses.isEmpty()) {
	        return new AnalyticsResponse<>(false, "No expense transactions found.", new ArrayList<>());
	    }

	    // ✅ Group by date or week
	    TreeMap<String, Double> grouped = new TreeMap<>();

	    for (Transaction tx : expenses) {
	        LocalDate date = tx.getDate().toLocalDate();
	        String key;

	        if ("daily".equalsIgnoreCase(type)) {
	            key = date.toString(); // Exact day
	        } else {
	            WeekFields weekFields = WeekFields.ISO;
	            LocalDate monday = date.with(weekFields.dayOfWeek(), 1); // ISO week starts Monday
	            key = monday.toString();
	        }

	        grouped.put(key, grouped.getOrDefault(key, 0.0) + tx.getAmount());
	    }

	    // ✅ Compute prefix sum
	    double runningTotal = 0.0;
	    List<DateCumulativeSpending> result = new ArrayList<>();

	    for (Map.Entry<String, Double> entry : grouped.entrySet()) {
	        runningTotal += entry.getValue();
	        result.add(new DateCumulativeSpending(entry.getKey(), runningTotal));
	    }

	    String msg = "Prefix sum (" + type + ") spending data retrieved successfully.";
	    return new AnalyticsResponse<>(true, msg, result);
	}
	
	// get minimum and max spending days
	public static AnalyticsResponse<Map<String, DateCumulativeSpending>> getMaxMinSpendingDays(int userId){
	    ValidationResult validation = validateUserAnalyticsAccess(userId);
	    if (!validation.isValid()) {
	        return new AnalyticsResponse<>(false, validation.getMessage(), new HashMap<>());
	    }

	    List<Transaction> expenses = TransactionDAO.getExpenseTransactionsByUser(userId);
	    if (expenses.isEmpty()) {
	        return new AnalyticsResponse<>(false, "No expense transactions found.", new HashMap<>());
	    }

	    // Group expenses by date
	    Map<String, Double> spendingPerDay = new HashMap<>();
	    for (Transaction tx : expenses) {
	        String date = tx.getDate().toLocalDate().toString();
	        spendingPerDay.put(date, spendingPerDay.getOrDefault(date, 0.0) + tx.getAmount());
	    }

	    // Find max and min
	    String maxDate = null, minDate = null;
	    double maxAmount = -Double.MIN_VALUE;
	    double minAmount = Double.MAX_VALUE;

	    for (Map.Entry<String, Double> entry : spendingPerDay.entrySet()) {
	        double amount = entry.getValue();
	        if (amount > maxAmount) {
	            maxAmount = amount;
	            maxDate = entry.getKey();
	        }
	        if (amount < minAmount) {
	            minAmount = amount;
	            minDate = entry.getKey();
	        }
	    }

	    Map<String, DateCumulativeSpending> result = new HashMap<>();
	    result.put("max", new DateCumulativeSpending(maxDate, maxAmount));
	    result.put("min", new DateCumulativeSpending(minDate, minAmount));

	    return new AnalyticsResponse<>(true, "Max and Min spending days retrieved.", result);
	}

	
	//compute  category based trend  on the basis of months
	public static AnalyticsResponse<List<CategoryTrend>> getCategoryTrendOverTime(int userId) {
	    // ✅ Validate user and transactions
	    ValidationResult validation = validateUserAnalyticsAccess(userId);
	    if (!validation.isValid()) {
	        return new AnalyticsResponse<>(false, validation.getMessage(), new ArrayList<>());
	    }

	    List<Transaction> expenses = TransactionDAO.getExpenseTransactionsByUser(userId);

	    if (expenses.isEmpty()) {
	        return new AnalyticsResponse<>(false, "No expense transactions found.", new ArrayList<>());
	    }

	    // ✅ Group by month-year and then by category
	    Map<String, Map<String, Double>> monthCategoryMap = new TreeMap<>(); // Sorted by month

	    for (Transaction tx : expenses) {
	        String month = tx.getDate().toLocalDate().getYear() + "-" +
	                       String.format("%02d", tx.getDate().toLocalDate().getMonthValue());
	        String category = tx.getCategory().toLowerCase();
	        double amount = tx.getAmount();

	        monthCategoryMap
	            .computeIfAbsent(month, k -> new HashMap<>())
	            .merge(category, amount, Double::sum);
	    }

	    // ✅ Convert to response objects
	    List<CategoryTrend> result = new ArrayList<>();
	    for (Map.Entry<String, Map<String, Double>> entry : monthCategoryMap.entrySet()) {
	        result.add(new CategoryTrend(entry.getKey(), entry.getValue()));
	    }

	    return new AnalyticsResponse<>(true, "Category trend over time retrieved.", result);
	}

//recurring dates detector 
	
	public static AnalyticsResponse<List<RecurringTransaction>> detectRecurringTransactions(int userId) {
	    ValidationResult validation = validateUserAnalyticsAccess(userId);
	    if (!validation.isValid()) {
	        return new AnalyticsResponse<>(false, validation.getMessage(), new ArrayList<>());
	    }

	    List<Transaction> expenses = TransactionDAO.getExpenseTransactionsByUser(userId);
	    if (expenses.isEmpty()) {
	        return new AnalyticsResponse<>(false, "No expense transactions found.", new ArrayList<>());
	    }

	    // Group by category and amount range (±10%)
	    Map<String, List<Transaction>> grouped = new HashMap<>();
	    for (Transaction tx : expenses) {
	        String key = tx.getCategory().toLowerCase() + "-" + Math.round(tx.getAmount() / 10.0); // group by rounded bucket
	        grouped.computeIfAbsent(key, k -> new ArrayList<>()).add(tx);
	    }

	    List<RecurringTransaction> recurringList = new ArrayList<>();

	    //here we are considering if they are recurring transaction or not 
	    for (Map.Entry<String, List<Transaction>> entry : grouped.entrySet()) {
	        List<Transaction> txs = entry.getValue();
	        if (txs.size() < 3) continue; // require at least 3 similar transactions

	        // Sort by date and check intervals
	        txs.sort(Comparator.comparing(Transaction::getDate));
	        List<String> recurringDates = new ArrayList<>();
	        for (Transaction tx : txs) {
	            recurringDates.add(tx.getDate().toString());
	        }

	        Transaction sample = txs.get(0);
	        recurringList.add(new RecurringTransaction(sample.getCategory(), sample.getAmount(), recurringDates));
	    }
	    
	    if (recurringList.isEmpty()) {
	        return new AnalyticsResponse<>(true, "No recurring transactions found.", recurringList);
	    }

	    return new AnalyticsResponse<>(true, "Recurring transactions detected.", recurringList);
	}

	// get warning alerts based on spending per category  corresponding to budget 
	public static AnalyticsResponse<List<OverspendWarning>> getOverspendWarnings(int userId) {
	    AnalyticsResponse<List<BudgetUtilization>> utilizationResponse = getBudgetUtilization(userId);

	    if (!utilizationResponse.isSuccess()) {
	        return new AnalyticsResponse<>(false, utilizationResponse.getMessage(), new ArrayList<>());
	    }

	    List<OverspendWarning> warnings = new ArrayList<>();

	    for (BudgetUtilization item : utilizationResponse.getData()) {
	        if (item.getPercentUsed() > 100.0) {
	            warnings.add(new OverspendWarning(
	                item.getCategory(),
	                item.getBudget(),
	                item.getSpent(),
	                item.getPercentUsed(),
	                "Overspent"
	            ));
	        } else if (item.getPercentUsed() >= 80.0) {
	            warnings.add(new OverspendWarning(
	                item.getCategory(),
	                item.getBudget(),
	                item.getSpent(),
	                item.getPercentUsed(),
	                "Nearing Limit"
	            ));
	        }
	    }

	    if (warnings.isEmpty()) {
	        return new AnalyticsResponse<>(true, "No overspending or warning detected.", warnings);
	    }

	    return new AnalyticsResponse<>(true, "Overspending warnings retrieved.", warnings);
	}


}
