package controller;

import java.sql.*;
import javax.swing.table.DefaultTableModel;
import model.DBConnection;

public class AdminUserController {

    // Load all users into a table model
    public DefaultTableModel getAllUsers() {
        DefaultTableModel model = new DefaultTableModel(
            new String[]{"Name", "Username", "Email", "Contact", "Position", "Status", "Actions"}, 0
        );

        try (Connection conn = DBConnection.connect()) {
            // Fix 1: exclude soft-deleted users
            String sql = "SELECT fullname, username, email, contact, role, status FROM users WHERE deleted_at IS NULL";
            PreparedStatement ps = conn.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                model.addRow(new Object[]{
                    rs.getString("fullname"),
                    rs.getString("username"),
                    rs.getString("email"),
                    rs.getString("contact"),
                    rs.getString("role"),
                    rs.getString("status"),
                    null
                });
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return model;
    }

    // Search users by name, email, or contact
    public DefaultTableModel searchUsers(String keyword) {
        DefaultTableModel model = new DefaultTableModel(
            new String[]{"Name", "Username", "Email", "Contact", "Position", "Status", "Actions"}, 0
        );

        try (Connection conn = DBConnection.connect()) {
            // Fix 2: exclude soft-deleted users from search too
            String sql = "SELECT fullname, username, email, contact, role, status FROM users " +
                         "WHERE deleted_at IS NULL " +
                         "AND (fullname LIKE ? OR email LIKE ? OR contact LIKE ? OR username LIKE ? OR role LIKE ? OR status LIKE ?)";
            PreparedStatement ps = conn.prepareStatement(sql);
            String key = "%" + keyword + "%";
            for (int i = 1; i <= 6; i++) ps.setString(i, key);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                model.addRow(new Object[]{
                    rs.getString("fullname"),
                    rs.getString("username"),
                    rs.getString("email"),
                    rs.getString("contact"),
                    rs.getString("role"),
                    rs.getString("status"),
                    null
                });
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return model;
    }
    
    public boolean softDeleteUser(String username) {
        try (Connection conn = DBConnection.connect()) {

            // Get role of user being deleted
            String role = "";
            String checkRole = "SELECT role FROM users WHERE username = ?";
            try (PreparedStatement ps = conn.prepareStatement(checkRole)) {
                ps.setString(1, username);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) role = rs.getString("role");
                }
            }

            // If admin being deleted — check if they're the last one
            if ("admin".equalsIgnoreCase(role)) {
                int adminCount = getActiveAdminCount();
                if (adminCount <= 1) {
                    return false; // last admin — block deletion
                }
            }

            // Proceed with soft delete
            String sql = "UPDATE users SET deleted_at = NOW() WHERE username = ?";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, username);
                return ps.executeUpdate() > 0;
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
    
    public int getUserIdByUsername(String username) {
        String sql = "SELECT id FROM users WHERE username = ?";
        try (Connection conn = DBConnection.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt("id");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }
    
    public void logAction(String adminUsername, String actionType, String targetTable,
                          int targetId, String oldValue, String newValue) {
        String sql = """
            INSERT INTO admin_logs(admin_username, action_type, target_table, target_id, old_value, new_value)
            VALUES (?, ?, ?, ?, ?, ?)
        """;
        try (Connection conn = DBConnection.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, adminUsername);
            ps.setString(2, actionType);
            ps.setString(3, targetTable);
            ps.setInt(4, targetId);
            ps.setString(5, oldValue);
            ps.setString(6, newValue);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public boolean isActive(String username) throws SQLException {
        String sql = "SELECT * FROM users WHERE username = ? AND status = 'active' AND deleted_at IS NULL";
        try (Connection conn = DBConnection.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            ResultSet rs = ps.executeQuery();
            return rs.next();
        }
    }
    
    public boolean recoverUser(String username) {
        String sql = "UPDATE users SET deleted_at = NULL, status = 'active' WHERE username = ?";
        try (Connection conn = DBConnection.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    public DefaultTableModel getDeletedUsers() {
        DefaultTableModel model = new DefaultTableModel(
            new String[]{"Name", "Username", "Email", "Contact", "Position", "Status"}, 0
        );
        try (Connection conn = DBConnection.connect()) {
            String sql = "SELECT fullname, username, email, contact, role, status FROM users WHERE deleted_at IS NOT NULL";
            PreparedStatement ps = conn.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                model.addRow(new Object[]{
                    rs.getString("fullname"),
                    rs.getString("username"),
                    rs.getString("email"),
                    rs.getString("contact"),
                    rs.getString("role"),
                    rs.getString("status")
                });
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return model;
    }
    
    public int getActiveAdminCount() {
        String sql = "SELECT COUNT(*) FROM users WHERE role = 'admin' AND deleted_at IS NULL AND status = 'active'";
        try (Connection conn = DBConnection.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

}