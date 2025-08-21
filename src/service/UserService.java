package service;

import java.sql.Timestamp;

import dao.UserDAO;
import model.AuthResponse;
import model.LoginResponse;
import model.User;
import model.ValidationResult;
import util.EmailService;
import util.JWTUtils;
import util.PasswordUtils;
import util.TokenUtils;

public class UserService {
	

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

	
	// Reusable helper to validate login inputs
	private AuthResponse<User> validateLoginInputs(String email, String password) {
	    ValidationResult emailResult = validateEmail(email);
	    if (!emailResult.isValid()) {
	        return new AuthResponse<>(false, emailResult.getMessage());
	    }

	    ValidationResult passwordResult = validatePassword(password);
	    if (!passwordResult.isValid()) {
	        return new AuthResponse<>(false, passwordResult.getMessage());
	    }

	    return new AuthResponse<>(true, "Inputs are valid");
	}

	//registration method to register the user 
	public ValidationResult register(User user) {
	    // Step 1: Validate fields
	    ValidationResult nameResult = validateName(user.getName());
	    if (!nameResult.isValid()) return nameResult;

	    ValidationResult emailResult = validateEmail(user.getEmail());
	    if (!emailResult.isValid()) return emailResult;

	    ValidationResult passwordResult = validatePassword(user.getPassword());
	    if (!passwordResult.isValid()) return passwordResult;

	    // Step 2: Check for duplicate email
	    if (UserDAO.getUserByEmail(user.getEmail()) != null) {
	        return new ValidationResult(false, "Email is already registered");
	    }

	    // Step 3: Hash password
	    String hashedPassword = PasswordUtils.hashPassword(user.getPassword());
	    user.setPassword(hashedPassword);

	    // Step 4: Generate email verification token and expiry
	    String verificationToken = TokenUtils.generateToken();
	    Timestamp expiry = TokenUtils.generateExpiry(24); // 24 hours validity
	    user.setEmailVerificationToken(verificationToken);
	    user.setEmailVerificationTokenExpires(expiry);
	    user.setVerified(false); // Must verify email before access

	    // Step 5: Set role and timestamps
	    user.setRole("user");
	    Timestamp now = new Timestamp(System.currentTimeMillis());
	    user.setCreatedAt(now);
	    user.setUpdatedAt(now);

	    // Step 6: Insert user into DB
	    boolean success = UserDAO.registerUser(user);
	    if (success) {
	    	  // Trigger email sending after user is saved
	        EmailService.sendVerificationEmail(
	            user.getEmail(),
	            user.getName(),
	            user.getEmailVerificationToken()
	        );

	        // NOTE: We'll send email in next step
	        return new ValidationResult(true, "Registration successful. Please check your email to verify your account.");
	    } else {
	        return new ValidationResult(false, "Registration failed. Please try again.");
	    }
	}
	public AuthResponse<LoginResponse> loginUser(String email, String password) {
	    // Step 1: Validate inputs
	    AuthResponse<User> validationResponse = validateLoginInputs(email, password);
	    if (!validationResponse.isSuccess()) {
	        return new AuthResponse<>(false, validationResponse.getMessage());
	    }

	    // Step 2: Get user
	    User user = UserDAO.getUserByEmail(email);
	    if (user == null) {
	        return new AuthResponse<>(false, "User not found");
	    }

	    // Step 3: Verify password
	    if (!PasswordUtils.verifyPassword(password, user.getPassword())) {
	        return new AuthResponse<>(false, "Incorrect password");
	    }

	    // Step 4: Check verification status
	    if (!user.isVerified()) {
	        return new AuthResponse<>(false, "Please verify your email before logging in");
	    }

	    // Step 5: Generate JWT
	    String token = JWTUtils.generateToken(user.getId(), user.getEmail());

	    // Step 6: Return success response
	    LoginResponse loginResponse = new LoginResponse(user.getId(), user.getEmail(), token);
	    return new AuthResponse<>(true, "Login successful", loginResponse);
	}
	


}
