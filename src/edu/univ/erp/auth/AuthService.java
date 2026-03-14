package edu.univ.erp.auth;

import edu.univ.erp.data.DBConnection;
import edu.univ.erp.domain.User;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.sql.*;
import java.util.Base64;

public class AuthService {
    private User sessionUser = null;
    private String lastError = "Incorrect username or password.";

    public AuthService(Object ignored) {}

    // --- Hashing Helper ---
    public String hash(String plainText) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(plainText.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hash);
        } catch (Exception ex) {
            return plainText; // Fallback only if system is broken
        }
    }

    public String getLastError() {
        return lastError;
    }

    public boolean verify(String username, String plainPassword) {
        // SECURITY: Never print passwords to console!

        String sql = "SELECT user_id, username, role, password_hash, failed_attempts, lockout_end FROM users_auth WHERE username = ?";

        try (Connection conn = DBConnection.getAuthConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    // 1. CHECK LOCKOUT
                    Timestamp lockEnd = rs.getTimestamp("lockout_end");
                    if (lockEnd != null && lockEnd.after(new Timestamp(System.currentTimeMillis()))) {
                        long diffMin = (lockEnd.getTime() - System.currentTimeMillis()) / (60 * 1000) + 1;
                        lastError = "Account locked. Try again in " + diffMin + " minutes.";
                        return false;
                    }

                    // 2. CHECK PASSWORD
                    String dbHash = rs.getString("password_hash");
                    String roleStr = rs.getString("role");
                    String userId = rs.getString("user_id");
                    int attempts = rs.getInt("failed_attempts");

                    boolean match = false;
                    // Check Hash first (Secure), then Plain Text (Legacy support)
                    if (hash(plainPassword).equals(dbHash)) match = true;
                    else if (plainPassword.equals(dbHash)) match = true;

                    if (match) {
                        resetLockout(username);
                        this.sessionUser = new User(userId, username, dbHash, User.Role.valueOf(roleStr));
                        return true;
                    } else {
                        handleFailedAttempt(username, attempts);
                        return false;
                    }
                } else {
                    lastError = "Incorrect username or password.";
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            lastError = "Database error.";
        }

        this.sessionUser = null;
        return false;
    }

    private void resetLockout(String username) {
        try (Connection conn = DBConnection.getAuthConnection();
             PreparedStatement ps = conn.prepareStatement("UPDATE users_auth SET failed_attempts = 0, lockout_end = NULL WHERE username = ?")) {
            ps.setString(1, username);
            ps.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    private void handleFailedAttempt(String username, int currentAttempts) {
        int newAttempts = currentAttempts + 1;
        try (Connection conn = DBConnection.getAuthConnection()) {
            if (newAttempts >= 5) {
                String sql = "UPDATE users_auth SET failed_attempts = ?, lockout_end = ? WHERE username = ?";
                try(PreparedStatement ps = conn.prepareStatement(sql)) {
                    ps.setInt(1, newAttempts);
                    ps.setTimestamp(2, new Timestamp(System.currentTimeMillis() + (5 * 60 * 1000)));
                    ps.setString(3, username);
                    ps.executeUpdate();
                }
                lastError = "Too many failed attempts. Account locked for 5 min.";
            } else {
                String sql = "UPDATE users_auth SET failed_attempts = ? WHERE username = ?";
                try(PreparedStatement ps = conn.prepareStatement(sql)) {
                    ps.setInt(1, newAttempts);
                    ps.setString(2, username);
                    ps.executeUpdate();
                }
                lastError = "Incorrect username or password.";
            }
        } catch (SQLException e) { e.printStackTrace(); }
    }

    public User getSessionUser() { return sessionUser; }
    public void logout() { sessionUser = null; }
}