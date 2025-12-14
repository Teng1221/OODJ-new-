package edu.apu.crs.service;

import edu.apu.crs.models.AcademicOfficer;
import edu.apu.crs.models.CourseAdministrator;
import edu.apu.crs.models.SystemUser;
import edu.apu.crs.dataIO.SystemUserFileReader; 
import java.io.DataOutputStream; // For binary logging (Phase III, Step 10)
import java.io.FileOutputStream; 
import java.io.IOException;
//import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class SystemUserService {

    // List holds the abstract class SystemUser 
    private List<SystemUser> users;

    // Path for binary logging file 
    private static final String LOG_FILE = "logs/login_timestamps.dat";

    public SystemUserService() {
        // Remove ALL hardcoded users and load from file instead
        System.out.println("Loading System Users from file...");
        this.users = SystemUserFileReader.readAllUsers();
        System.out.println("Loaded " + this.users.size() + " system users.");
    }

    public SystemUser login(String input, String password) {
        // Polymorphism: iterating over the abstract SystemUser list to find the
        // concrete subclass
        for (SystemUser u : users) {
            boolean nameMatch = u.getUsername().equalsIgnoreCase(input);
            boolean idMatch = u.getUserID().equalsIgnoreCase(input);

            if ((nameMatch || idMatch) && u.getPassword().equals(password) && u.isActive()) {
                return u;
            }
        }
        return null;
    }

    // Logs the logout timestamp to a binary file. (Phase III, Step 10)

    public void logout(String username) {
        logTimestamp(username, false); // Log logout
    }

    // Private helper method to log timestamps to a binary file 
    private void logTimestamp(String username, boolean isLogin) {
        try (DataOutputStream dos = new DataOutputStream(new FileOutputStream(LOG_FILE, true))) {
            long timestamp = new Date().getTime();
            String action = isLogin ? "LOGIN" : "LOGOUT";

            // Write to binary file: [timestamp] | [action] | [username]
            dos.writeLong(timestamp); // Log the timestamp in binary form
            dos.writeUTF("|" + action + "|" + username + "\n");
        } catch (IOException e) {
            System.err.println("Error logging timestamp: " + e.getMessage());
        }
    }

    // Adds a new SystemUser to the system, instantiating the correct subclass.

    public void addUser(String userID, String username, String email, String password, String role, boolean active) {

        SystemUser newUser = null;
        String normalizedRole = role.toLowerCase().replace(" ", "");
        switch (normalizedRole) {
            // Role assignment based on assignment requirements
            case "courseadmin":
            case "courseadministrator":
                newUser = new CourseAdministrator(userID, username, email, password, role, active);
                break;
            case "academicofficer":
                newUser = new AcademicOfficer(userID, username, email, password, role, active);
                break;
            default:
                System.err.println("Attempted to add user with unsupported role: " + role);
                newUser = null;
                break;
        }
        if (newUser != null) {
            users.add(newUser);
        }
    }

    public boolean removeUser(String username) {
        return users.removeIf(u -> u.getUsername().equalsIgnoreCase(username));
    }

    public SystemUser findUser(String username) {
        for (SystemUser u : users) {
            if (u.getUsername().equalsIgnoreCase(username)) {
                return u;
            }
        }
        return null;
    }

    public boolean updatePassword(String username, String newPassword) {
        SystemUser u = findUser(username);
        if (u != null) {
            u.setPassword(newPassword); // Encapsulation: The SystemUser object changes its own state
            return true;
        }
        return false;
    }

    // The rest of the User Management methods...
    public boolean deactivateUser(String username) {
        SystemUser u = findUser(username);
        if (u != null) {
            u.setActive(false);
            return true;
        }
        return false;
    }

    public boolean activateUser(String username) {
        SystemUser u = findUser(username);
        if (u != null) {
            u.setActive(true);
            return true;
        }
        return false;
    }

    public List<SystemUser> getAllUsers() {
        return users;
    }

    public void printAllUsers() {
        System.out.println("---- All System Users ----");
        for (SystemUser u : users) {
            // Polymorphism: calling getRoleTitle() to get the specific role name
            System.out.println(u.getUsername() + " | " + u.getRoleTitle() + " | Active: " + u.isActive());
        }
        System.out.println("-------------------");
    }

    // find user email ( for password recovery )
    public SystemUser findUserByEmail(String email) {
        for (SystemUser u : users) {
            if (u.getEmail().equalsIgnoreCase(email)) {
                return u;
            }
        }
        return null;
    }


}
