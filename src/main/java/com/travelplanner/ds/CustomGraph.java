package com.travelplanner.ds;

import com.travelplanner.model.Destination;

import java.util.ArrayList;
import java.util.List;

/**
 * Custom Graph implemented with explicit linked Adjacency Lists.
 * Uses custom linked nodes (VertexNode and EdgeNode) rather than Java Collections.
 */
public class CustomGraph {

    /**
     * Node in the adjacency linked list representing an outgoing edge.
     */
    public static class EdgeNode {
        public String targetId;
        public double distanceKm;
        public double travelCost;
        public EdgeNode next;

        public EdgeNode(String targetId, double distanceKm, double travelCost, EdgeNode next) {
            this.targetId = targetId;
            this.distanceKm = distanceKm;
            this.travelCost = travelCost;
            this.next = next;
        }
    }

    /**
     * Node in the vertices linked list.
     */
    public static class VertexNode {
        public String id;
        public Destination destination;
        public EdgeNode edgesHead; // head of linked list of outgoing edges
        public VertexNode next;

        public VertexNode(String id, Destination destination, VertexNode next) {
            this.id = id;
            this.destination = destination;
            this.edgesHead = null;
            this.next = next;
        }
    }

    public static class TraversalStep {
        public String currentVertexId;
        public String action;
        public List<String> frontierState; // Queue state for BFS, Stack state for DFS
        public List<String> visitedSoFar;

        public TraversalStep(String currentVertexId, String action, List<String> frontierState, List<String> visitedSoFar) {
            this.currentVertexId = currentVertexId;
            this.action = action;
            this.frontierState = frontierState;
            this.visitedSoFar = visitedSoFar;
        }
    }

    public static class TraversalResult {
        public String algorithm;
        public String startVertexId;
        public List<String> traversalOrder = new ArrayList<>();
        public List<String> reachableDestinations = new ArrayList<>();
        public List<TraversalStep> steps = new ArrayList<>();
        public String explanation;

        public TraversalResult(String algorithm, String startVertexId) {
            this.algorithm = algorithm;
            this.startVertexId = startVertexId;
        }
    }

    public static class DijkstraResult {
        public String startId;
        public String targetId;
        public boolean pathFound;
        public double totalDistance;
        public List<String> path = new ArrayList<>();
        public List<String> visitedNodes = new ArrayList<>();
        public String explanation;
    }

    private VertexNode verticesHead;
    private int vertexCount;

    public CustomGraph() {
        this.verticesHead = null;
        this.vertexCount = 0;
    }

    /**
     * Adds a vertex to the graph if not already present.
     * Time Complexity: O(V)
     */
    public void addVertex(Destination destination) {
        if (destination == null || destination.getId() == null) return;
        String id = destination.getId();
        if (findVertex(id) != null) {
            // Update destination info if vertex already exists
            findVertex(id).destination = destination;
            return;
        }
        verticesHead = new VertexNode(id, destination, verticesHead);
        vertexCount++;
    }

    /**
     * Finds a vertex by ID.
     */
    public VertexNode findVertex(String id) {
        if (id == null) return null;
        VertexNode cur = verticesHead;
        while (cur != null) {
            if (cur.id.equalsIgnoreCase(id)) {
                return cur;
            }
            cur = cur.next;
        }
        return null;
    }

    /**
     * Adds a directed or undirected weighted edge between two vertices.
     * Time Complexity: O(V + E)
     */
    public void addEdge(String fromId, String toId, double distanceKm, double travelCost, boolean bidirectional) {
        VertexNode u = findVertex(fromId);
        VertexNode v = findVertex(toId);
        if (u == null || v == null) return;

        // Remove existing edge if any to prevent duplicates
        removeEdgeDirect(u, toId);
        u.edgesHead = new EdgeNode(toId, distanceKm, travelCost, u.edgesHead);

        if (bidirectional) {
            removeEdgeDirect(v, fromId);
            v.edgesHead = new EdgeNode(fromId, distanceKm, travelCost, v.edgesHead);
        }
    }

