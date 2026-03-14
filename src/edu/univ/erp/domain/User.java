package edu.univ.erp.domain;

public class User {
    private final String userId;
    private final String username;
    private String passwordHash; // hashed
    private final Role role;

    public enum Role { ADMIN, INSTRUCTOR, STUDENT }

    public User(String userId, String username, String passwordHash, Role role) {
        this.userId = userId;
        this.username = username;
        this.passwordHash = passwordHash;
        this.role = role;
    }

    public String getUserId() { return userId; }
    public String getUsername() { return username; }
    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String h) { this.passwordHash = h; }
    public Role getRole() { return role; }
}