package service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import dao.TransactionDAO;
import model.Transaction;
import model.User;
import model.ValidationResult;
import util.CategoryValidator;
import util.Utils;
import model.TransactionResponse;
public class TransactionService {

	
	
	private static String generateSignature(Transaction tx) {
	    return tx.getUserId() + "|" +
	           tx.getType().toLowerCase() + "|" +
	           tx.getAmount() + "|" +
	           tx.getCategory().toLowerCase() + "|" +
	           tx.getDate();
	}

	// Allowed types for transaction
	private static final List<String> VALID_TYPES = Arrays.asList("income", "expense");


	
	
	//validate the user  ownership of user regarding the transaction
	private static ValidationResult validateOwnership(int userId, int transactionId) {
	    List<Transaction> userTransactions = TransactionDAO.getTransactionsByUserId(userId);
	    boolean exists = userTransactions.stream().anyMatch(tx -> tx.getId() == transactionId);

	    return exists
	        ? new ValidationResult(true, "valid")
	        : new ValidationResult(false, "Transaction does not belong to the specified user.");
	}

	
	 // Validate transaction ID
	private static ValidationResult validateTransactionId(int transactionId) {
	    if (transactionId <= 0) {
	        return new ValidationResult(false, "Invalid transaction ID");
	    }

	    if (TransactionDAO.getTransactionById(transactionId) == null) {
	        return new ValidationResult(false, "Transaction does not exist");
	    }

	    return new ValidationResult(true, "valid");
	}

	
	
	

