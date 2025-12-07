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
import com.cbi.utils.Utils;
import com.cbi.utils.SessionManager;
import com.cbi.utils.IdGenerator;
import com.cbi.view.ViewHelper;


public class AuthHandler implements HttpHandler {
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getPath();
        String method = exchange.getRequestMethod();
        String response = "";

        if (path.equals("/register")) {
            if (method.equals("GET")) {
                response = getRegisterPage();
                sendResponse(exchange, response, 200);
            } else if (method.equals("POST")) {
                Map<String, String> params = Utils.parseParams(exchange);
                registerUser(exchange, params);
            }
        } else if (path.equals("/login")) {
            if (method.equals("GET")) {
                response = getLoginPage(null);
                sendResponse(exchange, response, 200);
            } else if (method.equals("POST")) {
                Map<String, String> params = Utils.parseParams(exchange);
                loginUser(exchange, params);
            }
        }
    }

    private void registerUser(HttpExchange exchange, Map<String, String> params) throws IOException {
        // Updated Query with new columns
        String sql = "INSERT INTO users (name, username, password, age, mobile, address, occupation, account_id) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = Database.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, params.get("name"));
            pstmt.setString(2, params.get("username"));
            pstmt.setString(3, params.get("password"));
            pstmt.setInt(4, Integer.parseInt(params.get("age")));
            // New Fields
            pstmt.setString(5, params.get("mobile"));
            pstmt.setString(6, params.get("address"));
            pstmt.setString(7, params.get("occupation"));
            pstmt.setString(8, IdGenerator.generate());
            
            pstmt.executeUpdate();
            
            exchange.getResponseHeaders().set("Location", "/login");
            exchange.sendResponseHeaders(302, -1);
        } catch (Exception e) {
            sendResponse(exchange, "Error: " + e.getMessage(), 500);
        }
    }

    private void loginUser(HttpExchange exchange, Map<String, String> params) throws IOException {
        String sql = "SELECT * FROM users WHERE username = ? AND password = ?";
        try (Connection conn = Database.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, params.get("username"));
            pstmt.setString(2, params.get("password"));
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                String sessionId = SessionManager.createSession(rs.getString("username"));
                exchange.getResponseHeaders().set("Set-Cookie", "session_id=" + sessionId + "; Path=/");
                exchange.getResponseHeaders().set("Location", "/dashboard");
                exchange.sendResponseHeaders(302, -1);
            } else {
                sendResponse(exchange, getLoginPage("Invalid Credentials"), 200);
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void sendResponse(HttpExchange exchange, String response, int code) throws IOException {
        exchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
        exchange.sendResponseHeaders(code, response.getBytes().length);
        OutputStream os = exchange.getResponseBody();
        os.write(response.getBytes());
        os.close();
    }

    private String getLoginPage(String error) {
        return ViewHelper.getHeader("CBI Login") +
            "<div class='container'>" +
            "<h2>CBI Bank Login</h2>" + 
            (error != null ? "<p style='color:#c0392b; background:#fadbd8; padding:10px; border-radius:4px;'>" + error + "</p>" : "") +
            "<form method='POST' action='/login'>" +
            "<input type='text' name='username' placeholder='Username' required>" +
            
            // Password with Eye Icon
            "<div class='password-container'>" +
            "<input type='password' id='loginPass' name='password' placeholder='Password' required>" +
            "<span id='icon-loginPass' class='toggle-icon' onclick=\"togglePass('loginPass')\">&#128064;</span>" +
            "</div>" +

            "<button type='submit'>Login</button>" +
            "<button type='button' style='background-color:#7f8c8d' onclick=\"alert('1. Enter credentials\\n2. Click Login')\">Need Help?</button>" +
            "</form>" +
            "<br><a href='/register' style='color:#2980b9; text-decoration:none;'>Open New Account</a>" +
            "</div>" +
            ViewHelper.getFooter();
    }

    private String getRegisterPage() {
        return ViewHelper.getHeader("CBI Register") +
            "<div class='container' style='max-width:500px;'>" + // Slightly wider for more fields
            "<h2>CBI Account Opening Form</h2>" +
            "<form method='POST' action='/register'>" +
            
            "<input type='text' name='name' placeholder='Full Name' required>" +
            "<input type='text' name='username' placeholder='Choose Username' required>" +
            
            // Password with Eye Icon
            "<div class='password-container'>" +
            "<input type='password' id='regPass' name='password' placeholder='Create Password' required>" +
            "<span id='icon-regPass' class='toggle-icon' onclick=\"togglePass('regPass')\">&#128064;</span>" +
            "</div>" +

            "<div style='display:flex; gap:10px;'>" +
            "<input type='number' name='age' placeholder='Age' style='flex:1' required>" +
            "<input type='text' name='mobile' placeholder='Mobile No.' style='flex:2' required>" +
            "</div>" +

            "<input type='text' name='occupation' placeholder='Occupation' required>" +
            "<textarea name='address' placeholder='Permanent Address' rows='3' required></textarea>" +

            "<button type='submit'>Submit Application</button>" +
            "</form>" +
            "<br><a href='/login' style='color:#2980b9; text-decoration:none;'>Back to Login</a>" +
            "</div>" +
            ViewHelper.getFooter();
    }
}