package com.wayfarer.model;

/**
 * Wayfarer — TravelConnection Model
 * Maps to MySQL 'travel_connections' table. Represents graph edges between destinations.
 */
public class TravelConnection {

    private int id;
    private int sourceDestinationId;
    private int targetDestinationId;
    private String sourceCode;
    private String targetCode;
    private String sourceName;
    private String targetName;
    private double distanceKm;
    private double estimatedTravelCost;
    private Integer estimatedTravelMinutes;

    public TravelConnection() {}

    public TravelConnection(int id, int sourceDestinationId, int targetDestinationId,
                            String sourceCode, String targetCode,
                            double distanceKm, double estimatedTravelCost, Integer estimatedTravelMinutes) {
        this.id = id;
        this.sourceDestinationId = sourceDestinationId;
        this.targetDestinationId = targetDestinationId;
        this.sourceCode = sourceCode;
        this.targetCode = targetCode;
        this.distanceKm = distanceKm;
        this.estimatedTravelCost = estimatedTravelCost;
        this.estimatedTravelMinutes = estimatedTravelMinutes;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getSourceDestinationId() { return sourceDestinationId; }
    public void setSourceDestinationId(int sourceDestinationId) { this.sourceDestinationId = sourceDestinationId; }

    public int getTargetDestinationId() { return targetDestinationId; }
    public void setTargetDestinationId(int targetDestinationId) { this.targetDestinationId = targetDestinationId; }

    public String getSourceCode() { return sourceCode; }
    public void setSourceCode(String sourceCode) { this.sourceCode = sourceCode; }

    public String getTargetCode() { return targetCode; }
    public void setTargetCode(String targetCode) { this.targetCode = targetCode; }

    public String getSourceName() { return sourceName; }
    public void setSourceName(String sourceName) { this.sourceName = sourceName; }

    public String getTargetName() { return targetName; }
    public void setTargetName(String targetName) { this.targetName = targetName; }

    public double getDistanceKm() { return distanceKm; }
    public void setDistanceKm(double distanceKm) { this.distanceKm = distanceKm; }

    public double getEstimatedTravelCost() { return estimatedTravelCost; }
    public void setEstimatedTravelCost(double estimatedTravelCost) { this.estimatedTravelCost = estimatedTravelCost; }

    public Integer getEstimatedTravelMinutes() { return estimatedTravelMinutes; }
    public void setEstimatedTravelMinutes(Integer estimatedTravelMinutes) { this.estimatedTravelMinutes = estimatedTravelMinutes; }
}
