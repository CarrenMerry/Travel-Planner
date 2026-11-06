package com.wayfarer.servlet;

import com.wayfarer.model.User;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Wayfarer — Authentication Controller
 * Handles session checking, login, registration, and logout.
 */
@WebServlet(name = "wayfarerAuthServlet", urlPatterns = "/api/auth/*")
public class AuthServlet extends BaseServlet {

    public static class LoginRequest {
        public String email;
        public String password;
    }

    public static class RegisterRequest {
        public String name;
        public String email;
        public String password;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String pathInfo = req.getPathInfo();
        if (pathInfo != null && pathInfo.equalsIgnoreCase("/me")) {
            User user = authService.getCurrentUser(req);
            Map<String, Object> data = new HashMap<>();
            if (user != null) {
                Map<String, Object> u = new HashMap<>();
                u.put("id", user.getId());
                u.put("name", user.getName());
                u.put("email", user.getEmail());
                u.put("role", user.getRole().name());
                u.put("isAdmin", user.isAdmin());

                data.put("authenticated", true);
                data.put("user", u);
                data.put("id", user.getId());
                data.put("name", user.getName());
                data.put("email", user.getEmail());
                data.put("role", user.getRole().name());
                data.put("isAdmin", user.isAdmin());
            } else {
                data.put("authenticated", false);
                data.put("user", null);
                data.put("role", "GUEST");
                data.put("isAdmin", false);
            }
            sendJsonResponse(resp, HttpServletResponse.SC_OK, data);
            return;
        }

        sendErrorResponse(resp, HttpServletResponse.SC_BAD_REQUEST, "Invalid auth endpoint.");
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String pathInfo = req.getPathInfo();

        if (pathInfo != null && pathInfo.equalsIgnoreCase("/login")) {
            handleLogin(req, resp);
        } else if (pathInfo != null && pathInfo.equalsIgnoreCase("/register")) {
            handleRegister(req, resp);
        } else if (pathInfo != null && pathInfo.equalsIgnoreCase("/logout")) {
            handleLogout(req, resp);
        } else {
            sendErrorResponse(resp, HttpServletResponse.SC_NOT_FOUND, "Auth endpoint not found.");
        }
    }

    private void handleLogin(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            LoginRequest body = parseJsonBody(req, LoginRequest.class);
            if (body == null || body.email == null || body.password == null) {
                sendErrorResponse(resp, HttpServletResponse.SC_BAD_REQUEST, "Email and password are required.");
                return;
            }

            User user = authService.login(body.email, body.password, req);
            if (user == null) {
                sendErrorResponse(resp, HttpServletResponse.SC_UNAUTHORIZED, "Invalid email or password.");
                return;
            }

            Map<String, Object> u = new HashMap<>();
            u.put("id", user.getId());
            u.put("name", user.getName());
            u.put("email", user.getEmail());
            u.put("role", user.getRole().name());
            u.put("isAdmin", user.isAdmin());

            Map<String, Object> res = new HashMap<>();
            res.put("success", true);
            res.put("message", "Welcome back, " + user.getName());
            res.put("role", user.getRole().name());
            res.put("name", user.getName());
            res.put("email", user.getEmail());
            res.put("isAdmin", user.isAdmin());
            res.put("user", u);

            sendJsonResponse(resp, HttpServletResponse.SC_OK, res);
        } catch (Exception e) {
            sendErrorResponse(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Login processing failed: " + e.getMessage());
        }
    }

    private void handleRegister(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            RegisterRequest body = parseJsonBody(req, RegisterRequest.class);
            if (body == null) {
                sendErrorResponse(resp, HttpServletResponse.SC_BAD_REQUEST, "Registration data is required.");
                return;
            }

            User user = authService.register(body.name, body.email, body.password, req);

            Map<String, Object> u = new HashMap<>();
            u.put("id", user.getId());
            u.put("name", user.getName());
            u.put("email", user.getEmail());
            u.put("role", user.getRole().name());
            u.put("isAdmin", user.isAdmin());

            Map<String, Object> res = new HashMap<>();
            res.put("success", true);
            res.put("message", "Account successfully created. Welcome to Wayfarer!");
            res.put("role", user.getRole().name());
            res.put("name", user.getName());
            res.put("email", user.getEmail());
            res.put("isAdmin", user.isAdmin());
            res.put("user", u);

            sendJsonResponse(resp, HttpServletResponse.SC_CREATED, res);
        } catch (IllegalArgumentException | IllegalStateException e) {
            sendErrorResponse(resp, HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
        } catch (Exception e) {
            sendErrorResponse(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Registration failed: " + e.getMessage());
        }
    }

    private void handleLogout(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        authService.logout(req);
        sendJsonResponse(resp, HttpServletResponse.SC_OK, Map.of(
                "success", true,
                "message", "Session invalidated. Logged out successfully."
        ));
    }
}
