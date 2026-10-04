package view.Admin;

import controller.AdminUserController;
import java.awt.Frame;
import java.sql.SQLException;
import javax.swing.JFrame;
import javax.swing.table.DefaultTableModel;
import view.Dialogue.Dialogue_Confirmation;

public class UsersPanel extends javax.swing.JPanel {

    private JFrame parentFrame; // store actual JFrame for dialogs
    private AdminUserController controller;

    public UsersPanel(JFrame parentFrame) throws SQLException { // accept JFrame
        initComponents();
        this.parentFrame = parentFrame;
        this.controller = new AdminUserController();
        Table.setRowHeight(30);
        loadTable();
        adjustColumnWidths();
        setupActionButtons();
    }

    public void loadTable() throws SQLException {
        DefaultTableModel model = controller.getAllUsers();
        Table.setModel(model);
        Table.setDefaultEditor(Object.class, null);
        setupActionButtons();
        adjustColumnWidths();
    }

    void setupActionButtons() throws SQLException {
        Table.getColumn("Actions").setCellRenderer(new ActionButtonRenderer());
        Table.getColumn("Actions").setCellEditor(new ActionButtonEditor(Table, this, parentFrame));
    }
    
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return false;
    }

    private void adjustColumnWidths() {
        if (Table.getColumnModel().getColumnCount() >= 7) {
            Table.getColumnModel().getColumn(0).setPreferredWidth(100);
            Table.getColumnModel().getColumn(2).setPreferredWidth(150);
            Table.getColumnModel().getColumn(3).setPreferredWidth(30);
            Table.getColumnModel().getColumn(4).setPreferredWidth(10);
            Table.getColumnModel().getColumn(5).setPreferredWidth(10);
            Table.getColumnModel().getColumn(6).setPreferredWidth(120);
        }
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
        jLabel9.setText("Users");

        jPanel1.setBackground(new java.awt.Color(249, 249, 249));

        jTextField1.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0), 2));

        jbtnsearch.setBackground(new java.awt.Color(249, 249, 249));
        jbtnsearch.setIcon(new javax.swing.ImageIcon(System.getProperty("user.dir") + "\\src\\main\\java\\assets\\search.png"));
        jbtnsearch.addActionListener(this::jbtnsearchActionPerformed);

        Table.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {"", null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null}
            },
            new String [] {
                "Name", "Username", "Email", "Contact", "Position", "Status", "Actions"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false, false, false, false, false, false
            };

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
                .addGap(10, 10, 10)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 580, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
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
                .addContainerGap(57, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void jbtnsearchActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jbtnsearchActionPerformed
      String keyword = jTextField1.getText().trim();
        if (!keyword.isEmpty()) {
            Table.setModel(controller.searchUsers(keyword));
            try {
                adjustColumnWidths();
                setupActionButtons();
            } catch (SQLException ex) {
                System.getLogger(UsersPanel.class.getName()).log(System.Logger.Level.ERROR, (String) null, ex);
            }
        } else {
            try {
                loadTable();
                adjustColumnWidths();
                setupActionButtons();
            } catch (SQLException ex) {
                System.getLogger(UsersPanel.class.getName()).log(System.Logger.Level.ERROR, (String) null, ex);
            }
        }
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
