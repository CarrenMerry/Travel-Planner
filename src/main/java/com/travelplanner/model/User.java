package com.travelplanner.model;

/**
 * User account model representing authenticated portal actors.
 * Maps directly to MySQL 'users' table.
 */
public class User {
    public enum Role {
        ADMIN,
        USER
    }

    private int id;
    private String username;
    private String email;
    private String passwordHash;
    private Role role;
    private String displayName;

    public User() {}

    public User(String username, String passwordHash, Role role, String displayName) {
        this.username = username;
        this.passwordHash = passwordHash;
        this.role = role;
        this.displayName = displayName;
        this.email = username.contains("@") ? username : (username + "@travelplanner.com");
    }

    public User(int id, String name, String email, String passwordHash, Role role) {
        this.id = id;
        this.displayName = name;
        this.email = email;
        this.username = email != null && email.contains("@") ? email.substring(0, email.indexOf('@')) : name;
        this.passwordHash = passwordHash;
        this.role = role;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUsername() {
        return username != null ? username : (email != null ? email : displayName);
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public String getDisplayName() {
        return displayName != null ? displayName : username;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public boolean isAdmin() {
        return this.role == Role.ADMIN;
    }

    @Override
    public String toString() {
        return displayName + " (" + getUsername() + " - " + role + ")";
    }
}
