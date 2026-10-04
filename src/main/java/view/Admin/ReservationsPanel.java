package view.Admin;

import java.awt.Frame;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;
import model.DBConnection;
import view.Dialogue.Dialogue_Confirmation;

public class ReservationsPanel extends javax.swing.JPanel {

    private JFrame parentFrame;
    private DashboardForm parent;
    private int hoveredRow = -1;

    public ReservationsPanel(DashboardForm parent) {
        initComponents();
        this.parent = parent;
        this.parentFrame = parent;
        Table.setRowHeight(30);
        
        Table.setRowSelectionAllowed(true);
        
        adjustColumnWidths();
        setupHoverAndClick();
        loadTable();
    }

    public void loadTable() {
        DefaultTableModel model = (DefaultTableModel) Table.getModel();
        model.setRowCount(0);

        String sql = """
            SELECT id, slot_number, plate_number, vehicle,
                   contact, reservation_time, status
            FROM reservations
            ORDER BY reservation_time DESC
        """;

        try (java.sql.Connection conn = DBConnection.connect();
             java.sql.PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                model.addRow(new Object[]{
                    rs.getInt("id"),
                    rs.getInt("slot_number"),
                    rs.getString("plate_number"),
                    rs.getString("vehicle"),
                    rs.getString("contact"),
                    rs.getTimestamp("reservation_time"),
                    rs.getString("status")
                });
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void searchRecords(String keyword) {
        DefaultTableModel model = (DefaultTableModel) Table.getModel();
        model.setRowCount(0);

        String sql = """
            SELECT id, slot_number, plate_number, vehicle,
                   contact, reservation_time, status
            FROM reservations
            WHERE plate_number LIKE ?
               OR CAST(slot_number AS CHAR) LIKE ?
               OR vehicle LIKE ?
               OR status LIKE ?
               OR contact LIKE ?
            ORDER BY reservation_time DESC
        """;

        try (java.sql.Connection conn = DBConnection.connect();
             java.sql.PreparedStatement ps = conn.prepareStatement(sql)) {

            String key = "%" + keyword + "%";
            for (int i = 1; i <= 5; i++) ps.setString(i, key);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    model.addRow(new Object[]{
                        rs.getInt("id"),
                        rs.getInt("slot_number"),
                        rs.getString("plate_number"),
                        rs.getString("vehicle"),
                        rs.getString("contact"),
                        rs.getTimestamp("reservation_time"),
                        rs.getString("status")
                    });
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void setupHoverAndClick() {
        Table.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
            @Override
            public void mouseMoved(java.awt.event.MouseEvent e) {
                int row = Table.rowAtPoint(e.getPoint());
                if (row != hoveredRow) {
                    hoveredRow = row;
                    Table.repaint();
                }
            }
        });

        Table.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                hoveredRow = -1;
                Table.repaint();
            }
        });

        Table.setDefaultRenderer(Object.class,
            new javax.swing.table.DefaultTableCellRenderer() {
                @Override
                public java.awt.Component getTableCellRendererComponent(
                        javax.swing.JTable table, Object value,
                        boolean isSelected, boolean hasFocus,
                        int row, int column) {
                    java.awt.Component c = super.getTableCellRendererComponent(
                        table, value, isSelected, hasFocus, row, column);
                    if (row == hoveredRow && !isSelected) {
                        c.setBackground(new java.awt.Color(220, 240, 240));
                        c.setForeground(java.awt.Color.BLACK);
                    } else if (isSelected) {
                        c.setBackground(new java.awt.Color(0, 102, 102));
                        c.setForeground(java.awt.Color.WHITE);
                    } else {
                        c.setBackground(java.awt.Color.WHITE);
                        c.setForeground(java.awt.Color.BLACK);
                    }
                    return c;
                }
            });
    }

    private void adjustColumnWidths() {
        Table.setAutoResizeMode(javax.swing.JTable.AUTO_RESIZE_ALL_COLUMNS);
        Table.getColumnModel().getColumn(0).setPreferredWidth(40);   // ID
        Table.getColumnModel().getColumn(1).setPreferredWidth(50);   // Slot
        Table.getColumnModel().getColumn(2).setPreferredWidth(120);  // Plate No.
        Table.getColumnModel().getColumn(3).setPreferredWidth(80);   // Vehicle
        Table.getColumnModel().getColumn(4).setPreferredWidth(100);  // Contact
        Table.getColumnModel().getColumn(5).setPreferredWidth(150);  // Reservation Time
        Table.getColumnModel().getColumn(6).setPreferredWidth(80);   // Status
    }
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel9 = new javax.swing.JLabel();
        jPanel1 = new javax.swing.JPanel();
        jTextField1 = new javax.swing.JTextField();
        jbtnsearch = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        Table = new javax.swing.JTable();

        setBackground(new java.awt.Color(255, 255, 255));

        jLabel9.setFont(new java.awt.Font("Javanese Text", 1, 22)); // NOI18N
        jLabel9.setForeground(new java.awt.Color(0, 53, 48));
        jLabel9.setText("Reservations");

        jPanel1.setBackground(new java.awt.Color(249, 249, 249));

        jTextField1.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0), 2));

        jbtnsearch.setBackground(new java.awt.Color(249, 249, 249));
        jbtnsearch.setIcon(new javax.swing.ImageIcon(System.getProperty("user.dir") + "\\src\\main\\java\\assets\\search.png"));
        jbtnsearch.addActionListener(this::jbtnsearchActionPerformed);

        Table.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null}
            },
            new String [] {
                "ID", "Slot", "Plate No.", "Vehicle", "Contact", "Reservation Time", "Status"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.Integer.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.Object.class, java.lang.Object.class
            };
            boolean[] canEdit = new boolean [] {
                false, false, false, false, false, false, false
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        Table.setRowSelectionAllowed(false);
        Table.getTableHeader().setResizingAllowed(false);
        Table.getTableHeader().setReorderingAllowed(false);
        jScrollPane1.setViewportView(Table);

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addGap(20, 20, 20)
                .addComponent(jbtnsearch, javax.swing.GroupLayout.PREFERRED_SIZE, 42, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, 351, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(28, 496, Short.MAX_VALUE))
            .addComponent(jScrollPane1, javax.swing.GroupLayout.Alignment.TRAILING)
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(3, 3, 3)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jbtnsearch, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jTextField1))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 10, Short.MAX_VALUE)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 577, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(14, 14, 14))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(99, 99, 99)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel9)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(6, 6, 6)
                        .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(59, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(39, 39, 39)
                .addComponent(jLabel9)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(52, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void jbtnsearchActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jbtnsearchActionPerformed
        String keyword = jTextField1.getText().trim();
        if (!keyword.isEmpty()) {
            searchRecords(keyword);
        } else {
            loadTable();
        }
        adjustColumnWidths();
    }//GEN-LAST:event_jbtnsearchActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTable Table;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTextField jTextField1;
    private javax.swing.JButton jbtnsearch;
    // End of variables declaration//GEN-END:variables
}
