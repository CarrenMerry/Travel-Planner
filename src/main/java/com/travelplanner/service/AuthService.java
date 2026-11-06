package com.travelplanner.service;

import com.travelplanner.dao.UserDao;
import com.travelplanner.model.User;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Authentication and Role-Based Access Control (RBAC) service.
 * Integrates MySQL 'users' table persistence with fallback in-memory cache,
 * server-side HttpSession tracking, and salted SHA-256 password hashing.
 */
public class AuthService {

    private static final String SESSION_USER_KEY = "AUTH_USER";
    private static final String SALT = "SmartTravelPlanner_DSA_2026_SecureSalt";
    private static AuthService instance;

    private final UserDao userDao = new UserDao();
    private final Map<String, User> memoryFallbackRegistry = new ConcurrentHashMap<>();

    private AuthService() {
        provisionAccounts();
    }

    public static synchronized AuthService getInstance() {
        if (instance == null) {
            instance = new AuthService();
        }
        return instance;
    }

    private void provisionAccounts() {
        String adminHash = hashPassword("admin123");
        String userHash = hashPassword("travel123");

        // Provision in memory fallback
        memoryFallbackRegistry.put("admin", new User("admin", adminHash, User.Role.ADMIN, "System Administrator"));
        memoryFallbackRegistry.put("traveler", new User("traveler", userHash, User.Role.USER, "Alex Traveler"));

        // Persist to MySQL 'users' table if empty
        try {
            userDao.ensureDefaultAccounts(adminHash, userHash);
        } catch (Exception e) {
            System.err.println("[AuthService] Warning: Could not provision MySQL users: " + e.getMessage());
        }
    }

    public static String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] encoded = digest.digest((password + SALT).getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : encoded) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 not supported", e);
        }
    }

    /**
     * Authenticates credentials against MySQL users table (with memory fallback).
     */
    public User authenticate(String username, String password) {
        if (username == null || password == null) return null;
        String cleanUser = username.trim().toLowerCase();
        String hashedInput = hashPassword(password);

        // 1. Try MySQL database lookup
        try {
            User dbUser = userDao.findByEmailOrUsername(cleanUser);
            if (dbUser != null) {
                if (hashedInput.equals(dbUser.getPasswordHash())) {
                    return dbUser;
                }
            }
        } catch (Exception e) {
            System.err.println("[AuthService] Database auth query failed: " + e.getMessage());
        }

        // 2. Fallback to memory registry
        User memUser = memoryFallbackRegistry.get(cleanUser);
        if (memUser != null && hashedInput.equals(memUser.getPasswordHash())) {
            return memUser;
        }

        return null;
    }

    /**
     * Logs in the user and records the User entity into the server-side HTTP session.
     */
    public User login(String username, String password, HttpServletRequest req) {
        User user = authenticate(username, password);
        if (user != null) {
            HttpSession session = req.getSession(true);
            session.setAttribute(SESSION_USER_KEY, user);
            return user;
        }
        return null;
    }

    /**
     * Registers a new USER account in MySQL, then auto-logs in.
     * Returns the created User on success, or throws RuntimeException on failure.
     */
    public User register(String username, String password, String email,
                         String firstName, String lastName, HttpServletRequest req) {
        if (username == null || username.trim().isEmpty()) throw new RuntimeException("Username is required.");
        if (password == null || password.trim().length() < 6) throw new RuntimeException("Password must be at least 6 characters.");

        String cleanUser = username.trim().toLowerCase();

        // Reject if username already exists
        try {
            User existing = userDao.findByEmailOrUsername(cleanUser);
            if (existing != null) throw new RuntimeException("Username '" + cleanUser + "' is already taken.");
        } catch (RuntimeException re) {
            throw re;
        } catch (Exception e) {
            System.err.println("[AuthService] DB check failed during registration: " + e.getMessage());
        }

        // Build display name
        String displayName = (firstName != null && !firstName.isBlank()) ? firstName.trim() : cleanUser;
        if (lastName != null && !lastName.isBlank()) displayName += " " + lastName.trim();

        // Build email (use username@travelplanner.com if not provided)
        String resolvedEmail = (email != null && !email.isBlank()) ? email.trim() :
                cleanUser + "@travelplanner.local";

        String passwordHash = hashPassword(password);

        boolean created = userDao.createUser(displayName, resolvedEmail, passwordHash, User.Role.USER);
        if (!created) throw new RuntimeException("Failed to create account. Please try again.");

        // Fetch back from DB
        User newUser = userDao.findByEmailOrUsername(cleanUser);
        if (newUser == null) {
            // Fallback: construct from local data
            newUser = new User(cleanUser, passwordHash, User.Role.USER, displayName);
        }

        // Auto-login into session
        HttpSession session = req.getSession(true);
        session.setAttribute(SESSION_USER_KEY, newUser);
        return newUser;
    }

    /**
     * Logs out the user and invalidates the session.
     */
    public void logout(HttpServletRequest req) {
        HttpSession session = req.getSession(false);
        if (session != null) {
            session.invalidate();
        }
    }

    /**
     * Retrieves the authenticated user from the current request's session.
     */
    public User getCurrentUser(HttpServletRequest req) {
        HttpSession session = req.getSession(false);
        if (session != null) {
            Object obj = session.getAttribute(SESSION_USER_KEY);
            if (obj instanceof User) {
                return (User) obj;
            }
        }
        return null;
    }

    /**
     * Checks if the active session belongs to an authorized Administrator.
     */
    public boolean isAdmin(HttpServletRequest req) {
        User user = getCurrentUser(req);
        return user != null && user.isAdmin();
    }

    /**
     * Checks if a session exists (either User or Admin).
     */
    public boolean isAuthenticated(HttpServletRequest req) {
        return getCurrentUser(req) != null;
    }
}
