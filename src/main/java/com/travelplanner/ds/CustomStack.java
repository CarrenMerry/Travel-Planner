package com.travelplanner.ds;

import java.util.ArrayList;
import java.util.EmptyStackException;
import java.util.List;

/**
 * Custom Stack implemented using a singly linked node structure.
 * Used for Undo functionality and Expression Evaluation.
 *
 * @param <T> Element type
 */
public class CustomStack<T> {

    public static class Node<E> {
        public E data;
        public Node<E> next;

        public Node(E data, Node<E> next) {
            this.data = data;
            this.next = next;
        }
    }

    private Node<T> top;
    private int size;

    public CustomStack() {
        this.top = null;
        this.size = 0;
    }

    /**
     * Pushes an item onto the top of the stack.
     * Time Complexity: O(1)
     */
    public void push(T item) {
        top = new Node<>(item, top);
        size++;
    }

    /**
     * Removes and returns the item from the top of the stack.
     * Time Complexity: O(1)
     */
    public T pop() {
        if (isEmpty()) {
            throw new EmptyStackException();
        }
        T data = top.data;
        top = top.next;
        size--;
        return data;
    }

    /**
     * Returns the item on top of the stack without removing it.
     * Time Complexity: O(1)
     */
    public T peek() {
        if (isEmpty()) {
            throw new EmptyStackException();
        }
        return top.data;
    }

    /**
     * Checks if the stack is empty.
     * Time Complexity: O(1)
     */
    public boolean isEmpty() {
        return top == null;
    }

    /**
     * Returns the number of items in the stack.
     * Time Complexity: O(1)
     */
    public int size() {
        return size;
    }

    /**
     * Clears all items in the stack.
     */
    public void clear() {
        top = null;
        size = 0;
    }

    /**
     * Converts stack items from top to bottom into a List for inspection.
     * Time Complexity: O(n)
     */
    public List<T> toList() {
        List<T> list = new ArrayList<>();
        Node<T> current = top;
        while (current != null) {
            list.add(current.data);
            current = current.next;
        }
        return list;
    }
}
