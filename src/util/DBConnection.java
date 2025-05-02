package util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {
	
	   private static final String URL = "jdbc:mysql://localhost:3306/finance_db"; // DB location
	    private static final String USER = "root";        // DB username
	    private static final String PASSWORD = "javaee"; // DB password

	    public static Connection getConnection() {
	        try {
	            Class.forName("com.mysql.cj.jdbc.Driver"); // Load the MySQL JDBC driver
	            return DriverManager.getConnection(URL, USER, PASSWORD); // Return connection
	        } catch (ClassNotFoundException e) {
	            System.out.println("JDBC Driver not found!");
	            e.printStackTrace();
	        } catch (SQLException e) {
	            System.out.println("Database connection failed!");
	            e.printStackTrace();
	        }
	        return null; // If connection fails, return null
	    }
}
