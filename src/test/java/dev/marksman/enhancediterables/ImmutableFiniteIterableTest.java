package dev.marksman.enhancediterables;

import com.jnape.palatable.lambda.adt.hlist.Tuple2;
import com.jnape.palatable.lambda.functions.Fn1;
import com.jnape.palatable.lambda.functions.builtin.fn2.LT;
import org.hamcrest.collection.IsEmptyIterable;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
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
import static com.jnape.palatable.lambda.functions.builtin.fn2.Tupler2.tupler;
import static dev.marksman.enhancediterables.EnhancedIterables.immutableFiniteIterable;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.emptyIterable;
import static org.hamcrest.collection.IsIterableContainingInOrder.contains;
import static org.junit.jupiter.api.Assertions.*;
import static testsupport.IterablesContainSameElements.iterablesContainSameElements;
import static testsupport.IterablesContainSameElements.maybeIterablesContainSameElements;

class ImmutableFiniteIterableTest {

    @Test
    void singletonIteration() {
        assertThat(immutableFiniteIterable(singletonList(1)), contains(1));
    }

    @Test
    void multipleIteration() {
        assertThat(immutableFiniteIterable(asList(1, 2, 3, 4, 5, 6)), contains(1, 2, 3, 4, 5, 6));
    }

    @Test
    void iteratorNextReturnsCorrectElements() {
        ImmutableFiniteIterable<String> subject = immutableFiniteIterable(asList("foo", "bar", "baz"));
        Iterator<String> iterator = subject.iterator();
        assertEquals("foo", iterator.next());
        assertEquals("bar", iterator.next());
        assertEquals("baz", iterator.next());
    }

    @SuppressWarnings("ConstantConditions")
    @Test
    void iteratorHasNextCanBeCalledMultipleTimes() {
        ImmutableFiniteIterable<String> subject = immutableFiniteIterable(asList("foo", "bar", "baz"));
        Iterator<String> iterator = subject.iterator();
        assertTrue(iterator.hasNext());
        assertTrue(iterator.hasNext());
        assertTrue(iterator.hasNext());
        assertEquals("foo", iterator.next());
    }

    @Test
    void iteratorHasNextReturnsFalseIfNothingRemains() {
        ImmutableFiniteIterable<String> subject = immutableFiniteIterable(singletonList("foo"));
        Iterator<String> iterator = subject.iterator();
        iterator.next();
        assertFalse(iterator.hasNext());
    }

    @Test
    void iteratorNextThrowsIfNothingRemains() {
        ImmutableFiniteIterable<String> subject = immutableFiniteIterable(singletonList("foo"));
        Iterator<String> iterator = subject.iterator();
        iterator.next();
        assertThrows(NoSuchElementException.class, iterator::next);
    }

    @Test
    void iteratorThrowsIfRemoveIsCalled() {
        ImmutableFiniteIterable<String> subject = immutableFiniteIterable(asList("foo", "bar", "baz"));
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
            assertThat(immutableFiniteIterable(emptyList()).append("foo"), contains("foo"));
        }

