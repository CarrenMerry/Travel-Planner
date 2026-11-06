package com.wayfarer.servlet;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.wayfarer.model.User;
import com.wayfarer.service.AuthService;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;

/**
 * Wayfarer — Base Servlet Helper
 * Provides standardized JSON serialization, error formatting, and role authorization checks.
 */
public abstract class BaseServlet extends HttpServlet {

    protected static final Gson GSON = new GsonBuilder()
            .setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ")
            .serializeNulls()
            .create();

    protected final AuthService authService = AuthService.getInstance();

    protected void sendJsonResponse(HttpServletResponse resp, int statusCode, Object data) throws IOException {
        resp.setStatus(statusCode);
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        resp.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");

        PrintWriter out = resp.getWriter();
        out.print(GSON.toJson(data));
        out.flush();
    }

    protected void sendErrorResponse(HttpServletResponse resp, int statusCode, String message) throws IOException {
        sendJsonResponse(resp, statusCode, Map.of(
                "success", false,
                "error", message != null ? message : "An unexpected error occurred."
        ));
    }

    protected <T> T parseJsonBody(HttpServletRequest req, Class<T> clazz) throws IOException {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = req.getReader()) {
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
        }
        String body = sb.toString();
        if (body.trim().isEmpty()) return null;
        return GSON.fromJson(body, clazz);
    }

    /**
     * Enforces user authentication. Returns User if session is valid; otherwise responds 401.
     */
    protected User requireAuth(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        User user = authService.getCurrentUser(req);
        if (user == null) {
            sendErrorResponse(resp, HttpServletResponse.SC_UNAUTHORIZED, "Authentication required. Please sign in.");
            return null;
        }
        return user;
    }

    /**
     * Enforces administrator authorization. Returns User if user has ADMIN role; otherwise responds 403.
     */
    protected User requireAdmin(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        User user = requireAuth(req, resp);
        if (user == null) return null;

        if (!user.isAdmin()) {
            sendErrorResponse(resp, HttpServletResponse.SC_FORBIDDEN, "Access forbidden: Administrator privileges required.");
            return null;
        }
        return user;
    }
}
