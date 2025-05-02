package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import model.Transaction;
import util.DBConnection;

public class TransactionDAO {


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
}
