package com.wayfarer.servlet;

import com.wayfarer.algorithm.BFSRouteFinder;
import com.wayfarer.service.RouteService;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

/**
 * Wayfarer — Route Finder Controller
 * Executes BFS route traversal on the TravelGraph.
 */
@WebServlet(name = "wayfarerRouteServlet", urlPatterns = "/api/routes/*")
public class RouteServlet extends BaseServlet {

    private final RouteService routeService = RouteService.getInstance();

    public static class RouteSearchRequest {
        public String sourceCode;
        public String targetCode;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String pathInfo = req.getPathInfo();
        if (pathInfo != null && pathInfo.equalsIgnoreCase("/destinations")) {
            sendJsonResponse(resp, HttpServletResponse.SC_OK, com.wayfarer.service.DestinationService.getInstance().getAllDestinations());
            return;
        }

        if (pathInfo == null || pathInfo.equals("/") || pathInfo.equalsIgnoreCase("/bfs")) {
            String source = req.getParameter("source");
            if (source == null || source.trim().isEmpty()) {
                source = req.getParameter("origin");
            }
            String target = req.getParameter("target");
            if (target == null || target.trim().isEmpty()) {
                target = req.getParameter("destination");
            }
            if (source == null || target == null || source.isEmpty() || target.isEmpty()) {
                sendErrorResponse(resp, HttpServletResponse.SC_BAD_REQUEST, "Parameters 'source' (or 'origin') and 'target' (or 'destination') are required.");
                return;
            }

            BFSRouteFinder.BFSResult result = routeService.findBfsRoute(source.trim(), target.trim());
            sendJsonResponse(resp, HttpServletResponse.SC_OK, result);
            return;
        }

        sendErrorResponse(resp, HttpServletResponse.SC_NOT_FOUND, "Route endpoint not found.");
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String pathInfo = req.getPathInfo();

        if (pathInfo != null && pathInfo.equalsIgnoreCase("/bfs")) {
            RouteSearchRequest body = parseJsonBody(req, RouteSearchRequest.class);
            if (body == null || body.sourceCode == null || body.targetCode == null) {
                sendErrorResponse(resp, HttpServletResponse.SC_BAD_REQUEST, "Source and target destination codes are required.");
                return;
            }

            BFSRouteFinder.BFSResult result = routeService.findBfsRoute(body.sourceCode, body.targetCode);
            sendJsonResponse(resp, HttpServletResponse.SC_OK, result);
            return;
        }

        sendErrorResponse(resp, HttpServletResponse.SC_NOT_FOUND, "Route endpoint not found. Use POST /api/routes/bfs");
    }
}
