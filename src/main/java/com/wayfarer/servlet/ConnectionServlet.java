package com.wayfarer.servlet;

import com.wayfarer.model.TravelConnection;
import com.wayfarer.model.User;
import com.wayfarer.service.RouteService;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.Map;

/**
 * Wayfarer — Connection Management Controller
 * Allows administrators to view and configure travel route connections between destinations.
 */
@WebServlet(name = "wayfarerConnectionServlet", urlPatterns = "/api/connections/*")
public class ConnectionServlet extends BaseServlet {

    private final RouteService routeService = RouteService.getInstance();

    public static class ConnectionRequest {
        public int id;
        public int sourceDestinationId;
        public int targetDestinationId;
        public double distanceKm;
        public double estimatedTravelCost;
        public Integer estimatedTravelMinutes;
        public Boolean bidirectional;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        sendJsonResponse(resp, HttpServletResponse.SC_OK, routeService.getAllConnections());
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        User admin = requireAdmin(req, resp);
        if (admin == null) return;

        try {
            ConnectionRequest body = parseJsonBody(req, ConnectionRequest.class);
            if (body == null || body.sourceDestinationId <= 0 || body.targetDestinationId <= 0) {
                sendErrorResponse(resp, HttpServletResponse.SC_BAD_REQUEST, "Valid source and target destination IDs are required.");
                return;
            }

            if (body.sourceDestinationId == body.targetDestinationId) {
                sendErrorResponse(resp, HttpServletResponse.SC_BAD_REQUEST, "Source and target destinations cannot be identical.");
                return;
            }

            boolean ok = routeService.createConnection(
                    body.sourceDestinationId,
                    body.targetDestinationId,
                    body.distanceKm,
                    body.estimatedTravelCost,
                    body.estimatedTravelMinutes
            );

            if (ok && Boolean.TRUE.equals(body.bidirectional)) {
                routeService.createConnection(
                        body.targetDestinationId,
                        body.sourceDestinationId,
                        body.distanceKm,
                        body.estimatedTravelCost,
                        body.estimatedTravelMinutes
                );
            }

            if (ok) {
                sendJsonResponse(resp, HttpServletResponse.SC_CREATED, Map.of(
                        "success", true,
                        "message", "Travel connection successfully created."
                ));
            } else {
                sendErrorResponse(resp, HttpServletResponse.SC_CONFLICT, "Failed to create connection. Verify that destinations exist.");
            }
        } catch (Exception e) {
            sendErrorResponse(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Creation error: " + e.getMessage());
        }
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        User admin = requireAdmin(req, resp);
        if (admin == null) return;

        try {
            ConnectionRequest body = parseJsonBody(req, ConnectionRequest.class);
            if (body == null || body.id <= 0) {
                sendErrorResponse(resp, HttpServletResponse.SC_BAD_REQUEST, "Valid connection ID is required.");
                return;
            }

            boolean ok = routeService.updateConnection(
                    body.id,
                    body.distanceKm,
                    body.estimatedTravelCost,
                    body.estimatedTravelMinutes
            );

            if (ok) {
                sendJsonResponse(resp, HttpServletResponse.SC_OK, Map.of(
                        "success", true,
                        "message", "Travel connection updated successfully."
                ));
            } else {
                sendErrorResponse(resp, HttpServletResponse.SC_NOT_FOUND, "Connection not found or update failed.");
            }
        } catch (Exception e) {
            sendErrorResponse(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Update error: " + e.getMessage());
        }
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        User admin = requireAdmin(req, resp);
        if (admin == null) return;

        String idParam = req.getParameter("id");
        if (idParam == null || idParam.isBlank()) {
            sendErrorResponse(resp, HttpServletResponse.SC_BAD_REQUEST, "Connection ID parameter is required.");
            return;
        }

        try {
            int id = Integer.parseInt(idParam.trim());
            boolean ok = routeService.deleteConnection(id);
            if (ok) {
                sendJsonResponse(resp, HttpServletResponse.SC_OK, Map.of(
                        "success", true,
                        "message", "Travel connection deleted successfully."
                ));
            } else {
                sendErrorResponse(resp, HttpServletResponse.SC_NOT_FOUND, "Connection not found or could not be deleted.");
            }
        } catch (NumberFormatException e) {
            sendErrorResponse(resp, HttpServletResponse.SC_BAD_REQUEST, "Invalid connection ID.");
        }
    }
}
