package com.travelplanner.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Core model representing a travel destination.
 * Maps directly to the 'destinations' table in MySQL while preserving
 * compatibility with all custom Java DSA algorithm implementations and UI controllers.
 */
public class Destination implements Comparable<Destination> {
    private int dbId;
    private String id;               // e.g. "DEST-01" (compatible with destinationCode)
    private String destinationCode;  // MySQL column 'destination_code'
    private String name;
    private String country;          // MySQL column 'country'
    private String city;             // MySQL column 'city'
    private String location;         // Composite: "City, Country" or "Country"
    private String description;
    private double estimatedCost;    // MySQL column 'estimated_cost'
    private double visitDuration;    // in days (MySQL column 'duration_days')
    private int durationDays;
    private int activitiesCount;     // MySQL column 'activities_count'
    private double rating;           // MySQL column 'rating'
    private String imageUrl;         // MySQL column 'image_url'
    private List<Activity> activities = new ArrayList<>();
    private List<Connection> connections = new ArrayList<>();

    public Destination() {}

    public Destination(String id, String name, String location, String description,
                       double estimatedCost, double visitDuration, double rating, String imageUrl) {
        this.id = id;
        this.destinationCode = id;
        this.name = name;
        this.location = location;
        parseLocation(location);
        this.description = description;
        this.estimatedCost = estimatedCost;
        this.visitDuration = visitDuration;
        this.durationDays = (int) Math.round(visitDuration);
        this.rating = rating;
        this.imageUrl = imageUrl;
    }

    public Destination(int dbId, String destinationCode, String name, String country, String city,
                       String description, double estimatedCost, int durationDays, int activitiesCount,
                       double rating, String imageUrl) {
        this.dbId = dbId;
        this.dbId = dbId;
        this.destinationCode = destinationCode;
        this.id = destinationCode != null ? destinationCode : ("DEST-" + dbId);
        this.name = name;
        this.country = country;
        this.city = city;
        if (city != null && !city.trim().isEmpty() && country != null && !country.trim().isEmpty()) {
            this.location = city.trim() + ", " + country.trim();
        } else if (country != null && !country.trim().isEmpty()) {
            this.location = country.trim();
        } else {
            this.location = city != null ? city.trim() : "";
        }
        this.description = description;
        this.estimatedCost = estimatedCost;
        this.durationDays = durationDays;
        this.visitDuration = durationDays > 0 ? durationDays : 1.0;
        this.activitiesCount = activitiesCount;
        this.rating = rating;
        this.imageUrl = imageUrl;
    }

    private void parseLocation(String loc) {
        if (loc == null || loc.trim().isEmpty()) return;
        if (loc.contains(",")) {
            String[] parts = loc.split(",", 2);
            this.city = parts[0].trim();
            this.country = parts[1].trim();
        } else {
            this.country = loc.trim();
            this.city = loc.trim();
        }
    }

    // Validation logic
    public boolean isValid() {
        if (getId() == null || getId().trim().isEmpty()) return false;
        if (name == null || name.trim().isEmpty()) return false;
        if (getCountry() == null || getCountry().trim().isEmpty()) {
            if (location == null || location.trim().isEmpty()) return false;
        }
        if (estimatedCost < 0) return false;
        if (getVisitDuration() <= 0) return false;
        if (rating < 0.0 || rating > 5.0) return false;
        return true;
    }

    public String validateAndGetError() {
        if (getId() == null || getId().trim().isEmpty()) return "Destination code/ID cannot be empty.";
        if (name == null || name.trim().isEmpty()) return "Destination Name cannot be empty.";
        if ((country == null || country.trim().isEmpty()) && (location == null || location.trim().isEmpty())) {
            return "Country or Location cannot be empty.";
        }
        if (estimatedCost < 0) return "Estimated cost must be a non-negative number.";
        if (getVisitDuration() <= 0) return "Visit duration must be greater than zero.";
        if (rating < 0.0 || rating > 5.0) return "Rating must be between 1.0 and 5.0.";
        return null;
    }

    public int getDbId() {
        return dbId;
    }

