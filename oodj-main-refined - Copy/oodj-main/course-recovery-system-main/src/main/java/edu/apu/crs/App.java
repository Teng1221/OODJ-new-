package edu.apu.crs;
import javax.swing.SwingUtilities;
import edu.apu.crs.usermanagement.LoginPage;
import edu.apu.crs.usermanagement.UserManager;

public class App {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            // UserManager userManager = new UserManager();
            LoginPage login = new LoginPage();
            login.setVisible(true);
        });
    }
}