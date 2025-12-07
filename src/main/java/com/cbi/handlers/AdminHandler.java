package com.cbi.handlers;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.IOException;
import java.io.OutputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Map;
import java.time.format.DateTimeFormatter;

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
        
        // --- NEW FEATURE: VIEW ALL USERS ---
        if (query != null && query.contains("view=users")) {
            showUsersList(exchange);
            return;
        }
        
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

            // --- NEW CARD: VIEW USERS BUTTON ---
            "<div class='card'>" +
            "<h3>&#128196; Bank Database</h3>" +
            "<p>View details of all registered customers, including account age and current balance.</p>" +
            "<a href='/admin?view=users' style='display:inline-block; width:100%; text-align:center; background-color:#34495e; color:white; padding:12px; margin-top:10px; border-radius:4px; text-decoration:none; box-sizing:border-box;'>View All Customers</a>" +
            "</div>" +

            "</div>" + // End Grid
            ViewHelper.getFooter();

        sendHtmlResponse(exchange, html);
    }

    // --- NEW METHOD: GENERATE USER TABLE ---
    private void showUsersList(HttpExchange exchange) throws IOException {
        StringBuilder html = new StringBuilder();
        html.append(ViewHelper.getHeader("CBI Customer Records"));
        html.append("<div class='navbar'><div><h1>CBI Records</h1><small>Confidential</small></div><a href='/admin' class='btn-logout' style='background-color:#2980b9;'>Back to Dashboard</a></div>");
        
        html.append("<div class='container' style='max-width:900px;'>");
        html.append("<h3>Registered Customers</h3>");
        html.append("<table>");
        html.append("<tr><th>Name</th><th>Account ID</th><th>Opening Date</th><th>Current Balance</th></tr>");

        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT name, account_id, created_at, balance FROM users WHERE role != 'EMPLOYEE' ORDER BY created_at DESC")) {
             
             ResultSet rs = ps.executeQuery();
             DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

             while(rs.next()) {
                 String dateStr = "N/A";
                 if(rs.getTimestamp("created_at") != null) {
                     dateStr = rs.getTimestamp("created_at").toLocalDateTime().format(formatter);
                 }
                 
                 html.append("<tr>")
                     .append("<td>").append(rs.getString("name")).append("</td>")
                     .append("<td>").append(rs.getString("account_id")).append("</td>")
                     .append("<td>").append(dateStr).append("</td>")
                     .append("<td>&#8377;").append(String.format("%.2f", rs.getDouble("balance"))).append("</td>")
                     .append("</tr>");
             }
        } catch(Exception e) {
             html.append("<tr><td colspan='4' style='color:red;'>Error loading data: ").append(e.getMessage()).append("</td></tr>");
        }

        html.append("</table>");
        html.append("</div>"); // End container
        html.append(ViewHelper.getFooter());

        sendHtmlResponse(exchange, html.toString());
    }

    private void handlePost(HttpExchange exchange, Map<String, String> params) throws IOException {
        String action = params.get("action");
        String message = "Action Completed";

        try (Connection conn = Database.getConnection()) {
            if ("deposit".equals(action)) {
                String targetId = params.get("targetId");
                double amount = Double.parseDouble(params.get("amount"));
                
                PreparedStatement ps = conn.prepareStatement("UPDATE users SET balance = balance + ? WHERE account_id = ?");
                ps.setDouble(1, amount);
                ps.setString(2, targetId);
                int rows = ps.executeUpdate();
                
                if (rows == 0) throw new Exception("Account ID not found");

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

    private void sendHtmlResponse(HttpExchange exchange, String response) throws IOException {
        byte[] responseBytes = response.getBytes("UTF-8");
        exchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
        exchange.sendResponseHeaders(200, responseBytes.length);
        OutputStream os = exchange.getResponseBody();
        os.write(responseBytes);
        os.close();
    }
}
