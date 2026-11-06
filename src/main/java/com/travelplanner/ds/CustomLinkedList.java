package com.travelplanner.ds;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 * Custom Generic Singly Linked List implementation.
 * Used for maintaining the ordered travel itinerary.
 *
 * @param <T> Element type
 */
public class CustomLinkedList<T> {

    public static class Node<E> {
        public E data;
        public Node<E> next;

        public Node(E data) {
            this.data = data;
            this.next = null;
        }
    }

    private Node<T> head;
    private int size;

    public CustomLinkedList() {
        this.head = null;
        this.size = 0;
    }

    /**
     * Inserts a new node at the beginning of the list.
     * Time Complexity: O(1)
     */
    public void insertAtBeginning(T data) {
        Node<T> newNode = new Node<>(data);
        newNode.next = head;
        head = newNode;
        size++;
    }

    /**
     * Inserts a new node at the end of the list.
     * Time Complexity: O(n)
     */
    public void insertAtEnd(T data) {
        Node<T> newNode = new Node<>(data);
        if (head == null) {
            head = newNode;
        } else {
            Node<T> current = head;
            while (current.next != null) {
                current = current.next;
            }
            current.next = newNode;
        }
        size++;
    }

    /**
     * Inserts a new node at a specified 0-based position.
     * If index is 0, inserts at beginning.
     * If index >= size, inserts at end.
     * Time Complexity: O(n)
     */
    public void insertAtPosition(int index, T data) {
        if (index < 0) {
            throw new IndexOutOfBoundsException("Negative index: " + index);
        }
        if (index == 0) {
            insertAtBeginning(data);
            return;
        }
        if (index >= size) {
            insertAtEnd(data);
            return;
        }

        Node<T> newNode = new Node<>(data);
        Node<T> current = head;
        for (int i = 0; i < index - 1; i++) {
            current = current.next;
        }
        newNode.next = current.next;
        current.next = newNode;
        size++;
    }

    /**
     * Deletes the node at a specified 0-based position.
     * Time Complexity: O(n)
     */
    public T deleteAtPosition(int index) {
        if (index < 0 || index >= size || head == null) {
            throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + size);
        }

        T removedData;
        if (index == 0) {
            removedData = head.data;
            head = head.next;
        } else {
            Node<T> current = head;
            for (int i = 0; i < index - 1; i++) {
                current = current.next;
            }
            removedData = current.next.data;
            current.next = current.next.next;
        }
        size--;
        return removedData;
    }

    /**
     * Deletes a node matching an ID extracted via a key mapper.
     * Time Complexity: O(n)
     */
    public boolean deleteById(String id, Function<T, String> keyExtractor) {
        if (head == null || id == null) {
            return false;
        }

        if (id.equalsIgnoreCase(keyExtractor.apply(head.data))) {
            head = head.next;
            size--;
            return true;
        }

        Node<T> current = head;
        while (current.next != null) {
            if (id.equalsIgnoreCase(keyExtractor.apply(current.next.data))) {
                current.next = current.next.next;
                size--;
                return true;
            }
            current = current.next;
        }
        return false;
    }

    /**
     * Retrieves an element at a given 0-based index.
     * Time Complexity: O(n)
     */
    public T get(int index) {
        if (index < 0 || index >= size) {
            throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + size);
        }
        Node<T> current = head;
        for (int i = 0; i < index; i++) {
            current = current.next;
        }
        return current.data;
    }

    /**
     * Searches for an element matching an ID extractor.
     * Time Complexity: O(n)
     */
    public T search(String id, Function<T, String> keyExtractor) {
        if (head == null || id == null) return null;
        Node<T> current = head;
        while (current != null) {
            if (id.equalsIgnoreCase(keyExtractor.apply(current.data))) {
                return current.data;
            }
            current = current.next;
        }
        return null;
    }

    /**
     * Finds the 0-based index of an element matching the ID.
     */
    public int indexOf(String id, Function<T, String> keyExtractor) {
        if (head == null || id == null) return -1;
        Node<T> current = head;
        int idx = 0;
        while (current != null) {
            if (id.equalsIgnoreCase(keyExtractor.apply(current.data))) {
                return idx;
            }
            current = current.next;
            idx++;
        }
        return -1;
    }

    /**
     * Moves an item from fromIndex to toIndex.
     */
    public void move(int fromIndex, int toIndex) {
        if (fromIndex == toIndex) return;
        T item = deleteAtPosition(fromIndex);
        insertAtPosition(toIndex, item);
    }

    /**
     * Traverses the linked list and converts to a Java List for JSON serialization and rendering.
     * Time Complexity: O(n)
     */
    public List<T> toList() {
        List<T> list = new ArrayList<>();
        Node<T> current = head;
        while (current != null) {
            list.add(current.data);
            current = current.next;
        }
        return list;
    }

    public void clear() {
        head = null;
        size = 0;
    }

    public int size() {
        return size;
    }

    public boolean isEmpty() {
        return size == 0;
    }

    public Node<T> getHead() {
        return head;
    }
}
