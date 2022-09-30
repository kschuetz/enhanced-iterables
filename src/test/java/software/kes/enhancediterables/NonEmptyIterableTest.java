package software.kes.enhancediterables;

import com.jnape.palatable.lambda.functions.Fn1;
import com.jnape.palatable.lambda.functions.Fn2;
import com.jnape.palatable.lambda.functions.builtin.fn1.Cycle;
import com.jnape.palatable.lambda.functions.builtin.fn1.Repeat;
import com.jnape.palatable.lambda.functions.builtin.fn2.Eq;
import com.jnape.palatable.lambda.functions.builtin.fn2.LT;
import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import testsupport.IntSequence;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.NoSuchElementException;

import static com.jnape.palatable.lambda.adt.Maybe.just;
import static com.jnape.palatable.lambda.adt.Maybe.nothing;
import static com.jnape.palatable.lambda.adt.hlist.HList.tuple;
import static com.jnape.palatable.lambda.functions.builtin.fn1.Constantly.constantly;
import static com.jnape.palatable.lambda.functions.builtin.fn1.Id.id;
import static com.jnape.palatable.lambda.functions.builtin.fn1.Size.size;
import static com.jnape.palatable.lambda.functions.builtin.fn2.Tupler2.tupler;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsEmptyIterable.emptyIterable;
import static org.hamcrest.collection.IsIterableContainingInOrder.contains;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static software.kes.enhancediterables.FiniteIterable.finiteIterable;
import static testsupport.IterablesContainSameElements.iterablesContainSameElements;
import static testsupport.IterablesContainSameElements.maybeIterablesContainSameElements;
import static testsupport.IterateN.iterateN;

class NonEmptyIterableTest {

    @Test
    void singletonHead() {
        NonEmptyIterable<Integer> subject = NonEmptyIterable.nonEmptyIterable(1, emptyList());
        assertEquals(1, subject.head());
    }

    @Test
    void singletonTail() {
        NonEmptyIterable<Integer> subject = NonEmptyIterable.nonEmptyIterable(1, emptyList());
        assertThat(subject.tail(), emptyIterable());
    }

    @Test
    void singletonIteration() {
        NonEmptyIterable<Integer> subject = NonEmptyIterable.nonEmptyIterable(1, emptyList());
        assertThat(subject, contains(1));
    }

    @Test
    void multipleHead() {
        NonEmptyIterable<Integer> subject = NonEmptyIterable.nonEmptyIterable(1, asList(2, 3));
        assertEquals(1, subject.head());
    }

    @Test
    void multipleTail() {
        NonEmptyIterable<Integer> subject = NonEmptyIterable.nonEmptyIterable(1, asList(2, 3));
        assertThat(subject.tail(), contains(2, 3));
    }

    @Test
    void multipleIteration() {
        NonEmptyIterable<Integer> subject = NonEmptyIterable.nonEmptyIterable(1, asList(2, 3, 4, 5, 6));
        assertThat(subject, contains(1, 2, 3, 4, 5, 6));
    }

    @Test
    void canWrapInfiniteIterables() {
        NonEmptyIterable<Integer> subject = NonEmptyIterable.nonEmptyIterable(1, Repeat.repeat(1));
        assertEquals(1, subject.iterator().next());
    }

    @Test
    void iteratorNextReturnsCorrectElements() {
        NonEmptyIterable<String> subject = NonEmptyIterable.nonEmptyIterable("foo", asList("bar", "baz"));
        Iterator<String> iterator = subject.iterator();
        assertEquals("foo", iterator.next());
        assertEquals("bar", iterator.next());
        assertEquals("baz", iterator.next());
    }

    @SuppressWarnings("ConstantConditions")
    @Test
    void iteratorHasNextCanBeCalledMultipleTimes() {
        NonEmptyIterable<String> subject = NonEmptyIterable.nonEmptyIterable("foo", asList("bar", "baz"));
        Iterator<String> iterator = subject.iterator();
        assertTrue(iterator.hasNext());
        assertTrue(iterator.hasNext());
        assertTrue(iterator.hasNext());
        assertEquals("foo", iterator.next());
    }

    @Test
    void iteratorHasNextReturnsFalseIfNothingRemains() {
        NonEmptyIterable<String> subject = NonEmptyIterable.nonEmptyIterable("foo", emptyList());
        Iterator<String> iterator = subject.iterator();
        iterator.next();
        assertFalse(iterator.hasNext());
    }

