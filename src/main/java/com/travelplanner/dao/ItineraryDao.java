package com.travelplanner.dao;

import com.travelplanner.persistence.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for the 'itineraries' table in MySQL.
 */
public class ItineraryDao {

    public static class ItineraryRecord {
        public int id;
        public int userId;
        public String name;
        public double budget;
        public double totalEstimatedCost;
        public int totalDurationDays;
        public String status;

        public ItineraryRecord(int id, int userId, String name, double budget,
                               double totalEstimatedCost, int totalDurationDays, String status) {
            this.id = id;
            this.userId = userId;
            this.name = name;
            this.budget = budget;
            this.totalEstimatedCost = totalEstimatedCost;
            this.totalDurationDays = totalDurationDays;
            this.status = status;
        }
    }

    public ItineraryRecord getOrCreateActiveItinerary(int userId, String defaultName, double budget) {
        String query = "SELECT id, user_id, name, budget, total_estimated_cost, total_duration_days, status " +
                       "FROM itineraries WHERE user_id = ? ORDER BY id ASC LIMIT 1";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToItinerary(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("[ItineraryDao] Error checking existing itinerary: " + e.getMessage());
        }

        // If none exists, create default one
        int createdId = createItinerary(userId, defaultName != null ? defaultName : "Grand World Tour", budget);
        return findById(createdId);
    }

    public ItineraryRecord findById(int id) {
        String sql = "SELECT id, user_id, name, budget, total_estimated_cost, total_duration_days, status " +
                     "FROM itineraries WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToItinerary(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("[ItineraryDao] Error in findById: " + e.getMessage());
        }
        return null;
    }

    public List<ItineraryRecord> findByUserId(int userId) {
        String sql = "SELECT id, user_id, name, budget, total_estimated_cost, total_duration_days, status " +
                     "FROM itineraries WHERE user_id = ? ORDER BY id ASC";
        List<ItineraryRecord> list = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToItinerary(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("[ItineraryDao] Error in findByUserId: " + e.getMessage());
        }
        return list;
    }

    public int createItinerary(int userId, String name, double budget) {
        String sql = "INSERT INTO itineraries (user_id, name, budget, total_estimated_cost, total_duration_days, status) " +
                     "VALUES (?, ?, ?, 0.00, 0, 'DRAFT')";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, userId);
            ps.setString(2, name);
            ps.setDouble(3, budget);

            ps.executeUpdate();
            try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    return generatedKeys.getInt(1);
                }
            }
        } catch (SQLException e) {
            System.err.println("[ItineraryDao] Error in createItinerary: " + e.getMessage());
        }
        return -1;
    }

    public boolean updateBudget(int itineraryId, double budget) {
        String sql = "UPDATE itineraries SET budget = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setDouble(1, budget);
            ps.setInt(2, itineraryId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[ItineraryDao] Error in updateBudget: " + e.getMessage());
        }
        return false;
    }

    public boolean updateTotals(int itineraryId, double totalCost, int totalDuration, double budget) {
        String sql = "UPDATE itineraries SET total_estimated_cost = ?, total_duration_days = ?, budget = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setDouble(1, totalCost);
            ps.setInt(2, totalDuration);
            ps.setDouble(3, budget);
            ps.setInt(4, itineraryId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[ItineraryDao] Error in updateTotals: " + e.getMessage());
        }
        return false;
    }

    private ItineraryRecord mapResultSetToItinerary(ResultSet rs) throws SQLException {
        return new ItineraryRecord(
                rs.getInt("id"),
                rs.getInt("user_id"),
                rs.getString("name"),
                rs.getDouble("budget"),
                rs.getDouble("total_estimated_cost"),
                rs.getInt("total_duration_days"),
                rs.getString("status")
        );
    }
}
