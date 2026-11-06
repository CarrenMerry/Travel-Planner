package com.wayfarer.ds;

import java.util.*;

/**
 * Wayfarer — Travel Graph Data Structure
 *
 * Academic Data Structure Requirement 2:
 * Adjacency-list graph representing destinations as vertices and travel connections as edges.
 *
 * Space Complexity: O(V + E)
 * Vertices: Destination codes (e.g. DEST-01)
 * Edges: Travel routes with distance, cost, and duration attributes.
 */
public class TravelGraph {

    public static class Edge {
        private final String targetCode;
        private final double distanceKm;
        private final double estimatedCost;
        private final Integer estimatedMinutes;

        public Edge(String targetCode, double distanceKm, double estimatedCost, Integer estimatedMinutes) {
            this.targetCode = targetCode;
            this.distanceKm = distanceKm;
            this.estimatedCost = estimatedCost;
            this.estimatedMinutes = estimatedMinutes;
        }

        public String getTargetCode() { return targetCode; }
        public double getDistanceKm() { return distanceKm; }
        public double getEstimatedCost() { return estimatedCost; }
        public Integer getEstimatedMinutes() { return estimatedMinutes; }
    }

    private final Map<String, List<Edge>> adjacencyList = new LinkedHashMap<>();

    public synchronized void addVertex(String destinationCode) {
        if (destinationCode == null || destinationCode.trim().isEmpty()) return;
        String code = destinationCode.trim().toUpperCase();
        adjacencyList.putIfAbsent(code, new ArrayList<>());
    }

    public synchronized void addEdge(String fromCode, String toCode, double distanceKm, double cost, Integer minutes, boolean bidirectional) {
        if (fromCode == null || toCode == null) return;
        String from = fromCode.trim().toUpperCase();
        String to = toCode.trim().toUpperCase();

        addVertex(from);
        addVertex(to);

        // Avoid duplicate edges
        List<Edge> edges = adjacencyList.get(from);
        edges.removeIf(e -> e.getTargetCode().equalsIgnoreCase(to));
        edges.add(new Edge(to, distanceKm, cost, minutes));

        if (bidirectional) {
            List<Edge> reverseEdges = adjacencyList.get(to);
            reverseEdges.removeIf(e -> e.getTargetCode().equalsIgnoreCase(from));
            reverseEdges.add(new Edge(from, distanceKm, cost, minutes));
        }
    }

    public synchronized List<Edge> getNeighbors(String destinationCode) {
        if (destinationCode == null) return Collections.emptyList();
        String code = destinationCode.trim().toUpperCase();
        return adjacencyList.getOrDefault(code, Collections.emptyList());
    }

    public synchronized Set<String> getAllVertexCodes() {
        return Collections.unmodifiableSet(adjacencyList.keySet());
    }

    public synchronized boolean hasVertex(String destinationCode) {
        if (destinationCode == null) return false;
        return adjacencyList.containsKey(destinationCode.trim().toUpperCase());
    }

    public synchronized Edge getEdge(String fromCode, String toCode) {
        if (fromCode == null || toCode == null) return null;
        List<Edge> edges = getNeighbors(fromCode);
        for (Edge e : edges) {
            if (e.getTargetCode().equalsIgnoreCase(toCode.trim())) {
                return e;
            }
        }
        return null;
    }

    public synchronized int getVertexCount() {
        return adjacencyList.size();
    }

    public synchronized int getEdgeCount() {
        int count = 0;
        for (List<Edge> edges : adjacencyList.values()) {
            count += edges.size();
        }
        return count;
    }

    public synchronized void clear() {
        adjacencyList.clear();
    }
}
