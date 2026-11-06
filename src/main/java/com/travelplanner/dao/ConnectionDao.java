package com.travelplanner.dao;

import com.travelplanner.model.Destination;
import com.travelplanner.persistence.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for the 'travel_connections' table in MySQL.
 * Synchronizes persistent graph edges with the custom in-memory adjacency list graph.
 */
public class ConnectionDao {

    private final DestinationDao destinationDao = new DestinationDao();

    public static class ConnectionRecord {
        public int id;
        public int sourceDestinationId;
        public int targetDestinationId;
        public String sourceCode;
        public String targetCode;
        public double distanceKm;
        public double estimatedTravelCost;
        public Integer estimatedTravelMinutes;

        public ConnectionRecord(int id, int sourceDestinationId, int targetDestinationId,
                                String sourceCode, String targetCode,
                                double distanceKm, double estimatedTravelCost, Integer estimatedTravelMinutes) {
            this.id = id;
            this.sourceDestinationId = sourceDestinationId;
            this.targetDestinationId = targetDestinationId;
            this.sourceCode = sourceCode;
            this.targetCode = targetCode;
            this.distanceKm = distanceKm;
            this.estimatedTravelCost = estimatedTravelCost;
            this.estimatedTravelMinutes = estimatedTravelMinutes;
        }
    }

    public List<ConnectionRecord> findAll() {
        String sql = "SELECT c.id, c.source_destination_id, c.target_destination_id, " +
                     "s.destination_code AS source_code, t.destination_code AS target_code, " +
                     "c.distance_km, c.estimated_travel_cost, c.estimated_travel_minutes " +
                     "FROM travel_connections c " +
                     "JOIN destinations s ON c.source_destination_id = s.id " +
                     "JOIN destinations t ON c.target_destination_id = t.id " +
                     "ORDER BY c.id ASC";
        List<ConnectionRecord> list = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(new ConnectionRecord(
                        rs.getInt("id"),
                        rs.getInt("source_destination_id"),
                        rs.getInt("target_destination_id"),
                        rs.getString("source_code"),
                        rs.getString("target_code"),
                        rs.getDouble("distance_km"),
                        rs.getDouble("estimated_travel_cost"),
                        (Integer) rs.getObject("estimated_travel_minutes")
                ));
            }
        } catch (SQLException e) {
            System.err.println("[ConnectionDao] Error in findAll: " + e.getMessage());
        }
        return list;
    }

    public boolean addConnection(String sourceIdentifier, String targetIdentifier, double distanceKm, double travelCost) {
        Destination source = destinationDao.findByIdOrCode(sourceIdentifier);
        Destination target = destinationDao.findByIdOrCode(targetIdentifier);

        if (source == null || target == null) {
            System.err.println("[ConnectionDao] Cannot add connection: Unknown source or target (" + sourceIdentifier + " -> " + targetIdentifier + ")");
            return false;
        }
        if (source.getDbId() == target.getDbId()) {
            System.err.println("[ConnectionDao] Source and target cannot be the same destination.");
            return false;
        }

        // Insert edge bidirectionally to match undirected graph model, ignoring if row already exists
        String sql = "INSERT INTO travel_connections (source_destination_id, target_destination_id, distance_km, estimated_travel_cost, estimated_travel_minutes) " +
                     "VALUES (?, ?, ?, ?, ?) " +
                     "ON DUPLICATE KEY UPDATE distance_km = VALUES(distance_km), estimated_travel_cost = VALUES(estimated_travel_cost)";

        int estMinutes = (int) Math.round((distanceKm / 800.0) * 60 + 90); // heuristic flight minutes

        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                // Forward
                ps.setInt(1, source.getDbId());
                ps.setInt(2, target.getDbId());
                ps.setDouble(3, distanceKm);
                ps.setDouble(4, travelCost);
                ps.setInt(5, estMinutes);
                ps.executeUpdate();

                // Reverse
                ps.setInt(1, target.getDbId());
                ps.setInt(2, source.getDbId());
                ps.setDouble(3, distanceKm);
                ps.setDouble(4, travelCost);
                ps.setInt(5, estMinutes);
                ps.executeUpdate();

                conn.commit();
                return true;
            } catch (SQLException e) {
                conn.rollback();
                System.err.println("[ConnectionDao] Error adding connection: " + e.getMessage());
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            System.err.println("[ConnectionDao] Connection error: " + e.getMessage());
        }
        return false;
    }

    public boolean removeConnection(String sourceIdentifier, String targetIdentifier) {
        Destination source = destinationDao.findByIdOrCode(sourceIdentifier);
        Destination target = destinationDao.findByIdOrCode(targetIdentifier);

        if (source == null || target == null) {
            return false;
        }

        String sql = "DELETE FROM travel_connections WHERE (source_destination_id = ? AND target_destination_id = ?) " +
                     "OR (source_destination_id = ? AND target_destination_id = ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, source.getDbId());
            ps.setInt(2, target.getDbId());
            ps.setInt(3, target.getDbId());
            ps.setInt(4, source.getDbId());

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[ConnectionDao] Error in removeConnection: " + e.getMessage());
        }
        return false;
    }

    public int count() {
        String sql = "SELECT COUNT(*) FROM travel_connections";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("[ConnectionDao] Error in count: " + e.getMessage());
        }
        return 0;
    }
}
