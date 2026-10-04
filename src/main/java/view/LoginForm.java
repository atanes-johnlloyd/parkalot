package view;

import controller.UserController;
import java.awt.Dimension;
import java.sql.SQLException;
import javax.swing.JOptionPane;
import view.Dialogue.Dialogue_Email;
import view.Dialogue.Dialogue_SystemMessage;

public class LoginForm extends javax.swing.JFrame {
    
    private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(LoginForm.class.getName());

    public LoginForm() {
        initComponents();
        
        this.setResizable(false);
        this.setLocationRelativeTo(null);
        
        addPlaceholder(txtIdentifier, "e.g. juan123 or juan@email.com");
        addPasswordPlaceholder(txtPassword, "e.g. Password123");
        
        txtPassword.setPreferredSize(new Dimension(526, 68));
        txtPassword.setMinimumSize(new Dimension(526, 68));
        txtPassword.setMaximumSize(new Dimension(526, 68));

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
        jLabel2 = new javax.swing.JLabel();
        txtIdentifier = new javax.swing.JTextField();
        btnLogin = new javax.swing.JButton();
        lblForgetPassword = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        txtPassword = new javax.swing.JPasswordField();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jPanel2.setBackground(new java.awt.Color(255, 255, 255));
        jPanel2.setFont(new java.awt.Font("Century Schoolbook", 0, 18)); // NOI18N

        jPanel1.setBackground(new java.awt.Color(0, 54, 49));

        jLabel3.setIcon(new javax.swing.ImageIcon(System.getProperty("user.dir") + "\\src\\main\\java\\assets\\logo_white.png"));

        jLabel4.setFont(new java.awt.Font("Century Schoolbook", 0, 24)); // NOI18N
        jLabel4.setForeground(new java.awt.Color(255, 255, 255));
        jLabel4.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel4.setText("<html><div style='text-align: center;'>A parking management<br> company that gives the parking<br  privilege to any businesses.<br><br>We ensure to give you the<br>convenience, simplicity, and<br>security in every access.</div></html>");

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(64, 64, 64)
                .addComponent(jLabel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(69, Short.MAX_VALUE))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jLabel3)
                .addGap(109, 109, 109))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(100, 100, 100)
                .addComponent(jLabel3)
                .addGap(18, 18, 18)
                .addComponent(jLabel4, javax.swing.GroupLayout.PREFERRED_SIZE, 288, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(260, Short.MAX_VALUE))
        );

        jLabel1.setFont(new java.awt.Font("Century Schoolbook", 0, 60)); // NOI18N
        jLabel1.setForeground(new java.awt.Color(1, 96, 88));
        jLabel1.setText("WELCOME");

        jLabel2.setFont(new java.awt.Font("Century Schoolbook", 1, 48)); // NOI18N
        jLabel2.setForeground(new java.awt.Color(51, 51, 51));
        jLabel2.setText("po");

        txtIdentifier.setFont(new java.awt.Font("Century Schoolbook", 0, 18)); // NOI18N
        txtIdentifier.setBorder(javax.swing.BorderFactory.createTitledBorder(new javax.swing.border.LineBorder(new java.awt.Color(0, 102, 102), 3, true), "  Username or Email  ", javax.swing.border.TitledBorder.LEFT, javax.swing.border.TitledBorder.TOP, new java.awt.Font("Century Schoolbook", 1, 18), new java.awt.Color(0, 102, 102))); // NOI18N
        txtIdentifier.addActionListener(this::txtIdentifierActionPerformed);

        btnLogin.setBackground(new java.awt.Color(0, 102, 102));
        btnLogin.setFont(new java.awt.Font("Century Schoolbook", 1, 18)); // NOI18N
        btnLogin.setForeground(new java.awt.Color(255, 255, 255));
        btnLogin.setText("Log In");
        btnLogin.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(0, 102, 102), 1, true));
        btnLogin.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnLogin.addActionListener(this::btnLoginActionPerformed);

        lblForgetPassword.setFont(new java.awt.Font("Century Schoolbook", 0, 14)); // NOI18N
        lblForgetPassword.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        lblForgetPassword.setText("Forget Password?");
        lblForgetPassword.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        lblForgetPassword.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                lblForgetPasswordMouseClicked(evt);
            }
        });

        jLabel5.setFont(new java.awt.Font("Century Schoolbook", 0, 18)); // NOI18N
        jLabel5.setForeground(new java.awt.Color(0, 51, 51));
        jLabel5.setText("Register");
        jLabel5.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        jLabel5.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jLabel5MouseClicked(evt);
            }
        });

        txtPassword.setFont(new java.awt.Font("Century Schoolbook", 0, 18)); // NOI18N
        txtPassword.setBorder(javax.swing.BorderFactory.createTitledBorder(new javax.swing.border.LineBorder(new java.awt.Color(0, 102, 102), 3, true), "  Password  ", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.TOP, new java.awt.Font("Century Schoolbook", 1, 18), new java.awt.Color(0, 102, 102))); // NOI18N
        txtPassword.setMinimumSize(new java.awt.Dimension(64, 68));
        txtPassword.setName(""); // NOI18N
        txtPassword.setPreferredSize(new java.awt.Dimension(64, 68));

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(136, 136, 136)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(jPanel2Layout.createSequentialGroup()
                            .addComponent(jLabel1)
                            .addGap(18, 18, 18)
                            .addComponent(jLabel2))
                        .addComponent(txtIdentifier, javax.swing.GroupLayout.PREFERRED_SIZE, 526, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(txtPassword, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 526, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(btnLogin, javax.swing.GroupLayout.PREFERRED_SIZE, 129, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel2Layout.createSequentialGroup()
                                .addGap(30, 30, 30)
                                .addComponent(jLabel5))
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(lblForgetPassword)))))
                .addContainerGap(131, Short.MAX_VALUE))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGap(182, 182, 182)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 83, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGap(9, 9, 9)
                        .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 74, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(18, 18, 18)
                .addComponent(txtIdentifier, javax.swing.GroupLayout.PREFERRED_SIZE, 68, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(txtPassword, javax.swing.GroupLayout.PREFERRED_SIZE, 68, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGap(25, 25, 25)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(btnLogin, javax.swing.GroupLayout.PREFERRED_SIZE, 41, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel5)))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGap(10, 10, 10)
                        .addComponent(lblForgetPassword))))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void txtIdentifierActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtIdentifierActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtIdentifierActionPerformed

    private void btnLoginActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnLoginActionPerformed
        String identifier = txtIdentifier.getText().trim();
        String password = new String(txtPassword.getPassword()).trim();
        
        if (identifier.equals("e.g. juan123 or juan@email.com")) identifier = "";
        if (password.equals("e.g. Password123")) password = "";

        
        if (identifier.isEmpty() || password.isEmpty()) {
            Dialogue_SystemMessage.showMessage(this, "Fill up all the informations.");
            return;
        }

        UserController userController = new UserController();

        if (userController.login(identifier, password)) {

            // Set session
            model.User user = userController.getUserByIdentifier(identifier);
            if (user != null) {
                model.SessionManager.login(
                    user.getUsername(),
                    user.getPosition(),
                    user.getFullName()
                );
            }
            
            try {
                if (userController.isAdmin(identifier)) {
                    Dialogue_SystemMessage.showMessage(this, "Login Success! Welcome Admin!");
                    this.dispose();
                    view.Admin.DashboardForm adminObj = new view.Admin.DashboardForm();
                    adminObj.setVisible(true);
                } else {
                    // Staff goes to HomeForm
                    Dialogue_SystemMessage.showMessage(this, "Login Success!");
                    this.dispose();
                    HomeForm homeObj = new HomeForm();
                    homeObj.setVisible(true);
                }
            } catch (SQLException ex) {
                Dialogue_SystemMessage.showMessage(this, "Error opening dashboard.");
                ex.printStackTrace();
            }

        } else {
            if (userController.isPendingAccount(identifier)) {
                Dialogue_SystemMessage.showMessage(this,
                    "Your account is pending admin approval.\n" +
                    "Please contact an administrator.");
                return;
            }

            boolean emailExists = userController.isEmailRegistered(identifier);
            boolean usernameExists = userController.isUsernameRegistered(identifier);

            if (emailExists || usernameExists) {
                Dialogue_SystemMessage.showMessage(this, "Incorrect Password.");
            } else {
                Dialogue_SystemMessage.showMessage(this,
                    "This Email or Username is not registered.");
            }
        }
    }//GEN-LAST:event_btnLoginActionPerformed

    private void lblForgetPasswordMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_lblForgetPasswordMouseClicked
        Dialogue_Email emailDialog = new Dialogue_Email(this, true);
        emailDialog.setLocationRelativeTo(this);
        emailDialog.setVisible(true);
    }//GEN-LAST:event_lblForgetPasswordMouseClicked

    private void jLabel5MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel5MouseClicked
        this.dispose();
        
        RegisterForm registerObj = new RegisterForm();
        registerObj.setVisible(true);
    }//GEN-LAST:event_jLabel5MouseClicked

    public static void main(String args[]) {
        
        java.awt.EventQueue.invokeLater(() -> new LoginForm().setVisible(true));
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnLogin;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JLabel lblForgetPassword;
    private javax.swing.JTextField txtIdentifier;
    private javax.swing.JPasswordField txtPassword;
    // End of variables declaration//GEN-END:variables
}
