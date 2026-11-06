package com.travelplanner.service;

import com.travelplanner.dao.ConnectionDao;
import com.travelplanner.dao.DestinationDao;
import com.travelplanner.ds.BinarySearchTree;
import com.travelplanner.ds.CustomGraph;
import com.travelplanner.model.Connection;
import com.travelplanner.model.Destination;
import com.travelplanner.persistence.FileStorage;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Service managing destination data and transit connections.
 * Synchronizes MySQL persistent storage with in-memory custom data structures
 * (BinarySearchTree and CustomGraph) required for algorithm execution.
 */
public class DestinationService {

    private static DestinationService instance;

    private final DestinationDao destinationDao = new DestinationDao();
    private final ConnectionDao connectionDao = new ConnectionDao();

    private final List<Destination> destinations = new CopyOnWriteArrayList<>();
    private final BinarySearchTree<Destination> bst = new BinarySearchTree<>();
    private final CustomGraph graph = new CustomGraph();

    private DestinationService() {
        reloadFromStorage();
    }

    public static synchronized DestinationService getInstance() {
        if (instance == null) {
            instance = new DestinationService();
        }
        return instance;
    }

    public synchronized void reloadFromStorage() {
        destinations.clear();
        bst.clear();
        graph.clear();

        // 1. Load destinations from MySQL
        List<Destination> loaded = new ArrayList<>();
        try {
            loaded = destinationDao.findAll();
        } catch (Exception e) {
            System.err.println("[DestinationService] Warning: Could not query destinations from MySQL: " + e.getMessage());
        }

        // Fallback to FileStorage only if database returned nothing or error
        if (loaded.isEmpty()) {
            System.out.println("[DestinationService] Loading from fallback local storage...");
            loaded = FileStorage.loadDestinations();
        }

        destinations.addAll(loaded);

        // 2. Populate in-memory BST and Graph vertices
        for (Destination d : destinations) {
            bst.insert(d);
            graph.addVertex(d);
        }

        // 3. Load travel connections from MySQL
        List<ConnectionDao.ConnectionRecord> connRecords = new ArrayList<>();
        try {
            connRecords = connectionDao.findAll();
            // If MySQL connections table is empty, seed connections between existing destinations
            if (connRecords.isEmpty() && destinations.size() >= 2) {
                seedInitialConnectionsIfEmpty();
                connRecords = connectionDao.findAll();
            }
        } catch (Exception e) {
            System.err.println("[DestinationService] Warning: Could not query travel connections: " + e.getMessage());
        }

        if (!connRecords.isEmpty()) {
            for (ConnectionDao.ConnectionRecord rec : connRecords) {
                Destination source = getById(rec.sourceCode);
                Destination target = getById(rec.targetCode);
                if (source != null && target != null) {
                    source.addConnection(new Connection(rec.targetCode, rec.distanceKm, rec.estimatedTravelCost));
                    graph.addEdge(rec.sourceCode, rec.targetCode, rec.distanceKm, rec.estimatedTravelCost, false);
                }
            }
        } else {
            // Fallback connections from objects if DB connections were empty
            for (Destination d : destinations) {
                for (Connection c : d.getConnections()) {
                    graph.addEdge(d.getId(), c.getTargetId(), c.getDistanceKm(), c.getTravelCost(), false);
                }
            }
        }

        System.out.println("[DestinationService] Loaded " + destinations.size() + " destinations and " +
                graph.getAllVertexIds().size() + " graph nodes from MySQL.");
    }

    private void seedInitialConnectionsIfEmpty() {
        System.out.println("[DestinationService] Seeding default travel connections in MySQL...");
        // Use existing codes: DEST-01 (Tokyo), DEST-02 (Paris), DEST-03 (Rome), DEST-04 (Bali), DEST-05 (NY), DEST-07 (Cairo)
        connectionDao.addConnection("DEST-01", "DEST-04", 5500.0, 410.0); // Tokyo <-> Bali
        connectionDao.addConnection("DEST-02", "DEST-03", 1100.0, 120.0); // Paris <-> Rome
        connectionDao.addConnection("DEST-02", "DEST-05", 5800.0, 520.0); // Paris <-> New York
        connectionDao.addConnection("DEST-03", "DEST-07", 2100.0, 240.0); // Rome <-> Cairo
        connectionDao.addConnection("DEST-01", "DEST-05", 10800.0, 950.0); // Tokyo <-> New York
        connectionDao.addConnection("DEST-04", "DEST-07", 9800.0, 750.0); // Bali <-> Cairo
    }

