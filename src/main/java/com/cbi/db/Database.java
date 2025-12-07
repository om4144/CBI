package com.cbi.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;


public class Database {
    public static Connection getConnection() throws SQLException {

        String dbUrl = "jdbc:postgresql://ep-twilight-star-a1aq8c4l-pooler.ap-southeast-1.aws.neon.tech/neondb";
        String dbUser = "neondb_owner";
        String dbPass = "npg_YmCo2PO6zuEr";

        return DriverManager.getConnection(dbUrl, dbUser, dbPass);
    }
}