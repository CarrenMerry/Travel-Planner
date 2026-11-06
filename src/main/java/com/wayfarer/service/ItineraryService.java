package com.wayfarer.service;

import com.wayfarer.dao.DestinationDAO;
import com.wayfarer.dao.ItineraryDAO;
import com.wayfarer.dao.ItineraryStopDAO;
import com.wayfarer.ds.CustomLinkedList;
import com.wayfarer.model.Destination;
import com.wayfarer.model.Itinerary;
import com.wayfarer.model.ItineraryStop;

import java.util.List;

/**
 * Wayfarer — Itinerary Management Service
 *
 * Academic Data Structure Requirement 1 Integration:
 * Employs the custom Java CustomLinkedList to maintain in-memory ordered stops sequence,
 * performing index-based inserts, deletions, and drag-and-drop reordering before
 * persisting atomically to the MySQL 'itineraries' and 'itinerary_stops' tables.
 */
public class ItineraryService {

    private static final ItineraryService INSTANCE = new ItineraryService();
    private final ItineraryDAO itineraryDAO = new ItineraryDAO();
    private final ItineraryStopDAO stopDAO = new ItineraryStopDAO();
    private final DestinationDAO destinationDAO = new DestinationDAO();

    public static ItineraryService getInstance() {
        return INSTANCE;
    }

    private ItineraryService() {}

    public List<Itinerary> getUserItineraries(int userId) {
        return itineraryDAO.findAllByUserId(userId);
    }

    public List<Itinerary> getAllItineraries() {
        return itineraryDAO.findAll();
    }

    public Itinerary getItinerary(int id, int requestingUserId, boolean isAdmin) {
        Itinerary itinerary = itineraryDAO.findById(id);
        if (itinerary == null) return null;

        // Security: enforce ownership unless administrator
        if (!isAdmin && itinerary.getUserId() != requestingUserId) {
            throw new SecurityException("Access denied: You do not own this itinerary.");
        }
        return itinerary;
    }

    public int createItinerary(int userId, String name, double budget) {
        return itineraryDAO.create(userId, name, budget);
    }

    public boolean updateItineraryHeader(int id, int userId, boolean isAdmin, String name, double budget, String status) {
        Itinerary it = itineraryDAO.findById(id);
        if (it == null) return false;
        if (!isAdmin && it.getUserId() != userId) {
            throw new SecurityException("Access denied: Cannot update itinerary belonging to another user.");
        }
        return itineraryDAO.updateHeader(id, name, budget, status);
    }

    public boolean deleteItinerary(int id, int userId, boolean isAdmin) {
        Itinerary it = itineraryDAO.findById(id);
        if (it == null) return false;
        if (!isAdmin && it.getUserId() != userId) {
            throw new SecurityException("Access denied: Cannot delete itinerary belonging to another user.");
        }
        return itineraryDAO.delete(id, isAdmin ? 0 : userId);
    }

    /**
     * Adds a stop to an itinerary using the custom in-memory CustomLinkedList.
     */
    public boolean addStop(int itineraryId, int userId, boolean isAdmin, int destinationId, Integer insertIndex, String notes) {
        Itinerary itinerary = getItinerary(itineraryId, userId, isAdmin);
        if (itinerary == null) return false;

        Destination destination = destinationDAO.findById(destinationId);
        if (destination == null) {
            throw new IllegalArgumentException("Destination ID #" + destinationId + " not found.");
        }

        CustomLinkedList<ItineraryStop> list = itinerary.getStopsList();

        ItineraryStop newStop = new ItineraryStop(
                0, itineraryId, destinationId, list.size() + 1,
                destination.getEstimatedCost(),
                destination.getDurationDays(),
                notes
        );
        newStop.setDestination(destination);

        if (insertIndex != null && insertIndex >= 0 && insertIndex <= list.size()) {
            list.insert(insertIndex, newStop);
        } else {
            list.add(newStop);
        }

        // Persist the modified Linked List to MySQL
        return stopDAO.saveStopsAtomically(itineraryId, list.toList());
    }

    /**
     * Removes a stop from the itinerary at the specified index using CustomLinkedList.remove(index).
     */
    public boolean removeStop(int itineraryId, int userId, boolean isAdmin, int stopIndex) {
        Itinerary itinerary = getItinerary(itineraryId, userId, isAdmin);
        if (itinerary == null) return false;

        CustomLinkedList<ItineraryStop> list = itinerary.getStopsList();
        if (stopIndex < 0 || stopIndex >= list.size()) {
            throw new IndexOutOfBoundsException("Invalid stop index: " + stopIndex);
        }

        list.remove(stopIndex);

        // Persist updated sequence to MySQL
        return stopDAO.saveStopsAtomically(itineraryId, list.toList());
    }

    /**
     * Reorders stops in the itinerary using CustomLinkedList.reorder(fromIndex, toIndex).
     */
    public boolean reorderStops(int itineraryId, int userId, boolean isAdmin, int fromIndex, int toIndex) {
        Itinerary itinerary = getItinerary(itineraryId, userId, isAdmin);
        if (itinerary == null) return false;

        CustomLinkedList<ItineraryStop> list = itinerary.getStopsList();
        list.reorder(fromIndex, toIndex);

        // Persist updated sequence to MySQL
        return stopDAO.saveStopsAtomically(itineraryId, list.toList());
    }

    public boolean moveStopUp(int itineraryId, int userId, boolean isAdmin, int index) {
        Itinerary itinerary = getItinerary(itineraryId, userId, isAdmin);
        if (itinerary == null) return false;

        CustomLinkedList<ItineraryStop> list = itinerary.getStopsList();
        boolean moved = list.moveUp(index);
        if (!moved) return false;

        return stopDAO.saveStopsAtomically(itineraryId, list.toList());
    }

    public boolean moveStopDown(int itineraryId, int userId, boolean isAdmin, int index) {
        Itinerary itinerary = getItinerary(itineraryId, userId, isAdmin);
        if (itinerary == null) return false;

        CustomLinkedList<ItineraryStop> list = itinerary.getStopsList();
        boolean moved = list.moveDown(index);
        if (!moved) return false;

        return stopDAO.saveStopsAtomically(itineraryId, list.toList());
    }

    public int count() {
        return itineraryDAO.count();
    }
}
