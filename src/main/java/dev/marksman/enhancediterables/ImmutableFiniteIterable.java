package dev.marksman.enhancediterables;

import com.jnape.palatable.lambda.adt.Maybe;
import com.jnape.palatable.lambda.adt.coproduct.CoProduct2;
import com.jnape.palatable.lambda.adt.hlist.Tuple2;
import com.jnape.palatable.lambda.functions.Fn1;
import com.jnape.palatable.lambda.functions.Fn2;
import com.jnape.palatable.lambda.functions.builtin.fn1.Inits;
import com.jnape.palatable.lambda.functions.builtin.fn1.Reverse;
import com.jnape.palatable.lambda.functions.builtin.fn1.Tails;
import com.jnape.palatable.lambda.functions.builtin.fn2.*;
import com.jnape.palatable.lambda.functions.builtin.fn3.ZipWith;
import com.jnape.palatable.lambda.monoid.builtin.Concat;

import java.util.Collection;

import static com.jnape.palatable.lambda.adt.Maybe.just;
import static com.jnape.palatable.lambda.adt.hlist.HList.tuple;
import static dev.marksman.enhancediterables.EnhancedIterables.*;
import static dev.marksman.enhancediterables.Validation.validateDrop;
import static dev.marksman.enhancediterables.Validation.validateSlide;
import static java.util.Objects.requireNonNull;

/**
 * An {@code EnhancedIterable} that is both finite and safe from mutation.
 *
 * @param <A> the element type
 */
public interface ImmutableFiniteIterable<A> extends ImmutableIterable<A>, FiniteIterable<A> {

    /**
     * Lazily appends an element to the end of this {@code ImmutableFiniteIterable}, yielding a new {@code ImmutableNonEmptyFiniteIterable}.
     *
     * @param element the element to append
     * @return an {@code ImmutableNonEmptyFiniteIterable<A>}
     */
    @Override
    default ImmutableNonEmptyFiniteIterable<A> append(A element) {
        return immutableNonEmptyFiniteIterableOrThrow(Snoc.snoc(element, this));
    }

    /**
     * Lazily concatenates another {@code ImmutableFiniteIterable} to the end of this {@code ImmutableFiniteIterable},
     * yielding a new {@code ImmutableFiniteIterable}.
     *
     * @param other the other {@link ImmutableFiniteIterable}
     * @return an {@code ImmutableFiniteIterable<A>}
     */
    default ImmutableFiniteIterable<A> concat(ImmutableFiniteIterable<A> other) {
        requireNonNull(other);
        return immutableFiniteIterable(Concat.concat(this, other));
    }

    /**
     * Lazily concatenates an {@code ImmutableNonEmptyFiniteIterable} to the end of this {@code ImmutableFiniteIterable},
     * yielding a new {@code ImmutableNonEmptyFiniteIterable}.
     *
     * @param other an {@link ImmutableNonEmptyFiniteIterable}
     * @return an {@code ImmutableNonEmptyFiniteIterable<A>}
     */
    default ImmutableNonEmptyFiniteIterable<A> concat(ImmutableNonEmptyFiniteIterable<A> other) {
        requireNonNull(other);
        return immutableNonEmptyFiniteIterableOrThrow(Concat.concat(this, other));
    }

    /**
     * Returns the lazily computed cartesian product of this {@code ImmutableFiniteIterable} with another {@code ImmutableFiniteIterable}.
     *
     * @param other an {@code ImmutableFiniteIterable} of any type
     * @param <B>   the type of the other {@code ImmutableFiniteIterable}
     * @return a {@code ImmutableFiniteIterable<Tuple2<A, B>>}
     */
    default <B> ImmutableFiniteIterable<Tuple2<A, B>> cross(ImmutableFiniteIterable<B> other) {
        requireNonNull(other);
        return immutableFiniteIterable(CartesianProduct.cartesianProduct(this, other));
    }

    /**
     * Returns a new {@code ImmutableFiniteIterable} that drops the first {@code count} elements of this {@code ImmutableFiniteIterable}.
     *
     * @param count the number of elements to drop from this {@code ImmutableFiniteIterable}.
     *              Must be &gt;= 0.
     *              May exceed size of this {@code ImmutableFiniteIterable}, in which case, the result will be an
     *              empty {@code ImmutableFiniteIterable}.
     * @return an {@code ImmutableFiniteIterable<A>}
     */
    @Override
    default ImmutableFiniteIterable<A> drop(int count) {
        validateDrop(count);
        return immutableFiniteIterable(Drop.drop(count, this));
    }

