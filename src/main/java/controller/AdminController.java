package controller;

import java.sql.*;
import model.DBConnection;

public class AdminController {
    
    public String getCurrentOccupancy() {
        try (Connection conn = DBConnection.connect()) {
            // Get occupied count
            String occupiedSql = "SELECT COUNT(*) FROM parking_slots WHERE status = 'occupied'";
            PreparedStatement ps1 = conn.prepareStatement(occupiedSql);
            ResultSet rs1 = ps1.executeQuery();
            int occupied = rs1.next() ? rs1.getInt(1) : 0;

            // Get total count
            String totalSql = "SELECT COUNT(*) FROM parking_slots";
            PreparedStatement ps2 = conn.prepareStatement(totalSql);
            ResultSet rs2 = ps2.executeQuery();
            int total = rs2.next() ? rs2.getInt(1) : 0;

            return occupied + "/" + total;

        } catch (SQLException e) {
            System.out.println("Error getting occupancy: " + e.getMessage());
        }
        return "0/0";
    }
    
        // Add this method to get Active/Parked Vehicles count
    public int getActiveVehicles() {
        try (Connection conn = DBConnection.connect();
             PreparedStatement ps = conn.prepareStatement(
                 "SELECT COUNT(*) FROM parking_slots WHERE status = 'occupied'");
             ResultSet rs = ps.executeQuery()) {

            if (rs.next()) return rs.getInt(1);

        } catch (SQLException e) {
            System.out.println("Error getting active vehicles: " + e.getMessage());
        }
        return 0;
    }

    // Add this method to get Average Stay Time
    public String getAverageStayTime() {
        try (Connection conn = DBConnection.connect();
             PreparedStatement ps = conn.prepareStatement(
                 "SELECT AVG(TIMESTAMPDIFF(MINUTE, time_in, NOW())) FROM parking_slots " +
                 "WHERE status = 'occupied' AND time_in IS NOT NULL");
             ResultSet rs = ps.executeQuery()) {

            if (rs.next()) {
                int avgMinutes = rs.getInt(1);
                if (avgMinutes > 0) {
                    int hours = avgMinutes / 60;
                    int minutes = avgMinutes % 60;
                    return hours + "h " + minutes + "m";
                }
            }

        } catch (SQLException e) {
            System.out.println("Error getting average stay: " + e.getMessage());
        }
        return "0h 0m";
    }
    
    public int getDailyParkingCount() {
        try (Connection conn = DBConnection.connect();
             PreparedStatement ps = conn.prepareStatement(
                 "SELECT COUNT(*) FROM parking_logs " +
                 "WHERE DATE(action_time) = CURDATE() AND action_type = 'time_in'");
             ResultSet rs = ps.executeQuery()) {

            if (rs.next()) return rs.getInt(1);

        } catch (SQLException e) {
            System.out.println("Error getting daily count: " + e.getMessage());
        }
        return 0;
    }

    public int getAverageDailyParking() {
        try (Connection conn = DBConnection.connect();
             PreparedStatement ps = conn.prepareStatement(
                 "SELECT AVG(daily_count) FROM " +
                 "(SELECT COUNT(*) as daily_count FROM parking_logs " +
                 "WHERE action_type = 'time_in' GROUP BY DATE(action_time)) as daily");
             ResultSet rs = ps.executeQuery()) {

            if (rs.next()) return (int) rs.getDouble(1);

        } catch (SQLException e) {
            System.out.println("Error getting average daily: " + e.getMessage());
        }
        return 0;
    }
    
