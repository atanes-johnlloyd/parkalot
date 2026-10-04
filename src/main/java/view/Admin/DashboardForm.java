package view.Admin;

import controller.AdminController;
import controller.SettingsController;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.sql.SQLException;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import model.SessionManager;
import view.Dialogue.Dialogue_Confirmation;
import view.HomeForm;

/**
 *
 * @author Alumbres
 */
public class DashboardForm extends javax.swing.JFrame {
    
    private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(DashboardForm.class.getName());
    private AdminController adminController;
    private SettingsController settingsController;
    private java.awt.Color activeColor = new java.awt.Color(0, 102, 102);
    private java.awt.Color normalColor = new java.awt.Color(0, 53, 48);
    private UsersPanel usersPanel;

    CardLayout cardLayout;
    JPanel contentPanel;
    DefaultTableModel model;
    JTable table;
    
    public DashboardForm() throws SQLException {
        String fullName = SessionManager.getFullName();
        String role = SessionManager.getRole();
        
        initComponents();
        
        if (fullName != null) {
            lblCurrentUser.setText(fullName);
        } else {
            lblCurrentUser.setText("");
        }
        
        setActiveNav(btnDashboard);

        adminController = new AdminController();
        settingsController = new SettingsController();

        cardLayout = new CardLayout();
        contentPanel = new JPanel(cardLayout);
        contentPanel.add(White, "dashboard");

        // Fix: store UsersPanel reference
        usersPanel = new UsersPanel(this);
        contentPanel.add(usersPanel, "users");
        contentPanel.add(new RecordsPanel(this), "records");
        contentPanel.add(new TransactionsPanel(this), "transactions");
        contentPanel.add(new ReservationsPanel(this), "reservations");
        contentPanel.add(new AnalyticsPanel(this), "analytics");
        contentPanel.add(new SettingsPanel(this), "settings");

        setLayout(new BorderLayout());
        add(Green, BorderLayout.WEST);
        add(contentPanel, BorderLayout.CENTER);
        new Thread(() -> {
        // Load all data in background
        final String occupancy = adminController.getCurrentOccupancy();
        final int active = adminController.getActiveVehicles();
        final String avgStay = adminController.getAverageStayTime();
        final int daily = adminController.getDailyParkingCount();
        final int avgDaily = adminController.getAverageDailyParking();
        final String earned = adminController.getTotalEarned();
        final String lastHour = adminController.getLastHourEarnings();
        final String[] activities = adminController.getRecentActivities();
        final Object[] capacity = adminController.getCapacityStatus();

        // Update UI on EDT once data is ready
        javax.swing.SwingUtilities.invokeLater(() -> {
                lblCO.setText(occupancy);
                lblActive.setText(active + " Active");
                lblAvg.setText("Avg stay: " + avgStay);
                lblDaily.setText(String.valueOf(daily));
                lblDAvg.setText("avg daily park: " + avgDaily);
                lblEarned.setText(earned);
                lblAvgE.setText(lastHour);
                updateRecentActivitiesWithData(activities);
                updateCapacityPanelWithData(capacity);
            });
        }).start();

        // Auto-refresh every 60 seconds
        new javax.swing.Timer(60000, e -> {
            new Thread(() -> {
                final String occ = adminController.getCurrentOccupancy();
                final int act = adminController.getActiveVehicles();
                final String avg = adminController.getAverageStayTime();
                final int dal = adminController.getDailyParkingCount();
                final int avgDal = adminController.getAverageDailyParking();
                final String ear = adminController.getTotalEarned();
                final String lh = adminController.getLastHourEarnings();
                final String[] acts = adminController.getRecentActivities();
                final Object[] cap = adminController.getCapacityStatus();

                javax.swing.SwingUtilities.invokeLater(() -> {
                    lblCO.setText(occ);
                    lblActive.setText(act + " Active");
                    lblAvg.setText("Avg stay: " + avg);
                    lblDaily.setText(String.valueOf(dal));
                    lblDAvg.setText("avg daily park: " + avgDal);
                    lblEarned.setText(ear);
                    lblAvgE.setText(lh);
                    updateRecentActivitiesWithData(acts);
                    updateCapacityPanelWithData(cap);
                });
            }).start();
        }).start();
    }
    
    private void setActiveNav(javax.swing.JButton activeButton) {

        // reset all buttons
        btnDashboard.setBackground(normalColor);
        btnUsers.setBackground(normalColor);
        btnRecords.setBackground(normalColor);
        btnTransactions.setBackground(normalColor);
        btnReservations.setBackground(normalColor);
        btnAnalytics.setBackground(normalColor);
        btnSettings.setBackground(normalColor);

        // highlight active button
        activeButton.setBackground(activeColor);

    }
    
