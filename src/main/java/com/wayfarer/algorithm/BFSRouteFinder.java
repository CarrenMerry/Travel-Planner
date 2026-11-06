package com.wayfarer.algorithm;

import com.wayfarer.ds.TravelGraph;
import java.util.*;

/**
 * Wayfarer — Breadth-First Search (BFS) Route Finder
 *
 * Academic Algorithm Requirement 3:
 * Traverses the TravelGraph using a queue to determine the route between two destinations
 * with the minimum number of transit connections (fewest edges/hops).
 *
 * Time Complexity: O(V + E)
 * Space Complexity: O(V) for visited set, queue, and predecessor map.
 *
 * Theoretical Note: BFS guarantees the route with the fewest connections in an unweighted graph.
 * It optimizes for minimum stops, not necessarily minimum physical road distance.
 */
public class BFSRouteFinder {

    public static class RouteStep {
        public String fromCode;
        public String toCode;
        public double distanceKm;
        public double estimatedCost;
        public Integer estimatedMinutes;

        public RouteStep(String fromCode, String toCode, double distanceKm, double cost, Integer minutes) {
            this.fromCode = fromCode;
            this.toCode = toCode;
            this.distanceKm = distanceKm;
            this.estimatedCost = cost;
            this.estimatedMinutes = minutes;
        }
    }

    public static class BFSResult {
        public boolean success;
        public String sourceCode;
        public String targetCode;
        public List<String> path = new ArrayList<>();
        public List<RouteStep> steps = new ArrayList<>();
        public int connectionCount = 0;
        public double totalDistanceKm = 0.0;
        public double totalEstimatedCost = 0.0;
        public String algorithmNote = "BFS finds the path with the fewest transit connections (fewest intermediate stops).";
        public String message;

        public static BFSResult failure(String source, String target, String message) {
            BFSResult res = new BFSResult();
            res.success = false;
            res.sourceCode = source;
            res.targetCode = target;
            res.message = message;
            return res;
        }
    }

    /**
     * Executes Breadth-First Search from sourceCode to targetCode.
     */
    public BFSResult findRoute(TravelGraph graph, String sourceCode, String targetCode) {
        if (graph == null) {
            return BFSResult.failure(sourceCode, targetCode, "Travel graph data is currently unavailable.");
        }

        if (sourceCode == null || targetCode == null || sourceCode.trim().isEmpty() || targetCode.trim().isEmpty()) {
            return BFSResult.failure(sourceCode, targetCode, "Please specify both starting destination and target destination.");
        }

        String start = sourceCode.trim().toUpperCase();
        String target = targetCode.trim().toUpperCase();

        if (!graph.hasVertex(start)) {
            return BFSResult.failure(start, target, "Starting destination code '" + start + "' not found in graph catalog.");
        }
        if (!graph.hasVertex(target)) {
            return BFSResult.failure(start, target, "Target destination code '" + target + "' not found in graph catalog.");
        }

        // Trivial case: identical source and target
        if (start.equals(target)) {
            BFSResult res = new BFSResult();
            res.success = true;
            res.sourceCode = start;
            res.targetCode = target;
            res.path.add(start);
            res.message = "Origin and destination are identical. Zero connections required.";
            return res;
        }

        // Queue for BFS traversal
        Queue<String> queue = new ArrayDeque<>();
        Set<String> visited = new HashSet<>();
        Map<String, String> parentMap = new HashMap<>();
        Map<String, TravelGraph.Edge> edgeMap = new HashMap<>();

        queue.add(start);
        visited.add(start);
        boolean found = false;

        while (!queue.isEmpty()) {
            String current = queue.poll();

            if (current.equals(target)) {
                found = true;
                break;
            }

            for (TravelGraph.Edge edge : graph.getNeighbors(current)) {
                String neighbor = edge.getTargetCode();
                if (!visited.contains(neighbor)) {
                    visited.add(neighbor);
                    parentMap.put(neighbor, current);
                    edgeMap.put(neighbor, edge);
                    queue.add(neighbor);

                    if (neighbor.equals(target)) {
                        found = true;
                        break;
                    }
                }
            }
            if (found) break;
        }

        if (!found) {
            return BFSResult.failure(start, target,
                    "No transit route exists between " + start + " and " + target + " in the current connection network.");
        }

        // Reconstruct path from target back to start using parentMap
        LinkedList<String> reconstructedPath = new LinkedList<>();
        String curr = target;
        while (curr != null) {
            reconstructedPath.addFirst(curr);
            curr = parentMap.get(curr);
        }

        // Build route steps
        BFSResult result = new BFSResult();
        result.success = true;
        result.sourceCode = start;
        result.targetCode = target;
        result.path = new ArrayList<>(reconstructedPath);

        double totalDist = 0.0;
        double totalCost = 0.0;

        for (int i = 0; i < result.path.size() - 1; i++) {
            String from = result.path.get(i);
            String to = result.path.get(i + 1);
            TravelGraph.Edge edge = edgeMap.get(to);

            double dist = edge != null ? edge.getDistanceKm() : 0.0;
            double cost = edge != null ? edge.getEstimatedCost() : 0.0;
            Integer mins = edge != null ? edge.getEstimatedMinutes() : null;

            result.steps.add(new RouteStep(from, to, dist, cost, mins));
            totalDist += dist;
            totalCost += cost;
        }

        result.connectionCount = result.steps.size();
        result.totalDistanceKm = Math.round(totalDist * 10.0) / 10.0;
        result.totalEstimatedCost = Math.round(totalCost * 100.0) / 100.0;
        result.message = String.format("Found optimal route with %d connection(s) spanning %.1f km.",
                result.connectionCount, result.totalDistanceKm);

        return result;
    }
}
