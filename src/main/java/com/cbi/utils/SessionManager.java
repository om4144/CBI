package com.cbi.utils;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;


public class SessionManager {
    // Map SessionID -> Username
    private static final Map<String, String> sessions = new HashMap<>();

    public static String createSession(String username) {
        String sessionId = UUID.randomUUID().toString();
        sessions.put(sessionId, username);
        return sessionId;
    }

    public static String getUserFromSession(String cookieHeader) {
        if (cookieHeader == null) return null;
        for (String cookie : cookieHeader.split(";")) {
            String[] parts = cookie.trim().split("=");
            if (parts.length == 2 && parts[0].equals("session_id")) {
                return sessions.get(parts[1]);
            }
        }
        return null;
    }
    
    public static void removeSession(String cookieHeader) {
        if (cookieHeader == null) return;
        for (String cookie : cookieHeader.split(";")) {
            String[] parts = cookie.trim().split("=");
            if (parts[0].equals("session_id")) sessions.remove(parts[1]);
        }
    }
}