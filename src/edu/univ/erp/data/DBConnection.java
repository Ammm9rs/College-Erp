package edu.univ.erp.data;

import edu.univ.erp.util.ErrorHandler; // Import the new handler
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {
    // ⚠️ ENSURE THIS MATCHES YOUR DATABASE PASSWORD ⚠️
    private static final String DB_USER = "root";
    private static final String DB_PASS = "ammarkrishna";

    // URL 1: Points to the Authentication Database
    private static final String AUTH_DB_URL = "jdbc:mysql://localhost:3306/university_auth";

    // URL 2: Points to the ERP/Student Database
    private static final String ERP_DB_URL = "jdbc:mysql://localhost:3306/university_erp";

    static {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static Connection getAuthConnection() throws SQLException {
        try {
            return DriverManager.getConnection(AUTH_DB_URL, DB_USER, DB_PASS);
        } catch (SQLException e) {
            System.err.println(" Failed to connect to Auth Database.");
            // We re-throw so the UI can catch it and show the popup
            throw e;
        }
    }

    public static Connection getERPConnection() throws SQLException {
        try {
            return DriverManager.getConnection(ERP_DB_URL, DB_USER, DB_PASS);
        } catch (SQLException e) {
            System.err.println(" Failed to connect to ERP Database.");
            throw e;
        }
    }
}