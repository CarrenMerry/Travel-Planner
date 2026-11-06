package com.wayfarer.model;

import java.sql.Timestamp;

/**
 * Wayfarer — User Model
 * Maps directly to MySQL 'users' table.
 */
public class User {

    public enum Role {
        ADMIN,
        USER
    }

    private int id;
    private String name;
    private String email;
    private String passwordHash;
    private Role role;
    private Timestamp createdAt;

    public User() {}

    public User(int id, String name, String email, String passwordHash, Role role) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.passwordHash = passwordHash;
        this.role = role != null ? role : Role.USER;
    }

    public User(String name, String email, String passwordHash, Role role) {
        this(0, name, email, passwordHash, role);
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }

    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }

    public boolean isAdmin() { return role == Role.ADMIN; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", email='" + email + '\'' +
                ", role=" + role +
                '}';
    }
}
