package com.wayfarer.model;

import java.sql.Timestamp;

/**
 * Wayfarer — ItineraryStop Model
 * Maps to MySQL 'itinerary_stops' table.
 * Represents an ordered stop inside a user's trip itinerary.
 */
public class ItineraryStop {

    private int id;
    private int itineraryId;
    private int destinationId;
    private int stopOrder;
    private double estimatedCost;
    private int durationDays;
    private String notes;
    private Timestamp createdAt;

    // Attached destination data for UI rendering
    private Destination destination;

    public ItineraryStop() {}

    public ItineraryStop(int id, int itineraryId, int destinationId, int stopOrder,
                         double estimatedCost, int durationDays, String notes) {
        this.id = id;
        this.itineraryId = itineraryId;
        this.destinationId = destinationId;
        this.stopOrder = stopOrder;
        this.estimatedCost = estimatedCost;
        this.durationDays = durationDays;
        this.notes = notes;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getItineraryId() { return itineraryId; }
    public void setItineraryId(int itineraryId) { this.itineraryId = itineraryId; }

    public int getDestinationId() { return destinationId; }
    public void setDestinationId(int destinationId) { this.destinationId = destinationId; }

    public int getStopOrder() { return stopOrder; }
    public void setStopOrder(int stopOrder) { this.stopOrder = stopOrder; }

    public double getEstimatedCost() { return estimatedCost; }
    public void setEstimatedCost(double estimatedCost) { this.estimatedCost = estimatedCost; }

    public int getDurationDays() { return durationDays; }
    public void setDurationDays(int durationDays) { this.durationDays = durationDays; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

    public Destination getDestination() { return destination; }
    public void setDestination(Destination destination) { this.destination = destination; }
}
