package com.wayfarer.algorithm;

import com.wayfarer.ds.TravelGraph;
import com.wayfarer.model.Destination;

import java.util.*;

/**
 * Wayfarer — Greedy Destination Recommendation Algorithm
 *
 * Academic Algorithm Requirement 4:
 * Iteratively makes locally optimal destination choices based on dynamic multi-factor scoring:
 *   - Rating Score (weight 0.40): Higher rated destinations prioritized
 *   - Category Affinity (weight 0.35): Matches user's preferred travel theme
 *   - Budget Viability (weight 0.25): Rewards affordable stops within remaining budget
 *   - Route Connectivity (bonus +0.10): Extra affinity for directly connected graph neighbors
 *
 * Theoretical Note: The Greedy algorithm selects the maximum score at each iteration.
 * It is demonstrable, fast (O(K * N)), and locally optimal, but does not guarantee
 * a globally optimal combination (as would be explored by exponential search).
 */
public class GreedyRecommendationService {

    public static class RecommendationRequest {
        public String startDestinationCode;
        public String preferredCategory;
        public Double totalBudget;
        public int desiredStops = 4;
        public boolean requireConnectivity = false;
    }

    public static class RecommendedStop {
        public int stepOrder;
        public Destination destination;
        public double greedyScore;
        public double remainingBudgetAfter;
        public String rationale;
        public boolean isDirectConnection;

        public RecommendedStop(int stepOrder, Destination destination, double greedyScore,
                               double remainingBudgetAfter, String rationale, boolean isDirectConnection) {
            this.stepOrder = stepOrder;
            this.destination = destination;
            this.greedyScore = Math.round(greedyScore * 1000.0) / 1000.0;
            this.remainingBudgetAfter = remainingBudgetAfter;
            this.rationale = rationale;
            this.isDirectConnection = isDirectConnection;
        }
    }

    public static class RecommendationResult {
        public boolean success;
        public List<RecommendedStop> stops = new ArrayList<>();
        public double totalEstimatedCost = 0.0;
        public double remainingBudget = 0.0;
        public String algorithmType = "Greedy Local Optimization";
        public String message;
        public String theoreticalNote = "Greedy heuristics make the highest-scoring local choice at each step without backtracking.";

        public static RecommendationResult failure(String message) {
            RecommendationResult r = new RecommendationResult();
            r.success = false;
            r.message = message;
            return r;
        }
    }