    private void removeEdgeDirect(VertexNode u, String targetId) {
        if (u == null || u.edgesHead == null) return;
        if (u.edgesHead.targetId.equalsIgnoreCase(targetId)) {
            u.edgesHead = u.edgesHead.next;
            return;
        }
        EdgeNode cur = u.edgesHead;
        while (cur.next != null) {
            if (cur.next.targetId.equalsIgnoreCase(targetId)) {
                cur.next = cur.next.next;
                return;
            }
            cur = cur.next;
        }
    }

    /**
     * Removes an edge.
     */
    public void removeEdge(String fromId, String toId, boolean bidirectional) {
        VertexNode u = findVertex(fromId);
        if (u != null) {
            removeEdgeDirect(u, toId);
        }
        if (bidirectional) {
            VertexNode v = findVertex(toId);
            if (v != null) {
                removeEdgeDirect(v, fromId);
            }
        }
    }

    /**
     * Breadth-First Search (BFS) using CustomQueue.
     * Traverses the graph layer-by-layer (unweighted exploration).
     * Time Complexity: O(V + E)
     * Space Complexity: O(V)
     */
    public TraversalResult bfs(String startId) {
        TraversalResult result = new TraversalResult("BFS", startId);
        result.explanation = "BFS explores vertices level by level using a FIFO Queue. "
                + "Note: BFS finds the shortest path by edge count (unweighted), NOT minimum total distance on weighted edges.";

        VertexNode start = findVertex(startId);
        if (start == null) {
            return result;
        }

        CustomQueue<String> queue = new CustomQueue<>();
        List<String> visited = new ArrayList<>();

        queue.enqueue(start.id);
        visited.add(start.id);
        result.steps.add(new TraversalStep(start.id, "Enqueued start node", queue.toList(), new ArrayList<>(visited)));

        while (!queue.isEmpty()) {
            String currentId = queue.dequeue();
            result.traversalOrder.add(currentId);
            result.reachableDestinations.add(currentId);

            result.steps.add(new TraversalStep(currentId, "Dequeued node " + currentId, queue.toList(), new ArrayList<>(visited)));

            VertexNode vNode = findVertex(currentId);
            if (vNode != null) {
                EdgeNode edge = vNode.edgesHead;
                while (edge != null) {
                    if (!visited.contains(edge.targetId)) {
                        visited.add(edge.targetId);
                        queue.enqueue(edge.targetId);
                        result.steps.add(new TraversalStep(edge.targetId,
                                "Discovered unvisited neighbor " + edge.targetId + " (dist: " + edge.distanceKm + " km), enqueued",
                                queue.toList(), new ArrayList<>(visited)));
                    }
                    edge = edge.next;
                }
            }
        }

        return result;
    }

    /**
     * Depth-First Search (DFS) using CustomStack.
     * Traverses the graph by exploring as deep as possible along each branch before backtracking.
     * Time Complexity: O(V + E)
     * Space Complexity: O(V)
     */
    public TraversalResult dfs(String startId) {
        TraversalResult result = new TraversalResult("DFS", startId);
        result.explanation = "DFS explores deeply along each branch using a LIFO Stack until a dead end is reached, then backtracks.";

        VertexNode start = findVertex(startId);
        if (start == null) {
            return result;
        }

        CustomStack<String> stack = new CustomStack<>();
        List<String> visited = new ArrayList<>();

        stack.push(start.id);
        result.steps.add(new TraversalStep(start.id, "Pushed start node onto stack", stack.toList(), new ArrayList<>(visited)));

        while (!stack.isEmpty()) {
            String currentId = stack.pop();

            if (!visited.contains(currentId)) {
                visited.add(currentId);
                result.traversalOrder.add(currentId);
                result.reachableDestinations.add(currentId);
                result.steps.add(new TraversalStep(currentId, "Popped and visited node " + currentId, stack.toList(), new ArrayList<>(visited)));

                VertexNode vNode = findVertex(currentId);
                if (vNode != null) {
                    EdgeNode edge = vNode.edgesHead;
                    while (edge != null) {
                        if (!visited.contains(edge.targetId)) {
                            stack.push(edge.targetId);
                            result.steps.add(new TraversalStep(edge.targetId,
                                    "Pushed neighbor " + edge.targetId + " onto stack for depth exploration",
                                    stack.toList(), new ArrayList<>(visited)));
                        }
                        edge = edge.next;
                    }
                }
            }
        }

        return result;
    }

