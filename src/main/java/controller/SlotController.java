package controller;

import java.sql.*;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import model.DBConnection;
import model.ParkingSession;
import model.TicketInfo;

public class SlotController {
        
        public ResultSet getSlots() throws SQLException{
        Connection conn = DBConnection.connect();
        String sql = "SELECT * FROM parking_slots";
        PreparedStatement ps = conn.prepareStatement(sql);
        return ps.executeQuery();
        
    }
    
    public boolean parkVehicle(int slot, String plate, String contact) {

        boolean success = false;

        try {

            Connection conn = DBConnection.connect();

            conn.setAutoCommit(false);
            
            String checkReservationSql = "SELECT reservation_time, status FROM reservations WHERE slot_number=? AND status='active'";
            PreparedStatement psRes = conn.prepareStatement(checkReservationSql);
            psRes.setInt(1, slot);
            ResultSet rsRes = psRes.executeQuery();
            if(rsRes.next()){
                Timestamp resTime = rsRes.getTimestamp("reservation_time");
                Timestamp now = new Timestamp(System.currentTimeMillis());
                if(resTime.after(now)){
                    // Slot is reserved in the future
                    System.out.println("Cannot park: slot reserved for " + resTime);
                    return false;
                }
            }

            String updateSlot = "UPDATE parking_slots SET status='occupied', plate_number=?, contact=?, time_in=NOW() WHERE slot_number=?";

            PreparedStatement ps1 = conn.prepareStatement(updateSlot);
            ps1.setString(1, plate);
            ps1.setString(2, contact);
            ps1.setInt(3, slot);

            ps1.executeUpdate();
            
            String completeReservation = """
                UPDATE reservations 
                SET status = 'completed' 
                WHERE plate_number = ? 
                AND status = 'active'
            """;
            PreparedStatement psComplete = conn.prepareStatement(completeReservation);
            psComplete.setString(1, plate);
            psComplete.executeUpdate();

            String getType = "SELECT type FROM parking_slots WHERE slot_number=?";
            PreparedStatement ps2 = conn.prepareStatement(getType);
            ps2.setInt(1, slot);

            ResultSet rs = ps2.executeQuery();
            
            String type = getSlotType(slot);
            String vehicle = getSlotVehicle(slot);

            if(rs.next()){
                type = rs.getString("type");
            }

            String insertLog = """
                INSERT INTO parking_logs 
                (slot_number, plate_number, vehicle, type, action_type, staff_username, action_time) 
                VALUES (?, ?, ?, ?, 'time_in', ?, NOW())
            """;
            PreparedStatement ps3 = conn.prepareStatement(insertLog);
            ps3.setInt(1, slot);
            ps3.setString(2, plate);
            ps3.setString(3, vehicle);
            ps3.setString(4, type);
            ps3.setString(5, model.SessionManager.getUsername() != null 
                ? model.SessionManager.getUsername() : "system");
            ps3.executeUpdate();

            conn.commit();

            success = true;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return success;
    }
    
    public void autoReleaseSlot(int slot, String plate) {
        String sql = """
            UPDATE parking_slots 
            SET status='available', plate_number=NULL, contact=NULL, time_in=NULL 
            WHERE slot_number=?
        """;
        try (Connection conn = DBConnection.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, slot);
            ps.executeUpdate();

            // Log as 'auto_released' instead of 'time_out'
            logAction(slot, plate, getSlotVehicle(slot), getSlotType(slot), "auto_released");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public ResultSet getReservations() {
        try {
            Connection conn = DBConnection.connect();
            String sql = """
                SELECT id, slot_number, plate_number, contact, 
                       reservation_time, status
                FROM reservations 
                WHERE status IN ('active', 'pending')
                ORDER BY reservation_time ASC
            """;
            PreparedStatement ps = conn.prepareStatement(sql);
            return ps.executeQuery();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    
    public String capitalize(String str) {
        if (str == null || str.length() == 0) {
            return str;
        }
        return str.substring(0, 1).toUpperCase() + str.substring(1).toLowerCase();
    }
    
    public boolean isVehicleAllowedInSlot(int slotNo, String vehicleType) {
        try {
            Connection conn = DBConnection.connect();
            String sql = "SELECT vehicle FROM parking_slots WHERE slot_number = ?";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, slotNo);

            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                String slotType = rs.getString("vehicle").toLowerCase();
                return slotType.equals(vehicleType);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    public void reserveSlot(int slotNo, String plateNo, String contactNo, String vehicleType, Timestamp reservationTime) throws SQLException {
        try (Connection conn = DBConnection.connect()) {

            // 1️⃣ Check if slot exists
            String checkSlotSql = "SELECT slot_number FROM parking_slots WHERE slot_number=?";
            try (PreparedStatement psCheck = conn.prepareStatement(checkSlotSql)) {
                psCheck.setInt(1, slotNo);
                try (ResultSet rs = psCheck.executeQuery()) {
                    if (!rs.next()) {
                        throw new SQLException("Slot number does not exist.");
                    }
                }
            }

            String conflictSql = """
                SELECT COUNT(*) AS count
                FROM reservations
                WHERE slot_number = ?
                  AND reservation_time = ?
                  AND status IN ('pending', 'confirmed')
            """;
            try (PreparedStatement psConflict = conn.prepareStatement(conflictSql)) {
                psConflict.setInt(1, slotNo);
                psConflict.setTimestamp(2, reservationTime);
                try (ResultSet rs = psConflict.executeQuery()) {
                    if (rs.next() && rs.getInt("count") > 0) {
                        throw new SQLException("Slot is already reserved at that time.");
                    }
                }
            }
            

            String insertReservationSql = """
                INSERT INTO reservations (slot_number, plate_number, vehicle, contact, reservation_time, status)
                VALUES (?, ?, ?, ?, ?, 'pending')
            """;
            try (PreparedStatement psInsert = conn.prepareStatement(insertReservationSql)) {
                psInsert.setInt(1, slotNo);
                psInsert.setString(2, plateNo);
                psInsert.setString(3, vehicleType);
                psInsert.setString(4, contactNo);
                psInsert.setTimestamp(5, reservationTime);
                psInsert.executeUpdate();
            }
            
            long diffMillis = reservationTime.getTime() - System.currentTimeMillis();
            if (diffMillis <= 3 * 60 * 60 * 1000) { // less than 3 hours from now
                String updateSlot = "UPDATE parking_slots SET status='reserved', plate_number=?, contact=? WHERE slot_number=?";
                try (PreparedStatement psUpdate = conn.prepareStatement(updateSlot)) {
                    psUpdate.setString(1, plateNo);
                    psUpdate.setString(2, contactNo);
                    psUpdate.setInt(3, slotNo);
                    psUpdate.executeUpdate();
                }
                String updateReservation = "UPDATE reservations SET status='active' WHERE slot_number=? AND reservation_time=?";
                try (PreparedStatement psRes = conn.prepareStatement(updateReservation)) {
                    psRes.setInt(1, slotNo);
                    psRes.setTimestamp(2, reservationTime);
                    psRes.executeUpdate();
                }
            }
        }
    }

    public String getSlotStatus(int slot){
        String sql = "SELECT status FROM parking_slots WHERE slot_number=?";

        try (Connection conn = DBConnection.connect();
            PreparedStatement ps = conn.prepareStatement(sql)) {

           ps.setInt(1, slot);
           try (ResultSet rs = ps.executeQuery()) {
               if (rs.next()) {
                   return rs.getString("status");
               }
           }

       } catch (SQLException e) {
           e.printStackTrace();
       }
       return "available";
    }
    
    public String getSlotType(int slot){
        String sql = "SELECT type FROM parking_slots WHERE slot_number=?";

        try(Connection conn = DBConnection.connect();
            PreparedStatement ps = conn.prepareStatement(sql)){

            ps.setInt(1, slot);

            ResultSet rs = ps.executeQuery();

            if(rs.next()){
                return rs.getString("type");
            }

        }catch(Exception e){
            e.printStackTrace();
        }

        return "car";
    }
    
    public String getSlotVehicle(int slotNo) throws SQLException {
    String sql = "SELECT vehicle FROM parking_slots WHERE slot_number = ?";
    try (Connection conn = DBConnection.connect();
         PreparedStatement ps = conn.prepareStatement(sql)) {
        ps.setInt(1, slotNo);
        try (ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getString("vehicle"); // fetch vehicle type
            } else {
                throw new SQLException("Slot number does not exist.");
            }
        }
    }
}
    
    public ResultSet getRecords(){
        String sql = """
            SELECT slot_number, plate_number, vehicle, type, action_type, action_time
            FROM parking_logs
            WHERE action_time >= NOW() - INTERVAL 30 DAY
            ORDER BY action_time DESC
        """;

        try{
            Connection conn = DBConnection.connect();
            PreparedStatement ps = conn.prepareStatement(sql);
            return ps.executeQuery();
        }catch(Exception e){
            e.printStackTrace();
        }

        return null;
    }
    
    public boolean isPlateParked(String plate){
        String sql = "SELECT COUNT(*) FROM parking_slots WHERE plate_number=? AND status='occupied'";
        try (Connection conn = DBConnection.connect();
            PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setString(1, plate);
            ResultSet rs = ps.executeQuery();
            if(rs.next()){
                return rs.getInt(1) > 0;
            }
        }catch(Exception e){
            e.printStackTrace();
        }
        return false;
    }
    
    public int getBestAvailableSlot(String vehicleType){
        if(vehicleType.equals("pwd")){
            String sql = "SELECT slot_number FROM parking_slots WHERE (vehicle=? OR vehicle=?) AND status='available' ORDER BY slot_number ASC LIMIT 1";
            try (Connection conn = DBConnection.connect();
                PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, vehicleType);
                ps.setString(2, "car");
                
                ResultSet rs = ps.executeQuery();
                if(rs.next()){
                    return rs.getInt("slot_number");
                }
            }catch(Exception e){
                e.printStackTrace();
            }
        }else{
            String sql = "SELECT slot_number FROM parking_slots WHERE vehicle=? AND status='available' ORDER BY slot_number ASC LIMIT 1";
            try (Connection conn = DBConnection.connect();
                PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, vehicleType);
                ResultSet rs = ps.executeQuery();
                if(rs.next()){
                    return rs.getInt("slot_number");
                }
            }catch(Exception e){
                e.printStackTrace();
            }
        }
        return -1;
    }
    
    public void assignSlot(int slot, String plate, String contact) {
        String sql = """
            UPDATE parking_slots 
            SET plate_number=?, contact=?, status='occupied', time_in=NOW() 
            WHERE slot_number=?
        """;
        try (Connection conn = DBConnection.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, plate);
            ps.setString(2, contact);
            ps.setInt(3, slot);
            ps.executeUpdate();

            // Fix: pass vehicle and type correctly
            String vehicle = getSlotVehicle(slot);
            String type = getSlotType(slot);
            logAction(slot, plate, vehicle, type, "time_in");

            // Mark reservation completed if exists
            String completeReservation = """
                UPDATE reservations 
                SET status = 'completed' 
                WHERE plate_number = ? 
                AND status = 'active'
            """;
            Connection conn2 = DBConnection.connect();
            PreparedStatement psComplete = conn2.prepareStatement(completeReservation);
            psComplete.setString(1, plate);
            psComplete.executeUpdate();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public int getReservedSlot(String plate){
        String sql = "SELECT slot_number FROM parking_slots WHERE plate_number=? AND status='reserved'";
        try (Connection conn = DBConnection.connect();
            PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, plate);
            ResultSet rs = ps.executeQuery();
            if(rs.next()){
                return rs.getInt("slot_number");
            }
        }catch(Exception e){
            e.printStackTrace();
        }
        return -1;
    }

    public int getParkedSlot(String plate){
        String sql = "SELECT slot_number FROM parking_slots WHERE plate_number=? AND status='occupied'";
        try (Connection conn = DBConnection.connect();
            PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setString(1, plate);
            ResultSet rs = ps.executeQuery();
            if(rs.next()){
                return rs.getInt("slot_number");
            }
        }catch(Exception e){
            e.printStackTrace();
        }
        return -1;
    }
    
    public boolean isPlateReserved(String plateNumber) {
        String sql = "SELECT COUNT(*) AS count FROM parking_slots WHERE plate_number = ? AND status = 'reserved'";
        try (Connection conn = DBConnection.connect();
            PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setString(1, plateNumber);

            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt("count") > 0;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public double calculateFee(String plate){
        String sql = "SELECT time_in FROM parking_slots WHERE plate_number=?";
        try (Connection conn = DBConnection.connect();
            PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, plate);
            ResultSet rs = ps.executeQuery();
            if(rs.next()){
                Timestamp timeIn = rs.getTimestamp("time_in");
                LocalDateTime in = timeIn.toLocalDateTime();
                LocalDateTime now = LocalDateTime.now();
                long minutes = Duration.between(in, now).toMinutes();

                double ratePerHour = 20.0;
                double cost = (minutes / 60.0) * ratePerHour;
                return Math.round(cost * 100.0) / 100.0;
            }
        }catch(Exception e){
            e.printStackTrace();
        }
        return 0.0;
    }
    
    public ParkingSession getParkingSession(String plate){
        
        ParkingSession session = new ParkingSession();

        String sql = "SELECT time_in FROM parking_slots WHERE plate_number=?";

        try(Connection conn = DBConnection.connect();
            PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, plate);
            ResultSet rs = ps.executeQuery();

            if(rs.next()){
                Timestamp timeInStamp = rs.getTimestamp("time_in");

                if(timeInStamp == null){
                    // Handle missing time_in
                    System.out.println("No 'time_in' recorded for plate: " + plate);
                    return null; // or return an empty session with cost = 0
                }

                LocalDateTime timeIn = timeInStamp.toLocalDateTime();
                LocalDateTime timeOut = LocalDateTime.now();

                long minutes = Duration.between(timeIn, timeOut).toMinutes();
                double ratePerHour = 20.0;
                double cost = (minutes / 60.0) * ratePerHour;

                session.cost = Math.round(cost * 100.0) / 100.0;

                DateTimeFormatter timeFormat = DateTimeFormatter.ofPattern("HH:mm:ss");
                DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd");

                session.timeIn = timeIn.format(timeFormat);
                session.dateIn = timeIn.format(dateFormat);
                session.timeOut = timeOut.format(timeFormat);
                session.dateOut = timeOut.format(dateFormat);
            } else {
                System.out.println("No parking session found for plate: " + plate);
                return null;
            }

        } catch(Exception e){
            e.printStackTrace();
            return null;
        }

        return session;
    }

    public void releaseSlot(int slot, String plate){
        String sql = "UPDATE parking_slots SET status='available', plate_number=NULL, contact=NULL, time_in=NULL WHERE slot_number=?";
        try (Connection conn = DBConnection.connect();
            PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, slot);
            ps.executeUpdate();

            String vehicle = getSlotVehicle(slot); // or "car" as default
            logAction(slot, plate, vehicle, getSlotType(slot), "time_out");
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    private void logAction(int slot, String plate, String vehicle, 
                        String type, String actionType) {
        String sql = """
            INSERT INTO parking_logs
            (slot_number, plate_number, vehicle, type, action_type, staff_username, action_time) 
            VALUES(?,?,?,?,?,?,NOW())
        """;
        try (Connection conn = DBConnection.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, slot);
            ps.setString(2, plate);
            ps.setString(3, vehicle);
            ps.setString(4, type);
            ps.setString(5, actionType);
            // Fix: save logged-in staff username
            ps.setString(6, model.SessionManager.getUsername() != null 
                ? model.SessionManager.getUsername() : "system");
            ps.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public void activateUpcomingReservations() {
        SettingsController settings = new SettingsController();
        int gracePeriodMinutes = 30; // default
        
        try {
            gracePeriodMinutes = Integer.parseInt(settings.getGracePeriod());
        } catch (NumberFormatException e) {
            System.out.println("Using default grace period: 30 minutes");
        }
        
        final long GRACE_PERIOD_MILLIS = gracePeriodMinutes * 60 * 1000L;

        String sql = "SELECT id, slot_number, plate_number, contact, reservation_time, status " +
                     "FROM reservations WHERE status='pending'";

        try (Connection conn = DBConnection.connect();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            Timestamp now = new Timestamp(System.currentTimeMillis());

            while (rs.next()) {
                int reservationId = rs.getInt("id");
                int slotNumber = rs.getInt("slot_number");
                String plate = rs.getString("plate_number");
                String contact = rs.getString("contact");
                Timestamp reservationTime = rs.getTimestamp("reservation_time");
                String status = rs.getString("status");

                long diffMillis = reservationTime.getTime() - now.getTime();

                if ("pending".equalsIgnoreCase(status)) {
                    // Reservation is within next 3 hours and not yet expired (CONFIGURE)
                    if (diffMillis <= 3 * 60 * 60 * 1000 && diffMillis > -GRACE_PERIOD_MILLIS) {
                        // Check slot status
                        String checkSlotSql = "SELECT status FROM parking_slots WHERE slot_number=?";
                        try (PreparedStatement psCheck = conn.prepareStatement(checkSlotSql)) {
                            psCheck.setInt(1, slotNumber);
                            try (ResultSet rsSlot = psCheck.executeQuery()) {
                                if (rsSlot.next()) {
                                    String slotStatus = rsSlot.getString("status");

                                    if (!"available".equalsIgnoreCase(slotStatus)) {
                                        // Slot occupied: mark reservation as missed
                                        String updateReservation = "UPDATE reservations SET status='missed' WHERE id=?";
                                        try (PreparedStatement psRes = conn.prepareStatement(updateReservation)) {
                                            psRes.setInt(1, reservationId);
                                            psRes.executeUpdate();
                                        }
                                    } else {
                                        // Slot available: reserve it and mark reservation active
                                        String updateSlot = "UPDATE parking_slots SET status='reserved', plate_number=?, contact=? WHERE slot_number=?";
                                        try (PreparedStatement psUpdate = conn.prepareStatement(updateSlot)) {
                                            psUpdate.setString(1, plate);
                                            psUpdate.setString(2, contact);
                                            psUpdate.setInt(3, slotNumber);
                                            psUpdate.executeUpdate();
                                        }

                                        String updateReservation = "UPDATE reservations SET status='active' WHERE id=?";
                                        try (PreparedStatement psRes = conn.prepareStatement(updateReservation)) {
                                            psRes.setInt(1, reservationId);
                                            psRes.executeUpdate();
                                        }
                                    }
                                }
                            }
                        }
                    } else if (diffMillis < -GRACE_PERIOD_MILLIS) {
                        String updateReservation = "UPDATE reservations SET status='missed' WHERE id=?";
                        try (PreparedStatement psRes = conn.prepareStatement(updateReservation)) {
                            psRes.setInt(1, reservationId);
                            psRes.executeUpdate();
                        }
                        String updateSlot = "UPDATE parking_slots SET status='available', plate_number=NULL, contact=NULL, time_in=NULL WHERE slot_number=?";
                        try (PreparedStatement psUpdate = conn.prepareStatement(updateSlot)) {
                            psUpdate.setInt(1, slotNumber);
                            psUpdate.executeUpdate();
                        }
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public ResultSet getRecentLogs(){
        String sql = """
            SELECT slot_number, plate_number, action_type, action_time
            FROM parking_logs
            ORDER BY action_time DESC
            LIMIT 5
        """;

        try{
            Connection conn = DBConnection.connect();
            PreparedStatement ps = conn.prepareStatement(sql);
            return ps.executeQuery();
        }catch(Exception e){
            e.printStackTrace();
        }

        return null;
    }
    
    public TicketInfo getTicketInfo(int slotNumber) {
        String sql = "SELECT slot_number, vehicle, status, plate_number, contact, time_in FROM parking_slots WHERE slot_number = ?";
        try (Connection conn = DBConnection.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, slotNumber);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    TicketInfo info = new TicketInfo();
                    info.slotNo = rs.getInt("slot_number");
                    info.vehicleType = rs.getString("vehicle");
                    info.plate = rs.getString("plate_number");
                    info.contact = rs.getString("contact");
                    info.timeIn = rs.getTimestamp("time_in");
                    return info;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    
    public String getPlateNo(int slotNo){
        String sql = "SELECT plate_number FROM parking_slots WHERE slot_number=?";
        try (Connection conn = DBConnection.connect();
            PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setInt(1, slotNo);
            ResultSet rs = ps.executeQuery();
            if(rs.next()){
                return rs.getString("plate_number");
            }
            return "";
        }catch(Exception e){
            e.printStackTrace();
        }
        
         return "";
    }
    
    public int getReservationCountForPlate(String plate) {
        String sql = "SELECT COUNT(*) FROM reservations WHERE plate_number = ?";
        try (Connection conn = DBConnection.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, plate);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }
    
    public ResultSet getExpiredReservations() {
        String sql = """
            SELECT r.id, r.slot_number, r.plate_number, r.contact, r.reservation_time
            FROM reservations r
            JOIN settings s ON s.setting_key = 'grace_period'
            WHERE r.status = 'active'
            AND NOW() > DATE_ADD(r.reservation_time, INTERVAL CAST(s.setting_value AS UNSIGNED) MINUTE)
        """;
        try {
            Connection conn = DBConnection.connect();
            PreparedStatement ps = conn.prepareStatement(sql);
            return ps.executeQuery();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
    
    public boolean hasActiveReservation(String plateNo) throws SQLException {
        String sql = """
            SELECT COUNT(*) FROM reservations 
            WHERE plate_number = ? 
            AND status = 'active'
            AND reservation_time >= NOW() - INTERVAL (
                SELECT CAST(setting_value AS UNSIGNED) 
                FROM settings 
                WHERE setting_key = 'grace_period'
            ) MINUTE
        """;
        try (Connection conn = DBConnection.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, plateNo);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1) > 0;
            }
        }
        return false;
    }

    public boolean isVehicleParked(String plateNo) throws SQLException {
        String sql = "SELECT COUNT(*) FROM parking_slots WHERE plate_number = ? AND status = 'occupied'";
        try (Connection conn = DBConnection.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, plateNo);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }
        return false;
    }
    
    // Add to SlotController.java

    public String getActiveReservationDetails(String plateNo) throws SQLException {
        String sql = """
            SELECT slot_number, reservation_time FROM reservations
            WHERE plate_number = ? AND status = 'active'
            AND reservation_time >= NOW() - INTERVAL (
                SELECT CAST(setting_value AS UNSIGNED) 
                FROM settings 
                WHERE setting_key = 'grace_period'
            ) MINUTE
            ORDER BY reservation_time ASC LIMIT 1
        """;
        try (Connection conn = DBConnection.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, plateNo);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return "Reserved at Slot " + rs.getInt("slot_number") +
                           " for " + rs.getTimestamp("reservation_time");
                }
            }
        }
        return "";
    }

    public String getParkedVehicleDetails(String plateNo) throws SQLException {
        String sql = "SELECT slot_number, time_in FROM parking_slots WHERE plate_number = ? AND status = 'occupied'";
        try (Connection conn = DBConnection.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, plateNo);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return "Parked at Slot " + rs.getInt("slot_number") +
                           " since " + rs.getTimestamp("time_in");
                }
            }
        }
        return "";
    }

    public boolean isSlotAvailableAtTime(int slotNo, Timestamp reservationTime) throws SQLException {
        Connection conn = DBConnection.connect();
        String query = "SELECT COUNT(*) FROM reservations WHERE slot_number = ? AND status = 'reserved' " +
                       "AND ABS(TIMESTAMPDIFF(MINUTE, reservation_time, ?)) < 60"; // 1-hour buffer
        try (PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, slotNo);
            pstmt.setTimestamp(2, reservationTime);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) == 0;
                }
            }
        }
        return true;
    }

    public String getConflictingReservationDetails(int slotNo, Timestamp reservationTime) throws SQLException {
        Connection conn = DBConnection.connect();
        String query = "SELECT plate_number, reservation_time FROM reservations WHERE slot_number = ? AND status = 'reserved' " +
                       "AND ABS(TIMESTAMPDIFF(MINUTE, reservation_time, ?)) < 60 LIMIT 1";
        try (PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, slotNo);
            pstmt.setTimestamp(2, reservationTime);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return "Conflicts with reservation for " + rs.getString("plate_number") + 
                           " at " + rs.getTimestamp("reservation_time");
                }
            }
        }
        return "";
    }

    public boolean hasGracePeriodConflict(int slotNo, Timestamp reservationTime, int gracePeriod) throws SQLException {
        Connection conn = DBConnection.connect();
        String query = "SELECT COUNT(*) FROM reservations WHERE slot_number = ? AND status = 'reserved' " +
                       "AND ABS(TIMESTAMPDIFF(MINUTE, reservation_time, ?)) < ?";
        try (PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, slotNo);
            pstmt.setTimestamp(2, reservationTime);
            pstmt.setInt(3, gracePeriod);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }
        return false;
    }

    public int getActiveReservationCount(String plateNo) throws SQLException {
        String sql = """
            SELECT COUNT(*) FROM reservations
            WHERE plate_number = ? AND status = 'active'
            AND reservation_time >= NOW() - INTERVAL (
                SELECT CAST(setting_value AS UNSIGNED) 
                FROM settings 
                WHERE setting_key = 'grace_period'
            ) MINUTE
        """;
        try (Connection conn = DBConnection.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, plateNo);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        }
        return 0;
    }
    
    public void markReservationMissed(int reservationId) {
        String sql = "UPDATE reservations SET status='missed' WHERE id=?";
        try (Connection conn = DBConnection.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, reservationId);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    public boolean saveTransaction(int slotNumber, String plateNumber,
                                    Timestamp timeIn, Timestamp timeOut,
                                    long durationMinutes, double fee,
                                    double amountReceived, double changeAmount) {
        String sql = """
            INSERT INTO transactions 
            (slot_number, plate_number, time_in, time_out, duration_minutes, fee, amount_received, change_amount)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?)
        """;
        try (Connection conn = DBConnection.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, slotNumber);
            ps.setString(2, plateNumber);
            ps.setTimestamp(3, timeIn);
            ps.setTimestamp(4, timeOut);
            ps.setLong(5, durationMinutes);
            ps.setDouble(6, fee);
            ps.setDouble(7, amountReceived);
            ps.setDouble(8, changeAmount);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    public boolean slotExists(int slotNo) {
        String sql = "SELECT COUNT(*) FROM parking_slots WHERE slot_number = ?";
        try (Connection conn = DBConnection.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, slotNo);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1) > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
    
    public String getContactFromReservation(String plate) {
        String sql = """
            SELECT contact FROM reservations 
            WHERE plate_number = ? 
            AND status = 'active' 
            ORDER BY reservation_time ASC 
            LIMIT 1
        """;
        try (Connection conn = DBConnection.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, plate);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getString("contact");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "";
    }
    
    public boolean cancelReservation(int reservationId, int slotNo, String plate) {
        String sql = "UPDATE reservations SET status = 'cancelled' WHERE id = ? AND status IN ('active', 'pending')";
        try (Connection conn = DBConnection.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, reservationId);
            int updated = ps.executeUpdate();

            if (updated > 0) {
                // Release slot if it was active
                String slotStatus = getSlotStatus(slotNo);
                if ("reserved".equalsIgnoreCase(slotStatus)) {
                    String releaseSql = """
                        UPDATE parking_slots 
                        SET status = 'available', plate_number = NULL, 
                        contact = NULL, time_in = NULL 
                        WHERE slot_number = ?
                    """;
                    try (PreparedStatement psRelease = conn.prepareStatement(releaseSql)) {
                        psRelease.setInt(1, slotNo);
                        psRelease.executeUpdate();
                    }
                }

                // Log the cancellation
                logAction(slotNo, plate, getSlotVehicle(slotNo), 
                         getSlotType(slotNo), "cancelled");
                return true;
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean isCancellable(int reservationId) {
        String sql = """
            SELECT r.reservation_time, r.status,
                   s.setting_value AS cancellation_window
            FROM reservations r
            JOIN settings s ON s.setting_key = 'cancellation_window'
            WHERE r.id = ?
        """;
        try (Connection conn = DBConnection.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, reservationId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String status = rs.getString("status");

                    // Only active or pending can be cancelled
                    if (!status.equals("active") && !status.equals("pending")) {
                        return false;
                    }

                    Timestamp resTime = rs.getTimestamp("reservation_time");
                    int windowMinutes = Integer.parseInt(
                        rs.getString("cancellation_window"));

                    long diffMillis = resTime.getTime() - System.currentTimeMillis();
                    long diffMinutes = diffMillis / (1000 * 60);

                    return diffMinutes > windowMinutes;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
    
    public ResultSet getOverstayingSlots() {
        String sql = """
            SELECT ps.slot_number, ps.plate_number, ps.vehicle, 
                   ps.time_in, ps.contact,
                   TIMESTAMPDIFF(MINUTE, ps.time_in, NOW()) AS minutes_parked,
                   CAST(s.setting_value AS UNSIGNED) AS max_stay_minutes
            FROM parking_slots ps
            JOIN settings s ON s.setting_key = 'max_stay'
            WHERE ps.status = 'occupied'
            AND ps.time_in IS NOT NULL
            AND TIMESTAMPDIFF(MINUTE, ps.time_in, NOW()) > 
                (CAST(s.setting_value AS UNSIGNED) * 60)
        """;
        try {
            Connection conn = DBConnection.connect();
            PreparedStatement ps = conn.prepareStatement(sql);
            return ps.executeQuery();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean isSlotOverstaying(int slotNo) {
        String sql = """
            SELECT TIMESTAMPDIFF(MINUTE, ps.time_in, NOW()) AS minutes_parked,
                   CAST(s.setting_value AS UNSIGNED) * 60 AS max_stay_minutes
            FROM parking_slots ps
            JOIN settings s ON s.setting_key = 'max_stay'
            WHERE ps.slot_number = ?
            AND ps.status = 'occupied'
            AND ps.time_in IS NOT NULL
        """;
        try (Connection conn = DBConnection.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, slotNo);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    long minutesParked = rs.getLong("minutes_parked");
                    long maxStayMinutes = rs.getLong("max_stay_minutes");
                    return minutesParked > maxStayMinutes;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
    
}