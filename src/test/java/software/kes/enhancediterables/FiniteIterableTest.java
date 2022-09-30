package software.kes.enhancediterables;

import com.jnape.palatable.lambda.adt.hlist.Tuple2;
import com.jnape.palatable.lambda.functions.Fn1;
import com.jnape.palatable.lambda.functions.Fn2;
import com.jnape.palatable.lambda.functions.builtin.fn2.LT;
import org.hamcrest.MatcherAssert;
import org.hamcrest.collection.IsEmptyIterable;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import testsupport.IntSequence;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;
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
import static com.jnape.palatable.lambda.functor.builtin.Lazy.lazy;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.emptyIterable;
import static org.hamcrest.collection.IsIterableContainingInOrder.contains;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static testsupport.IterablesContainSameElements.iterablesContainSameElements;
import static testsupport.IterablesContainSameElements.maybeIterablesContainSameElements;
import static testsupport.IterateN.iterateN;

class FiniteIterableTest {

    @Test
    void singletonIteration() {
        MatcherAssert.assertThat(EnhancedIterables.finiteIterable(singletonList(1)), contains(1));
    }

    @Test
    void multipleIteration() {
        MatcherAssert.assertThat(EnhancedIterables.finiteIterable(asList(1, 2, 3, 4, 5, 6)), contains(1, 2, 3, 4, 5, 6));
    }

    @Test
    void iteratorNextReturnsCorrectElements() {
        FiniteIterable<String> subject = EnhancedIterables.finiteIterable(asList("foo", "bar", "baz"));
        Iterator<String> iterator = subject.iterator();
        assertEquals("foo", iterator.next());
        assertEquals("bar", iterator.next());
        assertEquals("baz", iterator.next());
    }

    @SuppressWarnings("ConstantConditions")
    @Test
    void iteratorHasNextCanBeCalledMultipleTimes() {
        FiniteIterable<String> subject = EnhancedIterables.finiteIterable(asList("foo", "bar", "baz"));
        Iterator<String> iterator = subject.iterator();
        assertTrue(iterator.hasNext());
        assertTrue(iterator.hasNext());
        assertTrue(iterator.hasNext());
        assertEquals("foo", iterator.next());
    }

    @Test
    void iteratorHasNextReturnsFalseIfNothingRemains() {
        FiniteIterable<String> subject = EnhancedIterables.finiteIterable(singletonList("foo"));
        Iterator<String> iterator = subject.iterator();
        iterator.next();
        assertFalse(iterator.hasNext());
    }

    @Test
    void iteratorNextThrowsIfNothingRemains() {
        FiniteIterable<String> subject = EnhancedIterables.finiteIterable(singletonList("foo"));
        Iterator<String> iterator = subject.iterator();
        iterator.next();
        assertThrows(NoSuchElementException.class, iterator::next);
    }

    @Test
    void iteratorThrowsIfRemoveIsCalled() {
        FiniteIterable<String> subject = EnhancedIterables.finiteIterable(asList("foo", "bar", "baz"));
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
            MatcherAssert.assertThat(EnhancedIterables.finiteIterable(emptyList()).append("foo"), contains("foo"));
        }

        @Test
        void toSize3() {
            MatcherAssert.assertThat(EnhancedIterables.finiteIterable(asList("foo", "bar", "baz")).append("qux"),
                    contains("foo", "bar", "baz", "qux"));
        }

