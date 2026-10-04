/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package view.Dialogue;

import controller.UserController;
import javax.swing.*;
import java.awt.*;

public class Dialogue_ResetPassword extends javax.swing.JDialog {
    
    private JTextField txtCode;
    private JPasswordField txtNewPassword;
    private JPasswordField txtConfirmPassword;
    private JButton btnResetPassword;
    private JButton btnResendCode;
    private JButton btnCancel;
    private UserController controller;
    private String email;
    
    public Dialogue_ResetPassword(java.awt.Frame parent, boolean modal, String email) {
        super(parent, modal);
        this.email = email;
        controller = new UserController();
        initComponents();
    }
    
    private void initComponents() {
        setTitle("Reset Password");
        setSize(500, 450);
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        getContentPane().setBackground(Color.WHITE);
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 10, 8, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        // Title
        JLabel lblTitle = new JLabel("Reset Password");
        lblTitle.setFont(new Font("Century Schoolbook", Font.BOLD, 24));
        lblTitle.setForeground(new Color(1, 96, 88));
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        add(lblTitle, gbc);
        
        // Email label
        JLabel lblEmail = new JLabel("Email: " + email);
        lblEmail.setFont(new Font("Century Schoolbook", Font.ITALIC, 12));
        lblEmail.setForeground(new Color(51, 51, 51));
        gbc.gridy = 1;
        add(lblEmail, gbc);
        
        gbc.gridwidth = 1;
        
        // Verification Code
        gbc.gridx = 0; gbc.gridy = 2;
        JLabel lblCode = new JLabel("Verification Code:");
        lblCode.setFont(new Font("Century Schoolbook", Font.PLAIN, 16));
        add(lblCode, gbc);
        
        txtCode = new JTextField(20);
        txtCode.setFont(new Font("Century Schoolbook", Font.PLAIN, 16));
        txtCode.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(0, 102, 102), 2),
            BorderFactory.createEmptyBorder(5, 5, 5, 5)
        ));
        gbc.gridx = 1;
        add(txtCode, gbc);
        
        // New Password
        gbc.gridx = 0; gbc.gridy = 3;
        JLabel lblNewPassword = new JLabel("New Password:");
        lblNewPassword.setFont(new Font("Century Schoolbook", Font.PLAIN, 16));
        add(lblNewPassword, gbc);
        
        txtNewPassword = new JPasswordField(20);
        txtNewPassword.setFont(new Font("Century Schoolbook", Font.PLAIN, 16));
        txtNewPassword.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(0, 102, 102), 2),
            BorderFactory.createEmptyBorder(5, 5, 5, 5)
        ));
        gbc.gridx = 1;
        add(txtNewPassword, gbc);
        
        // Confirm Password
        gbc.gridx = 0; gbc.gridy = 4;
        JLabel lblConfirm = new JLabel("Confirm Password:");
        lblConfirm.setFont(new Font("Century Schoolbook", Font.PLAIN, 16));
        add(lblConfirm, gbc);
        
        txtConfirmPassword = new JPasswordField(20);
        txtConfirmPassword.setFont(new Font("Century Schoolbook", Font.PLAIN, 16));
        txtConfirmPassword.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(0, 102, 102), 2),
            BorderFactory.createEmptyBorder(5, 5, 5, 5)
        ));
        gbc.gridx = 1;
        add(txtConfirmPassword, gbc);
        
        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        buttonPanel.setBackground(Color.WHITE);
        
        btnResetPassword = new JButton("Reset Password");
        btnResetPassword.setFont(new Font("Century Schoolbook", Font.BOLD, 16));
        btnResetPassword.setBackground(new Color(0, 102, 102));
        btnResetPassword.setForeground(Color.WHITE);
        btnResetPassword.setBorder(BorderFactory.createLineBorder(new Color(0, 102, 102)));
        btnResetPassword.setPreferredSize(new Dimension(150, 40));
        btnResetPassword.setFocusPainted(false);
        btnResetPassword.addActionListener(e -> resetPassword());
        
        btnResendCode = new JButton("Resend Code");
        btnResendCode.setFont(new Font("Century Schoolbook", Font.PLAIN, 14));
        btnResendCode.setForeground(new Color(0, 51, 51));
        btnResendCode.setBorder(BorderFactory.createLineBorder(new Color(0, 102, 102)));
        btnResendCode.setPreferredSize(new Dimension(130, 40));
        btnResendCode.setFocusPainted(false);
        btnResendCode.addActionListener(e -> resendCode());
        
        btnCancel = new JButton("Cancel");
        btnCancel.setFont(new Font("Century Schoolbook", Font.PLAIN, 14));
        btnCancel.setForeground(new Color(0, 51, 51));
        btnCancel.setBorder(BorderFactory.createLineBorder(new Color(0, 102, 102)));
        btnCancel.setPreferredSize(new Dimension(100, 40));
        btnCancel.setFocusPainted(false);
        btnCancel.addActionListener(e -> dispose());
        
        buttonPanel.add(btnResetPassword);
        buttonPanel.add(btnResendCode);
        buttonPanel.add(btnCancel);
        
        gbc.gridx = 0; gbc.gridy = 5; gbc.gridwidth = 2;
        add(buttonPanel, gbc);
    }
    
    private void resetPassword() {
        String code = txtCode.getText().trim();
        String newPassword = new String(txtNewPassword.getPassword());
        String confirmPassword = new String(txtConfirmPassword.getPassword());
        
        if (code.isEmpty() || newPassword.isEmpty() || confirmPassword.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill all fields!");
            return;
        }
        
        if (!newPassword.equals(confirmPassword)) {
            JOptionPane.showMessageDialog(this, "Passwords do not match!");
            return;
        }
        
        // Verify code
        boolean codeValid = controller.verifyResetCode(email, code);
        
        if (!codeValid) {
            JOptionPane.showMessageDialog(this, 
                "Invalid or expired verification code!\nPlease try again or resend code.",
                "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        // Reset password
        boolean success = controller.resetPassword(email, newPassword);
        
        if (success) {
            JOptionPane.showMessageDialog(this, 
                "Password reset successful!\nYou can now login with your new password.",
                "Success", JOptionPane.INFORMATION_MESSAGE);
            dispose();
        } else {
            JOptionPane.showMessageDialog(this, 
                "Failed to reset password!",
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void resendCode() {
        btnResendCode.setEnabled(false);
        btnResendCode.setText("Sending...");
        
        new Thread(() -> {
            boolean success = controller.sendPasswordResetCode(email);
            
            SwingUtilities.invokeLater(() -> {
                btnResendCode.setEnabled(true);
                btnResendCode.setText("Resend Code");
                
                if (success) {
                    JOptionPane.showMessageDialog(this, "New verification code sent!");
                } else {
                    JOptionPane.showMessageDialog(this, "Failed to resend code!");
                }
            });
        }).start();
    }
}
