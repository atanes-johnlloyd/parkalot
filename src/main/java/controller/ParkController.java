package controller;

import java.sql.*;
import model.DBConnection;

public class ParkController {

    // Fix: removed Connection field and constructor
    // Each method now gets its own connection via try-with-resources

    public ResultSet getAllSlots() throws SQLException {
        // Note: Connection intentionally not closed here
        // ResultSet needs connection to stay open for caller to use
        Connection conn = DBConnection.connect();
        String sql = "SELECT * FROM parking_slots ORDER BY slot_number";
        PreparedStatement ps = conn.prepareStatement(sql);
        return ps.executeQuery();
    }

    public String getSlotStatus(int slotNumber) throws SQLException {
        String sql = "SELECT status FROM parking_slots WHERE slot_number = ?";
        try (Connection conn = DBConnection.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, slotNumber);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getString("status");
            }
        }
        return "available";
    }

    public String getSlotVehicle(int slotNumber) throws SQLException {
        String sql = "SELECT vehicle FROM parking_slots WHERE slot_number = ?";
        try (Connection conn = DBConnection.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, slotNumber);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getString("vehicle");
            }
        }
        return "car";
    }

    public void updateSlotStatus(int slotNumber, String status) throws SQLException {
        String sql = "UPDATE parking_slots SET status = ? WHERE slot_number = ?";
        try (Connection conn = DBConnection.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setInt(2, slotNumber);
            ps.executeUpdate();
        }
    }

    public void updateSlotVehicle(int slotNumber, String vehicle) throws SQLException {
        String sql = "UPDATE parking_slots SET vehicle = ? WHERE slot_number = ?";
        try (Connection conn = DBConnection.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, vehicle);
            ps.setInt(2, slotNumber);
            ps.executeUpdate();
        }
    }

    public void resetSlot(int slotNumber) throws SQLException {
        String sql = """
            UPDATE parking_slots 
            SET status = 'available', plate_number = NULL, 
            contact = NULL, time_in = NULL 
            WHERE slot_number = ?
        """;
        try (Connection conn = DBConnection.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, slotNumber);
            ps.executeUpdate();
        }
    }

    // Fix: removed close() method — no longer needed
}