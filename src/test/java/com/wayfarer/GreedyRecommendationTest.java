package com.wayfarer;

import com.wayfarer.algorithm.GreedyRecommendationService;
import com.wayfarer.ds.TravelGraph;
import com.wayfarer.model.Destination;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class GreedyRecommendationTest {

    private final GreedyRecommendationService service = new GreedyRecommendationService();
    private List<Destination> destinations;
    private TravelGraph graph;

    @BeforeEach
    void setUp() {
        destinations = new ArrayList<>();
        destinations.add(new Destination(1, "DEST-01", "Kyoto Temples", "Japan", "Kyoto", "Historical", "Temples", 500.0, 3, 4, 4.9, ""));
        destinations.add(new Destination(2, "DEST-02", "Bali Beach Retreat", "Indonesia", "Bali", "Relaxation", "Tropical beaches", 400.0, 4, 3, 4.8, ""));
        destinations.add(new Destination(3, "DEST-03", "Alpine Trek", "Switzerland", "Interlaken", "Adventure", "Mountain trails", 1200.0, 5, 5, 4.7, ""));
        destinations.add(new Destination(4, "DEST-04", "Rome Colosseum", "Italy", "Rome", "Historical", "Ancient ruins", 800.0, 3, 4, 4.9, ""));
        destinations.add(new Destination(5, "DEST-05", "Tokyo Metropolis", "Japan", "Tokyo", "Cultural", "Urban culture", 600.0, 4, 6, 4.6, ""));

        graph = new TravelGraph();
        graph.addEdge("DEST-01", "DEST-05", 450.0, 100.0, 120, true);
    }

    @Test
    @DisplayName("Verify Greedy selects highest-scoring category match within budget")
    void testGreedyCategoryAndBudgetSelection() {
        GreedyRecommendationService.RecommendationRequest req = new GreedyRecommendationService.RecommendationRequest();
        req.startDestinationCode = "DEST-01";
        req.preferredCategory = "Historical";
        req.totalBudget = 1500.0;
        req.desiredStops = 3;

        GreedyRecommendationService.RecommendationResult res = service.generateRecommendations(destinations, graph, req);
        assertTrue(res.success);
        assertEquals(2, res.stops.size(), "Should pick starting anchor + 1 historical destination fitting budget");

        assertEquals("DEST-01", res.stops.get(0).destination.getDestinationCode());
        assertEquals("DEST-04", res.stops.get(1).destination.getDestinationCode(), "Rome should be chosen as best historical candidate");
        assertTrue(res.totalEstimatedCost <= 1500.0, "Total cost must not exceed budget constraint");
    }

    @Test
    @DisplayName("Verify Greedy excludes already selected destinations")
    void testNoDuplicateSelections() {
        GreedyRecommendationService.RecommendationRequest req = new GreedyRecommendationService.RecommendationRequest();
        req.startDestinationCode = "DEST-01";
        req.desiredStops = 5;

        GreedyRecommendationService.RecommendationResult res = service.generateRecommendations(destinations, graph, req);
        assertTrue(res.success);

        long uniqueCount = res.stops.stream()
                .map(s -> s.destination.getDestinationCode())
                .distinct()
                .count();
        assertEquals(res.stops.size(), uniqueCount, "All recommended stops must be distinct");
    }
}
