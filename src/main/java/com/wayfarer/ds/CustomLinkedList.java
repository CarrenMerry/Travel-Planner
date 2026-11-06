package com.wayfarer.ds;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * Wayfarer — Custom Singly Linked List Data Structure
 *
 * Academic Data Structure Requirement 1:
 * Manages the ordered sequence of stops in an itinerary with deterministic positioning.
 * Supports:
 *  - Insert at end: O(1) using tail pointer
 *  - Insert at index: O(N) traversal
 *  - Remove at index: O(N) traversal
 *  - Reorder stops: O(N) repositioning
 *  - Traverse in order: O(N) via Iterator
 */
public class CustomLinkedList<E> implements Iterable<E> {

    public static class Node<E> {
        public E data;
        public Node<E> next;

        public Node(E data) {
            this.data = data;
            this.next = null;
        }
    }

    private Node<E> head;
    private Node<E> tail;
    private int size;

    public CustomLinkedList() {
        this.head = null;
        this.tail = null;
        this.size = 0;
    }

    public int size() {
        return size;
    }

    public boolean isEmpty() {
        return size == 0;
    }

    /**
     * Appends an element to the end of the list in O(1) time.
     */
    public void add(E element) {
        Node<E> newNode = new Node<>(element);
        if (head == null) {
            head = newNode;
            tail = newNode;
        } else {
            tail.next = newNode;
            tail = newNode;
        }
        size++;
    }

    /**
     * Inserts an element at a specified index in O(N) time.
     */
    public void insert(int index, E element) {
        if (index < 0 || index > size) {
            throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + size);
        }

        if (index == size) {
            add(element);
            return;
        }

        Node<E> newNode = new Node<>(element);
        if (index == 0) {
            newNode.next = head;
            head = newNode;
            if (tail == null) tail = newNode;
        } else {
            Node<E> prev = getNode(index - 1);
            newNode.next = prev.next;
            prev.next = newNode;
        }
        size++;
    }

    /**
     * Removes the element at the specified index in O(N) time.
     */
    public E remove(int index) {
        if (index < 0 || index >= size) {
            throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + size);
        }

        E removedData;
        if (index == 0) {
            removedData = head.data;
            head = head.next;
            if (head == null) {
                tail = null;
            }
        } else {
            Node<E> prev = getNode(index - 1);
            Node<E> target = prev.next;
            removedData = target.data;
            prev.next = target.next;
            if (target == tail) {
                tail = prev;
            }
        }
        size--;
        return removedData;
    }

    /**
     * Retrieves element at specified index in O(N) time.
     */
    public E get(int index) {
        return getNode(index).data;
    }

    /**
     * Updates element at specified index.
     */
    public void set(int index, E element) {
        getNode(index).data = element;
    }

    /**
     * Reorders an element from one index position to another.
     */
    public void reorder(int fromIndex, int toIndex) {
        if (fromIndex < 0 || fromIndex >= size || toIndex < 0 || toIndex >= size) {
            throw new IndexOutOfBoundsException("Invalid reorder indices: from=" + fromIndex + ", to=" + toIndex);
        }
        if (fromIndex == toIndex) return;

        E item = remove(fromIndex);
        insert(toIndex, item);
    }

    /**
     * Moves a node up one position (swapping with predecessor) in O(N) time.
     * @return true if moved, false if already at top (index 0)
     */
    public boolean moveUp(int index) {
        if (index <= 0 || index >= size) return false;
        reorder(index, index - 1);
        return true;
    }

    /**
     * Moves a node down one position (swapping with successor) in O(N) time.
     * @return true if moved, false if already at bottom (index size - 1)
     */
    public boolean moveDown(int index) {
        if (index < 0 || index >= size - 1) return false;
        reorder(index, index + 1);
        return true;
    }

    private Node<E> getNode(int index) {
        if (index < 0 || index >= size) {
            throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + size);
        }
        Node<E> current = head;
        for (int i = 0; i < index; i++) {
            current = current.next;
        }
        return current;
    }

    public void clear() {
        head = null;
        tail = null;
        size = 0;
    }

    public List<E> toList() {
        List<E> list = new ArrayList<>(size);
        Node<E> current = head;
        while (current != null) {
            list.add(current.data);
            current = current.next;
        }
        return list;
    }

    @Override
    public Iterator<E> iterator() {
        return new Iterator<E>() {
            private Node<E> curr = head;

            @Override
            public boolean hasNext() {
                return curr != null;
            }

            @Override
            public E next() {
                if (curr == null) throw new NoSuchElementException();
                E data = curr.data;
                curr = curr.next;
                return data;
            }
        };
    }
}
