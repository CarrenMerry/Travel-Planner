package com.travelplanner.servlet;

import com.travelplanner.model.User;
import com.travelplanner.service.AuthService;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@WebServlet("/api/auth/*")
public class AuthServlet extends BaseServlet {

    private final AuthService authService = AuthService.getInstance();

    public static class LoginRequest {
        public String username;
        public String password;
    }

    public static class RegisterRequest {
        public String username;
        public String password;
        public String email;
        public String firstName;
        public String lastName;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String pathInfo = req.getPathInfo();

        if (pathInfo != null && pathInfo.equalsIgnoreCase("/me")) {
            User user = authService.getCurrentUser(req);
            Map<String, Object> data = new HashMap<>();
            if (user != null) {
                data.put("authenticated", true);
                data.put("username", user.getUsername());
                data.put("role", user.getRole().name());
                data.put("displayName", user.getDisplayName());
                data.put("isAdmin", user.isAdmin());
            } else {
                data.put("authenticated", false);
                data.put("role", "GUEST");
                data.put("isAdmin", false);
            }
            sendJsonResponse(resp, HttpServletResponse.SC_OK, data);
            return;
        }

        sendErrorResponse(resp, HttpServletResponse.SC_BAD_REQUEST, "Unknown auth endpoint. Use /api/auth/me, /login, or /logout.");
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String pathInfo = req.getPathInfo();

        if (pathInfo != null && pathInfo.equalsIgnoreCase("/login")) {
            handleLogin(req, resp);
            return;
        } else if (pathInfo != null && pathInfo.equalsIgnoreCase("/logout")) {
            handleLogout(req, resp);
            return;
        } else if (pathInfo != null && pathInfo.equalsIgnoreCase("/register")) {
            handleRegister(req, resp);
            return;
        }

        sendErrorResponse(resp, HttpServletResponse.SC_BAD_REQUEST, "Endpoint must be /login, /logout, or /register.");
    }

    private void handleLogin(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            LoginRequest body = parseJsonBody(req, LoginRequest.class);
            if (body == null || body.username == null || body.password == null) {
                sendErrorResponse(resp, HttpServletResponse.SC_BAD_REQUEST, "Username and password are required.");
                return;
            }

            User user = authService.login(body.username, body.password, req);
            if (user == null) {
                sendErrorResponse(resp, HttpServletResponse.SC_UNAUTHORIZED, "Invalid username or password.");
                return;
            }

            Map<String, Object> res = new HashMap<>();
            res.put("success", true);
            res.put("message", "Login successful as " + user.getDisplayName());
            res.put("role", user.getRole().name());
            res.put("username", user.getUsername());
            res.put("displayName", user.getDisplayName());
            res.put("isAdmin", user.isAdmin());
            res.put("user", Map.of(
                    "username", user.getUsername(),
                    "role", user.getRole().name(),
                    "displayName", user.getDisplayName(),
                    "isAdmin", user.isAdmin()
            ));
            sendJsonResponse(resp, HttpServletResponse.SC_OK, res);
        } catch (Exception e) {
            sendErrorResponse(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Server login error: " + e.getMessage());
        }
    }

    private void handleLogout(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        authService.logout(req);
        Map<String, Object> res = new HashMap<>();
        res.put("success", true);
        res.put("message", "Session terminated. Logged out successfully.");
        sendJsonResponse(resp, HttpServletResponse.SC_OK, res);
    }

    private void handleRegister(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            RegisterRequest body = parseJsonBody(req, RegisterRequest.class);
            if (body == null || body.username == null || body.password == null) {
                sendErrorResponse(resp, HttpServletResponse.SC_BAD_REQUEST, "Username and password are required.");
                return;
            }

            User user = authService.register(body.username, body.password, body.email,
                    body.firstName, body.lastName, req);

            Map<String, Object> res = new HashMap<>();
            res.put("success", true);
            res.put("message", "Account created successfully as " + user.getDisplayName());
            res.put("role", user.getRole().name());
            res.put("username", user.getUsername());
            res.put("displayName", user.getDisplayName());
            res.put("isAdmin", user.isAdmin());
            sendJsonResponse(resp, HttpServletResponse.SC_CREATED, res);

        } catch (RuntimeException re) {
            sendErrorResponse(resp, HttpServletResponse.SC_CONFLICT, re.getMessage());
        } catch (Exception e) {
            sendErrorResponse(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Registration error: " + e.getMessage());
        }
    }
}
