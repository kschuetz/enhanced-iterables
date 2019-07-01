package dev.marksman.enhancediterables;

import org.junit.jupiter.api.Test;

import java.util.Iterator;
import java.util.NoSuchElementException;

import static dev.marksman.enhancediterables.EnhancedIterable.emptyEnhancedIterable;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.emptyIterable;
import static org.junit.jupiter.api.Assertions.*;

class EmptyEnhancedIterableTest {

    @Test
    void isEmpty() {
        assertTrue(emptyEnhancedIterable().isEmpty());
    }

    @Test
    void iteration() {
        assertThat(emptyEnhancedIterable(), emptyIterable());
    }

    @Test
    void iteratorHasNextReturnsFalse() {
        EnhancedIterable<String> subject = emptyEnhancedIterable();
        assertFalse(subject.iterator().hasNext());
    }

    @Test
    void iteratorHasNextCanBeCalledMultipleTimes() {
        EnhancedIterable<String> subject = emptyEnhancedIterable();
        assertFalse(subject.iterator().hasNext());
        assertFalse(subject.iterator().hasNext());
    }

    @Test
    void iteratorNextThrowsIfNothingRemains() {
        EnhancedIterable<String> subject = emptyEnhancedIterable();
        Iterator<String> iterator = subject.iterator();
        assertThrows(NoSuchElementException.class, iterator::next);
    }

    @Test
    void iteratorThrowsIfRemoveIsCalled() {
        EnhancedIterable<String> subject = emptyEnhancedIterable();
        Iterator<String> iterator = subject.iterator();
        assertThrows(UnsupportedOperationException.class, iterator::remove);
    }

}
