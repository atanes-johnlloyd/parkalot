package view;

import controller.UserController;
import java.awt.Dimension;
import java.sql.SQLException;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import view.Dialogue.Dialogue_SystemMessage;

public class RegisterForm extends javax.swing.JFrame {
    
    private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(RegisterForm.class.getName());

    public RegisterForm() {
        initComponents();
        
        this.setResizable(false);
        this.setLocationRelativeTo(null);
        
        addPlaceholder(txtFullName, "e.g. Juan Dela Cruz");
        addPlaceholder(txtUsername, "e.g. juan123");
        addPlaceholder(txtEmail, "e.g. juan@email.com");
        addPlaceholder(txtContact, "e.g. 09123456789");
        addPasswordPlaceholder(txtPassword, "e.g. Password123");
        addPasswordPlaceholder(txtConfirmPassword, "e.g. Password123");
        
        txtPassword.setMinimumSize(new Dimension(526, 55));
        txtPassword.setMaximumSize(new Dimension(Integer.MAX_VALUE, 55));
        txtPassword.setPreferredSize(new Dimension(526, 55));

        txtConfirmPassword.setMinimumSize(new Dimension(526, 55));
        txtConfirmPassword.setMaximumSize(new Dimension(Integer.MAX_VALUE, 55));
        txtConfirmPassword.setPreferredSize(new Dimension(526, 55));

        jPanel2.revalidate();
        jPanel2.repaint();
   
    }
    
