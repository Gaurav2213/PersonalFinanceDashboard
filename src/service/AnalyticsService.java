package service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import dao.TransactionDAO;
import model.CategoryTotal;
import model.Transaction;
import model.ValidationResult;

public class AnalyticsService {
	
	   //validate the user id and check for the existence of transaction related to user id 
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


    /**
     * Get top N spending categories by total expense amount
     */
    public static List<CategoryTotal> getTopSpendingCategories(int userId, int limit) {
        List<Transaction> transactions = TransactionDAO.getTransactionsByUserId(userId);

        // Group expenses by category
        Map<String, Double> categoryTotals = new HashMap<>();
        for (Transaction tx : transactions) {
            if ("expense".equalsIgnoreCase(tx.getType())) {
                String category = tx.getCategory().toLowerCase();
                categoryTotals.put(category, categoryTotals.getOrDefault(category, 0.0) + tx.getAmount());
            }
        }

        // Sort and limit
        return categoryTotals.entrySet().stream()
                .map(entry -> new CategoryTotal(entry.getKey(), entry.getValue()))
                .sorted((a, b) -> Double.compare(b.getTotal(), a.getTotal()))
                .limit(limit)
                .collect(Collectors.toList());
    }
}
