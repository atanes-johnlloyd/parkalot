/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JDialog.java to edit this template
 */
package view.Admin;
import controller.ParkController;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.swing.JOptionPane;
/**
 *
 * @author Alumbres
 */
public class park extends javax.swing.JDialog {
    private ParkController parkController;
    private javax.swing.JLabel[] slotLabels;
    private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(park.class.getName());
    private java.util.Map<Integer, String> pendingStatusChanges = new java.util.HashMap<>();

    /**
     * Creates new form park
     */
    public park(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
        initComponents();
        
         parkController = new ParkController();
        
        slotLabels = new javax.swing.JLabel[] {
            lblSlot1, lblSlot2, lblSlot3, lblSlot4, lblSlot5,
            lblSlot6, lblSlot7, lblSlot8, lblSlot9, lblSlot10,
            lblSlot11, lblSlot12, lblSlot13, lblSlot14, lblSlot15,
            lblSlot16, lblSlot17, lblSlot18, lblSlot19, lblSlot20,
            lblSlot21, lblSlot22, lblSlot23, lblSlot24, lblSlot25,
            lblSlot26, lblSlot27, lblSlot28, lblSlot29, lblSlot30
        };
        
        loadSlotConfigurations();
        setupSlotClickListeners();
        addLegend();
        
        setLocationRelativeTo(parent);
    }
    