    public void setDbId(int dbId) {
        this.dbId = dbId;
    }

    public String getId() {
        if (destinationCode != null && !destinationCode.trim().isEmpty()) {
            return destinationCode;
        }
        return id;
    }

    public void setId(String id) {
        this.id = id;
        if (this.destinationCode == null) {
            this.destinationCode = id;
        }
    }

    public String getDestinationCode() {
        if (destinationCode != null && !destinationCode.trim().isEmpty()) {
            return destinationCode;
        }
        return id;
    }

    public void setDestinationCode(String destinationCode) {
        this.destinationCode = destinationCode;
        this.id = destinationCode;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCountry() {
        if (country != null && !country.trim().isEmpty()) {
            return country;
        }
        parseLocation(this.location);
        return country != null ? country : "";
    }

    public void setCountry(String country) {
        this.country = country;
        updateLocation();
    }

    public String getCity() {
        if (city != null && !city.trim().isEmpty()) {
            return city;
        }
        parseLocation(this.location);
        return city != null ? city : "";
    }

    public void setCity(String city) {
        this.city = city;
        updateLocation();
    }

    private void updateLocation() {
        if (city != null && !city.trim().isEmpty() && country != null && !country.trim().isEmpty()) {
            this.location = city.trim() + ", " + country.trim();
        } else if (country != null && !country.trim().isEmpty()) {
            this.location = country.trim();
        } else if (city != null && !city.trim().isEmpty()) {
            this.location = city.trim();
        }
    }

    public String getLocation() {
        if (location != null && !location.trim().isEmpty()) {
            return location;
        }
        updateLocation();
        return location != null ? location : "";
    }

    public void setLocation(String location) {
        this.location = location;
        parseLocation(location);
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public double getEstimatedCost() {
        return estimatedCost;
    }

    public void setEstimatedCost(double estimatedCost) {
        this.estimatedCost = estimatedCost;
    }

    public double getVisitDuration() {
        if (visitDuration > 0) return visitDuration;
        if (durationDays > 0) return durationDays;
        return 1.0;
    }

    public void setVisitDuration(double visitDuration) {
        this.visitDuration = visitDuration;
        this.durationDays = (int) Math.round(visitDuration);
    }

    public int getDurationDays() {
        if (durationDays > 0) return durationDays;
        return (int) Math.round(getVisitDuration());
    }

    public void setDurationDays(int durationDays) {
        this.durationDays = durationDays;
        this.visitDuration = durationDays;
    }

    public int getActivitiesCount() {
        if (activitiesCount > 0) return activitiesCount;
        return activities != null ? activities.size() : 0;
    }

    public void setActivitiesCount(int activitiesCount) {
        this.activitiesCount = activitiesCount;
    }

    public double getRating() {
        return rating;
    }

    public void setRating(double rating) {
        this.rating = rating;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public List<Activity> getActivities() {
        return activities;
    }

    public void setActivities(List<Activity> activities) {
        this.activities = activities != null ? activities : new ArrayList<>();
        if (this.activities != null && !this.activities.isEmpty()) {
            this.activitiesCount = this.activities.size();
        }
    }

    public void addActivity(Activity activity) {
        if (activity != null) {
            this.activities.add(activity);
            this.activitiesCount = this.activities.size();
        }
    }

    public List<Connection> getConnections() {
        return connections;
    }

    public void setConnections(List<Connection> connections) {
        this.connections = connections != null ? connections : new ArrayList<>();
    }

    public void addConnection(Connection connection) {
        if (connection != null) {
            this.connections.removeIf(c -> c.getTargetId().equalsIgnoreCase(connection.getTargetId()));
            this.connections.add(connection);
        }
    }

    public void removeConnection(String targetId) {
        this.connections.removeIf(c -> c.getTargetId().equalsIgnoreCase(targetId));
    }

    @Override
    public int compareTo(Destination o) {
        if (o == null) return 1;
        return this.name.compareToIgnoreCase(o.name);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Destination that = (Destination) o;
        return Objects.equals(getId(), that.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId());
    }

    @Override
    public String toString() {
        return name + " (" + getLocation() + ")";
    }
}
