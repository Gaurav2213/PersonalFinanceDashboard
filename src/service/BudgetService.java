package service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
	
	private static ValidationResult validateBudgetInput(Budget budget, boolean skipDuplicateCheck , boolean skipUserValidation) {
	    if (budget == null) {
	        return new ValidationResult(false, "Budget object cannot be null");
	    }

	    if (!skipUserValidation) {
	        ValidationResult userValidation = TransactionService.validateUserId(budget.getUserId());
	        if (!userValidation.isValid()) return userValidation;
	    }
	    
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
		    ValidationResult validation = validateBudgetInput(budget, false,false); // false = add operation
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
		    ValidationResult validation = validateBudgetInput(budget, true, true); // `true` = skip duplicate check
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

	 //***************************************** budget batch operations 

	 public static BudgetResponse<List<Budget>> addBudgetsBatch(List<Budget> budgets) {
		    if (budgets == null || budgets.isEmpty()) {
		        return new BudgetResponse<>(false, "Budget list cannot be empty.", new ArrayList<>());
		    }

		    int userId = budgets.get(0).getUserId();
		    ValidationResult userResult = TransactionService.validateUserId(userId);
		    if (!userResult.isValid()) {
		        return new BudgetResponse<>(false, userResult.getMessage(), new ArrayList<>());
		    }

		    List<String> errors = new ArrayList<>();
		    Set<String> batchCategories = new HashSet<>();
		    Set<String> existingCategories = BudgetDAO.getBudgetsByUserId(userId).keySet();

		    for (int i = 0; i < budgets.size(); i++) {
		        Budget b = budgets.get(i);

		        ValidationResult result = validateBudgetInput(b, false , true);  // false = add mode
		        if (!result.isValid()) {
		            errors.add("Budget " + (i + 1) + ": " + result.getMessage());
		            continue;
		        }

		        String category = b.getCategory().toLowerCase();

		        if (!batchCategories.add(category)) {
		            errors.add("Budget " + (i + 1) + ": Duplicate category in batch.");
		        } else if (existingCategories.contains(category)) {
		            errors.add("Budget " + (i + 1) + ": Budget for this category already exists.");
		        }
		    }

		    if (!errors.isEmpty()) {
		        return new BudgetResponse<>(false, String.join("\n", errors), new ArrayList<>());
		    }

		    boolean inserted = BudgetDAO.insertMultipleBudgets(budgets);
		    return inserted
		        ? new BudgetResponse<>(true, "Budgets added successfully.", budgets)
		        : new BudgetResponse<>(false, "Failed to add budgets.", new ArrayList<>());
		}

	 //validate id and duplicate  existing check and batch duplication check 
	 public static BudgetResponse<List<Budget>> updateBudgetsBatch(List<Budget> budgets) {
		    if (budgets == null || budgets.isEmpty()) {
		        return new BudgetResponse<>(false, "Budget list cannot be empty.", new ArrayList<>());
		    }

		    int userId = budgets.get(0).getUserId();
		    ValidationResult userResult = TransactionService.validateUserId(userId);
		    if (!userResult.isValid()) {
		        return new BudgetResponse<>(false, userResult.getMessage(), new ArrayList<>());
		    }

		    List<String> errors = new ArrayList<>();
		    Set<String> seenCategories = new HashSet<>();

		    for (int i = 0; i < budgets.size(); i++) {
		        Budget b = budgets.get(i);

		        // Validate without checking for duplicates (since this is update)
		        ValidationResult result = validateBudgetInput(b, true, true);  // true = skipDuplicateCheck
		        if (!result.isValid()) {
		            errors.add("Budget " + (i + 1) + ": " + result.getMessage());
		            continue;
		        }

		        String category = b.getCategory().toLowerCase();
		        if (!seenCategories.add(category)) {
		            errors.add("Budget " + (i + 1) + ": Duplicate category in batch.");
		        }
		    }

		    if (!errors.isEmpty()) {
		        return new BudgetResponse<>(false, String.join("\n", errors), new ArrayList<>());
		    }

		    boolean updated = BudgetDAO.updateBudgetsBatch(budgets);
		    return updated
		        ? new BudgetResponse<>(true, "Budgets updated successfully.", budgets)
		        : new BudgetResponse<>(false, "Failed to update budgets.", new ArrayList<>());
		}

//batch operation to delete the budget 

	 public static BudgetResponse<List<String>> deleteBudgetsBatch(int userId, List<String> categories) {
		    // Validate user
		    ValidationResult userResult = TransactionService.validateUserId(userId);
		    if (!userResult.isValid()) {
		        return new BudgetResponse<>(false, userResult.getMessage(), new ArrayList<>());
		    }

		    if (categories == null || categories.isEmpty()) {
		        return new BudgetResponse<>(false, "Category list cannot be empty", new ArrayList<>());
		    }

		    List<String> errors = new ArrayList<>();
		    Set<String> normalizedCategories = new HashSet<>();
		    Set<String> existing = BudgetDAO.getBudgetsByUserId(userId).keySet();  // Existing categories

		    for (int i = 0; i < categories.size(); i++) {
		        String raw = categories.get(i);
		        ValidationResult valid = CategoryValidator.validateCategory(raw);
		        if (!valid.isValid()) {
		            errors.add("Category " + (i + 1) + ": " + valid.getMessage());
		            continue;
		        }

		        String normalized = raw.toLowerCase();
		        if (!existing.contains(normalized)) {
		            errors.add("Category " + (i + 1) + ": Budget for this category does not exist.");
		        } else {
		            normalizedCategories.add(normalized);
		        }
		    }

		    if (!errors.isEmpty()) {
		        return new BudgetResponse<>(false, String.join("\n", errors), new ArrayList<>());
		    }

		    boolean deleted = BudgetDAO.deleteBudgetsBatch(userId, new ArrayList<>(normalizedCategories));
		    return deleted
		        ? new BudgetResponse<>(true, "Budgets deleted successfully.", new ArrayList<>(normalizedCategories))
		        : new BudgetResponse<>(false, "Failed to delete budgets.", new ArrayList<>());
		}

   
}

