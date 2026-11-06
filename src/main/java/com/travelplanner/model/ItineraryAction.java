package com.travelplanner.model;

import java.util.List;

/**
 * Represents an action performed on the itinerary, stored in the custom Stack for undo functionality.
 */
public class ItineraryAction {
    public enum ActionType {
        ADD,
        INSERT,
        REMOVE,
        CLEAR,
        REORDER
    }

    private ActionType actionType;
    private Destination destination;
    private int index;
    private List<Destination> snapshot; // Used for multi-item actions like CLEAR or REORDER
    private String description;

    public ItineraryAction(ActionType actionType, Destination destination, int index, String description) {
        this.actionType = actionType;
        this.destination = destination;
        this.index = index;
        this.description = description;
    }

    public ItineraryAction(ActionType actionType, List<Destination> snapshot, String description) {
        this.actionType = actionType;
        this.snapshot = snapshot;
        this.description = description;
    }

    public ActionType getActionType() {
        return actionType;
    }

    public Destination getDestination() {
        return destination;
    }

    public int getIndex() {
        return index;
    }

    public List<Destination> getSnapshot() {
        return snapshot;
    }

    public String getDescription() {
        return description;
    }

    @Override
    public String toString() {
        return "Action: " + actionType + " - " + description;
    }
}
