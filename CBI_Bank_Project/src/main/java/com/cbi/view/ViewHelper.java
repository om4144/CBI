package com.cbi.view;

public class ViewHelper {
    public static final String CSS = 
        "<style>" +
        "body { font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; background-color: #f4f7f6; margin: 0; padding: 0; }" +
        ".navbar { background-color: #2c3e50; color: white; padding: 15px 20px; display: flex; justify-content: space-between; align-items: center; box-shadow: 0 2px 5px rgba(0,0,0,0.1); }" +
        ".navbar h1 { margin: 0; font-size: 24px; }" +
        ".container { max-width: 450px; margin: 40px auto; background: white; padding: 30px; border-radius: 8px; box-shadow: 0 4px 15px rgba(0,0,0,0.1); text-align: center; }" +
        ".grid { display: flex; flex-wrap: wrap; gap: 20px; padding: 20px; justify-content: center; }" +
        ".card { background: white; padding: 25px; border-radius: 8px; box-shadow: 0 2px 8px rgba(0,0,0,0.08); flex: 1; min-width: 300px; max-width: 400px; }" +
        "input, select, textarea { width: 100%; padding: 12px; margin: 10px 0; border: 1px solid #ddd; border-radius: 4px; box-sizing: border-box; font-size: 16px; }" +
        "button { width: 100%; background-color: #27ae60; color: white; padding: 12px; margin-top: 10px; border: none; border-radius: 4px; cursor: pointer; font-size: 16px; transition: background 0.3s; }" +
        "button:hover { background-color: #219150; }" +
        ".btn-logout { background-color: #c0392b; padding: 8px 15px; text-decoration: none; color: white; border-radius: 4px; font-size: 14px; }" +
        ".btn-logout:hover { background-color: #a93226; }" +
        "table { width: 100%; border-collapse: collapse; margin-top: 20px; background: white; box-shadow: 0 2px 8px rgba(0,0,0,0.08); }" +
        "th, td { padding: 12px 15px; text-align: left; border-bottom: 1px solid #ddd; }" +
        "th { background-color: #34495e; color: white; }" +
        "tr:hover { background-color: #f1f1f1; }" +
        "h2, h3 { color: #2c3e50; margin-top: 0; }" +
        ".balance { font-size: 2em; color: #27ae60; font-weight: bold; margin: 10px 0; }" +
        ".guide-item { margin-bottom: 10px; padding: 10px; background: #eaf2f8; border-left: 4px solid #3498db; border-radius: 4px; font-size: 0.9em; }" +
        
        // --- NEW CSS FOR PASSWORD TOGGLE ---
        ".password-container { position: relative; width: 100%; }" +
        ".password-container input { width: 100%; padding-right: 40px; }" + // Make room for icon
        ".toggle-icon { position: absolute; right: 10px; top: 50%; transform: translateY(-50%); cursor: pointer; font-size: 18px; color: #7f8c8d; user-select: none; }" +
        "</style>" +
        
        // --- JAVASCRIPT FOR TOGGLE ---
        "<script>" +
        "function togglePass(id) {" +
        "  var x = document.getElementById(id);" +
        "  var icon = document.getElementById('icon-' + id);" +
        "  if (x.type === 'password') { " +
        "    x.type = 'text'; " +
        "    icon.innerHTML = '&#128065;'; " + // Open Eye
        "  } else { " +
        "    x.type = 'password'; " +
        "    icon.innerHTML = '&#128064;'; " + // Closed Eye (Slash equivalent)
        "  }" +
        "}" +
        "</script>";

    public static String getHeader(String title) {
        return "<html><head><title>" + title + "</title>" + CSS + 
            "<meta charset='UTF-8'>" +
            "<script src='https://cdn.jsdelivr.net/npm/chart.js'></script>" +
            "</head><body>";
    }

    public static String getFooter() {
        return "</body></html>";
    }
}