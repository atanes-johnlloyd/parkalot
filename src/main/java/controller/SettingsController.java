package controller;
import java.sql.*;
import model.DBConnection;

public class SettingsController {
    public String getSetting(String key) {
        String sql = "SELECT setting_value FROM settings WHERE setting_key = ?";
        try (Connection conn = DBConnection.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, key);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getString("setting_value");
            }
        } catch (SQLException e) {
            System.out.println("Error getting setting " + key + ": " + e.getMessage());
        }
        return "";
    }

    public boolean updateSetting(String key, String value) {
        String sql = "INSERT INTO settings (setting_key, setting_value) VALUES (?, ?) " +
                     "ON DUPLICATE KEY UPDATE setting_value = ?";
        try (Connection conn = DBConnection.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, key);
            ps.setString(2, value);
            ps.setString(3, value);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("Error updating setting " + key + ": " + e.getMessage());
            return false;
        }
    }

    public java.util.Properties getAllSettings() {
        java.util.Properties props = new java.util.Properties();
        String sql = "SELECT setting_key, setting_value FROM settings";
        try (Connection conn = DBConnection.connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                props.setProperty(rs.getString("setting_key"), rs.getString("setting_value"));
            }
        } catch (SQLException e) {
            System.out.println("Error getting all settings: " + e.getMessage());
        }
        return props;
    }

    public boolean updateAllSettings(java.util.Properties props) {
        boolean success = true;
        for (String key : props.stringPropertyNames()) {
            if (!updateSetting(key, props.getProperty(key))) {
                success = false;
            }
        }
        return success;
    }
    
    // Convenience methods for specific settings
    public String getLotName() {
        return getSetting("lot_name");
    }
    
    public void setLotName(String value) {
        updateSetting("lot_name", value);
    }
    
    public String getLocation() {
        return getSetting("location");
    }
    
    public void setLocation(String value) {
        updateSetting("location", value);
    }
    
    public String getCurrency() {
        return getSetting("currency");
    }
    
    public void setCurrency(String value) {
        updateSetting("currency", value);
    }
    
    public String getTimeZone() {
        return getSetting("timezone");
    }
    
    public void setTimeZone(String value) {
        updateSetting("timezone", value);
    }
    
    public String getWeekendRate() {
        return getSetting("weekend_rate");
    }
    
    public void setWeekendRate(String value) {
        updateSetting("weekend_rate", value);
    }
    
    public String getWeekdayRate() {
        return getSetting("weekday_rate");
    }
    
    public void setWeekdayRate(String value) {
        updateSetting("weekday_rate", value);
    }
    
    public String getMaxStay() {
        return getSetting("max_stay");
    }
    
    public void setMaxStay(String value) {
        updateSetting("max_stay", value);
    }
    
    public String getOverstayPenalty() {
        return getSetting("overstay_penalty");
    }
    
    public void setOverstayPenalty(String value) {
        updateSetting("overstay_penalty", value);
    }
    
    public String getMaxAdvance() {
        return getSetting("max_advance");
    }
    
    public void setMaxAdvance(String value) {
        updateSetting("max_advance", value);
    }
    
    public String getMaxReservationSlot() {
        return getSetting("max_reservation_slot");
    }
    
    public void setMaxReservationSlot(String value) {
        updateSetting("max_reservation_slot", value);
    }
    
    public String getGracePeriod() {
        return getSetting("grace_period");
    }
    
    public void setGracePeriod(String value) {
        updateSetting("grace_period", value);
    }
    
    public String getAllowPrintTicket() {
    return getSetting("allow_print_ticket");
    }

    public void setAllowPrintTicket(String value) {
        updateSetting("allow_print_ticket", value);
    }

    public boolean isPrintTicketEnabled() {
        return "yes".equals(getSetting("allow_print_ticket"));
    }
    
    public String getCancellationWindow() {
        return getSetting("cancellation_window");
    }
    
    public void setCancellationWindow(String value) {
        updateSetting("cancellation_window", value);
    }
    
    public String getNotifyOnMissed() {
        return getSetting("notify_on_missed");
    }

    public void setNotifyOnMissed(String value) {
        updateSetting("notify_on_missed", value);
    }

    public boolean isNotifyOnMissedEnabled() {
        return "yes".equals(getSetting("notify_on_missed"));
    }
    
}