package edu.apu.crs.usermanagement;

import edu.apu.crs.models.SystemUser;
import javax.swing.*;
import java.awt.*;
import java.util.List;

public class AdminDashboard extends JFrame {

    private UserManager manager;
    private JList<String> userList; // Display strings, or custom renderer
    private DefaultListModel<String> listModel;
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
        title.setFont(new Font("Arial", Font.BOLD, 18));
        title.setForeground(Color.WHITE);
        title.setBounds(180, 20, 200, 30);
        add(title);

        // List Model
        listModel = new DefaultListModel<>();
        refreshList();

        userList = new JList<>(listModel);
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
            String selected = userList.getSelectedValue();
            if (selected == null) {
                JOptionPane.showMessageDialog(this, "Select a user first!");
                return;
            }
            // Parse email from string "Username | Role | Active: true | Email"
            // Wait, toString might differ. Let's use robust parsing or object list.
            // Using logic from original: User toString was "email | role | active"
            
            // Let's rely on finding by email. I need to store email in the list string.
            // Format in refreshList: "ID | Name | Email | Role | Active" 
            
            String[] parts = selected.split("\\|");
            if (parts.length < 3) return;
            String email = parts[2].trim();

            manager.setStatus(email, true);
            refreshList();
        });

        // DEACTIVATE USER
        deactivate.addActionListener(e -> {
            String selected = userList.getSelectedValue();
            if (selected == null) {
                JOptionPane.showMessageDialog(this, "Select a user first!");
                return;
            }

            String[] parts = selected.split("\\|");
            if (parts.length < 3) return;
            String email = parts[2].trim();

            if (email.equalsIgnoreCase(adminEmail)) {
                JOptionPane.showMessageDialog(this, "Admin cannot deactivate themselves!");
                return;
            }

            manager.setStatus(email, false);
            refreshList();
        });

        // ADD USER
        addUser.addActionListener(e -> {
            // New Requirement: Ask for Username (Name)
            String name = JOptionPane.showInputDialog(this, "Enter Name/Username:");
            if (name == null || name.trim().isEmpty()) return;

            String email = JOptionPane.showInputDialog(this, "Enter Email:");
            if (email == null || email.trim().isEmpty()) return;

            String pass = JOptionPane.showInputDialog(this, "Enter Password:");
            if (pass == null || pass.trim().isEmpty()) return;

            String role = JOptionPane.showInputDialog(this, "Enter Role (Course Admin / Academic Officer):");
            if (role == null || role.trim().isEmpty()) return;

            if (manager.addUser(name, email, pass, role)) {
                JOptionPane.showMessageDialog(this, "User added successfully!");
                refreshList();
            } else {
                JOptionPane.showMessageDialog(this, "User email already exists!");
            }
        });

        // LOGOUT
        logout.addActionListener(e -> {
            dispose();
            new LoginPage().setVisible(true);
        });

        setLocationRelativeTo(null);
        setVisible(true);
    }

    private void refreshList() {
        listModel.clear();
        List<SystemUser> users = manager.getUsers();
        
        // Sorting or keeping file order? File order is fine.
        for (SystemUser u : users) {
            // Display format
            String display = String.format("%s | %s | %s | %s | Active: %b",
                    u.getUserID(),
                    u.getUsername(),
                    u.getEmail(),
                    u.getRoleTitle(),
                    u.isActive()
            );
            listModel.addElement(display);
        }
    }
}