    public String getTotalEarned() {
        try (Connection conn = DBConnection.connect()) {
            // Get currency from settings
            double weekdayRate = getSettingValue(conn, "weekday_rate", 7.0);
            String currency = "";
            try (PreparedStatement ps = conn.prepareStatement(
                    "SELECT setting_value FROM settings WHERE setting_key = 'currency'");
                 ResultSet rs = ps.executeQuery()) {
                if (rs.next()) currency = rs.getString("setting_value");
            }

            String sql = "SELECT COALESCE(SUM(fee), 0) FROM transactions WHERE DATE(transaction_time) = CURDATE()";
            try (PreparedStatement ps = conn.prepareStatement(sql);
                 ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    double total = rs.getDouble(1);
                    return currency + String.format("%.2f", total);
                }
            }

        } catch (SQLException e) {
            System.out.println("Error getting total earned: " + e.getMessage());
        }
        return "0.00";
    }

    public String getLastHourEarnings() {
        try (Connection conn = DBConnection.connect()) {
            // Get currency
            String currency = "";
            try (PreparedStatement ps = conn.prepareStatement(
                    "SELECT setting_value FROM settings WHERE setting_key = 'currency'");
                 ResultSet rs = ps.executeQuery()) {
                if (rs.next()) currency = rs.getString("setting_value");
            }

            try (PreparedStatement ps = conn.prepareStatement(
                    "SELECT COALESCE(SUM(fee), 0) FROM transactions " +
                    "WHERE transaction_time >= DATE_SUB(NOW(), INTERVAL 1 HOUR)");
                 ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    double earned = rs.getDouble(1);
                    return "+" + currency + String.format("%.2f", earned) + " from last hour";
                }
            }

        } catch (SQLException e) {
            System.out.println("Error getting last hour earnings: " + e.getMessage());
        }
        return "+0.00 from last hour";
    }
    
    // Update this method - remove ImageIcon from controller, just return the text
    public String[] getRecentActivities() {
        String[] activities = new String[4];

        try (Connection conn = DBConnection.connect();
             PreparedStatement ps = conn.prepareStatement(
                 "SELECT plate_number, action_type, action_time FROM parking_logs " +
                 "ORDER BY action_time DESC LIMIT 4");
             ResultSet rs = ps.executeQuery()) {

            int index = 0;
            while (rs.next() && index < 4) {
                String plate = rs.getString("plate_number");
                String action = rs.getString("action_type");
                Timestamp time = rs.getTimestamp("action_time");

                java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("h:mm a");
                String timeStr = sdf.format(time);

                if (action.equals("time_in")) {
                    // Fix: "Entered" not "Entred"
                    activities[index] = "Vehicle " + plate + " Entered - " + timeStr;
                } else if (action.equals("time_out")) {
                    activities[index] = "Vehicle " + plate + " Exited - " + timeStr;
                } else {
                    activities[index] = "Vehicle " + plate + " Reserved - " + timeStr;
                }
                index++;
            }

            // Fill remaining slots with empty string instead of hardcoded fake data
            for (int i = 0; i < 4; i++) {
                if (activities[i] == null) activities[i] = "";
            }

        } catch (SQLException e) {
            System.out.println("Error getting activities: " + e.getMessage());
            for (int i = 0; i < 4; i++) activities[i] = "";
        }

        return activities;
    }
    
    public Object[] getCapacityStatus() {
        Object[] capacityData = new Object[4];

        try (Connection conn = DBConnection.connect()) {

            String totalSql = "SELECT COUNT(*) FROM parking_slots";
            PreparedStatement ps1 = conn.prepareStatement(totalSql);
            ResultSet rs1 = ps1.executeQuery();
            int total = rs1.next() ? rs1.getInt(1) : 0;

            String availableSql = "SELECT COUNT(*) FROM parking_slots WHERE status = 'available'";
            PreparedStatement ps2 = conn.prepareStatement(availableSql);
            ResultSet rs2 = ps2.executeQuery();
            int available = rs2.next() ? rs2.getInt(1) : 0;

            capacityData[1] = available;
            capacityData[3] = (available < 5); // critical if less than 5 available

            if (available > (total / 2)) {
                capacityData[0] = "Good Availability";
                capacityData[2] = "Plenty of space today";
            } else if (available > (total / 5)) {
                capacityData[0] = "Moderate Availability";
                capacityData[2] = available + " spots available";
            } else if (available > 0) {
                capacityData[0] = "Limited Availability";
                capacityData[2] = "Only " + available + " spots left";
            } else {
                capacityData[0] = "FULL";
                capacityData[2] = "No spots available";
            }

        } catch (SQLException e) {
            System.out.println("Error getting capacity: " + e.getMessage());
            capacityData[0] = "Unknown";
            capacityData[1] = 0;
            capacityData[2] = "Error loading capacity";
            capacityData[3] = false;
        }

        return capacityData;
    }
    
    private double getSettingValue(Connection conn, String key, double defaultValue) {
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT setting_value FROM settings WHERE setting_key = ?")) {
            ps.setString(1, key);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return Double.parseDouble(rs.getString("setting_value"));
            }
        } catch (Exception e) {
            System.out.println("Error getting setting " + key + ": " + e.getMessage());
        }
        return defaultValue;
    }
}