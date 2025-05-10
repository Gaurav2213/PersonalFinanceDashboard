package service;

import dao.UserDAO;
import model.User;
import model.ValidationResult;

public class UserService {
	
	// Login with validation (used by API handler)
	public ValidationResult loginWithValidation(String email, String password) {
	    // Validate email format and domain
	    ValidationResult emailResult = validateEmail(email);
	    if (!emailResult.isValid()) {
	        return emailResult;
	    }

	    // Validate password format
	    ValidationResult passwordResult = validatePassword(password);
	    if (!passwordResult.isValid()) {
	        return passwordResult;
	    }

	    // Check user existence and password match
	    User user = UserDAO.getUserByEmail(email);
	    if (user != null && user.getPassword().equals(password)) {
	        return new ValidationResult(true, "Login successful");
	    }

	    return new ValidationResult(false, "Invalid email or password");
	}

	// Helper method to validate name
	private ValidationResult validateName(String name) {
	    if (name == null || name.trim().isEmpty()) {
	        return new ValidationResult(false, "Name cannot be empty");
	    }
	    if (!name.matches("^[A-Za-z ]+$")) {
	        return new ValidationResult(false, "Name can only contain letters and spaces");
	    }
	    return new ValidationResult(true, "valid");
	}

	// Helper method to validate password
	private ValidationResult validatePassword(String password) {
	    if (password == null || password.trim().isEmpty()) {
	        return new ValidationResult(false, "Password cannot be empty");
	    }

	    if (password.length() < 6) {
	        return new ValidationResult(false, "Password must be at least 6 characters long");
	    }

	    if (!password.matches("^(?=.*[A-Z])(?=.*[0-9])(?=.*[!@#$%^&*]).{6,}$")) {
	        return new ValidationResult(false, "Password must contain uppercase, number, and special character");
	    }

	    return new ValidationResult(true, "valid");
	}

	// Helper method to validate email with extra protection layer for email domain
	private ValidationResult validateEmail(String email) {
	    if (email == null || email.trim().isEmpty()) {
	        return new ValidationResult(false, "Email cannot be empty");
	    }

	    if (!email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
	        return new ValidationResult(false, "Invalid email format");
	    }

	    String[] allowedDomains = { "gmail.com", "yahoo.com", "outlook.com", "hotmail.com" };
	    String domain = email.substring(email.indexOf("@") + 1);

	    for (String allowed : allowedDomains) {
	        if (domain.equalsIgnoreCase(allowed)) {
	            return new ValidationResult(true, "valid");
	        }
	    }

	    return new ValidationResult(false, "Email domain is not supported. Use Gmail, Yahoo, Outlook, or Hotmail.");
	}

	// Register a new user with validation
	public ValidationResult register(User user) {
	    ValidationResult nameResult = validateName(user.getName());
	    if (!nameResult.isValid()) return nameResult;

	    ValidationResult emailResult = validateEmail(user.getEmail());
	    if (!emailResult.isValid()) return emailResult;

	    ValidationResult passwordResult = validatePassword(user.getPassword());
	    if (!passwordResult.isValid()) return passwordResult;

	    // Check for duplicate email
	    if (UserDAO.getUserByEmail(user.getEmail()) != null) {
	        return new ValidationResult(false, "Email is already registered");
	    }

	    // If everything is valid and user not registered yet
	    boolean success = UserDAO.registerUser(user);
	    if (success) {
	        return new ValidationResult(true, "User registered successfully");
	    } else {
	        return new ValidationResult(false, "Registration failed. Please try again.");
	    }
	}

	// Login with validation
	public User login(String email, String password) {
	    ValidationResult result = loginWithValidation(email, password);
	    if (result.isValid()) {
	        return UserDAO.getUserByEmail(email); // Already authenticated
	    }
	    return null;
}
}