    /**
     * Dijkstra's algorithm for finding the true shortest path on a weighted graph.
     * Time Complexity: O(V^2 + E)
     */
    public DijkstraResult dijkstra(String startId, String targetId) {
        DijkstraResult res = new DijkstraResult();
        res.startId = startId;
        res.targetId = targetId;
        res.explanation = "Dijkstra's Algorithm computes the genuine shortest distance route on weighted edges (unlike BFS which only counts hops).";

        VertexNode start = findVertex(startId);
        VertexNode target = findVertex(targetId);
        if (start == null || target == null) {
            res.pathFound = false;
            return res;
        }

        List<String> allIds = getAllVertexIds();
        int n = allIds.size();

        double[] dist = new double[n];
        int[] prev = new int[n];
        boolean[] settled = new boolean[n];

        for (int i = 0; i < n; i++) {
            dist[i] = Double.POSITIVE_INFINITY;
            prev[i] = -1;
            settled[i] = false;
        }

        int startIdx = allIds.indexOf(startId);
        int targetIdx = allIds.indexOf(targetId);
        dist[startIdx] = 0;

        for (int count = 0; count < n; count++) {
            int u = -1;
            double minDist = Double.POSITIVE_INFINITY;
            for (int i = 0; i < n; i++) {
                if (!settled[i] && dist[i] < minDist) {
                    minDist = dist[i];
                    u = i;
                }
            }

            if (u == -1 || minDist == Double.POSITIVE_INFINITY) break;

            settled[u] = true;
            String uId = allIds.get(u);
            res.visitedNodes.add(uId);

            if (u == targetIdx) break;

            VertexNode uNode = findVertex(uId);
            if (uNode != null) {
                EdgeNode edge = uNode.edgesHead;
                while (edge != null) {
                    int v = allIds.indexOf(edge.targetId);
                    if (v != -1 && !settled[v]) {
                        double alt = dist[u] + edge.distanceKm;
                        if (alt < dist[v]) {
                            dist[v] = alt;
                            prev[v] = u;
                        }
                    }
                    edge = edge.next;
                }
            }
        }

        if (dist[targetIdx] == Double.POSITIVE_INFINITY) {
            res.pathFound = false;
            return res;
        }

        res.pathFound = true;
        res.totalDistance = dist[targetIdx];

        CustomStack<String> pathStack = new CustomStack<>();
        int curr = targetIdx;
        while (curr != -1) {
            pathStack.push(allIds.get(curr));
            curr = prev[curr];
        }

        while (!pathStack.isEmpty()) {
            res.path.add(pathStack.pop());
        }

        return res;
    }

    public List<String> getAllVertexIds() {
        List<String> ids = new ArrayList<>();
        VertexNode cur = verticesHead;
        while (cur != null) {
            ids.add(cur.id);
            cur = cur.next;
        }
        return ids;
    }

    public List<Destination> getAllDestinations() {
        List<Destination> list = new ArrayList<>();
        VertexNode cur = verticesHead;
        while (cur != null) {
            list.add(cur.destination);
            cur = cur.next;
        }
        return list;
    }

    public List<EdgeNode> getEdgesForVertex(String id) {
        List<EdgeNode> edges = new ArrayList<>();
        VertexNode v = findVertex(id);
        if (v != null) {
            EdgeNode edge = v.edgesHead;
            while (edge != null) {
                edges.add(edge);
                edge = edge.next;
            }
        }
        return edges;
    }

    public int getVertexCount() {
        return vertexCount;
    }

    public void clear() {
        verticesHead = null;
        vertexCount = 0;
    }
}