    @Test
    void iteratorNextThrowsIfNothingRemains() {
        NonEmptyIterable<String> subject = NonEmptyIterable.nonEmptyIterable("foo", emptyList());
        Iterator<String> iterator = subject.iterator();
        iterator.next();
        assertThrows(NoSuchElementException.class, iterator::next);
    }

    @Test
    void iteratorThrowsIfRemoveIsCalled() {
        NonEmptyIterable<String> subject = NonEmptyIterable.nonEmptyIterable("foo", asList("bar", "baz"));
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
            MatcherAssert.assertThat(NonEmptyIterable.nonEmptyIterable("foo", asList("bar", "baz")).append("qux"),
                    contains("foo", "bar", "baz", "qux"));
        }

        @Test
        void stackSafe() {
            assertTimeoutPreemptively(Duration.ofSeconds(5), () -> {
                NonEmptyIterable<Integer> result = iterateN(50_000,
                        NonEmptyIterable.nonEmptyIterable(1, emptyList()),
                        acc -> acc.append(1));
                assertEquals(50_001, size(result));
            });
        }

    }

    @Nested
    @DisplayName("concat")
    class Concat {

        @Test
        void throwsOnNullArgument() {
            assertThrows(NullPointerException.class, () -> NonEmptyIterable.nonEmptyIterable("foo", emptyList()).concat(null));
        }

        @Test
        void size3PlusSize3() {
            NonEmptyIterable<String> subject = NonEmptyIterable.nonEmptyIterable("foo", asList("bar", "baz"));
            assertThat(subject.concat(subject),
                    contains("foo", "bar", "baz", "foo", "bar", "baz"));
        }

        @Test
        void stackSafe() {
            NonEmptyIterable<Integer> xs = NonEmptyIterable.nonEmptyIterable(0, asList(1, 2, 3, 4, 5, 6, 7, 8, 9));
            assertTimeoutPreemptively(Duration.ofSeconds(5), () -> {
                NonEmptyIterable<Integer> result = iterateN(9999,
                        xs,
                        acc -> acc.concat(xs));
                assertEquals(100_000, size(result));
            });
        }

    }

    @Nested
    @DisplayName("drop")
    class Drop {

        @Test
        void throwsOnNegativeArgument() {
            assertThrows(IllegalArgumentException.class, () -> NonEmptyIterable.nonEmptyIterable("foo", emptyList()).drop(-1));
        }

        @Test
        void countOfZero() {
            NonEmptyIterable<Integer> subject = NonEmptyIterable.nonEmptyIterable(1, asList(2, 3));
            assertThat(subject.drop(0), contains(1, 2, 3));
        }

        @Test
        void countOfOne() {
            NonEmptyIterable<Integer> subject = NonEmptyIterable.nonEmptyIterable(1, asList(2, 3));
            assertThat(subject.drop(1), contains(2, 3));
        }

        @Test
        void countExceedingSize() {
            NonEmptyIterable<Integer> subject = NonEmptyIterable.nonEmptyIterable(1, asList(2, 3));
            assertThat(subject.drop(10000), emptyIterable());
        }

        @Test
        void stackSafe() {
            assertTimeoutPreemptively(Duration.ofSeconds(5), () -> {
                EnhancedIterable<Integer> result = iterateN(10_000,
                        (EnhancedIterable<Integer>) NonEmptyIterable.nonEmptyIterable(1, IntSequence.integers(2, 10_003)),
                        acc -> acc.drop(1));
                assertThat(result, contains(10_001, 10_002, 10_003));
            });
        }

    }

    @Nested
    @DisplayName("dropWhile")
    class DropWhile {

        @Test
        void throwsOnNullArgument() {
            assertThrows(NullPointerException.class, () -> NonEmptyIterable.nonEmptyIterable("foo", emptyList()).dropWhile(null));
        }

        @Test
        void predicateNeverTrue() {
            NonEmptyIterable<Integer> subject = NonEmptyIterable.nonEmptyIterable(1, asList(2, 3));
            assertThat(subject.dropWhile(constantly(false)), contains(1, 2, 3));
        }

        @Test
        void predicateAlwaysTrue() {
            NonEmptyIterable<Integer> subject = NonEmptyIterable.nonEmptyIterable(1, asList(2, 3));
            assertThat(subject.dropWhile(constantly(true)), emptyIterable());
        }

        @Test
        void predicateSometimesTrue() {
            NonEmptyIterable<Integer> subject = NonEmptyIterable.nonEmptyIterable(1, asList(2, 3));
            assertThat(subject.dropWhile(LT.lt(2)), contains(2, 3));
        }

    }

    @Nested
    @DisplayName("filter")
    class Filter {

        @Test
        void throwsOnNullArgument() {
            assertThrows(NullPointerException.class, () -> NonEmptyIterable.nonEmptyIterable("foo", emptyList()).dropWhile(null));
        }

        @Test
        void predicateNeverTrue() {
            NonEmptyIterable<Integer> subject = NonEmptyIterable.nonEmptyIterable(1, asList(2, 3));
            assertThat(subject.filter(constantly(false)), emptyIterable());
        }

        @Test
        void predicateAlwaysTrue() {
            NonEmptyIterable<Integer> subject = NonEmptyIterable.nonEmptyIterable(1, asList(2, 3));
            assertThat(subject.filter(constantly(true)), contains(1, 2, 3));
        }

        @Test
        void predicateSometimesTrue() {
            NonEmptyIterable<Integer> subject = NonEmptyIterable.nonEmptyIterable(1, asList(2, 3));
            assertThat(subject.filter(n -> n % 2 == 1), contains(1, 3));
        }

        @Test
        void stackSafe() {
            assertTimeoutPreemptively(Duration.ofSeconds(5), () -> {
                EnhancedIterable<Integer> result = iterateN(10_000,
                        (EnhancedIterable<Integer>) NonEmptyIterable.nonEmptyIterable(1, IntSequence.integers(2, 10)),
                        acc -> acc.filter(x -> x % 2 == 0));
                assertThat(result, contains(2, 4, 6, 8, 10));
            });
        }

    }

    @Nested
    @DisplayName("find")
    class Find {

        @Test
        void predicateNeverTrue() {
            NonEmptyIterable<Integer> subject = NonEmptyIterable.nonEmptyIterable(1, asList(2, 3));
            assertEquals(nothing(), subject.find(constantly(false)));
        }

        @Test
        void predicateAlwaysTrue() {
            NonEmptyIterable<Integer> subject = NonEmptyIterable.nonEmptyIterable(1, asList(2, 3));
            assertEquals(just(1), subject.find(constantly(true)));
        }

        @Test
        void predicateSometimesTrue() {
            NonEmptyIterable<Integer> subject = NonEmptyIterable.nonEmptyIterable(1, asList(2, 3, 4));
            assertEquals(just(2), subject.find(n -> n % 2 == 0));
        }
    }

    @Nested
    @DisplayName("fmap")
    class Fmap {

        @Test
        void throwsOnNullArgument() {
            assertThrows(NullPointerException.class, () -> NonEmptyIterable.nonEmptyIterable("foo", emptyList()).fmap(null));
        }

        @Test
        void testCase1() {
            MatcherAssert.assertThat(NonEmptyIterable.nonEmptyIterable(1, asList(2, 3)).fmap(n -> n * 2), contains(2, 4, 6));
        }

        @Test
        void functorIdentity() {
            NonEmptyIterable<Integer> subject = NonEmptyIterable.nonEmptyIterable(1, asList(2, 3));
            assertTrue(iterablesContainSameElements(subject, subject.fmap(id())));
        }

        @Test
        void functorComposition() {
            NonEmptyIterable<Integer> subject = NonEmptyIterable.nonEmptyIterable(1, asList(2, 3));
            Fn1<Integer, Integer> f = n -> n * 2;
            Fn1<Integer, String> g = Object::toString;
            assertTrue(iterablesContainSameElements(subject.fmap(f).fmap(g), subject.fmap(f.fmap(g))));
        }

        @Test
        void stackSafe() {
            assertTimeoutPreemptively(Duration.ofSeconds(5), () -> {
                NonEmptyIterable<Integer> result = iterateN(10_000,
                        NonEmptyIterable.nonEmptyIterable(0, asList(1, 2)),
                        acc -> acc.fmap(x -> x + 1));
                assertThat(result, contains(10_000, 10_001, 10_002));
            });
        }

    }

    @Nested
    @DisplayName("intersperse")
    class Intersperse {

        @Test
        void doesNothingOnSingletonList() {
            MatcherAssert.assertThat(NonEmptyIterable.nonEmptyIterable("foo", emptyList()).intersperse("*"),
                    contains("foo"));
        }

        @Test
        void testCase1() {
            MatcherAssert.assertThat(NonEmptyIterable.nonEmptyIterable("foo", asList("bar", "baz")).intersperse("*"),
                    contains("foo", "*", "bar", "*", "baz"));
        }

    }

    @Nested
    @DisplayName("isEmpty")
    class IsEmpty {

        @Test
        void negative() {
            Assertions.assertFalse(NonEmptyIterable.nonEmptyIterable("foo", emptyList()).isEmpty());
        }

    }

    @Nested
    @DisplayName("magnetizeBy")
    class MagnetizeBy {

        @Test
        void throwsOnNullArgument() {
            assertThrows(NullPointerException.class, () -> NonEmptyIterable.nonEmptyIterable("foo", emptyList()).magnetizeBy(null));
        }

        @Test
        void lambdaTestCase() {
            Fn2<Integer, Integer, Boolean> lte = (x, y) -> x <= y;
            MatcherAssert.assertThat(NonEmptyIterable.nonEmptyIterable(1, emptyList()).magnetizeBy(lte), contains(contains(1)));
            MatcherAssert.assertThat(NonEmptyIterable.nonEmptyIterable(1, asList(2, 3, 2, 2, 3, 2, 1)).magnetizeBy(lte),
                    contains(contains(1, 2, 3),
                            contains(2, 2, 3),
                            contains(2),
                            contains(1)));
        }

        @Test
        void worksWithInfinite() {
            MatcherAssert.assertThat(EnhancedIterables.unsafeNonEmptyIterable(Cycle.cycle(1, 1, 2, 2, 3))
                            .magnetizeBy(Eq.eq()).take(10),
                    contains(contains(1, 1),
                            contains(2, 2),
                            contains(3),
                            contains(1, 1),
                            contains(2, 2),
                            contains(3),
                            contains(1, 1),
                            contains(2, 2),
                            contains(3),
                            contains(1, 1)));
        }

    }

    @Nested
    @DisplayName("prepend")
    class Prepend {

        @Test
        void toSize3() {
            MatcherAssert.assertThat(NonEmptyIterable.nonEmptyIterable("foo", asList("bar", "baz")).prepend("qux"),
                    contains("qux", "foo", "bar", "baz"));
        }

        @Test
        void stackSafe() {
            assertTimeoutPreemptively(Duration.ofSeconds(5), () -> {
                NonEmptyIterable<Integer> result = iterateN(50_000,
                        NonEmptyIterable.nonEmptyIterable(1, emptyList()),
                        acc -> acc.prepend(1));
                assertEquals(50_001, size(result));
            });
        }

    }

    @Nested
    @DisplayName("prependAll")
    class PrependAll {

        @Test
        void testCase1() {
            MatcherAssert.assertThat(NonEmptyIterable.nonEmptyIterable("foo", emptyList()).prependAll("*"),
                    contains("*", "foo"));
        }

        @Test
        void testCase2() {
            MatcherAssert.assertThat(NonEmptyIterable.nonEmptyIterable("foo", asList("bar", "baz")).prependAll("*"),
                    contains("*", "foo", "*", "bar", "*", "baz"));
        }

    }

    @Nested
    @DisplayName("slide")
    class Slide {

        @Test
        void throwsOnZeroArgument() {
            assertThrows(IllegalArgumentException.class, () -> NonEmptyIterable.nonEmptyIterable("foo", emptyList()).slide(0));
        }

        @Test
        void k1() {
            MatcherAssert.assertThat(NonEmptyIterable.nonEmptyIterable(0, asList(1, 2, 3)).slide(1),
                    contains(contains(0), contains(1), contains(2), contains(3)));
        }

        @Test
        void k2() {
            MatcherAssert.assertThat(NonEmptyIterable.nonEmptyIterable(0, asList(1, 2, 3)).slide(2),
                    contains(contains(0, 1), contains(1, 2), contains(2, 3)));
        }

    }

    @Nested
    @DisplayName("tails")
    class Tails {

        @Test
        void testCase1() {
            MatcherAssert.assertThat(NonEmptyIterable.nonEmptyIterable(1, asList(2, 3, 4, 5)).tails(),
                    contains(contains(1, 2, 3, 4, 5), contains(2, 3, 4, 5), contains(3, 4, 5), contains(4, 5),
                            contains(5), emptyIterable()));
        }

    }

    @Nested
    @DisplayName("take")
    class Take {

        @Test
        void throwsOnNegativeArgument() {
            assertThrows(IllegalArgumentException.class, () -> NonEmptyIterable.nonEmptyIterable("foo", emptyList()).drop(-1));
        }

        @Test
        void countOfZero() {
            NonEmptyIterable<Integer> subject = NonEmptyIterable.nonEmptyIterable(1, asList(2, 3));
            assertThat(subject.take(0), emptyIterable());
        }

        @Test
        void countOfOne() {
            NonEmptyIterable<Integer> subject = NonEmptyIterable.nonEmptyIterable(1, asList(2, 3));
            assertThat(subject.take(1), contains(1));
        }

        @Test
        void countExceedingSize() {
            NonEmptyIterable<Integer> subject = NonEmptyIterable.nonEmptyIterable(1, asList(2, 3));
            assertThat(subject.take(10000), contains(1, 2, 3));
        }

    }

    @Nested
    @DisplayName("takeWhile")
    class TakeWhile {

        @Test
        void throwsOnNullArgument() {
            assertThrows(NullPointerException.class, () -> NonEmptyIterable.nonEmptyIterable("foo", emptyList()).takeWhile(null));
        }

        @Test
        void predicateNeverTrue() {
            NonEmptyIterable<Integer> subject = NonEmptyIterable.nonEmptyIterable(1, asList(2, 3));
            assertThat(subject.takeWhile(constantly(false)), emptyIterable());
        }

        @Test
        void predicateAlwaysTrue() {
            NonEmptyIterable<Integer> subject = NonEmptyIterable.nonEmptyIterable(1, asList(2, 3));
            assertThat(subject.takeWhile(constantly(true)), contains(1, 2, 3));
        }

        @Test
        void predicateSometimesTrue() {
            NonEmptyIterable<Integer> subject = NonEmptyIterable.nonEmptyIterable(1, asList(2, 3));
            assertThat(subject.takeWhile(LT.lt(2)), contains(1));
        }

    }

    @Nested
    @DisplayName("toArray")
    class ToArray {

        @Test
        void throwsOnNullArgument() {
            assertThrows(NullPointerException.class, () -> NonEmptyIterable.nonEmptyIterable("foo", emptyList()).toArray(null));
        }

        @Test
        void writesToArray() {
            Assertions.assertArrayEquals(new Integer[]{1, 2, 3}, NonEmptyIterable.nonEmptyIterable(1, asList(2, 3)).toArray(Integer[].class));
        }

    }

    @Nested
    @DisplayName("toCollection")
    class ToCollection {

        @Test
        void throwsOnNullArgument() {
            assertThrows(NullPointerException.class, () -> NonEmptyIterable.nonEmptyIterable("foo", emptyList()).toCollection(null));
        }

        @Test
        void toArrayList() {
            MatcherAssert.assertThat(NonEmptyIterable.nonEmptyIterable(1, asList(2, 3)).toCollection(ArrayList::new), contains(1, 2, 3));
        }

    }

    @Nested
    @DisplayName("toFinite")
    class ToFinite {

        @Test
        void successCase() {
            assertTrue(maybeIterablesContainSameElements(
                    just(finiteIterable(asList(1, 2, 3))),
                    NonEmptyIterable.nonEmptyIterable(1, asList(2, 3)).toFinite()));
        }

        @Test
        void failureCase() {
            Assertions.assertEquals(nothing(), NonEmptyIterable.nonEmptyIterable(1, Repeat.repeat(1)).toFinite());
        }

    }

    @Nested
    @DisplayName("toNonEmpty")
    class ToNonEmpty {

        @Test
        void alwaysSucceeds() {
            NonEmptyIterable<Integer> subject = NonEmptyIterable.nonEmptyIterable(1, asList(2, 3));
            assertSame(subject, subject.toNonEmpty().orElseThrow(AssertionError::new));
        }

    }

    @Nested
    @DisplayName("zipWith")
    class ZipWith {

        @Test
        void throwsOnNullFunction() {
            assertThrows(NullPointerException.class, () -> NonEmptyIterable.nonEmptyIterable("foo", emptyList()).zipWith(null, emptyList()));
        }

        @Test
        void throwsOnNullArgument() {
            assertThrows(NullPointerException.class, () -> NonEmptyIterable.nonEmptyIterable("foo", emptyList())
                    .zipWith(tupler(), (NonEmptyIterable<Object>) null));
        }

        @Test
        void testCase1() {
            NonEmptyIterable<Integer> list1 = NonEmptyIterable.nonEmptyIterable(1, asList(2, 3, 4, 5));
            NonEmptyIterable<String> list2 = NonEmptyIterable.nonEmptyIterable("foo", asList("bar", "baz"));
            assertThat(list1.zipWith(tupler(), list2), contains(tuple(1, "foo"), tuple(2, "bar"), tuple(3, "baz")));
        }

    }

}
