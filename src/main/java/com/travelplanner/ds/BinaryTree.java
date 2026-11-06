package com.travelplanner.ds;

import java.util.ArrayList;
import java.util.List;

/**
 * Custom Binary Tree implementation.
 * Used for demonstrating hierarchical travel regions and tree traversals.
 *
 * @param <T> Element type
 */
public class BinaryTree<T> {

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

    private Node<T> root;

    public BinaryTree() {
        this.root = null;
    }

    public BinaryTree(Node<T> root) {
        this.root = root;
    }

    public Node<T> getRoot() {
        return root;
    }

    public void setRoot(Node<T> root) {
        this.root = root;
    }

    /**
     * Inorder Traversal: Left -> Root -> Right
     * Time Complexity: O(n), Space Complexity: O(h)
     */
    public List<T> inorderTraversal() {
        List<T> result = new ArrayList<>();
        inorderHelper(root, result);
        return result;
    }

    private void inorderHelper(Node<T> node, List<T> result) {
        if (node != null) {
            inorderHelper(node.left, result);
            result.add(node.data);
            inorderHelper(node.right, result);
        }
    }

    /**
     * Preorder Traversal: Root -> Left -> Right
     * Time Complexity: O(n), Space Complexity: O(h)
     */
    public List<T> preorderTraversal() {
        List<T> result = new ArrayList<>();
        preorderHelper(root, result);
        return result;
    }

    private void preorderHelper(Node<T> node, List<T> result) {
        if (node != null) {
            result.add(node.data);
            preorderHelper(node.left, result);
            preorderHelper(node.right, result);
        }
    }

    /**
     * Postorder Traversal: Left -> Right -> Root
     * Time Complexity: O(n), Space Complexity: O(h)
     */
    public List<T> postorderTraversal() {
        List<T> result = new ArrayList<>();
        postorderHelper(root, result);
        return result;
    }

    private void postorderHelper(Node<T> node, List<T> result) {
        if (node != null) {
            postorderHelper(node.left, result);
            postorderHelper(node.right, result);
            result.add(node.data);
        }
    }

    /**
     * Inserts in level-order for general binary tree.
     */
    public void insertLevelOrder(T data) {
        Node<T> newNode = new Node<>(data);
        if (root == null) {
            root = newNode;
            return;
        }

        CustomQueue<Node<T>> queue = new CustomQueue<>();
        queue.enqueue(root);

        while (!queue.isEmpty()) {
            Node<T> current = queue.dequeue();
            if (current.left == null) {
                current.left = newNode;
                break;
            } else {
                queue.enqueue(current.left);
            }

            if (current.right == null) {
                current.right = newNode;
                break;
            } else {
                queue.enqueue(current.right);
            }
        }
    }

    public int height() {
        return heightHelper(root);
    }

    private int heightHelper(Node<T> node) {
        if (node == null) return 0;
        return 1 + Math.max(heightHelper(node.left), heightHelper(node.right));
    }
}
