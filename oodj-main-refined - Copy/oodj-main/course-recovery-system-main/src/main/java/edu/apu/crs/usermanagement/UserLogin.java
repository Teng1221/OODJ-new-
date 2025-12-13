/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package edu.apu.crs.usermanagement;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import jakarta.mail.*;
import jakarta.mail.internet.*;

/**
 *
 * @author acer
 */
public class UserLogin extends JFrame {

    private JTextField emailField;
    private JPasswordField passwordField;
    private UserManager manager;

    
    public UserLogin(UserManager manager) {
        this.manager = manager;

        setTitle("Login");
        setSize(460, 400);
        setResizable(false);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(null);
        getContentPane().setBackground(new Color(25, 25, 25));

        JLabel title = new JLabel("LOGIN");
        title.setForeground(Color.WHITE);
        title.setFont(new Font("Arial", Font.BOLD, 26));
        title.setBounds(170, 30, 200, 40);
        add(title);

        JLabel emailLbl = new JLabel("Email:");
        emailLbl.setForeground(Color.WHITE);
        emailLbl.setBounds(60, 110, 150, 20);
        add(emailLbl);

        emailField = new JTextField();
        emailField.setBounds(60, 135, 300, 30);
        add(emailField);

        JLabel passLbl = new JLabel("Password:");
        passLbl.setForeground(Color.WHITE);
        passLbl.setBounds(60, 175, 150, 20);
        add(passLbl);

        passwordField = new JPasswordField();
        passwordField.setBounds(60, 200, 300, 30);
        add(passwordField);

        JButton loginBtn = new JButton("Login");
        loginBtn.setBounds(60, 250, 300, 35);
        loginBtn.setBackground(new Color(50, 150, 240));
        loginBtn.setForeground(Color.WHITE);
        add(loginBtn);

        JButton forgotBtn = new JButton("Forgot Password");
        forgotBtn.setBounds(60, 300, 300, 30);
        forgotBtn.setBackground(Color.GRAY);
        forgotBtn.setForeground(Color.WHITE);
        add(forgotBtn);

        JButton backBtn = new JButton("Back to User Login");
        backBtn.setBounds(60, 350, 300, 30);
        backBtn.setBackground(Color.GRAY);
        backBtn.setForeground(Color.WHITE);
        add(backBtn);

        // LOGIN 
        loginBtn.addActionListener(e -> {
            String email = emailField.getText().trim();
            String pass = new String(passwordField.getPassword());

            User u = manager.authenticate(email, pass);

            if (u == null) {
                JOptionPane.showMessageDialog(this, "Invalid login or inactive account!");
                return;
            }

            if (u.getRole().equals("admin")) {
                new AdminDashboard(manager, u.getEmail());
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, "User login successful!");
            }
        });

        forgotBtn.addActionListener(e -> new ChangePassword(manager));

        backBtn.addActionListener(e -> {
            dispose();
            new LoginPage().setVisible(true);
        });

        setLocationRelativeTo(null);
        setVisible(true);
    }

}