	// reuse the common validation practice in all the methods
	private static ValidationResult validateTransaction(Transaction transaction) {
		
		   if (transaction == null) {
		        return new ValidationResult(false, "Transaction object cannot be null");
		    }
		
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
		ValidationResult categoryResult =  CategoryValidator.validateCategory(transaction.getCategory().trim());
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
	public static ValidationResult validateUserId(int userId) {
		
	

	    if (userId <= 0) {
	        return new ValidationResult(false, "Invalid user Id or missing ");
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
	    ValidationResult categoryResult = CategoryValidator.validateCategory(category);
;
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
	public TransactionResponse deleteTransaction(int transactionId, int userId) {
	    ValidationResult idResult = validateTransactionId(transactionId);
	    if (!idResult.isValid()) {
	        return new TransactionResponse(false, idResult.getMessage(), null);
	    }

	    ValidationResult userResult = validateUserId(userId);
	    if (!userResult.isValid()) {
	        return new TransactionResponse(false, userResult.getMessage(), null);
	    }

	    List<Transaction> transactions = TransactionDAO.getTransactionsByUserId(userId);
	    boolean exists = transactions.stream().anyMatch(tx -> tx.getId() == transactionId);

	    if (!exists) {
	        return new TransactionResponse(false, "Transaction not found for this user", null);
	    }

	    boolean deleted = TransactionDAO.deleteTransaction(transactionId, userId);
	    return deleted
	        ? new TransactionResponse(true, "Transaction deleted successfully", null)
	        : new TransactionResponse(false, "Failed to delete transaction", null);
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
	
	
	
	
	//batch operations ***************************************
	

	public static ValidationResult addTransactionsBatch(List<Transaction> transactions , int userId) {
		ValidationResult batchSizeResult = Utils.validateTransactionBatchSize(transactions);
		if (!batchSizeResult.isValid()) return batchSizeResult;


	   // int userId = transactions.get(0).getUserId();  // assumes all transactions belong to the same user

	    // 🔄 Load existing transactions for that user
	    List<Transaction> existing = TransactionDAO.getTransactionsByUserId(userId);

	    // 🧠 Build a Set of "transaction signatures"
	    Set<String> existingSignatures = new HashSet<>();
	    for (Transaction tx : existing) {
	        existingSignatures.add(generateSignature(tx));
	    }

	    List<String> errorMessages = new ArrayList<>();
	    for (int i = 0; i < transactions.size(); i++) {
	        Transaction tx = transactions.get(i);
	        ValidationResult result = validateTransaction(tx);

	        if (!result.isValid()) {
	            errorMessages.add("Transaction " + (i + 1) + ": " + result.getMessage());
	            continue;
	        }

	        if (existingSignatures.contains(generateSignature(tx))) {
	            errorMessages.add("Transaction " + (i + 1) + ": Duplicate transaction not allowed.");
	        }
	    }
	    
	    if (!errorMessages.isEmpty()) {
	        String combined = String.join("\n", errorMessages);
	        return new ValidationResult(false, combined);
	    }


	    // 🔧 Proceed with batch insert
	    boolean success = TransactionDAO.addTransactionsBatch(transactions ,userId);
	    return success
	            ? new ValidationResult(true, "All transactions added successfully.")
	            : new ValidationResult(false, "Failed to add transactions.");
	}
	
	public static ValidationResult updateTransactionsBatch(List<Transaction> transactions , int userId) {
	    // ✅ Batch size + null/empty list validation
	    ValidationResult batchSizeResult = Utils.validateTransactionBatchSize(transactions);
	    if (!batchSizeResult.isValid()) return batchSizeResult;

	    List<String> errorMessages = new ArrayList<>();
	    Set<String> batchSignatures = new HashSet<>();

	    // 🧠 Assume all transactions belong to the same user (like your add logic)
	
	    List<Transaction> existing = TransactionDAO.getTransactionsByUserId(userId);

	    // ✅ Build signature set from existing DB records (excluding current update targets)
	    Set<String> existingSignatures = existing.stream()
	        .filter(t -> transactions.stream().noneMatch(u -> u.getId() == t.getId()))
	        .map(TransactionService::generateSignature)
	        .collect(Collectors.toSet());

	    for (int i = 0; i < transactions.size(); i++) {
	        Transaction tx = transactions.get(i);

	

	        // ✅ ID + existence validation
	        ValidationResult idValidation = validateTransactionId(tx.getId());
	        if (!idValidation.isValid()) {
	            errorMessages.add("Transaction " + (i + 1) + ": " + idValidation.getMessage());
	            continue;
	        }
	        
	        //validate the ownership
	        ValidationResult ownership = validateOwnership(userId, tx.getId());
	        if (!ownership.isValid()) {
	            errorMessages.add("Transaction " + (i + 1) + ": " + ownership.getMessage());
	            continue;
	        }


	        // ✅ Field and null check  validation
	        ValidationResult txValidation = validateTransaction(tx);
	        if (!txValidation.isValid()) {
	            errorMessages.add("Transaction " + (i + 1) + ": " + txValidation.getMessage());
	            continue;
	        }

	        // ✅ Duplicate check
	        String sig = generateSignature(tx);

	        if (existingSignatures.contains(sig)) {
	            errorMessages.add("Transaction " + (i + 1) + ": would duplicate another existing transaction.");
	        } else if (!batchSignatures.add(sig)) {
	            errorMessages.add("Transaction " + (i + 1) + ": duplicates another transaction in this batch.");
	        }
	    }

	    if (!errorMessages.isEmpty()) {
	        return new ValidationResult(false, String.join("\n", errorMessages));
	    }

	    boolean success = TransactionDAO.updateTransactionsBatch(transactions , userId);
	    return success
	            ? new ValidationResult(true, "All transactions updated successfully.")
	            : new ValidationResult(false, "Failed to update transactions.");
	}

	
	public static ValidationResult deleteTransactionsBatch(int userId, List<Integer> transactionIds) {
	    if (transactionIds == null || transactionIds.isEmpty()) {
	        return new ValidationResult(false, "Transaction ID list cannot be empty");
	    }

	    // Validate userId
	    ValidationResult userResult = validateUserId(userId);
	    if (!userResult.isValid()) {
	        return userResult;
	    }

	    Set<Integer> uniqueIds = new HashSet<>();
	    List<String> errorMessages = new ArrayList<>();

	    for (int i = 0; i < transactionIds.size(); i++) {
	        int id = transactionIds.get(i);

	        // ✅ Reuse validation method for transaction ID existence
	        ValidationResult idResult = validateTransactionId(id);
	        if (!idResult.isValid()) {
	            errorMessages.add("Transaction " + (i + 1) + ": " + idResult.getMessage());
	            continue;
	        }

	        // ✅ Reuse ownership check
	        ValidationResult ownership = validateOwnership(userId, id);
	        if (!ownership.isValid()) {
	            errorMessages.add("Transaction " + (i + 1) + ": " + ownership.getMessage());
	            continue;
	        }

	        // ✅ Check for duplicates in the incoming request
	        if (!uniqueIds.add(id)) {
	            errorMessages.add("Transaction " + (i + 1) + ": duplicate ID in request.");
	        }
	    }

	    if (!errorMessages.isEmpty()) {
	        return new ValidationResult(false, String.join("\n", errorMessages));
	    }

	    boolean success = TransactionDAO.deleteTransactionsBatch(transactionIds, userId);
	    return success
	            ? new ValidationResult(true, "All transactions deleted successfully.")
	            : new ValidationResult(false, "Failed to delete transactions.");
	}

	
	
	
	
	
	
	
	

}
