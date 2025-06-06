package util;



import java.util.Arrays;
import java.util.List;
import model.ValidationResult;

public class CategoryValidator {

    private static final List<String> VALID_CATEGORIES = Arrays.asList(
        "food", "transport", "utilities", "shopping",
        "health", "salary", "entertainment", "other"
    );

    //validate the user input categories  against the predefined list of valid categories 
    public static ValidationResult validateCategory(String category) {
        if (category == null || category.trim().isEmpty()) {
            return new ValidationResult(false, "Category cannot be empty");
        }

        category = category.trim().toLowerCase();
        if (!VALID_CATEGORIES.contains(category)) {
            return new ValidationResult(false, "Invalid category. Valid categories are: " + VALID_CATEGORIES);
        }

        return new ValidationResult(true, "valid");
    }

    //get  the validation categories list 
    public static List<String> getValidCategories() {
        return VALID_CATEGORIES;
    }
}

