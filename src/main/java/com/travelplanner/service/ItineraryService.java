package com.travelplanner.service;

import com.travelplanner.dao.ItineraryDao;
import com.travelplanner.dao.ItineraryStopDao;
import com.travelplanner.dao.UserDao;
import com.travelplanner.ds.CustomLinkedList;
import com.travelplanner.ds.CustomStack;
import com.travelplanner.model.Destination;
import com.travelplanner.model.ItineraryAction;
import com.travelplanner.model.User;
import com.travelplanner.persistence.FileStorage;

import java.util.ArrayList;
import java.util.List;

/**
 * Service managing the user's travel itinerary using a custom Singly Linked List
 * and providing an undo history powered by a custom Linked-Node Stack.
 * Persists itinerary header and ordered stops in MySQL using ACID transactions.
 */
public class ItineraryService {

    private static ItineraryService instance;

    private final CustomLinkedList<Destination> itineraryList = new CustomLinkedList<>();
    private final CustomStack<ItineraryAction> undoStack = new CustomStack<>();
    private final DestinationService destinationService = DestinationService.getInstance();

    private final ItineraryDao itineraryDao = new ItineraryDao();
    private final ItineraryStopDao itineraryStopDao = new ItineraryStopDao();
    private final UserDao userDao = new UserDao();

    private int activeUserId = 1;
    private int activeItineraryId = 1;
    private double budget = 5000.0;

    public static class ItineraryStats {
        public int totalStops;
        public double totalCost;
        public double totalDuration;
        public double averageRating;
        public int undoCount;
        public double budget;

        public ItineraryStats(int totalStops, double totalCost, double totalDuration, double averageRating, int undoCount, double budget) {
            this.totalStops = totalStops;
            this.totalCost = totalCost;
            this.totalDuration = totalDuration;
            this.averageRating = averageRating;
            this.undoCount = undoCount;
            this.budget = budget;
        }
    }

    private ItineraryService() {
        loadPersistedItinerary();
    }

    public static synchronized ItineraryService getInstance() {
        if (instance == null) {
            instance = new ItineraryService();
        }
        return instance;
    }

    public synchronized void loadPersistedItinerary() {
        itineraryList.clear();
        undoStack.clear();

        try {
            // Resolve active user (prefer traveler, fallback to first user)
            User traveler = userDao.findByEmailOrUsername("traveler");
            if (traveler == null) {
                traveler = userDao.findByEmailOrUsername("admin");
            }
            if (traveler != null) {
                activeUserId = traveler.getId();
            }

            ItineraryDao.ItineraryRecord activeRecord = itineraryDao.getOrCreateActiveItinerary(
                    activeUserId, "Global Adventure Tour", 5000.0);

            if (activeRecord != null) {
                activeItineraryId = activeRecord.id;
                budget = activeRecord.budget;

                // Load ordered stops from MySQL
                List<Destination> stops = itineraryStopDao.loadOrderedStops(activeItineraryId);
                for (Destination d : stops) {
                    // Harmonize with destinationService in-memory instance
                    Destination inMem = destinationService.getById(d.getId());
                    itineraryList.insertAtEnd(inMem != null ? inMem : d);
                }
            }
        } catch (Exception e) {
            System.err.println("[ItineraryService] Warning: Could not load itinerary from MySQL: " + e.getMessage());
        }

        // Fallback: If empty, seed initial destinations from database catalogue
        if (itineraryList.isEmpty()) {
            List<Destination> available = destinationService.getAllDestinations();
            if (available.size() >= 3) {
                itineraryList.insertAtEnd(available.get(0));
                itineraryList.insertAtEnd(available.get(1));
                itineraryList.insertAtEnd(available.get(2));
            } else if (!available.isEmpty()) {
                for (Destination d : available) {
                    itineraryList.insertAtEnd(d);
                }
            }
            persistState();
        }

        System.out.println("[ItineraryService] Loaded itinerary with " + itineraryList.size() +
                " stops from MySQL (Itinerary ID: " + activeItineraryId + ").");
    }

    public synchronized void switchUserItinerary(User user) {
        if (user == null) return;
        this.activeUserId = user.getId();
        loadPersistedItinerary();
    }

    private synchronized void persistState() {
        List<Destination> currentStops = itineraryList.toList();

        // 1. Persist to MySQL in a single transaction
        try {
            itineraryStopDao.saveAllStops(activeItineraryId, currentStops, budget);
        } catch (Exception e) {
            System.err.println("[ItineraryService] Failed to persist itinerary stops to MySQL: " + e.getMessage());
        }

        // 2. Secondary fallback to FileStorage
        try {
            List<String> ids = new ArrayList<>();
            for (Destination d : currentStops) {
                ids.add(d.getId());
            }
            FileStorage.saveItineraryIds(ids);
        } catch (Exception ignored) {}
    }

