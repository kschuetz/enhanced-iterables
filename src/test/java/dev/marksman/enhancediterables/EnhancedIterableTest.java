package dev.marksman.enhancediterables;

import com.jnape.palatable.lambda.adt.hlist.Tuple2;
import com.jnape.palatable.lambda.functions.builtin.fn2.LT;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.jnape.palatable.lambda.adt.Maybe.just;
import static com.jnape.palatable.lambda.adt.Maybe.nothing;
import static com.jnape.palatable.lambda.adt.choice.Choice2.a;
import static com.jnape.palatable.lambda.adt.choice.Choice2.b;
import static com.jnape.palatable.lambda.functions.builtin.fn1.Constantly.constantly;
import static com.jnape.palatable.lambda.functions.builtin.fn2.Tupler2.tupler;
import static dev.marksman.enhancediterables.EnhancedIterable.enhance;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsEmptyIterable.emptyIterable;
import static org.hamcrest.collection.IsIterableContainingInOrder.contains;
import static org.junit.jupiter.api.Assertions.*;

class EnhancedIterableTest {

    @Nested
    @DisplayName("append")
    class Append {

        @Test
        void toEmpty() {
            assertThat(enhance(emptyList()).append("foo"), contains("foo"));
        }

        @Test
        void toSize3() {
            assertThat(enhance(asList("foo", "bar", "baz")).append("qux"),
                    contains("foo", "bar", "baz", "qux"));
        }

    }

    @Nested
    @DisplayName("concat")
    class Concat {

        @Test
        void throwsOnNullArgument() {
            assertThrows(NullPointerException.class, () -> enhance(emptyList()).concat(null));
        }

        @Test
        void emptyPlusEmpty() {
            assertThat(enhance(emptyList()).concat(emptyList()), emptyIterable());
        }

        @Test
        void emptyPlusSize3() {
            assertThat(enhance(emptyList()).concat(asList("foo", "bar", "baz")),
                    contains("foo", "bar", "baz"));
        }

        @Test
        void size3PlusEmpty() {
            assertThat(enhance(asList("foo", "bar", "baz")).concat(emptyList()),
                    contains("foo", "bar", "baz"));
        }

        @Test
        void size3PlusSize3() {
            List<String> underlying = asList("foo", "bar", "baz");
            assertThat(enhance(underlying).concat(underlying),
                    contains("foo", "bar", "baz", "foo", "bar", "baz"));
        }

    }

    @Nested
    @DisplayName("drop")
    class Drop {

        @Test
        void throwsOnNegativeArgument() {
            assertThrows(IllegalArgumentException.class, () -> enhance(emptyList()).drop(-1));
        }

        @Test
        void countOfZero() {
            EnhancedIterable<Integer> subject = enhance(asList(1, 2, 3));
            assertThat(subject.drop(0), contains(1, 2, 3));
        }

        @Test
        void countOfOne() {
            EnhancedIterable<Integer> subject = enhance(asList(1, 2, 3));
            assertThat(subject.drop(1), contains(2, 3));
        }

        @Test
        void countExceedingSize() {
            EnhancedIterable<Integer> subject = enhance(asList(1, 2, 3));
            assertThat(subject.drop(10000), emptyIterable());
        }

    }

    @Nested
    @DisplayName("dropWhile")
    class DropWhile {

        @Test
        void throwsOnNullArgument() {
            assertThrows(NullPointerException.class, () -> enhance(emptyList()).dropWhile(null));
        }

        @Test
        void predicateNeverTrue() {
            EnhancedIterable<Integer> subject = enhance(asList(1, 2, 3));
            assertThat(subject.dropWhile(constantly(false)), contains(1, 2, 3));
        }

        @Test
        void predicateAlwaysTrue() {
            EnhancedIterable<Integer> subject = enhance(asList(1, 2, 3));
            assertThat(subject.dropWhile(constantly(true)), emptyIterable());
        }

        @Test
        void predicateSometimesTrue() {
            EnhancedIterable<Integer> subject = enhance(asList(1, 2, 3));
            assertThat(subject.dropWhile(LT.lt(2)), contains(2, 3));
        }

    }

    @Nested
    @DisplayName("filter")
    class Filter {

        @Test
        void throwsOnNullArgument() {
            assertThrows(NullPointerException.class, () -> enhance(emptyList()).dropWhile(null));
        }

        @Test
        void predicateNeverTrue() {
            EnhancedIterable<Integer> subject = enhance(asList(1, 2, 3));
            assertThat(subject.filter(constantly(false)), emptyIterable());
        }

        @Test
        void predicateAlwaysTrue() {
            EnhancedIterable<Integer> subject = enhance(asList(1, 2, 3));
            assertThat(subject.filter(constantly(true)), contains(1, 2, 3));
        }

        @Test
        void predicateSometimesTrue() {
            EnhancedIterable<Integer> subject = enhance(asList(1, 2, 3));
            assertThat(subject.filter(n -> n % 2 == 1), contains(1, 3));
        }

    }

    @Nested
    @DisplayName("find")
    class Find {

        @Test
        void predicateNeverTrue() {
            EnhancedIterable<Integer> subject = enhance(asList(1, 2, 3));
            assertEquals(nothing(), subject.find(constantly(false)));
        }

