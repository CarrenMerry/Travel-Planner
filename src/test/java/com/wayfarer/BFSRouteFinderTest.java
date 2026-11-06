package com.wayfarer;

import com.wayfarer.algorithm.BFSRouteFinder;
import com.wayfarer.ds.TravelGraph;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class BFSRouteFinderTest {

    private TravelGraph graph;
    private final BFSRouteFinder finder = new BFSRouteFinder();

    @BeforeEach
    void setUp() {
        graph = new TravelGraph();
        // Topology:
        // A <-> B (100km)
        // B <-> C (150km)
        // C <-> D (200km)
        // A <-> E (300km)
        // E <-> D (400km)
        // F is isolated
        graph.addEdge("A", "B", 100.0, 50.0, 60, true);
        graph.addEdge("B", "C", 150.0, 75.0, 90, true);
        graph.addEdge("C", "D", 200.0, 100.0, 120, true);
        graph.addEdge("A", "E", 300.0, 120.0, 180, true);
        graph.addEdge("E", "D", 400.0, 150.0, 240, true);
        graph.addVertex("F");
    }

    @Test
    @DisplayName("Verify BFS finds route with fewest hops (A -> E -> D vs A -> B -> C -> D)")
    void testBfsFewestHops() {
        BFSRouteFinder.BFSResult res = finder.findRoute(graph, "A", "D");
        assertTrue(res.success);
        // A -> E -> D has 2 connections; A -> B -> C -> D has 3 connections. BFS must choose 2 connections!
        assertEquals(List.of("A", "E", "D"), res.path);
        assertEquals(2, res.connectionCount);
        assertEquals(700.0, res.totalDistanceKm);
        assertEquals(270.0, res.totalEstimatedCost);
    }

    @Test
    @DisplayName("Verify identical source and target returns 0 connections")
    void testIdenticalSourceAndTarget() {
        BFSRouteFinder.BFSResult res = finder.findRoute(graph, "B", "B");
        assertTrue(res.success);
        assertEquals(List.of("B"), res.path);
        assertEquals(0, res.connectionCount);
    }

    @Test
    @DisplayName("Verify disconnected destination returns failure")
    void testDisconnectedDestination() {
        BFSRouteFinder.BFSResult res = finder.findRoute(graph, "A", "F");
        assertFalse(res.success);
        assertTrue(res.message.contains("No transit route exists"));
    }

    @Test
    @DisplayName("Verify invalid destination codes return clear failure")
    void testInvalidDestinationCodes() {
        BFSRouteFinder.BFSResult res = finder.findRoute(graph, "NON_EXISTENT", "D");
        assertFalse(res.success);
        assertTrue(res.message.contains("not found in graph catalog"));
    }
}
