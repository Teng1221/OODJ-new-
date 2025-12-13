/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package edu.apu.crs.usermanagement;
import javax.swing.*;
import java.awt.*;
import jakarta.mail.*;
import jakarta.mail.internet.*;
import java.util.Properties;

import java.util.Random;

/**
 *
 * @author acer
 */
public class ChangePassword extends JFrame {
    private UserManager manager;

    private String generatedCode = null;   // The verification code

    public ChangePassword(UserManager manager) {
        this.manager = manager;

        setTitle("Reset Password");
        setSize(430, 380);
        setResizable(false);
        setLayout(null);
        getContentPane().setBackground(new Color(25, 25, 25));

        JLabel title = new JLabel("Reset Password");
        title.setForeground(Color.WHITE);
        title.setBounds(150, 20, 200, 30);
        add(title);

        JLabel emailLbl = new JLabel("Email:");
        emailLbl.setForeground(Color.WHITE);
        emailLbl.setBounds(60, 70, 150, 20);
        add(emailLbl);

        JTextField emailTxt = new JTextField();
        emailTxt.setBounds(60, 90, 300, 30);
        add(emailTxt);

        JButton sendBtn = new JButton("Send Code");
        sendBtn.setBounds(60, 130, 300, 30);
        add(sendBtn);

        JLabel codeLbl = new JLabel("Reset Code:");
        codeLbl.setForeground(Color.WHITE);
        codeLbl.setBounds(60, 170, 150, 20);
        add(codeLbl);

        JTextField codeTxt = new JTextField();
        codeTxt.setBounds(60, 190, 300, 30);
        add(codeTxt);

        JLabel newLbl = new JLabel("New Password:");
        newLbl.setForeground(Color.WHITE);
        newLbl.setBounds(60, 220, 150, 20);
        add(newLbl);

        JPasswordField newPass = new JPasswordField();
        newPass.setBounds(60, 240, 300, 30);
        add(newPass);

        JButton updateBtn = new JButton("Update Password");
        updateBtn.setBounds(60, 275, 300, 30);
        add(updateBtn);

        // send email
        sendBtn.addActionListener(e -> {
            String email = emailTxt.getText().trim();

            // Check if email exists
            boolean exists = false;
            for (User u : manager.getUsers()) {
                if (u.getEmail().equalsIgnoreCase(email)) {
                    exists = true;
                    break;
                }
            }
            if (!exists) {
                JOptionPane.showMessageDialog(this, "Email not found!");
                return;
            }

            // Generate 6 digit code
            generatedCode = String.format("%06d", new Random().nextInt(999999));

            try {
                sendEmail(email, generatedCode);
                JOptionPane.showMessageDialog(this, "Reset code sent to email!");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Failed to send email: " + ex.getMessage());
            }
        });

        //Update password
        updateBtn.addActionListener(e -> {
            String email = emailTxt.getText();
            String code = codeTxt.getText().trim();
            String newP = new String(newPass.getPassword());

            if (generatedCode == null) {
                JOptionPane.showMessageDialog(this, "Please request a reset code first.");
                return;
            }

            if (!generatedCode.equals(code)) {
                JOptionPane.showMessageDialog(this, "Invalid reset code.");
                return;
            }

            // Update in UserManager
            boolean ok = manager.resetPassword(email, newP);

            if (ok) {
                JOptionPane.showMessageDialog(this, "Password updated!");
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, "Something went wrong.");
            }
        });

        setLocationRelativeTo(null);
        setVisible(true);
    }

    // Send email 
    private void sendEmail(String to, String code) throws Exception {
        String host = "smtp.gmail.com";
        final String username = "courserecoverysystem.apu@gmail.com";
        final String appPassword = "sunztfhrmdxsqffn"; // App password

        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", host);
        props.put("mail.smtp.port", "587");

       Session session = Session.getInstance(props, new Authenticator() {
    @Override
    protected PasswordAuthentication getPasswordAuthentication() {
        return new PasswordAuthentication(username, appPassword);
    }
});


        Message msg = new MimeMessage(session);
        msg.setFrom(new InternetAddress(username));
        msg.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
        msg.setSubject("Your Password Reset Code");
        msg.setText("Your reset code is: " + code);

        Transport.send(msg);
    }
}
