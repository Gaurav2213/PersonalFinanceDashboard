package dao;
import util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import model.Budget;
public class BudgetDAO {

	// extract the budget based based on per user per category 
	public static Map<String, Double> getBudgetsByUserId(int userId) {
        Map<String, Double> budgets = new HashMap<>();

        String sql = "SELECT category, amount FROM budgets WHERE user_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                String category = rs.getString("category").toLowerCase();
                double amount = rs.getDouble("amount");
                budgets.put(category, amount);
            }

        } catch (Exception e) {
            e.printStackTrace(); // You can log this in production
        }

        return budgets;
    }
	
	
	//insert the budget in budget entity
	public static boolean insertBudget(Budget budget,int userId) {
	    String sql = "INSERT INTO budgets (user_id, category, amount) VALUES (?, ?, ?)";

	    try (Connection conn = DBConnection.getConnection();
	         PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

	        stmt.setInt(1, userId);
	        stmt.setString(2, budget.getCategory());
	        stmt.setDouble(3, budget.getAmount());

	        int affectedRows = stmt.executeUpdate();

	        if (affectedRows > 0) {
	            // ✅ Retrieve generated keys (i.e., auto-increment ID)
	            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
	                if (generatedKeys.next()) {
	                    budget.setId(generatedKeys.getInt(1)); // 🪄 Set generated ID into the object
	                }
	            }
	            return true;
	        }
	        return false;

	    } catch (SQLException e) {
	        e.printStackTrace();
	        return false;
	    }
	}

	  
	  //check the existence of the budget based on id and category
	  public static boolean budgetExists(int userId, String category) {
		    String query = "SELECT COUNT(*) FROM budgets WHERE user_id = ? AND LOWER(category) = LOWER(?)";
		    
		    try (Connection conn = DBConnection.getConnection();
		         PreparedStatement stmt = conn.prepareStatement(query)) {

		        stmt.setInt(1, userId);
		        stmt.setString(2, category);

		        ResultSet rs = stmt.executeQuery();
		        if (rs.next()) {
		            return rs.getInt(1) > 0;  // true if count > 0
		        }
		    } catch (SQLException e) {
		        e.printStackTrace(); // Ideally use proper logging
		    }

		    return false;
		}
	  
	  
	  //update the budget based on category and user id
	  public static boolean updateBudgetAmount(int userId, String category, double newAmount) {
		    String sql = "UPDATE budgets SET amount = ? WHERE user_id = ? AND category = ?";
		    
		    try (Connection conn = DBConnection.getConnection();
		         PreparedStatement stmt = conn.prepareStatement(sql)) {

		        stmt.setDouble(1, newAmount);
		        stmt.setInt(2, userId);
		        stmt.setString(3, category.toLowerCase());

		        return stmt.executeUpdate() > 0;

		    } catch (SQLException e) {
		        e.printStackTrace();
		        return false;
		    }
		}
	  
	// delete budget for th user based on category user had set .
	  public static boolean deleteBudget(int userId, String category) {
	      String sql = "DELETE FROM budgets WHERE user_id = ? AND category = ?";
	      
	      try (Connection conn = DBConnection.getConnection();
	           PreparedStatement stmt = conn.prepareStatement(sql)) {
	          
	          stmt.setInt(1, userId);
	          stmt.setString(2, category.toLowerCase());  // normalize

	          int affectedRows = stmt.executeUpdate();
	          return affectedRows > 0;  // true if deletion happened
	      } catch (Exception e) {
	          e.printStackTrace();
	      }
	      return false;
	  }

	  //GET budget of the user based on user id 
	  public static List<Budget> getBudgetsByUser(int userId) {
		    List<Budget> budgets = new ArrayList<>();
		    String sql = "SELECT id, user_id, category, amount FROM budgets WHERE user_id = ?";

		    try (Connection conn = DBConnection.getConnection();
		         PreparedStatement stmt = conn.prepareStatement(sql)) {
		        
		        stmt.setInt(1, userId);
		        ResultSet rs = stmt.executeQuery();

		        while (rs.next()) {
		            Budget budget = new Budget();
		            budget.setId(rs.getInt("id"));
		            budget.setUserId(rs.getInt("user_id"));
		            budget.setCategory(rs.getString("category"));
		            budget.setAmount(rs.getDouble("amount"));
		            budgets.add(budget);
		        }
		    } catch (SQLException e) {
		        e.printStackTrace();
		    }

		    return budgets;
		}

	  //*********************************************************Batch Operations
	  
	  //batch operation for insertion 
	  public static boolean insertMultipleBudgets(List<Budget> budgets ,int userId) {
		    String sql = "INSERT INTO budgets (user_id, category, amount) VALUES (?, ?, ?)";

		    try (Connection conn = DBConnection.getConnection();
		         PreparedStatement stmt = conn.prepareStatement(sql)) {

		        conn.setAutoCommit(false);

		        for (Budget budget : budgets) {
		            stmt.setInt(1,userId );
		            stmt.setString(2, budget.getCategory().toLowerCase());
		            stmt.setDouble(3, budget.getAmount());
		            stmt.addBatch();
		        }

		        stmt.executeBatch();
		        conn.commit();
		        return true;

		    } catch (SQLException e) {
		        e.printStackTrace();
		        return false;
		    }
		}
	  
	  //batch operation for update 
	  
	  public static boolean updateBudgetsBatch(List<Budget> budgets, int userId) {
		    String sql = "UPDATE budgets SET amount = ? WHERE user_id = ? AND LOWER(category) = ?";

		    try (Connection conn = DBConnection.getConnection();
		         PreparedStatement stmt = conn.prepareStatement(sql)) {

		        conn.setAutoCommit(false);  // Start transaction

		        for (Budget b : budgets) {
		            stmt.setDouble(1, b.getAmount());
		            stmt.setInt(2, userId);
		            stmt.setString(3, b.getCategory().toLowerCase());
		            stmt.addBatch();
		        }

		        stmt.executeBatch();
		        conn.commit();  // Commit all updates
		        return true;

		    } catch (SQLException e) {
		        e.printStackTrace();
		        return false;
		    }
		}

	  // batch operation for delete 
	  public static boolean deleteBudgetsBatch(int userId, List<String> categories) {
		    if (categories == null || categories.isEmpty()) return false;

		    String sql = "DELETE FROM budgets WHERE user_id = ? AND LOWER(category) = ?";

		    try (Connection conn = DBConnection.getConnection();
		         PreparedStatement stmt = conn.prepareStatement(sql)) {

		        conn.setAutoCommit(false);

		        for (String category : categories) {
		            stmt.setInt(1, userId);
		            stmt.setString(2, category.toLowerCase());
		            stmt.addBatch();
		        }

		        stmt.executeBatch();
		        conn.commit();
		        return true;

		    } catch (Exception e) {
		        e.printStackTrace();
		        return false;
		    }
		}


	  
	  
	
}
