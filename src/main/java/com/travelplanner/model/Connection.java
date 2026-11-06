package com.travelplanner.model;

import java.util.Objects;

/**
 * Represents a connection (graph edge) between destinations with distance and travel cost.
 */
public class Connection {
    private String targetId;
    private double distanceKm;
    private double travelCost;

    public Connection() {}

    public Connection(String targetId, double distanceKm, double travelCost) {
        this.targetId = targetId;
        this.distanceKm = distanceKm;
        this.travelCost = travelCost;
    }

    public String getTargetId() {
        return targetId;
    }

    public void setTargetId(String targetId) {
        this.targetId = targetId;
    }

    public double getDistanceKm() {
        return distanceKm;
    }

    public void setDistanceKm(double distanceKm) {
        this.distanceKm = distanceKm;
    }

    public double getTravelCost() {
        return travelCost;
    }

    public void setTravelCost(double travelCost) {
        this.travelCost = travelCost;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Connection that = (Connection) o;
        return Objects.equals(targetId, that.targetId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(targetId);
    }

    @Override
    public String toString() {
        return "-> " + targetId + " (" + distanceKm + " km, $" + travelCost + ")";
    }
}
