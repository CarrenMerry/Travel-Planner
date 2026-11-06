package com.wayfarer.servlet;

import com.wayfarer.model.Itinerary;
import com.wayfarer.model.User;
import com.wayfarer.service.ItineraryService;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Wayfarer — Itinerary Management Controller
 * Handles user trip itineraries, stop management via CustomLinkedList, and drag-and-drop reordering.
 */
@WebServlet(name = "wayfarerItineraryServlet", urlPatterns = "/api/itineraries/*")
public class ItineraryServlet extends BaseServlet {

    private final ItineraryService itineraryService = ItineraryService.getInstance();

    public static class ItineraryCreateRequest {
        public String name;
        public double budget;
    }

    public static class ItineraryUpdateRequest {
        public int id;
        public String name;
        public double budget;
        public String status;
    }

    public static class AddStopRequest {
        public int itineraryId;
        public int destinationId;
        public Integer insertIndex;
        public String notes;
    }

    public static class ReorderStopsRequest {
        public int itineraryId;
        public int fromIndex;
        public int toIndex;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        User user = requireAuth(req, resp);
        if (user == null) return;

        String idParam = req.getParameter("id");
        if (idParam != null && !idParam.isBlank()) {
            try {
                int id = Integer.parseInt(idParam.trim());
                Itinerary it = itineraryService.getItinerary(id, user.getId(), user.isAdmin());
                if (it != null) {
                    Map<String, Object> data = new HashMap<>();
                    data.put("id", it.getId());
                    data.put("name", it.getName());
                    data.put("budget", it.getBudget());
                    data.put("totalEstimatedCost", it.getTotalEstimatedCost());
                    data.put("totalDurationDays", it.getTotalDurationDays());
                    data.put("status", it.getStatus());
                    data.put("stops", it.getStops());
                    data.put("userId", it.getUserId());
                    sendJsonResponse(resp, HttpServletResponse.SC_OK, data);
                } else {
                    sendErrorResponse(resp, HttpServletResponse.SC_NOT_FOUND, "Itinerary not found.");
                }
                return;
            } catch (SecurityException se) {
                sendErrorResponse(resp, HttpServletResponse.SC_FORBIDDEN, se.getMessage());
                return;
            } catch (NumberFormatException e) {
                sendErrorResponse(resp, HttpServletResponse.SC_BAD_REQUEST, "Invalid itinerary ID format.");
                return;
            }
        }

        // Return all itineraries for the authenticated user
        List<Itinerary> userList = itineraryService.getUserItineraries(user.getId());
        sendJsonResponse(resp, HttpServletResponse.SC_OK, userList);
    }

    public static class MoveStopRequest {
        public int itineraryId;
        public int index;
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        User user = requireAuth(req, resp);
        if (user == null) return;

        String pathInfo = req.getPathInfo();

        if (pathInfo != null && pathInfo.equalsIgnoreCase("/stops")) {
            handleAddStop(req, resp, user);
        } else if (pathInfo != null && pathInfo.equalsIgnoreCase("/reorder")) {
            handleReorderStops(req, resp, user);
        } else if (pathInfo != null && pathInfo.equalsIgnoreCase("/move-up")) {
            handleMoveStop(req, resp, user, true);
        } else if (pathInfo != null && pathInfo.equalsIgnoreCase("/move-down")) {
            handleMoveStop(req, resp, user, false);
        } else {
            handleCreateItinerary(req, resp, user);
        }
    }

