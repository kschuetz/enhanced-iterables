package dev.marksman.enhancediterables;

import com.jnape.palatable.lambda.adt.hlist.Tuple2;
import com.jnape.palatable.lambda.functions.Fn1;
import com.jnape.palatable.lambda.functions.Fn2;
import com.jnape.palatable.lambda.functions.builtin.fn1.Cycle;
import com.jnape.palatable.lambda.functions.builtin.fn1.Repeat;
import com.jnape.palatable.lambda.functions.builtin.fn2.Eq;
import com.jnape.palatable.lambda.functions.builtin.fn2.LT;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import testsupport.IntSequence;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import static com.jnape.palatable.lambda.adt.Maybe.just;
import static com.jnape.palatable.lambda.adt.Maybe.nothing;
import static com.jnape.palatable.lambda.adt.choice.Choice2.a;
import static com.jnape.palatable.lambda.adt.choice.Choice2.b;
import static com.jnape.palatable.lambda.adt.hlist.HList.tuple;
import static com.jnape.palatable.lambda.functions.builtin.fn1.Constantly.constantly;
import static com.jnape.palatable.lambda.functions.builtin.fn1.Id.id;
import static com.jnape.palatable.lambda.functions.builtin.fn1.Size.size;
import static com.jnape.palatable.lambda.functions.builtin.fn2.Tupler2.tupler;
import static dev.marksman.enhancediterables.EnhancedIterables.immutableIterable;
import static dev.marksman.enhancediterables.FiniteIterable.finiteIterable;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsEmptyIterable.emptyIterable;
import static org.hamcrest.collection.IsIterableContainingInOrder.contains;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static testsupport.IterablesContainSameElements.iterablesContainSameElements;
import static testsupport.IterablesContainSameElements.maybeIterablesContainSameElements;
import static testsupport.IterateN.iterateN;

class ImmutableIterableTest {

    @Test
    void singletonIteration() {
        assertThat(immutableIterable(singletonList(1)), contains(1));
    }

    @Test
    void multipleIteration() {
        assertThat(immutableIterable(asList(1, 2, 3, 4, 5, 6)), contains(1, 2, 3, 4, 5, 6));
    }

    @Test
    void iteratorNextReturnsCorrectElements() {
        ImmutableIterable<String> subject = immutableIterable(asList("foo", "bar", "baz"));
        Iterator<String> iterator = subject.iterator();
        assertEquals("foo", iterator.next());
        assertEquals("bar", iterator.next());
        assertEquals("baz", iterator.next());
    }

    @SuppressWarnings("ConstantConditions")
    @Test
    void iteratorHasNextCanBeCalledMultipleTimes() {
        ImmutableIterable<String> subject = immutableIterable(asList("foo", "bar", "baz"));
        Iterator<String> iterator = subject.iterator();
        assertTrue(iterator.hasNext());
        assertTrue(iterator.hasNext());
        assertTrue(iterator.hasNext());
        assertEquals("foo", iterator.next());
    }

    @Test
    void iteratorHasNextReturnsFalseIfNothingRemains() {
        ImmutableIterable<String> subject = immutableIterable(singletonList("foo"));
        Iterator<String> iterator = subject.iterator();
        iterator.next();
        assertFalse(iterator.hasNext());
    }

    @Test
    void iteratorNextThrowsIfNothingRemains() {
        ImmutableIterable<String> subject = immutableIterable(singletonList("foo"));
        Iterator<String> iterator = subject.iterator();
        iterator.next();
        assertThrows(NoSuchElementException.class, iterator::next);
    }

    @Test
    void iteratorThrowsIfRemoveIsCalled() {
        ImmutableIterable<String> subject = immutableIterable(asList("foo", "bar", "baz"));
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
        void toEmpty() {
            assertThat(immutableIterable(emptyList()).append("foo"), contains("foo"));
        }

        @Test
        void toSize3() {
            assertThat(immutableIterable(asList("foo", "bar", "baz")).append("qux"),
                    contains("foo", "bar", "baz", "qux"));
        }

        @Test
        void stackSafe() {
            assertTimeoutPreemptively(Duration.ofSeconds(5), () -> {
                ImmutableIterable<Integer> result = iterateN(50_000,
                        immutableIterable(emptyList()),
                        acc -> acc.append(1));
                assertEquals(50_000, size(result));
            });
        }

    }