    private void updateRecentActivitiesWithData(String[] activities) {
        javax.swing.JLabel[] labels = {lblEntered, lblExited, lblOverstayed, lblPayment};

        for (int i = 0; i < activities.length; i++) {
            String activity = activities[i];
            javax.swing.JLabel currentLabel = labels[i];

            currentLabel.setText(activity);

            if (activity.contains("Entered")) {
                currentLabel.setIcon(new javax.swing.ImageIcon(
                    System.getProperty("user.dir") + "\\src\\main\\java\\assets\\car.png"));
            } else if (activity.contains("Exited")) {
                currentLabel.setIcon(new javax.swing.ImageIcon(
                    System.getProperty("user.dir") + "\\src\\main\\java\\assets\\clock.png"));
            } else if (activity.contains("Reserved")) {
                currentLabel.setIcon(new javax.swing.ImageIcon(
                    System.getProperty("user.dir") + "\\src\\main\\java\\assets\\car.png"));
            } else if (activity.contains("Alert") || activity.contains("Overstayed")) {
                currentLabel.setIcon(new javax.swing.ImageIcon(
                    System.getProperty("user.dir") + "\\src\\main\\java\\assets\\warning.png"));
            } else {
                currentLabel.setIcon(null);
            }
        }
    }

    private void updateCapacityPanelWithData(Object[] capacityData) {
        int available = (int) capacityData[1];
        boolean isCritical = (boolean) capacityData[3];

        capacity.removeAll();
        capacity.setLayout(new java.awt.BorderLayout());

        if (isCritical) {
            CapacityWarningPanel warningPanel = new CapacityWarningPanel();
            warningPanel.setSize(220, 286);
            warningPanel.setPercentText(available + "% full");
            warningPanel.setRemainText(available + " spots remaining");
            capacity.add(warningPanel, java.awt.BorderLayout.CENTER);
        } else {
            CapacityNormalPanel normalPanel = new CapacityNormalPanel();
            normalPanel.setSize(220, 286);
            normalPanel.setSpotsText(available + " spots available");
            capacity.add(normalPanel, java.awt.BorderLayout.CENTER);
        }

        capacity.revalidate();
        capacity.repaint();
    }
    
    public SettingsController getSettingsController() {
    return settingsController;
    }
        
    public UsersPanel getUsersPanel() {
        return usersPanel;
    }

    public void showPage(String page){
        cardLayout.show(contentPanel, page);
    }
    
    private int getPageIndex(String pageName) {

        switch (pageName) {
            case "dashboard":
                return 0;
            case "users":
                return 1;
            case "records":
                return 2;
            case "transactions":
                return 3;
            case "reservations":
                return 4;
            case "analytics":
                return 5;
            case "settings":
                return 6;
            default:
                return 0; // Default to dashboard
        }
    }
    
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        White = new javax.swing.JPanel();
        Green = new javax.swing.JPanel();
        title = new javax.swing.JLabel();
        jLabel1 = new javax.swing.JLabel();
        btnDashboard = new javax.swing.JButton();
        btnUsers = new javax.swing.JButton();
        btnAnalytics = new javax.swing.JButton();
        btnSettings = new javax.swing.JButton();
        txtCar = new javax.swing.JLabel();
        btnStaffView = new javax.swing.JButton();
        btnRecords = new javax.swing.JButton();
        btnTransactions = new javax.swing.JButton();
        btnReservations = new javax.swing.JButton();
        btnLogout = new javax.swing.JButton();
        lblCurrentUser = new javax.swing.JLabel();
        lblUser1 = new javax.swing.JLabel();
        Occupancy = new javax.swing.JPanel();
        jLabel17 = new javax.swing.JLabel();
        lblCO = new javax.swing.JLabel();
        jLabel28 = new javax.swing.JLabel();
        jLabel30 = new javax.swing.JLabel();
        jLabel11 = new javax.swing.JLabel();
        Parked = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        lblActive = new javax.swing.JLabel();
        lblAvg = new javax.swing.JLabel();
        jLabel35 = new javax.swing.JLabel();
        Daily = new javax.swing.JPanel();
        jLabel7 = new javax.swing.JLabel();
        lblDAvg = new javax.swing.JLabel();
        lblDaily = new javax.swing.JLabel();
        jLabel36 = new javax.swing.JLabel();
        earned = new javax.swing.JPanel();
        jLabel8 = new javax.swing.JLabel();
        lblEarned = new javax.swing.JLabel();
        lblAvgE = new javax.swing.JLabel();
        jLabel33 = new javax.swing.JLabel();
        Recent = new javax.swing.JPanel();
        jLabel10 = new javax.swing.JLabel();
        lblPayment = new javax.swing.JLabel();
        lblEntered = new javax.swing.JLabel();
        lblExited = new javax.swing.JLabel();
        lblOverstayed = new javax.swing.JLabel();
        capacity = new javax.swing.JPanel();
        jLabel5 = new javax.swing.JLabel();
        jLabel23 = new javax.swing.JLabel();
        lblspots = new javax.swing.JLabel();
        jLabel25 = new javax.swing.JLabel();
        jLabel26 = new javax.swing.JLabel();
        jLabel13 = new javax.swing.JLabel();
        jLabel9 = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        White.setBackground(new java.awt.Color(255, 255, 255));
        White.setPreferredSize(new java.awt.Dimension(1280, 768));

