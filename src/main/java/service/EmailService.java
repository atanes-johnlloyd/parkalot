/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package service;

import javax.mail.*;
import javax.mail.internet.*;
import java.util.Properties;
import java.util.Random;

public class EmailService {
    
    // CHANGE THESE TO YOUR GMAIL CREDENTIALS!
    private static final String FROM_EMAIL = "parkalot2026@gmail.com";
    private static final String APP_PASSWORD = "ndqc lllc lted tgrk";
    
    public String generateVerificationCode() {
        Random random = new Random();
        int code = 100000 + random.nextInt(900000);
        return String.valueOf(code);
    }
    
    public boolean sendVerificationEmail(String toEmail, String verificationCode) {
        Properties props = new Properties();
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        
        Session session = Session.getInstance(props, new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(FROM_EMAIL, APP_PASSWORD);
            }
        });
        
        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(FROM_EMAIL));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
            message.setSubject("ParkаLot - Password Reset Code");
            
            String emailBody = "Hello,\n\n"
                    + "Your password reset verification code is: " + verificationCode + "\n\n"
                    + "This code will expire in 10 minutes.\n\n"
                    + "If you didn't request this, please ignore this email.\n\n"
                    + "Best regards,\nParkаLot Team";
            
            message.setText(emailBody);
            Transport.send(message);
            
            System.out.println("Email sent successfully to: " + toEmail);
            return true;
            
        } catch (MessagingException e) {
            System.out.println("Failed to send email: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
}
