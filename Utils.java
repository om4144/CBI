import com.sun.net.httpserver.HttpExchange;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.net.URLDecoder;

public class Utils {
    public static Map<String, String> parseParams(HttpExchange exchange) throws IOException {
        Map<String, String> result = new HashMap<>();
        InputStreamReader isr = new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8);
        BufferedReader br = new BufferedReader(isr);
        String query = br.readLine();
        if (query != null) {
            String[] pairs = query.split("&");
            for (String pair : pairs) {
                String[] idx = pair.split("=");
                if (idx.length > 1)
                    result.put(URLDecoder.decode(idx[0], "UTF-8"), URLDecoder.decode(idx[1], "UTF-8"));
            }
        }
        return result;
    }
}