        Green.setBackground(new java.awt.Color(0, 53, 48));

        title.setFont(new java.awt.Font("Javanese Text", 1, 22)); // NOI18N
        title.setForeground(new java.awt.Color(255, 255, 255));
        title.setText("ParkaLot");

        jLabel1.setIcon(new javax.swing.ImageIcon(System.getProperty("user.dir") + "\\src\\main\\java\\assets\\logo.png"));

        btnDashboard.setBackground(new java.awt.Color(0, 102, 102));
        btnDashboard.setFont(new java.awt.Font("Javanese Text", 1, 18)); // NOI18N
        btnDashboard.setForeground(new java.awt.Color(255, 255, 255));
        btnDashboard.setText("  Dashboard");
        btnDashboard.setBorder(null);
        btnDashboard.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        btnDashboard.addActionListener(this::btnDashboardActionPerformed);

        btnUsers.setBackground(new java.awt.Color(0, 53, 48));
        btnUsers.setFont(new java.awt.Font("Javanese Text", 1, 18)); // NOI18N
        btnUsers.setForeground(new java.awt.Color(255, 255, 255));
        btnUsers.setText("  Users");
        btnUsers.setBorder(null);
        btnUsers.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        btnUsers.addActionListener(this::btnUsersActionPerformed);

        btnAnalytics.setBackground(new java.awt.Color(0, 53, 48));
        btnAnalytics.setFont(new java.awt.Font("Javanese Text", 1, 18)); // NOI18N
        btnAnalytics.setForeground(new java.awt.Color(255, 255, 255));
        btnAnalytics.setText("  Analytics");
        btnAnalytics.setBorder(null);
        btnAnalytics.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        btnAnalytics.addActionListener(this::btnAnalyticsActionPerformed);

        btnSettings.setBackground(new java.awt.Color(0, 53, 48));
        btnSettings.setFont(new java.awt.Font("Javanese Text", 1, 18)); // NOI18N
        btnSettings.setForeground(new java.awt.Color(255, 255, 255));
        btnSettings.setText("  Settings");
        btnSettings.setBorder(null);
        btnSettings.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        btnSettings.addActionListener(this::btnSettingsActionPerformed);

        txtCar.setIcon(new javax.swing.ImageIcon(System.getProperty("user.dir") + "\\src\\main\\java\\assets\\car_nav.png"));

        btnStaffView.setBackground(new java.awt.Color(0, 53, 48));
        btnStaffView.setFont(new java.awt.Font("Javanese Text", 1, 18)); // NOI18N
        btnStaffView.setForeground(new java.awt.Color(255, 255, 255));
        btnStaffView.setText("  Staff View");
        btnStaffView.setBorder(null);
        btnStaffView.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        btnStaffView.addActionListener(this::btnStaffViewActionPerformed);

        btnRecords.setBackground(new java.awt.Color(0, 53, 48));
        btnRecords.setFont(new java.awt.Font("Javanese Text", 1, 18)); // NOI18N
        btnRecords.setForeground(new java.awt.Color(255, 255, 255));
        btnRecords.setText("  Records");
        btnRecords.setBorder(null);
        btnRecords.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        btnRecords.addActionListener(this::btnRecordsActionPerformed);

        btnTransactions.setBackground(new java.awt.Color(0, 53, 48));
        btnTransactions.setFont(new java.awt.Font("Javanese Text", 1, 18)); // NOI18N
        btnTransactions.setForeground(new java.awt.Color(255, 255, 255));
        btnTransactions.setText("  Transactions");
        btnTransactions.setBorder(null);
        btnTransactions.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        btnTransactions.addActionListener(this::btnTransactionsActionPerformed);

