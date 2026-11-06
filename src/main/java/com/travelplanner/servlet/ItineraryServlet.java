package com.travelplanner.servlet;

import com.travelplanner.model.Destination;
import com.travelplanner.service.ItineraryService;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@WebServlet("/api/itinerary/*")
public class ItineraryServlet extends BaseServlet {

    private final ItineraryService itineraryService = ItineraryService.getInstance();

    public static class AddItineraryRequest {
        public String destinationId;
        public Integer index;
    }

    public static class ReorderRequest {
        public int fromIndex;
        public int toIndex;
    }

    public static class BudgetRequest {
        public double budget;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        List<Destination> list = itineraryService.getItineraryList();
        ItineraryService.ItineraryStats stats = itineraryService.getStatistics();

        Map<String, Object> data = new HashMap<>();
        data.put("itinerary", list);
        data.put("stats", stats);
        sendJsonResponse(resp, HttpServletResponse.SC_OK, data);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String pathInfo = req.getPathInfo();

        if (pathInfo != null) {
            if (pathInfo.equalsIgnoreCase("/undo")) {
                handleUndo(resp);
                return;
            } else if (pathInfo.equalsIgnoreCase("/clear")) {
                handleClear(resp);
                return;
            } else if (pathInfo.equalsIgnoreCase("/reorder")) {
                handleReorder(req, resp);
                return;
            } else if (pathInfo.equalsIgnoreCase("/budget")) {
                handleBudget(req, resp);
                return;
            }
        }

        // Default POST: Add or insert into itinerary
        try {
            AddItineraryRequest body = parseJsonBody(req, AddItineraryRequest.class);
            if (body == null || body.destinationId == null || body.destinationId.trim().isEmpty()) {
                sendErrorResponse(resp, HttpServletResponse.SC_BAD_REQUEST, "Field 'destinationId' is required.");
                return;
            }

            boolean success;
            if (body.index != null && body.index >= 0) {
                success = itineraryService.insertDestination(body.index, body.destinationId);
            } else {
                success = itineraryService.addDestination(body.destinationId);
            }

            if (!success) {
                sendErrorResponse(resp, HttpServletResponse.SC_NOT_FOUND, "Destination not found with ID: " + body.destinationId);
                return;
            }

            Map<String, Object> res = new HashMap<>();
            res.put("success", true);
            res.put("message", "Destination added to itinerary and persisted in MySQL.");
            res.put("itinerary", itineraryService.getItineraryList());
            res.put("stats", itineraryService.getStatistics());
            sendJsonResponse(resp, HttpServletResponse.SC_OK, res);
        } catch (Exception e) {
            sendErrorResponse(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error adding to itinerary: " + e.getMessage());
        }
    }

    private void handleBudget(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            BudgetRequest body = parseJsonBody(req, BudgetRequest.class);
            if (body == null || body.budget < 0) {
                sendErrorResponse(resp, HttpServletResponse.SC_BAD_REQUEST, "Valid non-negative budget is required.");
                return;
            }

            itineraryService.updateBudget(body.budget);

            Map<String, Object> res = new HashMap<>();
            res.put("success", true);
            res.put("message", "Budget updated to $" + body.budget);
            res.put("stats", itineraryService.getStatistics());
            sendJsonResponse(resp, HttpServletResponse.SC_OK, res);
        } catch (Exception e) {
            sendErrorResponse(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    private void handleReorder(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            ReorderRequest body = parseJsonBody(req, ReorderRequest.class);
            if (body == null) {
                sendErrorResponse(resp, HttpServletResponse.SC_BAD_REQUEST, "Invalid reorder payload.");
                return;
            }

            boolean success = itineraryService.reorder(body.fromIndex, body.toIndex);
            if (!success) {
                sendErrorResponse(resp, HttpServletResponse.SC_BAD_REQUEST, "Invalid indices for reordering.");
                return;
            }

            Map<String, Object> res = new HashMap<>();
            res.put("success", true);
            res.put("message", "Itinerary reordered successfully and persisted in MySQL.");
            res.put("itinerary", itineraryService.getItineraryList());
            res.put("stats", itineraryService.getStatistics());
            sendJsonResponse(resp, HttpServletResponse.SC_OK, res);
        } catch (Exception e) {
            sendErrorResponse(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    private void handleUndo(HttpServletResponse resp) throws IOException {
        String undoMsg = itineraryService.undo();
        if (undoMsg == null) {
            sendErrorResponse(resp, HttpServletResponse.SC_BAD_REQUEST, "Undo stack is empty; nothing to undo.");
            return;
        }

        Map<String, Object> res = new HashMap<>();
        res.put("success", true);
        res.put("message", undoMsg);
        res.put("itinerary", itineraryService.getItineraryList());
        res.put("stats", itineraryService.getStatistics());
        sendJsonResponse(resp, HttpServletResponse.SC_OK, res);
    }

    private void handleClear(HttpServletResponse resp) throws IOException {
        itineraryService.clearItinerary();
        Map<String, Object> res = new HashMap<>();
        res.put("success", true);
        res.put("message", "Itinerary cleared.");
        res.put("itinerary", itineraryService.getItineraryList());
        res.put("stats", itineraryService.getStatistics());
        sendJsonResponse(resp, HttpServletResponse.SC_OK, res);
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String idParam = req.getParameter("id");
        String indexParam = req.getParameter("index");

        boolean success = false;
        if (indexParam != null && !indexParam.isEmpty()) {
            try {
                int idx = Integer.parseInt(indexParam);
                success = itineraryService.removeDestination(idx);
            } catch (NumberFormatException e) {
                sendErrorResponse(resp, HttpServletResponse.SC_BAD_REQUEST, "Invalid index: " + indexParam);
                return;
            }
        } else if (idParam != null && !idParam.isEmpty()) {
            success = itineraryService.removeDestinationById(idParam);
        } else {
            sendErrorResponse(resp, HttpServletResponse.SC_BAD_REQUEST, "Must provide 'id' or 'index' parameter to remove.");
            return;
        }

        if (!success) {
            sendErrorResponse(resp, HttpServletResponse.SC_NOT_FOUND, "Item not found in itinerary.");
            return;
        }

        Map<String, Object> res = new HashMap<>();
        res.put("success", true);
        res.put("message", "Destination removed from itinerary and updated in MySQL.");
        res.put("itinerary", itineraryService.getItineraryList());
        res.put("stats", itineraryService.getStatistics());
        sendJsonResponse(resp, HttpServletResponse.SC_OK, res);
    }
}
