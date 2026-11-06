package com.travelplanner.algorithms;

import com.travelplanner.model.Destination;

import java.util.ArrayList;
import java.util.List;

/**
 * Custom Search Algorithms implemented in Java.
 * Tracks comparison counts, execution time, and step-by-step traces for educational comparison.
 */
public class SearchingAlgorithms {

    public static class SearchTraceStep {
        public int stepNumber;
        public int index;
        public String examinedItem;
        public String status; // "CHECKING", "MATCH", "MISMATCH", "HALVED_LEFT", "HALVED_RIGHT"
        public int low;       // for binary search
        public int mid;       // for binary search
        public int high;      // for binary search

        public SearchTraceStep(int stepNumber, int index, String examinedItem, String status, int low, int mid, int high) {
            this.stepNumber = stepNumber;
            this.index = index;
            this.examinedItem = examinedItem;
            this.status = status;
            this.low = low;
            this.mid = mid;
            this.high = high;
        }
    }

    public static class SearchResult {
        public String algorithm;
        public boolean found;
        public Destination destination;
        public int indexFound;
        public int totalComparisons;
        public long executionTimeNanos;
        public List<SearchTraceStep> steps = new ArrayList<>();
        public String explanation;

        public SearchResult(String algorithm) {
            this.algorithm = algorithm;
        }
    }

    /**
     * Linear Search over an array/list of destinations.
     * Works on unsorted data.
     * Time Complexity: Best O(1), Average O(n), Worst O(n)
     * Space Complexity: O(1)
     */
    public static SearchResult linearSearch(List<Destination> list, String query) {
        SearchResult res = new SearchResult("Linear Search");
        res.explanation = "Linear Search checks every record sequentially from index 0 to n-1. Operates on unsorted data.";

        if (list == null || query == null || query.trim().isEmpty()) {
            res.found = false;
            res.indexFound = -1;
            return res;
        }

        String q = query.trim().toLowerCase();
        long start = System.nanoTime();

        for (int i = 0; i < list.size(); i++) {
            res.totalComparisons++;
            Destination d = list.get(i);
            boolean match = d.getId().equalsIgnoreCase(q) ||
                            d.getName().toLowerCase().contains(q) ||
                            d.getLocation().toLowerCase().contains(q);

            if (match) {
                res.steps.add(new SearchTraceStep(i + 1, i, d.getName(), "MATCH", -1, -1, -1));
                res.found = true;
                res.destination = d;
                res.indexFound = i;
                res.executionTimeNanos = System.nanoTime() - start;
                return res;
            } else {
                res.steps.add(new SearchTraceStep(i + 1, i, d.getName(), "MISMATCH", -1, -1, -1));
            }
        }

        res.found = false;
        res.indexFound = -1;
        res.executionTimeNanos = System.nanoTime() - start;
        return res;
    }

    /**
     * Binary Search over a sorted list of destinations by Name.
     * Requires the list to be strictly sorted by name.
     * Time Complexity: Best O(1), Average O(log n), Worst O(log n)
     * Space Complexity: O(1)
     */
    public static SearchResult binarySearchByName(List<Destination> sortedList, String targetName) {
        SearchResult res = new SearchResult("Binary Search");
        res.explanation = "Binary Search halves the search space at every step using mid = (low + high) / 2. Requires sorted data.";

        if (sortedList == null || targetName == null || targetName.trim().isEmpty()) {
            res.found = false;
            res.indexFound = -1;
            return res;
        }

        String target = targetName.trim().toLowerCase();
        long start = System.nanoTime();

        int low = 0;
        int high = sortedList.size() - 1;
        int step = 1;

        while (low <= high) {
            int mid = low + (high - low) / 2;
            res.totalComparisons++;
            Destination midDest = sortedList.get(mid);
            String midName = midDest.getName().toLowerCase();

            int cmp = midName.compareTo(target);

            if (cmp == 0 || midName.contains(target)) {
                res.steps.add(new SearchTraceStep(step++, mid, midDest.getName(), "MATCH", low, mid, high));
                res.found = true;
                res.destination = midDest;
                res.indexFound = mid;
                res.executionTimeNanos = System.nanoTime() - start;
                return res;
            } else if (cmp < 0) {
                res.steps.add(new SearchTraceStep(step++, mid, midDest.getName(), "HALVED_RIGHT (Target is alphabetically after)", low, mid, high));
                low = mid + 1;
            } else {
                res.steps.add(new SearchTraceStep(step++, mid, midDest.getName(), "HALVED_LEFT (Target is alphabetically before)", low, mid, high));
                high = mid - 1;
            }
        }

        res.found = false;
        res.indexFound = -1;
        res.executionTimeNanos = System.nanoTime() - start;
        return res;
    }
}
