package dev.marksman.enhancediterables;

import com.jnape.palatable.lambda.adt.Maybe;
import com.jnape.palatable.lambda.adt.coproduct.CoProduct2;
import com.jnape.palatable.lambda.adt.hlist.Tuple2;
import com.jnape.palatable.lambda.functions.Fn0;
import com.jnape.palatable.lambda.functions.Fn1;
import com.jnape.palatable.lambda.functions.Fn2;
import com.jnape.palatable.lambda.functions.builtin.fn1.Tails;
import com.jnape.palatable.lambda.functions.builtin.fn2.*;
import com.jnape.palatable.lambda.functions.builtin.fn3.ZipWith;
import com.jnape.palatable.lambda.functor.Functor;
import com.jnape.palatable.lambda.monoid.builtin.Concat;

import java.util.Collection;

import static com.jnape.palatable.lambda.adt.hlist.HList.tuple;
import static dev.marksman.enhancediterables.EnhancedIterables.finiteIterable;
import static dev.marksman.enhancediterables.EnhancedIterables.nonEmptyIterableOrThrow;
import static dev.marksman.enhancediterables.ProtectedIterator.protectedIterator;
import static dev.marksman.enhancediterables.Validation.*;
import static java.util.Objects.requireNonNull;

/**
 * An {@code Iterable} with some additional methods.
 * <p>
 * May be infinite, finite, or empty.
 * <p>
 * Any {@link Iterable} can be upgraded to an {@code EnhancedIterable} by calling {@link EnhancedIterable#enhance(Iterable)}}.
 *
 * @param <A> the element type
 */
public interface EnhancedIterable<A> extends Iterable<A>, Functor<A, EnhancedIterable<?>> {

    /**
     * Lazily appends an element to the end of this {@code EnhancedIterable}, yielding a new {@code NonEmptyIterable}.
     *
     * @param element the element to append
     * @return a {@link NonEmptyIterable<A>}
     */
    default NonEmptyIterable<A> append(A element) {
        return nonEmptyIterableOrThrow(Snoc.snoc(element, this));
    }

    /**
     * Lazily concatenates another {@code Iterable} to the end of this {@code EnhancedIterable},
     * yielding a new {@code EnhancedIterable}.
     *
     * @param other the other {@link Iterable}
     * @return an {@link EnhancedIterable<A>}
     */
    default EnhancedIterable<A> concat(Iterable<A> other) {
        requireNonNull(other);
        return enhance(Concat.concat(this, other));
    }

    /**
     * Lazily concatenates a {@code NonEmptyIterable} to the end of this {@code EnhancedIterable},
     * yielding a new {@code NonEmptyIterable}.
     *
     * @param other a {@link NonEmptyIterable}
     * @return a {@code NonEmptyIterable<A>}
     */
    default NonEmptyIterable<A> concat(NonEmptyIterable<A> other) {
        requireNonNull(other);
        return nonEmptyIterableOrThrow(Concat.concat(this, other));
    }

    /**
     * Returns a new {@code EnhancedIterable} that drops the first {@code count} elements of this {@code EnhancedIterable}.
     *
     * @param count the number of elements to drop from this {@code EnhancedIterable}.
     *              Must be &gt;= 0.
     *              May exceed size of this {@code EnhancedIterable}, in which case, the result will be an
     *              empty {@code EnhancedIterable}.
     * @return an {@code EnhancedIterable<A>}
     */
    default EnhancedIterable<A> drop(int count) {
        validateDrop(count);
        return enhance(Drop.drop(count, this));
    }

    /**
     * Returns a new {@code EnhancedIterable} that skips the first contiguous group of elements of this
     * {@code EnhancedIterable} that satisfy a predicate.
     * <p>
     * Iteration begins at the first element for which the predicate evaluates to false.
     *
     * @param predicate the predicate; should be referentially transparent and not have side-effects
     * @return an {@link EnhancedIterable<A>}
     */
    default EnhancedIterable<A> dropWhile(Fn1<? super A, ? extends Boolean> predicate) {
        requireNonNull(predicate);
        return enhance(DropWhile.dropWhile(predicate, this));
    }

    /**
     * Returns a new {@code EnhancedIterable} that contains all elements of this {@code EnhancedIterable}
     * that satisfy a predicate.
     *
     * @param predicate the predicate; should be referentially transparent and not have side-effects
     * @return an {@link EnhancedIterable<A>}
     */
    default EnhancedIterable<A> filter(Fn1<? super A, ? extends Boolean> predicate) {
        requireNonNull(predicate);
        return enhance(Filter.<A>filter(predicate).apply(this));
    }

    /**
     * Finds the first element of this {@code EnhancedIterable} that satisfies a predicate, if any.
     *
     * @param predicate a predicate; not null
     * @return an element wrapped in a {@link Maybe#just} if a matching element is found;
     * {@link Maybe#nothing} otherwise.
     */
    default Maybe<A> find(Fn1<? super A, ? extends Boolean> predicate) {
        requireNonNull(predicate);
        return Find.find(predicate, this);
    }

