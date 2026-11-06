package com.travelplanner.algorithms;

import com.travelplanner.model.Activity;

import java.util.ArrayList;
import java.util.List;

/**
 * Dynamic Programming Planner for Budget-Constrained Experience Selection.
 * Formulates the travel activity selection as a 0/1 Knapsack problem.
 * State: dp[i][b] = maximum travel value achievable using a subset of the first i activities with budget b.
 * Recurrence:
 *   If cost[i] > b: dp[i][b] = dp[i-1][b]
 *   Else: dp[i][b] = max(dp[i-1][b], dp[i-1][b - cost[i]] + value[i])
 */
public class DynamicProgrammingPlanner {

    public static class DpCellSnapshot {
        public int activityIndex;
        public String activityName;
        public int budgetCol;
        public int value;

        public DpCellSnapshot(int activityIndex, String activityName, int budgetCol, int value) {
            this.activityIndex = activityIndex;
            this.activityName = activityName;
            this.budgetCol = budgetCol;
            this.value = value;
        }
    }

    public static class DpResult {
        public double totalBudget;
        public int budgetUnits;
        public int unitScale; // e.g. $10 or $1 per DP column
        public List<Activity> availableActivities = new ArrayList<>();
        public List<Activity> selectedActivities = new ArrayList<>();
        public double totalCost;
        public int totalTravelValue;
        public double remainingBudget;
        public String recurrenceRelation;
        public String stateDefinition;
        public String explanation;
        public int[][] dpTableSample; // preview slice of DP table for rendering
        public List<String> tableRowHeaders = new ArrayList<>();
        public List<Integer> tableColHeaders = new ArrayList<>();

        public DpResult() {
            this.stateDefinition = "dp[i][b]: Maximum travel satisfaction value using a subset of first i activities with budget limit b.";
            this.recurrenceRelation = "dp[i][b] = max(dp[i-1][b], dp[i-1][b - cost[i]] + value[i])";
        }
    }

    /**
     * Solves the budget-constrained activity selection problem.
     * Time Complexity: O(N * W) where N is number of activities, W is budget / unitScale.
     * Space Complexity: O(N * W) for the explicit DP table.
     */
    public static DpResult optimizeActivities(List<Activity> activities, double budget) {
        DpResult result = new DpResult();
        result.totalBudget = budget;

        if (activities == null || activities.isEmpty() || budget <= 0) {
            result.explanation = "No activities available or budget is 0.";
            return result;
        }

        result.availableActivities = new ArrayList<>(activities);
        int n = activities.size();

        // Dynamically choose budget unit scale to keep DP table dimensions optimal
        // e.g. for budget $1000, scale = 10 -> 100 columns.
        int scale = 1;
        if (budget > 5000) scale = 50;
        else if (budget > 1000) scale = 20;
        else if (budget > 300) scale = 10;
        else if (budget > 100) scale = 5;

        result.unitScale = scale;
        int maxWeight = (int) Math.floor(budget / scale);
        result.budgetUnits = maxWeight;

        int[] weights = new int[n + 1];
        int[] values = new int[n + 1];

        for (int i = 0; i < n; i++) {
            Activity a = activities.get(i);
            weights[i + 1] = Math.max(1, (int) Math.round(a.getCost() / scale));
            values[i + 1] = a.getTravelValue();
        }

        int[][] dp = new int[n + 1][maxWeight + 1];

        // Fill DP table
        for (int i = 1; i <= n; i++) {
            for (int w = 0; w <= maxWeight; w++) {
                if (weights[i] <= w) {
                    dp[i][w] = Math.max(dp[i - 1][w], dp[i - 1][w - weights[i]] + values[i]);
                } else {
                    dp[i][w] = dp[i - 1][w];
                }
            }
        }

        // Backtrack to reconstruct the optimal set of activities
        int w = maxWeight;
        List<Activity> chosen = new ArrayList<>();
        double accumulatedCost = 0;

        for (int i = n; i > 0; i--) {
            if (dp[i][w] != dp[i - 1][w]) {
                Activity picked = activities.get(i - 1);
                chosen.add(0, picked);
                accumulatedCost += picked.getCost();
                w -= weights[i];
            }
        }

        result.selectedActivities = chosen;
        result.totalCost = accumulatedCost;
        result.totalTravelValue = dp[n][maxWeight];
        result.remainingBudget = Math.max(0, budget - accumulatedCost);
        result.explanation = "Evaluated " + n + " activities across " + (maxWeight + 1)
                + " discrete budget states. Found optimal combination achieving "
                + result.totalTravelValue + " points for $" + String.format("%.2f", accumulatedCost)
                + " (Remaining: $" + String.format("%.2f", result.remainingBudget) + ").";

        // Generate sample slice of the DP table for UI presentation (e.g. up to 10 rows, up to 12 columns)
        int sampleCols = Math.min(12, maxWeight + 1);
        int colStep = Math.max(1, maxWeight / (sampleCols - 1));

        List<Integer> colHeaders = new ArrayList<>();
        for (int c = 0; c <= maxWeight; c += colStep) {
            colHeaders.add(c * scale);
            if (colHeaders.size() >= sampleCols) break;
        }
        if (!colHeaders.contains(maxWeight * scale)) {
            colHeaders.add(maxWeight * scale);
        }
        result.tableColHeaders = colHeaders;

        result.tableRowHeaders.add("None (Base)");
        for (Activity a : activities) {
            result.tableRowHeaders.add(a.getName() + " ($" + (int)a.getCost() + ")");
        }

        int[][] sampleSlice = new int[n + 1][colHeaders.size()];
        for (int i = 0; i <= n; i++) {
            for (int j = 0; j < colHeaders.size(); j++) {
                int colWeight = colHeaders.get(j) / scale;
                sampleSlice[i][j] = dp[i][Math.min(colWeight, maxWeight)];
            }
        }
        result.dpTableSample = sampleSlice;

        return result;
    }
}
