package view;

import controller.SettingsController;
import controller.SlotController;
import controller.UserController;
import javax.swing.*;
import java.awt.*;
import javax.swing.table.DefaultTableModel;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import model.ParkingSession;
import view.Dialogue.Dialogue_Confirmation;
import view.Dialogue.Dialogue_Parking;
import view.Dialogue.Dialogue_Payment;
import view.Dialogue.Dialogue_SystemMessage;
import view.Dialogue.Dialogue_VehicleType;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import model.DBConnection;
import model.SessionManager;
import model.TicketInfo;
import view.Dialogue.Dialogue_Ticket;

public class HomeForm extends javax.swing.JFrame {
    private RecordsPanel recordsPanel;
    private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(HomeForm.class.getName());
    private SlotController controller = new SlotController();
    private int hoveredRow = -1;
    CardLayout cardLayout;
    JPanel contentPanel;
    DefaultTableModel model;
    JTable table;
    
    public HomeForm() {
        String fullName = SessionManager.getFullName();
        String role = SessionManager.getRole();
        
        initComponents();
        
        if (fullName != null) {
            lblCurrentUser.setText(fullName + " | " + role);
        } else {
            lblCurrentUser.setText("");
        }
        
        this.setResizable(false);
        this.setLocationRelativeTo(null);

        cardLayout = new CardLayout();
        contentPanel = new JPanel(cardLayout);

        JPanel homePanel = jPanel1;
        recordsPanel = new RecordsPanel(this);
        JPanel reservationPanel = new ReservationPanel(this);

        contentPanel.add(homePanel, "home");
        contentPanel.add(recordsPanel, "records");
        contentPanel.add(reservationPanel, "reservation");

        setLayout(new BorderLayout());
        add(jPanel2, BorderLayout.WEST);
        add(contentPanel, BorderLayout.CENTER);

        initSlots();
        loadSlotsFromDatabase();
        loadTable();
        updateSlotCounts();
        
        SlotController startupController = new SlotController();
        startupController.activateUpcomingReservations();
        
        startSchedulers();
        repaint();
    }
    
