package com.wayfarer.dao;

import com.wayfarer.model.Destination;
import com.wayfarer.model.ItineraryStop;
import com.wayfarer.persistence.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Wayfarer — Data Access Object for 'itinerary_stops' table in MySQL.
 * Employs ACID database transactions for atomic persistence of ordered stops.
 */
public class ItineraryStopDAO {

    public List<ItineraryStop> loadStopsForItinerary(int itineraryId) {
        String sql = "SELECT s.id AS stop_id, s.itinerary_id, s.destination_id, s.stop_order, " +
                     "s.estimated_cost AS stop_cost, s.duration_days AS stop_duration, s.notes, s.created_at AS stop_created_at, " +
                     "d.id AS dest_id, d.destination_code, d.name, d.country, d.city, d.description, " +
                     "d.estimated_cost, d.duration_days, d.activities_count, d.rating, d.image_url " +
                     "FROM itinerary_stops s " +
                     "JOIN destinations d ON s.destination_id = d.id " +
                     "WHERE s.itinerary_id = ? " +
                     "ORDER BY s.stop_order ASC";

        List<ItineraryStop> stops = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, itineraryId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ItineraryStop stop = new ItineraryStop(
                            rs.getInt("stop_id"),
                            rs.getInt("itinerary_id"),
                            rs.getInt("destination_id"),
                            rs.getInt("stop_order"),
                            rs.getDouble("stop_cost"),
                            rs.getInt("stop_duration"),
                            rs.getString("notes")
                    );
                    stop.setCreatedAt(rs.getTimestamp("stop_created_at"));

                    Destination d = new Destination(
                            rs.getInt("dest_id"),
                            rs.getString("destination_code"),
                            rs.getString("name"),
                            rs.getString("country"),
                            rs.getString("city"),
                            rs.getString("description"),
                            rs.getDouble("estimated_cost"),
                            rs.getInt("duration_days"),
                            rs.getInt("activities_count"),
                            rs.getDouble("rating"),
                            rs.getString("image_url")
                    );
                    stop.setDestination(d);
                    stops.add(stop);
                }
            }
        } catch (SQLException e) {
            System.err.println("[ItineraryStopDAO] Error loading stops: " + e.getMessage());
        }
        return stops;
    }

    public boolean saveStopsAtomically(int itineraryId, List<ItineraryStop> stops) {
        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false);

            // 1. Clear existing stops for this itinerary
            try (PreparedStatement delPs = conn.prepareStatement("DELETE FROM itinerary_stops WHERE itinerary_id = ?")) {
                delPs.setInt(1, itineraryId);
                delPs.executeUpdate();
            }

            // 2. Insert new sequence with continuous stop_order
            String insertSql = "INSERT INTO itinerary_stops (itinerary_id, destination_id, stop_order, estimated_cost, duration_days, notes) " +
                               "VALUES (?, ?, ?, ?, ?, ?)";
            double totalCost = 0.0;
            int totalDays = 0;

            try (PreparedStatement insPs = conn.prepareStatement(insertSql)) {
                for (int i = 0; i < stops.size(); i++) {
                    ItineraryStop stop = stops.get(i);
                    int order = i + 1;

                    insPs.setInt(1, itineraryId);
                    insPs.setInt(2, stop.getDestinationId());
                    insPs.setInt(3, order);
                    insPs.setDouble(4, stop.getEstimatedCost());
                    insPs.setInt(5, stop.getDurationDays());
                    insPs.setString(6, stop.getNotes());
                    insPs.addBatch();

                    totalCost += stop.getEstimatedCost();
                    totalDays += stop.getDurationDays();
                }
                insPs.executeBatch();
            }

            // 3. Atomically update parent itinerary totals
            String updateHeaderSql = "UPDATE itineraries SET total_estimated_cost = ?, total_duration_days = ?, updated_at = CURRENT_TIMESTAMP WHERE id = ?";
            try (PreparedStatement updPs = conn.prepareStatement(updateHeaderSql)) {
                updPs.setDouble(1, totalCost);
                updPs.setInt(2, totalDays);
                updPs.setInt(3, itineraryId);
                updPs.executeUpdate();
            }

            conn.commit();
            return true;
        } catch (SQLException e) {
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ignored) {}
            }
            System.err.println("[ItineraryStopDAO] Transaction error saving stops: " + e.getMessage());
            return false;
        } finally {
            if (conn != null) {
                try { conn.setAutoCommit(true); conn.close(); } catch (SQLException ignored) {}
            }
        }
    }
}
