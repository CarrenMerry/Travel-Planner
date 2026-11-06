package com.travelplanner.dao;

import com.travelplanner.model.User;
import com.travelplanner.persistence.DatabaseConnection;

import java.sql.*;

/**
 * Data Access Object for the 'users' table in MySQL.
 */
public class UserDao {

    public User findByEmailOrUsername(String identifier) {
        if (identifier == null || identifier.trim().isEmpty()) return null;
        String idStr = identifier.trim().toLowerCase();

        String sql = "SELECT id, name, email, password_hash, role FROM users " +
                     "WHERE LOWER(email) = ? OR LOWER(name) = ? OR LOWER(SUBSTRING_INDEX(email, '@', 1)) = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, idStr);
            ps.setString(2, idStr);
            ps.setString(3, idStr);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToUser(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("[UserDao] Error finding user: " + e.getMessage());
        }
        return null;
    }

    public User findById(int id) {
        String sql = "SELECT id, name, email, password_hash, role FROM users WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToUser(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("[UserDao] Error finding user by ID: " + e.getMessage());
        }
        return null;
    }

    public boolean createUser(String name, String email, String passwordHash, User.Role role) {
        String sql = "INSERT INTO users (name, email, password_hash, role) VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, name);
            ps.setString(2, email);
            ps.setString(3, passwordHash);
            ps.setString(4, role != null ? role.name() : "USER");

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[UserDao] Error creating user: " + e.getMessage());
        }
        return false;
    }

    public int count() {
        String sql = "SELECT COUNT(*) FROM users";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("[UserDao] Error in count: " + e.getMessage());
        }
        return 0;
    }

    public void ensureDefaultAccounts(String adminHash, String userHash) {
        if (count() == 0) {
            System.out.println("[UserDao] Provisioning initial administrator and traveler accounts into MySQL...");
            createUser("System Administrator", "admin@travelplanner.com", adminHash, User.Role.ADMIN);
            createUser("Alex Traveler", "traveler@travelplanner.com", userHash, User.Role.USER);
        }
    }

    private User mapResultSetToUser(ResultSet rs) throws SQLException {
        int id = rs.getInt("id");
        String name = rs.getString("name");
        String email = rs.getString("email");
        String hash = rs.getString("password_hash");
        String roleStr = rs.getString("role");
        User.Role role = "ADMIN".equalsIgnoreCase(roleStr) ? User.Role.ADMIN : User.Role.USER;

        return new User(id, name, email, hash, role);
    }
}
