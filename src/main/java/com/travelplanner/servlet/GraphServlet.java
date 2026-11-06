package com.travelplanner.servlet;

import com.travelplanner.ds.CustomGraph;
import com.travelplanner.model.Destination;
import com.travelplanner.service.AuthService;
import com.travelplanner.service.DestinationService;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.*;

@WebServlet("/api/graph/*")
public class GraphServlet extends BaseServlet {

    private final DestinationService destinationService = DestinationService.getInstance();

    public static class TraversalRequest {
        public String startId;
        public String targetId; // for Dijkstra
    }

    public static class EdgeRequest {
        public String fromId;
        public String toId;
        public double distanceKm;
        public double travelCost;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        CustomGraph graph = destinationService.getGraph();
        List<String> vertexIds = graph.getAllVertexIds();

        List<Map<String, Object>> vertices = new ArrayList<>();
        List<Map<String, Object>> edges = new ArrayList<>();
        Set<String> seenEdges = new HashSet<>();

        for (String vId : vertexIds) {
            Destination dest = destinationService.getById(vId);
            Map<String, Object> vMap = new HashMap<>();
            vMap.put("id", vId);
            vMap.put("name", dest != null ? dest.getName() : vId);
            vMap.put("location", dest != null ? dest.getLocation() : "");
            vMap.put("cost", dest != null ? dest.getEstimatedCost() : 0);
            vMap.put("rating", dest != null ? dest.getRating() : 0);
            vertices.add(vMap);

            List<CustomGraph.EdgeNode> edgeList = graph.getEdgesForVertex(vId);
            for (CustomGraph.EdgeNode e : edgeList) {
                // To avoid duplicate visual lines in undirected graph
                String key = vId.compareTo(e.targetId) < 0 ? vId + "-" + e.targetId : e.targetId + "-" + vId;
                if (!seenEdges.contains(key)) {
                    seenEdges.add(key);
                    Map<String, Object> eMap = new HashMap<>();
                    eMap.put("source", vId);
                    eMap.put("target", e.targetId);
                    eMap.put("distanceKm", e.distanceKm);
                    eMap.put("travelCost", e.travelCost);
                    edges.add(eMap);
                }
            }
        }

        Map<String, Object> responseData = new HashMap<>();
        responseData.put("vertexCount", vertices.size());
        responseData.put("edgeCount", edges.size());
        responseData.put("vertices", vertices);
        responseData.put("edges", edges);

        sendJsonResponse(resp, HttpServletResponse.SC_OK, responseData);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String pathInfo = req.getPathInfo();
        CustomGraph graph = destinationService.getGraph();

        if (pathInfo != null) {
            if (pathInfo.equalsIgnoreCase("/bfs")) {
                TraversalRequest body = parseJsonBody(req, TraversalRequest.class);
                String start = (body != null && body.startId != null) ? body.startId : "DEST-01";
                CustomGraph.TraversalResult result = graph.bfs(start);
                sendJsonResponse(resp, HttpServletResponse.SC_OK, result);
                return;
            } else if (pathInfo.equalsIgnoreCase("/dfs")) {
                TraversalRequest body = parseJsonBody(req, TraversalRequest.class);
                String start = (body != null && body.startId != null) ? body.startId : "DEST-01";
                CustomGraph.TraversalResult result = graph.dfs(start);
                sendJsonResponse(resp, HttpServletResponse.SC_OK, result);
                return;
            } else if (pathInfo.equalsIgnoreCase("/dijkstra")) {
                TraversalRequest body = parseJsonBody(req, TraversalRequest.class);
                if (body == null || body.startId == null || body.targetId == null) {
                    sendErrorResponse(resp, HttpServletResponse.SC_BAD_REQUEST, "Must provide 'startId' and 'targetId'.");
                    return;
                }
                CustomGraph.DijkstraResult result = graph.dijkstra(body.startId, body.targetId);
                sendJsonResponse(resp, HttpServletResponse.SC_OK, result);
                return;
            } else if (pathInfo.equalsIgnoreCase("/edge")) {
                if (!AuthService.getInstance().isAdmin(req)) {
                    sendErrorResponse(resp, HttpServletResponse.SC_FORBIDDEN, "Access denied: Administrator privileges required to add connections.");
                    return;
                }

                EdgeRequest body = parseJsonBody(req, EdgeRequest.class);
                if (body == null || body.fromId == null || body.toId == null) {
                    sendErrorResponse(resp, HttpServletResponse.SC_BAD_REQUEST, "Invalid edge parameters.");
                    return;
                }
                destinationService.addConnection(body.fromId, body.toId, body.distanceKm, body.travelCost);
                Map<String, Object> res = new HashMap<>();
                res.put("success", true);
                res.put("message", "Edge added successfully between " + body.fromId + " and " + body.toId);
                sendJsonResponse(resp, HttpServletResponse.SC_OK, res);
                return;
            }
        }

        sendErrorResponse(resp, HttpServletResponse.SC_BAD_REQUEST, "Invalid graph action endpoint.");
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        if (!AuthService.getInstance().isAdmin(req)) {
            sendErrorResponse(resp, HttpServletResponse.SC_FORBIDDEN, "Access denied: Administrator privileges required to remove connections.");
            return;
        }

        String fromId = req.getParameter("fromId");
        String toId = req.getParameter("toId");

        if (fromId == null || toId == null) {
            sendErrorResponse(resp, HttpServletResponse.SC_BAD_REQUEST, "Must provide 'fromId' and 'toId' parameters.");
            return;
        }

        destinationService.removeConnection(fromId, toId);
        Map<String, Object> res = new HashMap<>();
        res.put("success", true);
        res.put("message", "Edge removed between " + fromId + " and " + toId);
        sendJsonResponse(resp, HttpServletResponse.SC_OK, res);
    }
}
