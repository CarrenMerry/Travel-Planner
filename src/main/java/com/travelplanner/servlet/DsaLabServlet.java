package com.travelplanner.servlet;

import com.travelplanner.ds.BinarySearchTree;
import com.travelplanner.ds.BinaryTree;
import com.travelplanner.ds.ExpressionEvaluator;
import com.travelplanner.model.Destination;
import com.travelplanner.service.DestinationService;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@WebServlet("/api/dsa-lab/*")
public class DsaLabServlet extends BaseServlet {

    private final DestinationService destinationService = DestinationService.getInstance();

    public static class EvalRequest {
        public String expression;
    }

    public static class BstRequest {
        public String action; // "search", "insert", "delete", "traversals"
        public String destinationName;
        public String destinationId;
    }

    public static class TreeRequest {
        public List<String> nodeLabels; // optional custom labels
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String pathInfo = req.getPathInfo();

        if (pathInfo != null && pathInfo.equalsIgnoreCase("/all-structures")) {
            Map<String, Object> summary = new HashMap<>();
            summary.put("destinationCount", destinationService.getAllDestinations().size());
            summary.put("bstSize", destinationService.getBst().size());
            summary.put("bstHeight", destinationService.getBst().height());
            summary.put("graphVertices", destinationService.getGraph().getVertexCount());
            summary.put("graphAllVertexIds", destinationService.getGraph().getAllVertexIds());
            sendJsonResponse(resp, HttpServletResponse.SC_OK, summary);
            return;
        }

        // Return BST current traversals
        BinarySearchTree<Destination> bst = destinationService.getBst();
        Map<String, Object> bstData = new HashMap<>();
        bstData.put("size", bst.size());
        bstData.put("height", bst.height());
        bstData.put("inorder", bst.inorderTraversal());
        bstData.put("preorder", bst.preorderTraversal());
        bstData.put("postorder", bst.postorderTraversal());
        sendJsonResponse(resp, HttpServletResponse.SC_OK, bstData);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String pathInfo = req.getPathInfo();

        if (pathInfo != null) {
            if (pathInfo.equalsIgnoreCase("/evaluate")) {
                handleEvaluation(req, resp);
                return;
            } else if (pathInfo.equalsIgnoreCase("/tree")) {
                handleTree(req, resp);
                return;
            } else if (pathInfo.equalsIgnoreCase("/bst")) {
                handleBst(req, resp);
                return;
            }
        }

        sendErrorResponse(resp, HttpServletResponse.SC_BAD_REQUEST, "Invalid DSA lab action.");
    }

    private void handleEvaluation(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        EvalRequest body = parseJsonBody(req, EvalRequest.class);
        String expr = (body != null && body.expression != null) ? body.expression : "(1500 * 0.8) + (250 / 2) - 100";

        ExpressionEvaluator.EvaluationResult result = ExpressionEvaluator.evaluate(expr);
        if (!result.isSuccess()) {
            sendErrorResponse(resp, HttpServletResponse.SC_BAD_REQUEST, result.getErrorMessage());
        } else {
            sendJsonResponse(resp, HttpServletResponse.SC_OK, result);
        }
    }

    private void handleTree(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        TreeRequest body = parseJsonBody(req, TreeRequest.class);

        BinaryTree<String> tree = new BinaryTree<>();
        if (body != null && body.nodeLabels != null && !body.nodeLabels.isEmpty()) {
            for (String label : body.nodeLabels) {
                tree.insertLevelOrder(label);
            }
        } else {
            // Default sample regional hierarchy
            tree.insertLevelOrder("Global Hub");
            tree.insertLevelOrder("Asia-Pacific");
            tree.insertLevelOrder("Europe-Americas");
            tree.insertLevelOrder("Tokyo Hub");
            tree.insertLevelOrder("Sydney Hub");
            tree.insertLevelOrder("Paris Hub");
            tree.insertLevelOrder("New York Hub");
        }

        Map<String, Object> res = new HashMap<>();
        res.put("height", tree.height());
        res.put("inorder", tree.inorderTraversal());
        res.put("preorder", tree.preorderTraversal());
        res.put("postorder", tree.postorderTraversal());
        res.put("explanationInorder", "Inorder (L -> Root -> R): Visited bottom-left up through subtrees.");
        res.put("explanationPreorder", "Preorder (Root -> L -> R): Root first, ideal for copying tree hierarchies.");
        res.put("explanationPostorder", "Postorder (L -> R -> Root): Children first, ideal for bottom-up cost aggregation.");
        sendJsonResponse(resp, HttpServletResponse.SC_OK, res);
    }

    private void handleBst(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        BstRequest body = parseJsonBody(req, BstRequest.class);
        if (body == null || body.action == null) {
            sendErrorResponse(resp, HttpServletResponse.SC_BAD_REQUEST, "Missing 'action' parameter.");
            return;
        }

        BinarySearchTree<Destination> bst = destinationService.getBst();

        if (body.action.equalsIgnoreCase("search")) {
            String name = body.destinationName != null ? body.destinationName : "";
            Destination probe = new Destination();
            probe.setName(name);

            BinarySearchTree.SearchResult<Destination> searchRes = bst.search(probe);
            Map<String, Object> res = new HashMap<>();
            res.put("found", searchRes.found);
            res.put("targetName", name);
            res.put("comparisons", searchRes.comparisons);
            res.put("searchPath", searchRes.searchPath);
            res.put("item", searchRes.foundItem);
            res.put("explanation", "BST Search compares the probe against each node: if smaller go left, if larger go right. Average O(log n).");
            sendJsonResponse(resp, HttpServletResponse.SC_OK, res);
        } else if (body.action.equalsIgnoreCase("traversals")) {
            Map<String, Object> res = new HashMap<>();
            res.put("size", bst.size());
            res.put("height", bst.height());
            res.put("inorderSorted", bst.inorderTraversal());
            res.put("preorder", bst.preorderTraversal());
            res.put("postorder", bst.postorderTraversal());
            sendJsonResponse(resp, HttpServletResponse.SC_OK, res);
        } else {
            sendErrorResponse(resp, HttpServletResponse.SC_BAD_REQUEST, "Unsupported BST action: " + body.action);
        }
    }
}
