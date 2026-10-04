package view;

import controller.SettingsController;
import controller.SlotController;
import java.awt.Color;
import java.awt.Dimension;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;
import view.Dialogue.Dialogue_Reservation;
import view.Dialogue.Dialogue_SystemMessage;

public class ReservationPanel extends javax.swing.JPanel {
    private SlotController slotController = new SlotController();
    private HomeForm parent;
    private int hoveredRow = -1;
    private SettingsController settingsController;
    
    public ReservationPanel(HomeForm parent) {
        this.parent = parent;
        this.settingsController = new SettingsController();
        initComponents();
        
        jTable1.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                int row = jTable1.getSelectedRow();

                if (row != -1) {
                    int slotNumber = Integer.parseInt(jTable1.getValueAt(row, 0).toString());
                    
                }
            }
        });
        
        jTable1.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
            @Override
            public void mouseMoved(java.awt.event.MouseEvent e) {
                int row = jTable1.rowAtPoint(e.getPoint());

                if (row != hoveredRow) {
                    hoveredRow = row;
                    jTable1.repaint();

                    if (row != -1) {
                        int slotNumber = Integer.parseInt(jTable1.getValueAt(row, 0).toString());
                        parent.highlightSlotFromRecords(slotNumber);
                        
                        jTable1.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
                    } else {
                        parent.highlightSlotFromRecords(-1);
                        jTable1.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
                    }
                }
            }
        });
        
        jTable1.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                hoveredRow = -1;
                jTable1.repaint();
                parent.highlightSlotFromRecords(-1);
                jTable1.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
            }
        });
        
        jTable1.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                
                if (evt.getClickCount() == 2) {
                    int row = jTable1.getSelectedRow();
                    if (row != -1) showRowDetails(row);
                }
            }
        });
        
        jTable1.setAutoResizeMode(javax.swing.JTable.AUTO_RESIZE_OFF);
        jTable1.getColumnModel().getColumn(0).setPreferredWidth(40);  // ID
        jTable1.getColumnModel().getColumn(1).setPreferredWidth(50);  // Slot
        jTable1.getColumnModel().getColumn(2).setPreferredWidth(120); // Plate
        jTable1.getColumnModel().getColumn(3).setPreferredWidth(150); // Date & Time
        jTable1.getColumnModel().getColumn(4).setPreferredWidth(80);  // Status
        
        jTable1.setDefaultEditor(Object.class, null);
        
        jTable1.setSelectionBackground(new java.awt.Color(0, 102, 102));
        jTable1.setSelectionForeground(new java.awt.Color(255, 255, 255));
        
        jTable1.setRowHeight(30);

        jTable1.getTableHeader().setPreferredSize(new Dimension(100, 35));
        
        jTable1.setFillsViewportHeight(true);
        
        jTable1.setDefaultRenderer(Object.class, new javax.swing.table.DefaultTableCellRenderer() {
            @Override
            public java.awt.Component getTableCellRendererComponent(
                    javax.swing.JTable table, Object value,
                    boolean isSelected, boolean hasFocus,
                    int row, int column) {

                java.awt.Component c = super.getTableCellRendererComponent(
                        table, value, isSelected, hasFocus, row, column);

                if (row == hoveredRow) {
                    c.setBackground(new Color(0, 102, 102));
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
        
        loadReservations();
        repaint();
    }
    
    private void showRowDetails(int row) {
        String id       = jTable1.getValueAt(row, 0).toString();
        String slot     = jTable1.getValueAt(row, 1).toString();
        String plate    = jTable1.getValueAt(row, 2).toString();
        String dateTime = jTable1.getValueAt(row, 3).toString();
        String status   = jTable1.getValueAt(row, 4).toString();

        // Fetch contact and vehicle from DB
        String vehicle = "-";
        String contact = "-";

        String sql = "SELECT vehicle, contact FROM reservations WHERE id = ? LIMIT 1";
        try (java.sql.Connection conn = model.DBConnection.connect();
             java.sql.PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, Integer.parseInt(id));
            try (java.sql.ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    vehicle = rs.getString("vehicle") != null 
                        ? rs.getString("vehicle") : "-";
                    contact = rs.getString("contact") != null 
                        ? rs.getString("contact") : "-";
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        String details = String.format(
            "Reservation ID : %s\n" +
            "Slot Number    : %s\n" +
            "Plate Number   : %s\n" +
            "Vehicle        : %s\n" +
            "Contact        : %s\n" +
            "Reservation At : %s\n" +
            "Status         : %s",
            id, slot, plate, vehicle, contact, dateTime, status
        );

        javax.swing.JOptionPane.showMessageDialog(
            this,
            details,
            "Reservation Details — Slot " + slot,
            javax.swing.JOptionPane.INFORMATION_MESSAGE
        );
    }
    
    public void loadReservations() {
        DefaultTableModel model = (DefaultTableModel) jTable1.getModel();
        model.setRowCount(0);

        try {
            SlotController slotControl = new SlotController();
            ResultSet rs = slotControl.getReservations();

            while (rs != null && rs.next()) {
                model.addRow(new Object[]{
                    rs.getInt("id"),
                    rs.getInt("slot_number"),
                    rs.getString("plate_number"),
                    rs.getTimestamp("reservation_time"),
                    rs.getString("status")
                });
            }

        } catch (SQLException e) {
            e.printStackTrace();
            Dialogue_SystemMessage.showMessage(parent, "Failed to load reservations.");
        }
    }
    
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jButton1 = new javax.swing.JButton();
        jPanel1 = new javax.swing.JPanel();
        lblBrandName = new javax.swing.JLabel();
        jLabel1 = new javax.swing.JLabel();
        btnBack = new javax.swing.JButton();
        jLabel2 = new javax.swing.JLabel();
        btnAdd = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTable1 = new javax.swing.JTable();
        btnCancel = new javax.swing.JButton();
        jLabel3 = new javax.swing.JLabel();

        jButton1.setText("jButton1");

        jPanel1.setBackground(new java.awt.Color(255, 255, 255));

        lblBrandName.setFont(new java.awt.Font("Century Schoolbook", 0, 48)); // NOI18N
        lblBrandName.setForeground(new java.awt.Color(0, 102, 102));
        lblBrandName.setText("ParkaLot");

        jLabel1.setFont(new java.awt.Font("Century Schoolbook", 0, 18)); // NOI18N
        jLabel1.setForeground(new java.awt.Color(0, 51, 51));
        jLabel1.setText("RESERVATION");

        btnBack.setFont(new java.awt.Font("Century Schoolbook", 0, 18)); // NOI18N
        btnBack.setForeground(new java.awt.Color(0, 51, 51));
        btnBack.setText("Back");
        btnBack.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(0, 51, 51), 3, true));
        btnBack.setFocusPainted(false);
        btnBack.setOpaque(true);
        btnBack.addActionListener(this::btnBackActionPerformed);

        jLabel2.setIcon(new javax.swing.ImageIcon(System.getProperty("user.dir") + "\\src\\main\\java\\assets\\logo_big.png"));

        btnAdd.setBackground(new java.awt.Color(0, 102, 102));
        btnAdd.setFont(new java.awt.Font("Century Schoolbook", 1, 18)); // NOI18N
        btnAdd.setForeground(new java.awt.Color(255, 255, 255));
        btnAdd.setText("Add Reservation");
        btnAdd.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(0, 102, 102), 1, true));
        btnAdd.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnAdd.addActionListener(this::btnAddActionPerformed);

        jTable1.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null}
            },
            new String [] {
                "ID", "Slot", "Plate Number", "Date & Time", "Status"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.Object.class, java.lang.String.class
            };
            boolean[] canEdit = new boolean [] {
                false, false, false, false, false
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        jTable1.getTableHeader().setResizingAllowed(false);
        jTable1.getTableHeader().setReorderingAllowed(false);
        jScrollPane1.setViewportView(jTable1);

        btnCancel.setBackground(new java.awt.Color(204, 0, 0));
        btnCancel.setFont(new java.awt.Font("Century Schoolbook", 1, 18)); // NOI18N
        btnCancel.setForeground(new java.awt.Color(255, 255, 255));
        btnCancel.setText("Cancel Reservation");
        btnCancel.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(0, 102, 102), 1, true));
        btnCancel.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnCancel.addActionListener(this::btnCancelActionPerformed);

        jLabel3.setFont(new java.awt.Font("Century Schoolbook", 0, 12)); // NOI18N
        jLabel3.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel3.setText("Double Click for More Info.");

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(33, 33, 33)
                        .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 178, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, 0)
                        .addComponent(lblBrandName))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(15, 15, 15)
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(26, 26, 26)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(btnBack, javax.swing.GroupLayout.PREFERRED_SIZE, 113, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(btnAdd, javax.swing.GroupLayout.PREFERRED_SIZE, 197, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(btnCancel, javax.swing.GroupLayout.PREFERRED_SIZE, 197, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(13, 13, 13)))))
                .addContainerGap(13, Short.MAX_VALUE))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addGap(0, 0, Short.MAX_VALUE)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                        .addComponent(jLabel1)
                        .addGap(168, 168, 168))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                        .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, 398, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(43, 43, 43))))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(23, 23, 23)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 108, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(33, 33, 33)
                        .addComponent(lblBrandName)))
                .addGap(27, 27, 27)
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 361, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(4, 4, 4)
                .addComponent(jLabel3)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnAdd, javax.swing.GroupLayout.PREFERRED_SIZE, 41, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnCancel, javax.swing.GroupLayout.PREFERRED_SIZE, 41, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(31, 31, 31)
                .addComponent(btnBack)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
    }// </editor-fold>//GEN-END:initComponents

    private void btnAddActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAddActionPerformed
        Dialogue_Reservation reservationObj = new Dialogue_Reservation(parent, this, true);
        reservationObj.setVisible(true);
    }//GEN-LAST:event_btnAddActionPerformed

    private void btnBackActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnBackActionPerformed
        parent.showPage("home");
    }//GEN-LAST:event_btnBackActionPerformed

    private void btnCancelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCancelActionPerformed
        int row = jTable1.getSelectedRow();

        if (row == -1) {
            Dialogue_SystemMessage.showMessage(parent, 
                "Please select a reservation to cancel.");
            return;
        }

        int reservationId = Integer.parseInt(jTable1.getValueAt(row, 0).toString());
        int slotNo = Integer.parseInt(jTable1.getValueAt(row, 1).toString());
        String plate = jTable1.getValueAt(row, 2).toString();
        String status = jTable1.getValueAt(row, 4).toString();

        // Check if cancellable
        SlotController slotController = new SlotController();
        if (!slotController.isCancellable(reservationId)) {
            Dialogue_SystemMessage.showMessage(parent,
                "This reservation cannot be cancelled.\n" +
                "Cancellation window has passed");
            return;
        }

        // Confirm
        int confirm = javax.swing.JOptionPane.showConfirmDialog(
            parent,
            "Cancel reservation for plate " + plate + 
            " at slot " + slotNo + "?",
            "Confirm Cancellation",
            javax.swing.JOptionPane.YES_NO_OPTION
        );

        if (confirm == javax.swing.JOptionPane.YES_OPTION) {
            boolean cancelled = slotController.cancelReservation(
                reservationId, slotNo, plate);

            if (cancelled) {
                Dialogue_SystemMessage.showMessage(parent,
                    "Reservation cancelled successfully.");
                loadReservations();
                parent.loadSlotsFromDatabase();
                parent.updateSlotCounts();
            } else {
                Dialogue_SystemMessage.showMessage(parent,
                    "Failed to cancel reservation.");
            }
        }
    }//GEN-LAST:event_btnCancelActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnAdd;
    private javax.swing.JButton btnBack;
    private javax.swing.JButton btnCancel;
    private javax.swing.JButton jButton1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTable jTable1;
    private javax.swing.JLabel lblBrandName;
    // End of variables declaration//GEN-END:variables
}
