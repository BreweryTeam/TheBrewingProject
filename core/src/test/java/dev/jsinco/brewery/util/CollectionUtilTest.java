package dev.jsinco.brewery.util;

import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.SequencedSet;
import java.util.SortedSet;
import java.util.TreeSet;

import static org.junit.jupiter.api.Assertions.*;

public class CollectionUtilTest {

    @Test
    void sequenceSetOf() {
        SequencedSet<String> set = CollectionUtil.sequencedSetOf("value");
        assertEquals(1, set.size());
        assertEquals("value", set.getFirst());
    }

    @Test
    void isEqualWithOrdering_equals() {
        SequencedSet<String> set1 = CollectionUtil.sequencedSetOf("one", "two", "three");
        SequencedSet<String> set2 = CollectionUtil.sequencedSetOf("one", "two", "three");
        assertTrue(CollectionUtil.isEqualWithOrdering(set1, set2));
        assertTrue(CollectionUtil.isEqualWithOrdering(set2, set1));
    }

    @Test
    void isEqualWithOrdering_empty() {
        SequencedSet<String> set1 = Collections.emptySortedSet();
        SequencedSet<String> set2 = Collections.emptySortedSet();
        assertTrue(CollectionUtil.isEqualWithOrdering(set1, set2));
        assertTrue(CollectionUtil.isEqualWithOrdering(set2, set1));
    }

    @Test
    void isEqualWithOrdering_sorted() {
        SortedSet<String> set1 = new TreeSet<>();
        set1.add("one");
        set1.add("two");
        set1.add("three");
        SortedSet<String> set2 = new TreeSet<>();
        set2.add("three");
        set2.add("two");
        set2.add("one");
        assertTrue(CollectionUtil.isEqualWithOrdering(set1, set2));
        assertTrue(CollectionUtil.isEqualWithOrdering(set2, set1));
    }

    @Test
    void isEqualWithOrdering_notEquals() {
        SequencedSet<String> set1 = CollectionUtil.sequencedSetOf("one", "two", "three");
        SequencedSet<String> set2 = CollectionUtil.sequencedSetOf("one", "two", "four");
        assertFalse(CollectionUtil.isEqualWithOrdering(set1, set2));
        assertFalse(CollectionUtil.isEqualWithOrdering(set2, set1));
    }

    @Test
    void isEqualWithOrdering_wrongOrder() {
        SequencedSet<String> set1 = CollectionUtil.sequencedSetOf("one", "two", "three");
        SequencedSet<String> set2 = CollectionUtil.sequencedSetOf("three", "two", "one");
        assertFalse(CollectionUtil.isEqualWithOrdering(set1, set2));
        assertFalse(CollectionUtil.isEqualWithOrdering(set2, set1));
    }

    @Test
    void isEqualWithOrdering_differentLength() {
        SequencedSet<String> set1 = CollectionUtil.sequencedSetOf("one", "two", "three");
        SequencedSet<String> set2 = CollectionUtil.sequencedSetOf("one", "two");
        assertFalse(CollectionUtil.isEqualWithOrdering(set1, set2));
        assertFalse(CollectionUtil.isEqualWithOrdering(set2, set1));
    }

}
