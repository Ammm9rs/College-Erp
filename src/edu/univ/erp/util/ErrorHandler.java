package edu.univ.erp.util;

import javax.swing.*;
import java.awt.*;

public class ErrorHandler {

    /**
     * Shows a friendly warning message (Validation errors, Business logic).
     */
    public static void showWarning(Component parent, String message) {
        JOptionPane.showMessageDialog(parent,
                "<html><body style='width: 250px;'>" + message + "</body></html>",
                "Action Required",
                JOptionPane.WARNING_MESSAGE);
    }

    /**
     * Shows a critical error message (Database failures, System crashes).
     */
    public static void showError(Component parent, String message, Exception ex) {
        // Print detailed error to console for the developer
        if (ex != null) ex.printStackTrace();

        String displayMsg = message;
        if (ex != null) {
            // Make SQL errors readable
            if (ex instanceof java.sql.SQLException) {
                displayMsg += "<br><br><b>Technical Detail:</b> Database connection failed or query error.";
            } else {
                displayMsg += "<br><br><b>Technical Detail:</b> " + ex.getMessage();
            }
        }

        JOptionPane.showMessageDialog(parent,
                "<html><body style='width: 300px;'>" + displayMsg + "</body></html>",
                "System Error",
                JOptionPane.ERROR_MESSAGE);
    }

    /**
     * Shows a generic info message (Success actions).
     */
    public static void showInfo(Component parent, String message) {
        JOptionPane.showMessageDialog(parent, message, "Success", JOptionPane.INFORMATION_MESSAGE);
    }
}