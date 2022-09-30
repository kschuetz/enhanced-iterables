package software.kes.enhancediterables;

import com.jnape.palatable.lambda.adt.hlist.Tuple2;
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
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsEmptyIterable.emptyIterable;
import static org.hamcrest.collection.IsIterableContainingInOrder.contains;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static testsupport.IterablesContainSameElements.iterablesContainSameElements;
import static testsupport.IterablesContainSameElements.maybeIterablesContainSameElements;
import static testsupport.IterateN.iterateN;

class EnhancedIterableTest {

    @Test
    void singletonIteration() {
        MatcherAssert.assertThat(EnhancedIterable.enhance(singletonList(1)), contains(1));
    }

    @Test
    void multipleIteration() {
        MatcherAssert.assertThat(EnhancedIterable.enhance(asList(1, 2, 3, 4, 5, 6)), contains(1, 2, 3, 4, 5, 6));
    }

    @Test
    void canWrapInfiniteIterables() {
        EnhancedIterable<Integer> subject = EnhancedIterable.enhance(Repeat.repeat(1));
        assertEquals(1, subject.iterator().next());
    }

    @Test
    void iteratorNextReturnsCorrectElements() {
        EnhancedIterable<String> subject = EnhancedIterable.enhance(asList("foo", "bar", "baz"));
        Iterator<String> iterator = subject.iterator();
        assertEquals("foo", iterator.next());
        assertEquals("bar", iterator.next());
        assertEquals("baz", iterator.next());
    }

    @Test
    void constructedUsingRepeatRepeatsTheSameValueForever() {
        assertThat(EnhancedIterables.repeat(1).drop(10000).take(10),
                contains(1, 1, 1, 1, 1, 1, 1, 1, 1, 1));
    }

    @SuppressWarnings("ConstantConditions")
    @Test
    void iteratorHasNextCanBeCalledMultipleTimes() {
        EnhancedIterable<String> subject = EnhancedIterable.enhance(asList("foo", "bar", "baz"));
        Iterator<String> iterator = subject.iterator();
        assertTrue(iterator.hasNext());
        assertTrue(iterator.hasNext());
        assertTrue(iterator.hasNext());
        assertEquals("foo", iterator.next());
    }

    @Test
    void iteratorHasNextReturnsFalseIfNothingRemains() {
        EnhancedIterable<String> subject = EnhancedIterable.enhance(singletonList("foo"));
        Iterator<String> iterator = subject.iterator();
        iterator.next();
        assertFalse(iterator.hasNext());
    }

    @Test
    void iteratorNextThrowsIfNothingRemains() {
        EnhancedIterable<String> subject = EnhancedIterable.enhance(singletonList("foo"));
        Iterator<String> iterator = subject.iterator();
        iterator.next();
        assertThrows(NoSuchElementException.class, iterator::next);
    }

    @Test
    void iteratorThrowsIfRemoveIsCalled() {
        EnhancedIterable<String> subject = EnhancedIterable.enhance(asList("foo", "bar", "baz"));
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
            MatcherAssert.assertThat(EnhancedIterable.enhance(emptyList()).append("foo"), contains("foo"));
        }

        @Test
        void toSize3() {
            MatcherAssert.assertThat(EnhancedIterable.enhance(asList("foo", "bar", "baz")).append("qux"),
                    contains("foo", "bar", "baz", "qux"));
        }

