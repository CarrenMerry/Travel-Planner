package com.travelplanner.servlet;

import com.travelplanner.model.TravelRequest;
import com.travelplanner.service.QueueService;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@WebServlet("/api/queue/*")
public class QueueServlet extends BaseServlet {

    private final QueueService queueService = QueueService.getInstance();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        Map<String, Object> data = new HashMap<>();
        data.put("pending", queueService.getPendingRequests());
        data.put("processed", queueService.getProcessedRequests());
        data.put("pendingCount", queueService.getPendingCount());
        data.put("processedCount", queueService.getProcessedCount());
        data.put("nextRequest", queueService.peekNext());
        sendJsonResponse(resp, HttpServletResponse.SC_OK, data);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String pathInfo = req.getPathInfo();

        if (pathInfo != null && pathInfo.equalsIgnoreCase("/process")) {
            TravelRequest processed = queueService.processNextRequest();
            if (processed == null) {
                sendErrorResponse(resp, HttpServletResponse.SC_BAD_REQUEST, "Queue is empty; no pending requests to process.");
                return;
            }

            Map<String, Object> res = new HashMap<>();
            res.put("success", true);
            res.put("message", "Processed travel request: " + processed.getId());
            res.put("processedRequest", processed);
            res.put("pendingCount", queueService.getPendingCount());
            sendJsonResponse(resp, HttpServletResponse.SC_OK, res);
            return;
        }

        // Default: enqueue
        try {
            TravelRequest request = parseJsonBody(req, TravelRequest.class);
            if (request == null || request.getTravelerName() == null || request.getTravelerName().trim().isEmpty()) {
                sendErrorResponse(resp, HttpServletResponse.SC_BAD_REQUEST, "Traveler name is required.");
                return;
            }

            TravelRequest enqueued = queueService.enqueueRequest(request);
            Map<String, Object> res = new HashMap<>();
            res.put("success", true);
            res.put("message", "Request enqueued successfully.");
            res.put("request", enqueued);
            res.put("pendingCount", queueService.getPendingCount());
            sendJsonResponse(resp, HttpServletResponse.SC_CREATED, res);
        } catch (Exception e) {
            sendErrorResponse(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }
}
