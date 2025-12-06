import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Database {

    // The method must be static so we can call Database.getConnection() without 'new Database()'
    public static Connection getConnection() throws SQLException {
        
        // 1. Define variables INSIDE the static method (Local variables)
        String dbUrl = System.getenv("DB_URL");
        String dbUser = System.getenv("DB_USER");
        String dbPass = System.getenv("DB_PASS");

        // 2. Fallback for Localhost (If running on your computer)
        if (dbUrl == null || dbUrl.isEmpty()) {
            dbUrl = "jdbc:postgresql://localhost:5432/bankdb";
            dbUser = "postgres";
            dbPass = "password";
        }

        // 3. Connect
        return DriverManager.getConnection(dbUrl, dbUser, dbPass);
    }
}
