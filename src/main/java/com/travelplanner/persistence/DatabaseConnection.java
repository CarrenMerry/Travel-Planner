package com.travelplanner.persistence;

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
 * Reusable MySQL Database Connection utility using JDBC.
 * Reads configuration from Environment Variables, System Properties,
 * or local db.properties file excluded from version control.
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

        // 1. Try loading from working directory db.properties
        File file = new File("db.properties");
        if (file.exists()) {
            try (InputStream in = new FileInputStream(file)) {
                props.load(in);
            } catch (Exception e) {
                System.err.println("[DatabaseConnection] Warning: Could not read local db.properties: " + e.getMessage());
            }
        } else {
            // 2. Try loading from classpath
            try (InputStream in = DatabaseConnection.class.getClassLoader().getResourceAsStream("db.properties")) {
                if (in != null) {
                    props.load(in);
                }
            } catch (Exception ignored) {}
        }

        // Priority: ENV VAR > System Property > db.properties > Default
        String host = getVal("DB_HOST", "db.host", props, DEFAULT_HOST);
        String port = getVal("DB_PORT", "db.port", props, DEFAULT_PORT);
        String dbName = getVal("DB_NAME", "db.name", props, DEFAULT_DB);
        dbUser = getVal("DB_USER", "db.user", props, DEFAULT_USER);
        dbPassword = getVal("DB_PASSWORD", "db.password", props, "");

        String rawUrl = getVal("DB_URL", "db.url", props, null);
        if (rawUrl != null && !rawUrl.trim().isEmpty()) {
            jdbcUrl = rawUrl.trim();
        } else {
            jdbcUrl = String.format("jdbc:mysql://%s:%s/%s?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC&characterEncoding=UTF-8",
                    host, port, dbName);
        }

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            initialized = true;
        } catch (ClassNotFoundException e) {
            System.err.println("[DatabaseConnection] CRITICAL: MySQL Connector/J driver not found: " + e.getMessage());
            initialized = false;
        }
    }

    private static String getVal(String envName, String propName, Properties props, String defaultVal) {
        String env = System.getenv(envName);
        if (env != null && !env.trim().isEmpty()) return env.trim();

        String sysProp = System.getProperty(propName);
        if (sysProp != null && !sysProp.trim().isEmpty()) return sysProp.trim();

        if (props.containsKey(propName)) {
            String pVal = props.getProperty(propName);
            if (pVal != null) return pVal.trim();
        }

        return defaultVal;
    }

    /**
     * Obtains a new JDBC Connection to MySQL.
     * Callers should manage the connection lifecycle using try-with-resources.
     */
    public static Connection getConnection() throws SQLException {
        if (!initialized) {
            loadConfiguration();
        }
        try {
            return DriverManager.getConnection(jdbcUrl, dbUser, dbPassword);
        } catch (SQLException e) {
            System.err.println("[DatabaseConnection] FAILED to connect to MySQL at: " + jdbcUrl);
            System.err.println("[DatabaseConnection] User: " + dbUser + ", Reason: " + e.getMessage());
            throw new SQLException("Unable to connect to MySQL database (" + jdbcUrl + "): " + e.getMessage(), e);
        }
    }

    /**
     * Executes SELECT 1 to verify database connectivity.
     */
    public static boolean testConnection() {
        System.out.println("[DatabaseConnection] Verifying MySQL connection at " + jdbcUrl + " (User: " + dbUser + ")...");
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT 1")) {
            if (rs.next()) {
                System.out.println("[DatabaseConnection] SUCCESS: MySQL connection established and 'SELECT 1' passed.");
                return true;
            }
        } catch (SQLException e) {
            System.err.println("[DatabaseConnection] DIAGNOSTIC: Connection test failed: " + e.getMessage());
        }
        return false;
    }

    public static String getJdbcUrl() {
        return jdbcUrl;
    }

    public static String getDbUser() {
        return dbUser;
    }
}
