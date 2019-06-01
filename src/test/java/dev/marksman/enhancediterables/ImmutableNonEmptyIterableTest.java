package dev.marksman.enhancediterables;

import com.jnape.palatable.lambda.functions.Fn1;
import com.jnape.palatable.lambda.functions.builtin.fn1.Repeat;
import com.jnape.palatable.lambda.functions.builtin.fn2.LT;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.NoSuchElementException;

import static com.jnape.palatable.lambda.adt.Maybe.just;
import static com.jnape.palatable.lambda.adt.Maybe.nothing;
import static com.jnape.palatable.lambda.adt.hlist.HList.tuple;
import static com.jnape.palatable.lambda.functions.builtin.fn1.Constantly.constantly;
import static com.jnape.palatable.lambda.functions.builtin.fn1.Id.id;
import static com.jnape.palatable.lambda.functions.builtin.fn2.Tupler2.tupler;
import static dev.marksman.enhancediterables.EnhancedIterables.immutableFiniteIterable;
import static dev.marksman.enhancediterables.FiniteIterable.finiteIterable;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsEmptyIterable.emptyIterable;
import static org.hamcrest.collection.IsIterableContainingInOrder.contains;
import static org.junit.jupiter.api.Assertions.*;
import static testsupport.IterablesContainSameElements.iterablesContainSameElements;
import static testsupport.IterablesContainSameElements.maybeIterablesContainSameElements;

class ImmutableNonEmptyIterableTest {

    @Test
    void singletonHead() {
        NonEmptyIterable<Integer> subject = immutableNonEmptyIterable(1, emptyList());
        assertEquals(1, subject.head());
    }

    @Test
    void singletonTail() {
        NonEmptyIterable<Integer> subject = immutableNonEmptyIterable(1, emptyList());
        assertThat(subject.tail(), emptyIterable());
    }

    @Test
    void singletonIteration() {
        NonEmptyIterable<Integer> subject = immutableNonEmptyIterable(1, emptyList());
        assertThat(subject, contains(1));
    }

    @Test
    void multipleHead() {
        NonEmptyIterable<Integer> subject = immutableNonEmptyIterable(1, asList(2, 3));
        assertEquals(1, subject.head());
    }

    @Test
    void multipleTail() {
        NonEmptyIterable<Integer> subject = immutableNonEmptyIterable(1, asList(2, 3));
        assertThat(subject.tail(), contains(2, 3));
    }

    @Test
    void multipleIteration() {
        NonEmptyIterable<Integer> subject = immutableNonEmptyIterable(1, asList(2, 3, 4, 5, 6));
        assertThat(subject, contains(1, 2, 3, 4, 5, 6));
    }

    @Test
    void iteratorNextReturnsCorrectElements() {
        NonEmptyIterable<String> subject = immutableNonEmptyIterable("foo", asList("bar", "baz"));
        Iterator<String> iterator = subject.iterator();
        assertEquals("foo", iterator.next());
        assertEquals("bar", iterator.next());
        assertEquals("baz", iterator.next());
    }

    @SuppressWarnings("ConstantConditions")
    @Test
    void iteratorHasNextCanBeCalledMultipleTimes() {
        NonEmptyIterable<String> subject = immutableNonEmptyIterable("foo", asList("bar", "baz"));
        Iterator<String> iterator = subject.iterator();
        assertTrue(iterator.hasNext());
        assertTrue(iterator.hasNext());
        assertTrue(iterator.hasNext());
        assertEquals("foo", iterator.next());
    }

    @Test
    void iteratorHasNextReturnsFalseIfNothingRemains() {
        NonEmptyIterable<String> subject = immutableNonEmptyIterable("foo", emptyList());
        Iterator<String> iterator = subject.iterator();
        iterator.next();
        assertFalse(iterator.hasNext());
    }

    @Test
    void iteratorNextThrowsIfNothingRemains() {
        NonEmptyIterable<String> subject = immutableNonEmptyIterable("foo", emptyList());
        Iterator<String> iterator = subject.iterator();
        iterator.next();
        assertThrows(NoSuchElementException.class, iterator::next);
    }

    @Test
    void iteratorThrowsIfRemoveIsCalled() {
        NonEmptyIterable<String> subject = immutableNonEmptyIterable("foo", asList("bar", "baz"));
        Iterator<String> iterator = subject.iterator();
        assertThrows(UnsupportedOperationException.class, iterator::remove);
        iterator.next();
        assertThrows(UnsupportedOperationException.class, iterator::remove);
        iterator.next();
        assertThrows(UnsupportedOperationException.class, iterator::remove);
        iterator.next();
        assertThrows(UnsupportedOperationException.class, iterator::remove);
    }

