package service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import dao.TransactionDAO;
import model.Transaction;

public class TransactionService {

	// Allowed categories for dropdown selection
    private static final List<String> VALID_CATEGORIES = Arrays.asList(
        "Food", "Transport", "Utilities", "Shopping", "Health", "Salary", "Entertainment", "Other"
    );

    // Add a new transaction (income or expense)
    public String addTransaction(Transaction transaction) {
        // Validate amount
        if ( transaction.getAmount() <= 0) {
            return "Amount must be greater than 0";
        }

        // Validate type
        if (transaction.getType() == null ||
            (!transaction.getType().equalsIgnoreCase("income") &&
             !transaction.getType().equalsIgnoreCase("expense"))) {
            return "Invalid transaction type. Must be 'income' or 'expense'";
        }

        // Validate date
        if (transaction.getDate() == null) {
            return "Date cannot be null";
        }

        if (transaction.getDate().after(new java.util.Date())) {
            return "Transaction date cannot be in the future";
        }
        
        // Validate category
        if (transaction.getCategory() == null || !VALID_CATEGORIES.contains(transaction.getCategory())) {
            return "Invalid category selected";
        }

        // Save transaction
        boolean success = TransactionDAO.addTransaction(transaction);
        return success ? "success" : "Failed to add transaction";
    }

    // Retrieve all transactions for a user
    public List<Transaction> getTransactionsByUser(int userId) {
        if (userId <= 0) {
            System.out.println("Invalid user ID.");
            return new ArrayList<>();
        }

        return TransactionDAO.getTransactionsByUserId(userId);
    }

    // Retrieve transactions for a user filtered by category
    public List<Transaction> getTransactionsByCategory(int userId, String category) {
        if (userId <= 0) {
            System.out.println("Invalid user ID.");
            return new ArrayList<>();
        }

        if (category == null || category.trim().isEmpty()) {
            System.out.println("Category cannot be empty.");
            return new ArrayList<>();
        }

        if (!VALID_CATEGORIES.contains(category)) {
            System.out.println("Invalid category.");
            return new ArrayList<>();
        }

        return TransactionDAO.getTransactionsByCategory(userId, category);
    }
    
    
    //delete transaction based on pre check of existence of transaction 
    public String deleteTransaction(int transactionId, int userId) {
        if (transactionId <= 0 || userId <= 0) {
            return "Invalid transaction or user ID";
        }

        // Check if the transaction exists for this user
        List<Transaction> transactions = TransactionDAO.getTransactionsByUserId(userId);
        boolean exists = transactions.stream().anyMatch(tx -> tx.getId() == transactionId);

        if (!exists) {
            return "Transaction not found for this user.";
        }

        // Proceed to delete
        boolean deleted = TransactionDAO.deleteTransaction(transactionId, userId);
        return deleted ? "Transaction deleted successfully" : "Failed to delete transaction";
    }

}
