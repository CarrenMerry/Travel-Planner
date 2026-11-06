package com.travelplanner.servlet;

import com.travelplanner.algorithms.SearchingAlgorithms;
import com.travelplanner.algorithms.SortingAlgorithms;
import com.travelplanner.model.Destination;
import com.travelplanner.service.DestinationService;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@WebServlet("/api/search-sort/*")
public class SearchSortServlet extends BaseServlet {

    private final DestinationService destinationService = DestinationService.getInstance();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String pathInfo = req.getPathInfo();

        if (pathInfo != null && pathInfo.equalsIgnoreCase("/search")) {
            handleSearch(req, resp);
        } else if (pathInfo != null && pathInfo.equalsIgnoreCase("/sort")) {
            handleSort(req, resp);
        } else {
            sendErrorResponse(resp, HttpServletResponse.SC_BAD_REQUEST, "Endpoint must be /search or /sort.");
        }
    }

    private void handleSearch(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String query = req.getParameter("query");
        String algo = req.getParameter("algo"); // "linear" or "binary"

        if (query == null || query.trim().isEmpty()) {
            sendErrorResponse(resp, HttpServletResponse.SC_BAD_REQUEST, "Parameter 'query' is required.");
            return;
        }

        List<Destination> all = destinationService.getAllDestinations();

        if (algo != null && algo.equalsIgnoreCase("binary")) {
            // Binary search requires pre-sorted array by name
            all.sort(Comparator.comparing(Destination::getName, String.CASE_INSENSITIVE_ORDER));
            SearchingAlgorithms.SearchResult result = SearchingAlgorithms.binarySearchByName(all, query);
            sendJsonResponse(resp, HttpServletResponse.SC_OK, result);
        } else {
            // Default: Linear search on current unsorted list
            SearchingAlgorithms.SearchResult result = SearchingAlgorithms.linearSearch(all, query);
            sendJsonResponse(resp, HttpServletResponse.SC_OK, result);
        }
    }

    private void handleSort(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String criteria = req.getParameter("by");       // "cost", "rating", "duration", "name"
        String order = req.getParameter("order");       // "asc" or "desc"
        String algo = req.getParameter("algo");         // "mergesort" or "quicksort"

        if (criteria == null) criteria = "cost";
        boolean ascending = order == null || !order.equalsIgnoreCase("desc");
        if (algo == null) algo = "mergesort";

        List<Destination> all = destinationService.getAllDestinations();

        SortingAlgorithms.SortResult result;
        if (algo.equalsIgnoreCase("quicksort")) {
            result = SortingAlgorithms.quickSort(all, criteria, ascending);
        } else {
            result = SortingAlgorithms.mergeSort(all, criteria, ascending);
        }

        // Also run the other algorithm for side-by-side performance comparison
        SortingAlgorithms.SortResult altResult;
        if (algo.equalsIgnoreCase("quicksort")) {
            altResult = SortingAlgorithms.mergeSort(all, criteria, ascending);
        } else {
            altResult = SortingAlgorithms.quickSort(all, criteria, ascending);
        }

        Map<String, Object> comparison = new HashMap<>();
        comparison.put("primary", result);
        comparison.put("alternative", altResult);

        sendJsonResponse(resp, HttpServletResponse.SC_OK, comparison);
    }
}