    private void startSchedulers() {
        ScheduledExecutorService reservationScheduler = 
            Executors.newSingleThreadScheduledExecutor();
        reservationScheduler.scheduleAtFixedRate(() -> {
            try {
                SlotController slotController = new SlotController();
                slotController.activateUpcomingReservations();

                javax.swing.SwingUtilities.invokeLater(() -> {
                    loadSlotsFromDatabase();
                    loadTable();
                    updateSlotCounts();
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, 5, 5, TimeUnit.MINUTES);

        Timer autoReleaseTimer = new Timer(60000, e -> {
            SettingsController settingsController = new SettingsController();
            boolean notifyOnMissed = settingsController.isNotifyOnMissedEnabled();

            SlotController slotController = new SlotController();
            try {
                ResultSet expired = slotController.getExpiredReservations();
                if (expired == null) return;

                boolean anyChanged = false;
                StringBuilder missedInfo = new StringBuilder();

                while (expired.next()) {
                    int slotNo = expired.getInt("slot_number");
                    String plate = expired.getString("plate_number");
                    int reservationId = expired.getInt("id");

                    // Always mark missed and release slot
                    slotController.markReservationMissed(reservationId);
                    slotController.autoReleaseSlot(slotNo, plate);

                    System.out.println("Auto-released slot " + slotNo +
                        " for no-show: " + plate);

                    // Collect info for notification
                    if (notifyOnMissed) {
                        missedInfo.append("Slot ").append(slotNo)
                                  .append(" — Plate: ").append(plate)
                                  .append("\n");
                    }

                    anyChanged = true;
                }

                if (anyChanged) {
                    final String missedSummary = missedInfo.toString();
                    javax.swing.SwingUtilities.invokeLater(() -> {
                        loadSlotsFromDatabase();
                        loadTable();
                        updateSlotCounts();

                        // Show notification if enabled
                        if (notifyOnMissed && !missedSummary.isEmpty()) {
                            Dialogue_SystemMessage.showMessage(
                                HomeForm.this,
                                "Reservations missed and slots released:\n" +
                                missedSummary
                            );
                        }
                    });
                }

            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        });
        autoReleaseTimer.start();
        
        new Timer(15 * 60 * 1000, e -> {
            SlotController slotController = new SlotController();
            try {
                ResultSet overstaying = slotController.getOverstayingSlots();
                if (overstaying == null) return;

                StringBuilder overstayMsg = new StringBuilder();
                int count = 0;

                while (overstaying.next()) {
                    int slotNo = overstaying.getInt("slot_number");
                    String plate = overstaying.getString("plate_number");
                    long minutesParked = overstaying.getLong("minutes_parked");
                    long maxStay = overstaying.getLong("max_stay_minutes");
                    long overstayMins = minutesParked - maxStay;
                    long hours = overstayMins / 60;
                    long mins = overstayMins % 60;

                    overstayMsg.append(String.format(
                        "Slot %d — %s (overstay: %dh %dm)\n",
                        slotNo, plate, hours, mins
                    ));
                    count++;
                }

                if (count > 0) {
                    // Fix: copy count to a final variable for use in lambda
                    final int overstayCount = count;
                    final String msg = overstayMsg.toString();

                    javax.swing.SwingUtilities.invokeLater(() -> {
                        Dialogue_SystemMessage.showMessage(
                            HomeForm.this,
                            "⚠ OVERSTAY ALERT\n\n" +
                            overstayCount + " vehicle(s) have exceeded maximum stay:\n\n" +
                            msg
                        );
                        loadSlotsFromDatabase();
                    });
                }

            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }).start();
    }
    
    public void updateSlotCounts() {
        SlotController slotController = new SlotController();
        try {
            ResultSet rs = slotController.getSlots();
            int total = 0, occupied = 0, available = 0;
            while (rs.next()) {
                total++;
                String status = rs.getString("status");
                if (status.equals("occupied")) occupied++;
                else if (status.equals("available")) available++;
            }
            txtTotalStots.setText(String.valueOf(total));
            txtOccupiedSlots.setText(String.valueOf(occupied));
            txtAvailableSlots.setText(String.valueOf(available));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public void showPage(String page){
        cardLayout.show(contentPanel, page);

        if(page.equals("records")) {
            recordsPanel.loadRecords();
        }
    }
    
    public void loadTable() {

        model = new DefaultTableModel();
        model.addColumn("Slot");
        model.addColumn("Plate");
        model.addColumn("Action");
        model.addColumn("Time");

        try {
            SlotController slotController = new SlotController();
            ResultSet rs = slotController.getRecentLogs();

            while (rs.next()) {
                model.addRow(new Object[]{
                    rs.getInt("slot_number"),
                    rs.getString("plate_number"),
                    rs.getString("action_type"),
                    rs.getString("action_time")
                });
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        tblActivities.removeAll();

        table = new JTable(model);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        
        table.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
            @Override
            public void mouseMoved(java.awt.event.MouseEvent e) {
                int row = table.rowAtPoint(e.getPoint());

                if (row != hoveredRow) {
                    hoveredRow = row;
                    table.repaint();

                    if (row != -1) {
                        int slotNumber = Integer.parseInt(table.getValueAt(row, 0).toString());
                        highlightSlot(slotNumber);
                        
                        table.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
                    } else {
                        highlightSlot(-1);
                        table.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
                    }
                }
            }
        });
        
        table.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                hoveredRow = -1;
                table.repaint();
                highlightSlot(-1);
                table.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
            }
        });
        
        table.setDefaultRenderer(Object.class, new javax.swing.table.DefaultTableCellRenderer() {

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus,
                                                       int row, int column) {
            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

            if (row == hoveredRow) {
                c.setBackground(new Color(0, 102, 102)); // hover color
                c.setForeground(Color.WHITE);
            } else if (isSelected) {
                c.setBackground(table.getSelectionBackground());
                c.setForeground(table.getSelectionForeground());
            } else {
                c.setBackground(Color.WHITE);
                c.setForeground(Color.BLACK);
            }

            return c;
        }
    });
        
        
        table.getTableHeader().setResizingAllowed(false);
        table.setDefaultEditor(Object.class, null);

        table.getColumnModel().getColumn(0).setPreferredWidth(50);
        table.getColumnModel().getColumn(1).setPreferredWidth(70);
        table.getColumnModel().getColumn(2).setPreferredWidth(76);
        table.getColumnModel().getColumn(3).setPreferredWidth(160);
        
        table.setSelectionBackground(Color.WHITE);
        table.setSelectionForeground(Color.BLACK);

        table.setRowHeight(30);

        table.getTableHeader().setPreferredSize(new Dimension(100, 35));

        table.setFillsViewportHeight(true);

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(null);
        scroll.setPreferredSize(new Dimension(357, 243));

        tblActivities.setLayout(new BorderLayout());
        tblActivities.add(scroll, BorderLayout.CENTER);

        tblActivities.revalidate();
        tblActivities.repaint();

        table.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                int row = table.getSelectedRow();

                if (row != -1) {
                    int slotNumber = Integer.parseInt(table.getValueAt(row, 0).toString());
                }
            }
        });

    }
    
    
    private void highlightSlot(int slotNumber) {

        JLabel[] slots = {
            lblSlot1,lblSlot2,lblSlot3,lblSlot4,lblSlot5,
            lblSlot6,lblSlot7,lblSlot8,lblSlot9,lblSlot10,
            lblSlot11,lblSlot12,lblSlot13,lblSlot14,lblSlot15,
            lblSlot16,lblSlot17,lblSlot18,lblSlot19,lblSlot20,
            lblSlot21,lblSlot22,lblSlot23,lblSlot24,lblSlot25,
            lblSlot26,lblSlot27,lblSlot28,lblSlot29,lblSlot30
        };

        for (JLabel slot : slots) {
            slot.setBorder(null);
        }
        
        if (slotNumber < 1 || slotNumber > slots.length) {
            jLayeredPane2.repaint();
            return;
        }

        JLabel selected = slots[slotNumber - 1];

        jLayeredPane2.moveToFront(selected);

        selected.setBorder(BorderFactory.createLineBorder(Color.YELLOW, 3));

        jLayeredPane2.repaint();
    }
    
    public void reloadReservationsPanel() {
        ReservationPanel reservePanel = new ReservationPanel(this);
        if (reservePanel != null) {
            reservePanel.loadReservations();  // refresh the table
        }
    }
    
    public void highlightSlotFromRecords(int slotNumber) {
        highlightSlot(slotNumber);
    }
    
    private void slotClicked(int slotNumber) throws SQLException {
        SlotController controller = new SlotController();
        String status = controller.getSlotStatus(slotNumber);
        String vehicle = controller.getSlotVehicle(slotNumber);

        if (vehicle.equals("pwd")) {
            vehicle = vehicle.toUpperCase();
        } else {
            vehicle = controller.capitalize(vehicle);
        }

        if (status.equals("available")) {
            Dialogue_Parking parking = new Dialogue_Parking(
                this, true, slotNumber, vehicle);
            parking.setLocationRelativeTo(this);
            parking.setVisible(true);
            loadSlotsFromDatabase();
            loadTable();
            updateSlotCounts();
            repaint();

        } else if (status.equals("unavailable")) {
            Dialogue_SystemMessage.showMessage(this,
                "Slot " + slotNumber + " is currently unavailable.");

        } else if (status.equals("occupied")) {
            String plate = controller.getPlateNo(slotNumber);

            if (controller.isSlotOverstaying(slotNumber)) {
                // Get overstay details
                String overstayInfo = getOverstayInfo(slotNumber, plate);
                Dialogue_SystemMessage.showMessage(this,
                    "⚠ OVERSTAY ALERT\n\n" + overstayInfo);
            } else {
                Dialogue_SystemMessage.showMessage(this,
                    "Slot " + slotNumber + " is occupied by " + plate);
            }

        } else {
            // reserved
            String plate = controller.getPlateNo(slotNumber);
            Dialogue_SystemMessage.showMessage(this,
                "Slot " + slotNumber + " is " + status +
                (plate != null && !plate.isEmpty() ? " by " + plate : ""));
        }
    }
    
    private String getOverstayInfo(int slotNo, String plate) {
        String sql = """
            SELECT ps.time_in,
                   TIMESTAMPDIFF(MINUTE, ps.time_in, NOW()) AS minutes_parked,
                   CAST(s.setting_value AS UNSIGNED) * 60 AS max_stay_minutes,
                   ps.contact
            FROM parking_slots ps
            JOIN settings s ON s.setting_key = 'max_stay'
            WHERE ps.slot_number = ?
        """;
        try (java.sql.Connection conn = DBConnection.connect();
             java.sql.PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, slotNo);
            try (java.sql.ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    long minutesParked = rs.getLong("minutes_parked");
                    long maxStayMinutes = rs.getLong("max_stay_minutes");
                    long overstayMinutes = minutesParked - maxStayMinutes;
                    long overstayHours = overstayMinutes / 60;
                    long overstayMins = overstayMinutes % 60;

                    java.sql.Timestamp timeIn = rs.getTimestamp("time_in");
                    String contact = rs.getString("contact") != null
                        ? rs.getString("contact") : "N/A";

                    java.text.SimpleDateFormat sdf = 
                        new java.text.SimpleDateFormat("yyyy-MM-dd hh:mm a");

                    return String.format(
                    "Plate   : %s\n" +
                    "Overstay: %dh %dm",
                    plate,
                    overstayHours, overstayMins
                );
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "Slot " + slotNo + " is occupied by " + plate;
    }
    
    public void loadSlotsFromDatabase(){

        JLabel[] slots = {
            lblSlot1,lblSlot2,lblSlot3,lblSlot4,lblSlot5,
            lblSlot6,lblSlot7,lblSlot8,lblSlot9,lblSlot10,
            lblSlot11,lblSlot12,lblSlot13,lblSlot14,lblSlot15,
            lblSlot16,lblSlot17,lblSlot18,lblSlot19,lblSlot20,
            lblSlot21,lblSlot22,lblSlot23,lblSlot24,lblSlot25,
            lblSlot26,lblSlot27,lblSlot28,lblSlot29,lblSlot30
        };

        SlotController controller = new SlotController();

        for(int i=0;i<slots.length;i++){

            int slotNumber = i+1;

            String status = controller.getSlotStatus(slotNumber).trim();
            String type = controller.getSlotType(slotNumber).trim();
            
            if(type.equals("car")){
                if(status.equals("available")){
                    slots[i].setIcon(new ImageIcon("src/main/java/assets/available_slot.png"));
                }

                if(status.equals("occupied")){
                    slots[i].setIcon(new ImageIcon("src/main/java/assets/occupied_slot.png"));
                }

                if(status.equals("reserved")){
                    slots[i].setIcon(new ImageIcon("src/main/java/assets/reserved_slot.png"));
                }

                if(status.equals("unavailable")){
                    slots[i].setIcon(new ImageIcon("src/main/java/assets/unavailable_slot.png"));
                }
            }
            
            if(type.equals("side")){
                if(status.equals("available")){
                    slots[i].setIcon(new ImageIcon("src/main/java/assets/available_slot_side.png"));
                }

                if(status.equals("occupied")){
                    slots[i].setIcon(new ImageIcon("src/main/java/assets/occupied_slot_side.png"));
                }

                if(status.equals("reserved")){
                    slots[i].setIcon(new ImageIcon("src/main/java/assets/reserved_slot_side.png"));
                }

                if(status.equals("unavailable")){
                    slots[i].setIcon(new ImageIcon("src/main/java/assets/unavailable_slot_side.png"));
                }
            }
            
            if(type.equals("up")){
                if(status.equals("available")){
                    slots[i].setIcon(new ImageIcon("src/main/java/assets/available_slot_up.png"));
                }

                if(status.equals("occupied")){
                    slots[i].setIcon(new ImageIcon("src/main/java/assets/occupied_slot_up.png"));
                }

                if(status.equals("reserved")){
                    slots[i].setIcon(new ImageIcon("src/main/java/assets/reserved_slot_up.png"));
                }

                if(status.equals("unavailable")){
                    slots[i].setIcon(new ImageIcon("src/main/java/assets/unavailable_slot_up.png"));
                }
            }
            
            if(type.equals("motor")){
                if(status.equals("available")){
                    slots[i].setIcon(new ImageIcon("src/main/java/assets/available_slot_motor.png"));
                }

                if(status.equals("occupied")){
                    slots[i].setIcon(new ImageIcon("src/main/java/assets/occupied_slot_motor.png"));
                }

                if(status.equals("reserved")){
                    slots[i].setIcon(new ImageIcon("src/main/java/assets/reserved_slot_motor.png"));
                }

                if(status.equals("unavailable")){
                    slots[i].setIcon(new ImageIcon("src/main/java/assets/unavailable_slot_motor.png"));
                }
            }
            
            if(type.equals("pwd")){
                if(status.equals("available")){
                    slots[i].setIcon(new ImageIcon("src/main/java/assets/available_slot_pwd.png"));
                }

                if(status.equals("occupied")){
                    slots[i].setIcon(new ImageIcon("src/main/java/assets/occupied_slot_pwd.png"));
                }

                if(status.equals("reserved")){
                    slots[i].setIcon(new ImageIcon("src/main/java/assets/reserved_slot_pwd.png"));
                }

                if(status.equals("unavailable")){
                    slots[i].setIcon(new ImageIcon("src/main/java/assets/unavailable_slot_pwd.png"));
                }
            }
            
            if(type.equals("pwd_up")){
                if(status.equals("available")){
                    slots[i].setIcon(new ImageIcon("src/main/java/assets/available_slot_pwd_up.png"));
                }

                if(status.equals("occupied")){
                    slots[i].setIcon(new ImageIcon("src/main/java/assets/occupied_slot_pwd_up.png"));
                }

                if(status.equals("reserved")){
                    slots[i].setIcon(new ImageIcon("src/main/java/assets/reserved_slot_pwd_up.png"));
                }

                if(status.equals("unavailable")){
                    slots[i].setIcon(new ImageIcon("src/main/java/assets/unavailable_slot_pwd_up.png"));
                }
            }
            
            if (status.equals("occupied") && controller.isSlotOverstaying(slotNumber)) {
                slots[i].setBorder(BorderFactory.createLineBorder(
                    new Color(255, 0, 0), 3));
                slots[i].setToolTipText("OVERSTAY: Slot " + slotNumber);
            } else {
                slots[i].setBorder(null);
                slots[i].setToolTipText(null);
            }

        }
        jLayeredPane2.revalidate();
        jLayeredPane2.repaint();
    }
    
    private void initSlots(){
        JLabel[] slots = {
            lblSlot1,lblSlot2,lblSlot3,lblSlot4,lblSlot5,
            lblSlot6,lblSlot7,lblSlot8,lblSlot9,lblSlot10,
            lblSlot11,lblSlot12,lblSlot13,lblSlot14,lblSlot15,
            lblSlot16,lblSlot17,lblSlot18,lblSlot19,lblSlot20,
            lblSlot21,lblSlot22,lblSlot23,lblSlot24,lblSlot25,
            lblSlot26,lblSlot27,lblSlot28,lblSlot29,lblSlot30
        };

        for(int i=0;i<slots.length;i++){
            int slotNumber = i+1;

            slots[i].setCursor(new Cursor(Cursor.HAND_CURSOR));

            slots[i].addMouseListener(new java.awt.event.MouseAdapter(){

                @Override
                public void mouseClicked(java.awt.event.MouseEvent evt){
                    try {
                        slotClicked(slotNumber);
                    } catch (SQLException ex) {
                        System.getLogger(HomeForm.class.getName()).log(System.Logger.Level.ERROR, (String) null, ex);
                    }
                }

            });
        }
    }
    
    private void handleTimeIn(String plate){
        Dialogue_VehicleType vehicleDialog = new Dialogue_VehicleType(this, true, plate);
        vehicleDialog.setLocationRelativeTo(this);
        vehicleDialog.setVisible(true);
        
        loadSlotsFromDatabase();
        loadTable();
        repaint();
    }
    
    private void handleReservedTimeIn(String plate) {
        SlotController slotController = new SlotController();
        SettingsController settingsController = new SettingsController();

        int reservedSlot = slotController.getReservedSlot(plate);

        if (reservedSlot == -1) {
            Dialogue_SystemMessage.showMessage(this, 
                "No reserved slot found for " + plate);
            return;
        }

        String contact = slotController.getContactFromReservation(plate);
        slotController.assignSlot(reservedSlot, plate, contact);

        // Fix: show ticket after reserved time in
        if (settingsController.isPrintTicketEnabled()) {
            TicketInfo info = slotController.getTicketInfo(reservedSlot);
            if (info != null) {
                String date = "________", time = "________";
                if (info.timeIn != null) {
                    java.text.SimpleDateFormat dateFormat = 
                        new java.text.SimpleDateFormat("yyyy-MM-dd");
                    java.text.SimpleDateFormat timeFormat = 
                        new java.text.SimpleDateFormat("hh:mm a");
                    date = dateFormat.format(info.timeIn);
                    time = timeFormat.format(info.timeIn);
                }

                Dialogue_Ticket ticket = new Dialogue_Ticket(this, true);
                ticket.setTicketInfo(
                    String.valueOf(info.slotNo),
                    date,
                    time,
                    info.plate,
                    info.vehicleType,
                    String.valueOf(info.slotNo),
                    "________",
                    "________"
                );
                ticket.setVisible(true);
            }
        } else {
            Dialogue_SystemMessage.showMessage(this,
                "Your reserved slot " + reservedSlot + " is now occupied.");
        }

        loadSlotsFromDatabase();
        loadTable();
        updateSlotCounts();
        repaint();
    }
    
    private void handleTimeOut(String plate) {
        SlotController slotController = new SlotController();
        SettingsController settingsController = new SettingsController();

        int slotNumber = slotController.getParkedSlot(plate);
        ParkingSession session = slotController.getParkingSession(plate);

        if (session == null) {
            Dialogue_SystemMessage.showMessage(this, "No active parking session found.");
            return;
        }

        double calculatedFee = calculateFee(session, settingsController);

        // Calculate duration in minutes
        try {
            java.text.SimpleDateFormat dtf = new java.text.SimpleDateFormat("yyyy-MM-ddHH:mm:ss");
            java.util.Date dateIn = dtf.parse(session.dateIn + session.timeIn);
            java.util.Date dateOut = dtf.parse(session.dateOut + session.timeOut);
            long durationMinutes = (dateOut.getTime() - dateIn.getTime()) / (1000 * 60);
            Timestamp timeInTimestamp = new Timestamp(dateIn.getTime());

            Dialogue_Payment payment = new Dialogue_Payment(
                this, true,
                session.timeIn, session.dateIn,
                session.timeOut, session.dateOut,
                calculatedFee,
                settingsController,
                slotNumber,           // new
                plate,                // new
                timeInTimestamp,      // new
                durationMinutes       // new
            );

            payment.setVisible(true);

            if (payment.isConfirmed()) {
                slotController.releaseSlot(slotNumber, plate);
                loadSlotsFromDatabase();
                loadTable();
                updateSlotCounts();
                repaint();
            }

        } catch (Exception e) {
            e.printStackTrace();
            Dialogue_SystemMessage.showMessage(this, "Error processing checkout.");
        }
    }
    
    private double calculateFee(ParkingSession session, SettingsController settingsController) {
        try {
            // Parse session times
            java.text.SimpleDateFormat dateTimeFormat = new java.text.SimpleDateFormat("yyyy-MM-ddHH:mm:ss");
            java.util.Date dateIn = dateTimeFormat.parse(session.dateIn + session.timeIn);
            java.util.Date dateOut = dateTimeFormat.parse(session.dateOut + session.timeOut);

            long diffInMillies = dateOut.getTime() - dateIn.getTime();
            long minutes = diffInMillies / (1000 * 60);

            // ===== GRACE PERIOD =====
            double graceMinutes = Double.parseDouble(settingsController.getGracePeriod());
            if (minutes <= graceMinutes) {
                return 0.0;
            }
            double billableMinutes = minutes - graceMinutes;
            double hours = billableMinutes / 60.0;

            // Get rates from settings
            double weekdayRate = Double.parseDouble(settingsController.getWeekdayRate());
            double weekendRate = Double.parseDouble(settingsController.getWeekendRate());
            double penaltyRate = Double.parseDouble(settingsController.getOverstayPenalty());
            double maxStay = Double.parseDouble(settingsController.getMaxStay()); // ADD THIS BACK

            // Check if it's weekend
            java.util.Calendar cal = java.util.Calendar.getInstance();
            cal.setTime(dateIn);
            int dayOfWeek = cal.get(java.util.Calendar.DAY_OF_WEEK);
            boolean isWeekend = (dayOfWeek == java.util.Calendar.SATURDAY || dayOfWeek == java.util.Calendar.SUNDAY);

            double fixedRate = isWeekend ? weekendRate : weekdayRate;

            // Calculate fee based on 3-hour rule
            double fee;
            if (hours <= 3) {
                fee = fixedRate;
            } else {
                double extraHours = hours - 3;
                fee = fixedRate + (extraHours * penaltyRate);
            }

            // Check if exceeded maximum stay
            if (hours > maxStay) {
                // You can add additional penalty here if needed
                System.out.println("Warning: Vehicle exceeded maximum stay of " + maxStay + " hours!");
                // Example: Add 20% extra
                // fee = fee * 1.2;
            }

            return Math.round(fee * 100.0) / 100.0;

        } catch (Exception e) {
            System.out.println("Error calculating fee: " + e.getMessage());
            e.printStackTrace();
            return 0.0;
        }
    }
    
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        jPanel2 = new javax.swing.JPanel();
        lblLines = new javax.swing.JLabel();
        jLayeredPane2 = new javax.swing.JLayeredPane();
        lblSlot22 = new javax.swing.JLabel();
        lblSlot2 = new javax.swing.JLabel();
        lblSlot3 = new javax.swing.JLabel();
        lblSlot4 = new javax.swing.JLabel();
        lblSlot9 = new javax.swing.JLabel();
        lblSlot5 = new javax.swing.JLabel();
        lblSlot6 = new javax.swing.JLabel();
        lblSlot7 = new javax.swing.JLabel();
        lblSlot8 = new javax.swing.JLabel();
        lblSlot18 = new javax.swing.JLabel();
        lblSlot10 = new javax.swing.JLabel();
        lblSlot11 = new javax.swing.JLabel();
        lblSlot12 = new javax.swing.JLabel();
        lblSlot13 = new javax.swing.JLabel();
        lblSlot14 = new javax.swing.JLabel();
        lblSlot15 = new javax.swing.JLabel();
        lblSlot17 = new javax.swing.JLabel();
        lblSlot23 = new javax.swing.JLabel();
        lblSlot29 = new javax.swing.JLabel();
        lblSlot30 = new javax.swing.JLabel();
        lblSlot28 = new javax.swing.JLabel();
        lblSlot27 = new javax.swing.JLabel();
        lblSlot26 = new javax.swing.JLabel();
        lblSlot25 = new javax.swing.JLabel();
        lblSlot24 = new javax.swing.JLabel();
        lblSlot1 = new javax.swing.JLabel();
        lblSlot19 = new javax.swing.JLabel();
        lblSlot20 = new javax.swing.JLabel();
        lblSlot21 = new javax.swing.JLabel();
        lblSlot16 = new javax.swing.JLabel();
        lblBrandName = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        lblTotalSlots = new javax.swing.JLabel();
        lblAvailableSlots = new javax.swing.JLabel();
        lblOccupiedSlots = new javax.swing.JLabel();
        txtTotalStots = new javax.swing.JLabel();
        txtAvailableSlots = new javax.swing.JLabel();
        txtOccupiedSlots = new javax.swing.JLabel();
        tblActivities = new javax.swing.JPanel();
        btnRecords = new javax.swing.JButton();
        btnReservation = new javax.swing.JButton();
        txtTimeInOut = new javax.swing.JTextField();
        lblLogOut = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        btnSubmit = new javax.swing.JButton();
        jLabel1 = new javax.swing.JLabel();
        lblCurrentUser = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jPanel1.setBackground(new java.awt.Color(255, 255, 255));

        jPanel2.setBackground(new java.awt.Color(0, 51, 51));

        lblLines.setIcon(new javax.swing.ImageIcon(System.getProperty("user.dir") + "\\src\\main\\java\\assets\\lines.png"));

        jLayeredPane2.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        lblSlot22.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblSlot22.setIcon(new javax.swing.ImageIcon(System.getProperty("user.dir") + "\\src\\main\\java\\assets\\available_slot.png"));
        jLayeredPane2.add(lblSlot22, new org.netbeans.lib.awtextra.AbsoluteConstraints(518, 200, -1, -1));

        lblSlot2.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblSlot2.setIcon(new javax.swing.ImageIcon(System.getProperty("user.dir") + "\\src\\main\\java\\assets\\available_slot.png"));
        jLayeredPane2.add(lblSlot2, new org.netbeans.lib.awtextra.AbsoluteConstraints(316, 460, -1, -1));

        lblSlot3.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblSlot3.setIcon(new javax.swing.ImageIcon(System.getProperty("user.dir") + "\\src\\main\\java\\assets\\available_slot.png"));
        jLayeredPane2.add(lblSlot3, new org.netbeans.lib.awtextra.AbsoluteConstraints(238, 460, -1, -1));

        lblSlot4.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblSlot4.setIcon(new javax.swing.ImageIcon(System.getProperty("user.dir") + "\\src\\main\\java\\assets\\available_slot.png"));
        jLayeredPane2.add(lblSlot4, new org.netbeans.lib.awtextra.AbsoluteConstraints(162, 460, -1, -1));

        lblSlot9.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblSlot9.setIcon(new javax.swing.ImageIcon(System.getProperty("user.dir") + "\\src\\main\\java\\assets\\available_slot_side.png"));
        jLayeredPane2.add(lblSlot9, new org.netbeans.lib.awtextra.AbsoluteConstraints(60, 100, -1, -1));

        lblSlot5.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblSlot5.setIcon(new javax.swing.ImageIcon(System.getProperty("user.dir") + "\\src\\main\\java\\assets\\available_slot_side.png"));
        jLayeredPane2.add(lblSlot5, new org.netbeans.lib.awtextra.AbsoluteConstraints(60, 370, -1, -1));

        lblSlot6.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblSlot6.setIcon(new javax.swing.ImageIcon(System.getProperty("user.dir") + "\\src\\main\\java\\assets\\available_slot_side.png"));
        jLayeredPane2.add(lblSlot6, new org.netbeans.lib.awtextra.AbsoluteConstraints(60, 305, -1, -1));

        lblSlot7.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblSlot7.setIcon(new javax.swing.ImageIcon(System.getProperty("user.dir") + "\\src\\main\\java\\assets\\available_slot_side.png"));
        jLayeredPane2.add(lblSlot7, new org.netbeans.lib.awtextra.AbsoluteConstraints(60, 237, -1, -1));

        lblSlot8.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblSlot8.setIcon(new javax.swing.ImageIcon(System.getProperty("user.dir") + "\\src\\main\\java\\assets\\available_slot_side.png"));
        jLayeredPane2.add(lblSlot8, new org.netbeans.lib.awtextra.AbsoluteConstraints(60, 170, -1, -1));

        lblSlot18.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblSlot18.setIcon(new javax.swing.ImageIcon(System.getProperty("user.dir") + "\\src\\main\\java\\assets\\available_slot_up.png"));
        jLayeredPane2.add(lblSlot18, new org.netbeans.lib.awtextra.AbsoluteConstraints(279, 316, -1, 100));

        lblSlot10.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblSlot10.setIcon(new javax.swing.ImageIcon(System.getProperty("user.dir") + "\\src\\main\\java\\assets\\available_slot_up.png"));
        jLayeredPane2.add(lblSlot10, new org.netbeans.lib.awtextra.AbsoluteConstraints(260, 53, -1, 100));

        lblSlot11.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblSlot11.setIcon(new javax.swing.ImageIcon(System.getProperty("user.dir") + "\\src\\main\\java\\assets\\available_slot_up.png"));
        jLayeredPane2.add(lblSlot11, new org.netbeans.lib.awtextra.AbsoluteConstraints(338, 53, -1, 100));

        lblSlot12.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblSlot12.setIcon(new javax.swing.ImageIcon(System.getProperty("user.dir") + "\\src\\main\\java\\assets\\available_slot_up.png"));
        jLayeredPane2.add(lblSlot12, new org.netbeans.lib.awtextra.AbsoluteConstraints(418, 53, -1, 100));

        lblSlot13.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblSlot13.setIcon(new javax.swing.ImageIcon(System.getProperty("user.dir") + "\\src\\main\\java\\assets\\available_slot_up.png"));
        jLayeredPane2.add(lblSlot13, new org.netbeans.lib.awtextra.AbsoluteConstraints(495, 53, -1, 100));

        lblSlot14.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblSlot14.setIcon(new javax.swing.ImageIcon(System.getProperty("user.dir") + "\\src\\main\\java\\assets\\available_slot_up.png"));
        jLayeredPane2.add(lblSlot14, new org.netbeans.lib.awtextra.AbsoluteConstraints(572, 53, -1, 100));

        lblSlot15.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblSlot15.setIcon(new javax.swing.ImageIcon(System.getProperty("user.dir") + "\\src\\main\\java\\assets\\pwd_slot_up.png"));
        jLayeredPane2.add(lblSlot15, new org.netbeans.lib.awtextra.AbsoluteConstraints(518, 316, -1, 100));

        lblSlot17.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblSlot17.setIcon(new javax.swing.ImageIcon(System.getProperty("user.dir") + "\\src\\main\\java\\assets\\available_slot_up.png"));
        jLayeredPane2.add(lblSlot17, new org.netbeans.lib.awtextra.AbsoluteConstraints(358, 316, -1, 100));

        lblSlot23.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblSlot23.setIcon(new javax.swing.ImageIcon(System.getProperty("user.dir") + "\\src\\main\\java\\assets\\motor_slot.png"));
        jLayeredPane2.add(lblSlot23, new org.netbeans.lib.awtextra.AbsoluteConstraints(660, 178, -1, -1));

        lblSlot29.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblSlot29.setIcon(new javax.swing.ImageIcon(System.getProperty("user.dir") + "\\src\\main\\java\\assets\\motor_slot.png"));
        jLayeredPane2.add(lblSlot29, new org.netbeans.lib.awtextra.AbsoluteConstraints(660, 450, -1, -1));

        lblSlot30.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblSlot30.setIcon(new javax.swing.ImageIcon(System.getProperty("user.dir") + "\\src\\main\\java\\assets\\motor_slot.png"));
        jLayeredPane2.add(lblSlot30, new org.netbeans.lib.awtextra.AbsoluteConstraints(660, 497, -1, -1));

        lblSlot28.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblSlot28.setIcon(new javax.swing.ImageIcon(System.getProperty("user.dir") + "\\src\\main\\java\\assets\\motor_slot.png"));
        jLayeredPane2.add(lblSlot28, new org.netbeans.lib.awtextra.AbsoluteConstraints(660, 405, -1, -1));

        lblSlot27.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblSlot27.setIcon(new javax.swing.ImageIcon(System.getProperty("user.dir") + "\\src\\main\\java\\assets\\motor_slot.png"));
        jLayeredPane2.add(lblSlot27, new org.netbeans.lib.awtextra.AbsoluteConstraints(660, 360, -1, -1));

        lblSlot26.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblSlot26.setIcon(new javax.swing.ImageIcon(System.getProperty("user.dir") + "\\src\\main\\java\\assets\\motor_slot.png"));
        jLayeredPane2.add(lblSlot26, new org.netbeans.lib.awtextra.AbsoluteConstraints(660, 315, -1, -1));

        lblSlot25.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblSlot25.setIcon(new javax.swing.ImageIcon(System.getProperty("user.dir") + "\\src\\main\\java\\assets\\motor_slot.png"));
        jLayeredPane2.add(lblSlot25, new org.netbeans.lib.awtextra.AbsoluteConstraints(660, 269, -1, -1));

        lblSlot24.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblSlot24.setIcon(new javax.swing.ImageIcon(System.getProperty("user.dir") + "\\src\\main\\java\\assets\\motor_slot.png"));
        jLayeredPane2.add(lblSlot24, new org.netbeans.lib.awtextra.AbsoluteConstraints(660, 223, -1, -1));

        lblSlot1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblSlot1.setIcon(new javax.swing.ImageIcon(System.getProperty("user.dir") + "\\src\\main\\java\\assets\\available_slot_pwd.png"));
        lblSlot1.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                lblSlot1MouseClicked(evt);
            }
        });
        jLayeredPane2.add(lblSlot1, new org.netbeans.lib.awtextra.AbsoluteConstraints(397, 460, -1, -1));

        lblSlot19.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblSlot19.setIcon(new javax.swing.ImageIcon(System.getProperty("user.dir") + "\\src\\main\\java\\assets\\available_slot.png"));
        jLayeredPane2.add(lblSlot19, new org.netbeans.lib.awtextra.AbsoluteConstraints(280, 200, -1, -1));

        lblSlot20.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblSlot20.setIcon(new javax.swing.ImageIcon(System.getProperty("user.dir") + "\\src\\main\\java\\assets\\available_slot.png"));
        jLayeredPane2.add(lblSlot20, new org.netbeans.lib.awtextra.AbsoluteConstraints(359, 200, -1, -1));

        lblSlot21.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblSlot21.setIcon(new javax.swing.ImageIcon(System.getProperty("user.dir") + "\\src\\main\\java\\assets\\available_slot.png"));
        jLayeredPane2.add(lblSlot21, new org.netbeans.lib.awtextra.AbsoluteConstraints(438, 200, -1, -1));

        lblSlot16.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblSlot16.setIcon(new javax.swing.ImageIcon(System.getProperty("user.dir") + "\\src\\main\\java\\assets\\available_slot_up.png"));
        jLayeredPane2.add(lblSlot16, new org.netbeans.lib.awtextra.AbsoluteConstraints(437, 316, -1, 100));

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jLayeredPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 800, Short.MAX_VALUE)
            .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                    .addContainerGap(45, Short.MAX_VALUE)
                    .addComponent(lblLines, javax.swing.GroupLayout.PREFERRED_SIZE, 722, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGap(33, 33, 33)))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addComponent(jLayeredPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 769, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
            .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(jPanel2Layout.createSequentialGroup()
                    .addContainerGap()
                    .addComponent(lblLines, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addContainerGap()))
        );

        lblBrandName.setFont(new java.awt.Font("Century Schoolbook", 0, 48)); // NOI18N
        lblBrandName.setForeground(new java.awt.Color(0, 102, 102));
        lblBrandName.setText("ParkaLot");

        lblTotalSlots.setFont(new java.awt.Font("Century Schoolbook", 0, 22)); // NOI18N
        lblTotalSlots.setText("Total Slots..........................");

        lblAvailableSlots.setFont(new java.awt.Font("Century Schoolbook", 0, 22)); // NOI18N
        lblAvailableSlots.setText("Available Slots...................");

        lblOccupiedSlots.setFont(new java.awt.Font("Century Schoolbook", 0, 22)); // NOI18N
        lblOccupiedSlots.setText("Occupied Slots...................");

        txtTotalStots.setFont(new java.awt.Font("Century Schoolbook", 0, 22)); // NOI18N
        txtTotalStots.setText("15");

        txtAvailableSlots.setFont(new java.awt.Font("Century Schoolbook", 0, 22)); // NOI18N
        txtAvailableSlots.setText("15");

        txtOccupiedSlots.setFont(new java.awt.Font("Century Schoolbook", 0, 22)); // NOI18N
        txtOccupiedSlots.setText("15");

        tblActivities.setBackground(new java.awt.Color(0, 51, 51));
        tblActivities.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(0, 51, 51), 1, true));
        tblActivities.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                tblActivitiesMouseEntered(evt);
            }
        });

        javax.swing.GroupLayout tblActivitiesLayout = new javax.swing.GroupLayout(tblActivities);
        tblActivities.setLayout(tblActivitiesLayout);
        tblActivitiesLayout.setHorizontalGroup(
            tblActivitiesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 357, Short.MAX_VALUE)
        );
        tblActivitiesLayout.setVerticalGroup(
            tblActivitiesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 243, Short.MAX_VALUE)
        );

        btnRecords.setBackground(new java.awt.Color(244, 255, 255));
        btnRecords.setFont(new java.awt.Font("Century Schoolbook", 0, 18)); // NOI18N
        btnRecords.setForeground(new java.awt.Color(0, 51, 51));
        btnRecords.setText("Records");
        btnRecords.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(0, 51, 51), 3, true));
        btnRecords.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnRecords.setFocusPainted(false);
        btnRecords.setOpaque(true);
        btnRecords.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                btnRecordsMouseClicked(evt);
            }
        });
        btnRecords.addActionListener(this::btnRecordsActionPerformed);

        btnReservation.setBackground(new java.awt.Color(244, 255, 255));
        btnReservation.setFont(new java.awt.Font("Century Schoolbook", 0, 18)); // NOI18N
        btnReservation.setForeground(new java.awt.Color(0, 51, 51));
        btnReservation.setText("Reservation");
        btnReservation.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(0, 51, 51), 3, true));
        btnReservation.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnReservation.setFocusPainted(false);
        btnReservation.setOpaque(true);
        btnReservation.addActionListener(this::btnReservationActionPerformed);

        txtTimeInOut.setBorder(javax.swing.BorderFactory.createTitledBorder(new javax.swing.border.LineBorder(new java.awt.Color(0, 51, 51), 3, true), "  Time In/Out  ", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.TOP, new java.awt.Font("Century Schoolbook", 0, 18), new java.awt.Color(0, 51, 51))); // NOI18N
        txtTimeInOut.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                txtTimeInOutFocusLost(evt);
            }
        });
        txtTimeInOut.addActionListener(this::txtTimeInOutActionPerformed);
        txtTimeInOut.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                txtTimeInOutKeyPressed(evt);
            }
        });

        lblLogOut.setFont(new java.awt.Font("Century Schoolbook", 0, 18)); // NOI18N
        lblLogOut.setIcon(new javax.swing.ImageIcon(System.getProperty("user.dir") + "\\src\\main\\java\\assets\\log_out.png"));
        lblLogOut.setText("Log Out");
        lblLogOut.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        lblLogOut.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                lblLogOutMouseClicked(evt);
            }
        });

        jLabel2.setIcon(new javax.swing.ImageIcon(System.getProperty("user.dir") + "\\src\\main\\java\\assets\\logo_big.png"));

        btnSubmit.setBackground(new java.awt.Color(244, 255, 255));
        btnSubmit.setFont(new java.awt.Font("Gill Sans MT Ext Condensed Bold", 1, 18)); // NOI18N
        btnSubmit.setText(">");
        btnSubmit.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(0, 51, 51), 3, true));
        btnSubmit.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnSubmit.addActionListener(this::btnSubmitActionPerformed);

        jLabel1.setFont(new java.awt.Font("Century Schoolbook", 0, 14)); // NOI18N
        jLabel1.setText("User:");

        lblCurrentUser.setFont(new java.awt.Font("Century Schoolbook", 0, 18)); // NOI18N
        lblCurrentUser.setText("...");

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, 0)
                .addComponent(jLabel3)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(59, 59, 59)
                        .addComponent(lblTotalSlots, javax.swing.GroupLayout.PREFERRED_SIZE, 278, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(51, 51, 51)
                        .addComponent(txtTotalStots))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(59, 59, 59)
                        .addComponent(lblAvailableSlots, javax.swing.GroupLayout.PREFERRED_SIZE, 278, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(51, 51, 51)
                        .addComponent(txtAvailableSlots))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(59, 59, 59)
                        .addComponent(lblOccupiedSlots, javax.swing.GroupLayout.PREFERRED_SIZE, 276, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(53, 53, 53)
                        .addComponent(txtOccupiedSlots))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(57, 57, 57)
                        .addComponent(tblActivities, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(58, 58, 58)
                        .addComponent(btnRecords, javax.swing.GroupLayout.PREFERRED_SIZE, 170, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(btnReservation, javax.swing.GroupLayout.PREFERRED_SIZE, 170, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(32, 32, 32)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 173, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(lblBrandName))
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(jPanel1Layout.createSequentialGroup()
                                        .addComponent(jLabel1)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                        .addComponent(lblCurrentUser))
                                    .addGroup(jPanel1Layout.createSequentialGroup()
                                        .addComponent(txtTimeInOut, javax.swing.GroupLayout.PREFERRED_SIZE, 293, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(btnSubmit, javax.swing.GroupLayout.PREFERRED_SIZE, 58, javax.swing.GroupLayout.PREFERRED_SIZE))
                                    .addComponent(lblLogOut))
                                .addGap(6, 6, 6)))))
                .addContainerGap(59, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(338, 338, 338)
                        .addComponent(jLabel3))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addGap(56, 56, 56)
                                .addComponent(lblBrandName)
                                .addGap(34, 34, 34))
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                                .addContainerGap()
                                .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 108, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)))
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(lblTotalSlots)
                            .addComponent(txtTotalStots))
                        .addGap(12, 12, 12)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(lblAvailableSlots)
                            .addComponent(txtAvailableSlots))
                        .addGap(12, 12, 12)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(lblOccupiedSlots)
                            .addComponent(txtOccupiedSlots))
                        .addGap(18, 18, 18)
                        .addComponent(tblActivities, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(btnRecords, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(btnReservation, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(12, 12, 12)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(txtTimeInOut, javax.swing.GroupLayout.PREFERRED_SIZE, 58, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(btnSubmit, javax.swing.GroupLayout.PREFERRED_SIZE, 52, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addGap(18, 18, 18)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(lblCurrentUser))
                .addGap(18, 18, 18)
                .addComponent(lblLogOut)
                .addGap(0, 0, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(0, 0, 0)
                .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void txtTimeInOutActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtTimeInOutActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtTimeInOutActionPerformed

    private void btnRecordsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRecordsActionPerformed
        showPage("records");
    }//GEN-LAST:event_btnRecordsActionPerformed

    private void lblLogOutMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_lblLogOutMouseClicked
        Dialogue_Confirmation confirm = new Dialogue_Confirmation(this, true);
        confirm.setLocationRelativeTo(this);
        confirm.setVisible(true);
    }//GEN-LAST:event_lblLogOutMouseClicked

    private void lblSlot1MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_lblSlot1MouseClicked

    }//GEN-LAST:event_lblSlot1MouseClicked

    private void btnReservationActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnReservationActionPerformed
        showPage("reservation");
        reloadReservationsPanel();
    }//GEN-LAST:event_btnReservationActionPerformed

    private void txtTimeInOutKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtTimeInOutKeyPressed

    }//GEN-LAST:event_txtTimeInOutKeyPressed

    private void txtTimeInOutFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_txtTimeInOutFocusLost

    }//GEN-LAST:event_txtTimeInOutFocusLost

    private void btnSubmitActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSubmitActionPerformed
        String plate = txtTimeInOut.getText().trim();
        txtTimeInOut.setText("");

        if(plate.isEmpty()){
            Dialogue_SystemMessage.showMessage(this, "Please enter a plate number.");
            return;
        }

        UserController userController = new UserController();
        SlotController slotController = new SlotController();

        try {
            boolean isExistingReservation = slotController.isPlateReserved(plate);
            boolean isCurrentlyParked = slotController.isPlateParked(plate);

            if(!isExistingReservation && !isCurrentlyParked){
                handleTimeIn(plate);
            } else if(isCurrentlyParked){
                handleTimeOut(plate);
            } else if(isExistingReservation){
                handleReservedTimeIn(plate);
            }

        } catch(Exception e){
            e.printStackTrace();
            Dialogue_SystemMessage.showMessage(this, "Error processing plate number.");
        }
    }//GEN-LAST:event_btnSubmitActionPerformed

    private void btnRecordsMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnRecordsMouseClicked
        
    }//GEN-LAST:event_btnRecordsMouseClicked

    private void tblActivitiesMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tblActivitiesMouseEntered
        // TODO add your handling code here:
    }//GEN-LAST:event_tblActivitiesMouseEntered

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ReflectiveOperationException | javax.swing.UnsupportedLookAndFeelException ex) {
            logger.log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(() -> new HomeForm().setVisible(true));
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnRecords;
    private javax.swing.JButton btnReservation;
    private javax.swing.JButton btnSubmit;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLayeredPane jLayeredPane2;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JLabel lblAvailableSlots;
    private javax.swing.JLabel lblBrandName;
    private javax.swing.JLabel lblCurrentUser;
    private javax.swing.JLabel lblLines;
    private javax.swing.JLabel lblLogOut;
    private javax.swing.JLabel lblOccupiedSlots;
    private javax.swing.JLabel lblSlot1;
    private javax.swing.JLabel lblSlot10;
    private javax.swing.JLabel lblSlot11;
    private javax.swing.JLabel lblSlot12;
    private javax.swing.JLabel lblSlot13;
    private javax.swing.JLabel lblSlot14;
    private javax.swing.JLabel lblSlot15;
    private javax.swing.JLabel lblSlot16;
    private javax.swing.JLabel lblSlot17;
    private javax.swing.JLabel lblSlot18;
    private javax.swing.JLabel lblSlot19;
    private javax.swing.JLabel lblSlot2;
    private javax.swing.JLabel lblSlot20;
    private javax.swing.JLabel lblSlot21;
    private javax.swing.JLabel lblSlot22;
    private javax.swing.JLabel lblSlot23;
    private javax.swing.JLabel lblSlot24;
    private javax.swing.JLabel lblSlot25;
    private javax.swing.JLabel lblSlot26;
    private javax.swing.JLabel lblSlot27;
    private javax.swing.JLabel lblSlot28;
    private javax.swing.JLabel lblSlot29;
    private javax.swing.JLabel lblSlot3;
    private javax.swing.JLabel lblSlot30;
    private javax.swing.JLabel lblSlot4;
    private javax.swing.JLabel lblSlot5;
    private javax.swing.JLabel lblSlot6;
    private javax.swing.JLabel lblSlot7;
    private javax.swing.JLabel lblSlot8;
    private javax.swing.JLabel lblSlot9;
    private javax.swing.JLabel lblTotalSlots;
    private javax.swing.JPanel tblActivities;
    private javax.swing.JLabel txtAvailableSlots;
    private javax.swing.JLabel txtOccupiedSlots;
    private javax.swing.JTextField txtTimeInOut;
    private javax.swing.JLabel txtTotalStots;
    // End of variables declaration//GEN-END:variables
}
