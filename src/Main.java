import java.sql.Connection;

import util.DBConnection;

public class Main {
	public static void main(String[] args) {
        Connection conn = DBConnection.getConnection();

        if (conn != null) {
            System.out.println("✅ Database connected successfully!");
        } else {
            System.out.println("❌ Failed to connect to the database.");
        }
    }
}
