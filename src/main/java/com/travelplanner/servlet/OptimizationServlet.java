package com.travelplanner.servlet;

import com.travelplanner.algorithms.BacktrackingPlanner;
import com.travelplanner.algorithms.DynamicProgrammingPlanner;
import com.travelplanner.algorithms.GreedyOptimizer;
import com.travelplanner.model.Activity;
import com.travelplanner.model.Destination;
import com.travelplanner.service.DestinationService;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@WebServlet("/api/optimize/*")
public class OptimizationServlet extends BaseServlet {

    private final DestinationService destinationService = DestinationService.getInstance();

    public static class GreedyRequest {
        public String startId;
        public int maxStops = 5;
    }

    public static class DpRequest {
        public double budget = 1200.0;
        public List<String> destinationIds; // optional filter
    }

    public static class BacktrackRequest {
        public double maxBudget = 3000.0;
        public double maxDuration = 10.0;
        public int minStops = 3;
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String pathInfo = req.getPathInfo();

        if (pathInfo != null) {
            if (pathInfo.equalsIgnoreCase("/greedy")) {
                handleGreedy(req, resp);
                return;
            } else if (pathInfo.equalsIgnoreCase("/dp")) {
                handleDp(req, resp);
                return;
            } else if (pathInfo.equalsIgnoreCase("/backtrack")) {
                handleBacktrack(req, resp);
                return;
            }
        }

        sendErrorResponse(resp, HttpServletResponse.SC_BAD_REQUEST, "Must post to /greedy, /dp, or /backtrack.");
    }

    private void handleGreedy(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        GreedyRequest body = parseJsonBody(req, GreedyRequest.class);
        String startId = (body != null && body.startId != null) ? body.startId : "DEST-01";
        int maxStops = (body != null && body.maxStops > 0) ? body.maxStops : 5;

        List<Destination> destinations = destinationService.getAllDestinations();
        GreedyOptimizer.GreedyResult result = GreedyOptimizer.optimize(destinations, startId, maxStops);
        sendJsonResponse(resp, HttpServletResponse.SC_OK, result);
    }

    private void handleDp(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        DpRequest body = parseJsonBody(req, DpRequest.class);
        double budget = (body != null && body.budget > 0) ? body.budget : 1200.0;

        List<Destination> destinations = destinationService.getAllDestinations();
        List<Activity> pool = new ArrayList<>();

        if (body != null && body.destinationIds != null && !body.destinationIds.isEmpty()) {
            for (String dId : body.destinationIds) {
                Destination d = destinationService.getById(dId);
                if (d != null) pool.addAll(d.getActivities());
            }
        } else {
            for (Destination d : destinations) {
                pool.addAll(d.getActivities());
            }
        }

        DynamicProgrammingPlanner.DpResult result = DynamicProgrammingPlanner.optimizeActivities(pool, budget);
        sendJsonResponse(resp, HttpServletResponse.SC_OK, result);
    }

    private void handleBacktrack(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        BacktrackRequest body = parseJsonBody(req, BacktrackRequest.class);
        double maxBudget = (body != null && body.maxBudget > 0) ? body.maxBudget : 3000.0;
        double maxDuration = (body != null && body.maxDuration > 0) ? body.maxDuration : 10.0;
        int minStops = (body != null && body.minStops > 0) ? body.minStops : 3;

        List<Destination> destinations = destinationService.getAllDestinations();
        BacktrackingPlanner.BacktrackingResult result = BacktrackingPlanner.generateItineraries(
                destinations, maxBudget, maxDuration, minStops);
        sendJsonResponse(resp, HttpServletResponse.SC_OK, result);
    }
}