        @Test
        void predicateAlwaysTrue() {
            EnhancedIterable<Integer> subject = enhance(asList(1, 2, 3));
            assertEquals(just(1), subject.find(constantly(true)));
        }

        @Test
        void predicateSometimesTrue() {
            EnhancedIterable<Integer> subject = enhance(asList(1, 2, 3, 4));
            assertEquals(just(2), subject.find(n -> n % 2 == 0));
        }
    }

    @Nested
    @DisplayName("fmap")
    class Fmap {

        @Test
        void throwsOnNullArgument() {
            assertThrows(NullPointerException.class, () -> enhance(emptyList()).fmap(null));
        }

    }

    @Nested
    @DisplayName("intersperse")
    class Intersperse {

    }

    @Nested
    @DisplayName("isEmpty")
    class IsEmpty {

        @Test
        void positive() {
            assertTrue(enhance(emptyList()).isEmpty());
        }

        @Test
        void negative() {
            assertFalse(enhance(asList(1, 2, 3)).isEmpty());
        }

    }

    @Nested
    @DisplayName("partition")
    class Partition {

        @Test
        void throwsOnNullArgument() {
            assertThrows(NullPointerException.class, () -> enhance(emptyList()).partition(null));
        }

        @Test
        void lambdaTestCase() {
            EnhancedIterable<String> strings = enhance(asList("one", "two", "three", "four", "five"));
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
            assertThat(enhance(emptyList()).prepend("foo"), contains("foo"));
        }

        @Test
        void toSize3() {
            assertThat(enhance(asList("foo", "bar", "baz")).prepend("qux"),
                    contains("qux", "foo", "bar", "baz"));
        }

    }

    @Nested
    @DisplayName("prependAll")
    class PrependAll {

    }

    @Nested
    @DisplayName("slide")
    class Slide {

        @Test
        void throwsOnZeroArgument() {
            assertThrows(IllegalArgumentException.class, () -> enhance(emptyList()).slide(0));
        }

        @Test
        void onEmpty() {
            assertThat(enhance(emptyList()).slide(1), emptyIterable());
        }

        @Test
        void k1() {
            assertThat(enhance(asList(0, 1, 2, 3)).slide(1),
                    contains(contains(0), contains(1), contains(2), contains(3)));
        }

        @Test
        void k2() {
            assertThat(enhance(asList(0, 1, 2, 3)).slide(2),
                    contains(contains(0, 1), contains(1, 2), contains(2, 3)));
        }

    }

    @Nested
    @DisplayName("span")
    class Span {

        @Test
        void throwsOnNullArgument() {
            assertThrows(NullPointerException.class, () -> enhance(emptyList()).span(null));
        }

    }

    @Nested
    @DisplayName("tails")
    class Tails {

    }

    @Nested
    @DisplayName("take")
    class Take {

        @Test
        void throwsOnNegativeArgument() {
            assertThrows(IllegalArgumentException.class, () -> enhance(emptyList()).drop(-1));
        }

        @Test
        void countOfZero() {
            EnhancedIterable<Integer> subject = enhance(asList(1, 2, 3));
            assertThat(subject.take(0), emptyIterable());
        }

        @Test
        void countOfOne() {
            EnhancedIterable<Integer> subject = enhance(asList(1, 2, 3));
            assertThat(subject.take(1), contains(1));
        }

        @Test
        void countExceedingSize() {
            EnhancedIterable<Integer> subject = enhance(asList(1, 2, 3));
            assertThat(subject.take(10000), contains(1, 2, 3));
        }

    }

    @Nested
    @DisplayName("takeWhile")
    class TakeWhile {

        @Test
        void throwsOnNullArgument() {
            assertThrows(NullPointerException.class, () -> enhance(emptyList()).takeWhile(null));
        }

        @Test
        void predicateNeverTrue() {
            EnhancedIterable<Integer> subject = enhance(asList(1, 2, 3));
            assertThat(subject.takeWhile(constantly(false)), emptyIterable());
        }

        @Test
        void predicateAlwaysTrue() {
            EnhancedIterable<Integer> subject = enhance(asList(1, 2, 3));
            assertThat(subject.takeWhile(constantly(true)), contains(1, 2, 3));
        }

        @Test
        void predicateSometimesTrue() {
            EnhancedIterable<Integer> subject = enhance(asList(1, 2, 3));
            assertThat(subject.takeWhile(LT.lt(2)), contains(1));
        }

    }

    @Nested
    @DisplayName("toArray")
    class ToArray {

        @Test
        void throwsOnNullArgument() {
            assertThrows(NullPointerException.class, () -> enhance(emptyList()).toArray(null));
        }

    }

    @Nested
    @DisplayName("toCollection")
    class ToCollection {

        @Test
        void throwsOnNullArgument() {
            assertThrows(NullPointerException.class, () -> enhance(emptyList()).toCollection(null));
        }

    }

    @Nested
    @DisplayName("zipWith")
    class ZipWith {

        @Test
        void throwsOnNullFunction() {
            assertThrows(NullPointerException.class, () -> enhance(emptyList()).zipWith(null, emptyList()));
        }

        @Test
        void throwsOnNullArgument() {
            assertThrows(NullPointerException.class, () -> enhance(emptyList()).zipWith(tupler(), null));
        }

    }

}
