package com.wayfarer.service;

import com.wayfarer.algorithm.BFSRouteFinder;
import com.wayfarer.dao.ConnectionDAO;
import com.wayfarer.dao.DestinationDAO;
import com.wayfarer.ds.TravelGraph;
import com.wayfarer.model.Destination;
import com.wayfarer.model.TravelConnection;

import java.util.List;

/**
 * Wayfarer — Route Finding Service
 * Synchronizes the persistent travel connections with the in-memory TravelGraph,
 * and executes BFS route searches between destinations.
 */
public class RouteService {

    private static final RouteService INSTANCE = new RouteService();
    private final DestinationDAO destinationDAO = new DestinationDAO();
    private final ConnectionDAO connectionDAO = new ConnectionDAO();
    private final BFSRouteFinder bfsRouteFinder = new BFSRouteFinder();

    private final TravelGraph graph = new TravelGraph();
    private boolean initialized = false;

    public static RouteService getInstance() {
        return INSTANCE;
    }

    private RouteService() {
        refreshGraph();
    }

    public synchronized void refreshGraph() {
        graph.clear();

        List<Destination> destinations = destinationDAO.findAll();
        for (Destination d : destinations) {
            graph.addVertex(d.getDestinationCode());
        }

        List<TravelConnection> connections = connectionDAO.findAll();
        for (TravelConnection c : connections) {
            // Travel connections in a flight/rail transit network are bidirectional
            graph.addEdge(c.getSourceCode(), c.getTargetCode(),
                    c.getDistanceKm(), c.getEstimatedTravelCost(), c.getEstimatedTravelMinutes(), true);
        }

        initialized = true;
        System.out.printf("[RouteService] Graph synchronized: %d vertices, %d directed edges.%n",
                graph.getVertexCount(), graph.getEdgeCount());
    }

    public TravelGraph getGraph() {
        if (!initialized) refreshGraph();
        return graph;
    }

    public BFSRouteFinder.BFSResult findBfsRoute(String sourceCode, String targetCode) {
        if (!initialized) refreshGraph();
        return bfsRouteFinder.findRoute(graph, sourceCode, targetCode);
    }

    // Connection CRUD methods for Admin
    public List<TravelConnection> getAllConnections() {
        return connectionDAO.findAll();
    }

    public boolean createConnection(int sourceId, int targetId, double distanceKm, double cost, Integer minutes) {
        boolean ok = connectionDAO.insert(sourceId, targetId, distanceKm, cost, minutes);
        if (ok) refreshGraph();
        return ok;
    }

    public boolean updateConnection(int id, double distanceKm, double cost, Integer minutes) {
        boolean ok = connectionDAO.update(id, distanceKm, cost, minutes);
        if (ok) refreshGraph();
        return ok;
    }

    public boolean deleteConnection(int id) {
        boolean ok = connectionDAO.delete(id);
        if (ok) refreshGraph();
        return ok;
    }

    public int countConnections() {
        return connectionDAO.count();
    }
}