    @Nested
    @DisplayName("append")
    class Append {

        @Test
        void toSize3() {
            assertThat(immutableNonEmptyIterable("foo", asList("bar", "baz")).append("qux"),
                    contains("foo", "bar", "baz", "qux"));
        }

    }

    @Nested
    @DisplayName("concat")
    class Concat {

        @Test
        void throwsOnNullArgument() {
            assertThrows(NullPointerException.class, () -> immutableNonEmptyIterable("foo", emptyList()).
                    concat((ImmutableFiniteIterable<String>) null));
        }

        @Test
        void size3PlusSize3() {
            NonEmptyIterable<String> subject = immutableNonEmptyIterable("foo", asList("bar", "baz"));
            assertThat(subject.concat(subject),
                    contains("foo", "bar", "baz", "foo", "bar", "baz"));
        }

    }

    @Nested
    @DisplayName("drop")
    class Drop {

        @Test
        void throwsOnNegativeArgument() {
            assertThrows(IllegalArgumentException.class, () -> immutableNonEmptyIterable("foo", emptyList()).drop(-1));
        }

        @Test
        void countOfZero() {
            ImmutableNonEmptyIterable<Integer> subject = immutableNonEmptyIterable(1, asList(2, 3));
            assertThat(subject.drop(0), contains(1, 2, 3));
        }

        @Test
        void countOfOne() {
            ImmutableNonEmptyIterable<Integer> subject = immutableNonEmptyIterable(1, asList(2, 3));
            assertThat(subject.drop(1), contains(2, 3));
        }

        @Test
        void countExceedingSize() {
            ImmutableNonEmptyIterable<Integer> subject = immutableNonEmptyIterable(1, asList(2, 3));
            assertThat(subject.drop(10000), emptyIterable());
        }

    }

    @Nested
    @DisplayName("dropWhile")
    class DropWhile {

        @Test
        void throwsOnNullArgument() {
            assertThrows(NullPointerException.class, () -> immutableNonEmptyIterable("foo", emptyList()).dropWhile(null));
        }

        @Test
        void predicateNeverTrue() {
            ImmutableNonEmptyIterable<Integer> subject = immutableNonEmptyIterable(1, asList(2, 3));
            assertThat(subject.dropWhile(constantly(false)), contains(1, 2, 3));
        }

        @Test
        void predicateAlwaysTrue() {
            ImmutableNonEmptyIterable<Integer> subject = immutableNonEmptyIterable(1, asList(2, 3));
            assertThat(subject.dropWhile(constantly(true)), emptyIterable());
        }

        @Test
        void predicateSometimesTrue() {
            ImmutableNonEmptyIterable<Integer> subject = immutableNonEmptyIterable(1, asList(2, 3));
            assertThat(subject.dropWhile(LT.lt(2)), contains(2, 3));
        }

    }

    @Nested
    @DisplayName("filter")
    class Filter {

        @Test
        void throwsOnNullArgument() {
            assertThrows(NullPointerException.class, () -> immutableNonEmptyIterable("foo", emptyList()).dropWhile(null));
        }

        @Test
        void predicateNeverTrue() {
            ImmutableNonEmptyIterable<Integer> subject = immutableNonEmptyIterable(1, asList(2, 3));
            assertThat(subject.filter(constantly(false)), emptyIterable());
        }

        @Test
        void predicateAlwaysTrue() {
            ImmutableNonEmptyIterable<Integer> subject = immutableNonEmptyIterable(1, asList(2, 3));
            assertThat(subject.filter(constantly(true)), contains(1, 2, 3));
        }

        @Test
        void predicateSometimesTrue() {
            ImmutableNonEmptyIterable<Integer> subject = immutableNonEmptyIterable(1, asList(2, 3));
            assertThat(subject.filter(n -> n % 2 == 1), contains(1, 3));
        }

    }

    @Nested
    @DisplayName("find")
    class Find {

        @Test
        void predicateNeverTrue() {
            ImmutableNonEmptyIterable<Integer> subject = immutableNonEmptyIterable(1, asList(2, 3));
            assertEquals(nothing(), subject.find(constantly(false)));
        }

        @Test
        void predicateAlwaysTrue() {
            ImmutableNonEmptyIterable<Integer> subject = immutableNonEmptyIterable(1, asList(2, 3));
            assertEquals(just(1), subject.find(constantly(true)));
        }

