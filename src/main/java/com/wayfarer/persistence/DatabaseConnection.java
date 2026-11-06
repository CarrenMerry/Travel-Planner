package com.wayfarer.persistence;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

/**
 * Wayfarer — Smart Travel Planner
 * Centralized JDBC Database Connection Manager.
 * Safely connects to the existing 'smart_travel_planner' MySQL database
 * using configurations from db.properties, environment variables, or system properties.
 */
public class DatabaseConnection {

    private static final String DEFAULT_HOST = "localhost";
    private static final String DEFAULT_PORT = "3306";
    private static final String DEFAULT_DB = "smart_travel_planner";
    private static final String DEFAULT_USER = "root";

    private static String jdbcUrl;
    private static String dbUser;
    private static String dbPassword;
    private static boolean initialized = false;

    static {
        loadConfiguration();
    }

    public static synchronized void loadConfiguration() {
        Properties props = new Properties();

        File file = new File("db.properties");
        if (file.exists()) {
            try (InputStream in = new FileInputStream(file)) {
                props.load(in);
            } catch (Exception e) {
                System.err.println("[DatabaseConnection] Notice: Could not read local db.properties: " + e.getMessage());
            }
        } else {
            try (InputStream in = DatabaseConnection.class.getClassLoader().getResourceAsStream("db.properties")) {
                if (in != null) {
                    props.load(in);
                }
            } catch (Exception ignored) {}
        }

        String host = getVal("DB_HOST", "db.host", props, DEFAULT_HOST);
        String port = getVal("DB_PORT", "db.port", props, DEFAULT_PORT);
        String dbName = getVal("DB_NAME", "db.name", props, DEFAULT_DB);
        dbUser = getVal("DB_USER", "db.user", props, DEFAULT_USER);
        dbPassword = getVal("DB_PASSWORD", "db.password", props, "");

        String useSSL = getVal("DB_USE_SSL", "db.useSSL", props, "false");
        String allowPk = getVal("DB_ALLOW_PUBLIC_KEY", "db.allowPublicKeyRetrieval", props, "true");
        String timezone = getVal("DB_SERVER_TIMEZONE", "db.serverTimezone", props, "UTC");

        jdbcUrl = String.format("jdbc:mysql://%s:%s/%s?useSSL=%s&allowPublicKeyRetrieval=%s&serverTimezone=%s&characterEncoding=UTF-8",
                host, port, dbName, useSSL, allowPk, timezone);

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            initialized = true;
        } catch (ClassNotFoundException e) {
            System.err.println("[DatabaseConnection] FATAL: MySQL Connector/J Driver not found: " + e.getMessage());
        }
    }

    private static String getVal(String envKey, String propKey, Properties props, String defaultVal) {
        String env = System.getenv(envKey);
        if (env != null && !env.trim().isEmpty()) return env.trim();

        String sys = System.getProperty(propKey);
        if (sys != null && !sys.trim().isEmpty()) return sys.trim();

        String val = props.getProperty(propKey);
        if (val != null && !val.trim().isEmpty()) return val.trim();

        return defaultVal;
    }

    public static Connection getConnection() throws SQLException {
        if (!initialized) {
            loadConfiguration();
        }
        return DriverManager.getConnection(jdbcUrl, dbUser, dbPassword);
    }

    public static boolean testConnection() {
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT 1")) {
            return rs.next() && rs.getInt(1) == 1;
        } catch (SQLException e) {
            System.err.println("[DatabaseConnection] Test connection failed: " + e.getMessage());
            return false;
        }
    }

    /**
     * Non-destructive schema check: ensures 'category' column exists on 'destinations'.
     */
    public static void ensureCategoryColumn() {
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {

            boolean hasCategory = false;
            try (ResultSet rs = stmt.executeQuery(
                    "SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS " +
                    "WHERE TABLE_SCHEMA = 'smart_travel_planner' " +
                    "AND TABLE_NAME = 'destinations' AND COLUMN_NAME = 'category'")) {
                if (rs.next() && rs.getInt(1) > 0) {
                    hasCategory = true;
                }
            }

            if (!hasCategory) {
                System.out.println("[DatabaseConnection] Non-destructive schema update: Adding 'category' column to 'destinations'...");
                stmt.executeUpdate("ALTER TABLE destinations ADD COLUMN category VARCHAR(50) NOT NULL DEFAULT 'Cultural' AFTER city");

                stmt.executeUpdate("UPDATE destinations SET category = 'Adventure' WHERE name LIKE '%Trails%' OR name LIKE '%Trek%' OR description LIKE '%adventure%'");
                stmt.executeUpdate("UPDATE destinations SET category = 'Relaxation' WHERE name LIKE '%Beach%' OR name LIKE '%Escape%' OR description LIKE '%tropical%'");
                stmt.executeUpdate("UPDATE destinations SET category = 'Historical' WHERE name LIKE '%Temples%' OR name LIKE '%Shrine%' OR name LIKE '%Culture%'");
                System.out.println("[DatabaseConnection] 'category' column added and initialized.");
            }
        } catch (SQLException e) {
            System.err.println("[DatabaseConnection] Notice while checking category column: " + e.getMessage());
        }
    }

    public static String getJdbcUrl() { return jdbcUrl; }
    public static String getDbUser() { return dbUser; }
}
