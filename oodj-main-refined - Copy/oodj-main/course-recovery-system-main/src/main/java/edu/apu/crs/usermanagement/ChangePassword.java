package edu.apu.crs.usermanagement;

import javax.swing.*;
import edu.apu.crs.models.SystemUser;
import edu.apu.crs.notification.NotificationService;
import java.util.Random;
import java.awt.*;

/**
 * Handles password reset flow with email verification.
 */
public class ChangePassword extends JFrame {
    private UserManager manager;
    private NotificationService notificationService;
    private String generatedCode = null;
    private String emailForCode = null; // Store which email the code was sent to

    public ChangePassword(UserManager manager) {
        this.manager = manager;
        this.notificationService = new NotificationService();

        setTitle("Reset Password");
        setSize(450, 420);
        setResizable(false);
        setLayout(null);
        getContentPane().setBackground(new Color(25, 25, 25));

        JLabel title = new JLabel("Reset Password");
        title.setFont(new Font("Arial", Font.BOLD, 18));
        title.setForeground(Color.WHITE);
        title.setBounds(140, 20, 200, 30);
        title.setHorizontalAlignment(SwingConstants.CENTER);
        add(title);

        // --- Email Section ---
        JLabel emailLbl = new JLabel("Email:");
        emailLbl.setForeground(Color.WHITE);
        emailLbl.setBounds(50, 70, 150, 20);
        add(emailLbl);

        JTextField emailTxt = new JTextField();
        emailTxt.setBounds(50, 90, 330, 30);
        add(emailTxt);

        JButton sendBtn = new JButton("Send Verification Code");
        sendBtn.setBounds(50, 130, 330, 30);
        sendBtn.setBackground(new Color(70, 130, 180));
        sendBtn.setForeground(Color.WHITE);
        add(sendBtn);

        // --- Verification Section ---
        JLabel codeLbl = new JLabel("Enter Code:");
        codeLbl.setForeground(Color.WHITE);
        codeLbl.setBounds(50, 180, 150, 20);
        add(codeLbl);

        JTextField codeTxt = new JTextField();
        codeTxt.setBounds(50, 200, 330, 30);
        add(codeTxt);

        // --- New Password Section ---
        JLabel newLbl = new JLabel("New Password:");
        newLbl.setForeground(Color.WHITE);
        newLbl.setBounds(50, 240, 150, 20);
        add(newLbl);

        JPasswordField newPass = new JPasswordField();
        newPass.setBounds(50, 260, 330, 30);
        add(newPass);

        JButton updateBtn = new JButton("Update Password");
        updateBtn.setBounds(50, 310, 330, 40);
        updateBtn.setBackground(new Color(34, 139, 34));
        updateBtn.setForeground(Color.WHITE);
        add(updateBtn);

        // --- Action Listeners ---
        sendBtn.addActionListener(e -> {
            String email = emailTxt.getText().trim();
            if (email.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please enter your email.");
                return;
            }

            // Check if email exists
            boolean exists = false;
            for (SystemUser u : manager.getUsers()) {
                if (u.getEmail().equalsIgnoreCase(email)) {
                    exists = true;
                    break;
                }
            }

            if (!exists) {
                JOptionPane.showMessageDialog(this, "Email not found in system!");
                return;
            }


            // Generate 6 digit code
            generatedCode = String.format("%06d", new Random().nextInt(999999));
            emailForCode = email;

            try {
                notificationService.sendPasswordResetCode(email, generatedCode);
                JOptionPane.showMessageDialog(this, "Verification code sent to " + email + ".\nPlease check your inbox.");
            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Failed to send email. Check logs.");
            }
        });

        updateBtn.addActionListener(e -> {
            String email = emailTxt.getText().trim();
            String code = codeTxt.getText().trim();
            String newP = new String(newPass.getPassword());

            if (generatedCode == null || emailForCode == null) {
                JOptionPane.showMessageDialog(this, "Please request a verification code first.");
                return;
            }

            if (!email.equalsIgnoreCase(emailForCode)) {
                JOptionPane.showMessageDialog(this, "Email changed. Please resend code for the new email.");
                return;
            }

            if (!generatedCode.equals(code)) {
                JOptionPane.showMessageDialog(this, "Invalid verification code.");
                return;
            }

            if (newP.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Password cannot be empty.");
                return;
            }

            // Update in UserManager
            boolean ok = manager.resetPassword(email, newP);

            if (ok) {
                JOptionPane.showMessageDialog(this, "Password updated successfully!\nPlease login with your new password.");
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, "Error updating password. User may not exist or file error.");
            }
        });

        setLocationRelativeTo(null);
        setVisible(true);
    }
}
