package dev.marksman.enhancediterables;

import com.jnape.palatable.lambda.adt.hlist.Tuple2;
import com.jnape.palatable.lambda.functions.Fn1;
import com.jnape.palatable.lambda.functions.Fn2;
import com.jnape.palatable.lambda.functions.builtin.fn2.LT;
import org.hamcrest.collection.IsEmptyIterable;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.*;

import static com.jnape.palatable.lambda.adt.Maybe.just;
import static com.jnape.palatable.lambda.adt.Maybe.nothing;
import static com.jnape.palatable.lambda.adt.choice.Choice2.a;
import static com.jnape.palatable.lambda.adt.choice.Choice2.b;
import static com.jnape.palatable.lambda.adt.hlist.HList.tuple;
import static com.jnape.palatable.lambda.functions.builtin.fn1.Constantly.constantly;
import static com.jnape.palatable.lambda.functions.builtin.fn1.Id.id;
import static com.jnape.palatable.lambda.functions.builtin.fn2.Tupler2.tupler;
import static com.jnape.palatable.lambda.functor.builtin.Lazy.lazy;
import static dev.marksman.enhancediterables.EnhancedIterables.finiteIterable;
import static dev.marksman.enhancediterables.EnhancedIterables.nonEmptyFiniteIterable;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.emptyIterable;
import static org.hamcrest.collection.IsIterableContainingInOrder.contains;
import static org.junit.jupiter.api.Assertions.*;
import static testsupport.IterablesContainSameElements.iterablesContainSameElements;
import static testsupport.IterablesContainSameElements.maybeIterablesContainSameElements;

class FiniteIterableTest {

    @Test
    void singletonIteration() {
        assertThat(finiteIterable(singletonList(1)), contains(1));
    }

    @Test
    void multipleIteration() {
        assertThat(finiteIterable(asList(1, 2, 3, 4, 5, 6)), contains(1, 2, 3, 4, 5, 6));
    }

    @Test
    void iteratorNextReturnsCorrectElements() {
        FiniteIterable<String> subject = finiteIterable(asList("foo", "bar", "baz"));
        Iterator<String> iterator = subject.iterator();
        assertEquals("foo", iterator.next());
        assertEquals("bar", iterator.next());
        assertEquals("baz", iterator.next());
    }

    @SuppressWarnings("ConstantConditions")
    @Test
    void iteratorHasNextCanBeCalledMultipleTimes() {
        FiniteIterable<String> subject = finiteIterable(asList("foo", "bar", "baz"));
        Iterator<String> iterator = subject.iterator();
        assertTrue(iterator.hasNext());
        assertTrue(iterator.hasNext());
        assertTrue(iterator.hasNext());
        assertEquals("foo", iterator.next());
    }

    @Test
    void iteratorHasNextReturnsFalseIfNothingRemains() {
        FiniteIterable<String> subject = finiteIterable(singletonList("foo"));
        Iterator<String> iterator = subject.iterator();
        iterator.next();
        assertFalse(iterator.hasNext());
    }

    @Test
    void iteratorNextThrowsIfNothingRemains() {
        FiniteIterable<String> subject = finiteIterable(singletonList("foo"));
        Iterator<String> iterator = subject.iterator();
        iterator.next();
        assertThrows(NoSuchElementException.class, iterator::next);
    }

    @Test
    void iteratorThrowsIfRemoveIsCalled() {
        FiniteIterable<String> subject = finiteIterable(asList("foo", "bar", "baz"));
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
            assertThat(finiteIterable(emptyList()).append("foo"), contains("foo"));
        }

