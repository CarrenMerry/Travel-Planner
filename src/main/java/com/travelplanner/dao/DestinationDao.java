package com.travelplanner.dao;

import com.travelplanner.model.Destination;
import com.travelplanner.persistence.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for the 'destinations' table in MySQL.
 * Uses PreparedStatements and try-with-resources for secure resource management.
 */
public class DestinationDao {

    public List<Destination> findAll() {
        String sql = "SELECT id, destination_code, name, country, city, description, " +
                     "estimated_cost, duration_days, activities_count, rating, image_url " +
                     "FROM destinations ORDER BY id ASC";
        List<Destination> list = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(mapResultSetToDestination(rs));
            }
        } catch (SQLException e) {
            System.err.println("[DestinationDao] Error in findAll: " + e.getMessage());
        }
        return list;
    }

    public Destination findById(int id) {
        String sql = "SELECT id, destination_code, name, country, city, description, " +
                     "estimated_cost, duration_days, activities_count, rating, image_url " +
                     "FROM destinations WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToDestination(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("[DestinationDao] Error in findById: " + e.getMessage());
        }
        return null;
    }

    public Destination findByCode(String code) {
        if (code == null || code.trim().isEmpty()) return null;
        String sql = "SELECT id, destination_code, name, country, city, description, " +
                     "estimated_cost, duration_days, activities_count, rating, image_url " +
                     "FROM destinations WHERE destination_code = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, code.trim());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToDestination(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("[DestinationDao] Error in findByCode: " + e.getMessage());
        }
        return null;
    }

    public Destination findByIdOrCode(String identifier) {
        if (identifier == null || identifier.trim().isEmpty()) return null;
        String trimmed = identifier.trim();

        // 1. Try by destination_code
        Destination d = findByCode(trimmed);
        if (d != null) return d;

        // 2. If identifier is numeric, try by id
        try {
            int numId = Integer.parseInt(trimmed);
            return findById(numId);
        } catch (NumberFormatException ignored) {}

        return null;
    }

    public List<Destination> search(String query) {
        if (query == null || query.trim().isEmpty()) {
            return findAll();
        }
        String sql = "SELECT id, destination_code, name, country, city, description, " +
                     "estimated_cost, duration_days, activities_count, rating, image_url " +
                     "FROM destinations WHERE name LIKE ? OR country LIKE ? OR city LIKE ? OR destination_code LIKE ? " +
                     "ORDER BY name ASC";
        List<Destination> list = new ArrayList<>();
        String pattern = "%" + query.trim() + "%";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, pattern);
            ps.setString(2, pattern);
            ps.setString(3, pattern);
            ps.setString(4, pattern);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToDestination(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("[DestinationDao] Error in search: " + e.getMessage());
        }
        return list;
    }

    public List<Destination> filter(String country, Double maxCost, Double minRating) {
        StringBuilder sql = new StringBuilder("SELECT id, destination_code, name, country, city, description, " +
                "estimated_cost, duration_days, activities_count, rating, image_url FROM destinations WHERE 1=1 ");

        List<Object> params = new ArrayList<>();
        if (country != null && !country.trim().isEmpty() && !country.equalsIgnoreCase("all")) {
            sql.append("AND country = ? ");
            params.add(country.trim());
        }
        if (maxCost != null && maxCost > 0) {
            sql.append("AND estimated_cost <= ? ");
            params.add(maxCost);
        }
        if (minRating != null && minRating > 0) {
            sql.append("AND rating >= ? ");
            params.add(minRating);
        }
        sql.append("ORDER BY id ASC");

        List<Destination> list = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {

            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToDestination(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("[DestinationDao] Error in filter: " + e.getMessage());
        }
        return list;
    }

    public boolean insert(Destination d) {
        if (d == null) return false;

        String sql = "INSERT INTO destinations (destination_code, name, country, city, description, " +
                     "estimated_cost, duration_days, activities_count, rating, image_url) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        String code = d.getDestinationCode();
        if (code == null || code.trim().isEmpty()) {
            code = "DEST-" + String.format("%02d", count() + 1);
            d.setDestinationCode(code);
        }

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, code);
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
                try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        d.setDbId(generatedKeys.getInt(1));
                    }
                }
                return true;
            }
        } catch (SQLException e) {
            System.err.println("[DestinationDao] Error in insert: " + e.getMessage());
        }
        return false;
    }

    public boolean update(Destination d) {
        if (d == null) return false;

        String sql = "UPDATE destinations SET name = ?, country = ?, city = ?, description = ?, " +
                     "estimated_cost = ?, duration_days = ?, activities_count = ?, rating = ?, image_url = ? " +
                     "WHERE destination_code = ? OR id = ?";

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
            ps.setString(10, d.getDestinationCode());
            ps.setInt(11, d.getDbId());

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[DestinationDao] Error in update: " + e.getMessage());
        }
        return false;
    }

    public boolean delete(int id) {
        String sql = "DELETE FROM destinations WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[DestinationDao] Error in delete: " + e.getMessage());
        }
        return false;
    }

    public boolean deleteByCode(String code) {
        if (code == null) return false;
        String sql = "DELETE FROM destinations WHERE destination_code = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, code.trim());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[DestinationDao] Error in deleteByCode: " + e.getMessage());
        }
        return false;
    }

    public int count() {
        String sql = "SELECT COUNT(*) FROM destinations";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("[DestinationDao] Error in count: " + e.getMessage());
        }
        return 0;
    }

    public double calculateAverageRating() {
        String sql = "SELECT AVG(rating) FROM destinations";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            if (rs.next()) {
                return rs.getDouble(1);
            }
        } catch (SQLException e) {
            System.err.println("[DestinationDao] Error in calculateAverageRating: " + e.getMessage());
        }
        return 0.0;
    }

    private Destination mapResultSetToDestination(ResultSet rs) throws SQLException {
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

        if (img == null || img.trim().isEmpty()) {
            img = getDefaultImageForDestination(code, city, country);
        }

        return new Destination(dbId, code, name, country, city, desc, cost, duration, activities, rating, img);
    }

    private String getDefaultImageForDestination(String code, String city, String country) {
        String query = (city != null && !city.isEmpty()) ? city : (country != null ? country : code);
        query = query.toLowerCase();
        if (query.contains("tokyo") || query.contains("japan")) {
            return "https://images.unsplash.com/photo-1503899036084-c55cdd92da26?auto=format&fit=crop&w=800&q=80";
        } else if (query.contains("paris") || query.contains("france")) {
            return "https://images.unsplash.com/photo-1502602898657-3e91760cbb34?auto=format&fit=crop&w=800&q=80";
        } else if (query.contains("rome") || query.contains("italy")) {
            return "https://images.unsplash.com/photo-1552832230-c0197dd311b5?auto=format&fit=crop&w=800&q=80";
        } else if (query.contains("bali") || query.contains("indonesia")) {
            return "https://images.unsplash.com/photo-1537996194471-e657df975ab4?auto=format&fit=crop&w=800&q=80";
        } else if (query.contains("new york") || query.contains("usa")) {
            return "https://images.unsplash.com/photo-1496442226666-8d4d0e62e6e9?auto=format&fit=crop&w=800&q=80";
        } else if (query.contains("cairo") || query.contains("egypt") || query.contains("giza")) {
            return "https://images.unsplash.com/photo-1503177119275-0aa32b3a9368?auto=format&fit=crop&w=800&q=80";
        } else if (query.contains("cape town") || query.contains("south africa")) {
            return "https://images.unsplash.com/photo-1580618672591-eb180b1a973f?auto=format&fit=crop&w=800&q=80";
        } else if (query.contains("sydney") || query.contains("australia")) {
            return "https://images.unsplash.com/photo-1506973035872-a4ec16b8e8d9?auto=format&fit=crop&w=800&q=80";
        } else if (query.contains("barcelona") || query.contains("spain")) {
            return "https://images.unsplash.com/photo-1583422409516-2895a77efded?auto=format&fit=crop&w=800&q=80";
        }
        return "https://images.unsplash.com/photo-1488646953014-85cb44e25828?auto=format&fit=crop&w=800&q=80";
    }
}
