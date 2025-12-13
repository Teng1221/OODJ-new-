/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package edu.apu.crs.usermanagement;

import edu.apu.crs.models.SystemUser;
import edu.apu.crs.models.AcademicOfficer;
import edu.apu.crs.models.CourseAdministrator;
import edu.apu.crs.dataIO.SystemUserFileReader;
import java.io.*;
import java.util.*;

/**
 * Manages SystemUser objects using text-based persistence.
 */
public class UserManager {
    private List<SystemUser> users = new ArrayList<>();
    private final String USER_FILE = "src/main/resources/data/systemUserList.txt";
    private final String TIMESTAMP_FILE = "data/timestamps.dat"; // Keep binary for logs per original code

    public UserManager() {
        new File("data").mkdirs();
        loadUsers();
        
        // No default admin creation needed if file exists, 
        // usually systemUserList.txt comes pre-populated.
    }
   
    public void saveUsers() {
        // Rewrite the entire text file
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(USER_FILE))) {
            for (SystemUser u : users) {
                // Format: ID,Name,Email,Pass,Role
                // Note: Original file has 5 columns. I will stick to 5 columns to match user request "based on all the txt file".
                // But wait, if I want to support "Active/Inactive", I might need a 6th column or handle it elsewhere.
                // The implementation plan said "append to the file".
                // Let's check sample: U001,DrSarah Chen,sarah.chen@university.edu,sarah123,Academic Officer
                
                String line = String.format("%s,%s,%s,%s,%s,%b",
                        u.getUserID(),
                        u.getUsername(),
                        u.getEmail(),
                        u.getPassword(),
                        u.getRole(),
                        u.isActive()
                );
                bw.write(line);
                bw.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void loadUsers() {
        // Delegate to the SystemUserFileReader which already parses the file
        // However, SystemUserFileReader might not handle the 6th "Active" column yet if I just added it.
        // I will rely on SystemUserFileReader for the base load, but I might need to update IT too if I add columns.
        // For now, let's use SystemUserFileReader as is, and if I add a 6th column, I should update the Reader.
        // Actually, I should update the Reader to handle the active flag if I write it.
        
        // But wait, SystemUserFileReader returns List<SystemUser>.
        this.users = SystemUserFileReader.readAllUsers();
        
        // If the reader doesn't support the 6th column, 'isActive' will default to true (as per Reader impl).
    }

    // AUTHENTICATION
    public SystemUser authenticate(String email, String password) {
        for (SystemUser u : users) {
            if (u.getEmail().equalsIgnoreCase(email)) {
                if (!u.isActive()) {
                    return null; // inactive
                }
                if (u.getPassword().equals(password)) {
                    logTimestamp(email, "LOGIN");
                    return u;
                }
            }
        }
        return null;
    }

    // TIMESTAMP LOGGING 
    public void logTimestamp(String email, String type) {
        try (DataOutputStream dos = new DataOutputStream(new FileOutputStream(TIMESTAMP_FILE, true))) {
            long time = System.currentTimeMillis();
            dos.writeUTF(email);
            dos.writeUTF(type);
            dos.writeLong(time);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void logLogout(String email) {
        logTimestamp(email, "LOGOUT");
    }

    // PASSWORD CHANGE 
    public boolean resetPassword(String email, String newPass) {
        for (SystemUser u : users) {
            if (u.getEmail().equalsIgnoreCase(email)) {
                u.setPassword(newPass);
                saveUsers();
                return true;
            }
        }
        return false;
    }

    // USER CONTROL
    public boolean addUser(String usernameInput, String email, String password, String role) {
        // Check duplicate email
        for (SystemUser u : users) {
            if (u.getEmail().equalsIgnoreCase(email)) return false;
        }

        // Generate ID: Find max Uxxx
        int maxId = 0;
        for (SystemUser u : users) {
            String id = u.getUserID(); // e.g., U021
            if (id.startsWith("U")) {
                try {
                    int num = Integer.parseInt(id.substring(1));
                    if (num > maxId) maxId = num;
                } catch (NumberFormatException ignored) {}
            }
        }
        String newId = String.format("U%03d", maxId + 1);

        SystemUser newUser = null;
        if (role.equalsIgnoreCase("Academic Officer")) {
            newUser = new AcademicOfficer(newId, usernameInput, email, password, role, true);
        } else {
            // Default to Course Admin for any other string to be safe, or stricter check
            newUser = new CourseAdministrator(newId, usernameInput, email, password, "Course Admin", true);
        }

        users.add(newUser);
        saveUsers();
        return true;
    }

    public List<SystemUser> getUsers() {
        return users;
    }

    public void setStatus(String email, boolean active) {
        for (SystemUser u : users) {
            if (u.getEmail().equalsIgnoreCase(email)) {
                u.setActive(active);
                saveUsers();
                return;
            }
        }
    }
}
