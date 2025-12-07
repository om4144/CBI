package com.cbi.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;


public class Database {
    public static Connection getConnection() throws SQLException {

        String dbUrl = System.getenv("DB_URL");
        String dbUser = System.getenv("DB_USER");
        String dbPass = System.getenv("DB_PASSWORD");

        return DriverManager.getConnection(dbUrl, dbUser, dbPass);
    }
}
