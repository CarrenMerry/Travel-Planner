package com.travelplanner.algorithms;

import com.travelplanner.model.Connection;
import com.travelplanner.model.Destination;

import java.util.ArrayList;
import java.util.List;

/**
 * Greedy Route Optimizer.
 * Selects the next destination sequentially based on a documented local selection heuristic:
 * Heuristic: Score = (Rating * 100) / (Distance + 1) or Lowest Cost per Value.
 * Shows candidate destinations evaluated at each step, why the choice was made, and explains
 * that greedy selection does not guarantee a globally minimal route.
 */
public class GreedyOptimizer {

    public static class CandidateEvaluation {
        public String destinationId;
        public String destinationName;
        public double distanceKm;
        public double cost;
        public double rating;
        public double score;
        public String evaluationFormula;

        public CandidateEvaluation(String destinationId, String destinationName, double distanceKm,
                                   double cost, double rating, double score, String formula) {
            this.destinationId = destinationId;
            this.destinationName = destinationName;
            this.distanceKm = distanceKm;
            this.cost = cost;
            this.rating = rating;
            this.score = score;
            this.evaluationFormula = formula;
        }
    }

    public static class GreedyStep {
        public int stepIndex;
        public String fromDestinationId;
        public String fromDestinationName;
        public List<CandidateEvaluation> candidatesConsidered = new ArrayList<>();
        public CandidateEvaluation chosenDestination;
        public String rationale;

        public GreedyStep(int stepIndex, String fromDestinationId, String fromDestinationName) {
            this.stepIndex = stepIndex;
            this.fromDestinationId = fromDestinationId;
            this.fromDestinationName = fromDestinationName;
        }
    }

    public static class GreedyResult {
        public String startingDestinationId;
        public String heuristicName;
        public List<Destination> selectedRoute = new ArrayList<>();
        public List<GreedyStep> steps = new ArrayList<>();
        public double totalDistanceKm;
        public double totalCost;
        public double averageRating;
        public String theoreticalExplanation;

        public GreedyResult(String startId, String heuristic) {
            this.startingDestinationId = startId;
            this.heuristicName = heuristic;
        }
    }

    /**
     * Executes greedy route selection starting from startId visiting up to maxStops destinations.
     * Local rule: Among all unvisited connected or reachable destinations, choose candidate
     * maximizing score = (Rating * 100) / (DistanceKm + 10).
     */
    public static GreedyResult optimize(List<Destination> allDestinations, String startId, int maxStops) {
        GreedyResult result = new GreedyResult(startId, "Max Value-to-Distance Ratio: (Rating * 100) / (Distance + 10)");
        result.theoreticalExplanation = "The Greedy algorithm makes the locally optimal choice at each step without reconsidering prior decisions. "
                + "While fast (O(V^2)), it may get trapped in a dead end or miss a globally shorter route that starts with a higher initial cost.";

        if (allDestinations == null || allDestinations.isEmpty()) {
            return result;
        }

        Destination startDest = findDestination(allDestinations, startId);
        if (startDest == null) {
            startDest = allDestinations.get(0);
        }

        List<Destination> visited = new ArrayList<>();
        visited.add(startDest);
        result.selectedRoute.add(startDest);
        result.totalCost += startDest.getEstimatedCost();

        Destination current = startDest;
        int stepNum = 1;

        while (visited.size() < Math.min(allDestinations.size(), maxStops)) {
            GreedyStep step = new GreedyStep(stepNum++, current.getId(), current.getName());
            CandidateEvaluation bestCandidate = null;
            Destination bestNextDest = null;
            double bestScore = -Double.MAX_VALUE;

            for (Destination candidate : allDestinations) {
                if (visited.contains(candidate)) continue;

                // Find connection distance if exists, otherwise estimate default distance
                double distance = 150.0; // fallback base distance
                for (Connection c : current.getConnections()) {
                    if (c.getTargetId().equalsIgnoreCase(candidate.getId())) {
                        distance = c.getDistanceKm();
                        break;
                    }
                }

                // Heuristic Score: Higher rating is rewarded, longer distance is penalized
                double score = (candidate.getRating() * 100.0) / (distance + 10.0);
                String formula = "(" + candidate.getRating() + " * 100) / (" + distance + " + 10) = " + String.format("%.2f", score);

                CandidateEvaluation eval = new CandidateEvaluation(
                        candidate.getId(),
                        candidate.getName(),
                        distance,
                        candidate.getEstimatedCost(),
                        candidate.getRating(),
                        score,
                        formula
                );
                step.candidatesConsidered.add(eval);

                if (score > bestScore) {
                    bestScore = score;
                    bestCandidate = eval;
                    bestNextDest = candidate;
                }
            }

            if (bestNextDest == null) {
                // No more reachable unvisited destinations
                break;
            }

            step.chosenDestination = bestCandidate;
            step.rationale = "Selected '" + bestCandidate.destinationName + "' because it offered the highest local score ("
                    + String.format("%.2f", bestCandidate.score) + ") among " + step.candidatesConsidered.size() + " considered destinations.";
            result.steps.add(step);

            visited.add(bestNextDest);
            result.selectedRoute.add(bestNextDest);
            result.totalDistanceKm += bestCandidate.distanceKm;
            result.totalCost += bestNextDest.getEstimatedCost();
            current = bestNextDest;
        }

        // Calculate average rating
        double sumRating = 0;
        for (Destination d : result.selectedRoute) {
            sumRating += d.getRating();
        }
        result.averageRating = result.selectedRoute.isEmpty() ? 0 : sumRating / result.selectedRoute.size();

        return result;
    }

    private static Destination findDestination(List<Destination> list, String id) {
        for (Destination d : list) {
            if (d.getId().equalsIgnoreCase(id)) return d;
        }
        return null;
    }
}