        @Test
        void predicateSometimesTrue() {
            ImmutableNonEmptyIterable<Integer> subject = immutableNonEmptyIterable(1, asList(2, 3, 4));
            assertEquals(just(2), subject.find(n -> n % 2 == 0));
        }
    }

    @Nested
    @DisplayName("fmap")
    class Fmap {

        @Test
        void throwsOnNullArgument() {
            assertThrows(NullPointerException.class, () -> immutableNonEmptyIterable("foo", emptyList()).fmap(null));
        }

        @Test
        void testCase1() {
            assertThat(immutableNonEmptyIterable(1, asList(2, 3)).fmap(n -> n * 2), contains(2, 4, 6));
        }

        @Test
        void functorIdentity() {
            ImmutableNonEmptyIterable<Integer> subject = immutableNonEmptyIterable(1, asList(2, 3));
            assertTrue(iterablesContainSameElements(subject, subject.fmap(id())));
        }

        @Test
        void functorComposition() {
            ImmutableNonEmptyIterable<Integer> subject = immutableNonEmptyIterable(1, asList(2, 3));
            Fn1<Integer, Integer> f = n -> n * 2;
            Fn1<Integer, String> g = Object::toString;
            assertTrue(iterablesContainSameElements(subject.fmap(f).fmap(g), subject.fmap(f.fmap(g))));
        }

    }

    @Nested
    @DisplayName("intersperse")
    class Intersperse {

        @Test
        void doesNothingOnSingletonList() {
            assertThat(immutableNonEmptyIterable("foo", emptyList()).intersperse("*"),
                    contains("foo"));
        }

        @Test
        void testCase1() {
            assertThat(immutableNonEmptyIterable("foo", asList("bar", "baz")).intersperse("*"),
                    contains("foo", "*", "bar", "*", "baz"));
        }

    }

    @Nested
    @DisplayName("isEmpty")
    class IsEmpty {

        @Test
        void negative() {
            assertFalse(immutableNonEmptyIterable("foo", emptyList()).isEmpty());
        }

    }

    @Nested
    @DisplayName("prepend")
    class Prepend {


        @Test
        void toSize3() {
            assertThat(immutableNonEmptyIterable("foo", asList("bar", "baz")).prepend("qux"),
                    contains("qux", "foo", "bar", "baz"));
        }

    }

    @Nested
    @DisplayName("prependAll")
    class PrependAll {

        @Test
        void testCase1() {
            assertThat(immutableNonEmptyIterable("foo", emptyList()).prependAll("*"),
                    contains("*", "foo"));
        }

        @Test
        void testCase2() {
            assertThat(immutableNonEmptyIterable("foo", asList("bar", "baz")).prependAll("*"),
                    contains("*", "foo", "*", "bar", "*", "baz"));
        }

    }

    @Nested
    @DisplayName("slide")
    class Slide {

        @Test
        void throwsOnZeroArgument() {
            assertThrows(IllegalArgumentException.class, () -> immutableNonEmptyIterable("foo", emptyList()).slide(0));
        }

        @Test
        void k1() {
            assertThat(immutableNonEmptyIterable(0, asList(1, 2, 3)).slide(1),
                    contains(contains(0), contains(1), contains(2), contains(3)));
        }

        @Test
        void k2() {
            assertThat(immutableNonEmptyIterable(0, asList(1, 2, 3)).slide(2),
                    contains(contains(0, 1), contains(1, 2), contains(2, 3)));
        }

    }

    @Nested
    @DisplayName("tails")
    class Tails {

        @Test
        void testCase1() {
            assertThat(immutableNonEmptyIterable(1, asList(2, 3, 4, 5)).tails(),
                    contains(contains(1, 2, 3, 4, 5), contains(2, 3, 4, 5), contains(3, 4, 5), contains(4, 5),
                            contains(5), emptyIterable()));
        }

    }

    @Nested
    @DisplayName("take")
    class Take {

        @Test
        void throwsOnNegativeArgument() {
            assertThrows(IllegalArgumentException.class, () -> immutableNonEmptyIterable("foo", emptyList()).drop(-1));
        }

        @Test
        void countOfZero() {
            ImmutableNonEmptyIterable<Integer> subject = immutableNonEmptyIterable(1, asList(2, 3));
            assertThat(subject.take(0), emptyIterable());
        }

