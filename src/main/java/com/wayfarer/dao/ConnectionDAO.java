package com.wayfarer.dao;

import com.wayfarer.model.TravelConnection;
import com.wayfarer.persistence.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Wayfarer — Data Access Object for 'travel_connections' table in MySQL.
 */
public class ConnectionDAO {

    public List<TravelConnection> findAll() {
        String sql = "SELECT c.id, c.source_destination_id, c.target_destination_id, " +
                     "s.destination_code AS source_code, t.destination_code AS target_code, " +
                     "s.name AS source_name, t.name AS target_name, " +
                     "c.distance_km, c.estimated_travel_cost, c.estimated_travel_minutes " +
                     "FROM travel_connections c " +
                     "JOIN destinations s ON c.source_destination_id = s.id " +
                     "JOIN destinations t ON c.target_destination_id = t.id " +
                     "ORDER BY c.id ASC";

        List<TravelConnection> list = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                TravelConnection tc = new TravelConnection(
                        rs.getInt("id"),
                        rs.getInt("source_destination_id"),
                        rs.getInt("target_destination_id"),
                        rs.getString("source_code"),
                        rs.getString("target_code"),
                        rs.getDouble("distance_km"),
                        rs.getDouble("estimated_travel_cost"),
                        rs.getObject("estimated_travel_minutes") != null ? rs.getInt("estimated_travel_minutes") : null
                );
                tc.setSourceName(rs.getString("source_name"));
                tc.setTargetName(rs.getString("target_name"));
                list.add(tc);
            }
        } catch (SQLException e) {
            System.err.println("[ConnectionDAO] Error in findAll: " + e.getMessage());
        }
        return list;
    }

    public boolean insert(int sourceId, int targetId, double distanceKm, double cost, Integer minutes) {
        String sql = "INSERT INTO travel_connections (source_destination_id, target_destination_id, " +
                     "distance_km, estimated_travel_cost, estimated_travel_minutes) " +
                     "VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, sourceId);
            ps.setInt(2, targetId);
            ps.setDouble(3, distanceKm);
            ps.setDouble(4, cost);
            if (minutes != null) {
                ps.setInt(5, minutes);
            } else {
                ps.setNull(5, Types.INTEGER);
            }

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[ConnectionDAO] Error inserting connection: " + e.getMessage());
        }
        return false;
    }

    public boolean update(int id, double distanceKm, double cost, Integer minutes) {
        String sql = "UPDATE travel_connections SET distance_km = ?, estimated_travel_cost = ?, " +
                     "estimated_travel_minutes = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setDouble(1, distanceKm);
            ps.setDouble(2, cost);
            if (minutes != null) {
                ps.setInt(3, minutes);
            } else {
                ps.setNull(3, Types.INTEGER);
            }
            ps.setInt(4, id);

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[ConnectionDAO] Error updating connection: " + e.getMessage());
        }
        return false;
    }

    public boolean delete(int id) {
        String sql = "DELETE FROM travel_connections WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[ConnectionDAO] Error deleting connection: " + e.getMessage());
        }
        return false;
    }

    public int count() {
        String sql = "SELECT COUNT(*) FROM travel_connections";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            System.err.println("[ConnectionDAO] Error in count: " + e.getMessage());
        }
        return 0;
    }
}
