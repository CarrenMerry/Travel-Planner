package com.travelplanner;

import com.travelplanner.model.User;
import com.travelplanner.service.AuthService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class AuthServiceTest {

    private final AuthService authService = AuthService.getInstance();

    @Test
    @DisplayName("Verify administrator credentials and ADMIN role")
    void testAdminAuthentication() {
        User admin = authService.authenticate("admin", "admin123");
        assertNotNull(admin, "Admin should successfully authenticate with admin123");
        assertEquals("admin", admin.getUsername());
        assertEquals(User.Role.ADMIN, admin.getRole());
        assertTrue(admin.isAdmin());
        assertEquals("System Administrator", admin.getDisplayName());
    }

    @Test
    @DisplayName("Verify traveler credentials and USER role")
    void testUserAuthentication() {
        User traveler = authService.authenticate("traveler", "travel123");
        assertNotNull(traveler, "Traveler should successfully authenticate with travel123");
        assertEquals("traveler", traveler.getUsername());
        assertEquals(User.Role.USER, traveler.getRole());
        assertFalse(traveler.isAdmin(), "Regular traveler should not have ADMIN privileges");
    }

    @Test
    @DisplayName("Reject incorrect password and unknown user")
    void testInvalidAuthentication() {
        User wrongPass = authService.authenticate("admin", "wrongPassword");
        assertNull(wrongPass, "Authentication should fail with incorrect password");

        User unknown = authService.authenticate("hacker", "password");
        assertNull(unknown, "Authentication should fail for unregistered usernames");

        User nullUser = authService.authenticate(null, null);
        assertNull(nullUser, "Authentication should reject null inputs");
    }

    @Test
    @DisplayName("Verify SHA-256 salted password hashing consistency")
    void testPasswordHashing() {
        String hash1 = AuthService.hashPassword("admin123");
        String hash2 = AuthService.hashPassword("admin123");
        assertEquals(hash1, hash2, "Identical passwords with salt must produce matching SHA-256 hashes");

        String diffHash = AuthService.hashPassword("otherPass");
        assertNotEquals(hash1, diffHash, "Different passwords must produce distinct hashes");
    }
}