    private void addPlaceholder(javax.swing.JTextField field, String placeholder) {
        field.setForeground(java.awt.Color.GRAY);
        field.setText(placeholder);

        field.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override
            public void focusGained(java.awt.event.FocusEvent e) {
                if (field.getText().equals(placeholder)) {
                    field.setText("");
                    field.setForeground(java.awt.Color.BLACK);
                }
            }

            @Override
            public void focusLost(java.awt.event.FocusEvent e) {
                if (field.getText().isEmpty()) {
                    field.setForeground(java.awt.Color.GRAY);
                    field.setText(placeholder);
                }
            }
        });
    }
    
    private void addPasswordPlaceholder(javax.swing.JPasswordField field, String placeholder) {
        field.setForeground(java.awt.Color.GRAY);
        field.setText(placeholder);
        field.setEchoChar((char) 0); // show placeholder as plain text

        field.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override
            public void focusGained(java.awt.event.FocusEvent e) {
                if (String.valueOf(field.getPassword()).equals(placeholder)) {
                    field.setText("");
                    field.setForeground(java.awt.Color.BLACK);
                    field.setEchoChar('•'); // restore password masking
                }
            }

            @Override
            public void focusLost(java.awt.event.FocusEvent e) {
                if (field.getPassword().length == 0) {
                    field.setForeground(java.awt.Color.GRAY);
                    field.setText(placeholder);
                    field.setEchoChar((char) 0); // show placeholder as plain text
                }
            }
        });
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel2 = new javax.swing.JPanel();
        jPanel1 = new javax.swing.JPanel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jLabel1 = new javax.swing.JLabel();
        txtFullName = new javax.swing.JTextField();
        jButton1 = new javax.swing.JButton();
        txtUsername = new javax.swing.JTextField();
        txtEmail = new javax.swing.JTextField();
        txtPassword = new javax.swing.JPasswordField();
        txtConfirmPassword = new javax.swing.JPasswordField();
        txtContact = new javax.swing.JTextField();
        jLabel2 = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jPanel2.setBackground(new java.awt.Color(255, 255, 255));
        jPanel2.setFont(new java.awt.Font("Century Schoolbook", 0, 18)); // NOI18N

        jPanel1.setBackground(new java.awt.Color(0, 54, 49));
        jPanel1.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabel3.setIcon(new javax.swing.ImageIcon(System.getProperty("user.dir") + "\\src\\main\\java\\assets\\logo_white.png"));
        jPanel1.add(jLabel3, new org.netbeans.lib.awtextra.AbsoluteConstraints(115, 100, -1, -1));

        jLabel4.setFont(new java.awt.Font("Century Schoolbook", 0, 24)); // NOI18N
        jLabel4.setForeground(new java.awt.Color(255, 255, 255));
        jLabel4.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel4.setText("<html><div style='text-align: center;'>A parking management<br> company that gives the parking<br  privilege to any businesses.<br><br>We ensure to give you the<br>convenience, simplicity, and<br>security in every access.</div></html>");
        jPanel1.add(jLabel4, new org.netbeans.lib.awtextra.AbsoluteConstraints(64, 351, -1, 288));

        jLabel1.setFont(new java.awt.Font("Century Schoolbook", 0, 60)); // NOI18N
        jLabel1.setForeground(new java.awt.Color(1, 96, 88));
        jLabel1.setText("REGISTER");

        txtFullName.setBorder(javax.swing.BorderFactory.createTitledBorder(new javax.swing.border.LineBorder(new java.awt.Color(0, 102, 102), 3, true), "  Full Name  ", javax.swing.border.TitledBorder.LEFT, javax.swing.border.TitledBorder.TOP, new java.awt.Font("Century Schoolbook", 1, 18), new java.awt.Color(0, 102, 102))); // NOI18N
        txtFullName.addActionListener(this::txtFullNameActionPerformed);

        jButton1.setBackground(new java.awt.Color(0, 102, 102));
        jButton1.setFont(new java.awt.Font("Century Schoolbook", 1, 18)); // NOI18N
        jButton1.setForeground(new java.awt.Color(255, 255, 255));
        jButton1.setText("Register");
        jButton1.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(0, 102, 102), 1, true));
        jButton1.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        jButton1.addActionListener(this::jButton1ActionPerformed);

        txtUsername.setBorder(javax.swing.BorderFactory.createTitledBorder(new javax.swing.border.LineBorder(new java.awt.Color(0, 102, 102), 3, true), "  Username  ", javax.swing.border.TitledBorder.LEFT, javax.swing.border.TitledBorder.TOP, new java.awt.Font("Century Schoolbook", 1, 18), new java.awt.Color(0, 102, 102))); // NOI18N
        txtUsername.addActionListener(this::txtUsernameActionPerformed);

        txtEmail.setBorder(javax.swing.BorderFactory.createTitledBorder(new javax.swing.border.LineBorder(new java.awt.Color(0, 102, 102), 3, true), "  Email  ", javax.swing.border.TitledBorder.LEFT, javax.swing.border.TitledBorder.TOP, new java.awt.Font("Century Schoolbook", 1, 18), new java.awt.Color(0, 102, 102))); // NOI18N
        txtEmail.addActionListener(this::txtEmailActionPerformed);

        txtPassword.setBorder(javax.swing.BorderFactory.createTitledBorder(new javax.swing.border.LineBorder(new java.awt.Color(0, 102, 102), 3, true), "  Password  ", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.TOP, new java.awt.Font("Century Schoolbook", 1, 18), new java.awt.Color(0, 102, 102))); // NOI18N
        txtPassword.setMaximumSize(new java.awt.Dimension(2147483647, 55));
        txtPassword.setMinimumSize(new java.awt.Dimension(526, 55));
        txtPassword.setName(""); // NOI18N
        txtPassword.setPreferredSize(new java.awt.Dimension(526, 55));
        txtPassword.addActionListener(this::txtPasswordActionPerformed);

        txtConfirmPassword.setBorder(javax.swing.BorderFactory.createTitledBorder(new javax.swing.border.LineBorder(new java.awt.Color(0, 102, 102), 3, true), "  Confirm Password  ", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.TOP, new java.awt.Font("Century Schoolbook", 1, 18), new java.awt.Color(0, 102, 102))); // NOI18N
        txtConfirmPassword.setMinimumSize(new java.awt.Dimension(526, 55));
        txtConfirmPassword.setPreferredSize(new java.awt.Dimension(526, 55));
        txtConfirmPassword.addActionListener(this::txtConfirmPasswordActionPerformed);

        txtContact.setBorder(javax.swing.BorderFactory.createTitledBorder(new javax.swing.border.LineBorder(new java.awt.Color(0, 102, 102), 3, true), "  Contact  ", javax.swing.border.TitledBorder.LEFT, javax.swing.border.TitledBorder.TOP, new java.awt.Font("Century Schoolbook", 1, 18), new java.awt.Color(0, 102, 102))); // NOI18N
        txtContact.addActionListener(this::txtContactActionPerformed);

        jLabel2.setFont(new java.awt.Font("Century Schoolbook", 0, 14)); // NOI18N
        jLabel2.setText("Already have an account?");
        jLabel2.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        jLabel2.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jLabel2MouseClicked(evt);
            }
        });

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, 500, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(152, 152, 152)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel1)
                            .addComponent(txtFullName, javax.swing.GroupLayout.PREFERRED_SIZE, 526, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(txtUsername, javax.swing.GroupLayout.PREFERRED_SIZE, 526, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(txtContact, javax.swing.GroupLayout.PREFERRED_SIZE, 526, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addComponent(txtConfirmPassword, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(txtPassword, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(txtEmail, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 526, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 129, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(229, 229, 229)
                        .addComponent(jLabel2)))
                .addGap(108, 108, 108))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGap(50, 50, 50)
                .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 83, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(txtFullName, javax.swing.GroupLayout.PREFERRED_SIZE, 54, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(txtUsername, javax.swing.GroupLayout.PREFERRED_SIZE, 54, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(txtContact, javax.swing.GroupLayout.PREFERRED_SIZE, 54, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(txtEmail, javax.swing.GroupLayout.PREFERRED_SIZE, 54, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(txtPassword, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(txtConfirmPassword, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(36, 36, 36)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 41, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel2))
                .addGap(130, 130, 130))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addGap(0, 0, Short.MAX_VALUE)
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void txtFullNameActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtFullNameActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtFullNameActionPerformed

    private void txtUsernameActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtUsernameActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtUsernameActionPerformed

    private void txtEmailActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtEmailActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtEmailActionPerformed

    private void txtPasswordActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtPasswordActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtPasswordActionPerformed

    private void txtConfirmPasswordActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtConfirmPasswordActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtConfirmPasswordActionPerformed

    private void txtContactActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtContactActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtContactActionPerformed

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        String fullName = txtFullName.getText();
        String username = txtUsername.getText();
        String email = txtEmail.getText();
        String contact = txtContact.getText().trim();
        String password = txtPassword.getText();
        String confirmPassword = txtConfirmPassword.getText();

        UserController userController = new UserController();
        
        if (fullName.equals("e.g. Juan Dela Cruz")) fullName = "";
        if (username.equals("e.g. juan123")) username = "";
        if (email.equals("e.g. juan@email.com")) email = "";
        if (contact.equals("e.g. 09123456789")) contact = "";
        if (password.equals("e.g. Password123")) password = "";
        if (confirmPassword.equals("e.g. Password123")) confirmPassword = "";

        if (fullName.isEmpty() || username.isEmpty() || email.isEmpty() ||
            contact.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            Dialogue_SystemMessage.showMessage(this, 
                "Please fill up all the informations.");
            return;
        }

        if (!contact.matches("\\d+")) {
            Dialogue_SystemMessage.showMessage(this, 
                "Contact must be numbers only.");
            return;
        }

        if (contact.length() < 10 || contact.length() > 15) {
            Dialogue_SystemMessage.showMessage(this,
                "Contact number must be between 10 and 15 digits.");
            return;
        }

        // Fix: add password requirements
        if (password.length() < 8) {
            Dialogue_SystemMessage.showMessage(this,
                "Password must be at least 8 characters.");
            return;
        }

        if (!password.matches(".*[A-Z].*")) {
            Dialogue_SystemMessage.showMessage(this,
                "Password must contain at least one uppercase letter.");
            return;
        }

        if (!password.matches(".*[0-9].*")) {
            Dialogue_SystemMessage.showMessage(this,
                "Password must contain at least one number.");
            return;
        }

        if (!userController.isMatched(password, confirmPassword)) {
            Dialogue_SystemMessage.showMessage(this, "Password don't match.");
            return;
        }

        if (userController.isUsernameRegistered(username)) {
            Dialogue_SystemMessage.showMessage(this, 
                "Username already registered.");
            return;
        }

        if (userController.register(fullName, username, email, contact, password)) {
            try {
                // Check if they became admin
                if (userController.isAdmin(username)) {
                    Dialogue_SystemMessage.showMessage(this,
                            "Registration successful!\n\n" +
                                    "You are the first user and have been\n" +
                                    "assigned as Admin automatically.\n\n" +
                                    "You can now login.");
                } else {
                    Dialogue_SystemMessage.showMessage(this,
                            "Registration submitted!\n\n" +
                                    "Your account is pending admin approval.\n" +
                                    "Please wait for an admin to activate your account.");
                }
            } catch (SQLException ex) {
                System.getLogger(RegisterForm.class.getName()).log(System.Logger.Level.ERROR, (String) null, ex);
            }
            this.dispose();
            LoginForm loginObj = new LoginForm();
            loginObj.setVisible(true);
        } else {
            Dialogue_SystemMessage.showMessage(this, "Email already registered.");
        }
    }//GEN-LAST:event_jButton1ActionPerformed

    private void jLabel2MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel2MouseClicked
        this.dispose();

        LoginForm loginObj = new LoginForm();
        loginObj.setVisible(true);
    }//GEN-LAST:event_jLabel2MouseClicked

    public static void main(String args[]) {
        
        java.awt.EventQueue.invokeLater(() -> new RegisterForm().setVisible(true));
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPasswordField txtConfirmPassword;
    private javax.swing.JTextField txtContact;
    private javax.swing.JTextField txtEmail;
    private javax.swing.JTextField txtFullName;
    private javax.swing.JPasswordField txtPassword;
    private javax.swing.JTextField txtUsername;
    // End of variables declaration//GEN-END:variables
}