    public List<Destination> getAllDestinations() {
        return new ArrayList<>(destinations);
    }

    public Destination getById(String id) {
        if (id == null) return null;
        for (Destination d : destinations) {
            if (d.getId().equalsIgnoreCase(id) ||
                (d.getDestinationCode() != null && d.getDestinationCode().equalsIgnoreCase(id)) ||
                String.valueOf(d.getDbId()).equals(id)) {
                return d;
            }
        }
        return null;
    }

    public synchronized boolean addDestination(Destination destination) {
        if (destination == null || !destination.isValid()) {
            return false;
        }
        if (getById(destination.getId()) != null) {
            return false; // Code or ID duplicate
        }

        // 1. Persist to MySQL
        boolean inserted = destinationDao.insert(destination);
        if (!inserted) {
            System.err.println("[DestinationService] Failed to insert destination into MySQL.");
            return false;
        }

        // 2. Synchronize in-memory structures
        destinations.add(destination);
        bst.insert(destination);
        graph.addVertex(destination);

        // Add any initial connections
        for (Connection c : destination.getConnections()) {
            connectionDao.addConnection(destination.getId(), c.getTargetId(), c.getDistanceKm(), c.getTravelCost());
            graph.addEdge(destination.getId(), c.getTargetId(), c.getDistanceKm(), c.getTravelCost(), false);
        }

        return true;
    }

    public synchronized boolean updateDestination(Destination destination) {
        if (destination == null || !destination.isValid()) {
            return false;
        }
        Destination existing = getById(destination.getId());
        if (existing == null) {
            return false;
        }

        // Keep DB id aligned
        destination.setDbId(existing.getDbId());

        // 1. Persist to MySQL
        boolean updated = destinationDao.update(destination);
        if (!updated) {
            System.err.println("[DestinationService] Failed to update destination in MySQL.");
            return false;
        }

        // 2. Synchronize in-memory structures
        int index = destinations.indexOf(existing);
        if (index >= 0) {
            destinations.set(index, destination);
        }

        bst.delete(existing);
        bst.insert(destination);
        graph.addVertex(destination);

        return true;
    }

    public synchronized boolean deleteDestination(String id) {
        Destination existing = getById(id);
        if (existing == null) {
            return false;
        }

        // 1. Persist deletion in MySQL (cascades to connections and stops)
        boolean deleted = destinationDao.delete(existing.getDbId());
        if (!deleted) {
            deleted = destinationDao.deleteByCode(existing.getId());
        }

        if (!deleted) {
            System.err.println("[DestinationService] Failed to delete destination from MySQL.");
            return false;
        }

        // 2. Synchronize in-memory structures
        destinations.remove(existing);
        bst.delete(existing);

        for (Destination d : destinations) {
            d.removeConnection(id);
            graph.removeEdge(d.getId(), id, true);
        }

        return true;
    }

    public synchronized boolean addConnection(String fromId, String toId, double distanceKm, double travelCost) {
        Destination from = getById(fromId);
        Destination to = getById(toId);
        if (from == null || to == null) {
            return false;
        }

        // 1. Persist in MySQL
        boolean success = connectionDao.addConnection(from.getId(), to.getId(), distanceKm, travelCost);
        if (!success) {
            return false;
        }

        // 2. Synchronize in-memory structures
        from.addConnection(new Connection(to.getId(), distanceKm, travelCost));
        to.addConnection(new Connection(from.getId(), distanceKm, travelCost));
        graph.addEdge(from.getId(), to.getId(), distanceKm, travelCost, true);

        return true;
    }

    public synchronized boolean removeConnection(String fromId, String toId) {
        Destination from = getById(fromId);
        Destination to = getById(toId);
        if (from == null || to == null) {
            return false;
        }

        // 1. Persist in MySQL
        boolean success = connectionDao.removeConnection(from.getId(), to.getId());

        // 2. Synchronize in-memory structures
        from.removeConnection(to.getId());
        to.removeConnection(from.getId());
        graph.removeEdge(from.getId(), to.getId(), true);

        return success;
    }

    public BinarySearchTree<Destination> getBst() {
        return bst;
    }

    public CustomGraph getGraph() {
        return graph;
    }

    public DestinationDao getDestinationDao() {
        return destinationDao;
    }

    public ConnectionDao getConnectionDao() {
        return connectionDao;
    }
}
