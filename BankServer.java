import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.net.InetSocketAddress;

public class BankServer {
    public static void main(String[] args) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);

        server.createContext("/login", new AuthHandler());
        server.createContext("/register", new AuthHandler());
        server.createContext("/dashboard", new DashboardHandler());
        server.createContext("/transaction", new TransactionHandler());
        server.createContext("/learn", new LearnHandler()); // <--- New Route
        
        server.createContext("/", exchange -> {
            exchange.getResponseHeaders().set("Location", "/login");
            exchange.sendResponseHeaders(302, -1);
        });

        server.setExecutor(null);
        System.out.println("Bank Server is running on http://localhost:8080");
        server.start();
    }
}