    public synchronized boolean addDestination(String destinationId) {
        Destination d = destinationService.getById(destinationId);
        if (d == null) return false;

        itineraryList.insertAtEnd(d);
        undoStack.push(new ItineraryAction(ItineraryAction.ActionType.ADD, d, itineraryList.size() - 1,
                "Added '" + d.getName() + "' to itinerary."));
        persistState();
        return true;
    }

    public synchronized boolean insertDestination(int index, String destinationId) {
        Destination d = destinationService.getById(destinationId);
        if (d == null) return false;

        itineraryList.insertAtPosition(index, d);
        undoStack.push(new ItineraryAction(ItineraryAction.ActionType.INSERT, d, index,
                "Inserted '" + d.getName() + "' at position " + (index + 1) + "."));
        persistState();
        return true;
    }

    public synchronized boolean removeDestination(int index) {
        if (index < 0 || index >= itineraryList.size()) return false;

        Destination removed = itineraryList.deleteAtPosition(index);
        undoStack.push(new ItineraryAction(ItineraryAction.ActionType.REMOVE, removed, index,
                "Removed '" + removed.getName() + "' from position " + (index + 1) + "."));
        persistState();
        return true;
    }

    public synchronized boolean removeDestinationById(String destinationId) {
        int idx = itineraryList.indexOf(destinationId, Destination::getId);
        if (idx == -1) return false;
        return removeDestination(idx);
    }

    public synchronized boolean reorder(int fromIndex, int toIndex) {
        if (fromIndex < 0 || fromIndex >= itineraryList.size() ||
            toIndex < 0 || toIndex >= itineraryList.size() || fromIndex == toIndex) {
            return false;
        }

        List<Destination> before = itineraryList.toList();
        itineraryList.move(fromIndex, toIndex);
        undoStack.push(new ItineraryAction(ItineraryAction.ActionType.REORDER, before,
                "Reordered destination from position " + (fromIndex + 1) + " to " + (toIndex + 1) + "."));
        persistState();
        return true;
    }

    public synchronized void clearItinerary() {
        if (itineraryList.isEmpty()) return;

        List<Destination> snapshot = itineraryList.toList();
        itineraryList.clear();
        undoStack.push(new ItineraryAction(ItineraryAction.ActionType.CLEAR, snapshot,
                "Cleared all " + snapshot.size() + " destinations from itinerary."));
        persistState();
    }

    public synchronized void updateBudget(double newBudget) {
        if (newBudget >= 0) {
            this.budget = newBudget;
            itineraryDao.updateBudget(activeItineraryId, newBudget);
        }
    }

    /**
     * Reverts the most recent itinerary action using the custom Stack.
     * Time Complexity: O(n)
     */
    public synchronized String undo() {
        if (undoStack.isEmpty()) {
            return null;
        }

        ItineraryAction action = undoStack.pop();
        switch (action.getActionType()) {
            case ADD:
                itineraryList.deleteAtPosition(itineraryList.size() - 1);
                break;

            case INSERT:
                itineraryList.deleteAtPosition(action.getIndex());
                break;

            case REMOVE:
                itineraryList.insertAtPosition(action.getIndex(), action.getDestination());
                break;

            case REORDER:
            case CLEAR:
                itineraryList.clear();
                for (Destination d : action.getSnapshot()) {
                    itineraryList.insertAtEnd(d);
                }
                break;
        }

        persistState();
        return "Undone: " + action.getDescription();
    }

    public synchronized List<Destination> getItineraryList() {
        return itineraryList.toList();
    }

    public synchronized CustomLinkedList<Destination> getRawLinkedList() {
        return itineraryList;
    }

    public synchronized CustomStack<ItineraryAction> getUndoStack() {
        return undoStack;
    }

    public synchronized double getBudget() {
        return budget;
    }

    public synchronized ItineraryStats getStatistics() {
        List<Destination> items = itineraryList.toList();
        int stops = items.size();
        double cost = 0;
        double duration = 0;
        double ratingSum = 0;

        for (Destination d : items) {
            cost += d.getEstimatedCost();
            duration += d.getVisitDuration();
            ratingSum += d.getRating();
        }

        double avgRating = stops > 0 ? ratingSum / stops : 0.0;
        return new ItineraryStats(stops, cost, duration, avgRating, undoStack.size(), budget);
    }
}
