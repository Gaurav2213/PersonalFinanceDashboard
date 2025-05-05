package service;

import dao.UserDAO;
import model.User;

public class UserService {
	
	//helper method to validate password
	
	private String validatePassword(String password) {
	    if (password == null || password.trim().isEmpty()) {
	        return "Password cannot be empty";
	    }

	    if (password.length() < 6) {
	        return "Password must be at least 6 characters long";
	    }

	    if (!password.matches("^(?=.*[A-Z])(?=.*[0-9])(?=.*[!@#$%^&*]).{6,}$")) {
	        return "Password must contain uppercase, number, and special character";
	    }

	    return "valid";
	}

	
//helper method to validate email with extra protection layer for email domain 
	private String validateEmail(String email) {
	    if (email == null || email.trim().isEmpty()) {
	        return "Email cannot be empty";
	    }

	    if (!email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
	        return "Invalid email format";
	    }

	    String[] allowedDomains = { "gmail.com", "yahoo.com", "outlook.com", "hotmail.com" };
	    String domain = email.substring(email.indexOf("@") + 1);

	    boolean domainAllowed = false;
	    for (String allowed : allowedDomains) {
	        if (domain.equalsIgnoreCase(allowed)) {
	            domainAllowed = true;
	            break;
	        }
	    }

	    if (!domainAllowed) {
	        return "Email domain is not supported. Use Gmail, Yahoo, Outlook, or Hotmail.";
	    }

	    return "valid";
	}
	

	
	
	// Register a new user with validation
    public String register(User user) {
        // Validate name
        if (user.getName() == null || user.getName().trim().isEmpty()) {
            return "Name cannot be empty";
        }
       
        else if (!user.getName().matches("^[A-Za-z ]+$")) {
            return "Name can only contain letters and spaces";
        }

        // Validate email
        String emailValidation = validateEmail(user.getEmail());
        if (!emailValidation.equals("valid")) {
            return emailValidation;
        }


        // Validate password
        String passwordValidation = validatePassword(user.getPassword());
        if (!passwordValidation.equals("valid")) {
            return passwordValidation;
        }


        // Check for duplicate email
        if (UserDAO.getUserByEmail(user.getEmail()) != null) {
            return "Email is already registered";
        }

        // Register user through DAO
        boolean success = UserDAO.registerUser(user);
        return success ? "success" : "Registration failed. Please try again.";
    }
    
    

    // Login with validation
    public User login(String email, String password) {
    	
        // Validate email
    	String emailValidation = validateEmail(email);
    	if (!emailValidation.equals("valid")) {
    	    System.out.println(emailValidation); // shows: "Invalid email format" or "Unsupported domain"
    	    return null;
    	}
        

        // Validate password
    	String passwordValidation = validatePassword(password);
    	if (!passwordValidation.equals("valid")) {
    	    System.out.println(passwordValidation);
    	    return null;
    	}

        // Authenticate user
        User user = UserDAO.getUserByEmail(email);
        if (user != null && user.getPassword().equals(password)) {
            return user;
        }

        System.out.println("Invalid email or password.");
        return null;
    }
}
