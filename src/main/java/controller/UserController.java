package controller;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import model.DBConnection;
import org.mindrot.jbcrypt.BCrypt;

public class UserController {

    private service.EmailService emailService = new service.EmailService();

    public boolean isMatched(String password, String confirmPassword) {
        return password.equals(confirmPassword);
    }

    public boolean register(String fullName, String username, String email,
                            String contact, String password) {
        try (Connection conn = DBConnection.connect()) {

            // Check if any admin exists
            String checkAdmin = "SELECT COUNT(*) FROM users WHERE role = 'admin' AND deleted_at IS NULL";
            boolean noAdminExists = false;
            try (PreparedStatement ps = conn.prepareStatement(checkAdmin);
                 ResultSet rs = ps.executeQuery()) {
                if (rs.next()) noAdminExists = rs.getInt(1) == 0;
            }

            // Check email not taken
            String checkQuery = "SELECT * FROM users WHERE email = ? AND deleted_at IS NULL";
            try (PreparedStatement checkStmt = conn.prepareStatement(checkQuery)) {
                checkStmt.setString(1, email);
                try (ResultSet rs = checkStmt.executeQuery()) {
                    if (rs.next()) return false;
                }
            }

            // If no admin exists — first account becomes admin and active
            // Otherwise — pending staff account
            String role = noAdminExists ? "admin" : "staff";
            String status = noAdminExists ? "active" : "pending";

            String insertQuery = """
                INSERT INTO users(fullname, username, email, contact, password, role, status) 
                VALUES(?,?,?,?,?,?,?)
            """;
            try (PreparedStatement pst = conn.prepareStatement(insertQuery)) {
                String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt());
                pst.setString(1, fullName);
                pst.setString(2, username);
                pst.setString(3, email);
                pst.setString(4, contact);
                pst.setString(5, hashedPassword);
                pst.setString(6, role);
                pst.setString(7, status);
                pst.executeUpdate();
            }

            return true;

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean login(String identifier, String password) {
        String sql = "SELECT * FROM users WHERE (username = ? OR email = ?) AND status='active' AND deleted_at IS NULL";
        try (Connection conn = DBConnection.connect();
             PreparedStatement pst = conn.prepareStatement(sql)) {

            pst.setString(1, identifier);
            pst.setString(2, identifier);

            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    String storedHash = rs.getString("password");
                    return BCrypt.checkpw(password, storedHash);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public ResultSet getUsers() {
        try {
            // Note: ResultSet must stay open for caller to use
            // Connection intentionally not closed here
            Connection conn = DBConnection.connect();
            String query = "SELECT fullname, email, contact, password FROM users WHERE deleted_at IS NULL";
            PreparedStatement pst = conn.prepareStatement(query);
            return pst.executeQuery();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean isEmailRegistered(String email) {
        // Fix: exclude deleted accounts
        String sql = "SELECT * FROM users WHERE email = ? AND deleted_at IS NULL";
        try (Connection conn = DBConnection.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return false;
    }

    public boolean isUsernameRegistered(String username) {
        // Fix: exclude deleted accounts
        String sql = "SELECT * FROM users WHERE username = ? AND deleted_at IS NULL";
        try (Connection conn = DBConnection.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return false;
    }

    public boolean isAdmin(String identifier) throws SQLException {
        String sql = "SELECT * FROM users WHERE (email = ? OR username = ?) AND role='admin' AND deleted_at IS NULL";
        try (Connection conn = DBConnection.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, identifier);
            ps.setString(2, identifier);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    public boolean sendPasswordResetCode(String email) {
        try (Connection conn = DBConnection.connect()) {

            String checkQuery = "SELECT email FROM users WHERE email = ? AND deleted_at IS NULL";
            try (PreparedStatement checkStmt = conn.prepareStatement(checkQuery)) {
                checkStmt.setString(1, email);
                try (ResultSet rs = checkStmt.executeQuery()) {
                    if (!rs.next()) return false;
                }
            }

            String code = emailService.generateVerificationCode();
            long timestamp = System.currentTimeMillis();

            String updateQuery = "UPDATE users SET verification_code = ?, code_timestamp = ? WHERE email = ?";
            try (PreparedStatement updateStmt = conn.prepareStatement(updateQuery)) {
                updateStmt.setString(1, code);
                updateStmt.setLong(2, timestamp);
                updateStmt.setString(3, email);
                updateStmt.executeUpdate();
            }

            return emailService.sendVerificationEmail(email, code);

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean verifyResetCode(String email, String code) {
        String sql = "SELECT verification_code, code_timestamp FROM users WHERE email = ?";
        try (Connection conn = DBConnection.connect();
             PreparedStatement pst = conn.prepareStatement(sql)) {

            pst.setString(1, email);
            try (ResultSet rs = pst.executeQuery()) {
                if (!rs.next()) return false;

                String savedCode = rs.getString("verification_code");
                long timestamp = rs.getLong("code_timestamp");
                long tenMinutes = 10 * 60 * 1000;

                if ((System.currentTimeMillis() - timestamp) > tenMinutes) return false;

                return savedCode != null && savedCode.equals(code);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean resetPassword(String email, String newPassword) {
        String sql = "UPDATE users SET password = ?, verification_code = NULL, code_timestamp = NULL WHERE email = ?";
        try (Connection conn = DBConnection.connect();
             PreparedStatement pst = conn.prepareStatement(sql)) {

            pst.setString(1, BCrypt.hashpw(newPassword, BCrypt.gensalt()));
            pst.setString(2, email);
            return pst.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public model.User getUserByIdentifier(String identifier) {
        String sql = "SELECT * FROM users WHERE (username = ? OR email = ?) AND status = 'active' AND deleted_at IS NULL";
        try (Connection conn = DBConnection.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, identifier);
            ps.setString(2, identifier);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new model.User(
                        rs.getString("fullname"),
                        rs.getString("username"),
                        rs.getString("email"),
                        rs.getString("contact"),
                        rs.getString("password"),
                        rs.getString("role"),
                        rs.getString("status")
                    );
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
    
    public boolean isPendingAccount(String identifier) {
        String sql = """
            SELECT * FROM users 
            WHERE (username = ? OR email = ?) 
            AND status = 'pending'
            AND deleted_at IS NULL
        """;
        try (Connection conn = DBConnection.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, identifier);
            ps.setString(2, identifier);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
    
    public boolean registerByAdmin(String fullName, String username, 
                                    String email, String contact, 
                                    String password) {
        try (Connection conn = DBConnection.connect()) {

            String checkQuery = "SELECT * FROM users WHERE email = ? AND deleted_at IS NULL";
            try (PreparedStatement checkStmt = conn.prepareStatement(checkQuery)) {
                checkStmt.setString(1, email);
                try (ResultSet rs = checkStmt.executeQuery()) {
                    if (rs.next()) return false;
                }
            }

            String insertQuery = """
                INSERT INTO users(fullname, username, email, contact, password, status) 
                VALUES(?,?,?,?,?,'active')
            """;
            try (PreparedStatement pst = conn.prepareStatement(insertQuery)) {
                String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt());
                pst.setString(1, fullName);
                pst.setString(2, username);
                pst.setString(3, email);
                pst.setString(4, contact);
                pst.setString(5, hashedPassword);
                pst.executeUpdate();
            }

            return true;

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public String capitalize(String str) {
        if (str == null || str.length() == 0) return str;
        return str.substring(0, 1).toUpperCase() + str.substring(1).toLowerCase();
    }
}