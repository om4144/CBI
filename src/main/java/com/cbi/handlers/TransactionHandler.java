package com.cbi.handlers;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.IOException;
import java.net.URLEncoder;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Map;

import com.cbi.db.Database;
import com.cbi.utils.SessionManager;
import com.cbi.utils.Utils;

public class TransactionHandler implements HttpHandler {
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String username = SessionManager.getUserFromSession(exchange.getRequestHeaders().getFirst("Cookie"));
        if (username == null) {
            redirect(exchange, "/login");
            return;
        }

        if (exchange.getRequestMethod().equals("POST")) {
            Map<String, String> params = Utils.parseParams(exchange);
            try {
                processTransaction(username, params);
                redirect(exchange, "/dashboard?msg=Transaction+Successful");
            } catch (Exception e) {
                String errorMsg = URLEncoder.encode(e.getMessage(), "UTF-8");
                redirect(exchange, "/dashboard?error=" + errorMsg);
            }
        }
    }

    private void processTransaction(String username, Map<String, String> params) throws Exception {
        String action = params.get("action");
        double amount = Double.parseDouble(params.get("amount"));
        if (amount <= 0) throw new Exception("Invalid Amount");

        try (Connection conn = Database.getConnection()) {
            conn.setAutoCommit(false); 

            // Fetch Sender Info
            PreparedStatement psUser = conn.prepareStatement("SELECT account_id, balance, name FROM users WHERE username = ?");
            psUser.setString(1, username);
            ResultSet rs = psUser.executeQuery();
            if (!rs.next()) throw new Exception("User not found");

            String accId = rs.getString("account_id");
            String senderName = rs.getString("name");
            double currentBal = rs.getDouble("balance");

            if ("add".equals(action)) {
                updateBalance(conn, accId, currentBal + amount);
                // Self Transaction
                logTransaction(conn, accId, "DEPOSIT", amount, "Self", "-");
            } 
            else if ("withdraw".equals(action)) {
                if (currentBal < amount) throw new Exception("Insufficient Funds");
                updateBalance(conn, accId, currentBal - amount);
                // Self Transaction
                logTransaction(conn, accId, "WITHDRAW", amount, "Self", "-");
            } 
            else if ("invest".equals(action)) {
                if (currentBal < amount) throw new Exception("Insufficient Funds");
                String scheme = params.get("scheme");
                String nominee = params.get("nominee");
                
                updateBalance(conn, accId, currentBal - amount);
                // Save Nominee as Name, Scheme as ID
                logTransaction(conn, accId, "INVEST", amount, nominee, scheme);
            } 
            else if ("transfer".equals(action)) {
                if (currentBal < amount) throw new Exception("Insufficient Funds");

                String targetId = params.get("targetId");
                String targetNameInput = params.get("targetName");

                PreparedStatement psTarget = conn.prepareStatement("SELECT name, balance FROM users WHERE account_id = ?");
                psTarget.setString(1, targetId);
                ResultSet rsTarget = psTarget.executeQuery();

                if (rsTarget.next()) {
                    String realTargetName = rsTarget.getString("name");
                    if (!realTargetName.equalsIgnoreCase(targetNameInput)) {
                        throw new Exception("Beneficiary Name mismatch!");
                    }

                    double targetBal = rsTarget.getDouble("balance");
                    
                    // 1. Sender Side (Logs Receiver Name/ID)
                    updateBalance(conn, accId, currentBal - amount);
                    logTransaction(conn, accId, "SENT", amount, realTargetName, targetId);
                    
                    // 2. Receiver Side (Logs Sender Name/ID)
                    updateBalance(conn, targetId, targetBal + amount);
                    logTransaction(conn, targetId, "RECEIVED", amount, senderName, accId);
                } else {
                    throw new Exception("Beneficiary Account ID not found");
                }
            }
            conn.commit();
        } catch (Exception e) { throw e; }
    }

    private void updateBalance(Connection conn, String accId, double newBal) throws Exception {
        PreparedStatement ps = conn.prepareStatement("UPDATE users SET balance = ? WHERE account_id = ?");
        ps.setDouble(1, newBal);
        ps.setString(2, accId);
        ps.executeUpdate();
    }

    // UPDATED: Now accepts relatedName and relatedAccId
    private void logTransaction(Connection conn, String accId, String type, double amt, String relName, String relId) throws Exception {
        String sql = "INSERT INTO transactions (account_id, type, amount, related_name, related_account_id) VALUES (?, ?, ?, ?, ?)";
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setString(1, accId);
        ps.setString(2, type);
        ps.setDouble(3, amt);
        ps.setString(4, relName); // Beneficiary Name
        ps.setString(5, relId);   // Beneficiary Account ID
        ps.executeUpdate();
    }

    private void redirect(HttpExchange exchange, String location) throws IOException {
        exchange.getResponseHeaders().set("Location", location);
        exchange.sendResponseHeaders(302, -1);
    }
}