    /**
     * Returns a new {@code EnhancedIterable} by applying a function to all elements of this {@code EnhancedIterable}.
     *
     * @param f   a function from {@code A} to {@code B}.
     *            This function should be referentially transparent and not perform side-effects.
     *            It may be called zero or more times for each element.
     * @param <B> the type returned by {@code f}
     * @return an {@link EnhancedIterable<B>}
     */
    default <B> EnhancedIterable<B> fmap(Fn1<? super A, ? extends B> f) {
        requireNonNull(f);
        return enhance(Map.map(f, this));
    }

    default EnhancedIterable<A> intersperse(A a) {
        return enhance(Intersperse.intersperse(a, this));
    }

    default boolean isEmpty() {
        return !iterator().hasNext();
    }

    /**
     * Partitions this {@code EnhancedIterable} given a disjoint mapping function.
     * <p>
     * Note that while the returned tuple must be constructed eagerly, the left and right iterables contained therein
     * are both lazy, so comprehension over infinite iterables is supported.
     *
     * @param function the mapping function
     * @param <B>      The output left Iterable element type, as well as the CoProduct2 A type
     * @param <C>      The output right Iterable element type, as well as the CoProduct2 B type
     * @return a <code>Tuple2&lt;EnhancedIterable&lt;B&gt;, EnhancedIterable&lt;C&gt;&gt;</code>
     */
    default <B, C> Tuple2<? extends EnhancedIterable<B>, ? extends EnhancedIterable<C>> partition(
            Fn1<? super A, ? extends CoProduct2<B, C, ?>> function) {
        requireNonNull(function);
        Tuple2<Iterable<B>, Iterable<C>> partitionResult = Partition.partition(function, this);
        return tuple(enhance(partitionResult._1()), enhance(partitionResult._2()));
    }

    default NonEmptyIterable<A> prepend(A element) {
        return NonEmptyIterable.nonEmptyIterable(element, this);
    }

    default EnhancedIterable<A> prependAll(A a) {
        return enhance(PrependAll.prependAll(a, this));
    }

    /**
     * "Slide" a window of {@code k} elements across the {@code EnhancedIterable} by one element at a time.
     * <p>
     * Example:
     *
     * <code>EnhancedIterable.of(1, 2, 3, 4, 5).slide(2); // [[1, 2], [2, 3], [3, 4], [4, 5]]</code>
     *
     * @param k the number of elements in the sliding window.  Must be &gt;= 1.
     * @return an {@code EnhancedIterable<NonEmptyFiniteIterable<A>>}
     */
    default EnhancedIterable<? extends NonEmptyFiniteIterable<A>> slide(int k) {
        validateSlide(k);
        return enhance(Map.map(EnhancedIterables::nonEmptyFiniteIterableOrThrow,
                Slide.slide(k, this)));
    }

    default Tuple2<? extends EnhancedIterable<A>, ? extends EnhancedIterable<A>> span(Fn1<? super A, ? extends Boolean> predicate) {
        requireNonNull(predicate);
        Tuple2<Iterable<A>, Iterable<A>> spanResult = Span.<A>span(predicate).apply(this);
        return tuple(enhance(spanResult._1()), enhance(spanResult._2()));
    }

    default NonEmptyIterable<? extends EnhancedIterable<A>> tails() {
        return nonEmptyIterableOrThrow(Map.map(EnhancedIterable::enhance, Tails.tails(this)));
    }

    default FiniteIterable<A> take(int count) {
        validateTake(count);
        return finiteIterable(Take.take(count, this));
    }

    default EnhancedIterable<A> takeWhile(Fn1<? super A, ? extends Boolean> predicate) {
        requireNonNull(predicate);
        return enhance(TakeWhile.takeWhile(predicate, this));
    }

    default A[] toArray(Class<A[]> arrayType) {
        requireNonNull(arrayType);
        return ToArray.toArray(arrayType).apply(this);
    }

    default <C extends Collection<A>> C toCollection(Fn0<C> cSupplier) {
        requireNonNull(cSupplier);
        return ToCollection.toCollection(cSupplier).apply(this);
    }

    default <B, C> EnhancedIterable<C> zipWith(Fn2<A, B, C> fn, Iterable<B> other) {
        requireNonNull(fn);
        requireNonNull(other);
        return enhance(ZipWith.zipWith(fn, this, other));
    }

    static <A> EnhancedIterable<A> enhance(Iterable<A> underlying) {
        requireNonNull(underlying);
        if (underlying instanceof EnhancedIterable<?>) {
            return (EnhancedIterable<A>) underlying;
        } else {
            return () -> protectedIterator(underlying.iterator());
        }
    }

    @SafeVarargs
    static <A> ImmutableNonEmptyFiniteIterable<A> of(A first, A... more) {
        return EnhancedIterables.of(first, more);
    }

}
