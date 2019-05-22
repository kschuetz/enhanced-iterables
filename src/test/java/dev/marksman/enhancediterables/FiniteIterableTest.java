package dev.marksman.enhancediterables;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static dev.marksman.enhancediterables.EnhancedIterables.finiteIterable;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class FiniteIterableTest {

    @Nested
    @DisplayName("foldLeft")
    class FoldLeft {

        @Test
        void throwsOnNullOperator() {
            FiniteIterable<Integer> ints = finiteIterable(asList(1, 2, 3));
            assertThrows(NullPointerException.class, () -> ints.foldLeft(null, 0));
        }

        @Test
        void onEmpty() {
            FiniteIterable<Integer> ints = finiteIterable(emptyList());
            assertEquals(999, ints.foldLeft(Integer::sum, 999));
        }

        @Test
        void onSize5() {
            FiniteIterable<Integer> ints = finiteIterable(asList(1, 2, 3, 4, 5));
            assertEquals(25, ints.foldLeft(Integer::sum, 10));
        }

    }

}
