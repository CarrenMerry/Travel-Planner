package com.travelplanner.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Represents a travel planning or route exploration request queued for background or sequential processing.
 */
public class TravelRequest {
    private String id;
    private String travelerName;
    private String requestType; // "Route Exploration", "Itinerary Optimization", "Budget Analysis"
    private String destinationId;
    private String destinationName;
    private String status; // "PENDING", "PROCESSING", "COMPLETED"
    private String createdAt;
    private String resultSummary;

    public TravelRequest() {}

    public TravelRequest(String id, String travelerName, String requestType, String destinationId, String destinationName) {
        this.id = id;
        this.travelerName = travelerName;
        this.requestType = requestType;
        this.destinationId = destinationId;
        this.destinationName = destinationName;
        this.status = "PENDING";
        this.createdAt = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTravelerName() {
        return travelerName;
    }

    public void setTravelerName(String travelerName) {
        this.travelerName = travelerName;
    }

    public String getRequestType() {
        return requestType;
    }

    public void setRequestType(String requestType) {
        this.requestType = requestType;
    }

    public String getDestinationId() {
        return destinationId;
    }

    public void setDestinationId(String destinationId) {
        this.destinationId = destinationId;
    }

    public String getDestinationName() {
        return destinationName;
    }

    public void setDestinationName(String destinationName) {
        this.destinationName = destinationName;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getResultSummary() {
        return resultSummary;
    }

    public void setResultSummary(String resultSummary) {
        this.resultSummary = resultSummary;
    }

    @Override
    public String toString() {
        return "[" + status + "] " + travelerName + " - " + requestType + " for " + destinationName;
    }
}
