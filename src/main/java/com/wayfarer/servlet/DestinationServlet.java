package com.wayfarer.servlet;

import com.wayfarer.model.Destination;
import com.wayfarer.model.User;
import com.wayfarer.service.DestinationService;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Wayfarer — Destination API Controller
 * Public read endpoints with search & category filtering;
 * Protected CRUD endpoints for administrators.
 */
@WebServlet(name = "wayfarerDestinationServlet", urlPatterns = "/api/destinations/*")
public class DestinationServlet extends BaseServlet {

    private final DestinationService destinationService = DestinationService.getInstance();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String pathInfo = req.getPathInfo();

        if (pathInfo != null && pathInfo.equalsIgnoreCase("/categories")) {
            sendJsonResponse(resp, HttpServletResponse.SC_OK, destinationService.getAvailableCategories());
            return;
        }

        String idParam = req.getParameter("id");
        if (idParam != null && !idParam.isBlank()) {
            try {
                int id = Integer.parseInt(idParam.trim());
                Destination d = destinationService.getById(id);
                if (d != null) {
                    sendJsonResponse(resp, HttpServletResponse.SC_OK, d);
                } else {
                    sendErrorResponse(resp, HttpServletResponse.SC_NOT_FOUND, "Destination not found.");
                }
                return;
            } catch (NumberFormatException e) {
                sendErrorResponse(resp, HttpServletResponse.SC_BAD_REQUEST, "Invalid destination ID.");
                return;
            }
        }

        String codeParam = req.getParameter("code");
        if (codeParam != null && !codeParam.isBlank()) {
            Destination d = destinationService.getByCode(codeParam.trim());
            if (d != null) {
                sendJsonResponse(resp, HttpServletResponse.SC_OK, d);
            } else {
                sendErrorResponse(resp, HttpServletResponse.SC_NOT_FOUND, "Destination code not found.");
            }
            return;
        }

        String query = req.getParameter("query");
        String category = req.getParameter("category");

        List<Destination> results = destinationService.searchAndFilter(query, category);
        sendJsonResponse(resp, HttpServletResponse.SC_OK, results);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        // Enforce Admin authorization
        User admin = requireAdmin(req, resp);
        if (admin == null) return;

        try {
            Destination body = parseJsonBody(req, Destination.class);
            if (body == null || body.getName() == null || body.getCountry() == null || body.getDestinationCode() == null) {
                sendErrorResponse(resp, HttpServletResponse.SC_BAD_REQUEST, "Destination code, name, and country are required.");
                return;
            }

            boolean ok = destinationService.createDestination(body);
            if (ok) {
                Map<String, Object> res = new HashMap<>();
                res.put("success", true);
                res.put("message", "Destination created successfully.");
                res.put("destination", body);
                sendJsonResponse(resp, HttpServletResponse.SC_CREATED, res);
            } else {
                sendErrorResponse(resp, HttpServletResponse.SC_CONFLICT, "Failed to create destination. Code may already exist.");
            }
        } catch (Exception e) {
            sendErrorResponse(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Creation error: " + e.getMessage());
        }
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        // Enforce Admin authorization
        User admin = requireAdmin(req, resp);
        if (admin == null) return;

        try {
            Destination body = parseJsonBody(req, Destination.class);
            if (body == null || body.getId() <= 0) {
                sendErrorResponse(resp, HttpServletResponse.SC_BAD_REQUEST, "Valid destination ID is required.");
                return;
            }

            boolean ok = destinationService.updateDestination(body);
            if (ok) {
                sendJsonResponse(resp, HttpServletResponse.SC_OK, Map.of(
                        "success", true,
                        "message", "Destination updated successfully."
                ));
            } else {
                sendErrorResponse(resp, HttpServletResponse.SC_NOT_FOUND, "Destination not found or update failed.");
            }
        } catch (Exception e) {
            sendErrorResponse(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Update error: " + e.getMessage());
        }
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        // Enforce Admin authorization
        User admin = requireAdmin(req, resp);
        if (admin == null) return;

        String idParam = req.getParameter("id");
        if (idParam == null || idParam.isBlank()) {
            sendErrorResponse(resp, HttpServletResponse.SC_BAD_REQUEST, "Destination ID parameter is required.");
            return;
        }

        try {
            int id = Integer.parseInt(idParam.trim());
            boolean ok = destinationService.deleteDestination(id);
            if (ok) {
                sendJsonResponse(resp, HttpServletResponse.SC_OK, Map.of(
                        "success", true,
                        "message", "Destination and associated route connections removed successfully."
                ));
            } else {
                sendErrorResponse(resp, HttpServletResponse.SC_NOT_FOUND, "Destination not found or could not be deleted.");
            }
        } catch (NumberFormatException e) {
            sendErrorResponse(resp, HttpServletResponse.SC_BAD_REQUEST, "Invalid destination ID.");
        }
    }
}