    @Nested
    @DisplayName("concat")
    class Concat {

        @Test
        void throwsOnNullArgument() {
            assertThrows(NullPointerException.class, () -> immutableIterable(emptyList()).concat((ImmutableIterable<Object>) null));
        }

        @Test
        void emptyPlusEmpty() {
            assertThat(immutableIterable(emptyList()).concat(emptyList()), emptyIterable());
        }

        @Test
        void emptyPlusSize3() {
            assertThat(immutableIterable(emptyList()).concat(asList("foo", "bar", "baz")),
                    contains("foo", "bar", "baz"));
        }

        @Test
        void size3PlusEmpty() {
            assertThat(immutableIterable(asList("foo", "bar", "baz")).concat(emptyList()),
                    contains("foo", "bar", "baz"));
        }

        @Test
        void size3PlusSize3() {
            List<String> underlying = asList("foo", "bar", "baz");
            assertThat(immutableIterable(underlying).concat(underlying),
                    contains("foo", "bar", "baz", "foo", "bar", "baz"));
        }

        @Test
        void stackSafe() {
            ImmutableIterable<Integer> xs = immutableIterable(asList(0, 1, 2, 3, 4, 5, 6, 7, 8, 9));
            assertTimeoutPreemptively(Duration.ofSeconds(5), () -> {
                ImmutableIterable<Integer> result = iterateN(10_000,
                        immutableIterable(emptyList()),
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
            assertThrows(IllegalArgumentException.class, () -> immutableIterable(emptyList()).drop(-1));
        }

        @Test
        void countOfZero() {
            ImmutableIterable<Integer> subject = immutableIterable(asList(1, 2, 3));
            assertThat(subject.drop(0), contains(1, 2, 3));
        }

        @Test
        void countOfOne() {
            ImmutableIterable<Integer> subject = immutableIterable(asList(1, 2, 3));
            assertThat(subject.drop(1), contains(2, 3));
        }

        @Test
        void countExceedingSize() {
            ImmutableIterable<Integer> subject = immutableIterable(asList(1, 2, 3));
            assertThat(subject.drop(10000), emptyIterable());
        }

        @Test
        void stackSafe() {
            assertTimeoutPreemptively(Duration.ofSeconds(5), () -> {
                ImmutableIterable<Integer> result = iterateN(10_000,
                        immutableIterable(IntSequence.integers(1, 10_003)),
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
            assertThrows(NullPointerException.class, () -> immutableIterable(emptyList()).dropWhile(null));
        }

        @Test
        void predicateNeverTrue() {
            ImmutableIterable<Integer> subject = immutableIterable(asList(1, 2, 3));
            assertThat(subject.dropWhile(constantly(false)), contains(1, 2, 3));
        }

        @Test
        void predicateAlwaysTrue() {
            ImmutableIterable<Integer> subject = immutableIterable(asList(1, 2, 3));
            assertThat(subject.dropWhile(constantly(true)), emptyIterable());
        }

        @Test
        void predicateSometimesTrue() {
            ImmutableIterable<Integer> subject = immutableIterable(asList(1, 2, 3));
            assertThat(subject.dropWhile(LT.lt(2)), contains(2, 3));
        }

    }

    @Nested
    @DisplayName("filter")
    class Filter {

        @Test
        void throwsOnNullArgument() {
            assertThrows(NullPointerException.class, () -> immutableIterable(emptyList()).dropWhile(null));
        }

        @Test
        void predicateNeverTrue() {
            ImmutableIterable<Integer> subject = immutableIterable(asList(1, 2, 3));
            assertThat(subject.filter(constantly(false)), emptyIterable());
        }

        @Test
        void predicateAlwaysTrue() {
            ImmutableIterable<Integer> subject = immutableIterable(asList(1, 2, 3));
            assertThat(subject.filter(constantly(true)), contains(1, 2, 3));
        }

        @Test
        void predicateSometimesTrue() {
            ImmutableIterable<Integer> subject = immutableIterable(asList(1, 2, 3));
            assertThat(subject.filter(n -> n % 2 == 1), contains(1, 3));
        }

        @Test
        void stackSafe() {
            assertTimeoutPreemptively(Duration.ofSeconds(5), () -> {
                ImmutableIterable<Integer> result = iterateN(10_000,
                        immutableIterable(IntSequence.integers(1, 10)),
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
            ImmutableIterable<Integer> subject = immutableIterable(asList(1, 2, 3));
            assertEquals(nothing(), subject.find(constantly(false)));
        }

        @Test
        void predicateAlwaysTrue() {
            ImmutableIterable<Integer> subject = immutableIterable(asList(1, 2, 3));
            assertEquals(just(1), subject.find(constantly(true)));
        }

        @Test
        void predicateSometimesTrue() {
            ImmutableIterable<Integer> subject = immutableIterable(asList(1, 2, 3, 4));
            assertEquals(just(2), subject.find(n -> n % 2 == 0));
        }
    }

    @Nested
    @DisplayName("fmap")
    class Fmap {

        @Test
        void throwsOnNullArgument() {
            assertThrows(NullPointerException.class, () -> immutableIterable(emptyList()).fmap(null));
        }

        @Test
        void testCase1() {
            assertThat(immutableIterable(asList(1, 2, 3)).fmap(n -> n * 2), contains(2, 4, 6));
        }

        @Test
        void functorIdentity() {
            ImmutableIterable<Integer> subject = immutableIterable(asList(1, 2, 3));
            assertTrue(iterablesContainSameElements(subject, subject.fmap(id())));
        }

        @Test
        void functorComposition() {
            ImmutableIterable<Integer> subject = immutableIterable(asList(1, 2, 3));
            Fn1<Integer, Integer> f = n -> n * 2;
            Fn1<Integer, String> g = Object::toString;
            assertTrue(iterablesContainSameElements(subject.fmap(f).fmap(g), subject.fmap(f.fmap(g))));
        }

        @Test
        void stackSafe() {
            assertTimeoutPreemptively(Duration.ofSeconds(5), () -> {
                ImmutableIterable<Integer> result = iterateN(10_000,
                        immutableIterable(asList(0, 1, 2)),
                        acc -> acc.fmap(x -> x + 1));
                assertThat(result, contains(10_000, 10_001, 10_002));
            });
        }

    }

    @Nested
    @DisplayName("intersperse")
    class Intersperse {

        @Test
        void doesNothingOnEmptyList() {
            assertThat(immutableIterable(emptyList()).intersperse("*"), emptyIterable());
        }

        @Test
        void doesNothingOnSingletonList() {
            assertThat(immutableIterable(singletonList("foo")).intersperse("*"),
                    contains("foo"));
        }

        @Test
        void testCase1() {
            assertThat(immutableIterable(asList("foo", "bar", "baz")).intersperse("*"),
                    contains("foo", "*", "bar", "*", "baz"));
        }

    }

    @Nested
    @DisplayName("isEmpty")
    class IsEmpty {

        @Test
        void positive() {
            assertTrue(immutableIterable(emptyList()).isEmpty());
        }

        @Test
        void negative() {
            assertFalse(immutableIterable(asList(1, 2, 3)).isEmpty());
        }

    }

    @Nested
    @DisplayName("magnetizeBy")
    class MagnetizeBy {

        @Test
        void throwsOnNullArgument() {
            assertThrows(NullPointerException.class, () -> immutableIterable(emptyList()).magnetizeBy(null));
        }

        @Test
        void lambdaTestCase() {
            Fn2<Integer, Integer, Boolean> lte = (x, y) -> x <= y;
            assertThat(immutableIterable(Collections.<Integer>emptyList()).magnetizeBy(lte), emptyIterable());
            assertThat(immutableIterable(singletonList(1)).magnetizeBy(lte), contains(contains(1)));
            assertThat(immutableIterable(asList(1, 2, 3, 2, 2, 3, 2, 1)).magnetizeBy(lte),
                    contains(contains(1, 2, 3),
                            contains(2, 2, 3),
                            contains(2),
                            contains(1)));
        }

        @Test
        void worksWithInfinite() {
            assertThat(immutableIterable(Cycle.cycle(1, 1, 2, 2, 3))
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
    @DisplayName("partition")
    class Partition {

        @Test
        void throwsOnNullArgument() {
            assertThrows(NullPointerException.class, () -> immutableIterable(emptyList()).partition(null));
        }

        @Test
        void lambdaTestCase() {
            ImmutableIterable<String> strings = immutableIterable(asList("one", "two", "three", "four", "five"));
            Tuple2<? extends ImmutableIterable<String>, ? extends ImmutableIterable<Integer>> partition =
                    strings.partition(s -> s.length() % 2 == 1 ? a(s) : b(s.length()));

            assertThat(partition._1(), contains("one", "two", "three"));
            assertThat(partition._2(), contains(4, 4));
        }

    }

    @Nested
    @DisplayName("prepend")
    class Prepend {

        @Test
        void toEmpty() {
            assertThat(immutableIterable(emptyList()).prepend("foo"), contains("foo"));
        }

        @Test
        void toSize3() {
            assertThat(immutableIterable(asList("foo", "bar", "baz")).prepend("qux"),
                    contains("qux", "foo", "bar", "baz"));
        }

        @Test
        void stackSafe() {
            assertTimeoutPreemptively(Duration.ofSeconds(5), () -> {
                ImmutableIterable<Integer> result = iterateN(50_000,
                        immutableIterable(emptyList()),
                        acc -> acc.prepend(1));
                assertEquals(50_000, size(result));
            });
        }

    }

    @Nested
    @DisplayName("prependAll")
    class PrependAll {

        @Test
        void doesNothingOnEmptyList() {
            assertThat(immutableIterable(emptyList()).prependAll("*"), emptyIterable());
        }

        @Test
        void testCase1() {
            assertThat(immutableIterable(singletonList("foo")).prependAll("*"),
                    contains("*", "foo"));
        }

        @Test
        void testCase2() {
            assertThat(immutableIterable(asList("foo", "bar", "baz")).prependAll("*"),
                    contains("*", "foo", "*", "bar", "*", "baz"));
        }

    }

    @Nested
    @DisplayName("slide")
    class Slide {

        @Test
        void throwsOnZeroArgument() {
            assertThrows(IllegalArgumentException.class, () -> immutableIterable(emptyList()).slide(0));
        }

        @Test
        void onEmpty() {
            assertThat(immutableIterable(emptyList()).slide(1), emptyIterable());
        }

        @Test
        void k1() {
            assertThat(immutableIterable(asList(0, 1, 2, 3)).slide(1),
                    contains(contains(0), contains(1), contains(2), contains(3)));
        }

        @Test
        void k2() {
            assertThat(immutableIterable(asList(0, 1, 2, 3)).slide(2),
                    contains(contains(0, 1), contains(1, 2), contains(2, 3)));
        }

    }

    @Nested
    @DisplayName("span")
    class Span {

        @Test
        void throwsOnNullArgument() {
            assertThrows(NullPointerException.class, () -> immutableIterable(emptyList()).span(null));
        }

        @Test
        void testCase1() {
            Tuple2<? extends ImmutableIterable<Integer>, ? extends ImmutableIterable<Integer>> spanResult =
                    immutableIterable(asList(1, 2, 3, 4, 5)).span(n -> n < 4);
            assertThat(spanResult._1(), contains(1, 2, 3));
            assertThat(spanResult._2(), contains(4, 5));
        }

    }

    @Nested
    @DisplayName("tails")
    class Tails {

        @Test
        void testCase1() {
            assertThat(immutableIterable(asList(1, 2, 3, 4, 5)).tails(),
                    contains(contains(1, 2, 3, 4, 5), contains(2, 3, 4, 5), contains(3, 4, 5), contains(4, 5),
                            contains(5), emptyIterable()));
        }

    }

    @Nested
    @DisplayName("take")
    class Take {

        @Test
        void throwsOnNegativeArgument() {
            assertThrows(IllegalArgumentException.class, () -> immutableIterable(emptyList()).drop(-1));
        }

        @Test
        void countOfZero() {
            ImmutableIterable<Integer> subject = immutableIterable(asList(1, 2, 3));
            assertThat(subject.take(0), emptyIterable());
        }

        @Test
        void countOfOne() {
            ImmutableIterable<Integer> subject = immutableIterable(asList(1, 2, 3));
            assertThat(subject.take(1), contains(1));
        }

        @Test
        void countExceedingSize() {
            ImmutableIterable<Integer> subject = immutableIterable(asList(1, 2, 3));
            assertThat(subject.take(10000), contains(1, 2, 3));
        }

    }

    @Nested
    @DisplayName("takeWhile")
    class TakeWhile {

        @Test
        void throwsOnNullArgument() {
            assertThrows(NullPointerException.class, () -> immutableIterable(emptyList()).takeWhile(null));
        }

        @Test
        void predicateNeverTrue() {
            ImmutableIterable<Integer> subject = immutableIterable(asList(1, 2, 3));
            assertThat(subject.takeWhile(constantly(false)), emptyIterable());
        }

        @Test
        void predicateAlwaysTrue() {
            ImmutableIterable<Integer> subject = immutableIterable(asList(1, 2, 3));
            assertThat(subject.takeWhile(constantly(true)), contains(1, 2, 3));
        }

        @Test
        void predicateSometimesTrue() {
            ImmutableIterable<Integer> subject = immutableIterable(asList(1, 2, 3));
            assertThat(subject.takeWhile(LT.lt(2)), contains(1));
        }

    }

    @Nested
    @DisplayName("toArray")
    class ToArray {

        @Test
        void throwsOnNullArgument() {
            assertThrows(NullPointerException.class, () -> immutableIterable(emptyList()).toArray(null));
        }

        @Test
        void writesToArray() {
            assertArrayEquals(new Integer[]{1, 2, 3}, immutableIterable(asList(1, 2, 3)).toArray(Integer[].class));
        }

    }

    @Nested
    @DisplayName("toCollection")
    class ToCollection {

        @Test
        void throwsOnNullArgument() {
            assertThrows(NullPointerException.class, () -> immutableIterable(emptyList()).toCollection(null));
        }

        @Test
        void toArrayList() {
            assertThat(immutableIterable(asList(1, 2, 3)).toCollection(ArrayList::new), contains(1, 2, 3));
        }

    }

    @Nested
    @DisplayName("toFinite")
    class ToFinite {

        @Test
        void successCase() {
            assertTrue(maybeIterablesContainSameElements(
                    just(finiteIterable(asList(1, 2, 3))),
                    immutableIterable(asList(1, 2, 3)).toFinite()));
        }

        @Test
        void failureCase() {
            assertEquals(nothing(), immutableIterable(Repeat.repeat(1)).toFinite());
        }

    }

    @Nested
    @DisplayName("toNonEmpty")
    class ToNonEmpty {

        @Test
        void successCase() {
            assertTrue(maybeIterablesContainSameElements(
                    just(ImmutableNonEmptyIterable.of(1, 2, 3)),
                    ImmutableIterable.copyFrom(asList(1, 2, 3)).toNonEmpty()));
        }

        @Test
        void failureCase() {
            assertEquals(nothing(), ImmutableIterable.copyFrom(emptyList()).toNonEmpty());
        }

    }

    @Nested
    @DisplayName("zipWith")
    class ZipWith {

        @Test
        void throwsOnNullFunction() {
            assertThrows(NullPointerException.class, () -> immutableIterable(emptyList()).zipWith(null, emptyList()));
        }

        @Test
        void throwsOnNullArgument() {
            assertThrows(NullPointerException.class, () -> immutableIterable(emptyList())
                    .zipWith(tupler(), (ImmutableIterable<Object>) null));
        }

        @Test
        void testCase1() {
            ImmutableIterable<Integer> list1 = immutableIterable(asList(1, 2, 3, 4, 5));
            ImmutableIterable<String> list2 = immutableIterable(asList("foo", "bar", "baz"));
            assertThat(list1.zipWith(tupler(), list2), contains(tuple(1, "foo"), tuple(2, "bar"), tuple(3, "baz")));
        }

    }

}
