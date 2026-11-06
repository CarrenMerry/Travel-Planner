package com.wayfarer.dao;

import com.wayfarer.model.Itinerary;
import com.wayfarer.model.ItineraryStop;
import com.wayfarer.persistence.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Wayfarer — Data Access Object for 'itineraries' table in MySQL.
 */
public class ItineraryDAO {

    private final ItineraryStopDAO stopDAO = new ItineraryStopDAO();

    public List<Itinerary> findAllByUserId(int userId) {
        String sql = "SELECT id, user_id, name, budget, total_estimated_cost, total_duration_days, status, created_at, updated_at " +
                     "FROM itineraries WHERE user_id = ? ORDER BY id DESC";

        List<Itinerary> list = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("[ItineraryDAO] Error in findAllByUserId: " + e.getMessage());
        }
        return list;
    }

    public List<Itinerary> findAll() {
        String sql = "SELECT id, user_id, name, budget, total_estimated_cost, total_duration_days, status, created_at, updated_at " +
                     "FROM itineraries ORDER BY id DESC";

        List<Itinerary> list = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            System.err.println("[ItineraryDAO] Error in findAll: " + e.getMessage());
        }
        return list;
    }

    public Itinerary findById(int id) {
        String sql = "SELECT id, user_id, name, budget, total_estimated_cost, total_duration_days, status, created_at, updated_at " +
                     "FROM itineraries WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Itinerary it = mapRow(rs);
                    // Load stops into the custom linked list
                    List<ItineraryStop> stops = stopDAO.loadStopsForItinerary(id);
                    for (ItineraryStop s : stops) {
                        it.getStopsList().add(s);
                    }
                    it.recalculateTotals();
                    return it;
                }
            }
        } catch (SQLException e) {
            System.err.println("[ItineraryDAO] Error in findById: " + e.getMessage());
        }
        return null;
    }

    public int create(int userId, String name, double budget) {
        String sql = "INSERT INTO itineraries (user_id, name, budget, total_estimated_cost, total_duration_days, status) " +
                     "VALUES (?, ?, ?, 0.00, 0, 'DRAFT')";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, userId);
            ps.setString(2, name != null && !name.isBlank() ? name.trim() : "My Grand Journey");
            ps.setDouble(3, budget >= 0 ? budget : 0.00);

            int affected = ps.executeUpdate();
            if (affected > 0) {
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next()) return keys.getInt(1);
                }
            }
        } catch (SQLException e) {
            System.err.println("[ItineraryDAO] Error creating itinerary: " + e.getMessage());
        }
        return -1;
    }

    public boolean updateHeader(int id, String name, double budget, String status) {
        String sql = "UPDATE itineraries SET name = ?, budget = ?, status = ?, updated_at = CURRENT_TIMESTAMP WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, name);
            ps.setDouble(2, budget);
            ps.setString(3, status != null ? status : "DRAFT");
            ps.setInt(4, id);

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[ItineraryDAO] Error updating itinerary: " + e.getMessage());
        }
        return false;
    }

    public boolean delete(int id, int userId) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement delStops = conn.prepareStatement("DELETE FROM itinerary_stops WHERE itinerary_id = ?");
                 PreparedStatement delIt = conn.prepareStatement(userId > 0 ?
                         "DELETE FROM itineraries WHERE id = ? AND user_id = ?" :
                         "DELETE FROM itineraries WHERE id = ?")) {

                delStops.setInt(1, id);
                delStops.executeUpdate();

                delIt.setInt(1, id);
                if (userId > 0) {
                    delIt.setInt(2, userId);
                }

                int affected = delIt.executeUpdate();
                conn.commit();
                return affected > 0;
            } catch (SQLException ex) {
                conn.rollback();
                throw ex;
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            System.err.println("[ItineraryDAO] Error deleting itinerary: " + e.getMessage());
        }
        return false;
    }

    public int count() {
        String sql = "SELECT COUNT(*) FROM itineraries";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            System.err.println("[ItineraryDAO] Error in count: " + e.getMessage());
        }
        return 0;
    }

    private Itinerary mapRow(ResultSet rs) throws SQLException {
        Itinerary it = new Itinerary(
                rs.getInt("id"),
                rs.getInt("user_id"),
                rs.getString("name"),
                rs.getDouble("budget"),
                rs.getDouble("total_estimated_cost"),
                rs.getInt("total_duration_days"),
                rs.getString("status")
        );
        it.setCreatedAt(rs.getTimestamp("created_at"));
        it.setUpdatedAt(rs.getTimestamp("updated_at"));
        return it;
    }
}