        @Test
        void stackSafe() {
            assertTimeoutPreemptively(Duration.ofSeconds(5), () -> {
                FiniteIterable<Integer> result = iterateN(50_000,
                        EnhancedIterables.finiteIterable(emptyList()),
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
            assertThrows(NullPointerException.class, () -> EnhancedIterables.finiteIterable(emptyList())
                    .concat((FiniteIterable<Object>) null));
        }

        @Test
        void emptyPlusEmpty() {
            MatcherAssert.assertThat(EnhancedIterables.finiteIterable(emptyList()).concat(emptyList()), IsEmptyIterable.emptyIterable());
        }

        @Test
        void emptyPlusSize3() {
            MatcherAssert.assertThat(EnhancedIterables.finiteIterable(emptyList()).concat(asList("foo", "bar", "baz")),
                    contains("foo", "bar", "baz"));
        }

        @Test
        void size3PlusEmpty() {
            MatcherAssert.assertThat(EnhancedIterables.finiteIterable(asList("foo", "bar", "baz")).concat(emptyList()),
                    contains("foo", "bar", "baz"));
        }

        @Test
        void size3PlusSize3() {
            List<String> underlying = asList("foo", "bar", "baz");
            MatcherAssert.assertThat(EnhancedIterables.finiteIterable(underlying).concat(underlying),
                    contains("foo", "bar", "baz", "foo", "bar", "baz"));
        }

        @Test
        void stackSafe() {
            FiniteIterable<Integer> xs = EnhancedIterables.finiteIterable(asList(0, 1, 2, 3, 4, 5, 6, 7, 8, 9));
            assertTimeoutPreemptively(Duration.ofSeconds(5), () -> {
                FiniteIterable<Integer> result = iterateN(10_000,
                        EnhancedIterables.finiteIterable(emptyList()),
                        acc -> acc.concat(xs));
                assertEquals(100_000, size(result));
            });
        }

    }

    @Nested
    @DisplayName("cross")
    class Cross {

        @Test
        void throwsOnNullArgument() {
            assertThrows(NullPointerException.class, () -> EnhancedIterables.finiteIterable(asList(1, 2, 3))
                    .cross((FiniteIterable<Integer>) null));
        }

        @Test
        void nonEmptyWithEmpty() {
            MatcherAssert.assertThat(EnhancedIterables.finiteIterable(asList(1, 2, 3)).cross(EnhancedIterables.finiteIterable(emptyList())), emptyIterable());
        }

        @Test
        void emptyWithNonEmpty() {
            MatcherAssert.assertThat(EnhancedIterables.finiteIterable(emptyList()).cross(EnhancedIterables.finiteIterable(asList(1, 2, 3))), emptyIterable());
        }

        @Test
        void nonEmptyWithNonEmpty() {
            MatcherAssert.assertThat(EnhancedIterables.finiteIterable(asList(1, 2, 3)).cross(EnhancedIterables.finiteIterable(asList("foo", "bar", "baz"))),
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
            MatcherAssert.assertThat(EnhancedIterables.finiteIterable(emptyList()).cycle(), emptyIterable());
        }

        @Test
        void cycleSingleton() {
            MatcherAssert.assertThat(EnhancedIterables.finiteIterable(singletonList(1)).cycle().drop(10000).take(10),
                    contains(1, 1, 1, 1, 1, 1, 1, 1, 1, 1));
        }

        @Test
        void cycleSize3() {
            MatcherAssert.assertThat(EnhancedIterables.finiteIterable(asList(1, 2, 3)).cycle().drop(9999).take(10),
                    contains(1, 2, 3, 1, 2, 3, 1, 2, 3, 1));
        }

    }

    @Nested
    @DisplayName("distinct")
    class Distinct {

        @Test
        void empty() {
            MatcherAssert.assertThat(EnhancedIterables.finiteIterable(emptyList()).distinct(), emptyIterable());
        }

        @Test
        void removesRepeatedElementsAndRetainsOrder() {
            MatcherAssert.assertThat(EnhancedIterables.finiteIterable(asList(1, 2, 2, 3, 3, 3, 2, 2, 1, 4)).distinct(),
                    contains(1, 2, 3, 4));
        }

    }

    @Nested
    @DisplayName("drop")
    class Drop {

        @Test
        void throwsOnNegativeArgument() {
            assertThrows(IllegalArgumentException.class, () -> EnhancedIterables.finiteIterable(emptyList()).drop(-1));
        }

        @Test
        void countOfZero() {
            FiniteIterable<Integer> subject = EnhancedIterables.finiteIterable(asList(1, 2, 3));
            assertThat(subject.drop(0), contains(1, 2, 3));
        }

        @Test
        void countOfOne() {
            FiniteIterable<Integer> subject = EnhancedIterables.finiteIterable(asList(1, 2, 3));
            assertThat(subject.drop(1), contains(2, 3));
        }

        @Test
        void countExceedingSize() {
            FiniteIterable<Integer> subject = EnhancedIterables.finiteIterable(asList(1, 2, 3));
            assertThat(subject.drop(10000), IsEmptyIterable.emptyIterable());
        }

        @Test
        void stackSafe() {
            assertTimeoutPreemptively(Duration.ofSeconds(5), () -> {
                FiniteIterable<Integer> result = iterateN(10_000,
                        EnhancedIterables.finiteIterable(IntSequence.integers(1, 10_003)),
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
            assertThrows(NullPointerException.class, () -> EnhancedIterables.finiteIterable(emptyList()).dropWhile(null));
        }

        @Test
        void predicateNeverTrue() {
            FiniteIterable<Integer> subject = EnhancedIterables.finiteIterable(asList(1, 2, 3));
            assertThat(subject.dropWhile(constantly(false)), contains(1, 2, 3));
        }

        @Test
        void predicateAlwaysTrue() {
            FiniteIterable<Integer> subject = EnhancedIterables.finiteIterable(asList(1, 2, 3));
            assertThat(subject.dropWhile(constantly(true)), IsEmptyIterable.emptyIterable());
        }

        @Test
        void predicateSometimesTrue() {
            FiniteIterable<Integer> subject = EnhancedIterables.finiteIterable(asList(1, 2, 3));
            assertThat(subject.dropWhile(LT.lt(2)), contains(2, 3));
        }

    }

    @Nested
    @DisplayName("filter")
    class Filter {

        @Test
        void throwsOnNullArgument() {
            assertThrows(NullPointerException.class, () -> EnhancedIterables.finiteIterable(emptyList()).dropWhile(null));
        }

        @Test
        void predicateNeverTrue() {
            FiniteIterable<Integer> subject = EnhancedIterables.finiteIterable(asList(1, 2, 3));
            assertThat(subject.filter(constantly(false)), IsEmptyIterable.emptyIterable());
        }

        @Test
        void predicateAlwaysTrue() {
            FiniteIterable<Integer> subject = EnhancedIterables.finiteIterable(asList(1, 2, 3));
            assertThat(subject.filter(constantly(true)), contains(1, 2, 3));
        }

        @Test
        void predicateSometimesTrue() {
            FiniteIterable<Integer> subject = EnhancedIterables.finiteIterable(asList(1, 2, 3));
            assertThat(subject.filter(n -> n % 2 == 1), contains(1, 3));
        }

        @Test
        void stackSafe() {
            assertTimeoutPreemptively(Duration.ofSeconds(5), () -> {
                FiniteIterable<Integer> result = iterateN(10_000,
                        EnhancedIterables.finiteIterable(IntSequence.integers(1, 10)),
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
            FiniteIterable<Integer> subject = EnhancedIterables.finiteIterable(asList(1, 2, 3));
            assertEquals(nothing(), subject.find(constantly(false)));
        }

        @Test
        void predicateAlwaysTrue() {
            FiniteIterable<Integer> subject = EnhancedIterables.finiteIterable(asList(1, 2, 3));
            assertEquals(just(1), subject.find(constantly(true)));
        }

        @Test
        void predicateSometimesTrue() {
            FiniteIterable<Integer> subject = EnhancedIterables.finiteIterable(asList(1, 2, 3, 4));
            assertEquals(just(2), subject.find(n -> n % 2 == 0));
        }
    }

    @Nested
    @DisplayName("fmap")
    class Fmap {

        @Test
        void throwsOnNullArgument() {
            assertThrows(NullPointerException.class, () -> EnhancedIterables.finiteIterable(emptyList()).fmap(null));
        }

        @Test
        void testCase1() {
            MatcherAssert.assertThat(EnhancedIterables.finiteIterable(asList(1, 2, 3)).fmap(n -> n * 2), contains(2, 4, 6));
        }

        @Test
        void functorIdentity() {
            FiniteIterable<Integer> subject = EnhancedIterables.finiteIterable(asList(1, 2, 3));
            assertTrue(iterablesContainSameElements(subject, subject.fmap(id())));
        }

        @Test
        void functorComposition() {
            FiniteIterable<Integer> subject = EnhancedIterables.finiteIterable(asList(1, 2, 3));
            Fn1<Integer, Integer> f = n -> n * 2;
            Fn1<Integer, String> g = Object::toString;
            assertTrue(iterablesContainSameElements(subject.fmap(f).fmap(g), subject.fmap(f.fmap(g))));
        }

        @Test
        void stackSafe() {
            assertTimeoutPreemptively(Duration.ofSeconds(5), () -> {
                FiniteIterable<Integer> result = iterateN(10_000,
                        EnhancedIterables.finiteIterable(asList(0, 1, 2)),
                        acc -> acc.fmap(x -> x + 1));
                assertThat(result, contains(10_000, 10_001, 10_002));
            });
        }

    }

    @Nested
    @DisplayName("foldLeft")
    class FoldLeft {

        @Test
        void throwsOnNullOperator() {
            FiniteIterable<Integer> ints = EnhancedIterables.finiteIterable(asList(1, 2, 3));
            assertThrows(NullPointerException.class, () -> ints.foldLeft(null, 0));
        }

        @Test
        void onEmpty() {
            FiniteIterable<Integer> ints = EnhancedIterables.finiteIterable(emptyList());
            assertEquals(999, ints.foldLeft(Integer::sum, 999));
        }

        @Test
        void onSize5() {
            FiniteIterable<Integer> ints = EnhancedIterables.finiteIterable(asList(1, 2, 3, 4, 5));
            assertEquals(25, ints.foldLeft(Integer::sum, 10));
        }

    }

    @Nested
    @DisplayName("foldRight")
    class FoldRight {

        @Test
        void throwsOnNullOperator() {
            FiniteIterable<Integer> ints = EnhancedIterables.finiteIterable(asList(1, 2, 3));
            assertThrows(NullPointerException.class, () -> ints.foldRight(null, lazy(0)));
        }

        @Test
        void onEmpty() {
            FiniteIterable<Integer> ints = EnhancedIterables.finiteIterable(emptyList());
            assertEquals(999, ints.foldRight((x, acc) -> acc.fmap(y -> y + x), lazy(999)).value());
        }

        @Test
        void onSize5() {
            FiniteIterable<String> items = EnhancedIterables.finiteIterable(asList("1", "2", "3", "4", "5"));
            assertEquals("6,5,4,3,2,1", items.foldRight((x, acc) -> acc.fmap(s -> s + "," + x), lazy("6")).value());
        }

    }

    @Nested
    @DisplayName("inits")
    class Inits {

        @Test
        void testCase1() {
            MatcherAssert.assertThat(EnhancedIterables.finiteIterable(asList(1, 2, 3, 4, 5)).inits(),
                    contains(emptyIterable(), contains(1), contains(1, 2), contains(1, 2, 3),
                            contains(1, 2, 3, 4), contains(1, 2, 3, 4, 5)));
        }

    }

    @Nested
    @DisplayName("intersperse")
    class Intersperse {

        @Test
        void doesNothingOnEmptyList() {
            MatcherAssert.assertThat(EnhancedIterables.finiteIterable(emptyList()).intersperse("*"), IsEmptyIterable.emptyIterable());
        }

        @Test
        void doesNothingOnSingletonList() {
            MatcherAssert.assertThat(EnhancedIterables.finiteIterable(singletonList("foo")).intersperse("*"),
                    contains("foo"));
        }

        @Test
        void testCase1() {
            MatcherAssert.assertThat(EnhancedIterables.finiteIterable(asList("foo", "bar", "baz")).intersperse("*"),
                    contains("foo", "*", "bar", "*", "baz"));
        }

    }

    @Nested
    @DisplayName("isEmpty")
    class IsEmpty {

        @Test
        void positive() {
            Assertions.assertTrue(EnhancedIterables.finiteIterable(emptyList()).isEmpty());
        }

        @Test
        void negative() {
            Assertions.assertFalse(EnhancedIterables.finiteIterable(asList(1, 2, 3)).isEmpty());
        }

    }

    @Nested
    @DisplayName("magnetizeBy")
    class MagnetizeBy {

        @Test
        void throwsOnNullArgument() {
            assertThrows(NullPointerException.class, () -> EnhancedIterables.finiteIterable(emptyList()).magnetizeBy(null));
        }

        @Test
        void lambdaTestCase() {
            Fn2<Integer, Integer, Boolean> lte = (x, y) -> x <= y;
            MatcherAssert.assertThat(EnhancedIterables.finiteIterable(Collections.<Integer>emptyList()).magnetizeBy(lte), IsEmptyIterable.emptyIterable());
            MatcherAssert.assertThat(EnhancedIterables.finiteIterable(singletonList(1)).magnetizeBy(lte), contains(contains(1)));
            MatcherAssert.assertThat(EnhancedIterables.finiteIterable(asList(1, 2, 3, 2, 2, 3, 2, 1)).magnetizeBy(lte),
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
            assertThrows(NullPointerException.class, () -> EnhancedIterables.finiteIterable(emptyList()).partition(null));
        }

        @Test
        void lambdaTestCase() {
            FiniteIterable<String> strings = EnhancedIterables.finiteIterable(asList("one", "two", "three", "four", "five"));
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
            MatcherAssert.assertThat(EnhancedIterables.finiteIterable(emptyList()).prepend("foo"), contains("foo"));
        }

        @Test
        void toSize3() {
            MatcherAssert.assertThat(EnhancedIterables.finiteIterable(asList("foo", "bar", "baz")).prepend("qux"),
                    contains("qux", "foo", "bar", "baz"));
        }

        @Test
        void stackSafe() {
            assertTimeoutPreemptively(Duration.ofSeconds(5), () -> {
                FiniteIterable<Integer> result = iterateN(50_000,
                        EnhancedIterables.finiteIterable(emptyList()),
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
            MatcherAssert.assertThat(EnhancedIterables.finiteIterable(emptyList()).prependAll("*"), IsEmptyIterable.emptyIterable());
        }

        @Test
        void testCase1() {
            MatcherAssert.assertThat(EnhancedIterables.finiteIterable(singletonList("foo")).prependAll("*"),
                    contains("*", "foo"));
        }

        @Test
        void testCase2() {
            MatcherAssert.assertThat(EnhancedIterables.finiteIterable(asList("foo", "bar", "baz")).prependAll("*"),
                    contains("*", "foo", "*", "bar", "*", "baz"));
        }

    }

    @Nested
    @DisplayName("reverse")
    class Reverse {

        @Test
        void testCase1() {
            MatcherAssert.assertThat(EnhancedIterables.finiteIterable(asList(1, 2, 3, 4, 5)).reverse(), contains(5, 4, 3, 2, 1));
        }

    }

    @Nested
    @DisplayName("size")
    class Size {

        @Test
        void empty() {
            Assertions.assertEquals(0, EnhancedIterables.finiteIterable(emptyList()).size());
        }

        @Test
        void singleton() {
            Assertions.assertEquals(1, EnhancedIterables.finiteIterable(singletonList(1)).size());
        }

        @Test
        void size8() {
            Assertions.assertEquals(8, EnhancedIterables.finiteIterable(asList(1, 2, 3, 4, 5, 6, 7, 8)).size());
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
            Assertions.assertEquals(WEIRD_SIZE_VALUE, EnhancedIterables.finiteIterable(collection).size());
        }

    }

    @Nested
    @DisplayName("slide")
    class Slide {

        @Test
        void throwsOnZeroArgument() {
            assertThrows(IllegalArgumentException.class, () -> EnhancedIterables.finiteIterable(emptyList()).slide(0));
        }

        @Test
        void onEmpty() {
            MatcherAssert.assertThat(EnhancedIterables.finiteIterable(emptyList()).slide(1), IsEmptyIterable.emptyIterable());
        }

        @Test
        void k1() {
            MatcherAssert.assertThat(EnhancedIterables.finiteIterable(asList(0, 1, 2, 3)).slide(1),
                    contains(contains(0), contains(1), contains(2), contains(3)));
        }

        @Test
        void k2() {
            MatcherAssert.assertThat(EnhancedIterables.finiteIterable(asList(0, 1, 2, 3)).slide(2),
                    contains(contains(0, 1), contains(1, 2), contains(2, 3)));
        }

    }

    @Nested
    @DisplayName("span")
    class Span {

        @Test
        void throwsOnNullArgument() {
            assertThrows(NullPointerException.class, () -> EnhancedIterables.finiteIterable(emptyList()).span(null));
        }

        @Test
        void testCase1() {
            Tuple2<? extends FiniteIterable<Integer>, ? extends FiniteIterable<Integer>> spanResult =
                    EnhancedIterables.finiteIterable(asList(1, 2, 3, 4, 5)).span(n -> n < 4);
            assertThat(spanResult._1(), contains(1, 2, 3));
            assertThat(spanResult._2(), contains(4, 5));
        }

    }

    @Nested
    @DisplayName("tails")
    class Tails {

        @Test
        void testCase1() {
            MatcherAssert.assertThat(EnhancedIterables.finiteIterable(asList(1, 2, 3, 4, 5)).tails(),
                    contains(contains(1, 2, 3, 4, 5), contains(2, 3, 4, 5), contains(3, 4, 5), contains(4, 5),
                            contains(5), IsEmptyIterable.emptyIterable()));
        }

    }

    @Nested
    @DisplayName("take")
    class Take {

        @Test
        void throwsOnNegativeArgument() {
            assertThrows(IllegalArgumentException.class, () -> EnhancedIterables.finiteIterable(emptyList()).drop(-1));
        }

        @Test
        void countOfZero() {
            FiniteIterable<Integer> subject = EnhancedIterables.finiteIterable(asList(1, 2, 3));
            assertThat(subject.take(0), IsEmptyIterable.emptyIterable());
        }

        @Test
        void countOfOne() {
            FiniteIterable<Integer> subject = EnhancedIterables.finiteIterable(asList(1, 2, 3));
            assertThat(subject.take(1), contains(1));
        }

        @Test
        void countExceedingSize() {
            FiniteIterable<Integer> subject = EnhancedIterables.finiteIterable(asList(1, 2, 3));
            assertThat(subject.take(10000), contains(1, 2, 3));
        }

    }

    @Nested
    @DisplayName("takeWhile")
    class TakeWhile {

        @Test
        void throwsOnNullArgument() {
            assertThrows(NullPointerException.class, () -> EnhancedIterables.finiteIterable(emptyList()).takeWhile(null));
        }

        @Test
        void predicateNeverTrue() {
            FiniteIterable<Integer> subject = EnhancedIterables.finiteIterable(asList(1, 2, 3));
            assertThat(subject.takeWhile(constantly(false)), IsEmptyIterable.emptyIterable());
        }

        @Test
        void predicateAlwaysTrue() {
            FiniteIterable<Integer> subject = EnhancedIterables.finiteIterable(asList(1, 2, 3));
            assertThat(subject.takeWhile(constantly(true)), contains(1, 2, 3));
        }

        @Test
        void predicateSometimesTrue() {
            FiniteIterable<Integer> subject = EnhancedIterables.finiteIterable(asList(1, 2, 3));
            assertThat(subject.takeWhile(LT.lt(2)), contains(1));
        }

    }

    @Nested
    @DisplayName("toArray")
    class ToArray {

        @Test
        void throwsOnNullArgument() {
            assertThrows(NullPointerException.class, () -> EnhancedIterables.finiteIterable(emptyList()).toArray(null));
        }

        @Test
        void writesToArray() {
            Assertions.assertArrayEquals(new Integer[]{1, 2, 3}, EnhancedIterables.finiteIterable(asList(1, 2, 3)).toArray(Integer[].class));
        }

    }

    @Nested
    @DisplayName("toCollection")
    class ToCollection {

        @Test
        void throwsOnNullArgument() {
            assertThrows(NullPointerException.class, () -> EnhancedIterables.finiteIterable(emptyList()).toCollection(null));
        }

        @Test
        void toArrayList() {
            MatcherAssert.assertThat(EnhancedIterables.finiteIterable(asList(1, 2, 3)).toCollection(ArrayList::new), contains(1, 2, 3));
        }

    }

    @Nested
    @DisplayName("toFinite")
    class ToFinite {

        @Test
        void alwaysSucceeds() {
            FiniteIterable<Integer> subject = EnhancedIterables.finiteIterable(asList(1, 2, 3));
            assertSame(subject, subject.toFinite().orElseThrow(AssertionError::new));
        }

    }

    @Nested
    @DisplayName("toNonEmpty")
    class ToNonEmpty {

        @Test
        void successCase() {
            assertTrue(maybeIterablesContainSameElements(
                    just(EnhancedIterables.nonEmptyFiniteIterable(1, asList(2, 3))),
                    EnhancedIterables.finiteIterable(asList(1, 2, 3)).toNonEmpty()));
        }

        @Test
        void failureCase() {
            Assertions.assertEquals(nothing(), EnhancedIterables.finiteIterable(emptyList()).toNonEmpty());
        }

    }

    @Nested
    @DisplayName("zipWith")
    class ZipWith {

        @Test
        void throwsOnNullFunction() {
            assertThrows(NullPointerException.class, () -> EnhancedIterables.finiteIterable(emptyList()).zipWith(null, emptyList()));
        }

        @Test
        void throwsOnNullArgument() {
            assertThrows(NullPointerException.class, () -> EnhancedIterables.finiteIterable(emptyList())
                    .zipWith(tupler(), (FiniteIterable<Object>) null));
        }

        @Test
        void testCase1() {
            FiniteIterable<Integer> list1 = EnhancedIterables.finiteIterable(asList(1, 2, 3, 4, 5));
            FiniteIterable<String> list2 = EnhancedIterables.finiteIterable(asList("foo", "bar", "baz"));
            assertThat(list1.zipWith(tupler(), list2), contains(tuple(1, "foo"), tuple(2, "bar"), tuple(3, "baz")));
        }

    }

}