        @Test
        void stackSafe() {
            assertTimeoutPreemptively(Duration.ofSeconds(5), () -> {
                EnhancedIterable<Integer> result = iterateN(50_000,
                        EnhancedIterable.enhance(emptyList()),
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
            assertThrows(NullPointerException.class, () -> EnhancedIterable.enhance(emptyList()).concat(null));
        }

        @Test
        void emptyPlusEmpty() {
            MatcherAssert.assertThat(EnhancedIterable.enhance(emptyList()).concat(emptyList()), emptyIterable());
        }

        @Test
        void emptyPlusSize3() {
            MatcherAssert.assertThat(EnhancedIterable.enhance(emptyList()).concat(asList("foo", "bar", "baz")),
                    contains("foo", "bar", "baz"));
        }

        @Test
        void size3PlusEmpty() {
            MatcherAssert.assertThat(EnhancedIterable.enhance(asList("foo", "bar", "baz")).concat(emptyList()),
                    contains("foo", "bar", "baz"));
        }

        @Test
        void size3PlusSize3() {
            List<String> underlying = asList("foo", "bar", "baz");
            MatcherAssert.assertThat(EnhancedIterable.enhance(underlying).concat(underlying),
                    contains("foo", "bar", "baz", "foo", "bar", "baz"));
        }

        @Test
        void stackSafe() {
            EnhancedIterable<Integer> xs = EnhancedIterable.enhance(asList(0, 1, 2, 3, 4, 5, 6, 7, 8, 9));
            assertTimeoutPreemptively(Duration.ofSeconds(5), () -> {
                EnhancedIterable<Integer> result = iterateN(10_000,
                        EnhancedIterable.enhance(emptyList()),
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
            assertThrows(IllegalArgumentException.class, () -> EnhancedIterable.enhance(emptyList()).drop(-1));
        }

        @Test
        void countOfZero() {
            EnhancedIterable<Integer> subject = EnhancedIterable.enhance(asList(1, 2, 3));
            assertThat(subject.drop(0), contains(1, 2, 3));
        }

        @Test
        void countOfOne() {
            EnhancedIterable<Integer> subject = EnhancedIterable.enhance(asList(1, 2, 3));
            assertThat(subject.drop(1), contains(2, 3));
        }

        @Test
        void countExceedingSize() {
            EnhancedIterable<Integer> subject = EnhancedIterable.enhance(asList(1, 2, 3));
            assertThat(subject.drop(10000), emptyIterable());
        }

        @Test
        void stackSafe() {
            assertTimeoutPreemptively(Duration.ofSeconds(5), () -> {
                EnhancedIterable<Integer> result = iterateN(10_000,
                        EnhancedIterable.enhance(IntSequence.integers(1, 10_003)),
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
            assertThrows(NullPointerException.class, () -> EnhancedIterable.enhance(emptyList()).dropWhile(null));
        }

        @Test
        void predicateNeverTrue() {
            EnhancedIterable<Integer> subject = EnhancedIterable.enhance(asList(1, 2, 3));
            assertThat(subject.dropWhile(constantly(false)), contains(1, 2, 3));
        }

        @Test
        void predicateAlwaysTrue() {
            EnhancedIterable<Integer> subject = EnhancedIterable.enhance(asList(1, 2, 3));
            assertThat(subject.dropWhile(constantly(true)), emptyIterable());
        }

        @Test
        void predicateSometimesTrue() {
            EnhancedIterable<Integer> subject = EnhancedIterable.enhance(asList(1, 2, 3));
            assertThat(subject.dropWhile(LT.lt(2)), contains(2, 3));
        }

    }

    @Nested
    @DisplayName("filter")
    class Filter {

        @Test
        void throwsOnNullArgument() {
            assertThrows(NullPointerException.class, () -> EnhancedIterable.enhance(emptyList()).dropWhile(null));
        }

        @Test
        void predicateNeverTrue() {
            EnhancedIterable<Integer> subject = EnhancedIterable.enhance(asList(1, 2, 3));
            assertThat(subject.filter(constantly(false)), emptyIterable());
        }

        @Test
        void predicateAlwaysTrue() {
            EnhancedIterable<Integer> subject = EnhancedIterable.enhance(asList(1, 2, 3));
            assertThat(subject.filter(constantly(true)), contains(1, 2, 3));
        }

        @Test
        void predicateSometimesTrue() {
            EnhancedIterable<Integer> subject = EnhancedIterable.enhance(asList(1, 2, 3));
            assertThat(subject.filter(n -> n % 2 == 1), contains(1, 3));
        }

        @Test
        void stackSafe() {
            assertTimeoutPreemptively(Duration.ofSeconds(5), () -> {
                EnhancedIterable<Integer> result = iterateN(10_000,
                        EnhancedIterable.enhance(IntSequence.integers(1, 10)),
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
            EnhancedIterable<Integer> subject = EnhancedIterable.enhance(asList(1, 2, 3));
            assertEquals(nothing(), subject.find(constantly(false)));
        }

        @Test
        void predicateAlwaysTrue() {
            EnhancedIterable<Integer> subject = EnhancedIterable.enhance(asList(1, 2, 3));
            assertEquals(just(1), subject.find(constantly(true)));
        }

        @Test
        void predicateSometimesTrue() {
            EnhancedIterable<Integer> subject = EnhancedIterable.enhance(asList(1, 2, 3, 4));
            assertEquals(just(2), subject.find(n -> n % 2 == 0));
        }
    }

    @Nested
    @DisplayName("fmap")
    class Fmap {

        @Test
        void throwsOnNullArgument() {
            assertThrows(NullPointerException.class, () -> EnhancedIterable.enhance(emptyList()).fmap(null));
        }

        @Test
        void testCase1() {
            MatcherAssert.assertThat(EnhancedIterable.enhance(asList(1, 2, 3)).fmap(n -> n * 2), contains(2, 4, 6));
        }

        @Test
        void functorIdentity() {
            EnhancedIterable<Integer> subject = EnhancedIterable.enhance(asList(1, 2, 3));
            assertTrue(iterablesContainSameElements(subject, subject.fmap(id())));
        }

        @Test
        void functorComposition() {
            EnhancedIterable<Integer> subject = EnhancedIterable.enhance(asList(1, 2, 3));
            Fn1<Integer, Integer> f = n -> n * 2;
            Fn1<Integer, String> g = Object::toString;
            assertTrue(iterablesContainSameElements(subject.fmap(f).fmap(g), subject.fmap(f.fmap(g))));
        }

        @Test
        void stackSafe() {
            assertTimeoutPreemptively(Duration.ofSeconds(5), () -> {
                EnhancedIterable<Integer> result = iterateN(10_000,
                        EnhancedIterable.enhance(asList(0, 1, 2)),
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
            MatcherAssert.assertThat(EnhancedIterable.enhance(emptyList()).intersperse("*"), emptyIterable());
        }

        @Test
        void doesNothingOnSingletonList() {
            MatcherAssert.assertThat(EnhancedIterable.enhance(singletonList("foo")).intersperse("*"),
                    contains("foo"));
        }

        @Test
        void testCase1() {
            MatcherAssert.assertThat(EnhancedIterable.enhance(asList("foo", "bar", "baz")).intersperse("*"),
                    contains("foo", "*", "bar", "*", "baz"));
        }

    }

    @Nested
    @DisplayName("isEmpty")
    class IsEmpty {

        @Test
        void positive() {
            Assertions.assertTrue(EnhancedIterable.enhance(emptyList()).isEmpty());
        }

        @Test
        void negative() {
            Assertions.assertFalse(EnhancedIterable.enhance(asList(1, 2, 3)).isEmpty());
        }

    }

    @Nested
    @DisplayName("magnetizeBy")
    class MagnetizeBy {

        @Test
        void throwsOnNullArgument() {
            assertThrows(NullPointerException.class, () -> EnhancedIterable.enhance(emptyList()).magnetizeBy(null));
        }

        @Test
        void lambdaTestCase() {
            Fn2<Integer, Integer, Boolean> lte = (x, y) -> x <= y;
            MatcherAssert.assertThat(EnhancedIterable.enhance(Collections.<Integer>emptyList()).magnetizeBy(lte), emptyIterable());
            MatcherAssert.assertThat(EnhancedIterable.enhance(singletonList(1)).magnetizeBy(lte), contains(contains(1)));
            MatcherAssert.assertThat(EnhancedIterable.enhance(asList(1, 2, 3, 2, 2, 3, 2, 1)).magnetizeBy(lte),
                    contains(contains(1, 2, 3),
                            contains(2, 2, 3),
                            contains(2),
                            contains(1)));
        }

        @Test
        void worksWithInfinite() {
            MatcherAssert.assertThat(EnhancedIterable.enhance(Cycle.cycle(1, 1, 2, 2, 3))
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
            assertThrows(NullPointerException.class, () -> EnhancedIterable.enhance(emptyList()).partition(null));
        }

        @Test
        void lambdaTestCase() {
            EnhancedIterable<String> strings = EnhancedIterable.enhance(asList("one", "two", "three", "four", "five"));
            Tuple2<? extends EnhancedIterable<String>, ? extends EnhancedIterable<Integer>> partition =
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
            MatcherAssert.assertThat(EnhancedIterable.enhance(emptyList()).prepend("foo"), contains("foo"));
        }

        @Test
        void toSize3() {
            MatcherAssert.assertThat(EnhancedIterable.enhance(asList("foo", "bar", "baz")).prepend("qux"),
                    contains("qux", "foo", "bar", "baz"));
        }

        @Test
        void stackSafe() {
            assertTimeoutPreemptively(Duration.ofSeconds(5), () -> {
                EnhancedIterable<Integer> result = iterateN(50_000,
                        EnhancedIterable.enhance(emptyList()),
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
            MatcherAssert.assertThat(EnhancedIterable.enhance(emptyList()).prependAll("*"), emptyIterable());
        }

        @Test
        void testCase1() {
            MatcherAssert.assertThat(EnhancedIterable.enhance(singletonList("foo")).prependAll("*"),
                    contains("*", "foo"));
        }

        @Test
        void testCase2() {
            MatcherAssert.assertThat(EnhancedIterable.enhance(asList("foo", "bar", "baz")).prependAll("*"),
                    contains("*", "foo", "*", "bar", "*", "baz"));
        }

    }

    @Nested
    @DisplayName("slide")
    class Slide {

        @Test
        void throwsOnZeroArgument() {
            assertThrows(IllegalArgumentException.class, () -> EnhancedIterable.enhance(emptyList()).slide(0));
        }

        @Test
        void onEmpty() {
            MatcherAssert.assertThat(EnhancedIterable.enhance(emptyList()).slide(1), emptyIterable());
        }

        @Test
        void k1() {
            MatcherAssert.assertThat(EnhancedIterable.enhance(asList(0, 1, 2, 3)).slide(1),
                    contains(contains(0), contains(1), contains(2), contains(3)));
        }

        @Test
        void k2() {
            MatcherAssert.assertThat(EnhancedIterable.enhance(asList(0, 1, 2, 3)).slide(2),
                    contains(contains(0, 1), contains(1, 2), contains(2, 3)));
        }

    }

    @Nested
    @DisplayName("span")
    class Span {

        @Test
        void throwsOnNullArgument() {
            assertThrows(NullPointerException.class, () -> EnhancedIterable.enhance(emptyList()).span(null));
        }

        @Test
        void testCase1() {
            Tuple2<? extends EnhancedIterable<Integer>, ? extends EnhancedIterable<Integer>> spanResult =
                    EnhancedIterable.enhance(asList(1, 2, 3, 4, 5)).span(n -> n < 4);
            assertThat(spanResult._1(), contains(1, 2, 3));
            assertThat(spanResult._2(), contains(4, 5));
        }

    }

    @Nested
    @DisplayName("tails")
    class Tails {

        @Test
        void testCase1() {
            MatcherAssert.assertThat(EnhancedIterable.enhance(asList(1, 2, 3, 4, 5)).tails(),
                    contains(contains(1, 2, 3, 4, 5), contains(2, 3, 4, 5), contains(3, 4, 5), contains(4, 5),
                            contains(5), emptyIterable()));
        }

    }

    @Nested
    @DisplayName("take")
    class Take {

        @Test
        void throwsOnNegativeArgument() {
            assertThrows(IllegalArgumentException.class, () -> EnhancedIterable.enhance(emptyList()).drop(-1));
        }

        @Test
        void countOfZero() {
            EnhancedIterable<Integer> subject = EnhancedIterable.enhance(asList(1, 2, 3));
            assertThat(subject.take(0), emptyIterable());
        }

        @Test
        void countOfOne() {
            EnhancedIterable<Integer> subject = EnhancedIterable.enhance(asList(1, 2, 3));
            assertThat(subject.take(1), contains(1));
        }

        @Test
        void countExceedingSize() {
            EnhancedIterable<Integer> subject = EnhancedIterable.enhance(asList(1, 2, 3));
            assertThat(subject.take(10000), contains(1, 2, 3));
        }

    }

    @Nested
    @DisplayName("takeWhile")
    class TakeWhile {

        @Test
        void throwsOnNullArgument() {
            assertThrows(NullPointerException.class, () -> EnhancedIterable.enhance(emptyList()).takeWhile(null));
        }

        @Test
        void predicateNeverTrue() {
            EnhancedIterable<Integer> subject = EnhancedIterable.enhance(asList(1, 2, 3));
            assertThat(subject.takeWhile(constantly(false)), emptyIterable());
        }

        @Test
        void predicateAlwaysTrue() {
            EnhancedIterable<Integer> subject = EnhancedIterable.enhance(asList(1, 2, 3));
            assertThat(subject.takeWhile(constantly(true)), contains(1, 2, 3));
        }

        @Test
        void predicateSometimesTrue() {
            EnhancedIterable<Integer> subject = EnhancedIterable.enhance(asList(1, 2, 3));
            assertThat(subject.takeWhile(LT.lt(2)), contains(1));
        }

    }

    @Nested
    @DisplayName("toArray")
    class ToArray {

        @Test
        void throwsOnNullArgument() {
            assertThrows(NullPointerException.class, () -> EnhancedIterable.enhance(emptyList()).toArray(null));
        }

        @Test
        void writesToArray() {
            Assertions.assertArrayEquals(new Integer[]{1, 2, 3}, EnhancedIterable.enhance(asList(1, 2, 3)).toArray(Integer[].class));
        }

    }

    @Nested
    @DisplayName("toCollection")
    class ToCollection {

        @Test
        void throwsOnNullArgument() {
            assertThrows(NullPointerException.class, () -> EnhancedIterable.enhance(emptyList()).toCollection(null));
        }

        @Test
        void toArrayList() {
            MatcherAssert.assertThat(EnhancedIterable.enhance(asList(1, 2, 3)).toCollection(ArrayList::new), contains(1, 2, 3));
        }

    }

    @Nested
    @DisplayName("toFinite")
    class ToFinite {

        @Test
        void successCase() {
            assertTrue(maybeIterablesContainSameElements(
                    just(FiniteIterable.finiteIterable(asList(1, 2, 3))),
                    EnhancedIterable.enhance(asList(1, 2, 3)).toFinite()));
        }

        @Test
        void failureCase() {
            Assertions.assertEquals(nothing(), EnhancedIterable.enhance(Repeat.repeat(1)).toFinite());
        }

    }

    @Nested
    @DisplayName("toNonEmpty")
    class ToNonEmpty {

        @Test
        void successCase() {
            assertTrue(maybeIterablesContainSameElements(
                    just(EnhancedIterables.nonEmptyIterable(1, asList(2, 3))),
                    EnhancedIterable.enhance(asList(1, 2, 3)).toNonEmpty()));
        }

        @Test
        void failureCase() {
            Assertions.assertEquals(nothing(), EnhancedIterable.enhance(emptyList()).toNonEmpty());
        }

    }

    @Nested
    @DisplayName("zipWith")
    class ZipWith {

        @Test
        void throwsOnNullFunction() {
            assertThrows(NullPointerException.class, () -> EnhancedIterable.enhance(emptyList()).zipWith(null, emptyList()));
        }

        @Test
        void throwsOnNullArgument() {
            assertThrows(NullPointerException.class, () -> EnhancedIterable.enhance(emptyList())
                    .zipWith(tupler(), (EnhancedIterable<Object>) null));
        }

        @Test
        void testCase1() {
            EnhancedIterable<Integer> list1 = EnhancedIterable.enhance(asList(1, 2, 3, 4, 5));
            EnhancedIterable<String> list2 = EnhancedIterable.enhance(asList("foo", "bar", "baz"));
            assertThat(list1.zipWith(tupler(), list2), contains(tuple(1, "foo"), tuple(2, "bar"), tuple(3, "baz")));
        }

    }

}
