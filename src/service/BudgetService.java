package service;

import java.util.ArrayList;
import java.util.List;

import dao.BudgetDAO;
import model.Budget;
import model.BudgetResponse;
import model.ValidationResult;
import util.CategoryValidator;

public class BudgetService {
	
	/**
     * Shared validation logic for add/update operations
     * @param budget Budget object to validate
     * @param skipDuplicateCheck if true, does NOT check for duplicate existing entry (used in update)
     */
	
	private static ValidationResult validateBudgetInput(Budget budget, boolean skipDuplicateCheck) {
	    if (budget == null) {
	        return new ValidationResult(false, "Budget object cannot be null");
	    }

	    ValidationResult userValidation = TransactionService.validateUserId(budget.getUserId());
	    if (!userValidation.isValid()) return userValidation;

	    ValidationResult categoryValidation = CategoryValidator.validateCategory(budget.getCategory());
	    if (!categoryValidation.isValid()) return categoryValidation;

	    if (budget.getAmount() <= 0) {
	        return new ValidationResult(false, "Budget amount must be greater than 0");
	    }

	    // ✅ Always check existence and use flag to interpret
	    boolean exists = BudgetDAO.budgetExists(budget.getUserId(), budget.getCategory());
	    
	    if (!skipDuplicateCheck && exists) {
	        return new ValidationResult(false, "Budget for this category already exists");
	    }

	    if (skipDuplicateCheck && !exists) {
	        return new ValidationResult(false, "Budget does not exist for this category, cannot update.");
	    }

	    return new ValidationResult(true, "Valid input");
	}

	 
	 
	 //add the budget object 
	 public static BudgetResponse<Budget> addBudget(Budget budget) {
		    ValidationResult validation = validateBudgetInput(budget, false); // false = add operation
		    if (!validation.isValid()) {
		        return new BudgetResponse<>(false, validation.getMessage());
		    }

		    boolean success = BudgetDAO.insertBudget(budget);
		    return success
		        ? new BudgetResponse<>(true, "Budget added successfully",budget)
		        : new BudgetResponse<>(false, "Failed to add budget",null);
		}
	 
	 
	 //update the budget amount of existing budget
	 public static BudgetResponse<String> updateBudget(Budget budget) {
		    // 1. Validate input fields + existence
		    ValidationResult validation = validateBudgetInput(budget, true); // `true` = skip duplicate check
		    if (!validation.isValid()) {
		        return new BudgetResponse<>(false, validation.getMessage(), null);
		    }

		    // 2. Perform update in DB
		    boolean updated = BudgetDAO.updateBudgetAmount(budget.getUserId(), budget.getCategory(), budget.getAmount());
		    if (updated) {
		        return new BudgetResponse<>(true, "Budget updated successfully", null);
		    } else {
		        return new BudgetResponse<>(false, "Failed to update budget", null);
		    }
		}

	 
	// delete budget and validate the critical field and existence of it before DB Call
	 public static BudgetResponse<String> deleteBudget(int userId, String category) {
	     // 🔍 Validate user ID
	     ValidationResult userResult = TransactionService.validateUserId(userId);
	     if (!userResult.isValid()) {
	         return new BudgetResponse<>(false, userResult.getMessage(), null);
	     }

	     // 🔍 Validate category
	     ValidationResult categoryResult = CategoryValidator.validateCategory(category);
	     if (!categoryResult.isValid()) {
	         return new BudgetResponse<>(false, categoryResult.getMessage(), null);
	     }

	     // ✅ Check if the budget exists
	     if (!BudgetDAO.budgetExists(userId, category)) {
	         return new BudgetResponse<>(false, "No budget found for this category", null);
	     }

	     // 🗑️ Delete from DB
	     boolean deleted = BudgetDAO.deleteBudget(userId, category);
	     return deleted
	         ? new BudgetResponse<>(true, "Budget deleted successfully", null)
	         : new BudgetResponse<>(false, "Failed to delete budget", null);
	 }
	 
	 
	 //get budget of the user based on  user Id
	 public static BudgetResponse<List<Budget>> getBudgetsByUser(int userId) {
		    // ✅ Validate userId
		    ValidationResult validation = TransactionService.validateUserId(userId);
		    if (!validation.isValid()) {
		        return new BudgetResponse<>(false, validation.getMessage(), new ArrayList<>());
		    }

		    // ✅ Fetch budgets
		    List<Budget> budgets = BudgetDAO.getBudgetsByUser(userId);
		    if (budgets.isEmpty()) {
		        return new BudgetResponse<>(false, "No budgets found for this user.", new ArrayList<>());
		    }

		    // ✅ Return success
		    return new BudgetResponse<>(true, "Budgets retrieved successfully.", budgets);
		}




    
   
}

