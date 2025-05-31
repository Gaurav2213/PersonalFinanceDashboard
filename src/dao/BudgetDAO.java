package dao;
import util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;
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
}
