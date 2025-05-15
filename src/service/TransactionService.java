package service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import dao.TransactionDAO;
import model.Transaction;
import model.User;
import model.ValidationResult;
import model.TransactionResponse;
public class TransactionService {

	// Allowed types for transaction
	private static final List<String> VALID_TYPES = Arrays.asList("income", "expense");

	// Allowed categories for dropdown selection
	private static final List<String> VALID_CATEGORIES = Arrays.asList("food", "transport", "utilities", "shopping",
			"health", "salary", "entertainment", "other");
	
	
	
	
	//validate the category of transaction 
	public ValidationResult validateCategory(String category) {
	    if (category == null || category.trim().isEmpty()) {
	        return new ValidationResult(false, "Category cannot be empty");
	    }

	    category = category.trim().toLowerCase();
	    if (!VALID_CATEGORIES.contains(category)) {
	        return new ValidationResult(false, "Invalid category");
	    }

	    return new ValidationResult(true, "valid");
	   
	}
	
	 // Validate transaction ID
	private ValidationResult validateTransactionId(int transactionId) {
	    if (transactionId <= 0) {
	        return new ValidationResult(false, "Invalid transaction ID");
	    }

	    if (TransactionDAO.getTransactionById(transactionId) == null) {
	        return new ValidationResult(false, "Transaction does not exist");
	    }

	    return new ValidationResult(true, "valid");
	}

	
	
	

	// reuse the common validation practice in all the methods
	private ValidationResult validateTransaction(Transaction transaction) {
		// Validate amount
		if (transaction.getAmount() <= 0) {
			return new ValidationResult(false, "Amount must be greater than 0");
		}

		// Validate and clean type
		if (transaction.getType() == null || transaction.getType().trim().isEmpty()) {
			return new ValidationResult(false, "Transaction type cannot be empty");
		}
		String type = transaction.getType().trim().toLowerCase();
		if (!VALID_TYPES.contains(type)) {
			return new ValidationResult(false, "Invalid transaction type. Must be 'income' or 'expense'");
		}

		transaction.setType(type); // normalized update

		
		// Validate date
		if (transaction.getDate() == null) {
			return new ValidationResult(false, "Date cannot be null");
		}

		if (transaction.getDate().after(new java.util.Date())) {
			return new ValidationResult(false, "Transaction date cannot be in the future");
		}

		// Validate and clean category
		ValidationResult categoryResult = validateCategory(transaction.getCategory());
		if (!categoryResult.isValid()) {
		    return categoryResult;
		}
		
		 // Normalize the input
		transaction.setCategory(transaction.getCategory().trim().toLowerCase()); // Normalize

	        
		// validate the description
		String description = transaction.getDescription();

		if (description == null || description.trim().isEmpty()) {
		    return new ValidationResult(false, "Description cannot be empty");
		}

		if (description.trim().length() < 3) {
		    return new ValidationResult(false, "Description must be at least 3 characters long");
		}

		if (description.length() > 255) {
		    return new ValidationResult(false, "Description cannot exceed 255 characters");
		}


		return new ValidationResult(true, "success");
	}

	//validate user user id from database
	public ValidationResult validateUserId(int userId) {
		
	

	    if (userId <= 0) {
	        return new ValidationResult(false, "Invalid or missing userId");
	    }
	    

	    if (dao.UserDAO.getUserById(userId) == null) {
	        return new ValidationResult(false, "User does not exist");
	    }

	    return new ValidationResult(true, "valid");
	}


	
	// Add a new transaction (income or expense)
	public ValidationResult addTransaction(Transaction transaction) {
	    // Validate transaction
	    ValidationResult validation = validateTransaction(transaction);
	    if (!validation.isValid()) return validation;

	    
	 // Check for duplicates
	    if (TransactionDAO.isDuplicateTransaction(transaction)) {
	        return new ValidationResult(false, "Duplicate transaction not allowed");
	    }
	    
	    // Try to save
	    boolean success = TransactionDAO.addTransaction(transaction);
	    if (success) {
	        return new ValidationResult(true, "Transaction added successfully");
	    } else {
	        return new ValidationResult(false, "Failed to add transaction");
	    }
	}
	



	// Retrieve all transactions for a user
	public TransactionResponse getTransactionsByUser(int userId) {
		 ValidationResult result = validateUserId(userId);
		    if (!result.isValid()) {
		        return new TransactionResponse(false, result.getMessage(), null);
		    }

		    List<Transaction> list = TransactionDAO.getTransactionsByUserId(userId);
		    if (list.isEmpty()) {
		        return new TransactionResponse(false, "No transactions found for this user", null);
		    }

		    return new TransactionResponse(true, "Success", list);
	}
	
	

	// Retrieve transactions for a user filtered by category
	public TransactionResponse getTransactionsByCategory(int userId, String category) {
		category = category.toLowerCase().trim();
	    // Validate user
	    ValidationResult userResult = validateUserId(userId);
	    if (!userResult.isValid()) {
	        return new TransactionResponse(false, userResult.getMessage(), null);
	    }

	    // Validate category
	    ValidationResult categoryResult = validateCategory(category);
	    if (!categoryResult.isValid()) {
	        return new TransactionResponse(false, categoryResult.getMessage(), null);
	    }

	    // Fetch transactions
	    List<Transaction> transactions = TransactionDAO.getTransactionsByCategory(userId, category);
	    
	    if (transactions.isEmpty()) {
	        return new TransactionResponse(false, "No transactions found for this category", null);
	    }

	    return new TransactionResponse(true, "Success", transactions);
	}

	// delete transaction based on pre check of existence of transaction
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

	// update the transaction
	public TransactionResponse updateTransaction(Transaction transaction) {
	    // Validate transaction ID
	    ValidationResult idResult = validateTransactionId(transaction.getId());
	    if (!idResult.isValid()) {
	        return new TransactionResponse(false, idResult.getMessage(), null);
	    }

	    // Validate user ID
	    ValidationResult userResult = validateUserId(transaction.getUserId());
	    if (!userResult.isValid()) {
	        return new TransactionResponse(false, userResult.getMessage(), null);
	    }

	    // Validate transaction fields
	    ValidationResult fieldValidation = validateTransaction(transaction);
	    if (!fieldValidation.isValid()) {
	        return new TransactionResponse(false, fieldValidation.getMessage(), null);
	    }

	    // Update
	    boolean updated = TransactionDAO.updateTransaction(transaction);
	    return updated
	        ? new TransactionResponse(true, "Transaction updated successfully", List.of(transaction))
	        : new TransactionResponse(false, "Failed to update transaction", null);
	}

}
