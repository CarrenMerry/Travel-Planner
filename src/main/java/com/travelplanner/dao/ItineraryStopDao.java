package com.travelplanner.dao;

import com.travelplanner.model.Destination;
import com.travelplanner.persistence.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for 'itinerary_stops' table in MySQL.
 * Employs ACID transactions when updating, adding, removing, or reordering stops
 * to avoid unique constraint collisions on (itinerary_id, stop_order).
 */
public class ItineraryStopDao {

    private final DestinationDao destinationDao = new DestinationDao();

    public List<Destination> loadOrderedStops(int itineraryId) {
        String sql = "SELECT s.id AS stop_id, s.itinerary_id, s.destination_id, s.stop_order, " +
                     "s.estimated_cost AS stop_cost, s.duration_days AS stop_duration, s.notes, " +
                     "d.id, d.destination_code, d.name, d.country, d.city, d.description, " +
                     "d.estimated_cost, d.duration_days, d.activities_count, d.rating, d.image_url " +
                     "FROM itinerary_stops s " +
                     "JOIN destinations d ON s.destination_id = d.id " +
                     "WHERE s.itinerary_id = ? " +
                     "ORDER BY s.stop_order ASC";

        List<Destination> list = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, itineraryId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int dbId = rs.getInt("id");
                    String code = rs.getString("destination_code");
                    String name = rs.getString("name");
                    String country = rs.getString("country");
                    String city = rs.getString("city");
                    String desc = rs.getString("description");
                    double cost = rs.getDouble("estimated_cost");
                    int duration = rs.getInt("duration_days");
                    int activities = rs.getInt("activities_count");
                    double rating = rs.getDouble("rating");
                    String img = rs.getString("image_url");

                    Destination d = new Destination(dbId, code, name, country, city, desc, cost, duration, activities, rating, img);
                    list.add(d);
                }
            }
        } catch (SQLException e) {
            System.err.println("[ItineraryStopDao] Error loading stops: " + e.getMessage());
        }
        return list;
    }

    /**
     * Atomically saves the complete sequence of stops in an ACID database transaction.
     * Prevents unique constraint conflicts and updates totals on the parent itinerary.
     */
    public boolean saveAllStops(int itineraryId, List<Destination> stops, double budget) {
        String deleteSql = "DELETE FROM itinerary_stops WHERE itinerary_id = ?";
        String insertSql = "INSERT INTO itinerary_stops (itinerary_id, destination_id, stop_order, estimated_cost, duration_days, notes) " +
                           "VALUES (?, ?, ?, ?, ?, ?)";
        String updateItinerarySql = "UPDATE itineraries SET total_estimated_cost = ?, total_duration_days = ?, budget = ? WHERE id = ?";

        double totalCost = 0.0;
        int totalDuration = 0;

        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                // 1. Delete existing stops for this itinerary
                try (PreparedStatement delPs = conn.prepareStatement(deleteSql)) {
                    delPs.setInt(1, itineraryId);
                    delPs.executeUpdate();
                }

                // 2. Insert new stops in strict sequential order
                if (stops != null && !stops.isEmpty()) {
                    try (PreparedStatement insPs = conn.prepareStatement(insertSql)) {
                        int order = 1;
                        for (Destination d : stops) {
                            // Resolve DB ID if needed
                            int targetDbId = d.getDbId();
                            if (targetDbId <= 0) {
                                Destination resolved = destinationDao.findByIdOrCode(d.getId());
                                if (resolved != null) {
                                    targetDbId = resolved.getDbId();
                                    d.setDbId(targetDbId);
                                }
                            }

                            if (targetDbId > 0) {
                                insPs.setInt(1, itineraryId);
                                insPs.setInt(2, targetDbId);
                                insPs.setInt(3, order++);
                                insPs.setDouble(4, d.getEstimatedCost());
                                insPs.setInt(5, d.getDurationDays());
                                insPs.setString(6, "Stop at " + d.getName());
                                insPs.addBatch();

                                totalCost += d.getEstimatedCost();
                                totalDuration += d.getDurationDays();
                            }
                        }
                        insPs.executeBatch();
                    }
                }

                // 3. Update itinerary record totals
                try (PreparedStatement updPs = conn.prepareStatement(updateItinerarySql)) {
                    updPs.setDouble(1, totalCost);
                    updPs.setInt(2, totalDuration);
                    updPs.setDouble(3, budget);
                    updPs.setInt(4, itineraryId);
                    updPs.executeUpdate();
                }

                conn.commit();
                return true;
            } catch (SQLException ex) {
                conn.rollback();
                System.err.println("[ItineraryStopDao] Transaction rolled back due to error: " + ex.getMessage());
                throw ex;
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            System.err.println("[ItineraryStopDao] Error executing saveAllStops transaction: " + e.getMessage());
            return false;
        }
    }
}
