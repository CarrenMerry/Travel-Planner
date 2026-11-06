package com.travelplanner.algorithms;

import com.travelplanner.model.Destination;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Custom Sorting Algorithms implemented explicitly in Java.
 * Provides Merge Sort and Quick Sort with performance counters and complexity analysis.
 */
public class SortingAlgorithms {

    public static class SortResult {
        public String algorithm;
        public String sortBy;
        public boolean ascending;
        public int recordCount;
        public long comparisons;
        public long swapsOrMerges;
        public long executionTimeNanos;
        public List<Destination> sortedList;
        public String timeComplexity;
        public String spaceComplexity;

        public SortResult(String algorithm, String sortBy, boolean ascending, int recordCount) {
            this.algorithm = algorithm;
            this.sortBy = sortBy;
            this.ascending = ascending;
            this.recordCount = recordCount;
        }
    }

    /**
     * Creates a comparator based on the requested criteria.
     */
    public static Comparator<Destination> getComparator(String criteria, boolean ascending) {
        Comparator<Destination> comp;
        switch (criteria.toLowerCase()) {
            case "cost":
                comp = Comparator.comparingDouble(Destination::getEstimatedCost);
                break;
            case "rating":
                comp = Comparator.comparingDouble(Destination::getRating);
                break;
            case "duration":
                comp = Comparator.comparingDouble(Destination::getVisitDuration);
                break;
            case "name":
            default:
                comp = Comparator.comparing(Destination::getName, String.CASE_INSENSITIVE_ORDER);
                break;
        }
        return ascending ? comp : comp.reversed();
    }

    // ==========================================
    // MERGE SORT (Divide and Conquer)
    // Time Complexity: O(n log n) Best, Average, Worst
    // Space Complexity: O(n) auxiliary space
    // ==========================================

    public static SortResult mergeSort(List<Destination> inputList, String criteria, boolean ascending) {
        SortResult result = new SortResult("Merge Sort", criteria, ascending, inputList != null ? inputList.size() : 0);
        result.timeComplexity = "O(n log n) Best/Average/Worst";
        result.spaceComplexity = "O(n) auxiliary memory for sub-arrays";

        if (inputList == null || inputList.isEmpty()) {
            result.sortedList = new ArrayList<>();
            return result;
        }

        List<Destination> list = new ArrayList<>(inputList);
        Comparator<Destination> comp = getComparator(criteria, ascending);

        long start = System.nanoTime();
        mergeSortRec(list, 0, list.size() - 1, comp, result);
        result.executionTimeNanos = System.nanoTime() - start;
        result.sortedList = list;
        return result;
    }

    private static void mergeSortRec(List<Destination> list, int left, int right,
                                     Comparator<Destination> comp, SortResult stats) {
        if (left < right) {
            int mid = left + (right - left) / 2;
            mergeSortRec(list, left, mid, comp, stats);
            mergeSortRec(list, mid + 1, right, comp, stats);
            merge(list, left, mid, right, comp, stats);
        }
    }

    private static void merge(List<Destination> list, int left, int mid, int right,
                              Comparator<Destination> comp, SortResult stats) {
        int n1 = mid - left + 1;
        int n2 = right - mid;

        Destination[] leftArr = new Destination[n1];
        Destination[] rightArr = new Destination[n2];

        for (int i = 0; i < n1; i++) {
            leftArr[i] = list.get(left + i);
        }
        for (int j = 0; j < n2; j++) {
            rightArr[j] = list.get(mid + 1 + j);
        }

        int i = 0, j = 0;
        int k = left;

        while (i < n1 && j < n2) {
            stats.comparisons++;
            stats.swapsOrMerges++;
            if (comp.compare(leftArr[i], rightArr[j]) <= 0) {
                list.set(k, leftArr[i]);
                i++;
            } else {
                list.set(k, rightArr[j]);
                j++;
            }
            k++;
        }

        while (i < n1) {
            stats.swapsOrMerges++;
            list.set(k, leftArr[i]);
            i++;
            k++;
        }

        while (j < n2) {
            stats.swapsOrMerges++;
            list.set(k, rightArr[j]);
            j++;
            k++;
        }
    }

    // ==========================================
    // QUICK SORT (Partitioning and Recursion)
    // Time Complexity: O(n log n) Best/Average, O(n^2) Worst (rare with median/pivot)
    // Space Complexity: O(log n) call stack
    // ==========================================

    public static SortResult quickSort(List<Destination> inputList, String criteria, boolean ascending) {
        SortResult result = new SortResult("Quick Sort", criteria, ascending, inputList != null ? inputList.size() : 0);
        result.timeComplexity = "O(n log n) Best/Average, O(n^2) Worst";
        result.spaceComplexity = "O(log n) call stack";

        if (inputList == null || inputList.isEmpty()) {
            result.sortedList = new ArrayList<>();
            return result;
        }

        List<Destination> list = new ArrayList<>(inputList);
        Comparator<Destination> comp = getComparator(criteria, ascending);

        long start = System.nanoTime();
        quickSortRec(list, 0, list.size() - 1, comp, result);
        result.executionTimeNanos = System.nanoTime() - start;
        result.sortedList = list;
        return result;
    }

    private static void quickSortRec(List<Destination> list, int low, int high,
                                     Comparator<Destination> comp, SortResult stats) {
        if (low < high) {
            int pi = partition(list, low, high, comp, stats);
            quickSortRec(list, low, pi - 1, comp, stats);
            quickSortRec(list, pi + 1, high, comp, stats);
        }
    }

    private static int partition(List<Destination> list, int low, int high,
                                 Comparator<Destination> comp, SortResult stats) {
        Destination pivot = list.get(high);
        int i = (low - 1);

        for (int j = low; j < high; j++) {
            stats.comparisons++;
            if (comp.compare(list.get(j), pivot) <= 0) {
                i++;
                swap(list, i, j, stats);
            }
        }
        swap(list, i + 1, high, stats);
        return i + 1;
    }

    private static void swap(List<Destination> list, int i, int j, SortResult stats) {
        if (i != j) {
            stats.swapsOrMerges++;
            Destination temp = list.get(i);
            list.set(i, list.get(j));
            list.set(j, temp);
        }
    }
}
