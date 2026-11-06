package com.wayfarer.dao;

import com.wayfarer.model.Destination;
import com.wayfarer.persistence.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Wayfarer — Data Access Object for 'destinations' table in MySQL.
 */
public class DestinationDAO {

    public List<Destination> findAll() {
        String sql = "SELECT id, destination_code, name, country, city, category, description, " +
                     "estimated_cost, duration_days, activities_count, rating, image_url, created_at " +
                     "FROM destinations ORDER BY id ASC";
        List<Destination> list = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            // Fallback if category column is absent
            return findAllFallback();
        }
        return list;
    }

    private List<Destination> findAllFallback() {
        String sql = "SELECT id, destination_code, name, country, city, description, " +
                     "estimated_cost, duration_days, activities_count, rating, image_url, created_at " +
                     "FROM destinations ORDER BY id ASC";
        List<Destination> list = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(mapRowWithoutCategory(rs));
            }
        } catch (SQLException e) {
            System.err.println("[DestinationDAO] Error in findAll: " + e.getMessage());
        }
        return list;
    }

    public Destination findById(int id) {
        String sql = "SELECT id, destination_code, name, country, city, category, description, " +
                     "estimated_cost, duration_days, activities_count, rating, image_url, created_at " +
                     "FROM destinations WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapRow(rs);
            }
        } catch (SQLException e) {
            return findByIdFallback(id);
        }
        return null;
    }

    private Destination findByIdFallback(int id) {
        String sql = "SELECT id, destination_code, name, country, city, description, " +
                     "estimated_cost, duration_days, activities_count, rating, image_url, created_at " +
                     "FROM destinations WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapRowWithoutCategory(rs);
            }
        } catch (SQLException e) {
            System.err.println("[DestinationDAO] Error in findById: " + e.getMessage());
        }
        return null;
    }

    public Destination findByCode(String code) {
        if (code == null || code.trim().isEmpty()) return null;
        String sql = "SELECT id, destination_code, name, country, city, category, description, " +
                     "estimated_cost, duration_days, activities_count, rating, image_url, created_at " +
                     "FROM destinations WHERE UPPER(destination_code) = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, code.trim().toUpperCase());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapRow(rs);
            }
        } catch (SQLException e) {
            return findByCodeFallback(code);
        }
        return null;
    }

    private Destination findByCodeFallback(String code) {
        String sql = "SELECT id, destination_code, name, country, city, description, " +
                     "estimated_cost, duration_days, activities_count, rating, image_url, created_at " +
                     "FROM destinations WHERE UPPER(destination_code) = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, code.trim().toUpperCase());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapRowWithoutCategory(rs);
            }
        } catch (SQLException e) {
            System.err.println("[DestinationDAO] Error in findByCode: " + e.getMessage());
        }
        return null;
    }

    public boolean insert(Destination d) {
        String sqlWithCategory = "INSERT INTO destinations (destination_code, name, country, city, category, description, " +
                                 "estimated_cost, duration_days, activities_count, rating, image_url) " +
                                 "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sqlWithCategory, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, d.getDestinationCode().toUpperCase());
            ps.setString(2, d.getName());
            ps.setString(3, d.getCountry());
            ps.setString(4, d.getCity());
            ps.setString(5, d.getCategory() != null ? d.getCategory() : "Cultural");
            ps.setString(6, d.getDescription());
            ps.setDouble(7, d.getEstimatedCost());
            ps.setInt(8, d.getDurationDays());
            ps.setInt(9, d.getActivitiesCount());
            ps.setDouble(10, d.getRating());
            ps.setString(11, d.getImageUrl());

            int affected = ps.executeUpdate();
            if (affected > 0) {
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next()) d.setId(keys.getInt(1));
                }
                return true;
            }
        } catch (SQLException e) {
            // Fallback without category
            return insertFallback(d);
        }
        return false;
    }

    private boolean insertFallback(Destination d) {
        String sql = "INSERT INTO destinations (destination_code, name, country, city, description, " +
                     "estimated_cost, duration_days, activities_count, rating, image_url) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, d.getDestinationCode().toUpperCase());
            ps.setString(2, d.getName());
            ps.setString(3, d.getCountry());
            ps.setString(4, d.getCity());
            ps.setString(5, d.getDescription());
            ps.setDouble(6, d.getEstimatedCost());
            ps.setInt(7, d.getDurationDays());
            ps.setInt(8, d.getActivitiesCount());
            ps.setDouble(9, d.getRating());
            ps.setString(10, d.getImageUrl());

            int affected = ps.executeUpdate();
            if (affected > 0) {
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next()) d.setId(keys.getInt(1));
                }
                return true;
            }
        } catch (SQLException e) {
            System.err.println("[DestinationDAO] Error inserting destination: " + e.getMessage());
        }
        return false;
    }

    public boolean update(Destination d) {
        String sql = "UPDATE destinations SET name = ?, country = ?, city = ?, category = ?, description = ?, " +
                     "estimated_cost = ?, duration_days = ?, activities_count = ?, rating = ?, image_url = ? " +
                     "WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, d.getName());
            ps.setString(2, d.getCountry());
            ps.setString(3, d.getCity());
            ps.setString(4, d.getCategory());
            ps.setString(5, d.getDescription());
            ps.setDouble(6, d.getEstimatedCost());
            ps.setInt(7, d.getDurationDays());
            ps.setInt(8, d.getActivitiesCount());
            ps.setDouble(9, d.getRating());
            ps.setString(10, d.getImageUrl());
            ps.setInt(11, d.getId());

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            return updateFallback(d);
        }
    }

    private boolean updateFallback(Destination d) {
        String sql = "UPDATE destinations SET name = ?, country = ?, city = ?, description = ?, " +
                     "estimated_cost = ?, duration_days = ?, activities_count = ?, rating = ?, image_url = ? " +
                     "WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, d.getName());
            ps.setString(2, d.getCountry());
            ps.setString(3, d.getCity());
            ps.setString(4, d.getDescription());
            ps.setDouble(5, d.getEstimatedCost());
            ps.setInt(6, d.getDurationDays());
            ps.setInt(7, d.getActivitiesCount());
            ps.setDouble(8, d.getRating());
            ps.setString(9, d.getImageUrl());
            ps.setInt(10, d.getId());

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[DestinationDAO] Error updating destination: " + e.getMessage());
        }
        return false;
    }

    public boolean delete(int id) {
        // First delete child records (foreign keys in travel_connections and itinerary_stops)
        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement ps1 = conn.prepareStatement("DELETE FROM itinerary_stops WHERE destination_id = ?");
                 PreparedStatement ps2 = conn.prepareStatement("DELETE FROM travel_connections WHERE source_destination_id = ? OR target_destination_id = ?");
                 PreparedStatement ps3 = conn.prepareStatement("DELETE FROM destinations WHERE id = ?")) {

                ps1.setInt(1, id);
                ps1.executeUpdate();

                ps2.setInt(1, id);
                ps2.setInt(2, id);
                ps2.executeUpdate();

                ps3.setInt(1, id);
                int affected = ps3.executeUpdate();

                conn.commit();
                return affected > 0;
            } catch (SQLException ex) {
                conn.rollback();
                throw ex;
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            System.err.println("[DestinationDAO] Error deleting destination: " + e.getMessage());
        }
        return false;
    }

    public int count() {
        String sql = "SELECT COUNT(*) FROM destinations";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            System.err.println("[DestinationDAO] Error in count: " + e.getMessage());
        }
        return 0;
    }

    private Destination mapRow(ResultSet rs) throws SQLException {
        Destination d = new Destination(
                rs.getInt("id"),
                rs.getString("destination_code"),
                rs.getString("name"),
                rs.getString("country"),
                rs.getString("city"),
                rs.getString("category"),
                rs.getString("description"),
                rs.getDouble("estimated_cost"),
                rs.getInt("duration_days"),
                rs.getInt("activities_count"),
                rs.getDouble("rating"),
                rs.getString("image_url")
        );
        d.setCreatedAt(rs.getTimestamp("created_at"));
        return d;
    }

    private Destination mapRowWithoutCategory(ResultSet rs) throws SQLException {
        Destination d = new Destination(
                rs.getInt("id"),
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
        d.setCategory("Cultural");
        d.setCreatedAt(rs.getTimestamp("created_at"));
        return d;
    }
}
