/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package view.Dialogue;


import controller.UserController;
import javax.swing.*;
import java.awt.*;

public class Dialogue_Email extends javax.swing.JDialog {
    
    private JTextField txtEmail;
    private JButton btnSendCode;
    private JButton btnCancel;
    private UserController controller;
    
    public Dialogue_Email(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
        controller = new UserController();
        initComponents();
    }
    
    private void initComponents() {
        setTitle("Forgot Password");
        setSize(450, 250);
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        getContentPane().setBackground(Color.WHITE);
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        // Title
        JLabel lblTitle = new JLabel("Forgot Password");
        lblTitle.setFont(new Font("Century Schoolbook", Font.BOLD, 24));
        lblTitle.setForeground(new Color(1, 96, 88));
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        add(lblTitle, gbc);
        
        // Info
        JLabel lblInfo = new JLabel("Enter your email to receive verification code");
        lblInfo.setFont(new Font("Century Schoolbook", Font.PLAIN, 14));
        lblInfo.setForeground(new Color(51, 51, 51));
        gbc.gridy = 1;
        add(lblInfo, gbc);
        
        gbc.gridwidth = 1;
        
        // Email
        gbc.gridx = 0; gbc.gridy = 2;
        JLabel lblEmail = new JLabel("Email:");
        lblEmail.setFont(new Font("Century Schoolbook", Font.PLAIN, 16));
        add(lblEmail, gbc);
        
        txtEmail = new JTextField(20);
        txtEmail.setFont(new Font("Century Schoolbook", Font.PLAIN, 16));
        txtEmail.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(0, 102, 102), 2),
            BorderFactory.createEmptyBorder(5, 5, 5, 5)
        ));
        gbc.gridx = 1;
        add(txtEmail, gbc);
        
        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        buttonPanel.setBackground(Color.WHITE);
        
        btnSendCode = new JButton("Send Code");
        btnSendCode.setFont(new Font("Century Schoolbook", Font.BOLD, 16));
        btnSendCode.setBackground(new Color(0, 102, 102));
        btnSendCode.setForeground(Color.WHITE);
        btnSendCode.setBorder(BorderFactory.createLineBorder(new Color(0, 102, 102)));
        btnSendCode.setPreferredSize(new Dimension(130, 40));
        btnSendCode.setFocusPainted(false);
        btnSendCode.addActionListener(e -> sendVerificationCode());
        
        btnCancel = new JButton("Cancel");
        btnCancel.setFont(new Font("Century Schoolbook", Font.PLAIN, 16));
        btnCancel.setForeground(new Color(0, 51, 51));
        btnCancel.setBorder(BorderFactory.createLineBorder(new Color(0, 102, 102)));
        btnCancel.setPreferredSize(new Dimension(130, 40));
        btnCancel.setFocusPainted(false);
        btnCancel.addActionListener(e -> dispose());
        
        buttonPanel.add(btnSendCode);
        buttonPanel.add(btnCancel);
        
        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 2;
        add(buttonPanel, gbc);
    }
    
    private void sendVerificationCode() {
        String email = txtEmail.getText().trim();
        
        if (email.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter your email!");
            return;
        }
        
        btnSendCode.setEnabled(false);
        btnSendCode.setText("Sending...");
        
        new Thread(() -> {
            boolean success = controller.sendPasswordResetCode(email);
            
            SwingUtilities.invokeLater(() -> {
                btnSendCode.setEnabled(true);
                btnSendCode.setText("Send Code");
                
                if (success) {
                    JOptionPane.showMessageDialog(this, 
                        "Verification code sent to your email!\nPlease check your inbox.",
                        "Success", JOptionPane.INFORMATION_MESSAGE);
                    
                    this.dispose();
                    Dialogue_ResetPassword resetDialog = new Dialogue_ResetPassword(
                        (java.awt.Frame) getParent(), true, email);
                    resetDialog.setVisible(true);
                } else {
                    JOptionPane.showMessageDialog(this, 
                        "Email not found or failed to send!\nPlease try again.",
                        "Error", JOptionPane.ERROR_MESSAGE);
                }
            });
        }).start();
    }
}