        @Test
        void toSize3() {
            assertThat(finiteIterable(asList("foo", "bar", "baz")).append("qux"),
                    contains("foo", "bar", "baz", "qux"));
        }

    }

    @Nested
    @DisplayName("concat")
    class Concat {

        @Test
        void throwsOnNullArgument() {
            assertThrows(NullPointerException.class, () -> finiteIterable(emptyList())
                    .concat((FiniteIterable<Object>) null));
        }

        @Test
        void emptyPlusEmpty() {
            assertThat(finiteIterable(emptyList()).concat(emptyList()), IsEmptyIterable.emptyIterable());
        }

        @Test
        void emptyPlusSize3() {
            assertThat(finiteIterable(emptyList()).concat(asList("foo", "bar", "baz")),
                    contains("foo", "bar", "baz"));
        }

        @Test
        void size3PlusEmpty() {
            assertThat(finiteIterable(asList("foo", "bar", "baz")).concat(emptyList()),
                    contains("foo", "bar", "baz"));
        }

        @Test
        void size3PlusSize3() {
            List<String> underlying = asList("foo", "bar", "baz");
            assertThat(finiteIterable(underlying).concat(underlying),
                    contains("foo", "bar", "baz", "foo", "bar", "baz"));
        }

    }

    @Nested
    @DisplayName("cross")
    class Cross {

        @Test
        void throwsOnNullArgument() {
            assertThrows(NullPointerException.class, () -> finiteIterable(asList(1, 2, 3))
                    .cross((FiniteIterable<Integer>) null));
        }

        @Test
        void nonEmptyWithEmpty() {
            assertThat(finiteIterable(asList(1, 2, 3)).cross(finiteIterable(emptyList())), emptyIterable());
        }

        @Test
        void emptyWithNonEmpty() {
            assertThat(finiteIterable(emptyList()).cross(finiteIterable(asList(1, 2, 3))), emptyIterable());
        }

        @Test
        void nonEmptyWithNonEmpty() {
            assertThat(finiteIterable(asList(1, 2, 3)).cross(finiteIterable(asList("foo", "bar", "baz"))),
                    contains(tuple(1, "foo"), tuple(1, "bar"), tuple(1, "baz"),
                            tuple(2, "foo"), tuple(2, "bar"), tuple(2, "baz"),
                            tuple(3, "foo"), tuple(3, "bar"), tuple(3, "baz")));
        }

    }

    @Nested
    @DisplayName("cycle")
    class Cycle {

        @Test
        void cycleEmpty() {
            assertThat(finiteIterable(emptyList()).cycle(), emptyIterable());
        }

        @Test
        void cycleSingleton() {
            assertThat(finiteIterable(singletonList(1)).cycle().drop(10000).take(10),
                    contains(1, 1, 1, 1, 1, 1, 1, 1, 1, 1));
        }

        @Test
        void cycleSize3() {
            assertThat(finiteIterable(asList(1, 2, 3)).cycle().drop(9999).take(10),
                    contains(1, 2, 3, 1, 2, 3, 1, 2, 3, 1));
        }

    }

    @Nested
    @DisplayName("distinct")
    class Distinct {

        @Test
        void empty() {
            assertThat(finiteIterable(emptyList()).distinct(), emptyIterable());
        }

        @Test
        void removesRepeatedElementsAndRetainsOrder() {
            assertThat(finiteIterable(asList(1, 2, 2, 3, 3, 3, 2, 2, 1, 4)).distinct(),
                    contains(1, 2, 3, 4));
        }

    }

    @Nested
    @DisplayName("drop")
    class Drop {

        @Test
        void throwsOnNegativeArgument() {
            assertThrows(IllegalArgumentException.class, () -> finiteIterable(emptyList()).drop(-1));
        }

        @Test
        void countOfZero() {
            FiniteIterable<Integer> subject = finiteIterable(asList(1, 2, 3));
            assertThat(subject.drop(0), contains(1, 2, 3));
        }

        @Test
        void countOfOne() {
            FiniteIterable<Integer> subject = finiteIterable(asList(1, 2, 3));
            assertThat(subject.drop(1), contains(2, 3));
        }

        @Test
        void countExceedingSize() {
            FiniteIterable<Integer> subject = finiteIterable(asList(1, 2, 3));
            assertThat(subject.drop(10000), IsEmptyIterable.emptyIterable());
        }

    }

    @Nested
    @DisplayName("dropWhile")
    class DropWhile {

        @Test
        void throwsOnNullArgument() {
            assertThrows(NullPointerException.class, () -> finiteIterable(emptyList()).dropWhile(null));
        }

        @Test
        void predicateNeverTrue() {
            FiniteIterable<Integer> subject = finiteIterable(asList(1, 2, 3));
            assertThat(subject.dropWhile(constantly(false)), contains(1, 2, 3));
        }

        @Test
        void predicateAlwaysTrue() {
            FiniteIterable<Integer> subject = finiteIterable(asList(1, 2, 3));
            assertThat(subject.dropWhile(constantly(true)), IsEmptyIterable.emptyIterable());
        }

        @Test
        void predicateSometimesTrue() {
            FiniteIterable<Integer> subject = finiteIterable(asList(1, 2, 3));
            assertThat(subject.dropWhile(LT.lt(2)), contains(2, 3));
        }

    }

    @Nested
    @DisplayName("filter")
    class Filter {

        @Test
        void throwsOnNullArgument() {
            assertThrows(NullPointerException.class, () -> finiteIterable(emptyList()).dropWhile(null));
        }

        @Test
        void predicateNeverTrue() {
            FiniteIterable<Integer> subject = finiteIterable(asList(1, 2, 3));
            assertThat(subject.filter(constantly(false)), IsEmptyIterable.emptyIterable());
        }

        @Test
        void predicateAlwaysTrue() {
            FiniteIterable<Integer> subject = finiteIterable(asList(1, 2, 3));
            assertThat(subject.filter(constantly(true)), contains(1, 2, 3));
        }

        @Test
        void predicateSometimesTrue() {
            FiniteIterable<Integer> subject = finiteIterable(asList(1, 2, 3));
            assertThat(subject.filter(n -> n % 2 == 1), contains(1, 3));
        }

    }

    @Nested
    @DisplayName("find")
    class Find {

        @Test
        void predicateNeverTrue() {
            FiniteIterable<Integer> subject = finiteIterable(asList(1, 2, 3));
            assertEquals(nothing(), subject.find(constantly(false)));
        }

        @Test
        void predicateAlwaysTrue() {
            FiniteIterable<Integer> subject = finiteIterable(asList(1, 2, 3));
            assertEquals(just(1), subject.find(constantly(true)));
        }

        @Test
        void predicateSometimesTrue() {
            FiniteIterable<Integer> subject = finiteIterable(asList(1, 2, 3, 4));
            assertEquals(just(2), subject.find(n -> n % 2 == 0));
        }
    }

    @Nested
    @DisplayName("fmap")
    class Fmap {

        @Test
        void throwsOnNullArgument() {
            assertThrows(NullPointerException.class, () -> finiteIterable(emptyList()).fmap(null));
        }

        @Test
        void testCase1() {
            assertThat(finiteIterable(asList(1, 2, 3)).fmap(n -> n * 2), contains(2, 4, 6));
        }

        @Test
        void functorIdentity() {
            FiniteIterable<Integer> subject = finiteIterable(asList(1, 2, 3));
            assertTrue(iterablesContainSameElements(subject, subject.fmap(id())));
        }

        @Test
        void functorComposition() {
            FiniteIterable<Integer> subject = finiteIterable(asList(1, 2, 3));
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

    @Nested
    @DisplayName("foldRight")
    class FoldRight {

        @Test
        void throwsOnNullOperator() {
            FiniteIterable<Integer> ints = finiteIterable(asList(1, 2, 3));
            assertThrows(NullPointerException.class, () -> ints.foldRight(null, lazy(0)));
        }

        @Test
        void onEmpty() {
            FiniteIterable<Integer> ints = finiteIterable(emptyList());
            assertEquals(999, ints.foldRight((x, acc) -> acc.fmap(y -> y + x), lazy(999)).value());
        }

        @Test
        void onSize5() {
            FiniteIterable<String> items = finiteIterable(asList("1", "2", "3", "4", "5"));
            assertEquals("6,5,4,3,2,1", items.foldRight((x, acc) -> acc.fmap(s -> s + "," + x), lazy("6")).value());
        }

    }

    @Nested
    @DisplayName("inits")
    class Inits {

        @Test
        void testCase1() {
            assertThat(finiteIterable(asList(1, 2, 3, 4, 5)).inits(),
                    contains(emptyIterable(), contains(1), contains(1, 2), contains(1, 2, 3),
                            contains(1, 2, 3, 4), contains(1, 2, 3, 4, 5)));
        }

    }

    @Nested
    @DisplayName("intersperse")
    class Intersperse {

        @Test
        void doesNothingOnEmptyList() {
            assertThat(finiteIterable(emptyList()).intersperse("*"), IsEmptyIterable.emptyIterable());
        }

        @Test
        void doesNothingOnSingletonList() {
            assertThat(finiteIterable(singletonList("foo")).intersperse("*"),
                    contains("foo"));
        }

        @Test
        void testCase1() {
            assertThat(finiteIterable(asList("foo", "bar", "baz")).intersperse("*"),
                    contains("foo", "*", "bar", "*", "baz"));
        }

    }

    @Nested
    @DisplayName("isEmpty")
    class IsEmpty {

        @Test
        void positive() {
            assertTrue(finiteIterable(emptyList()).isEmpty());
        }

        @Test
        void negative() {
            assertFalse(finiteIterable(asList(1, 2, 3)).isEmpty());
        }

    }

    @Nested
    @DisplayName("magnetizeBy")
    class MagnetizeBy {

        @Test
        void throwsOnNullArgument() {
            assertThrows(NullPointerException.class, () -> finiteIterable(emptyList()).magnetizeBy(null));
        }

        @Test
        void lambdaTestCase() {
            Fn2<Integer, Integer, Boolean> lte = (x, y) -> x <= y;
            assertThat(finiteIterable(Collections.<Integer>emptyList()).magnetizeBy(lte), IsEmptyIterable.emptyIterable());
            assertThat(finiteIterable(singletonList(1)).magnetizeBy(lte), contains(contains(1)));
            assertThat(finiteIterable(asList(1, 2, 3, 2, 2, 3, 2, 1)).magnetizeBy(lte),
                    contains(contains(1, 2, 3),
                            contains(2, 2, 3),
                            contains(2),
                            contains(1)));
        }

    }

    @Nested
    @DisplayName("partition")
    class Partition {

        @Test
        void throwsOnNullArgument() {
            assertThrows(NullPointerException.class, () -> finiteIterable(emptyList()).partition(null));
        }

        @Test
        void lambdaTestCase() {
            FiniteIterable<String> strings = finiteIterable(asList("one", "two", "three", "four", "five"));
            Tuple2<? extends FiniteIterable<String>, ? extends FiniteIterable<Integer>> partition =
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
            assertThat(finiteIterable(emptyList()).prepend("foo"), contains("foo"));
        }

        @Test
        void toSize3() {
            assertThat(finiteIterable(asList("foo", "bar", "baz")).prepend("qux"),
                    contains("qux", "foo", "bar", "baz"));
        }

    }

    @Nested
    @DisplayName("prependAll")
    class PrependAll {

        @Test
        void doesNothingOnEmptyList() {
            assertThat(finiteIterable(emptyList()).prependAll("*"), IsEmptyIterable.emptyIterable());
        }

        @Test
        void testCase1() {
            assertThat(finiteIterable(singletonList("foo")).prependAll("*"),
                    contains("*", "foo"));
        }

        @Test
        void testCase2() {
            assertThat(finiteIterable(asList("foo", "bar", "baz")).prependAll("*"),
                    contains("*", "foo", "*", "bar", "*", "baz"));
        }

    }

    @Nested
    @DisplayName("reverse")
    class Reverse {

        @Test
        void testCase1() {
            assertThat(finiteIterable(asList(1, 2, 3, 4, 5)).reverse(), contains(5, 4, 3, 2, 1));
        }

    }

    @Nested
    @DisplayName("size")
    class Size {

        @Test
        void empty() {
            assertEquals(0, finiteIterable(emptyList()).size());
        }

        @Test
        void singleton() {
            assertEquals(1, finiteIterable(singletonList(1)).size());
        }

        @Test
        void size8() {
            assertEquals(8, finiteIterable(asList(1, 2, 3, 4, 5, 6, 7, 8)).size());
        }

        @Test
        void delegatesToUnderlyingCollection() {
            int WEIRD_SIZE_VALUE = 1234567;
            Collection<Integer> collection = new Collection<Integer>() {
                @Override
                public int size() {
                    return WEIRD_SIZE_VALUE;
                }

                @Override
                public boolean isEmpty() {
                    return false;
                }

                @Override
                public boolean contains(Object o) {
                    return false;
                }

                @Override
                public Iterator<Integer> iterator() {
                    return null;
                }

                @Override
                public Object[] toArray() {
                    return new Object[0];
                }

                @Override
                public <T> T[] toArray(T[] a) {
                    return null;
                }

                @Override
                public boolean add(Integer integer) {
                    return false;
                }

                @Override
                public boolean remove(Object o) {
                    return false;
                }

                @Override
                public boolean containsAll(Collection<?> c) {
                    return false;
                }

                @Override
                public boolean addAll(Collection<? extends Integer> c) {
                    return false;
                }

                @Override
                public boolean removeAll(Collection<?> c) {
                    return false;
                }

                @Override
                public boolean retainAll(Collection<?> c) {
                    return false;
                }

                @Override
                public void clear() {

                }
            };
            assertEquals(WEIRD_SIZE_VALUE, finiteIterable(collection).size());
        }

    }

    @Nested
    @DisplayName("slide")
    class Slide {

        @Test
        void throwsOnZeroArgument() {
            assertThrows(IllegalArgumentException.class, () -> finiteIterable(emptyList()).slide(0));
        }

        @Test
        void onEmpty() {
            assertThat(finiteIterable(emptyList()).slide(1), IsEmptyIterable.emptyIterable());
        }

        @Test
        void k1() {
            assertThat(finiteIterable(asList(0, 1, 2, 3)).slide(1),
                    contains(contains(0), contains(1), contains(2), contains(3)));
        }

        @Test
        void k2() {
            assertThat(finiteIterable(asList(0, 1, 2, 3)).slide(2),
                    contains(contains(0, 1), contains(1, 2), contains(2, 3)));
        }

    }

    @Nested
    @DisplayName("span")
    class Span {

        @Test
        void throwsOnNullArgument() {
            assertThrows(NullPointerException.class, () -> finiteIterable(emptyList()).span(null));
        }

        @Test
        void testCase1() {
            Tuple2<? extends FiniteIterable<Integer>, ? extends FiniteIterable<Integer>> spanResult =
                    finiteIterable(asList(1, 2, 3, 4, 5)).span(n -> n < 4);
            assertThat(spanResult._1(), contains(1, 2, 3));
            assertThat(spanResult._2(), contains(4, 5));
        }

    }

    @Nested
    @DisplayName("tails")
    class Tails {

        @Test
        void testCase1() {
            assertThat(finiteIterable(asList(1, 2, 3, 4, 5)).tails(),
                    contains(contains(1, 2, 3, 4, 5), contains(2, 3, 4, 5), contains(3, 4, 5), contains(4, 5),
                            contains(5), IsEmptyIterable.emptyIterable()));
        }

    }

    @Nested
    @DisplayName("take")
    class Take {

        @Test
        void throwsOnNegativeArgument() {
            assertThrows(IllegalArgumentException.class, () -> finiteIterable(emptyList()).drop(-1));
        }

        @Test
        void countOfZero() {
            FiniteIterable<Integer> subject = finiteIterable(asList(1, 2, 3));
            assertThat(subject.take(0), IsEmptyIterable.emptyIterable());
        }

        @Test
        void countOfOne() {
            FiniteIterable<Integer> subject = finiteIterable(asList(1, 2, 3));
            assertThat(subject.take(1), contains(1));
        }

        @Test
        void countExceedingSize() {
            FiniteIterable<Integer> subject = finiteIterable(asList(1, 2, 3));
            assertThat(subject.take(10000), contains(1, 2, 3));
        }

    }

    @Nested
    @DisplayName("takeWhile")
    class TakeWhile {

        @Test
        void throwsOnNullArgument() {
            assertThrows(NullPointerException.class, () -> finiteIterable(emptyList()).takeWhile(null));
        }

        @Test
        void predicateNeverTrue() {
            FiniteIterable<Integer> subject = finiteIterable(asList(1, 2, 3));
            assertThat(subject.takeWhile(constantly(false)), IsEmptyIterable.emptyIterable());
        }

        @Test
        void predicateAlwaysTrue() {
            FiniteIterable<Integer> subject = finiteIterable(asList(1, 2, 3));
            assertThat(subject.takeWhile(constantly(true)), contains(1, 2, 3));
        }

        @Test
        void predicateSometimesTrue() {
            FiniteIterable<Integer> subject = finiteIterable(asList(1, 2, 3));
            assertThat(subject.takeWhile(LT.lt(2)), contains(1));
        }

    }

    @Nested
    @DisplayName("toArray")
    class ToArray {

        @Test
        void throwsOnNullArgument() {
            assertThrows(NullPointerException.class, () -> finiteIterable(emptyList()).toArray(null));
        }

        @Test
        void writesToArray() {
            assertArrayEquals(new Integer[]{1, 2, 3}, finiteIterable(asList(1, 2, 3)).toArray(Integer[].class));
        }

    }

    @Nested
    @DisplayName("toCollection")
    class ToCollection {

        @Test
        void throwsOnNullArgument() {
            assertThrows(NullPointerException.class, () -> finiteIterable(emptyList()).toCollection(null));
        }

        @Test
        void toArrayList() {
            assertThat(finiteIterable(asList(1, 2, 3)).toCollection(ArrayList::new), contains(1, 2, 3));
        }

    }

    @Nested
    @DisplayName("toFinite")
    class ToFinite {

        @Test
        void alwaysSucceeds() {
            FiniteIterable<Integer> subject = finiteIterable(asList(1, 2, 3));
            assertSame(subject, subject.toFinite().orElseThrow(AssertionError::new));
        }

    }

    @Nested
    @DisplayName("toNonEmpty")
    class ToNonEmpty {

        @Test
        void successCase() {
            assertTrue(maybeIterablesContainSameElements(
                    just(nonEmptyFiniteIterable(1, asList(2, 3))),
                    finiteIterable(asList(1, 2, 3)).toNonEmpty()));
        }

        @Test
        void failureCase() {
            assertEquals(nothing(), finiteIterable(emptyList()).toNonEmpty());
        }

    }

    @Nested
    @DisplayName("zipWith")
    class ZipWith {

        @Test
        void throwsOnNullFunction() {
            assertThrows(NullPointerException.class, () -> finiteIterable(emptyList()).zipWith(null, emptyList()));
        }

        @Test
        void throwsOnNullArgument() {
            assertThrows(NullPointerException.class, () -> finiteIterable(emptyList())
                    .zipWith(tupler(), (FiniteIterable<Object>) null));
        }

        @Test
        void testCase1() {
            FiniteIterable<Integer> list1 = finiteIterable(asList(1, 2, 3, 4, 5));
            FiniteIterable<String> list2 = finiteIterable(asList("foo", "bar", "baz"));
            assertThat(list1.zipWith(tupler(), list2), contains(tuple(1, "foo"), tuple(2, "bar"), tuple(3, "baz")));
        }

    }

}
