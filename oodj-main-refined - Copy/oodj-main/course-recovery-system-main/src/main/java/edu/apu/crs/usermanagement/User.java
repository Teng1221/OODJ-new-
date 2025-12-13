/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package edu.apu.crs.usermanagement;
import java.io.Serializable;

/**
 *
 * @author acer
 */
public class User implements Serializable {
     private String email;
    private String password;
    private String role;      // admin or user
    private boolean active;   // check activity

    public User(String email, String password, String role, boolean active) {
        this.email = email;
        this.password = password;
        this.role = role;
        this.active = active;
    }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    @Override
    public String toString() {
        return email + " | " + role + " | " + (active ? "ACTIVE" : "INACTIVE");
    }
    
}
