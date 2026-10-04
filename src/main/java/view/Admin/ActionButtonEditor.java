package view.Admin;

import controller.AdminUserController;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.sql.SQLException;
import javax.swing.*;
import javax.swing.table.TableCellEditor;
import view.Dialogue.Dialogue_Edit;

public class ActionButtonEditor extends AbstractCellEditor implements TableCellEditor {

    private JPanel panel;
    private JButton updateButton;
    private JButton deleteButton;
    private JTable table;
    private int row;

    private UsersPanel parentPanel;
    private JFrame parentFrame;

    public ActionButtonEditor(JTable table, UsersPanel parentPanel, JFrame parentFrame) {
        this.table = table;
        this.parentPanel = parentPanel;
        this.parentFrame = parentFrame;

        panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 0));
        updateButton = new JButton("Update");
        deleteButton = new JButton("Delete");

        updateButton.setPreferredSize(new java.awt.Dimension(80, 25));
        deleteButton.setPreferredSize(new java.awt.Dimension(80, 25));

        panel.add(updateButton);
        panel.add(deleteButton);

        updateButton.addActionListener((ActionEvent e) -> {
            fireEditingStopped();

            String username = table.getValueAt(row, 1).toString();

            Dialogue_Edit dialog = new Dialogue_Edit(parentFrame, true, username);
            dialog.setLocationRelativeTo(parentFrame);
            dialog.setVisible(true);
            
            try {
                parentPanel.loadTable();
            } catch (SQLException ex) {
                System.getLogger(UsersPanel.class.getName()).log(System.Logger.Level.ERROR, (String) null, ex);
            }

            try {
                parentPanel.setupActionButtons();
            } catch (SQLException ex) {
                System.getLogger(UsersPanel.class.getName()).log(System.Logger.Level.ERROR, (String) null, ex);
            }
        });
        

        deleteButton.addActionListener((ActionEvent e) -> {
            fireEditingStopped();

            String username = table.getValueAt(row, 1).toString();
            String position = table.getValueAt(row, 4).toString();
            String currentUser = model.SessionManager.getUsername();

            // Check if deleting own account
            boolean isDeletingSelf = username.equals(currentUser);

            // Confirm message differs based on context
            String confirmMessage = isDeletingSelf
                ? "You are about to delete your own account.\nYou will be logged out. Continue?"
                : "Are you sure you want to delete " + username + "?";

            int confirm = JOptionPane.showConfirmDialog(
                parentFrame,
                confirmMessage,
                "Delete User",
                JOptionPane.YES_NO_OPTION
            );

            if (confirm == JOptionPane.YES_OPTION) {
                AdminUserController controller = new AdminUserController();

                // Check if last admin before attempting delete
                if ("admin".equalsIgnoreCase(position)) {
                    int adminCount = controller.getActiveAdminCount();
                    if (adminCount <= 1) {
                        JOptionPane.showMessageDialog(
                            parentFrame,
                            "Cannot delete the last admin account.\nAt least one admin must exist.",
                            "Action Blocked",
                            JOptionPane.WARNING_MESSAGE
                        );
                        return;
                    }
                }

                boolean deleted = controller.softDeleteUser(username);

                if (deleted) {
                    int userId = controller.getUserIdByUsername(username);
                    controller.logAction(
                        model.SessionManager.getUsername(),
                        "DELETE_USER",
                        "users",
                        userId,
                        username,
                        "deleted"
                    );

                    JOptionPane.showMessageDialog(
                        parentFrame,
                        username + " has been deleted."
                    );

                    try {
                        parentPanel.loadTable();
                    } catch (java.sql.SQLException ex) {
                        System.getLogger(ActionButtonEditor.class.getName())
                            .log(System.Logger.Level.ERROR, (String) null, ex);
                    }

                    // If deleted own account — logout and go to login
                    if (isDeletingSelf) {
                        model.SessionManager.logout();
                        parentFrame.dispose();
                        new view.LoginForm().setVisible(true);
                    }

                } else {
                    JOptionPane.showMessageDialog(
                        parentFrame,
                        "Failed to delete " + username + ".",
                        "Error",
                        JOptionPane.ERROR_MESSAGE
                    );
                }
            }
        });
    }

    @Override
    public Component getTableCellEditorComponent(
            JTable table, Object value,
            boolean isSelected, int row, int column) {

        this.row = row;
        panel.remove(deleteButton);

        String status = table.getValueAt(row, 5).toString();
        // Fix: show delete for everyone except already deleted accounts
        if (!status.equalsIgnoreCase("deleted")) {
            panel.add(deleteButton);
        }

        return panel;
    }

    @Override
    public Object getCellEditorValue() {
        return null;
    }
}