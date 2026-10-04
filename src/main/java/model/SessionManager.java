package model;

public class SessionManager {
    
    private static String loggedInUsername;
    private static String loggedInRole;
    private static String loggedInFullName;
    
    // Set session on login
    public static void login(String username, String role, String fullName) {
        loggedInUsername = username;
        loggedInRole = role;
        loggedInFullName = fullName;
    }
    
    // Clear session on logout
    public static void logout() {
        loggedInUsername = null;
        loggedInRole = null;
        loggedInFullName = null;
    }
    
    public static String getUsername() {
        return loggedInUsername;
    }
    
    public static String getRole() {
        return loggedInRole;
    }
    
    public static String getFullName() {
        return loggedInFullName;
    }
    
    public static boolean isAdmin() {
        return "admin".equalsIgnoreCase(loggedInRole);
    }
    
    public static boolean isLoggedIn() {
        return loggedInUsername != null;
    }
}