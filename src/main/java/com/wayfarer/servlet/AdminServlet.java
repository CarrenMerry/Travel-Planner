package com.wayfarer.servlet;

import com.wayfarer.dao.UserDAO;
import com.wayfarer.model.Itinerary;
import com.wayfarer.model.User;
import com.wayfarer.service.DestinationService;
import com.wayfarer.service.ItineraryService;
import com.wayfarer.service.RouteService;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Wayfarer — Administrator Controller
 * Strictly enforces admin authentication on the server for statistics, user management, and audit inspection.
 */
@WebServlet(name = "wayfarerAdminServlet", urlPatterns = "/api/admin/*")
public class AdminServlet extends BaseServlet {

    private final UserDAO userDAO = new UserDAO();
    private final DestinationService destinationService = DestinationService.getInstance();
    private final RouteService routeService = RouteService.getInstance();
    private final ItineraryService itineraryService = ItineraryService.getInstance();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        User admin = requireAdmin(req, resp);
        if (admin == null) return;

        String pathInfo = req.getPathInfo();

        if (pathInfo != null && pathInfo.equalsIgnoreCase("/stats")) {
            Map<String, Object> stats = new HashMap<>();
            stats.put("totalDestinations", destinationService.count());
            stats.put("totalUsers", userDAO.count());
            stats.put("totalConnections", routeService.countConnections());
            stats.put("totalItineraries", itineraryService.count());
            sendJsonResponse(resp, HttpServletResponse.SC_OK, stats);
            return;
        }

        if (pathInfo != null && pathInfo.equalsIgnoreCase("/users")) {
            List<User> allUsers = userDAO.findAll();
            List<Map<String, Object>> safeUsers = new ArrayList<>();
            for (User u : allUsers) {
                Map<String, Object> m = new HashMap<>();
                m.put("id", u.getId());
                m.put("name", u.getName());
                m.put("email", u.getEmail());
                m.put("role", u.getRole().name());
                m.put("createdAt", u.getCreatedAt());
                safeUsers.add(m);
            }
            sendJsonResponse(resp, HttpServletResponse.SC_OK, safeUsers);
            return;
        }

        if (pathInfo != null && pathInfo.equalsIgnoreCase("/itineraries")) {
            List<Itinerary> allItineraries = itineraryService.getAllItineraries();
            sendJsonResponse(resp, HttpServletResponse.SC_OK, allItineraries);
            return;
        }

        sendErrorResponse(resp, HttpServletResponse.SC_NOT_FOUND, "Admin endpoint not found.");
    }
}
