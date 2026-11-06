package com.travelplanner.service;

import com.travelplanner.ds.CustomQueue;
import com.travelplanner.model.TravelRequest;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Service managing asynchronous travel planning and exploration requests
 * using the custom Linked-Node Queue.
 */
public class QueueService {

    private static QueueService instance;

    private final CustomQueue<TravelRequest> pendingQueue = new CustomQueue<>();
    private final List<TravelRequest> processedRequests = new ArrayList<>();

    private QueueService() {
        // Seed initial sample queue requests for immediate academic demonstration
        enqueueRequest(new TravelRequest("REQ-101", "Dr. Alan Turing", "Route Exploration", "DEST-01", "Tokyo Imperial Trails"));
        enqueueRequest(new TravelRequest("REQ-102", "Ada Lovelace", "Itinerary Optimization", "DEST-02", "Parisian Art & Culture"));
        enqueueRequest(new TravelRequest("REQ-103", "Grace Hopper", "Budget Analysis", "DEST-10", "Iceland Glaciers & Northern Lights"));
    }

    public static synchronized QueueService getInstance() {
        if (instance == null) {
            instance = new QueueService();
        }
        return instance;
    }

    public synchronized TravelRequest enqueueRequest(TravelRequest request) {
        if (request.getId() == null || request.getId().trim().isEmpty()) {
            request.setId("REQ-" + UUID.randomUUID().toString().substring(0, 6).toUpperCase());
        }
        request.setStatus("PENDING");
        pendingQueue.enqueue(request);
        return request;
    }

    public synchronized TravelRequest processNextRequest() {
        if (pendingQueue.isEmpty()) {
            return null;
        }

        TravelRequest req = pendingQueue.dequeue();
        req.setStatus("COMPLETED");

        switch (req.getRequestType()) {
            case "Route Exploration":
                req.setResultSummary("Discovered multi-stop graph connectivity and verified shortest flight path to " + req.getDestinationName());
                break;
            case "Itinerary Optimization":
                req.setResultSummary("Generated optimal budget-constrained schedule maximizing travel satisfaction score.");
                break;
            case "Budget Analysis":
                req.setResultSummary("Evaluated expense formula and balanced estimated costs against itinerary constraints.");
                break;
            default:
                req.setResultSummary("Successfully fulfilled travel planning job for " + req.getDestinationName());
                break;
        }

        processedRequests.add(0, req); // insert newest at top
        return req;
    }

    public synchronized List<TravelRequest> getPendingRequests() {
        return pendingQueue.toList();
    }

    public synchronized List<TravelRequest> getProcessedRequests() {
        return new ArrayList<>(processedRequests);
    }

    public synchronized TravelRequest peekNext() {
        return pendingQueue.isEmpty() ? null : pendingQueue.peek();
    }

    public synchronized int getPendingCount() {
        return pendingQueue.size();
    }

    public synchronized int getProcessedCount() {
        return processedRequests.size();
    }
}
