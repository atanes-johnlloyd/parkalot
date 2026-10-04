package view;

import controller.SlotController;
import java.awt.Color;
import java.awt.Dimension;
import java.sql.ResultSet;
import javax.swing.table.DefaultTableModel;

public final class RecordsPanel extends javax.swing.JPanel {
    private final HomeForm parent;
    private int hoveredRow = -1;

    public RecordsPanel(HomeForm parent) {
        this.parent = parent;
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
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                
                if (evt.getClickCount() == 2) {
                    int row = jTable1.getSelectedRow();
                    if (row != -1) showRowDetails(row);
                }
            }
        });
        
        jTable1.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                int row = jTable1.getSelectedRow();
                if (row != -1) showRowDetails(row);
            }
        });
        
        jTable1.setAutoResizeMode(javax.swing.JTable.AUTO_RESIZE_OFF);
        jTable1.getColumnModel().getColumn(0).setPreferredWidth(50);
        jTable1.getColumnModel().getColumn(1).setPreferredWidth(100);
        jTable1.getColumnModel().getColumn(2).setPreferredWidth(70);
        jTable1.getColumnModel().getColumn(3).setPreferredWidth(145);
        
        jTable1.setDefaultEditor(Object.class, null);
        
        jTable1.setSelectionBackground(Color.WHITE);
        jTable1.setSelectionForeground(Color.BLACK);
        
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
        
        loadRecords();
        repaint();
    }
    
    private void showRowDetails(int row) {
        String slot     = jTable1.getValueAt(row, 0).toString();
        String plate    = jTable1.getValueAt(row, 1).toString();
        String action   = jTable1.getValueAt(row, 2).toString();
        String dateTime = jTable1.getValueAt(row, 3).toString();

        // Fetch additional info from DB
        String vehicle = "";
        String staff = "";

        String sql = """
            SELECT vehicle, staff_username 
            FROM parking_logs 
            WHERE slot_number = ? 
            AND plate_number = ? 
            AND action_type = ?
            AND action_time = ?
            LIMIT 1
        """;

        try (java.sql.Connection conn = model.DBConnection.connect();
             java.sql.PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, Integer.parseInt(slot));
            ps.setString(2, plate);
            ps.setString(3, action);
            ps.setString(4, dateTime);
            try (java.sql.ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    vehicle = rs.getString("vehicle") != null 
                        ? rs.getString("vehicle") : "-";
                    staff = rs.getString("staff_username") != null 
                        ? rs.getString("staff_username") : "system";
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        String details = String.format(
            "Slot Number  : %s\n" +
            "Plate Number : %s\n" +
            "Vehicle      : %s\n" +
            "Action       : %s\n" +
            "Staff        : %s\n" +
            "Date & Time  : %s",
            slot, plate, vehicle, action, staff, dateTime
        );

        javax.swing.JOptionPane.showMessageDialog(
            this,
            details,
            "Record Details — Slot " + slot,
            javax.swing.JOptionPane.INFORMATION_MESSAGE
        );
    }
    
    public void loadRecords(){

        DefaultTableModel model = (DefaultTableModel) jTable1.getModel();
        model.setRowCount(0);

        try{
            SlotController slotControl = new SlotController();
            ResultSet rs = slotControl.getRecords();
            
            while(rs.next()){

                model.addRow(new Object[]{
                    rs.getInt("slot_number"),
                    rs.getString("plate_number"),
                    rs.getString("action_type"),
                    rs.getTimestamp("action_time")
                });

            }

        }catch(Exception e){
            e.printStackTrace();
        }

    }
    
    
    public void refresh() {
        loadRecords();
    }
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jButton1 = new javax.swing.JButton();
        jPanel1 = new javax.swing.JPanel();
        lblBrandName = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTable1 = new javax.swing.JTable();
        jLabel1 = new javax.swing.JLabel();
        btnBack = new javax.swing.JButton();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();

        jButton1.setText("jButton1");

        jPanel1.setBackground(new java.awt.Color(255, 255, 255));

        lblBrandName.setFont(new java.awt.Font("Century Schoolbook", 0, 48)); // NOI18N
        lblBrandName.setForeground(new java.awt.Color(0, 102, 102));
        lblBrandName.setText("ParkaLot");

        jScrollPane1.setHorizontalScrollBar(null);

        jTable1.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Slot", "Plate Number", "Action", "Date & Time"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.Object.class
            };
            boolean[] canEdit = new boolean [] {
                false, false, false, false
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

        jLabel1.setFont(new java.awt.Font("Century Schoolbook", 0, 18)); // NOI18N
        jLabel1.setForeground(new java.awt.Color(0, 51, 51));
        jLabel1.setText("RECORDS");

        btnBack.setBackground(new java.awt.Color(244, 255, 255));
        btnBack.setFont(new java.awt.Font("Century Schoolbook", 0, 18)); // NOI18N
        btnBack.setForeground(new java.awt.Color(0, 51, 51));
        btnBack.setText("Back");
        btnBack.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(0, 51, 51), 3, true));
        btnBack.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnBack.setFocusPainted(false);
        btnBack.setOpaque(true);
        btnBack.addActionListener(this::btnBackActionPerformed);

        jLabel2.setIcon(new javax.swing.ImageIcon(System.getProperty("user.dir") + "\\src\\main\\java\\assets\\logo_big.png"));

        jLabel3.setFont(new java.awt.Font("Century Schoolbook", 0, 12)); // NOI18N
        jLabel3.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel3.setText("Double Click for More Info.");

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(49, 49, 49)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 371, javax.swing.GroupLayout.PREFERRED_SIZE)))
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(33, 33, 33)
                        .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 173, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(5, 5, 5)
                        .addComponent(lblBrandName))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(188, 188, 188)
                        .addComponent(jLabel1))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(26, 26, 26)
                        .addComponent(btnBack, javax.swing.GroupLayout.PREFERRED_SIZE, 113, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(0, 0, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(23, 23, 23)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 110, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(33, 33, 33)
                        .addComponent(lblBrandName)))
                .addGap(25, 25, 25)
                .addComponent(jLabel1)
                .addGap(12, 12, 12)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 417, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel3)
                .addGap(27, 27, 27)
                .addComponent(btnBack))
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

    private void btnBackActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnBackActionPerformed
        parent.showPage("home");
    }//GEN-LAST:event_btnBackActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnBack;
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
