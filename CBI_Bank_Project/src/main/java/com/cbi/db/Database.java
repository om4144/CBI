package com.cbi.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;


public class Database {

    // The method must be static so we can call Database.getConnection() without 'new Database()'
    public static Connection getConnection() throws SQLException {

        String dbUrl = System.getenv("DB_URL");
        String dbUser = System.getenv("DB_USER");
        String dbPass = System.getenv("DB_PASS");

        if (dbUrl == null || dbUrl.isEmpty()) {
            dbUrl = "jdbc:postgresql://ep-small-smoke-a1kqwj9n-pooler.ap-southeast-1.aws.neon.tech/neondb";
            dbUser = "neondb_owner";
            dbPass = "npg_aBt4yc1PLgKH";
        }
        // 3. Connect
        return DriverManager.getConnection(dbUrl, dbUser, dbPass);
    }
}