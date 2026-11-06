package com.travelplanner.ds;

import java.util.ArrayList;
import java.util.List;

/**
 * Custom Binary Search Tree (BST) implementation.
 * Generic over Comparable types, specifically optimized for Destination indexing by name or key.
 *
 * @param <T> Element type implementing Comparable
 */
public class BinarySearchTree<T extends Comparable<T>> {

    public static class Node<E> {
        public E data;
        public Node<E> left;
        public Node<E> right;

        public Node(E data) {
            this.data = data;
            this.left = null;
            this.right = null;
        }
    }

    public static class SearchResult<E> {
        public E foundItem;
        public int comparisons;
        public List<E> searchPath = new ArrayList<>();
        public boolean found;

        public SearchResult(E foundItem, int comparisons, List<E> searchPath, boolean found) {
            this.foundItem = foundItem;
            this.comparisons = comparisons;
            this.searchPath = searchPath;
            this.found = found;
        }
    }

    private Node<T> root;
    private int size;

    public BinarySearchTree() {
        this.root = null;
        this.size = 0;
    }

    /**
     * Inserts an element into the BST.
     * Average Time Complexity: O(log n)
     * Worst Time Complexity: O(n) if unbalanced
     */
    public void insert(T data) {
        if (data == null) return;
        root = insertRec(root, data);
    }

    private Node<T> insertRec(Node<T> current, T data) {
        if (current == null) {
            size++;
            return new Node<>(data);
        }

        int cmp = data.compareTo(current.data);
        if (cmp < 0) {
            current.left = insertRec(current.left, data);
        } else if (cmp > 0) {
            current.right = insertRec(current.right, data);
        }
        // Duplicates ignored or replaced
        return current;
    }

    /**
     * Searches for an element matching a probe key.
     * Tracks comparisons and the visited path for educational inspection.
     * Average Time: O(log n), Worst Time: O(n)
     */
    public SearchResult<T> search(T key) {
        List<T> path = new ArrayList<>();
        int comparisons = 0;
        Node<T> current = root;

        while (current != null) {
            comparisons++;
            path.add(current.data);
            int cmp = key.compareTo(current.data);
            if (cmp == 0) {
                return new SearchResult<>(current.data, comparisons, path, true);
            } else if (cmp < 0) {
                current = current.left;
            } else {
                current = current.right;
            }
        }
        return new SearchResult<>(null, comparisons, path, false);
    }

    /**
     * Deletes an element from the BST.
     * Average Time Complexity: O(log n), Worst Time Complexity: O(n)
     */
    public boolean delete(T key) {
        int initialSize = size;
        root = deleteRec(root, key);
        return size < initialSize;
    }

    private Node<T> deleteRec(Node<T> current, T key) {
        if (current == null) {
            return null;
        }

        int cmp = key.compareTo(current.data);
        if (cmp < 0) {
            current.left = deleteRec(current.left, key);
        } else if (cmp > 0) {
            current.right = deleteRec(current.right, key);
        } else {
            // Node to delete found
            size--;
            // Case 1 & 2: Node with 0 or 1 child
            if (current.left == null) {
                return current.right;
            } else if (current.right == null) {
                return current.left;
            }

            // Case 3: Node with 2 children - get inorder successor (min in right subtree)
            current.data = findMin(current.right);
            // Delete the inorder successor
            current.right = deleteRec(current.right, current.data);
            size++; // Compensate because recursive deleteRec will decrement size
        }
        return current;
    }

    private T findMin(Node<T> node) {
        T min = node.data;
        while (node.left != null) {
            min = node.left.data;
            node = node.left;
        }
        return min;
    }

    /**
     * Inorder Traversal gives elements in sorted order.
     */
    public List<T> inorderTraversal() {
        List<T> result = new ArrayList<>();
        inorderRec(root, result);
        return result;
    }

    private void inorderRec(Node<T> current, List<T> result) {
        if (current != null) {
            inorderRec(current.left, result);
            result.add(current.data);
            inorderRec(current.right, result);
        }
    }

    public List<T> preorderTraversal() {
        List<T> result = new ArrayList<>();
        preorderRec(root, result);
        return result;
    }

    private void preorderRec(Node<T> current, List<T> result) {
        if (current != null) {
            result.add(current.data);
            preorderRec(current.left, result);
            preorderRec(current.right, result);
        }
    }

    public List<T> postorderTraversal() {
        List<T> result = new ArrayList<>();
        postorderRec(root, result);
        return result;
    }

    private void postorderRec(Node<T> current, List<T> result) {
        if (current != null) {
            postorderRec(current.left, result);
            postorderRec(current.right, result);
            result.add(current.data);
        }
    }

    public int height() {
        return heightRec(root);
    }

    private int heightRec(Node<T> current) {
        if (current == null) return 0;
        return 1 + Math.max(heightRec(current.left), heightRec(current.right));
    }

    public int size() {
        return size;
    }

    public boolean isEmpty() {
        return size == 0;
    }

    public Node<T> getRoot() {
        return root;
    }

    public void clear() {
        root = null;
        size = 0;
    }
}
