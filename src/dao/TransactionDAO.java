package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import model.Transaction;
import util.DBConnection;

public class TransactionDAO {

	public static boolean isDuplicateTransaction(Transaction t) {
	    String sql = "SELECT COUNT(*) FROM transactions WHERE user_id = ? AND type = ? AND amount = ? AND category = ? AND date = ?";
	    
	    try (Connection conn = DBConnection.getConnection();
	         PreparedStatement stmt = conn.prepareStatement(sql)) {

	        stmt.setInt(1, t.getUserId());
	        stmt.setString(2, t.getType());
	        stmt.setDouble(3, t.getAmount());
	        stmt.setString(4, t.getCategory());
	        stmt.setDate(5, t.getDate());

	        ResultSet rs = stmt.executeQuery();
	        if (rs.next()) {
	            return rs.getInt(1) > 0; // if count > 0, duplicate exists
	        }
	    } catch (Exception e) {
	        e.printStackTrace();
	    }
	    return false;
	}
	
	
	// fetch transaction based on transaction id
	public static Transaction getTransactionById(int transactionId) {
	    String sql = "SELECT * FROM transactions WHERE id = ?";

	    try (Connection conn = DBConnection.getConnection();
	         PreparedStatement stmt = conn.prepareStatement(sql)) {

	        stmt.setInt(1, transactionId);
	        ResultSet rs = stmt.executeQuery();

	        if (rs.next()) {
	            return new Transaction(
	                rs.getInt("id"),
	                rs.getInt("user_id"),
	                rs.getString("type"),
	                rs.getDouble("amount"),
	                rs.getString("category"),
	                rs.getString("description"),
	                rs.getDate("date")
	            );
	        }

	    } catch (Exception e) {
	        e.printStackTrace();
	    }

	    return null;
	}


    // Insert a new transaction
    public static boolean addTransaction(Transaction t) {
        String sql = "INSERT INTO transactions (user_id, type, amount, category, description, date) VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, t.getUserId());
            stmt.setString(2, t.getType());
            stmt.setDouble(3, t.getAmount());
            stmt.setString(4, t.getCategory());
            stmt.setString(5, t.getDescription());
            stmt.setDate(6, t.getDate());

            int rows = stmt.executeUpdate();
            return rows > 0;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // Fetch all transactions by user ID
    public static List<Transaction> getTransactionsByUserId(int userId) {
        List<Transaction> list = new ArrayList<>();
        String sql = "SELECT * FROM transactions WHERE user_id = ? ORDER BY date DESC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Transaction t = new Transaction(
                    rs.getInt("id"),
                    rs.getInt("user_id"),
                    rs.getString("type"),
                    rs.getDouble("amount"),
                    rs.getString("category"),
                    rs.getString("description"),
                    rs.getDate("date")
                );
                list.add(t);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }
    
    //filter the transaction of the user based on category 
    public static List<Transaction> getTransactionsByCategory(int userId, String category) {
        List<Transaction> transactions = new ArrayList<>();
        String sql = "SELECT * FROM transactions WHERE user_id = ? AND LOWER(category) = ? ORDER BY date DESC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            stmt.setString(2, category);

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Transaction transaction = new Transaction(
                    rs.getInt("id"),
                    rs.getInt("user_id"),
                    rs.getString("type"),
                    rs.getDouble("amount"),
                    rs.getString("category"),
                    rs.getString("description"),
                    rs.getDate("date")
                );
                transactions.add(transaction);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return transactions;
    }
    	//update the transaction in transaction table  based on user id and transaction id
    public static boolean updateTransaction(Transaction t) {
        String sql = "UPDATE transactions SET type = ?, amount = ?, category = ?, description = ?, date = ? WHERE id = ? AND user_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, t.getType());
            stmt.setDouble(2, t.getAmount());
            stmt.setString(3, t.getCategory());
            stmt.setString(4, t.getDescription());
            stmt.setDate(5, t.getDate());
            stmt.setInt(6, t.getId());
            stmt.setInt(7, t.getUserId());

            int rows = stmt.executeUpdate();
            return rows > 0;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    //delete the particular transaction for particular user
    public static boolean deleteTransaction(int transactionId, int userId) {
        String sql = "DELETE FROM transactions WHERE id = ? AND user_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, transactionId);
            stmt.setInt(2, userId);

            int rows = stmt.executeUpdate();
            return rows > 0;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    
    //batch operations ******************************************************************
    
    public static boolean addTransactionsBatch(List<Transaction> transactions) {
        String sql = "INSERT INTO transactions (user_id, amount, category, type, date, description) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            conn.setAutoCommit(false);

            for (Transaction tx : transactions) {
                stmt.setInt(1, tx.getUserId());
                stmt.setDouble(2, tx.getAmount());
                stmt.setString(3, tx.getCategory());
                stmt.setString(4, tx.getType());
                stmt.setDate(5, tx.getDate());
                stmt.setString(6, tx.getDescription());
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

    
    
    
    
    
    
    
    
    
    
    
}
