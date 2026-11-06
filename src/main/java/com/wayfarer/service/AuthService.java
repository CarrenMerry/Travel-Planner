package com.wayfarer.service;

import com.wayfarer.dao.UserDAO;
import com.wayfarer.model.User;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.mindrot.jbcrypt.BCrypt;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

/**
 * Wayfarer — Authentication & Security Service
 *
 * Implements session-based authentication, BCrypt password hashing,
 * and a backward-compatible migration path for existing legacy SHA-256 salted hashes.
 */
public class AuthService {

    private static final String SESSION_USER_KEY = "WAYFARER_AUTH_USER";
    private static final String LEGACY_SALT = "SmartTravelPlanner2026_DSA_Salt#";

    private static final AuthService INSTANCE = new AuthService();
    private final UserDAO userDAO = new UserDAO();

    public static AuthService getInstance() {
        return INSTANCE;
    }

    private AuthService() {}

    /**
     * Authenticates an email and password against the MySQL database.
     * Supports BCrypt hashes as well as legacy SHA-256 hashes, seamlessly
     * upgrading legacy passwords to BCrypt upon successful login.
     */
    public User authenticate(String email, String password) {
        if (email == null || password == null || email.trim().isEmpty() || password.isEmpty()) {
            return null;
        }

        User user = userDAO.findByEmail(email.trim());
        if (user == null) {
            return null;
        }

        String storedHash = user.getPasswordHash();
        if (storedHash == null) {
            return null;
        }

        boolean passwordMatches = false;

        // 1. Check if stored hash is BCrypt
        if (storedHash.startsWith("$2a$") || storedHash.startsWith("$2b$") || storedHash.startsWith("$2y$")) {
            try {
                passwordMatches = BCrypt.checkpw(password, storedHash);
            } catch (Exception e) {
                System.err.println("[AuthService] BCrypt check failed: " + e.getMessage());
            }
        } else {
            // 2. Legacy verification against known historical salts
            passwordMatches = checkLegacyPassword(password, storedHash);
            if (passwordMatches) {
                // Seamlessly upgrade stored password to BCrypt
                try {
                    String newBcryptHash = BCrypt.hashpw(password, BCrypt.gensalt(12));
                    userDAO.updatePassword(user.getId(), newBcryptHash);
                    user.setPasswordHash(newBcryptHash);
                    System.out.println("[AuthService] Upgraded user #" + user.getId() + " (" + user.getEmail() + ") to BCrypt.");
                } catch (Exception e) {
                    System.err.println("[AuthService] Notice: Could not upgrade hash to BCrypt: " + e.getMessage());
                }
            }
        }

        return passwordMatches ? user : null;
    }

    public User login(String email, String password, HttpServletRequest req) {
        User user = authenticate(email, password);
        if (user != null && req != null) {
            HttpSession session = req.getSession(true);
            session.setAttribute(SESSION_USER_KEY, user);
            return user;
        }
        return null;
    }

    public User register(String name, String email, String password, HttpServletRequest req) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Full name is required.");
        }
        if (email == null || email.trim().isEmpty() || !email.contains("@")) {
            throw new IllegalArgumentException("A valid email address is required.");
        }
        if (password == null || password.length() < 6) {
            throw new IllegalArgumentException("Password must be at least 6 characters long.");
        }

        String cleanEmail = email.trim().toLowerCase();

        // Enforce uniqueness
        User existing = userDAO.findByEmail(cleanEmail);
        if (existing != null) {
            throw new IllegalStateException("An account with email '" + cleanEmail + "' already exists.");
        }

        // Hash using modern BCrypt
        String bcryptHash = BCrypt.hashpw(password, BCrypt.gensalt(12));

        // New accounts always receive normal USER role (never public admin creation)
        boolean created = userDAO.createUser(name.trim(), cleanEmail, bcryptHash, User.Role.USER);
        if (!created) {
            throw new RuntimeException("Could not create user account. Please try again.");
        }

        User newUser = userDAO.findByEmail(cleanEmail);
        if (newUser != null && req != null) {
            HttpSession session = req.getSession(true);
            session.setAttribute(SESSION_USER_KEY, newUser);
        }
        return newUser;
    }

    public User getCurrentUser(HttpServletRequest req) {
        if (req == null) return null;
        HttpSession session = req.getSession(false);
        if (session != null) {
            Object obj = session.getAttribute(SESSION_USER_KEY);
            if (obj instanceof User) {
                return (User) obj;
            }
        }
        return null;
    }

    public void logout(HttpServletRequest req) {
        if (req != null) {
            HttpSession session = req.getSession(false);
            if (session != null) {
                session.removeAttribute(SESSION_USER_KEY);
                session.invalidate();
            }
        }
    }

    private static boolean checkLegacyPassword(String password, String storedHash) {
        if (storedHash == null || password == null) return false;
        String[] salts = {
            "SmartTravelPlanner_DSA_2026_SecureSalt",
            "SmartTravelPlanner2026_DSA_Salt#",
            ""
        };

        for (String salt : salts) {
            try {
                MessageDigest md = MessageDigest.getInstance("SHA-256");
                byte[] bytes = md.digest((password + salt).getBytes(StandardCharsets.UTF_8));
                StringBuilder sb = new StringBuilder();
                for (byte b : bytes) {
                    sb.append(String.format("%02x", b));
                }
                if (sb.toString().equalsIgnoreCase(storedHash)) {
                    return true;
                }
            } catch (Exception ignored) {}
        }

        return password.equals(storedHash);
    }
}
