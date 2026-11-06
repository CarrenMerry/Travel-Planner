package com.travelplanner;

import com.travelplanner.algorithms.*;
import com.travelplanner.ds.*;
import com.travelplanner.model.Activity;
import com.travelplanner.model.Destination;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class DsaCoreTest {

    private Destination dTokyo;
    private Destination dParis;
    private Destination dRome;

    @BeforeEach
    void setUp() {
        dTokyo = new Destination("D1", "Tokyo", "Japan", "City of neon", 1400.0, 5.0, 4.9, "tokyo.jpg");
        dParis = new Destination("D2", "Paris", "France", "City of light", 1600.0, 4.0, 4.8, "paris.jpg");
        dRome = new Destination("D3", "Rome", "Italy", "Eternal city", 1200.0, 3.0, 4.7, "rome.jpg");
    }

    @Test
    @DisplayName("Phase 2: Custom Singly Linked List operations")
    void testLinkedList() {
        CustomLinkedList<Destination> list = new CustomLinkedList<>();
        assertTrue(list.isEmpty());
        assertEquals(0, list.size());

        list.insertAtEnd(dTokyo);
        list.insertAtBeginning(dParis);
        list.insertAtPosition(1, dRome);

        assertEquals(3, list.size());
        assertEquals("Paris", list.get(0).getName());
        assertEquals("Rome", list.get(1).getName());
        assertEquals("Tokyo", list.get(2).getName());

        // Test delete by ID
        boolean deleted = list.deleteById("D3", Destination::getId);
        assertTrue(deleted);
        assertEquals(2, list.size());
        assertEquals("Tokyo", list.get(1).getName());

        // Test move / reorder
        list.move(0, 1);
        assertEquals("Tokyo", list.get(0).getName());
        assertEquals("Paris", list.get(1).getName());
    }

    @Test
    @DisplayName("Phase 2: Custom Stack operations (LIFO)")
    void testCustomStack() {
        CustomStack<String> stack = new CustomStack<>();
        assertTrue(stack.isEmpty());

        stack.push("Action 1");
        stack.push("Action 2");
        stack.push("Action 3");

        assertEquals(3, stack.size());
        assertEquals("Action 3", stack.peek());
        assertEquals("Action 3", stack.pop());
        assertEquals("Action 2", stack.pop());
        assertEquals(1, stack.size());
        assertFalse(stack.isEmpty());
    }

    @Test
    @DisplayName("Phase 2: Custom Queue operations (FIFO)")
    void testCustomQueue() {
        CustomQueue<String> queue = new CustomQueue<>();
        assertTrue(queue.isEmpty());

        queue.enqueue("Req 1");
        queue.enqueue("Req 2");
        queue.enqueue("Req 3");

        assertEquals(3, queue.size());
        assertEquals("Req 1", queue.peek());
        assertEquals("Req 1", queue.dequeue());
        assertEquals("Req 2", queue.dequeue());
        assertEquals(1, queue.size());
        assertEquals("Req 3", queue.peek());
    }

    @Test
    @DisplayName("Phase 2: Stack-based arithmetic expression evaluator")
    void testExpressionEvaluator() {
        ExpressionEvaluator.EvaluationResult res1 = ExpressionEvaluator.evaluate("(1500 * 0.8) + (250 / 2) - 100");
        assertTrue(res1.isSuccess());
        // 1200 + 125 - 100 = 1225
        assertEquals(1225.0, res1.getResult(), 1e-6);
        assertFalse(res1.getTraces().isEmpty());

        // Division by zero
        ExpressionEvaluator.EvaluationResult res2 = ExpressionEvaluator.evaluate("100 / 0");
        assertFalse(res2.isSuccess());
        assertTrue(res2.getErrorMessage().contains("Division by zero"));

        // Mismatched parentheses
        ExpressionEvaluator.EvaluationResult res3 = ExpressionEvaluator.evaluate("(100 + 50");
        assertFalse(res3.isSuccess());
    }

    @Test
    @DisplayName("Phase 3: Custom Binary Search Tree insertion and search")
    void testBst() {
        BinarySearchTree<Destination> bst = new BinarySearchTree<>();
        bst.insert(dParis);
        bst.insert(dTokyo);
        bst.insert(dRome);

        assertEquals(3, bst.size());

        // Search
        Destination probe = new Destination();
        probe.setName("Tokyo");
        BinarySearchTree.SearchResult<Destination> res = bst.search(probe);
        assertTrue(res.found);
        assertEquals("D1", res.foundItem.getId());
        assertTrue(res.comparisons > 0);

        // Delete
        boolean deleted = bst.delete(probe);
        assertTrue(deleted);
        assertEquals(2, bst.size());

        BinarySearchTree.SearchResult<Destination> resAfter = bst.search(probe);
        assertFalse(resAfter.found);
    }

    @Test
    @DisplayName("Phase 3: Custom Graph with Adjacency List, BFS, DFS, and Dijkstra")
    void testGraph() {
        CustomGraph graph = new CustomGraph();
        graph.addVertex(dTokyo);
        graph.addVertex(dParis);
        graph.addVertex(dRome);

        graph.addEdge("D1", "D2", 5000.0, 400.0, true);
        graph.addEdge("D2", "D3", 1100.0, 100.0, true);

        // BFS from Tokyo
        CustomGraph.TraversalResult bfs = graph.bfs("D1");
        assertEquals("BFS", bfs.algorithm);
        assertTrue(bfs.traversalOrder.contains("D1"));
        assertTrue(bfs.traversalOrder.contains("D2"));
        assertTrue(bfs.traversalOrder.contains("D3"));

        // DFS from Tokyo
        CustomGraph.TraversalResult dfs = graph.dfs("D1");
        assertEquals("DFS", dfs.algorithm);
        assertTrue(dfs.traversalOrder.contains("D1"));

        // Dijkstra shortest path Tokyo to Rome: D1 -> D2 -> D3 (total dist 6100.0)
        CustomGraph.DijkstraResult dijkstra = graph.dijkstra("D1", "D3");
        assertTrue(dijkstra.pathFound);
        assertEquals(6100.0, dijkstra.totalDistance, 1e-6);
        assertEquals(List.of("D1", "D2", "D3"), dijkstra.path);
    }

    @Test
    @DisplayName("Phase 4: Searching and Sorting Algorithms")
    void testSearchingAndSorting() {
        List<Destination> list = new ArrayList<>(List.of(dTokyo, dParis, dRome));

        // Linear Search
        SearchingAlgorithms.SearchResult linRes = SearchingAlgorithms.linearSearch(list, "Rome");
        assertTrue(linRes.found);
        assertEquals("D3", linRes.destination.getId());

        // Merge Sort by Cost Ascending
        SortingAlgorithms.SortResult mergeRes = SortingAlgorithms.mergeSort(list, "cost", true);
        assertEquals("Rome", mergeRes.sortedList.get(0).getName()); // 1200
        assertEquals("Tokyo", mergeRes.sortedList.get(1).getName()); // 1400
        assertEquals("Paris", mergeRes.sortedList.get(2).getName()); // 1600

        // Quick Sort by Rating Descending
        SortingAlgorithms.SortResult quickRes = SortingAlgorithms.quickSort(list, "rating", false);
        assertEquals("Tokyo", quickRes.sortedList.get(0).getName()); // 4.9
        assertEquals("Paris", quickRes.sortedList.get(1).getName()); // 4.8
        assertEquals("Rome", quickRes.sortedList.get(2).getName());  // 4.7
    }

    @Test
    @DisplayName("Phase 5: Greedy, Dynamic Programming, and Backtracking")
    void testOptimizationAlgorithms() {
        List<Destination> list = new ArrayList<>(List.of(dTokyo, dParis, dRome));

        // 1. Greedy
        GreedyOptimizer.GreedyResult greedyRes = GreedyOptimizer.optimize(list, "D1", 3);
        assertNotNull(greedyRes);
        assertFalse(greedyRes.selectedRoute.isEmpty());
        assertEquals("D1", greedyRes.selectedRoute.get(0).getId());

        // 2. Dynamic Programming 0/1 Knapsack
        List<Activity> activities = new ArrayList<>();
        activities.add(new Activity("A1", "Museum", 100.0, 80));
        activities.add(new Activity("A2", "Skyline", 150.0, 95));
        activities.add(new Activity("A3", "Walking Tour", 50.0, 60));
        activities.add(new Activity("A4", "Luxury Dinner", 300.0, 70));

        // Budget $200: optimal choice is A2 ($150, 95 pts) + A3 ($50, 60 pts) = $200, 155 pts
        DynamicProgrammingPlanner.DpResult dpRes = DynamicProgrammingPlanner.optimizeActivities(activities, 200.0);
        assertTrue(dpRes.totalTravelValue >= 150);
        assertTrue(dpRes.totalCost <= 200.0);
        assertNotNull(dpRes.dpTableSample);

        // 3. Backtracking with Constraints
        // maxBudget $3000, maxDuration 8 days, minStops 2
        BacktrackingPlanner.BacktrackingResult btRes = BacktrackingPlanner.generateItineraries(list, 3000.0, 8.0, 2);
        assertNotNull(btRes);
        assertFalse(btRes.validItineraries.isEmpty());
        for (BacktrackingPlanner.ValidItinerary it : btRes.validItineraries) {
            assertTrue(it.totalCost <= 3000.0);
            assertTrue(it.totalDuration <= 8.0);
            assertTrue(it.destinations.size() >= 2);
        }
    }
}
