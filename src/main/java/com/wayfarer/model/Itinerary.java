package com.wayfarer.model;

import com.wayfarer.ds.CustomLinkedList;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

/**
 * Wayfarer — Itinerary Model
 * Maps to MySQL 'itineraries' table.
 * Holds user's trip header and ordered stops represented via CustomLinkedList.
 */
public class Itinerary {

    private int id;
    private int userId;
    private String name;
    private double budget;
    private double totalEstimatedCost;
    private int totalDurationDays;
    private String status = "DRAFT";
    private Timestamp createdAt;
    private Timestamp updatedAt;

    // Ordered sequence managed via custom Linked List
    private CustomLinkedList<ItineraryStop> stopsList = new CustomLinkedList<>();

    public Itinerary() {}

    public Itinerary(int id, int userId, String name, double budget,
                     double totalEstimatedCost, int totalDurationDays, String status) {
        this.id = id;
        this.userId = userId;
        this.name = name;
        this.budget = budget;
        this.totalEstimatedCost = totalEstimatedCost;
        this.totalDurationDays = totalDurationDays;
        this.status = status != null ? status : "DRAFT";
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public double getBudget() { return budget; }
    public void setBudget(double budget) { this.budget = budget; }

    public double getTotalEstimatedCost() { return totalEstimatedCost; }
    public void setTotalEstimatedCost(double totalEstimatedCost) { this.totalEstimatedCost = totalEstimatedCost; }

    public int getTotalDurationDays() { return totalDurationDays; }
    public void setTotalDurationDays(int totalDurationDays) { this.totalDurationDays = totalDurationDays; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

    public Timestamp getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Timestamp updatedAt) { this.updatedAt = updatedAt; }

    public CustomLinkedList<ItineraryStop> getStopsList() { return stopsList; }
    public void setStopsList(CustomLinkedList<ItineraryStop> stopsList) { this.stopsList = stopsList; }

    public List<ItineraryStop> getStops() {
        return stopsList != null ? stopsList.toList() : new ArrayList<>();
    }

    public void recalculateTotals() {
        double cost = 0;
        int duration = 0;
        if (stopsList != null) {
            for (ItineraryStop s : stopsList) {
                cost += s.getEstimatedCost();
                duration += s.getDurationDays();
            }
        }
        this.totalEstimatedCost = cost;
        this.totalDurationDays = duration;
    }
}