    private void handleMoveStop(HttpServletRequest req, HttpServletResponse resp, User user, boolean isUp) throws IOException {
        try {
            MoveStopRequest body = parseJsonBody(req, MoveStopRequest.class);
            if (body == null || body.itineraryId <= 0) {
                sendErrorResponse(resp, HttpServletResponse.SC_BAD_REQUEST, "Valid itinerary ID is required.");
                return;
            }

            boolean ok = isUp ?
                    itineraryService.moveStopUp(body.itineraryId, user.getId(), user.isAdmin(), body.index) :
                    itineraryService.moveStopDown(body.itineraryId, user.getId(), user.isAdmin(), body.index);

            if (ok) {
                Itinerary updated = itineraryService.getItinerary(body.itineraryId, user.getId(), user.isAdmin());
                sendJsonResponse(resp, HttpServletResponse.SC_OK, Map.of(
                        "success", true,
                        "message", (isUp ? "Moved stop up" : "Moved stop down") + " in custom linked list.",
                        "itinerary", updated
                ));
            } else {
                sendErrorResponse(resp, HttpServletResponse.SC_BAD_REQUEST, "Cannot move stop beyond boundaries.");
            }
        } catch (SecurityException se) {
            sendErrorResponse(resp, HttpServletResponse.SC_FORBIDDEN, se.getMessage());
        } catch (Exception e) {
            sendErrorResponse(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Move stop error: " + e.getMessage());
        }
    }

    private void handleCreateItinerary(HttpServletRequest req, HttpServletResponse resp, User user) throws IOException {
        try {
            ItineraryCreateRequest body = parseJsonBody(req, ItineraryCreateRequest.class);
            String name = (body != null && body.name != null && !body.name.isBlank()) ? body.name : "My Scenic Journey";
            double budget = body != null ? body.budget : 0.0;

            int createdId = itineraryService.createItinerary(user.getId(), name, budget);
            if (createdId > 0) {
                Itinerary created = itineraryService.getItinerary(createdId, user.getId(), user.isAdmin());
                sendJsonResponse(resp, HttpServletResponse.SC_CREATED, created);
            } else {
                sendErrorResponse(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Failed to create itinerary.");
            }
        } catch (Exception e) {
            sendErrorResponse(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Creation error: " + e.getMessage());
        }
    }

    private void handleAddStop(HttpServletRequest req, HttpServletResponse resp, User user) throws IOException {
        try {
            AddStopRequest body = parseJsonBody(req, AddStopRequest.class);
            if (body == null || body.itineraryId <= 0 || body.destinationId <= 0) {
                sendErrorResponse(resp, HttpServletResponse.SC_BAD_REQUEST, "Valid itinerary ID and destination ID are required.");
                return;
            }

            boolean ok = itineraryService.addStop(
                    body.itineraryId,
                    user.getId(),
                    user.isAdmin(),
                    body.destinationId,
                    body.insertIndex,
                    body.notes
            );

            if (ok) {
                Itinerary updated = itineraryService.getItinerary(body.itineraryId, user.getId(), user.isAdmin());
                sendJsonResponse(resp, HttpServletResponse.SC_OK, Map.of(
                        "success", true,
                        "message", "Stop added to custom linked list and saved to database.",
                        "itinerary", updated
                ));
            } else {
                sendErrorResponse(resp, HttpServletResponse.SC_BAD_REQUEST, "Could not add stop. Check destination existence.");
            }
        } catch (SecurityException se) {
            sendErrorResponse(resp, HttpServletResponse.SC_FORBIDDEN, se.getMessage());
        } catch (Exception e) {
            sendErrorResponse(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Add stop error: " + e.getMessage());
        }
    }

    private void handleReorderStops(HttpServletRequest req, HttpServletResponse resp, User user) throws IOException {
        try {
            ReorderStopsRequest body = parseJsonBody(req, ReorderStopsRequest.class);
            if (body == null || body.itineraryId <= 0) {
                sendErrorResponse(resp, HttpServletResponse.SC_BAD_REQUEST, "Valid itinerary ID is required.");
                return;
            }

            boolean ok = itineraryService.reorderStops(
                    body.itineraryId,
                    user.getId(),
                    user.isAdmin(),
                    body.fromIndex,
                    body.toIndex
            );

            if (ok) {
                Itinerary updated = itineraryService.getItinerary(body.itineraryId, user.getId(), user.isAdmin());
                sendJsonResponse(resp, HttpServletResponse.SC_OK, Map.of(
                        "success", true,
                        "message", "Stops reordered in custom linked list and synchronized to database.",
                        "itinerary", updated
                ));
            } else {
                sendErrorResponse(resp, HttpServletResponse.SC_BAD_REQUEST, "Reorder failed. Invalid indices.");
            }
        } catch (SecurityException se) {
            sendErrorResponse(resp, HttpServletResponse.SC_FORBIDDEN, se.getMessage());
        } catch (Exception e) {
            sendErrorResponse(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Reorder error: " + e.getMessage());
        }
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        User user = requireAuth(req, resp);
        if (user == null) return;

        try {
            ItineraryUpdateRequest body = parseJsonBody(req, ItineraryUpdateRequest.class);
            if (body == null || body.id <= 0) {
                sendErrorResponse(resp, HttpServletResponse.SC_BAD_REQUEST, "Valid itinerary ID is required.");
                return;
            }

            boolean ok = itineraryService.updateItineraryHeader(
                    body.id,
                    user.getId(),
                    user.isAdmin(),
                    body.name,
                    body.budget,
                    body.status
            );

            if (ok) {
                sendJsonResponse(resp, HttpServletResponse.SC_OK, Map.of(
                        "success", true,
                        "message", "Itinerary details updated successfully."
                ));
            } else {
                sendErrorResponse(resp, HttpServletResponse.SC_NOT_FOUND, "Itinerary not found or update failed.");
            }
        } catch (SecurityException se) {
            sendErrorResponse(resp, HttpServletResponse.SC_FORBIDDEN, se.getMessage());
        } catch (Exception e) {
            sendErrorResponse(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Update error: " + e.getMessage());
        }
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        User user = requireAuth(req, resp);
        if (user == null) return;

        String pathInfo = req.getPathInfo();

        if (pathInfo != null && pathInfo.equalsIgnoreCase("/stops")) {
            // Delete a stop at index
            String itIdParam = req.getParameter("itineraryId");
            String indexParam = req.getParameter("index");

            if (itIdParam == null || indexParam == null) {
                sendErrorResponse(resp, HttpServletResponse.SC_BAD_REQUEST, "itineraryId and index query parameters are required.");
                return;
            }

            try {
                int itId = Integer.parseInt(itIdParam.trim());
                int index = Integer.parseInt(indexParam.trim());

                boolean ok = itineraryService.removeStop(itId, user.getId(), user.isAdmin(), index);
                if (ok) {
                    Itinerary updated = itineraryService.getItinerary(itId, user.getId(), user.isAdmin());
                    sendJsonResponse(resp, HttpServletResponse.SC_OK, Map.of(
                            "success", true,
                            "message", "Stop removed from custom linked list and database updated.",
                            "itinerary", updated
                    ));
                } else {
                    sendErrorResponse(resp, HttpServletResponse.SC_BAD_REQUEST, "Could not remove stop at index " + index);
                }
            } catch (SecurityException se) {
                sendErrorResponse(resp, HttpServletResponse.SC_FORBIDDEN, se.getMessage());
            } catch (Exception e) {
                sendErrorResponse(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Delete stop error: " + e.getMessage());
            }
            return;
        }

        // Delete entire itinerary
        String idParam = req.getParameter("id");
        if (idParam == null || idParam.isBlank()) {
            sendErrorResponse(resp, HttpServletResponse.SC_BAD_REQUEST, "Itinerary ID parameter is required.");
            return;
        }

        try {
            int id = Integer.parseInt(idParam.trim());
            boolean ok = itineraryService.deleteItinerary(id, user.getId(), user.isAdmin());
            if (ok) {
                sendJsonResponse(resp, HttpServletResponse.SC_OK, Map.of(
                        "success", true,
                        "message", "Itinerary and all stops deleted successfully."
                ));
            } else {
                sendErrorResponse(resp, HttpServletResponse.SC_NOT_FOUND, "Itinerary not found or could not be deleted.");
            }
        } catch (SecurityException se) {
            sendErrorResponse(resp, HttpServletResponse.SC_FORBIDDEN, se.getMessage());
        } catch (NumberFormatException e) {
            sendErrorResponse(resp, HttpServletResponse.SC_BAD_REQUEST, "Invalid itinerary ID.");
        }
    }
}