        btnReservations.setBackground(new java.awt.Color(0, 53, 48));
        btnReservations.setFont(new java.awt.Font("Javanese Text", 1, 18)); // NOI18N
        btnReservations.setForeground(new java.awt.Color(255, 255, 255));
        btnReservations.setText("  Reservations");
        btnReservations.setBorder(null);
        btnReservations.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        btnReservations.addActionListener(this::btnReservationsActionPerformed);

        btnLogout.setFont(new java.awt.Font("Century Schoolbook", 1, 14)); // NOI18N
        btnLogout.setForeground(new java.awt.Color(0, 51, 51));
        btnLogout.setIcon(new javax.swing.ImageIcon(System.getProperty("user.dir") + "\\src\\main\\java\\assets\\door.png"));
        btnLogout.setText("  LOG OUT");
        btnLogout.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(0, 51, 51), 3, true));
        btnLogout.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        btnLogout.addActionListener(this::btnLogoutActionPerformed);

        lblCurrentUser.setFont(new java.awt.Font("Century Schoolbook", 0, 18)); // NOI18N
        lblCurrentUser.setForeground(new java.awt.Color(255, 255, 255));
        lblCurrentUser.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        lblCurrentUser.setText("...");

        lblUser1.setFont(new java.awt.Font("Century Schoolbook", 0, 14)); // NOI18N
        lblUser1.setForeground(new java.awt.Color(255, 255, 255));
        lblUser1.setText("User:");

        javax.swing.GroupLayout GreenLayout = new javax.swing.GroupLayout(Green);
        Green.setLayout(GreenLayout);
        GreenLayout.setHorizontalGroup(
            GreenLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(GreenLayout.createSequentialGroup()
                .addGap(17, 17, 17)
                .addGroup(GreenLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(GreenLayout.createSequentialGroup()
                        .addGroup(GreenLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addGroup(GreenLayout.createSequentialGroup()
                                .addComponent(jLabel1)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(title))
                            .addGroup(GreenLayout.createSequentialGroup()
                                .addComponent(txtCar)
                                .addGap(0, 0, 0)
                                .addGroup(GreenLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addComponent(lblUser1, javax.swing.GroupLayout.PREFERRED_SIZE, 103, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(btnDashboard, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(btnUsers, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(btnAnalytics, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(btnSettings, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(btnRecords, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(btnTransactions, javax.swing.GroupLayout.DEFAULT_SIZE, 126, Short.MAX_VALUE)
                                    .addComponent(btnReservations, javax.swing.GroupLayout.DEFAULT_SIZE, 126, Short.MAX_VALUE)
                                    .addComponent(btnStaffView, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(btnLogout, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
                        .addGap(0, 18, Short.MAX_VALUE))
                    .addComponent(lblCurrentUser, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        GreenLayout.setVerticalGroup(
            GreenLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(GreenLayout.createSequentialGroup()
                .addGap(39, 39, 39)
                .addGroup(GreenLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(GreenLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                        .addGroup(GreenLayout.createSequentialGroup()
                            .addGroup(GreenLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                .addComponent(title)
                                .addComponent(jLabel1))
                            .addGap(18, 18, 18)
                            .addComponent(btnDashboard)
                            .addGap(8, 8, 8))
                        .addComponent(txtCar))
                    .addGroup(GreenLayout.createSequentialGroup()
                        .addGap(119, 119, 119)
                        .addComponent(btnUsers)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnRecords)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnTransactions)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnReservations)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnAnalytics)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnSettings)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 134, Short.MAX_VALUE)
                .addComponent(btnStaffView)
                .addGap(41, 41, 41)
                .addComponent(lblUser1)
                .addGap(0, 0, 0)
                .addComponent(lblCurrentUser)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnLogout, javax.swing.GroupLayout.PREFERRED_SIZE, 46, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(20, 20, 20))
        );

        Occupancy.setBackground(new java.awt.Color(239, 239, 239));
        Occupancy.setMinimumSize(new java.awt.Dimension(155, 141));
        Occupancy.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabel17.setFont(new java.awt.Font("Javanese Text", 1, 20)); // NOI18N
        jLabel17.setForeground(new java.awt.Color(28, 28, 28));
        jLabel17.setText("Current Occupancy");
        Occupancy.add(jLabel17, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 130, -1, -1));

        lblCO.setFont(new java.awt.Font("Javanese Text", 1, 30)); // NOI18N
        lblCO.setForeground(new java.awt.Color(28, 28, 28));
        lblCO.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblCO.setText("...");
        Occupancy.add(lblCO, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 70, 200, 40));

        jLabel28.setFont(new java.awt.Font("Javanese Text", 1, 14)); // NOI18N
        jLabel28.setForeground(new java.awt.Color(68, 68, 68));
        jLabel28.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel28.setText("Live Count");
        Occupancy.add(jLabel28, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 100, 200, 20));

        jLabel30.setIcon(new javax.swing.ImageIcon(System.getProperty("user.dir") + "\\src\\main\\java\\assets\\occupied.png"));
        Occupancy.add(jLabel30, new org.netbeans.lib.awtextra.AbsoluteConstraints(75, 10, -1, -1));
        Occupancy.add(jLabel11, new org.netbeans.lib.awtextra.AbsoluteConstraints(75, 46, -1, -1));

        Parked.setBackground(new java.awt.Color(239, 239, 239));
        Parked.setPreferredSize(new java.awt.Dimension(135, 111));
        Parked.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabel2.setFont(new java.awt.Font("Javanese Text", 1, 21)); // NOI18N
        jLabel2.setForeground(new java.awt.Color(28, 28, 28));
        jLabel2.setText("Parked Vehicles");
        Parked.add(jLabel2, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 135, -1, 30));

        lblActive.setFont(new java.awt.Font("Javanese Text", 1, 30)); // NOI18N
        lblActive.setForeground(new java.awt.Color(28, 28, 28));
        lblActive.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblActive.setText("...");
        Parked.add(lblActive, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 70, 210, 40));

        lblAvg.setFont(new java.awt.Font("Javanese Text", 1, 14)); // NOI18N
        lblAvg.setForeground(new java.awt.Color(68, 68, 68));
        lblAvg.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblAvg.setText("...");
        Parked.add(lblAvg, new org.netbeans.lib.awtextra.AbsoluteConstraints(1, 100, 210, 20));

        jLabel35.setIcon(new javax.swing.ImageIcon(System.getProperty("user.dir") + "\\src\\main\\java\\assets\\parked.png"));
        Parked.add(jLabel35, new org.netbeans.lib.awtextra.AbsoluteConstraints(85, 20, -1, -1));

        Daily.setBackground(new java.awt.Color(239, 239, 239));
        Daily.setPreferredSize(new java.awt.Dimension(135, 111));
        Daily.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabel7.setFont(new java.awt.Font("Javanese Text", 1, 21)); // NOI18N
        jLabel7.setForeground(new java.awt.Color(28, 28, 28));
        jLabel7.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel7.setText("Daily Parking");
        Daily.add(jLabel7, new org.netbeans.lib.awtextra.AbsoluteConstraints(1, 130, 210, 35));

        lblDAvg.setFont(new java.awt.Font("Javanese Text", 1, 14)); // NOI18N
        lblDAvg.setForeground(new java.awt.Color(68, 68, 68));
        lblDAvg.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblDAvg.setText("...");
        Daily.add(lblDAvg, new org.netbeans.lib.awtextra.AbsoluteConstraints(3, 100, 200, 20));

        lblDaily.setFont(new java.awt.Font("Javanese Text", 1, 30)); // NOI18N
        lblDaily.setForeground(new java.awt.Color(28, 28, 28));
        lblDaily.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblDaily.setText("...");
        Daily.add(lblDaily, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 70, 210, 40));

        jLabel36.setIcon(new javax.swing.ImageIcon(System.getProperty("user.dir") + "\\src\\main\\java\\assets\\daily.png"));
        Daily.add(jLabel36, new org.netbeans.lib.awtextra.AbsoluteConstraints(80, 10, -1, -1));

        earned.setBackground(new java.awt.Color(239, 239, 239));
        earned.setPreferredSize(new java.awt.Dimension(135, 111));
        earned.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabel8.setFont(new java.awt.Font("Javanese Text", 1, 21)); // NOI18N
        jLabel8.setForeground(new java.awt.Color(28, 28, 28));
        jLabel8.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel8.setText("Total Earned");
        earned.add(jLabel8, new org.netbeans.lib.awtextra.AbsoluteConstraints(-4, 130, 230, 30));

        lblEarned.setFont(new java.awt.Font("Javanese Text", 1, 30)); // NOI18N
        lblEarned.setForeground(new java.awt.Color(28, 28, 28));
        lblEarned.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblEarned.setText("...");
        earned.add(lblEarned, new org.netbeans.lib.awtextra.AbsoluteConstraints(4, 70, 220, 40));

        lblAvgE.setFont(new java.awt.Font("Javanese Text", 1, 14)); // NOI18N
        lblAvgE.setForeground(new java.awt.Color(68, 68, 68));
        lblAvgE.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblAvgE.setText("...");
        earned.add(lblAvgE, new org.netbeans.lib.awtextra.AbsoluteConstraints(-2, 100, 230, 20));

        jLabel33.setIcon(new javax.swing.ImageIcon(System.getProperty("user.dir") + "\\src\\main\\java\\assets\\earn.png"));
        earned.add(jLabel33, new org.netbeans.lib.awtextra.AbsoluteConstraints(97, 20, -1, -1));

        Recent.setBackground(new java.awt.Color(239, 239, 239));
        Recent.setPreferredSize(new java.awt.Dimension(443, 187));
        Recent.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabel10.setFont(new java.awt.Font("Javanese Text", 1, 25)); // NOI18N
        jLabel10.setForeground(new java.awt.Color(0, 53, 48));
        jLabel10.setText("Recent Activities");
        Recent.add(jLabel10, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 10, -1, -1));

        lblPayment.setFont(new java.awt.Font("Javanese Text", 1, 20)); // NOI18N
        lblPayment.setForeground(new java.awt.Color(28, 28, 28));
        lblPayment.setIcon(new javax.swing.ImageIcon(System.getProperty("user.dir") + "\\src\\main\\java\\assets\\wallet.png"));
        lblPayment.setText("...");
        Recent.add(lblPayment, new org.netbeans.lib.awtextra.AbsoluteConstraints(50, 230, -1, 30));

        lblEntered.setFont(new java.awt.Font("Javanese Text", 1, 20)); // NOI18N
        lblEntered.setForeground(new java.awt.Color(28, 28, 28));
        lblEntered.setIcon(new javax.swing.ImageIcon(System.getProperty("user.dir") + "\\src\\main\\java\\assets\\car.png"));
        lblEntered.setText("...");
        Recent.add(lblEntered, new org.netbeans.lib.awtextra.AbsoluteConstraints(50, 80, -1, 30));

        lblExited.setFont(new java.awt.Font("Javanese Text", 1, 20)); // NOI18N
        lblExited.setForeground(new java.awt.Color(28, 28, 28));
        lblExited.setIcon(new javax.swing.ImageIcon(System.getProperty("user.dir") + "\\src\\main\\java\\assets\\clock.png"));
        lblExited.setText("...");
        Recent.add(lblExited, new org.netbeans.lib.awtextra.AbsoluteConstraints(50, 130, -1, 30));

        lblOverstayed.setFont(new java.awt.Font("Javanese Text", 1, 20)); // NOI18N
        lblOverstayed.setForeground(new java.awt.Color(28, 28, 28));
        lblOverstayed.setIcon(new javax.swing.ImageIcon(System.getProperty("user.dir") + "\\src\\main\\java\\assets\\warning.png"));
        lblOverstayed.setText("...");
        Recent.add(lblOverstayed, new org.netbeans.lib.awtextra.AbsoluteConstraints(50, 180, -1, 30));

        capacity.setBackground(new java.awt.Color(239, 239, 239));
        capacity.setPreferredSize(new java.awt.Dimension(170, 187));
        capacity.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabel5.setFont(new java.awt.Font("Javanese Text", 1, 25)); // NOI18N
        jLabel5.setForeground(new java.awt.Color(0, 53, 48));
        jLabel5.setText("Capacity Status");
        capacity.add(jLabel5, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 10, -1, -1));

        jLabel23.setFont(new java.awt.Font("Javanese Text", 1, 20)); // NOI18N
        jLabel23.setForeground(new java.awt.Color(28, 28, 28));
        jLabel23.setText("Good Availability");
        capacity.add(jLabel23, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 130, -1, 40));

        lblspots.setFont(new java.awt.Font("Javanese Text", 1, 17)); // NOI18N
        lblspots.setForeground(new java.awt.Color(28, 28, 28));
        lblspots.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblspots.setText("...");
        capacity.add(lblspots, new org.netbeans.lib.awtextra.AbsoluteConstraints(40, 160, 150, 40));

        jLabel25.setFont(new java.awt.Font("Javanese Text", 1, 17)); // NOI18N
        jLabel25.setForeground(new java.awt.Color(28, 28, 28));
        jLabel25.setText("operations normal");
        capacity.add(jLabel25, new org.netbeans.lib.awtextra.AbsoluteConstraints(40, 230, -1, 20));

        jLabel26.setFont(new java.awt.Font("Javanese Text", 1, 17)); // NOI18N
        jLabel26.setForeground(new java.awt.Color(28, 28, 28));
        jLabel26.setText("Plenty of space today");
        capacity.add(jLabel26, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 200, -1, 20));

        jLabel13.setIcon(new javax.swing.ImageIcon(System.getProperty("user.dir") + "\\src\\main\\java\\assets\\accept.png"));
        capacity.add(jLabel13, new org.netbeans.lib.awtextra.AbsoluteConstraints(80, 60, -1, -1));

        jLabel9.setFont(new java.awt.Font("Javanese Text", 1, 22)); // NOI18N
        jLabel9.setForeground(new java.awt.Color(0, 53, 48));
        jLabel9.setText("Dashboard");

        javax.swing.GroupLayout WhiteLayout = new javax.swing.GroupLayout(White);
        White.setLayout(WhiteLayout);
        WhiteLayout.setHorizontalGroup(
            WhiteLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(WhiteLayout.createSequentialGroup()
                .addComponent(Green, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 110, Short.MAX_VALUE)
                .addGroup(WhiteLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel9)
                    .addGroup(WhiteLayout.createSequentialGroup()
                        .addGroup(WhiteLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addComponent(Recent, javax.swing.GroupLayout.PREFERRED_SIZE, 667, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(WhiteLayout.createSequentialGroup()
                                .addComponent(Occupancy, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(Parked, javax.swing.GroupLayout.PREFERRED_SIZE, 207, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)
                                .addComponent(Daily, javax.swing.GroupLayout.PREFERRED_SIZE, 207, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addGap(18, 18, 18)
                        .addGroup(WhiteLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(earned, javax.swing.GroupLayout.DEFAULT_SIZE, 228, Short.MAX_VALUE)
                            .addComponent(capacity, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
                .addGap(90, 90, 90))
        );
        WhiteLayout.setVerticalGroup(
            WhiteLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(Green, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, WhiteLayout.createSequentialGroup()
                .addGap(39, 39, 39)
                .addComponent(jLabel9)
                .addGap(18, 18, 18)
                .addGroup(WhiteLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(Parked, javax.swing.GroupLayout.PREFERRED_SIZE, 171, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(earned, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(Daily, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(Occupancy, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE))
                .addGap(68, 68, 68)
                .addGroup(WhiteLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(capacity, javax.swing.GroupLayout.DEFAULT_SIZE, 284, Short.MAX_VALUE)
                    .addComponent(Recent, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(27, 137, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(White, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(White, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        pack();
        setLocationRelativeTo(null);
    }// </editor-fold>//GEN-END:initComponents

    private void btnAnalyticsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAnalyticsActionPerformed
        showPage("analytics");
        setActiveNav(btnAnalytics);
        txtCar.setLocation(txtCar.getX(), btnAnalytics.getY());
        
        AnalyticsPanel analyticsPanel = (AnalyticsPanel) contentPanel.getComponent(
        getPageIndex("analytics"));
        analyticsPanel.loadDailyParkinChart();
        analyticsPanel.loadVehiclePieChart();
        analyticsPanel.updateDailyParkLabel();
        analyticsPanel.updateDailyReservesLabel();
        analyticsPanel.updateMostVehicleTypeLabel();
        analyticsPanel.updateBusyDayLabel();
    }//GEN-LAST:event_btnAnalyticsActionPerformed

    private void btnLogoutActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnLogoutActionPerformed
        Dialogue_Confirmation confirm = new Dialogue_Confirmation(this, true);
        confirm.setLocationRelativeTo(this);
        confirm.setVisible(true);
    }//GEN-LAST:event_btnLogoutActionPerformed

    private void btnDashboardActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnDashboardActionPerformed
        showPage("dashboard");
        setActiveNav(btnDashboard);
        txtCar.setLocation(txtCar.getX(), btnDashboard.getY());
        
        new Thread(() -> {
        final String occupancy = adminController.getCurrentOccupancy();
        final int active = adminController.getActiveVehicles();
        final String avgStay = adminController.getAverageStayTime();
        final int daily = adminController.getDailyParkingCount();
        final int avgDaily = adminController.getAverageDailyParking();
        final String earned = adminController.getTotalEarned();
        final String lastHour = adminController.getLastHourEarnings();
        final String[] activities = adminController.getRecentActivities();
        final Object[] capacity = adminController.getCapacityStatus();
        javax.swing.SwingUtilities.invokeLater(() -> {
            lblCO.setText(occupancy);
            lblActive.setText(active + " Active");
            lblAvg.setText("Avg stay: " + avgStay);
            lblDaily.setText(String.valueOf(daily));
            lblDAvg.setText("avg daily park: " + avgDaily);
            lblEarned.setText(earned);
            lblAvgE.setText(lastHour);
            updateRecentActivitiesWithData(activities);
            updateCapacityPanelWithData(capacity);
        });
    }).start();
    }//GEN-LAST:event_btnDashboardActionPerformed

    private void btnUsersActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnUsersActionPerformed
        showPage("users");
        setActiveNav(btnUsers);
        txtCar.setLocation(txtCar.getX(), btnUsers.getY());
        
        try {
        usersPanel.loadTable();
        } catch (java.sql.SQLException ex) {
            ex.printStackTrace();
        }
    }//GEN-LAST:event_btnUsersActionPerformed

    private void btnSettingsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSettingsActionPerformed
        showPage("settings");
        setActiveNav(btnSettings);
        txtCar.setLocation(txtCar.getX(), btnSettings.getY());
    }//GEN-LAST:event_btnSettingsActionPerformed

    private void btnStaffViewActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnStaffViewActionPerformed
        HomeForm homeObj = new HomeForm();
        homeObj.setVisible(true);
        this.dispose();
    }//GEN-LAST:event_btnStaffViewActionPerformed

    private void btnRecordsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRecordsActionPerformed
        showPage("records");
        setActiveNav(btnRecords);
        txtCar.setLocation(txtCar.getX(), btnRecords.getY());
        
        ((RecordsPanel) contentPanel.getComponent(
        getPageIndex("records"))).loadTable();
    }//GEN-LAST:event_btnRecordsActionPerformed

    private void btnTransactionsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnTransactionsActionPerformed
        showPage("transactions");
        setActiveNav(btnTransactions);
        txtCar.setLocation(txtCar.getX(), btnTransactions.getY());
        
        ((TransactionsPanel) contentPanel.getComponent(
        getPageIndex("transactions"))).loadTable();
    }//GEN-LAST:event_btnTransactionsActionPerformed

    private void btnReservationsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnReservationsActionPerformed
        showPage("reservations");
        setActiveNav(btnReservations);
        txtCar.setLocation(txtCar.getX(), btnReservations.getY());
        
        ((ReservationsPanel) contentPanel.getComponent(
        getPageIndex("reservations"))).loadTable();
    }//GEN-LAST:event_btnReservationsActionPerformed

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
        java.awt.EventQueue.invokeLater(() -> {
            try {
                new DashboardForm().setVisible(true);
            } catch (SQLException ex) {
                System.getLogger(DashboardForm.class.getName()).log(System.Logger.Level.ERROR, (String) null, ex);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel Daily;
    private javax.swing.JPanel Green;
    private javax.swing.JPanel Occupancy;
    private javax.swing.JPanel Parked;
    private javax.swing.JPanel Recent;
    private javax.swing.JPanel White;
    private javax.swing.JButton btnAnalytics;
    private javax.swing.JButton btnDashboard;
    private javax.swing.JButton btnLogout;
    private javax.swing.JButton btnRecords;
    private javax.swing.JButton btnReservations;
    private javax.swing.JButton btnSettings;
    private javax.swing.JButton btnStaffView;
    private javax.swing.JButton btnTransactions;
    private javax.swing.JButton btnUsers;
    private javax.swing.JPanel capacity;
    private javax.swing.JPanel earned;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel17;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel23;
    private javax.swing.JLabel jLabel25;
    private javax.swing.JLabel jLabel26;
    private javax.swing.JLabel jLabel28;
    private javax.swing.JLabel jLabel30;
    private javax.swing.JLabel jLabel33;
    private javax.swing.JLabel jLabel35;
    private javax.swing.JLabel jLabel36;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JLabel lblActive;
    private javax.swing.JLabel lblAvg;
    private javax.swing.JLabel lblAvgE;
    private javax.swing.JLabel lblCO;
    private javax.swing.JLabel lblCurrentUser;
    private javax.swing.JLabel lblDAvg;
    private javax.swing.JLabel lblDaily;
    private javax.swing.JLabel lblEarned;
    private javax.swing.JLabel lblEntered;
    private javax.swing.JLabel lblExited;
    private javax.swing.JLabel lblOverstayed;
    private javax.swing.JLabel lblPayment;
    private javax.swing.JLabel lblUser1;
    private javax.swing.JLabel lblspots;
    private javax.swing.JLabel title;
    private javax.swing.JLabel txtCar;
    // End of variables declaration//GEN-END:variables

 
}