    /**
     * Returns a new {@code ImmutableFiniteIterable} that skips the first contiguous group of elements of this
     * {@code ImmutableFiniteIterable} that satisfy a predicate.
     * <p>
     * Iteration begins at the first element for which the predicate evaluates to false.
     *
     * @param predicate a predicate; should be referentially transparent and not have side-effects
     * @return a {@code ImmutableFiniteIterable<A>}
     */
    @Override
    default ImmutableFiniteIterable<A> dropWhile(Fn1<? super A, ? extends Boolean> predicate) {
        requireNonNull(predicate);
        return immutableFiniteIterable(DropWhile.dropWhile(predicate, this));
    }

    /**
     * Returns a new {@code ImmutableFiniteIterable} that contains all elements of this {@code ImmutableFiniteIterable}
     * that satisfy a predicate.
     *
     * @param predicate a predicate; should be referentially transparent and not have side-effects
     * @return a {@code ImmutableFiniteIterable<A>}
     */
    @Override
    default ImmutableFiniteIterable<A> filter(Fn1<? super A, ? extends Boolean> predicate) {
        requireNonNull(predicate);
        return immutableFiniteIterable(Filter.<A>filter(predicate).apply(this));
    }

    /**
     * Returns a new {@code ImmutableFiniteIterable} by applying a function to all elements of this {@code ImmutableFiniteIterable}.
     *
     * @param f   a function from {@code A} to {@code B}.
     *            This function should be referentially transparent and not perform side-effects.
     *            It may be called zero or more times for each element.
     * @param <B> the type returned by {@code f}
     * @return an {@code ImmutableFiniteIterable<B>}
     */
    @Override
    default <B> ImmutableFiniteIterable<B> fmap(Fn1<? super A, ? extends B> f) {
        requireNonNull(f);
        return immutableFiniteIterable(Map.map(f, this));
    }

    /**
     * Returns an {@code ImmutableNonEmptyIterable} containing all of the subsequences of initial
     * elements of this {@code ImmutableFiniteIterable}, ordered by size, starting with the empty list.
     * Example:
     *
     * <code>ImmutableFiniteIterable.of(1, 2, 3).inits(); // [[], [1], [1, 2], [1, 2, 3]]</code>
     *
     * @return an {@code ImmutableNonEmptyFiniteIterable<ImmutableFiniteIterable<A>>}
     */
    default ImmutableNonEmptyFiniteIterable<? extends ImmutableFiniteIterable<A>> inits() {
        return immutableNonEmptyFiniteIterableOrThrow(Map.map(EnhancedIterables::immutableFiniteIterable, Inits.inits(this)));
    }

    /**
     * Returns a new {@code ImmutableFiniteIterable} with the provided separator value injected between each value of this
     * {@code ImmutableFiniteIterable}.
     * <p>
     * If this {@code ImmutableFiniteIterable} contains fewer than two elements, it is left untouched.
     *
     * @param separator the separator value
     * @return a {@code ImmutableFiniteIterable<A>}
     */
    @Override
    default ImmutableFiniteIterable<A> intersperse(A separator) {
        return immutableFiniteIterable(Intersperse.intersperse(separator, this));
    }

    /**
     * Returns an {@code Iterable} of contiguous groups of elements in this {@code ImmutableFiniteIterable} that match a
     * predicate pairwise.
     *
     * @param predicate the predicate function.
     *                  This function should be referentially transparent and not perform side-effects.
     *                  It may be called zero or more times for each element.
     * @return an {@code ImmutableFiniteIterable<ImmutableNonEmptyFiniteIterable<A>>} containing the contiguous groups
     */
    @Override
    default ImmutableFiniteIterable<? extends ImmutableNonEmptyFiniteIterable<A>> magnetizeBy(Fn2<A, A, Boolean> predicate) {
        requireNonNull(predicate);
        return EnhancedIterables.immutableFiniteIterable(MagnetizeBy.magnetizeBy(predicate, this))
                .fmap(EnhancedIterables::immutableNonEmptyFiniteIterableOrThrow);
    }

    /**
     * Partitions this {@code ImmutableFiniteIterable} given a disjoint mapping function.
     *
     * @param function the mapping function
     * @param <B>      the output left Iterable element type, as well as the CoProduct2 A type
     * @param <C>      the output right Iterable element type, as well as the CoProduct2 B type
     * @return a {@code Tuple2<ImmutableFiniteIterable&lt;B&gt;, ImmutableFiniteIterable&lt;C&gt;>}
     */
    @Override
    default <B, C> Tuple2<? extends ImmutableFiniteIterable<B>, ? extends ImmutableFiniteIterable<C>> partition(
            Fn1<? super A, ? extends CoProduct2<B, C, ?>> function) {
        requireNonNull(function);
        Tuple2<Iterable<B>, Iterable<C>> partitionResult = Partition.partition(function, this);
        return tuple(immutableFiniteIterable(partitionResult._1()),
                immutableFiniteIterable(partitionResult._2()));
    }

