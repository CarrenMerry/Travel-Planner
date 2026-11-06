package com.wayfarer.servlet;

import com.wayfarer.algorithm.GreedyRecommendationService;
import com.wayfarer.service.RecommendationService;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

/**
 * Wayfarer — Smart Recommendation Controller
 * Executes Greedy Algorithm destination recommendations.
 */
@WebServlet(name = "wayfarerRecommendationServlet", urlPatterns = "/api/recommendations/*")
public class RecommendationServlet extends BaseServlet {

    private final RecommendationService recommendationService = RecommendationService.getInstance();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String pathInfo = req.getPathInfo();
        if (pathInfo == null || pathInfo.equals("/") || pathInfo.equalsIgnoreCase("/greedy")) {
            GreedyRecommendationService.RecommendationRequest body = new GreedyRecommendationService.RecommendationRequest();
            String cat = req.getParameter("category");
            String budgetStr = req.getParameter("budget");
            String stopsStr = req.getParameter("stops");
            String startCode = req.getParameter("start");

            if (cat != null && !cat.isEmpty()) body.preferredCategory = cat;
            if (startCode != null && !startCode.isEmpty()) body.startDestinationCode = startCode;
            if (budgetStr != null && !budgetStr.isEmpty()) {
                try {
                    body.totalBudget = Double.parseDouble(budgetStr);
                } catch (NumberFormatException ignored) {}
            }
            if (stopsStr != null && !stopsStr.isEmpty()) {
                try {
                    body.desiredStops = Integer.parseInt(stopsStr);
                } catch (NumberFormatException ignored) {}
            }

            GreedyRecommendationService.RecommendationResult result = recommendationService.recommend(body);
            sendJsonResponse(resp, HttpServletResponse.SC_OK, result);
            return;
        }

        sendErrorResponse(resp, HttpServletResponse.SC_NOT_FOUND, "Endpoint not found.");
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String pathInfo = req.getPathInfo();

        if (pathInfo != null && pathInfo.equalsIgnoreCase("/greedy")) {
            GreedyRecommendationService.RecommendationRequest body =
                    parseJsonBody(req, GreedyRecommendationService.RecommendationRequest.class);

            if (body == null) {
                body = new GreedyRecommendationService.RecommendationRequest();
            }

            GreedyRecommendationService.RecommendationResult result = recommendationService.recommend(body);
            sendJsonResponse(resp, HttpServletResponse.SC_OK, result);
            return;
        }

        sendErrorResponse(resp, HttpServletResponse.SC_NOT_FOUND, "Endpoint not found. Use POST /api/recommendations/greedy");
    }
}
