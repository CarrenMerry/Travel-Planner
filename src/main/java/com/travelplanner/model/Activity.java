package com.travelplanner.model;

import java.util.Objects;

/**
 * Represents an activity or experience at a destination.
 * Used for dynamic programming budget-constrained optimization (0/1 Knapsack).
 */
public class Activity {
    private String id;
    private String name;
    private double cost;
    private int travelValue; // Satisfaction / reward points (e.g. 10 - 100)

    public Activity() {}

    public Activity(String id, String name, double cost, int travelValue) {
        this.id = id;
        this.name = name;
        this.cost = cost;
        this.travelValue = travelValue;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getCost() {
        return cost;
    }

    public void setCost(double cost) {
        this.cost = cost;
    }

    public int getTravelValue() {
        return travelValue;
    }

    public void setTravelValue(int travelValue) {
        this.travelValue = travelValue;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Activity activity = (Activity) o;
        return Objects.equals(id, activity.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return name + " ($" + cost + ", " + travelValue + " pts)";
    }
}
