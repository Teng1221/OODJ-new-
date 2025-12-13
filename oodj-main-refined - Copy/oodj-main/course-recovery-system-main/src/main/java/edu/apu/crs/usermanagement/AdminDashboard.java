/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package edu.apu.crs.usermanagement;
import javax.swing.*;
import java.awt.*;
/**
 *
 * @author acer
 */
public class AdminDashboard extends JFrame {

    private UserManager manager;
    private JList<User> userList;
    private String adminEmail;

    public AdminDashboard(UserManager manager, String adminEmail) {
        this.manager = manager;
        this.adminEmail = adminEmail;

        setTitle("Admin Dashboard");
        setSize(500, 500);
        setResizable(false);
        setLayout(null);
        getContentPane().setBackground(new Color(25, 25, 25));

        JLabel title = new JLabel("User Manager");
        title.setForeground(Color.WHITE);
        title.setBounds(190, 20, 200, 30);
        add(title);

   
        userList = new JList<>(manager.getUsers().toArray(new User[0]));
        userList.setBounds(40, 70, 400, 250);
        add(userList);

        JButton activate = new JButton("Activate");
        activate.setBounds(40, 340, 120, 30);
        add(activate);

        JButton deactivate = new JButton("Deactivate");
        deactivate.setBounds(180, 340, 120, 30);
        add(deactivate);

        JButton addUser = new JButton("Add User");
        addUser.setBounds(320, 340, 120, 30);
        add(addUser);

        JButton logout = new JButton("Logout");
        logout.setBounds(180, 390, 120, 30);
        add(logout);

      

        // ACTIVATE USER
        activate.addActionListener(e -> {
            User selected = userList.getSelectedValue();
            if (selected == null) {
                JOptionPane.showMessageDialog(this, "Select a user first!");
                return;
            }

            manager.setStatus(selected.getEmail(), true);
            refreshList();
        });

        // DEACTIVATE USER
        deactivate.addActionListener(e -> {
            User selected = userList.getSelectedValue();
            if (selected == null) {
                JOptionPane.showMessageDialog(this, "Select a user first!");
                return;
            }

            if (selected.getEmail().equals(adminEmail)) {
                JOptionPane.showMessageDialog(this, "Admin cannot deactivate themselves!");
                return;
            }

            manager.setStatus(selected.getEmail(), false);
            refreshList();
        });

        // ADD USER
        addUser.addActionListener(e -> {
            String email = JOptionPane.showInputDialog(this, "Email:");
            if (email == null) return;

            String pass = JOptionPane.showInputDialog(this, "Password:");
            if (pass == null) return;

            String role = JOptionPane.showInputDialog(this, "Role (admin/user):");
            if (role == null) return;

            if (manager.addUser(email, pass, role)) {
                refreshList();
            } else {
                JOptionPane.showMessageDialog(this, "User already exists!");
            }
        });

        // LOGOUT
        logout.addActionListener(e -> {
            manager.logLogout(adminEmail);
            new UserLogin(manager);
            dispose();
        });

        setLocationRelativeTo(null);
        setVisible(true);
    }

    private void refreshList() {
        userList.setListData(manager.getUsers().toArray(new User[0]));
    }
}