package view.Dialogue;

import controller.AdminUserController;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.sql.SQLException;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import model.SessionManager;

public class Dialogue_DeletedUsers extends javax.swing.JDialog {

    private AdminUserController controller = new AdminUserController();
    private JTable table;
    private DefaultTableModel model;
    private view.Admin.UsersPanel usersPanel;

    public Dialogue_DeletedUsers(Frame parent, boolean modal, 
                                  view.Admin.UsersPanel usersPanel) {
        super(parent, modal);
        this.usersPanel = usersPanel;
        initComponents();
        loadDeletedUsers();
        setTitle("Deleted Users");
        setResizable(false);
        setLocationRelativeTo(parent);
    }

    private void initComponents() {
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setPreferredSize(new Dimension(750, 500));
        getContentPane().setBackground(Color.WHITE);
        setLayout(new BorderLayout(10, 10));

        // Title
        JLabel lblTitle = new JLabel("Deleted Users");
        lblTitle.setFont(new java.awt.Font("Century Schoolbook", 1, 24));
        lblTitle.setForeground(new Color(0, 53, 48));
        lblTitle.setBorder(BorderFactory.createEmptyBorder(15, 20, 5, 0));
        add(lblTitle, BorderLayout.NORTH);

        // Table
        model = new DefaultTableModel(
            new String[]{"Name", "Username", "Email", "Contact", "Position", "Action"}, 0
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 5; // only Action column editable
            }
        };

        table = new JTable(model);
        table.setRowHeight(35);
        table.getTableHeader().setReorderingAllowed(false);
        table.getTableHeader().setResizingAllowed(false);
        table.setSelectionBackground(Color.WHITE);
        table.setFillsViewportHeight(true);

        // Set column widths
        table.getColumnModel().getColumn(0).setPreferredWidth(130);
        table.getColumnModel().getColumn(1).setPreferredWidth(100);
        table.getColumnModel().getColumn(2).setPreferredWidth(160);
        table.getColumnModel().getColumn(3).setPreferredWidth(90);
        table.getColumnModel().getColumn(4).setPreferredWidth(70);
        table.getColumnModel().getColumn(5).setPreferredWidth(90);

        // Recover button renderer and editor
        table.getColumn("Action").setCellRenderer(new RecoverButtonRenderer());
        table.getColumn("Action").setCellEditor(new RecoverButtonEditor());

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createEmptyBorder(0, 15, 0, 15));
        add(scroll, BorderLayout.CENTER);

        // Bottom close button
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottomPanel.setBackground(Color.WHITE);
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 15));

        JButton btnClose = new JButton("Close");
        btnClose.setFont(new java.awt.Font("Century Schoolbook", 1, 16));
        btnClose.setForeground(new Color(0, 51, 51));
        btnClose.setBorder(BorderFactory.createLineBorder(new Color(0, 51, 51), 2));
        btnClose.setPreferredSize(new Dimension(100, 35));
        btnClose.setFocusPainted(false);
        btnClose.addActionListener(e -> dispose());
        bottomPanel.add(btnClose);

        add(bottomPanel, BorderLayout.SOUTH);

        pack();
    }

    public void loadDeletedUsers() {
        model.setRowCount(0);
        DefaultTableModel deleted = controller.getDeletedUsers();
        for (int i = 0; i < deleted.getRowCount(); i++) {
            model.addRow(new Object[]{
                deleted.getValueAt(i, 0), // name
                deleted.getValueAt(i, 1), // username
                deleted.getValueAt(i, 2), // email
                deleted.getValueAt(i, 3), // contact
                deleted.getValueAt(i, 4), // position
                "Recover"
            });
        }

        // Show message if no deleted users
        if (model.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this,
                "No deleted users found.",
                "Info",
                JOptionPane.INFORMATION_MESSAGE);
        }
    }

    // Recover button renderer
    private class RecoverButtonRenderer extends JPanel 
            implements javax.swing.table.TableCellRenderer {
        private JButton btn;

        public RecoverButtonRenderer() {
            setLayout(new FlowLayout(FlowLayout.CENTER, 0, 3));
            setBackground(Color.WHITE);
            btn = new JButton("Recover");
            btn.setFont(new java.awt.Font("Century Schoolbook", 1, 13));
            btn.setBackground(new Color(0, 102, 102));
            btn.setForeground(Color.WHITE);
            btn.setPreferredSize(new Dimension(80, 26));
            btn.setFocusPainted(false);
            add(btn);
        }

        @Override
        public java.awt.Component getTableCellRendererComponent(
                JTable table, Object value, boolean isSelected,
                boolean hasFocus, int row, int column) {
            return this;
        }
    }

    // Recover button editor
    private class RecoverButtonEditor extends DefaultCellEditor {
        private JPanel panel;
        private JButton btn;
        private int currentRow;

        public RecoverButtonEditor() {
            super(new JCheckBox());

            panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 3));
            panel.setBackground(Color.WHITE);

            btn = new JButton("Recover");
            btn.setFont(new java.awt.Font("Century Schoolbook", 1, 13));
            btn.setBackground(new Color(0, 102, 102));
            btn.setForeground(Color.WHITE);
            btn.setPreferredSize(new Dimension(80, 26));
            btn.setFocusPainted(false);
            panel.add(btn);

            btn.addActionListener(e -> {
                fireEditingStopped();

                String username = model.getValueAt(currentRow, 1).toString();

                int confirm = JOptionPane.showConfirmDialog(
                    Dialogue_DeletedUsers.this,
                    "Recover account for " + username + "?",
                    "Confirm Recovery",
                    JOptionPane.YES_NO_OPTION
                );

                if (confirm == JOptionPane.YES_OPTION) {
                    boolean recovered = controller.recoverUser(username);

                    if (recovered) {
                        int userId = controller.getUserIdByUsername(username);
                        controller.logAction(
                            SessionManager.getUsername(),
                            "RECOVER_USER",
                            "users",
                            userId,
                            "deleted",
                            "active"
                        );

                        JOptionPane.showMessageDialog(
                            Dialogue_DeletedUsers.this,
                            username + " has been recovered successfully."
                        );

                        // Refresh deleted users table
                        loadDeletedUsers();

                        // Fix: refresh UsersPanel immediately
                        if (usersPanel != null) {
                            try {
                                usersPanel.loadTable();
                            } catch (java.sql.SQLException ex) {
                                ex.printStackTrace();
                            }
                        }

                    } else {
                        JOptionPane.showMessageDialog(
                            Dialogue_DeletedUsers.this,
                            "Failed to recover " + username
                        );
                    }
                }
            });
        }

        @Override
        public java.awt.Component getTableCellEditorComponent(
                JTable table, Object value, boolean isSelected,
                int row, int column) {
            currentRow = row;
            return panel;
        }

        @Override
        public Object getCellEditorValue() {
            return "Recover";
        }
    }
}