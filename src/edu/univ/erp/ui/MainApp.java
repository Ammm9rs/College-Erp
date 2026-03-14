package edu.univ.erp.ui;

import javax.swing.SwingUtilities;
import edu.univ.erp.auth.AuthService;
import edu.univ.erp.service.ERPService;
import com.formdev.flatlaf.FlatLightLaf;

public class MainApp {
    public static void main(String[] args) {
        try {
            FlatLightLaf.setup();
        } catch (Exception ex) {
            System.err.println("Theme error: " + ex.getMessage());
        }

        // No more InMemoryStore!
        // Services connect directly to DB now.
        AuthService auth = new AuthService(null);
        ERPService service = new ERPService();

        SwingUtilities.invokeLater(() -> {
            LoginFrame lf = new LoginFrame(auth, service);
            lf.setVisible(true);
        });
    }
}