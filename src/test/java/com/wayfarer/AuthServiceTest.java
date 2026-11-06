package com.wayfarer;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mindrot.jbcrypt.BCrypt;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

import static org.junit.jupiter.api.Assertions.*;

public class AuthServiceTest {

    private static final String LEGACY_SALT = "SmartTravelPlanner2026_DSA_Salt#";

    private String hashLegacySha256(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hashed = md.digest((password + LEGACY_SALT).getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : hashed) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    @DisplayName("BCrypt hashing produces valid verifiable hashes")
    void testBcryptHashing() {
        String rawPassword = "TravelerPassword2026!";
        String hash = BCrypt.hashpw(rawPassword, BCrypt.gensalt(10));

        assertNotNull(hash);
        assertTrue(hash.startsWith("$2a$") || hash.startsWith("$2b$"));
        assertTrue(BCrypt.checkpw(rawPassword, hash), "Password should match BCrypt hash");
        assertFalse(BCrypt.checkpw("WrongPassword", hash), "Incorrect password should not match BCrypt hash");
    }

    @Test
    @DisplayName("Verify admin authentication via AuthService directly")
    void testLiveAdminAuthentication() {
        com.wayfarer.service.AuthService auth = com.wayfarer.service.AuthService.getInstance();
        com.wayfarer.model.User admin = auth.authenticate("admin@travelplanner.com", "admin123");
        assertNotNull(admin, "Admin should authenticate with admin@travelplanner.com and admin123");
        assertEquals("admin@travelplanner.com", admin.getEmail());
        assertTrue(admin.isAdmin());

        // Also test by username 'admin'
        com.wayfarer.model.User adminByUsername = auth.authenticate("admin", "admin123");
        assertNotNull(adminByUsername, "Admin should authenticate by username 'admin'");
    }

    @Test
    @DisplayName("Verify traveler authentication via AuthService directly")
    void testLiveTravelerAuthentication() {
        com.wayfarer.service.AuthService auth = com.wayfarer.service.AuthService.getInstance();
        com.wayfarer.model.User traveler = auth.authenticate("traveler@travelplanner.com", "travel123");
        assertNotNull(traveler, "Traveler should authenticate with traveler@travelplanner.com and travel123");
        assertEquals("traveler@travelplanner.com", traveler.getEmail());
        assertFalse(traveler.isAdmin());

        // Also test by username 'traveler'
        com.wayfarer.model.User travelerByUsername = auth.authenticate("traveler", "travel123");
        assertNotNull(travelerByUsername, "Traveler should authenticate by username 'traveler'");
    }
}