    /**
     * Lazily prepends an element to the front of this {@code ImmutableFiniteIterable}, yielding a new {@code ImmutableNonEmptyFiniteIterable}.
     *
     * @param element the element to prepend
     * @return a {@code ImmutableNonEmptyFiniteIterable<A>}
     */
    @Override
    default ImmutableNonEmptyFiniteIterable<A> prepend(A element) {
        return ImmutableNonEmptyFiniteIterable.immutableNonEmptyFiniteIterable(element, this);
    }

    /**
     * Returns a new {@code ImmutableFiniteIterable} with the provided separator value injected before each value of this
     * {@code ImmutableFiniteIterable}.
     * <p>
     * If this {@code ImmutableFiniteIterable} is empty, it is left untouched.
     *
     * @param separator the separator value
     * @return a {@code ImmutableFiniteIterable<A>}
     */
    @Override
    default ImmutableFiniteIterable<A> prependAll(A separator) {
        return immutableFiniteIterable(PrependAll.prependAll(separator, this));
    }

    /**
     * Returns a reversed representation of this {@code ImmutableFiniteIterable}.
     * <p>
     * Note that reversing is deferred until the returned {@code Iterable} is iterated.
     *
     * @return an {@code ImmutableFiniteIterable<A>}
     */
    @Override
    default ImmutableFiniteIterable<A> reverse() {
        return immutableFiniteIterable(Reverse.reverse(this));
    }

    /**
     * "Slides" a window of {@code k} elements across the {@code ImmutableFiniteIterable} by one element at a time.
     * <p>
     * Example:
     *
     * <code>ImmutableFiniteIterable.of(1, 2, 3, 4, 5).slide(2); // [[1, 2], [2, 3], [3, 4], [4, 5]]</code>
     *
     * @param k the number of elements in the sliding window.  Must be &gt;= 1.
     * @return an {@code ImmutableFiniteIterable<ImmutableNonEmptyFiniteIterable<A>>}
     */
    @Override
    default ImmutableFiniteIterable<? extends ImmutableNonEmptyFiniteIterable<A>> slide(int k) {
        validateSlide(k);
        return immutableFiniteIterable(Map.map(EnhancedIterables::immutableNonEmptyFiniteIterableOrThrow,
                Slide.slide(k, this)));
    }

    /**
     * Returns a {@code Tuple2} where the first slot is the front contiguous elements of this
     * {@code ImmutableFiniteIterable} matching a predicate and the second slot is all the remaining elements.
     *
     * @param predicate a predicate; should be referentially transparent and not have side-effects
     * @return a {@code Tuple2<ImmutableFiniteIterable&lt;B&gt;, ImmutableFiniteIterable&lt;C&gt;>}
     */
    @Override
    default Tuple2<? extends ImmutableFiniteIterable<A>, ? extends ImmutableFiniteIterable<A>> span(Fn1<? super A, ? extends Boolean> predicate) {
        requireNonNull(predicate);
        Tuple2<Iterable<A>, Iterable<A>> spanResult = Span.<A>span(predicate).apply(this);
        return tuple(immutableFiniteIterable(spanResult._1()),
                immutableFiniteIterable(spanResult._2()));
    }

    /**
     * Returns an {@code ImmutableNonEmptyIterable} containing all of the subsequences of tail
     * elements of this {@code ImmutableFiniteIterable}, ordered by size, starting with the full list.
     * Example:
     *
     * <code>ImmutableFiniteIterable.of(1, 2, 3).tails(); // [[1, 2, 3], [2, 3], [3], []]</code>
     *
     * @return an {@code ImmutableNonEmptyIterable<ImmutableFiniteIterable<A>>}
     */
    @Override
    default ImmutableNonEmptyIterable<? extends ImmutableFiniteIterable<A>> tails() {
        return immutableNonEmptyIterableOrThrow(Map.map(EnhancedIterables::immutableFiniteIterable, Tails.tails(this)));
    }

