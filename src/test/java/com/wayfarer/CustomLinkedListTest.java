package com.wayfarer;

import com.wayfarer.ds.CustomLinkedList;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class CustomLinkedListTest {

    @Test
    @DisplayName("Verify append, insert, remove, and ordered traversal in CustomLinkedList")
    void testBasicOperations() {
        CustomLinkedList<String> list = new CustomLinkedList<>();
        assertTrue(list.isEmpty());
        assertEquals(0, list.size());

        // Append operations O(1)
        list.add("Tokyo");
        list.add("Kyoto");
        list.add("Osaka");
        assertEquals(3, list.size());
        assertEquals("Tokyo", list.get(0));
        assertEquals("Kyoto", list.get(1));
        assertEquals("Osaka", list.get(2));

        // Insert at index 1
        list.insert(1, "Hakone");
        assertEquals(4, list.size());
        assertEquals("Tokyo", list.get(0));
        assertEquals("Hakone", list.get(1));
        assertEquals("Kyoto", list.get(2));
        assertEquals("Osaka", list.get(3));

        // Remove at index 2
        String removed = list.remove(2);
        assertEquals("Kyoto", removed);
        assertEquals(3, list.size());
        assertEquals("Osaka", list.get(2));

        // toList traversal
        List<String> array = list.toList();
        assertEquals(List.of("Tokyo", "Hakone", "Osaka"), array);
    }

    @Test
    @DisplayName("Verify drag-and-drop reordering in CustomLinkedList")
    void testReorder() {
        CustomLinkedList<String> list = new CustomLinkedList<>();
        list.add("Stop 1");
        list.add("Stop 2");
        list.add("Stop 3");
        list.add("Stop 4");

        // Move Stop 1 (index 0) to index 2
        list.reorder(0, 2);
        assertEquals(List.of("Stop 2", "Stop 3", "Stop 1", "Stop 4"), list.toList());

        // Move Stop 4 (index 3) to index 0
        list.reorder(3, 0);
        assertEquals(List.of("Stop 4", "Stop 2", "Stop 3", "Stop 1"), list.toList());
    }

    @Test
    @DisplayName("Verify moveUp and moveDown operations")
    void testMoveUpAndDown() {
        CustomLinkedList<String> list = new CustomLinkedList<>();
        list.add("Paris");
        list.add("Rome");
        list.add("Cairo");

        // Cannot move up first item
        assertFalse(list.moveUp(0));

        // Move Rome (index 1) up -> should become [Rome, Paris, Cairo]
        assertTrue(list.moveUp(1));
        assertEquals(List.of("Rome", "Paris", "Cairo"), list.toList());

        // Cannot move down last item
        assertFalse(list.moveDown(2));

        // Move Rome (index 0) down -> should become [Paris, Rome, Cairo]
        assertTrue(list.moveDown(0));
        assertEquals(List.of("Paris", "Rome", "Cairo"), list.toList());
    }

    @Test
    @DisplayName("Verify bounds checking and exceptions")
    void testIndexBounds() {
        CustomLinkedList<String> list = new CustomLinkedList<>();
        assertThrows(IndexOutOfBoundsException.class, () -> list.get(0));
        assertThrows(IndexOutOfBoundsException.class, () -> list.remove(0));
        assertThrows(IndexOutOfBoundsException.class, () -> list.insert(2, "Test"));
    }
}
