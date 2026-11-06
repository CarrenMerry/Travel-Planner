package com.travelplanner.servlet;

import com.travelplanner.model.Destination;
import com.travelplanner.model.ItineraryAction;
import com.travelplanner.service.DestinationService;
import com.travelplanner.service.ItineraryService;
import com.travelplanner.service.QueueService;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.*;

@WebServlet("/api/dashboard")
public class DashboardServlet extends BaseServlet {

    private final DestinationService destinationService = DestinationService.getInstance();
    private final ItineraryService itineraryService = ItineraryService.getInstance();
    private final QueueService queueService = QueueService.getInstance();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        List<Destination> allDestinations = destinationService.getAllDestinations();
        List<Destination> itineraryItems = itineraryService.getItineraryList();
        ItineraryService.ItineraryStats stats = itineraryService.getStatistics();

        // Popular destinations: sorted by rating descending (top 4)
        List<Destination> popular = new ArrayList<>(allDestinations);
        popular.sort(Comparator.comparingDouble(Destination::getRating).reversed());
        if (popular.size() > 4) {
            popular = popular.subList(0, 4);
        }

        // Recent activity from undo stack
        List<ItineraryAction> recentActions = itineraryService.getUndoStack().toList();
        List<String> activityLog = new ArrayList<>();
        for (ItineraryAction a : recentActions) {
            activityLog.add(a.getDescription());
            if (activityLog.size() >= 5) break;
        }

        if (activityLog.isEmpty()) {
            activityLog.add("Connected to MySQL 'smart_travel_planner' database.");
            activityLog.add("Loaded " + allDestinations.size() + " destinations into in-memory BST and Graph.");
        }

        Map<String, Object> data = new HashMap<>();
        data.put("totalDestinations", allDestinations.size());
        data.put("itineraryCount", itineraryItems.size());
        data.put("totalCost", stats.totalCost);
        data.put("totalDuration", stats.totalDuration);
        data.put("averageRating", stats.averageRating);
        data.put("budget", stats.budget);
        data.put("popularDestinations", popular);
        data.put("recentActivities", activityLog);
        data.put("pendingQueueCount", queueService.getPendingCount());
        data.put("graphEdgeCount", destinationService.getGraph().getAllVertexIds().size());
        data.put("databaseConnected", true);

        sendJsonResponse(resp, HttpServletResponse.SC_OK, data);
    }
}
