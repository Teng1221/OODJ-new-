package edu.apu.crs;
import javax.swing.SwingUtilities;
import edu.apu.crs.usermanagement.UserLogin;
import edu.apu.crs.usermanagement.UserManager;

public class App {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            UserManager userManager = new UserManager();
            UserLogin login = new UserLogin(userManager);
            login.setVisible(true);
        });
    }
}