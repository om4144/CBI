package com.cbi.handlers;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.IOException;
import java.io.OutputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDateTime;
import java.time.Month;

import com.cbi.db.Database;
import com.cbi.utils.SessionManager;
import com.cbi.view.ViewHelper;

public class DashboardHandler implements HttpHandler {
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String username = SessionManager.getUserFromSession(exchange.getRequestHeaders().getFirst("Cookie"));
        if (username == null) {
            exchange.getResponseHeaders().set("Location", "/login");
            exchange.sendResponseHeaders(302, -1);
            return;
        }

        String query = exchange.getRequestURI().getQuery();
        String msg = (query != null && query.contains("msg=")) ? java.net.URLDecoder.decode(query.split("msg=")[1].split("&")[0], "UTF-8") : "";
        String error = (query != null && query.contains("error=")) ? java.net.URLDecoder.decode(query.split("error=")[1].split("&")[0], "UTF-8") : "";

        StringBuilder html = new StringBuilder();
        try (Connection conn = Database.getConnection()) {
            PreparedStatement psUser = conn.prepareStatement("SELECT * FROM users WHERE username = ?");
            psUser.setString(1, username);
            ResultSet rs = psUser.executeQuery();
            rs.next();
            String name = rs.getString("name");
            String accId = rs.getString("account_id");
            double bal = rs.getDouble("balance");

            html.append(ViewHelper.getHeader("CBI Dashboard"));
            
            // NAVBAR UPDATED NAME
            html.append("<div class='navbar'>")
                .append("<div><h1>CBI Bank</h1><small>Welcome, ").append(name).append("</small></div>")
                .append("<a href='/login' class='btn-logout'>Logout</a>")
                .append("</div>");

            if (!msg.isEmpty()) html.append("<div style='background:#d5f5e3;color:#1e8449;padding:10px;text-align:center;'>").append(msg).append("</div>");
            if (!error.isEmpty()) html.append("<div style='background:#fadbd8;color:#c0392b;padding:10px;text-align:center;'>").append(error).append("</div>");

            // INFO
            html.append("<div style='text-align:center; margin-top:30px;'>")
                .append("<p style='color:#7f8c8d;'>Account ID: ").append(accId).append("</p>")
                .append("<div class='balance'>&#8377;").append(String.format("%.2f", bal)).append("</div>")
                .append("</div>");

            // ACTION GRID
            html.append("<div class='grid'>");
            
            // --- MODIFIED CARD: Removed 'Add Money' Option ---
            html.append("<div class='card'><h3>Manage Funds</h3>")
                .append("<form method='POST' action='/transaction'>")
                .append("<label>Action</label><select name='action'><option value='withdraw'>Withdraw</option></select>")
                .append("<input type='number' name='amount' placeholder='Amount (&#8377;)' required> ")
                .append("<button type='submit'>Submit</button></form></div>");

            html.append("<div class='card'><h3>Investments</h3>")
                .append("<form method='POST' action='/transaction'>")
                .append("<input type='hidden' name='action' value='invest'>")
                .append("<label>Scheme</label><input type='text' name='scheme' placeholder='e.g. FD' required> ")
                .append("<label>Nominee</label><input type='text' name='nominee' placeholder='Nominee Name' required> ")
                .append("<input type='number' name='amount' placeholder='Amount (&#8377;)' required> ")
                .append("<button type='submit' style='background-color:#e67e22;'>Invest Now</button></form></div>");

            html.append("<div class='card'><h3>Transfer</h3>")
                .append("<form method='POST' action='/transaction'>")
                .append("<input type='hidden' name='action' value='transfer'>")
                .append("<label>ID</label><input type='text' name='targetId' placeholder='10-digit ID' required> ")
                .append("<label>Name</label><input type='text' name='targetName' placeholder='Verify Name' required> ")
                .append("<input type='number' name='amount' placeholder='Amount (&#8377;)' required> ")
                .append("<button type='submit' style='background-color:#2980b9;'>Transfer</button></form></div>");
            html.append("</div>");

            // --- DATA PROCESSING ---
            PreparedStatement psHist = conn.prepareStatement("SELECT * FROM transactions WHERE account_id = ? ORDER BY transaction_date DESC");
            psHist.setString(1, accId);
            ResultSet rsHist = psHist.executeQuery();

            StringBuilder historyRows = new StringBuilder();
            
            // Stats Variables
            double monthlyIncome = 0;
            double monthlyExpense = 0;
            double monthlyInvest = 0; 
            Month currentMonth = LocalDateTime.now().getMonth();

            while(rsHist.next()) {
                String type = rsHist.getString("type");
                double amount = rsHist.getDouble("amount");
                LocalDateTime date = rsHist.getTimestamp("transaction_date").toLocalDateTime();
                
                // Logic to separate Income, Expense, and Investment
                if (date.getMonth() == currentMonth) {
                    // Include ADMIN_DEPOSIT as income
                    if (type.equals("DEPOSIT") || type.equals("RECEIVED") || type.equals("ADMIN_DEPOSIT")) {
                        monthlyIncome += amount;
                    } 
                    else if (type.equals("INVEST")) {
                        monthlyInvest += amount; 
                    } 
                    else {
                        monthlyExpense += amount; // Spending (Withdraw + Sent)
                    }
                }

                String color = (type.equals("DEPOSIT") || type.equals("RECEIVED") || type.equals("ADMIN_DEPOSIT")) ? "green" : "red";
                String relName = rsHist.getString("related_name");
                String relId = rsHist.getString("related_account_id");
                
                historyRows.append("<tr>")
                    .append("<td>#").append(rsHist.getInt("id")).append("</td>")
                    .append("<td>").append(rsHist.getTimestamp("transaction_date")).append("</td>")
                    .append("<td>").append(type).append("</td>")
                    .append("<td>").append(relName != null ? relName : "-").append("</td>")
                    .append("<td>").append(relId != null ? relId : "-").append("</td>")
                    .append("<td style='color:").append(color).append("'>&#8377;").append(amount).append("</td>")
                    .append("</tr>");
            }

            // --- BUTTONS ---
            html.append("<div style='text-align:center; margin: 20px 0;'>");
            html.append("<button onclick='toggleAnalytics()' style='width:auto; background:#34495e; margin-right:10px;'>&#128202; Toggle Charts</button>");
            html.append("<a href='/learn' style='display:inline-block; padding:12px 20px; background:#8e44ad; color:white; text-decoration:none; border-radius:4px; font-size:16px;'>&#128218; Learn Investments</a>");
            html.append("</div>");

            // --- ANALYTICS SECTION ---
            html.append("<div id='analyticsSection' class='grid' style='border-bottom: 1px solid #ddd; padding-bottom: 30px; margin-bottom: 20px;'>");

            // Chart Card
            html.append("<div class='card' style='display:flex; flex-direction:column; align-items:center;'>")
                .append("<h3>Monthly Overview (" + currentMonth.name() + ")</h3>")
                .append("<div style='width:250px; height:250px;'>")
                .append("<canvas id='financeChart'></canvas>")
                .append("</div>")
                .append("<p style='font-size:0.9em; color:#7f8c8d; margin-top:10px;'>")
                .append("<span style='color:#2ecc71'>In: &#8377;").append(monthlyIncome).append("</span> | ")
                .append("<span style='color:#e74c3c'>Out: &#8377;").append(monthlyExpense).append("</span> | ")
                .append("<span style='color:#e67e22'>Inv: &#8377;").append(monthlyInvest).append("</span>")
                .append("</p>")
                .append("</div>");

            // Tips Card
            html.append("<div class='card'>")
                .append("<h3>Financial Health</h3>")
                .append("<div class='guide-item'><b>1. Income (Green):</b><br>Money added or received from others.</div>")
                .append("<div class='guide-item'><b>2. Spending (Red):</b><br>Money withdrawn or transferred. Keep this lower than Income!</div>")
                .append("<div class='guide-item'><b>3. Investments (Orange):</b><br>Money put into FDs or Schemes. This is 'Good Spending' because it grows.</div>")
                .append("</div>");

            html.append("</div>"); // End Analytics

            // --- HISTORY TABLE ---
            html.append("<div style='padding:0 20px; max-width:1200px; margin:0 auto;'>");
            html.append("<h3>Transaction History</h3><table>");
            html.append("<tr><th>Txn ID</th><th>Date</th><th>Type</th><th>Related Name</th><th>Related ID</th><th>Amount</th></tr>");
            html.append(historyRows.toString());
            html.append("</table></div>");

            // --- JAVASCRIPT ---
            html.append("<script>")
                .append("function toggleAnalytics() {")
                .append("  var x = document.getElementById('analyticsSection');")
                .append("  if (x.style.display === 'none') { x.style.display = 'flex'; }")
                .append("  else { x.style.display = 'none'; }")
                .append("}")
                
                .append("var ctx = document.getElementById('financeChart').getContext('2d');")
                .append("var myChart = new Chart(ctx, {")
                .append("    type: 'doughnut',")
                .append("    data: {")
                .append("        labels: ['Income', 'Spending', 'Investments'],")
                .append("        datasets: [{")
                .append("            data: [").append(monthlyIncome).append(", ").append(monthlyExpense).append(", ").append(monthlyInvest).append("],")
                .append("            backgroundColor: ['#2ecc71', '#e74c3c', '#e67e22'],")
                .append("            borderWidth: 1")
                .append("        }]")
                .append("    },")
                .append("    options: { responsive: true, plugins: { legend: { position: 'bottom' } } }")
                .append("});")
                .append("</script>");

            html.append(ViewHelper.getFooter());

            byte[] responseBytes = html.toString().getBytes("UTF-8");
            exchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
            exchange.sendResponseHeaders(200, responseBytes.length);
            OutputStream os = exchange.getResponseBody();
            os.write(responseBytes);
            os.close();

        } catch (Exception e) { e.printStackTrace(); }
    }
}