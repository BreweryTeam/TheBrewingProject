package dev.jsinco.brewery.util;

import java.util.*;

public class CollectionUtil {
    private CollectionUtil() {}

    /**
     * Returns an unmodifiable sequenced set containing an arbitrary number of elements.
     * @param elements The elements to be contained in the sequenced set.
     * @return A new unmodifiable sequenced set.
     * @param <E> Set element type
     */
    @SafeVarargs
    public static <E> SequencedSet<E> sequencedSetOf(E... elements) {
        return Collections.unmodifiableSequencedSet(new LinkedHashSet<>(Arrays.asList(elements)));
    }

    /**
     * Checks if two sequenced sets are equal, and that their ordering is the same.
     * @param set1 The first set
     * @param set2 The second set
     * @return True if both sets are the same size, and element 1 of both sets are equal,
     *         element 2 of both sets are equal, and so on
     * @param <E> Set element type
     */
    public static <E> boolean isEqualWithOrdering(SequencedSet<E> set1, SequencedSet<E> set2) {
        if (set1.size() != set2.size()) {
            return false;
        }
        Iterator<E> iter1 = set1.iterator();
        Iterator<E> iter2 = set2.iterator();
        while (iter1.hasNext() && iter2.hasNext()) {
            if (!Objects.equals(iter1.next(), iter2.next())) {
                return false;
            }
        }
        return true;
    }

}