        @Test
        void toSize3() {
            assertThat(immutableFiniteIterable(asList("foo", "bar", "baz")).append("qux"),
                    contains("foo", "bar", "baz", "qux"));
        }

    }

    @Nested
    @DisplayName("concat")
    class Concat {

        @Test
        void throwsOnNullArgument() {
            assertThrows(NullPointerException.class, () -> immutableFiniteIterable(emptyList())
                    .concat((ImmutableFiniteIterable<Object>) null));
        }

        @Test
        void emptyPlusEmpty() {
            assertThat(immutableFiniteIterable(emptyList()).concat(emptyList()), IsEmptyIterable.emptyIterable());
        }

        @Test
        void emptyPlusSize3() {
            assertThat(immutableFiniteIterable(emptyList()).concat(asList("foo", "bar", "baz")),
                    contains("foo", "bar", "baz"));
        }

        @Test
        void size3PlusEmpty() {
            assertThat(immutableFiniteIterable(asList("foo", "bar", "baz")).concat(emptyList()),
                    contains("foo", "bar", "baz"));
        }

        @Test
        void size3PlusSize3() {
            List<String> underlying = asList("foo", "bar", "baz");
            assertThat(immutableFiniteIterable(underlying).concat(underlying),
                    contains("foo", "bar", "baz", "foo", "bar", "baz"));
        }

    }

    @Nested
    @DisplayName("cross")
    class Cross {

        @Test
        void throwsOnNullArgument() {
            assertThrows(NullPointerException.class, () -> immutableFiniteIterable(asList(1, 2, 3))
                    .cross((ImmutableFiniteIterable<Integer>) null));
        }

        @Test
        void nonEmptyWithEmpty() {
            assertThat(immutableFiniteIterable(asList(1, 2, 3)).cross(immutableFiniteIterable(emptyList())), emptyIterable());
        }

        @Test
        void emptyWithNonEmpty() {
            assertThat(immutableFiniteIterable(emptyList()).cross(immutableFiniteIterable(asList(1, 2, 3))), emptyIterable());
        }

        @Test
        void nonEmptyWithNonEmpty() {
            assertThat(immutableFiniteIterable(asList(1, 2, 3)).cross(immutableFiniteIterable(asList("foo", "bar", "baz"))),
                    contains(tuple(1, "foo"), tuple(1, "bar"), tuple(1, "baz"),
                            tuple(2, "foo"), tuple(2, "bar"), tuple(2, "baz"),
                            tuple(3, "foo"), tuple(3, "bar"), tuple(3, "baz")));
        }

    }

    @Nested
    @DisplayName("drop")
    class Drop {

        @Test
        void throwsOnNegativeArgument() {
            assertThrows(IllegalArgumentException.class, () -> immutableFiniteIterable(emptyList()).drop(-1));
        }

        @Test
        void countOfZero() {
            ImmutableFiniteIterable<Integer> subject = immutableFiniteIterable(asList(1, 2, 3));
            assertThat(subject.drop(0), contains(1, 2, 3));
        }

        @Test
        void countOfOne() {
            ImmutableFiniteIterable<Integer> subject = immutableFiniteIterable(asList(1, 2, 3));
            assertThat(subject.drop(1), contains(2, 3));
        }

        @Test
        void countExceedingSize() {
            ImmutableFiniteIterable<Integer> subject = immutableFiniteIterable(asList(1, 2, 3));
            assertThat(subject.drop(10000), IsEmptyIterable.emptyIterable());
        }

    }

    @Nested
    @DisplayName("dropWhile")
    class DropWhile {

        @Test
        void throwsOnNullArgument() {
            assertThrows(NullPointerException.class, () -> immutableFiniteIterable(emptyList()).dropWhile(null));
        }

        @Test
        void predicateNeverTrue() {
            ImmutableFiniteIterable<Integer> subject = immutableFiniteIterable(asList(1, 2, 3));
            assertThat(subject.dropWhile(constantly(false)), contains(1, 2, 3));
        }

        @Test
        void predicateAlwaysTrue() {
            ImmutableFiniteIterable<Integer> subject = immutableFiniteIterable(asList(1, 2, 3));
            assertThat(subject.dropWhile(constantly(true)), IsEmptyIterable.emptyIterable());
        }

        @Test
        void predicateSometimesTrue() {
            ImmutableFiniteIterable<Integer> subject = immutableFiniteIterable(asList(1, 2, 3));
            assertThat(subject.dropWhile(LT.lt(2)), contains(2, 3));
        }

    }

    @Nested
    @DisplayName("filter")
    class Filter {

        @Test
        void throwsOnNullArgument() {
            assertThrows(NullPointerException.class, () -> immutableFiniteIterable(emptyList()).dropWhile(null));
        }

        @Test
        void predicateNeverTrue() {
            ImmutableFiniteIterable<Integer> subject = immutableFiniteIterable(asList(1, 2, 3));
            assertThat(subject.filter(constantly(false)), IsEmptyIterable.emptyIterable());
        }

        @Test
        void predicateAlwaysTrue() {
            ImmutableFiniteIterable<Integer> subject = immutableFiniteIterable(asList(1, 2, 3));
            assertThat(subject.filter(constantly(true)), contains(1, 2, 3));
        }

        @Test
        void predicateSometimesTrue() {
            ImmutableFiniteIterable<Integer> subject = immutableFiniteIterable(asList(1, 2, 3));
            assertThat(subject.filter(n -> n % 2 == 1), contains(1, 3));
        }

    }

    @Nested
    @DisplayName("find")
    class Find {

        @Test
        void predicateNeverTrue() {
            ImmutableFiniteIterable<Integer> subject = immutableFiniteIterable(asList(1, 2, 3));
            assertEquals(nothing(), subject.find(constantly(false)));
        }

        @Test
        void predicateAlwaysTrue() {
            ImmutableFiniteIterable<Integer> subject = immutableFiniteIterable(asList(1, 2, 3));
            assertEquals(just(1), subject.find(constantly(true)));
        }

        @Test
        void predicateSometimesTrue() {
            ImmutableFiniteIterable<Integer> subject = immutableFiniteIterable(asList(1, 2, 3, 4));
            assertEquals(just(2), subject.find(n -> n % 2 == 0));
        }
    }

    @Nested
    @DisplayName("fmap")
    class Fmap {

        @Test
        void throwsOnNullArgument() {
            assertThrows(NullPointerException.class, () -> immutableFiniteIterable(emptyList()).fmap(null));
        }

        @Test
        void testCase1() {
            assertThat(immutableFiniteIterable(asList(1, 2, 3)).fmap(n -> n * 2), contains(2, 4, 6));
        }

        @Test
        void functorIdentity() {
            ImmutableFiniteIterable<Integer> subject = immutableFiniteIterable(asList(1, 2, 3));
            assertTrue(iterablesContainSameElements(subject, subject.fmap(id())));
        }

        @Test
        void functorComposition() {
            ImmutableFiniteIterable<Integer> subject = immutableFiniteIterable(asList(1, 2, 3));
            Fn1<Integer, Integer> f = n -> n * 2;
            Fn1<Integer, String> g = Object::toString;
            assertTrue(iterablesContainSameElements(subject.fmap(f).fmap(g), subject.fmap(f.fmap(g))));
        }

    }

    @Nested
    @DisplayName("foldLeft")
    class FoldLeft {

        @Test
        void throwsOnNullOperator() {
            FiniteIterable<Integer> ints = immutableFiniteIterable(asList(1, 2, 3));
            assertThrows(NullPointerException.class, () -> ints.foldLeft(null, 0));
        }

        @Test
        void onEmpty() {
            FiniteIterable<Integer> ints = immutableFiniteIterable(emptyList());
            assertEquals(999, ints.foldLeft(Integer::sum, 999));
        }

        @Test
        void onSize5() {
            FiniteIterable<Integer> ints = immutableFiniteIterable(asList(1, 2, 3, 4, 5));
            assertEquals(25, ints.foldLeft(Integer::sum, 10));
        }

    }

    @Nested
    @DisplayName("inits")
    class Inits {

        @Test
        void testCase1() {
            assertThat(immutableFiniteIterable(asList(1, 2, 3, 4, 5)).inits(),
                    contains(emptyIterable(), contains(1), contains(1, 2), contains(1, 2, 3),
                            contains(1, 2, 3, 4), contains(1, 2, 3, 4, 5)));
        }

    }

    @Nested
    @DisplayName("intersperse")
    class Intersperse {

        @Test
        void doesNothingOnEmptyList() {
            assertThat(immutableFiniteIterable(emptyList()).intersperse("*"), IsEmptyIterable.emptyIterable());
        }

        @Test
        void doesNothingOnSingletonList() {
            assertThat(immutableFiniteIterable(singletonList("foo")).intersperse("*"),
                    contains("foo"));
        }

        @Test
        void testCase1() {
            assertThat(immutableFiniteIterable(asList("foo", "bar", "baz")).intersperse("*"),
                    contains("foo", "*", "bar", "*", "baz"));
        }

    }

    @Nested
    @DisplayName("isEmpty")
    class IsEmpty {

        @Test
        void positive() {
            assertTrue(immutableFiniteIterable(emptyList()).isEmpty());
        }

        @Test
        void negative() {
            assertFalse(immutableFiniteIterable(asList(1, 2, 3)).isEmpty());
        }

    }

    @Nested
    @DisplayName("partition")
    class Partition {

        @Test
        void throwsOnNullArgument() {
            assertThrows(NullPointerException.class, () -> immutableFiniteIterable(emptyList()).partition(null));
        }

        @Test
        void lambdaTestCase() {
            ImmutableFiniteIterable<String> strings = immutableFiniteIterable(asList("one", "two", "three", "four", "five"));
            Tuple2<? extends ImmutableFiniteIterable<String>, ? extends ImmutableFiniteIterable<Integer>> partition =
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
            assertThat(immutableFiniteIterable(emptyList()).prepend("foo"), contains("foo"));
        }

        @Test
        void toSize3() {
            assertThat(immutableFiniteIterable(asList("foo", "bar", "baz")).prepend("qux"),
                    contains("qux", "foo", "bar", "baz"));
        }

    }

    @Nested
    @DisplayName("prependAll")
    class PrependAll {

        @Test
        void doesNothingOnEmptyList() {
            assertThat(immutableFiniteIterable(emptyList()).prependAll("*"), IsEmptyIterable.emptyIterable());
        }

        @Test
        void testCase1() {
            assertThat(immutableFiniteIterable(singletonList("foo")).prependAll("*"),
                    contains("*", "foo"));
        }

        @Test
        void testCase2() {
            assertThat(immutableFiniteIterable(asList("foo", "bar", "baz")).prependAll("*"),
                    contains("*", "foo", "*", "bar", "*", "baz"));
        }

    }

    @Nested
    @DisplayName("reverse")
    class Reverse {

        @Test
        void testCase1() {
            assertThat(immutableFiniteIterable(asList(1, 2, 3, 4, 5)).reverse(), contains(5, 4, 3, 2, 1));
        }

    }

    @Nested
    @DisplayName("slide")
    class Slide {

        @Test
        void throwsOnZeroArgument() {
            assertThrows(IllegalArgumentException.class, () -> immutableFiniteIterable(emptyList()).slide(0));
        }

        @Test
        void onEmpty() {
            assertThat(immutableFiniteIterable(emptyList()).slide(1), IsEmptyIterable.emptyIterable());
        }

        @Test
        void k1() {
            assertThat(immutableFiniteIterable(asList(0, 1, 2, 3)).slide(1),
                    contains(contains(0), contains(1), contains(2), contains(3)));
        }

        @Test
        void k2() {
            assertThat(immutableFiniteIterable(asList(0, 1, 2, 3)).slide(2),
                    contains(contains(0, 1), contains(1, 2), contains(2, 3)));
        }

    }

    @Nested
    @DisplayName("span")
    class Span {

        @Test
        void throwsOnNullArgument() {
            assertThrows(NullPointerException.class, () -> immutableFiniteIterable(emptyList()).span(null));
        }

        @Test
        void testCase1() {
            Tuple2<? extends ImmutableFiniteIterable<Integer>, ? extends ImmutableFiniteIterable<Integer>> spanResult =
                    immutableFiniteIterable(asList(1, 2, 3, 4, 5)).span(n -> n < 4);
            assertThat(spanResult._1(), contains(1, 2, 3));
            assertThat(spanResult._2(), contains(4, 5));
        }

    }

    @Nested
    @DisplayName("tails")
    class Tails {

        @Test
        void testCase1() {
            assertThat(immutableFiniteIterable(asList(1, 2, 3, 4, 5)).tails(),
                    contains(contains(1, 2, 3, 4, 5), contains(2, 3, 4, 5), contains(3, 4, 5), contains(4, 5),
                            contains(5), IsEmptyIterable.emptyIterable()));
        }

    }

    @Nested
    @DisplayName("take")
    class Take {

        @Test
        void throwsOnNegativeArgument() {
            assertThrows(IllegalArgumentException.class, () -> immutableFiniteIterable(emptyList()).drop(-1));
        }

        @Test
        void countOfZero() {
            ImmutableFiniteIterable<Integer> subject = immutableFiniteIterable(asList(1, 2, 3));
            assertThat(subject.take(0), IsEmptyIterable.emptyIterable());
        }

        @Test
        void countOfOne() {
            ImmutableFiniteIterable<Integer> subject = immutableFiniteIterable(asList(1, 2, 3));
            assertThat(subject.take(1), contains(1));
        }

        @Test
        void countExceedingSize() {
            ImmutableFiniteIterable<Integer> subject = immutableFiniteIterable(asList(1, 2, 3));
            assertThat(subject.take(10000), contains(1, 2, 3));
        }

    }

    @Nested
    @DisplayName("takeWhile")
    class TakeWhile {

        @Test
        void throwsOnNullArgument() {
            assertThrows(NullPointerException.class, () -> immutableFiniteIterable(emptyList()).takeWhile(null));
        }

        @Test
        void predicateNeverTrue() {
            ImmutableFiniteIterable<Integer> subject = immutableFiniteIterable(asList(1, 2, 3));
            assertThat(subject.takeWhile(constantly(false)), IsEmptyIterable.emptyIterable());
        }

        @Test
        void predicateAlwaysTrue() {
            ImmutableFiniteIterable<Integer> subject = immutableFiniteIterable(asList(1, 2, 3));
            assertThat(subject.takeWhile(constantly(true)), contains(1, 2, 3));
        }

        @Test
        void predicateSometimesTrue() {
            ImmutableFiniteIterable<Integer> subject = immutableFiniteIterable(asList(1, 2, 3));
            assertThat(subject.takeWhile(LT.lt(2)), contains(1));
        }

    }

    @Nested
    @DisplayName("toArray")
    class ToArray {

        @Test
        void throwsOnNullArgument() {
            assertThrows(NullPointerException.class, () -> immutableFiniteIterable(emptyList()).toArray(null));
        }

        @Test
        void writesToArray() {
            assertArrayEquals(new Integer[]{1, 2, 3}, immutableFiniteIterable(asList(1, 2, 3)).toArray(Integer[].class));
        }

    }

    @Nested
    @DisplayName("toCollection")
    class ToCollection {

        @Test
        void throwsOnNullArgument() {
            assertThrows(NullPointerException.class, () -> immutableFiniteIterable(emptyList()).toCollection(null));
        }

        @Test
        void toArrayList() {
            assertThat(immutableFiniteIterable(asList(1, 2, 3)).toCollection(ArrayList::new), contains(1, 2, 3));
        }

    }

    @Nested
    @DisplayName("toFinite")
    class ToFinite {

        @Test
        void alwaysSucceeds() {
            ImmutableFiniteIterable<Integer> subject = ImmutableFiniteIterable.copyFrom(asList(1, 2, 3));
            assertSame(subject, subject.toFinite().orElseThrow(AssertionError::new));
        }

    }

    @Nested
    @DisplayName("toNonEmpty")
    class ToNonEmpty {

        @Test
        void successCase() {
            assertTrue(maybeIterablesContainSameElements(
                    just(ImmutableNonEmptyFiniteIterable.of(1, 2, 3)),
                    immutableFiniteIterable(asList(1, 2, 3)).toNonEmpty()));
        }

        @Test
        void failureCase() {
            assertEquals(nothing(), immutableFiniteIterable(emptyList()).toNonEmpty());
        }

    }

    @Nested
    @DisplayName("zipWith")
    class ZipWith {

        @Test
        void throwsOnNullFunction() {
            assertThrows(NullPointerException.class, () -> immutableFiniteIterable(emptyList()).zipWith(null, emptyList()));
        }

        @Test
        void throwsOnNullArgument() {
            assertThrows(NullPointerException.class, () -> immutableFiniteIterable(emptyList())
                    .zipWith(tupler(), (ImmutableFiniteIterable<Object>) null));
        }

        @Test
        void testCase1() {
            ImmutableFiniteIterable<Integer> list1 = immutableFiniteIterable(asList(1, 2, 3, 4, 5));
            ImmutableFiniteIterable<String> list2 = immutableFiniteIterable(asList("foo", "bar", "baz"));
            assertThat(list1.zipWith(tupler(), list2), contains(tuple(1, "foo"), tuple(2, "bar"), tuple(3, "baz")));
        }

    }

}
