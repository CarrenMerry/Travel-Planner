package com.travelplanner;

import com.travelplanner.persistence.DatabaseConnection;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

import static org.junit.jupiter.api.Assertions.*;

public class DatabaseConnectionTest {

    @Test
    @DisplayName("Verify MySQL JDBC Connection and SELECT 1 execution")
    void testJdbcConnectionAndSelect1() throws Exception {
        boolean testPassed = DatabaseConnection.testConnection();
        assertTrue(testPassed, "DatabaseConnection.testConnection() must succeed on localhost:3306");

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT 1 AS check_val")) {
            assertTrue(rs.next(), "ResultSet must contain at least one row");
            assertEquals(1, rs.getInt("check_val"), "Expected value 1 from SELECT 1");
        }
    }

    @Test
    @DisplayName("Inspect and print complete database schema and tables")
    void testPrintDatabaseSchema() throws Exception {
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement()) {

            System.out.println("=== TABLES IN smart_travel_planner ===");
            try (ResultSet rs = stmt.executeQuery("SHOW TABLES")) {
                while (rs.next()) {
                    String table = rs.getString(1);
                    System.out.println("TABLE: " + table);
                }
            }

            System.out.println("\n=== COLUMNS FOR ALL TABLES ===");
            try (ResultSet rs = stmt.executeQuery(
                    "SELECT TABLE_NAME, COLUMN_NAME, DATA_TYPE, IS_NULLABLE, COLUMN_KEY, COLUMN_DEFAULT " +
                    "FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = 'smart_travel_planner' ORDER BY TABLE_NAME, ORDINAL_POSITION")) {
                while (rs.next()) {
                    System.out.printf("  %s.%s (%s) Null:%s Key:%s Def:%s%n",
                            rs.getString("TABLE_NAME"),
                            rs.getString("COLUMN_NAME"),
                            rs.getString("DATA_TYPE"),
                            rs.getString("IS_NULLABLE"),
                            rs.getString("COLUMN_KEY"),
                            rs.getString("COLUMN_DEFAULT"));
                }
            }

            System.out.println("\n=== EXISTING USERS ===");
            try (ResultSet rs = stmt.executeQuery("SELECT id, name, email, role, password_hash FROM users")) {
                while (rs.next()) {
                    System.out.printf("  User #%d: Name='%s', Email='%s', Role=%s, HashPrefix=%s%n",
                            rs.getInt("id"),
                            rs.getString("name"),
                            rs.getString("email"),
                            rs.getString("role"),
                            rs.getString("password_hash").substring(0, Math.min(16, rs.getString("password_hash").length())));
                }
            }
            try (ResultSet rs = stmt.executeQuery(
                    "SELECT TABLE_NAME, COLUMN_NAME, CONSTRAINT_NAME, REFERENCED_TABLE_NAME, REFERENCED_COLUMN_NAME " +
                    "FROM INFORMATION_SCHEMA.KEY_COLUMN_USAGE " +
                    "WHERE TABLE_SCHEMA = 'smart_travel_planner' AND REFERENCED_TABLE_NAME IS NOT NULL")) {
                while (rs.next()) {
                    System.out.printf("  %s.%s -> %s.%s (%s)%n",
                            rs.getString("TABLE_NAME"),
                            rs.getString("COLUMN_NAME"),
                            rs.getString("REFERENCED_TABLE_NAME"),
                            rs.getString("REFERENCED_COLUMN_NAME"),
                            rs.getString("CONSTRAINT_NAME"));
                }
            }

            System.out.println("\n=== ROW COUNTS & SAMPLE DATA ===");
            for (String tbl : new String[]{"users", "destinations", "travel_connections", "itineraries", "itinerary_stops"}) {
                try (ResultSet countRs = stmt.executeQuery("SELECT COUNT(*) FROM " + tbl)) {
                    if (countRs.next()) {
                        System.out.printf("  %s: %d rows%n", tbl, countRs.getInt(1));
                    }
                } catch (Exception e) {
                    System.out.println("  " + tbl + ": " + e.getMessage());
                }
            }
        }
    }
}
