package service;

import dao.UserDAO;
import model.User;

public class UserService {
	// Register a new user with validation
    public String register(User user) {
        // Validate name
        if (user.getName() == null || user.getName().trim().isEmpty()) {
            return "Name cannot be empty";
        }

        // Validate email
        if (user.getEmail() == null || user.getEmail().trim().isEmpty()) {
            return "Email cannot be empty";
        }

        if (!user.getEmail().matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            return "Invalid email format";
        }

        // Validate password
        if (user.getPassword() == null || user.getPassword().length() < 6) {
            return "Password must be at least 6 characters long";
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
        if (email == null || email.trim().isEmpty()) {
            System.out.println("Email cannot be empty.");
            return null;
        }

        if (!email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            System.out.println("Invalid email format.");
            return null;
        }

        // Validate password
        if (password == null || password.trim().isEmpty()) {
            System.out.println("Password cannot be empty.");
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