    private void addLegend() {
        getContentPane().setLayout(new java.awt.BorderLayout());
        getContentPane().add(jPanel2, java.awt.BorderLayout.CENTER);

        javax.swing.JPanel legendPanel = new javax.swing.JPanel(
            new java.awt.FlowLayout(java.awt.FlowLayout.CENTER, 15, 5));
        legendPanel.setBackground(new java.awt.Color(0, 51, 51));

        String[][] legends = {
            {"Available", "100,255,100"},
            {"Occupied", "255,100,100"},
            {"Reserved", "255,200,100"},
            {"Unavailable", "150,150,150"}
        };

        for (String[] legend : legends) {
            javax.swing.JPanel colorBox = new javax.swing.JPanel();
            String[] rgb = legend[1].split(",");
            colorBox.setBackground(new java.awt.Color(
                Integer.parseInt(rgb[0].trim()),
                Integer.parseInt(rgb[1].trim()),
                Integer.parseInt(rgb[2].trim())
            ));
            colorBox.setPreferredSize(new java.awt.Dimension(20, 20));
            colorBox.setBorder(javax.swing.BorderFactory.createLineBorder(
                java.awt.Color.WHITE, 1));

            javax.swing.JLabel legendLabel = new javax.swing.JLabel(legend[0]);
            legendLabel.setForeground(java.awt.Color.WHITE);
            legendLabel.setFont(new java.awt.Font("Javanese Text", 1, 12));

            legendPanel.add(colorBox);
            legendPanel.add(legendLabel);
        }
        jPanel2.add(legendPanel, 
            new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 630, 830, 35));
        jPanel2.revalidate();
        jPanel2.repaint();
    }
    
    private void loadSlotConfigurations() {
        try {
            ResultSet rs = parkController.getAllSlots();
            while (rs.next()) {
                int slotNum = rs.getInt("slot_number");
                String status = rs.getString("status");
                String vehicle = rs.getString("vehicle");

                if (slotNum >= 1 && slotNum <= 30) {
                    javax.swing.JLabel label = slotLabels[slotNum - 1];

                    if ("occupied".equals(status)) {
                        label.setBackground(new java.awt.Color(255, 100, 100)); // Red
                        label.setToolTipText("Slot " + slotNum + " - Occupied (" + vehicle + ")");
                    } else if ("reserved".equals(status)) {
                        label.setBackground(new java.awt.Color(255, 200, 100)); // Orange
                        label.setToolTipText("Slot " + slotNum + " - Reserved (" + vehicle + ")");
                    } else if ("unavailable".equals(status)) {
                        // Fix: add unavailable handling
                        label.setBackground(new java.awt.Color(150, 150, 150)); // Gray
                        label.setToolTipText("Slot " + slotNum + " - Unavailable (" + vehicle + ")");
                    } else {
                        label.setBackground(new java.awt.Color(100, 255, 100)); // Green
                        label.setToolTipText("Slot " + slotNum + " - Available (" + vehicle + ")");
                    }
                    label.setOpaque(true);
                }
            }
            rs.close();
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error loading slots: " + e.getMessage());
        }
    }
    
    private void setupSlotClickListeners() {
        for (int i = 0; i < slotLabels.length; i++) {
            final int slotNum = i + 1;
            slotLabels[i].addMouseListener(new java.awt.event.MouseAdapter() {
                public void mouseClicked(java.awt.event.MouseEvent evt) {
                    configureSlot(slotNum);
                }
            });
        }
    }
    
    private void configureSlot(int slotNumber) {
        try {
            String currentStatus = parkController.getSlotStatus(slotNumber);
            
            // Check pending changes first
            if (pendingStatusChanges.containsKey(slotNumber)) {
                currentStatus = pendingStatusChanges.get(slotNumber);
            }
            
            String currentVehicle = parkController.getSlotVehicle(slotNumber);

            // Block if slot is busy
            if (currentStatus.equals("occupied") || currentStatus.equals("reserved")) {
                JOptionPane.showMessageDialog(this,
                    "Cannot configure slot " + slotNumber +
                    " while it is " + currentStatus + ".\n" +
                    "Please process the vehicle first.",
                    "Slot Busy",
                    JOptionPane.WARNING_MESSAGE);
                return;
            }

            String[] statusOptions = {"Available", "Unavailable", "Cancel"};

            int statusChoice = JOptionPane.showOptionDialog(this,
                "Slot " + slotNumber +
                "\nCurrent Status: " + currentStatus +
                "\nVehicle Type: " + currentVehicle +
                "\n\nSelect new status:",
                "Configure Slot " + slotNumber,
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                statusOptions,
                statusOptions[0]);

            if (statusChoice == 2 || statusChoice == JOptionPane.CLOSED_OPTION) return;

            String newStatus = statusOptions[statusChoice].toLowerCase();

            // Fix: store in memory instead of writing to DB immediately
            pendingStatusChanges.put(slotNumber, newStatus);

            // Update visual only
            updateSlotVisual(slotNumber, newStatus, currentVehicle);

            JOptionPane.showMessageDialog(this,
                "Slot " + slotNumber + " marked as " + newStatus + 
                ".\nClick Save to apply changes.");

        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this,
                "Error configuring slot: " + e.getMessage());
        }
    }

    // Fix: update visual without touching DB
    private void updateSlotVisual(int slotNumber, String status, String vehicle) {
        javax.swing.JLabel label = slotLabels[slotNumber - 1];

        if ("occupied".equals(status)) {
            label.setBackground(new java.awt.Color(255, 100, 100));
            label.setToolTipText("Slot " + slotNumber + " - Occupied (" + vehicle + ")");
        } else if ("reserved".equals(status)) {
            label.setBackground(new java.awt.Color(255, 200, 100));
            label.setToolTipText("Slot " + slotNumber + " - Reserved (" + vehicle + ")");
        } else if ("unavailable".equals(status)) {
            label.setBackground(new java.awt.Color(150, 150, 150));
            label.setToolTipText("Slot " + slotNumber + " - Unavailable (" + vehicle + ")");
        } else {
            label.setBackground(new java.awt.Color(100, 255, 100));
            label.setToolTipText("Slot " + slotNumber + " - Available (" + vehicle + ")");
        }
        label.setOpaque(true);
        label.repaint();
    }

    private void btnCloseActionPerformed(java.awt.event.ActionEvent evt) {
        dispose();
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel2 = new javax.swing.JPanel();
        btnsave = new javax.swing.JButton();
        btnexit = new javax.swing.JButton();
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

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        jPanel2.setBackground(new java.awt.Color(0, 51, 51));
        jPanel2.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        btnsave.setBackground(new java.awt.Color(0, 51, 51));
        btnsave.setFont(new java.awt.Font("Javanese Text", 1, 16)); // NOI18N
        btnsave.setForeground(new java.awt.Color(255, 255, 255));
        btnsave.setText("Save");
        btnsave.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(255, 255, 255), 3, true));
        btnsave.addActionListener(this::btnsaveActionPerformed);
        jPanel2.add(btnsave, new org.netbeans.lib.awtextra.AbsoluteConstraints(200, 600, 120, 30));

        btnexit.setBackground(new java.awt.Color(0, 51, 51));
        btnexit.setFont(new java.awt.Font("Javanese Text", 1, 16)); // NOI18N
        btnexit.setForeground(new java.awt.Color(255, 255, 255));
        btnexit.setText("Exit");
        btnexit.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(255, 255, 255), 3, true));
        btnexit.addActionListener(this::btnexitActionPerformed);
        jPanel2.add(btnexit, new org.netbeans.lib.awtextra.AbsoluteConstraints(50, 600, 120, 30));

        lblLines.setIcon(new javax.swing.ImageIcon(System.getProperty("user.dir") + "\\src\\main\\java\\assets\\lines.png"));
        jPanel2.add(lblLines, new org.netbeans.lib.awtextra.AbsoluteConstraints(50, 20, 722, 674));

        jLayeredPane2.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        lblSlot22.setIcon(new javax.swing.ImageIcon(System.getProperty("user.dir") + "\\src\\main\\java\\assets\\available_slot.png"));
        jLayeredPane2.add(lblSlot22, new org.netbeans.lib.awtextra.AbsoluteConstraints(527, 200, -1, -1));

        lblSlot2.setIcon(new javax.swing.ImageIcon(System.getProperty("user.dir") + "\\src\\main\\java\\assets\\available_slot.png"));
        jLayeredPane2.add(lblSlot2, new org.netbeans.lib.awtextra.AbsoluteConstraints(325, 460, -1, -1));

        lblSlot3.setIcon(new javax.swing.ImageIcon(System.getProperty("user.dir") + "\\src\\main\\java\\assets\\available_slot.png"));
        jLayeredPane2.add(lblSlot3, new org.netbeans.lib.awtextra.AbsoluteConstraints(247, 460, -1, -1));

        lblSlot4.setIcon(new javax.swing.ImageIcon(System.getProperty("user.dir") + "\\src\\main\\java\\assets\\available_slot.png"));
        jLayeredPane2.add(lblSlot4, new org.netbeans.lib.awtextra.AbsoluteConstraints(171, 460, -1, -1));

        lblSlot9.setIcon(new javax.swing.ImageIcon(System.getProperty("user.dir") + "\\src\\main\\java\\assets\\available_slot_side.png"));
        jLayeredPane2.add(lblSlot9, new org.netbeans.lib.awtextra.AbsoluteConstraints(60, 85, -1, -1));

        lblSlot5.setIcon(new javax.swing.ImageIcon(System.getProperty("user.dir") + "\\src\\main\\java\\assets\\available_slot_side.png"));
        jLayeredPane2.add(lblSlot5, new org.netbeans.lib.awtextra.AbsoluteConstraints(60, 358, -1, -1));

        lblSlot6.setIcon(new javax.swing.ImageIcon(System.getProperty("user.dir") + "\\src\\main\\java\\assets\\available_slot_side.png"));
        jLayeredPane2.add(lblSlot6, new org.netbeans.lib.awtextra.AbsoluteConstraints(60, 291, -1, -1));

        lblSlot7.setIcon(new javax.swing.ImageIcon(System.getProperty("user.dir") + "\\src\\main\\java\\assets\\available_slot_side.png"));
        jLayeredPane2.add(lblSlot7, new org.netbeans.lib.awtextra.AbsoluteConstraints(60, 223, -1, -1));

        lblSlot8.setIcon(new javax.swing.ImageIcon(System.getProperty("user.dir") + "\\src\\main\\java\\assets\\available_slot_side.png"));
        jLayeredPane2.add(lblSlot8, new org.netbeans.lib.awtextra.AbsoluteConstraints(60, 155, -1, -1));

        lblSlot18.setIcon(new javax.swing.ImageIcon(System.getProperty("user.dir") + "\\src\\main\\java\\assets\\available_slot_up.png"));
        jLayeredPane2.add(lblSlot18, new org.netbeans.lib.awtextra.AbsoluteConstraints(289, 290, -1, 100));

        lblSlot10.setIcon(new javax.swing.ImageIcon(System.getProperty("user.dir") + "\\src\\main\\java\\assets\\available_slot_up.png"));
        jLayeredPane2.add(lblSlot10, new org.netbeans.lib.awtextra.AbsoluteConstraints(269, 30, -1, 100));

        lblSlot11.setIcon(new javax.swing.ImageIcon(System.getProperty("user.dir") + "\\src\\main\\java\\assets\\available_slot_up.png"));
        jLayeredPane2.add(lblSlot11, new org.netbeans.lib.awtextra.AbsoluteConstraints(348, 30, -1, 100));

        lblSlot12.setIcon(new javax.swing.ImageIcon(System.getProperty("user.dir") + "\\src\\main\\java\\assets\\available_slot_up.png"));
        jLayeredPane2.add(lblSlot12, new org.netbeans.lib.awtextra.AbsoluteConstraints(427, 30, -1, 100));

        lblSlot13.setIcon(new javax.swing.ImageIcon(System.getProperty("user.dir") + "\\src\\main\\java\\assets\\available_slot_up.png"));
        jLayeredPane2.add(lblSlot13, new org.netbeans.lib.awtextra.AbsoluteConstraints(505, 30, -1, 100));

        lblSlot14.setIcon(new javax.swing.ImageIcon(System.getProperty("user.dir") + "\\src\\main\\java\\assets\\available_slot_up.png"));
        jLayeredPane2.add(lblSlot14, new org.netbeans.lib.awtextra.AbsoluteConstraints(583, 30, -1, 100));

        lblSlot15.setIcon(new javax.swing.ImageIcon(System.getProperty("user.dir") + "\\src\\main\\java\\assets\\available_slot_up.png"));
        jLayeredPane2.add(lblSlot15, new org.netbeans.lib.awtextra.AbsoluteConstraints(526, 290, -1, 100));

        lblSlot17.setIcon(new javax.swing.ImageIcon(System.getProperty("user.dir") + "\\src\\main\\java\\assets\\available_slot_up.png"));
        jLayeredPane2.add(lblSlot17, new org.netbeans.lib.awtextra.AbsoluteConstraints(368, 290, -1, 100));

        lblSlot23.setIcon(new javax.swing.ImageIcon(System.getProperty("user.dir") + "\\src\\main\\java\\assets\\motor_slot.png"));
        jLayeredPane2.add(lblSlot23, new org.netbeans.lib.awtextra.AbsoluteConstraints(670, 160, -1, -1));

        lblSlot29.setIcon(new javax.swing.ImageIcon(System.getProperty("user.dir") + "\\src\\main\\java\\assets\\motor_slot.png"));
        jLayeredPane2.add(lblSlot29, new org.netbeans.lib.awtextra.AbsoluteConstraints(668, 435, -1, -1));

        lblSlot30.setIcon(new javax.swing.ImageIcon(System.getProperty("user.dir") + "\\src\\main\\java\\assets\\motor_slot.png"));
        jLayeredPane2.add(lblSlot30, new org.netbeans.lib.awtextra.AbsoluteConstraints(668, 480, -1, -1));

        lblSlot28.setIcon(new javax.swing.ImageIcon(System.getProperty("user.dir") + "\\src\\main\\java\\assets\\motor_slot.png"));
        jLayeredPane2.add(lblSlot28, new org.netbeans.lib.awtextra.AbsoluteConstraints(668, 388, -1, -1));

        lblSlot27.setIcon(new javax.swing.ImageIcon(System.getProperty("user.dir") + "\\src\\main\\java\\assets\\motor_slot.png"));
        jLayeredPane2.add(lblSlot27, new org.netbeans.lib.awtextra.AbsoluteConstraints(668, 341, -1, -1));

        lblSlot26.setIcon(new javax.swing.ImageIcon(System.getProperty("user.dir") + "\\src\\main\\java\\assets\\motor_slot.png"));
        jLayeredPane2.add(lblSlot26, new org.netbeans.lib.awtextra.AbsoluteConstraints(668, 297, -1, -1));

        lblSlot25.setIcon(new javax.swing.ImageIcon(System.getProperty("user.dir") + "\\src\\main\\java\\assets\\motor_slot.png"));
        jLayeredPane2.add(lblSlot25, new org.netbeans.lib.awtextra.AbsoluteConstraints(668, 252, -1, -1));

        lblSlot24.setIcon(new javax.swing.ImageIcon(System.getProperty("user.dir") + "\\src\\main\\java\\assets\\motor_slot.png"));
        jLayeredPane2.add(lblSlot24, new org.netbeans.lib.awtextra.AbsoluteConstraints(668, 205, -1, -1));

        lblSlot1.setIcon(new javax.swing.ImageIcon(System.getProperty("user.dir") + "\\src\\main\\java\\assets\\available_slot_pwd.png"));
        lblSlot1.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                lblSlot1MouseClicked(evt);
            }
        });
        jLayeredPane2.add(lblSlot1, new org.netbeans.lib.awtextra.AbsoluteConstraints(407, 460, -1, -1));

        lblSlot19.setIcon(new javax.swing.ImageIcon(System.getProperty("user.dir") + "\\src\\main\\java\\assets\\available_slot.png"));
        jLayeredPane2.add(lblSlot19, new org.netbeans.lib.awtextra.AbsoluteConstraints(288, 200, -1, -1));

        lblSlot20.setIcon(new javax.swing.ImageIcon(System.getProperty("user.dir") + "\\src\\main\\java\\assets\\available_slot.png"));
        jLayeredPane2.add(lblSlot20, new org.netbeans.lib.awtextra.AbsoluteConstraints(369, 200, -1, -1));

        lblSlot21.setIcon(new javax.swing.ImageIcon(System.getProperty("user.dir") + "\\src\\main\\java\\assets\\available_slot.png"));
        jLayeredPane2.add(lblSlot21, new org.netbeans.lib.awtextra.AbsoluteConstraints(448, 200, -1, -1));

        lblSlot16.setIcon(new javax.swing.ImageIcon(System.getProperty("user.dir") + "\\src\\main\\java\\assets\\available_slot_up.png"));
        jLayeredPane2.add(lblSlot16, new org.netbeans.lib.awtextra.AbsoluteConstraints(447, 290, -1, 100));

        jPanel2.add(jLayeredPane2, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 810, 686));

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void lblSlot1MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_lblSlot1MouseClicked

    }//GEN-LAST:event_lblSlot1MouseClicked

    private void btnsaveActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnsaveActionPerformed
        if (pendingStatusChanges.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No changes to save.");
            dispose();
            return;
        }

        try {
            for (java.util.Map.Entry<Integer, String> entry : pendingStatusChanges.entrySet()) {
                parkController.updateSlotStatus(entry.getKey(), entry.getValue());
            }

            JOptionPane.showMessageDialog(this, 
                "Changes saved successfully!");
            pendingStatusChanges.clear();
            dispose();

        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, 
                "Error saving changes: " + e.getMessage());
        }

    }//GEN-LAST:event_btnsaveActionPerformed

    private void btnexitActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnexitActionPerformed
        if (!pendingStatusChanges.isEmpty()) {
            int confirm = JOptionPane.showConfirmDialog(this,
                "You have unsaved changes. Exit without saving?",
                "Confirm Exit",
                JOptionPane.YES_NO_OPTION);

            if (confirm == JOptionPane.YES_OPTION) {
                // Discard — reload original from DB
                pendingStatusChanges.clear();
                loadSlotConfigurations();
                dispose();
            }
            // If NO — stay in dialog
        } else {
            dispose();
        }
    }//GEN-LAST:event_btnexitActionPerformed

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

        /* Create and display the dialog */
        java.awt.EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                park dialog = new park(new javax.swing.JFrame(), true);
                dialog.addWindowListener(new java.awt.event.WindowAdapter() {
                    @Override
                    public void windowClosing(java.awt.event.WindowEvent e) {
                        System.exit(0);
                    }
                });
                dialog.setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnexit;
    private javax.swing.JButton btnsave;
    private javax.swing.JLayeredPane jLayeredPane2;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JLabel lblLines;
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
    // End of variables declaration//GEN-END:variables
}
