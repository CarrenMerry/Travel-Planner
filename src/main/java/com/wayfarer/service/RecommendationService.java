package com.wayfarer.service;

import com.wayfarer.algorithm.GreedyRecommendationService;
import com.wayfarer.model.Destination;

import java.util.List;

/**
 * Wayfarer — Smart Recommendation Service
 * Executes the Greedy recommendation algorithm on current database destinations.
 */
public class RecommendationService {

    private static final RecommendationService INSTANCE = new RecommendationService();
    private final GreedyRecommendationService greedyService = new GreedyRecommendationService();
    private final DestinationService destinationService = DestinationService.getInstance();
    private final RouteService routeService = RouteService.getInstance();

    public static RecommendationService getInstance() {
        return INSTANCE;
    }

    private RecommendationService() {}

    public GreedyRecommendationService.RecommendationResult recommend(GreedyRecommendationService.RecommendationRequest request) {
        List<Destination> all = destinationService.getAllDestinations();
        return greedyService.generateRecommendations(all, routeService.getGraph(), request);
    }
}