    public RecommendationResult generateRecommendations(
            List<Destination> allDestinations,
            TravelGraph graph,
            RecommendationRequest request) {

        if (allDestinations == null || allDestinations.isEmpty()) {
            return RecommendationResult.failure("Destination catalog is empty. Cannot generate recommendations.");
        }

        int targetStops = request.desiredStops > 0 ? Math.min(request.desiredStops, allDestinations.size()) : 4;
        double currentBudget = request.totalBudget != null && request.totalBudget > 0 ? request.totalBudget : Double.MAX_VALUE;

        // Map destinations by code
        Map<String, Destination> destByCode = new HashMap<>();
        for (Destination d : allDestinations) {
            destByCode.put(d.getDestinationCode().toUpperCase(), d);
        }

        Set<String> selectedCodes = new HashSet<>();
        List<RecommendedStop> resultStops = new ArrayList<>();
        double accumulatedCost = 0.0;

        // 1. Identify starting destination if specified
        Destination currentAnchor = null;
        if (request.startDestinationCode != null && !request.startDestinationCode.trim().isEmpty()) {
            String startCode = request.startDestinationCode.trim().toUpperCase();
            currentAnchor = destByCode.get(startCode);
            if (currentAnchor != null) {
                selectedCodes.add(startCode);
                accumulatedCost += currentAnchor.getEstimatedCost();
                double budgetLeft = currentBudget < Double.MAX_VALUE ? (currentBudget - accumulatedCost) : Double.MAX_VALUE;

                resultStops.add(new RecommendedStop(
                        1,
                        currentAnchor,
                        1.0,
                        budgetLeft < Double.MAX_VALUE ? Math.max(0, budgetLeft) : -1,
                        "Starting anchor selected by traveler.",
                        false
                ));
            }
        }

        // 2. Greedily pick remaining stops
        while (resultStops.size() < targetStops) {
            Destination bestCandidate = null;
            double bestScore = -1.0;
            String bestRationale = "";
            boolean bestIsDirect = false;

            // Direct neighbors of current anchor if connectivity is enabled
            Set<String> neighborCodes = new HashSet<>();
            if (currentAnchor != null && graph != null) {
                for (TravelGraph.Edge edge : graph.getNeighbors(currentAnchor.getDestinationCode())) {
                    neighborCodes.add(edge.getTargetCode().toUpperCase());
                }
            }

            for (Destination candidate : allDestinations) {
                String candidateCode = candidate.getDestinationCode().toUpperCase();
                if (selectedCodes.contains(candidateCode)) {
                    continue; // Skip already visited/selected
                }

                // Check budget viability
                if (currentBudget < Double.MAX_VALUE && (accumulatedCost + candidate.getEstimatedCost() > currentBudget)) {
                    continue; // Exceeds budget limit
                }

                boolean isDirect = neighborCodes.contains(candidateCode);
                if (request.requireConnectivity && !neighborCodes.isEmpty() && !isDirect) {
                    continue; // Strict connectivity required
                }

                // Multi-factor greedy scoring formula
                // 1. Normalized rating [0.0 - 1.0] -> 40%
                double ratingScore = Math.min(5.0, Math.max(0.0, candidate.getRating())) / 5.0 * 0.40;

                // 2. Category Affinity -> 35%
                double categoryScore = 0.10;
                if (request.preferredCategory != null && !request.preferredCategory.isBlank()) {
                    if (candidate.getCategory() != null &&
                            candidate.getCategory().equalsIgnoreCase(request.preferredCategory.trim())) {
                        categoryScore = 0.35;
                    } else if (candidate.getDescription() != null &&
                            candidate.getDescription().toLowerCase().contains(request.preferredCategory.toLowerCase())) {
                        categoryScore = 0.25;
                    }
                } else {
                    categoryScore = 0.25; // neutral if no preference
                }

                // 3. Budget Efficiency -> 25%
                double budgetScore = 0.25;
                if (currentBudget < Double.MAX_VALUE) {
                    double costRatio = candidate.getEstimatedCost() / currentBudget;
                    budgetScore = Math.max(0.05, 0.25 * (1.0 - Math.min(1.0, costRatio)));
                }

                // 4. Connectivity Bonus -> +10%
                double connectivityBonus = isDirect ? 0.10 : 0.0;

                double totalScore = ratingScore + categoryScore + budgetScore + connectivityBonus;

                if (totalScore > bestScore) {
                    bestScore = totalScore;
                    bestCandidate = candidate;
                    bestIsDirect = isDirect;

                    List<String> rationaleTokens = new ArrayList<>();
                    if (candidate.getRating() >= 4.5) rationaleTokens.add("exceptional rating (" + candidate.getRating() + "★)");
                    if (categoryScore >= 0.30) rationaleTokens.add("matches '" + candidate.getCategory() + "' interest");
                    if (isDirect) rationaleTokens.add("direct transit link available");
                    if (budgetScore >= 0.18) rationaleTokens.add("high cost-to-value ratio");

                    bestRationale = "Greedy selected: " + String.join(", ", rationaleTokens) + ".";
                }
            }

            if (bestCandidate == null) {
                // No further candidate fits constraints
                break;
            }

            // Lock in greedy choice
            selectedCodes.add(bestCandidate.getDestinationCode().toUpperCase());
            accumulatedCost += bestCandidate.getEstimatedCost();
            currentAnchor = bestCandidate;

            double budgetLeft = currentBudget < Double.MAX_VALUE ? (currentBudget - accumulatedCost) : -1;
            resultStops.add(new RecommendedStop(
                    resultStops.size() + 1,
                    bestCandidate,
                    bestScore,
                    budgetLeft,
                    bestRationale,
                    bestIsDirect
            ));
        }

        if (resultStops.isEmpty()) {
            return RecommendationResult.failure("No destinations matched the requested budget and preference criteria.");
        }

        RecommendationResult res = new RecommendationResult();
        res.success = true;
        res.stops = resultStops;
        res.totalEstimatedCost = Math.round(accumulatedCost * 100.0) / 100.0;
        res.remainingBudget = currentBudget < Double.MAX_VALUE ? Math.round((currentBudget - accumulatedCost) * 100.0) / 100.0 : -1;
        res.message = String.format("Greedy recommendation synthesized %d stops totaling $%.2f.",
                resultStops.size(), res.totalEstimatedCost);

        return res;
    }
}
