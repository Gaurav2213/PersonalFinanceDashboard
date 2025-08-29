package dao;

import model.User;
import util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

public class UserDAO {

    // Register new user
	public static boolean registerUser(User user) {
	    String sql = "INSERT INTO users (name, email, password, role, mobile, bio, isVerified, " +
	                 "emailVerificationToken, emailVerificationTokenExpires, createdAt, updatedAt) " +
	                 "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

	    try (Connection conn = DBConnection.getConnection();
	         PreparedStatement stmt = conn.prepareStatement(sql)) {

	        stmt.setString(1, user.getName());
	        stmt.setString(2, user.getEmail());
	        stmt.setString(3, user.getPassword());
	        stmt.setString(4, user.getRole());
	        stmt.setString(5, user.getMobile());
	        stmt.setString(6, user.getBio());
	        stmt.setBoolean(7, user.isVerified());
	        stmt.setString(8, user.getEmailVerificationToken());
	        stmt.setTimestamp(9, user.getEmailVerificationTokenExpires());
	        stmt.setTimestamp(10, user.getCreatedAt());
	        stmt.setTimestamp(11, user.getUpdatedAt());

	        int rows = stmt.executeUpdate();
	        return rows > 0;

	    } catch (Exception e) {
	        e.printStackTrace();
	        return false;
	    }
	}


    // Find user by email
	public static User getUserByEmail(String email) {
	    String sql = "SELECT * FROM users WHERE email = ?";

	    try (Connection conn = DBConnection.getConnection();
	         PreparedStatement stmt = conn.prepareStatement(sql)) {

	        stmt.setString(1, email);
	        ResultSet rs = stmt.executeQuery();

	        if (rs.next()) {
	            return new User(
	                rs.getInt("id"),
	                rs.getString("name"),
	                rs.getString("email"),
	                rs.getString("password"),
	                rs.getString("role"),
	                rs.getString("mobile"),
	                rs.getString("bio"),
	                rs.getBoolean("isVerified"),
	                rs.getString("emailVerificationToken"),
	                rs.getTimestamp("emailVerificationTokenExpires"),
	                rs.getString("resetToken"),
	                rs.getTimestamp("resetTokenExpires"),
	                rs.getTimestamp("createdAt"),
	                rs.getTimestamp("updatedAt")
	            );
	        }

	    } catch (Exception e) {
	        e.printStackTrace();
	    }
	    return null;
	}

    
 // Find user by ID
    public static User getUserById(int id) {
        String sql = "SELECT * FROM users WHERE id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return new User(
                    rs.getInt("id"),
                    rs.getString("name"),
                    rs.getString("email"),
                    rs.getString("password"),
                    rs.getString("role"),
                    rs.getString("mobile"),
                    rs.getString("bio"),
                    rs.getBoolean("isVerified"),
                    rs.getString("emailVerificationToken"),
                    rs.getTimestamp("emailVerificationTokenExpires"),
                    rs.getString("resetToken"),
                    rs.getTimestamp("resetTokenExpires"),
                    rs.getTimestamp("createdAt"),
                    rs.getTimestamp("updatedAt")
                );
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    //get user by verified token
    public static User getUserByVerificationToken(String token) {
        String sql = "SELECT * FROM users WHERE emailVerificationToken = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, token);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return new User(
                    rs.getInt("id"),
                    rs.getString("name"),
                    rs.getString("email"),
                    rs.getString("password"),
                    rs.getString("role"),
                    rs.getString("mobile"),
                    rs.getString("bio"),
                    rs.getBoolean("isVerified"),
                    rs.getString("emailVerificationToken"),
                    rs.getTimestamp("emailVerificationTokenExpires"),
                    rs.getString("resetToken"),
                    rs.getTimestamp("resetTokenExpires"),
                    rs.getTimestamp("createdAt"),
                    rs.getTimestamp("updatedAt")
                );
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }
    
    //marks user as verified 
    public static boolean markUserAsVerified(int userId) {
        String sql = "UPDATE users SET isVerified = ?, emailVerificationToken = NULL, emailVerificationTokenExpires = NULL WHERE id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setBoolean(1, true);
            stmt.setInt(2, userId);

            int rows = stmt.executeUpdate();
            return rows > 0;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    public static boolean updateVerificationToken(int userId, String token, Timestamp expiresAt) {
        String sql = "UPDATE users SET emailVerificationToken = ?, emailVerificationTokenExpires= ? WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, token);
            stmt.setTimestamp(2, expiresAt);
            stmt.setInt(3, userId);
            return stmt.executeUpdate() == 1;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
 
    public static void setResetToken(int userId, String tokenHash, java.sql.Timestamp expiresAt) throws SQLException {
        String sql = "UPDATE users SET resetToken=?, resetTokenExpires=? WHERE id=?";
        try (var con = DBConnection.getConnection(); var ps = con.prepareStatement(sql)) {
            ps.setString(1, tokenHash);
            ps.setTimestamp(2, expiresAt);
            ps.setInt(3, userId);
            ps.executeUpdate();
        }
    }



}
