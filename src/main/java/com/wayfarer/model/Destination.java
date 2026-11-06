package com.wayfarer.model;

import java.sql.Timestamp;

/**
 * Wayfarer — Destination Model
 * Maps directly to MySQL 'destinations' table.
 */
public class Destination {

    private int id;
    private String destinationCode;
    private String name;
    private String country;
    private String city;
    private String category = "Cultural";
    private String description;
    private double estimatedCost;
    private int durationDays;
    private int activitiesCount;
    private double rating;
    private String imageUrl;
    private Timestamp createdAt;

    public Destination() {}

    public Destination(int id, String destinationCode, String name, String country, String city,
                       String description, double estimatedCost, int durationDays,
                       int activitiesCount, double rating, String imageUrl) {
        this.id = id;
        this.destinationCode = destinationCode;
        this.name = name;
        this.country = country;
        this.city = city;
        this.description = description;
        this.estimatedCost = estimatedCost;
        this.durationDays = durationDays;
        this.activitiesCount = activitiesCount;
        this.rating = rating;
        this.imageUrl = imageUrl;
    }

    public Destination(int id, String destinationCode, String name, String country, String city,
                       String category, String description, double estimatedCost, int durationDays,
                       int activitiesCount, double rating, String imageUrl) {
        this(id, destinationCode, name, country, city, description, estimatedCost, durationDays, activitiesCount, rating, imageUrl);
        this.category = category != null ? category : "Cultural";
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getDestinationCode() { return destinationCode; }
    public void setDestinationCode(String destinationCode) { this.destinationCode = destinationCode; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getCountry() { return country; }
    public void setCountry(String country) { this.country = country; }

    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public double getEstimatedCost() { return estimatedCost; }
    public void setEstimatedCost(double estimatedCost) { this.estimatedCost = estimatedCost; }

    public int getDurationDays() { return durationDays; }
    public void setDurationDays(int durationDays) { this.durationDays = durationDays; }

    public int getActivitiesCount() { return activitiesCount; }
    public void setActivitiesCount(int activitiesCount) { this.activitiesCount = activitiesCount; }

    public double getRating() { return rating; }
    public void setRating(double rating) { this.rating = rating; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

    public String getLocation() {
        if (city != null && !city.isBlank()) {
            return city + ", " + country;
        }
        return country;
    }
}
