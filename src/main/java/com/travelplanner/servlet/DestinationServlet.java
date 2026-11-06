package com.travelplanner.servlet;

import com.travelplanner.model.Destination;
import com.travelplanner.service.AuthService;
import com.travelplanner.service.DestinationService;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@WebServlet("/api/destinations/*")
public class DestinationServlet extends BaseServlet {

    private final DestinationService destinationService = DestinationService.getInstance();
    private final AuthService authService = AuthService.getInstance();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String pathInfo = req.getPathInfo();
        String idParam = req.getParameter("id");

        // 1. Single lookup by ID or destination_code via parameter
        if (idParam != null && !idParam.isEmpty()) {
            Destination d = destinationService.getById(idParam);
            if (d == null) {
                sendErrorResponse(resp, HttpServletResponse.SC_NOT_FOUND, "Destination not found with ID: " + idParam);
            } else {
                sendJsonResponse(resp, HttpServletResponse.SC_OK, d);
            }
            return;
        }

        // 2. Single lookup by path info: /api/destinations/{id}
        if (pathInfo != null && pathInfo.length() > 1) {
            String id = pathInfo.substring(1);
            Destination d = destinationService.getById(id);
            if (d == null) {
                sendErrorResponse(resp, HttpServletResponse.SC_NOT_FOUND, "Destination not found with ID: " + id);
            } else {
                sendJsonResponse(resp, HttpServletResponse.SC_OK, d);
            }
            return;
        }

        // 3. Search query: /api/destinations?q=query
        String q = req.getParameter("q");
        if (q != null && !q.trim().isEmpty()) {
            List<Destination> results = destinationService.getDestinationDao().search(q);
            Map<String, Object> responseData = new HashMap<>();
            responseData.put("count", results.size());
            responseData.put("destinations", results);
            sendJsonResponse(resp, HttpServletResponse.SC_OK, responseData);
            return;
        }

        // 4. Filtering by country, cost, rating: /api/destinations?country=...&cost=...&rating=...
        String country = req.getParameter("country");
        String costParam = req.getParameter("cost");
        String maxCostParam = req.getParameter("maxCost");
        String ratingParam = req.getParameter("rating");
        String minRatingParam = req.getParameter("minRating");

        Double maxCost = null;
        if (maxCostParam != null && !maxCostParam.isEmpty()) {
            try { maxCost = Double.parseDouble(maxCostParam); } catch (NumberFormatException ignored) {}
        } else if (costParam != null && !costParam.isEmpty()) {
            try { maxCost = Double.parseDouble(costParam); } catch (NumberFormatException ignored) {}
        }

        Double minRating = null;
        if (minRatingParam != null && !minRatingParam.isEmpty()) {
            try { minRating = Double.parseDouble(minRatingParam); } catch (NumberFormatException ignored) {}
        } else if (ratingParam != null && !ratingParam.isEmpty()) {
            try { minRating = Double.parseDouble(ratingParam); } catch (NumberFormatException ignored) {}
        }

        if (country != null || maxCost != null || minRating != null) {
            List<Destination> filtered = destinationService.getDestinationDao().filter(country, maxCost, minRating);
            Map<String, Object> responseData = new HashMap<>();
            responseData.put("count", filtered.size());
            responseData.put("destinations", filtered);
            sendJsonResponse(resp, HttpServletResponse.SC_OK, responseData);
            return;
        }

        // 5. Default: Return all destinations loaded from MySQL
        List<Destination> all = destinationService.getAllDestinations();
        Map<String, Object> responseData = new HashMap<>();
        responseData.put("count", all.size());
        responseData.put("destinations", all);
        sendJsonResponse(resp, HttpServletResponse.SC_OK, responseData);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        // Enforce Server-Side Admin Authorization
        if (!authService.isAdmin(req)) {
            sendErrorResponse(resp, HttpServletResponse.SC_FORBIDDEN, "Access denied: Administrator privileges required to create destinations.");
            return;
        }

        try {
            Destination destination = parseJsonBody(req, Destination.class);
            if (destination == null) {
                sendErrorResponse(resp, HttpServletResponse.SC_BAD_REQUEST, "Invalid or empty JSON body.");
                return;
            }

            String validationError = destination.validateAndGetError();
            if (validationError != null) {
                sendErrorResponse(resp, HttpServletResponse.SC_BAD_REQUEST, validationError);
                return;
            }

            boolean added = destinationService.addDestination(destination);
            if (!added) {
                sendErrorResponse(resp, HttpServletResponse.SC_CONFLICT, "A destination with ID/code '" + destination.getId() + "' already exists.");
                return;
            }

            Map<String, Object> res = new HashMap<>();
            res.put("success", true);
            res.put("message", "Destination created successfully in MySQL database.");
            res.put("destination", destination);
            sendJsonResponse(resp, HttpServletResponse.SC_CREATED, res);
        } catch (Exception e) {
            sendErrorResponse(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Server error: " + e.getMessage());
        }
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        // Enforce Server-Side Admin Authorization
        if (!authService.isAdmin(req)) {
            sendErrorResponse(resp, HttpServletResponse.SC_FORBIDDEN, "Access denied: Administrator privileges required to modify destinations.");
            return;
        }

        try {
            Destination destination = parseJsonBody(req, Destination.class);
            if (destination == null) {
                sendErrorResponse(resp, HttpServletResponse.SC_BAD_REQUEST, "Invalid or empty JSON body.");
                return;
            }

            String validationError = destination.validateAndGetError();
            if (validationError != null) {
                sendErrorResponse(resp, HttpServletResponse.SC_BAD_REQUEST, validationError);
                return;
            }

            boolean updated = destinationService.updateDestination(destination);
            if (!updated) {
                sendErrorResponse(resp, HttpServletResponse.SC_NOT_FOUND, "Destination with ID '" + destination.getId() + "' not found.");
                return;
            }

            Map<String, Object> res = new HashMap<>();
            res.put("success", true);
            res.put("message", "Destination updated successfully in MySQL database.");
            res.put("destination", destination);
            sendJsonResponse(resp, HttpServletResponse.SC_OK, res);
        } catch (Exception e) {
            sendErrorResponse(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Server error: " + e.getMessage());
        }
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        // Enforce Server-Side Admin Authorization
        if (!authService.isAdmin(req)) {
            sendErrorResponse(resp, HttpServletResponse.SC_FORBIDDEN, "Access denied: Administrator privileges required to delete destinations.");
            return;
        }

        String id = req.getParameter("id");
        String pathInfo = req.getPathInfo();
        if ((id == null || id.isEmpty()) && pathInfo != null && pathInfo.length() > 1) {
            id = pathInfo.substring(1);
        }

        if (id == null || id.trim().isEmpty()) {
            sendErrorResponse(resp, HttpServletResponse.SC_BAD_REQUEST, "Must provide 'id' parameter or /api/destinations/{id}.");
            return;
        }

        boolean deleted = destinationService.deleteDestination(id.trim());
        if (!deleted) {
            sendErrorResponse(resp, HttpServletResponse.SC_NOT_FOUND, "Destination with ID '" + id + "' not found.");
            return;
        }

        Map<String, Object> res = new HashMap<>();
        res.put("success", true);
        res.put("message", "Destination '" + id + "' deleted successfully from MySQL database.");
        sendJsonResponse(resp, HttpServletResponse.SC_OK, res);
    }
}
