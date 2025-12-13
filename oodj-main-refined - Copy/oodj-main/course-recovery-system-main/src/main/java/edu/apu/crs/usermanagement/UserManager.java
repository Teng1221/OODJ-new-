/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package edu.apu.crs.usermanagement;
import java.io.*;
import java.util.*;
/**
 *
 * @author acer
 */
public class UserManager {
     private ArrayList<User> users = new ArrayList<>();
    private final String USER_FILE = "data/users.dat";
    private final String TIMESTAMP_FILE = "data/timestamps.dat";

    public UserManager() {
    new File("data").mkdirs();

    loadUsers();
    if (users.isEmpty()) {
        users.add(new User("admin@example.com", "admin123", "admin", true));
        saveUsers();
    }
}
   
    public void saveUsers() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(USER_FILE))) {
            oos.writeObject(users);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void loadUsers() {
        File f = new File(USER_FILE);
        if (!f.exists()) return;

        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(USER_FILE))) {
            users = (ArrayList<User>) ois.readObject();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // AUTHENTICATION
    public User authenticate(String email, String password) {
        for (User u : users) {
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
    for (User u : users) {
        if (u.getEmail().equalsIgnoreCase(email)) {
            u.setPassword(newPass);
            saveUsers();
            return true;
        }
    }
    return false;
}


    // USER CONTROL
    public boolean addUser(String email, String password, String role) {
        for (User u : users) {
            if (u.getEmail().equalsIgnoreCase(email)) return false;
        }
        users.add(new User(email, password, role, true));
        saveUsers();
        return true;
    }

    public ArrayList<User> getUsers() {
        return users;
    }

    public void setStatus(String email, boolean active) {
        for (User u : users) {
            if (u.getEmail().equalsIgnoreCase(email)) {
                u.setActive(active);
                saveUsers();
                return;
            }
        }
    }
    
}