        @Test
        void countOfOne() {
            ImmutableNonEmptyIterable<Integer> subject = immutableNonEmptyIterable(1, asList(2, 3));
            assertThat(subject.take(1), contains(1));
        }

        @Test
        void countExceedingSize() {
            ImmutableNonEmptyIterable<Integer> subject = immutableNonEmptyIterable(1, asList(2, 3));
            assertThat(subject.take(10000), contains(1, 2, 3));
        }

    }

    @Nested
    @DisplayName("takeWhile")
    class TakeWhile {

        @Test
        void throwsOnNullArgument() {
            assertThrows(NullPointerException.class, () -> immutableNonEmptyIterable("foo", emptyList()).takeWhile(null));
        }

        @Test
        void predicateNeverTrue() {
            ImmutableNonEmptyIterable<Integer> subject = immutableNonEmptyIterable(1, asList(2, 3));
            assertThat(subject.takeWhile(constantly(false)), emptyIterable());
        }

        @Test
        void predicateAlwaysTrue() {
            ImmutableNonEmptyIterable<Integer> subject = immutableNonEmptyIterable(1, asList(2, 3));
            assertThat(subject.takeWhile(constantly(true)), contains(1, 2, 3));
        }

        @Test
        void predicateSometimesTrue() {
            ImmutableNonEmptyIterable<Integer> subject = immutableNonEmptyIterable(1, asList(2, 3));
            assertThat(subject.takeWhile(LT.lt(2)), contains(1));
        }

    }

    @Nested
    @DisplayName("toArray")
    class ToArray {

        @Test
        void throwsOnNullArgument() {
            assertThrows(NullPointerException.class, () -> immutableNonEmptyIterable("foo", emptyList()).toArray(null));
        }

        @Test
        void writesToArray() {
            assertArrayEquals(new Integer[]{1, 2, 3}, immutableNonEmptyIterable(1, asList(2, 3)).toArray(Integer[].class));
        }

    }

    @Nested
    @DisplayName("toCollection")
    class ToCollection {

        @Test
        void throwsOnNullArgument() {
            assertThrows(NullPointerException.class, () -> immutableNonEmptyIterable("foo", emptyList()).toCollection(null));
        }

        @Test
        void toArrayList() {
            assertThat(immutableNonEmptyIterable(1, asList(2, 3)).toCollection(ArrayList::new), contains(1, 2, 3));
        }

    }

    @Nested
    @DisplayName("toFinite")
    class ToFinite {

        @Test
        void successCase() {
            assertTrue(maybeIterablesContainSameElements(
                    just(finiteIterable(asList(1, 2, 3))),
                    immutableNonEmptyIterable(1, immutableFiniteIterable(asList(2, 3))).toFinite()));
        }

        @Test
        void failureCase() {
            assertEquals(nothing(), immutableNonEmptyIterable(1, Repeat.repeat(1)).toFinite());
        }

    }

    @Nested
    @DisplayName("toNonEmpty")
    class ToNonEmpty {

        @Test
        void alwaysSucceeds() {
            ImmutableNonEmptyIterable<Integer> subject = immutableNonEmptyIterable(1, Repeat.repeat(1));
            assertSame(subject, subject.toNonEmpty().orElseThrow(AssertionError::new));
        }

    }

    @Nested
    @DisplayName("zipWith")
    class ZipWith {

        @Test
        void throwsOnNullFunction() {
            assertThrows(NullPointerException.class, () -> immutableNonEmptyIterable("foo", emptyList())
                    .zipWith(null, emptyList()));
        }

        @Test
        void throwsOnNullArgument() {
            assertThrows(NullPointerException.class, () -> immutableNonEmptyIterable("foo", emptyList())
                    .zipWith(tupler(), (ImmutableNonEmptyIterable<String>) null));
        }

        @Test
        void testCase1() {
            ImmutableNonEmptyIterable<Integer> list1 = immutableNonEmptyIterable(1, asList(2, 3, 4, 5));
            ImmutableNonEmptyIterable<String> list2 = immutableNonEmptyIterable("foo", asList("bar", "baz"));
            assertThat(list1.zipWith(tupler(), list2), contains(tuple(1, "foo"), tuple(2, "bar"), tuple(3, "baz")));
        }

    }

    private static <A> ImmutableNonEmptyIterable<A> immutableNonEmptyIterable(A head, Iterable<A> tail) {
        return ImmutableNonEmptyIterable.immutableNonEmptyIterable(head, EnhancedIterables.immutableIterable(tail));
    }

}
