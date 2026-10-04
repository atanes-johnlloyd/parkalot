package controller;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.LinkedHashMap;
import java.util.Map;
import model.DBConnection;

public class AnalyticsController {

    public int getAverageDailyPark() {
        String sql = """
            SELECT AVG(daily_count) AS avg_daily
            FROM (
                SELECT DATE(action_time) AS day, COUNT(*) AS daily_count
                FROM parking_logs
                WHERE action_type='time_in'
                GROUP BY DATE(action_time)
            ) AS daily_totals
        """;
        try (Connection conn = DBConnection.connect();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            if (rs.next()) return rs.getInt("avg_daily");

        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    public int getAverageDailyReserves() {
        String sql = """
            SELECT AVG(daily_count) AS average_daily_reserves
            FROM (
                SELECT DATE(reservation_time), COUNT(*) AS daily_count
                FROM reservations
                GROUP BY DATE(reservation_time)
            ) AS daily_totals
        """;
        try (Connection conn = DBConnection.connect();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            if (rs.next()) return rs.getInt("average_daily_reserves");

        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    public String getMostVehicleType() {
        String sql = """
            SELECT vehicle FROM (
                SELECT vehicle, COUNT(*) AS total
                FROM parking_logs
                WHERE action_type='time_in'
                GROUP BY vehicle
            ) AS sub
            ORDER BY total DESC
            LIMIT 1
        """;
        try (Connection conn = DBConnection.connect();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            if (rs.next()) return rs.getString("vehicle");

        } catch (Exception e) {
            e.printStackTrace();
        }
        return "-";
    }

    public String getMostBusyDay() {
        String sql = """
            SELECT DAYNAME(action_time) AS day
            FROM parking_logs
            WHERE action_type = 'time_in'
            AND action_time >= NOW() - INTERVAL 90 DAY
            GROUP BY DAYOFWEEK(action_time), DAYNAME(action_time)
            ORDER BY COUNT(*) DESC
            LIMIT 1
        """;
        try (Connection conn = DBConnection.connect();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            if (rs.next()) return rs.getString("day");

        } catch (Exception e) {
            e.printStackTrace();
        }
        return "-";
    }

    public Map<String, Integer> getDailyParkChartData() {
        Map<String, Integer> dataset = new LinkedHashMap<>();
        String sql = """
            SELECT 
                DATE(action_time) AS day_date,
                DAYNAME(action_time) AS day_name,
                COUNT(*) AS total
            FROM parking_logs
            WHERE action_type = 'time_in'
            AND DATE(action_time) >= CURDATE() - INTERVAL 6 DAY
            GROUP BY DATE(action_time), DAYNAME(action_time)
            ORDER BY DATE(action_time) ASC
        """;
        try (Connection conn = DBConnection.connect();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                dataset.put(rs.getString("day_name"), rs.getInt("total"));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return dataset;
    }

    public Map<String, Integer> getVehiclePieChartData() {
        Map<String, Integer> dataset = new LinkedHashMap<>();
        String sql = """
            SELECT vehicle, COUNT(*) AS total
            FROM parking_logs
            WHERE action_type='time_in'
            GROUP BY vehicle
        """;
        try (Connection conn = DBConnection.connect();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                dataset.put(rs.getString("vehicle"), rs.getInt("total"));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return dataset;
    }

    public String capitalize(String str) {
        if (str == null || str.length() == 0) return str;
        return str.substring(0, 1).toUpperCase() + str.substring(1).toLowerCase();
    }
}