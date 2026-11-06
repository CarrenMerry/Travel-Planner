package com.travelplanner.algorithms;

import com.travelplanner.model.Destination;

import java.util.ArrayList;
import java.util.List;

/**
 * Backtracking Itinerary Generator with Multi-Constraint Pruning.
 * Recursively builds candidate destination sequences while verifying:
 * 1. Cumulative Cost <= maxBudget
 * 2. Cumulative Duration <= maxDuration
 * 3. No duplicate destination visits
 *
 * Prunes non-viable branches immediately when any constraint is violated,
 * logging the precise reason for abandonment.
 */
public class BacktrackingPlanner {

    public static class PruningLog {
        public int stepNumber;
        public String partialItinerary;
        public String candidateDestination;
        public String rejectionReason;
        public double currentCost;
        public double currentDuration;

        public PruningLog(int stepNumber, String partialItinerary, String candidateDestination,
                          String rejectionReason, double currentCost, double currentDuration) {
            this.stepNumber = stepNumber;
            this.partialItinerary = partialItinerary;
            this.candidateDestination = candidateDestination;
            this.rejectionReason = rejectionReason;
            this.currentCost = currentCost;
            this.currentDuration = currentDuration;
        }
    }

    public static class ValidItinerary {
        public List<Destination> destinations = new ArrayList<>();
        public double totalCost;
        public double totalDuration;
        public double averageRating;

        public ValidItinerary(List<Destination> list, double cost, double duration, double avgRating) {
            this.destinations = new ArrayList<>(list);
            this.totalCost = cost;
            this.totalDuration = duration;
            this.averageRating = avgRating;
        }
    }

    public static class BacktrackingResult {
        public double maxBudget;
        public double maxDuration;
        public int totalStatesExplored;
        public int branchesPruned;
        public List<ValidItinerary> validItineraries = new ArrayList<>();
        public List<PruningLog> pruningLogs = new ArrayList<>();
        public String explanation;

        public BacktrackingResult(double budget, double duration) {
            this.maxBudget = budget;
            this.maxDuration = duration;
        }
    }

    private static final int MAX_RESULTS = 10;
    private static final int MAX_EXPLORATION_STEPS = 5000;
    private static final int MAX_LOGS = 25; // keep log responsive

    /**
     * Generates valid itineraries within budget and duration limits using recursive backtracking.
     */
    public static BacktrackingResult generateItineraries(List<Destination> allDestinations,
                                                         double maxBudget,
                                                         double maxDuration,
                                                         int minStops) {
        BacktrackingResult result = new BacktrackingResult(maxBudget, maxDuration);
        result.explanation = "Backtracking explores candidate solution paths depth-first. "
                + "When a partial sequence violates either the budget or duration constraint, "
                + "the algorithm immediately prunes that entire sub-tree and backtracks to try alternatives.";

        if (allDestinations == null || allDestinations.isEmpty()) {
            return result;
        }

        List<Destination> currentPath = new ArrayList<>();
        boolean[] used = new boolean[allDestinations.size()];

        backtrack(allDestinations, used, currentPath, 0.0, 0.0, maxBudget, maxDuration, minStops, result);

        return result;
    }

    private static void backtrack(List<Destination> allDestinations,
                                  boolean[] used,
                                  List<Destination> currentPath,
                                  double currentCost,
                                  double currentDuration,
                                  double maxBudget,
                                  double maxDuration,
                                  int minStops,
                                  BacktrackingResult result) {
        result.totalStatesExplored++;

        if (result.validItineraries.size() >= MAX_RESULTS || result.totalStatesExplored >= MAX_EXPLORATION_STEPS) {
            return;
        }

        // If path meets minimum stops criterion, record it as a valid itinerary
        if (currentPath.size() >= minStops && !currentPath.isEmpty()) {
            double avgRating = 0;
            for (Destination d : currentPath) avgRating += d.getRating();
            avgRating /= currentPath.size();

            result.validItineraries.add(new ValidItinerary(currentPath, currentCost, currentDuration, avgRating));
        }

        // Try adding the next destination
        for (int i = 0; i < allDestinations.size(); i++) {
            if (used[i]) continue;

            Destination candidate = allDestinations.get(i);
            double nextCost = currentCost + candidate.getEstimatedCost();
            double nextDuration = currentDuration + candidate.getVisitDuration();

            // Constraint Checking & Pruning
            if (nextCost > maxBudget) {
                result.branchesPruned++;
                if (result.pruningLogs.size() < MAX_LOGS) {
                    result.pruningLogs.add(new PruningLog(
                            result.totalStatesExplored,
                            formatPath(currentPath),
                            candidate.getName(),
                            "Budget Exceeded: Cumulative cost $" + String.format("%.0f", nextCost) + " > limit $" + String.format("%.0f", maxBudget),
                            nextCost,
                            nextDuration
                    ));
                }
                continue; // PRUNE branch
            }

            if (nextDuration > maxDuration) {
                result.branchesPruned++;
                if (result.pruningLogs.size() < MAX_LOGS) {
                    result.pruningLogs.add(new PruningLog(
                            result.totalStatesExplored,
                            formatPath(currentPath),
                            candidate.getName(),
                            "Duration Exceeded: Cumulative duration " + String.format("%.1f", nextDuration) + " days > limit " + String.format("%.1f", maxDuration) + " days",
                            nextCost,
                            nextDuration
                    ));
                }
                continue; // PRUNE branch
            }

            // Make choice
            used[i] = true;
            currentPath.add(candidate);

            // Explore recursively
            backtrack(allDestinations, used, currentPath, nextCost, nextDuration, maxBudget, maxDuration, minStops, result);

            // Backtrack: Undo choice
            currentPath.remove(currentPath.size() - 1);
            used[i] = false;

            if (result.validItineraries.size() >= MAX_RESULTS || result.totalStatesExplored >= MAX_EXPLORATION_STEPS) {
                return;
            }
        }
    }

    private static String formatPath(List<Destination> path) {
        if (path.isEmpty()) return "[Start]";
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < path.size(); i++) {
            sb.append(path.get(i).getName());
            if (i < path.size() - 1) sb.append(" -> ");
        }
        sb.append("]");
        return sb.toString();
    }
}