    /**
     * Returns a new {@code ImmutableFiniteIterable} that limits to the first contiguous group of elements of this
     * {@code FiniteIterable} that satisfy a predicate.
     * <p>
     * Iteration ends at, but does not include, the first element for which the predicate evaluates to false.
     *
     * @param predicate a predicate; should be referentially transparent and not have side-effects
     * @return a {@code ImmutableFiniteIterable<A>}
     */
    @Override
    default ImmutableFiniteIterable<A> takeWhile(Fn1<? super A, ? extends Boolean> predicate) {
        requireNonNull(predicate);
        return immutableFiniteIterable(TakeWhile.takeWhile(predicate, this));
    }

    /**
     * Always succeeds because {@code ImmutableFiniteIterable}s are always finite.
     *
     * @return this {@code ImmutableFiniteIterable} wrapped in a `just`
     */
    @Override
    default Maybe<? extends ImmutableFiniteIterable<A>> toFinite() {
        return just(this);
    }

    /**
     * Converts this {@code ImmutableFiniteIterable} to a {@code ImmutableNonEmptyFiniteIterable} if it contains
     * one or more elements.
     *
     * @return a {@code Maybe<ImmutableNonEmptyFiniteIterable<A>}
     */
    @Override
    default Maybe<? extends ImmutableNonEmptyFiniteIterable<A>> toNonEmpty() {
        return EnhancedIterables.maybeNonEmpty(this)
                .fmap(EnhancedIterables::immutableNonEmptyFiniteIterableOrThrow);
    }

    /**
     * Zips together this {@code ImmutableFiniteIterable} with another {@code ImmutableIterable} by applying a zipping function.
     * <p>
     * Applies the function to the successive elements of each {@code Iterable} until one of them runs out of elements.
     *
     * @param fn    the zipping function.
     *              Not null.
     *              This function should be referentially transparent and not perform side-effects.
     *              It may be called zero or more times for each element.
     * @param other the other {@code Iterable}
     * @param <B>   the element type of the other {@code Iterable}
     * @param <C>   the element type of the result
     * @return an {@code ImmutableFiniteIterable<C>}
     */
    default <B, C> ImmutableFiniteIterable<C> zipWith(Fn2<A, B, C> fn, ImmutableIterable<B> other) {
        requireNonNull(fn);
        requireNonNull(other);
        return immutableFiniteIterable(ZipWith.zipWith(fn, this, other));
    }

    /**
     * Creates an {@code ImmutableFiniteIterable} by copying elements from a {@code FiniteIterable}.
     * <p>
     * If {@code source} is already an {@code ImmutableFiniteIterable}, this method will return it without copying.
     *
     * @param source the source to copy from
     * @param <A>    the element type
     * @return an {@code ImmutableFiniteIterable<A>}
     */
    static <A> ImmutableFiniteIterable<A> copyFrom(FiniteIterable<A> source) {
        return EnhancedIterables.copyFrom(source);
    }

    /**
     * Creates an {@code ImmutableFiniteIterable} by copying elements from a {@code Collection}.
     *
     * @param source the source to copy from
     * @param <A>    the element type
     * @return an {@code ImmutableFiniteIterable<A>}
     */
    static <A> ImmutableFiniteIterable<A> copyFrom(Collection<A> source) {
        return EnhancedIterables.copyFrom(source);
    }

    /**
     * Creates an {@code ImmutableFiniteIterable} by copying elements from an {@code Iterable}.
     * <p>
     * If {@code source} is already an {@code ImmutableIterable}, no copying will be performed.
     *
     * @param maxCount the maximum number of elements to take from the supplied {@link Iterable}.
     *                 Must be &gt;= 0.
     *                 May exceed size of the {@code Iterable}, in which case, the result will contain
     *                 as many elements available.
     * @param source   the source to copy from
     * @param <A>      the element type
     * @return an {@code ImmutableFiniteIterable<A>}
     */
    static <A> ImmutableFiniteIterable<A> copyFrom(int maxCount, Iterable<A> source) {
        return EnhancedIterables.copyFrom(maxCount, source);
    }

    /**
     * Creates an {@code ImmutableFiniteIterable} containing the given elements.
     * <p>
     * Note that this method actually returns an {@link ImmutableNonEmptyFiniteIterable}, which is
     * also an {@link ImmutableFiniteIterable}.
     *
     * @param first the first element
     * @param more  the remaining elements
     * @param <A>   the element type
     * @return an {@code ImmutableNonEmptyFiniteIterable<A>}
     */
    @SuppressWarnings("varargs")
    @SafeVarargs
    static <A> ImmutableNonEmptyFiniteIterable<A> of(A first, A... more) {
        return EnhancedIterables.of(first, more);
    }

}
