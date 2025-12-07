package com.cbi.handlers;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.IOException;
import java.io.OutputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Map;

import com.cbi.db.Database;
import com.cbi.utils.SessionManager;
import com.cbi.utils.Utils;
import com.cbi.utils.IdGenerator;
import com.cbi.view.ViewHelper;

public class AdminHandler implements HttpHandler {
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String username = SessionManager.getUserFromSession(exchange.getRequestHeaders().getFirst("Cookie"));
        
        // Security Check: Is user an Admin?
        if (!isAdmin(username)) {
            exchange.getResponseHeaders().set("Location", "/login");
            exchange.sendResponseHeaders(302, -1);
            return;
        }

        if (exchange.getRequestMethod().equals("POST")) {
            Map<String, String> params = Utils.parseParams(exchange);
            handlePost(exchange, params);
        } else {
            showDashboard(exchange);
        }
    }

    private boolean isAdmin(String username) {
        if (username == null) return false;
        try (Connection conn = Database.getConnection()) {
            PreparedStatement ps = conn.prepareStatement("SELECT role FROM users WHERE username = ?");
            ps.setString(1, username);
            ResultSet rs = ps.executeQuery();
            return rs.next() && "EMPLOYEE".equals(rs.getString("role"));
        } catch (Exception e) { return false; }
    }

    private void showDashboard(HttpExchange exchange) throws IOException {
        String query = exchange.getRequestURI().getQuery();
        String msg = (query != null && query.contains("msg=")) ? java.net.URLDecoder.decode(query.split("msg=")[1], "UTF-8") : "";

        String html = ViewHelper.getHeader("CBI Admin") +
            "<div class='navbar'>" +
            "<div><h1>CBI Admin Portal</h1><small>Employee Access</small></div>" +
            "<a href='/login' class='btn-logout'>Logout</a>" +
            "</div>" +
            
            (!msg.isEmpty() ? "<div style='background:#d5f5e3;color:#1e8449;padding:10px;text-align:center;'>" + msg + "</div>" : "") +

            "<div class='grid'>" +
            
            // CARD 1: DEPOSIT TO USER
            "<div class='card'>" +
            "<h3>&#128181; Deposit to Customer</h3>" +
            "<form method='POST'>" +
            "<input type='hidden' name='action' value='deposit'>" +
            "<label>Customer Account ID</label>" +
            "<input type='text' name='targetId' placeholder='e.g. A1B2C3D4E5' required>" +
            "<label>Amount</label>" +
            "<input type='number' name='amount' placeholder='Amount (&#8377;)' required>" +
            "<button type='submit' style='background-color:#2980b9;'>Deposit Funds</button>" +
            "</form>" +
            "</div>" +

            // CARD 2: CREATE EMPLOYEE
            "<div class='card'>" +
            "<h3>&#128100; Add Bank Employee</h3>" +
            "<form method='POST'>" +
            "<input type='hidden' name='action' value='create_employee'>" +
            "<input type='text' name='name' placeholder='Employee Name' required>" +
            "<input type='text' name='username' placeholder='Username' required>" +
            "<input type='password' name='password' placeholder='Password' required>" +
            "<button type='submit' style='background-color:#8e44ad;'>Create Account</button>" +
            "</form>" +
            "</div>" +

            "</div>" + // End Grid
            ViewHelper.getFooter();

        byte[] responseBytes = html.getBytes("UTF-8");
        exchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
        exchange.sendResponseHeaders(200, responseBytes.length);
        OutputStream os = exchange.getResponseBody();
        os.write(responseBytes);
        os.close();
    }

    private void handlePost(HttpExchange exchange, Map<String, String> params) throws IOException {
        String action = params.get("action");
        String message = "Action Completed";

        try (Connection conn = Database.getConnection()) {
            if ("deposit".equals(action)) {
                String targetId = params.get("targetId");
                double amount = Double.parseDouble(params.get("amount"));
                
                // Update Balance
                PreparedStatement ps = conn.prepareStatement("UPDATE users SET balance = balance + ? WHERE account_id = ?");
                ps.setDouble(1, amount);
                ps.setString(2, targetId);
                int rows = ps.executeUpdate();
                
                if (rows == 0) throw new Exception("Account ID not found");

                // Log Transaction
                PreparedStatement psLog = conn.prepareStatement("INSERT INTO transactions (account_id, type, amount, related_name) VALUES (?, 'ADMIN_DEPOSIT', ?, 'Bank Branch')");
                psLog.setString(1, targetId);
                psLog.setDouble(2, amount);
                psLog.executeUpdate();

                message = "Successfully Deposited " + amount + " to " + targetId;

            } else if ("create_employee".equals(action)) {
                String sql = "INSERT INTO users (name, username, password, role, account_id, age, mobile, address, occupation) VALUES (?, ?, ?, 'EMPLOYEE', ?, 0, '0', 'Bank', 'Employee')";
                PreparedStatement ps = conn.prepareStatement(sql);
                ps.setString(1, params.get("name"));
                ps.setString(2, params.get("username"));
                ps.setString(3, params.get("password"));
                ps.setString(4, IdGenerator.generate());
                ps.executeUpdate();

                message = "New Employee Added: " + params.get("username");
            }
            
            exchange.getResponseHeaders().set("Location", "/admin?msg=" + message);
            exchange.sendResponseHeaders(302, -1);

        } catch (Exception e) {
            e.printStackTrace();
            String error = "Error: " + e.getMessage();
            exchange.getResponseHeaders().set("Location", "/admin?msg=" + error);
            exchange.sendResponseHeaders(302, -1);
        }
    }
}
