import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.IOException;
import java.io.OutputStream;

public class LearnHandler implements HttpHandler {
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String username = SessionManager.getUserFromSession(exchange.getRequestHeaders().getFirst("Cookie"));
        if (username == null) {
            exchange.getResponseHeaders().set("Location", "/login");
            exchange.sendResponseHeaders(302, -1);
            return;
        }

        String html = ViewHelper.getHeader("CBI Learning") +
            "<div class='navbar'>" +
            "<div><h1>CBI Academy</h1><small>Financial Literacy</small></div>" +
            "<a href='/dashboard' class='btn-logout' style='background-color:#2980b9;'>Back to Dashboard</a>" +
            "</div>" +
            
            "<div style='max-width:800px; margin:40px auto;'>" +
            
            // Block 1: Intro
            "<div class='card' style='max-width:100%; margin-bottom:20px;'>" +
            "<h2>Why Invest?</h2>" +
            "<p>Keeping money in a savings account is safe, but inflation reduces its value over time. " +
            "Investing allows your money to grow through the power of <b>Compound Interest</b>.</p>" +
            "</div>" +

            // Block 2: FDs
            "<div class='card' style='max-width:100%; margin-bottom:20px; border-left: 5px solid #e67e22;'>" +
            "<h3>1. Fixed Deposits (FD)</h3>" +
            "<p><b>Risk:</b> Low | <b>Return:</b> 6-8%</p>" +
            "<p>You lock a specific amount for a fixed tenure (e.g., 1 year). In return, the bank guarantees a fixed interest rate. " +
            "This is safer than stocks but offers lower returns.</p>" +
            "</div>" +

            // Block 3: Govt Schemes
            "<div class='card' style='max-width:100%; margin-bottom:20px; border-left: 5px solid #27ae60;'>" +
            "<h3>2. Government Schemes</h3>" +
            "<p><b>Risk:</b> Zero (Sovereign Guarantee) | <b>Return:</b> 7-9%</p>" +
            "<p>Schemes like <i>PPF (Public Provident Fund)</i> or <i>NSC</i> are backed by the government. " +
            "They often come with tax benefits and are excellent for long-term goals like retirement.</p>" +
            "</div>" +

            // Block 4: Tips
            "<div class='card' style='max-width:100%; margin-bottom:20px;'>" +
            "<h3>The Rule of 72</h3>" +
            "<p>Want to know when your money will double? Divide 72 by your interest rate.</p>" +
            "<ul>" +
            "<li>At 6% return: 72 / 6 = 12 years to double.</li>" +
            "<li>At 12% return: 72 / 12 = 6 years to double.</li>" +
            "</ul>" +
            "</div>" +

            "<div style='text-align:center;'>" +
            "<a href='/dashboard' style='background:#2c3e50; color:white; padding:15px 30px; text-decoration:none; border-radius:5px;'>Start Investing Now</a>" +
            "</div>" +

            "</div>" + // End container
            ViewHelper.getFooter();

        byte[] responseBytes = html.getBytes("UTF-8");
        exchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
        exchange.sendResponseHeaders(200, responseBytes.length);
        OutputStream os = exchange.getResponseBody();
        os.write(responseBytes);
        os.close();
    }
}