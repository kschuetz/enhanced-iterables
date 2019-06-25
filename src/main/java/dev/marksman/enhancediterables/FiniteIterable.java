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
import com.jnape.palatable.lambda.functions.builtin.fn3.FoldLeft;
import com.jnape.palatable.lambda.functions.builtin.fn3.ZipWith;
import com.jnape.palatable.lambda.monoid.builtin.Concat;

import java.util.Collection;

import static com.jnape.palatable.lambda.adt.Maybe.just;
import static com.jnape.palatable.lambda.adt.hlist.HList.tuple;
import static dev.marksman.enhancediterables.EnhancedIterable.enhance;
import static dev.marksman.enhancediterables.EnhancedIterables.immutableNonEmptyIterableOrThrow;
import static dev.marksman.enhancediterables.EnhancedIterables.nonEmptyFiniteIterableOrThrow;
import static dev.marksman.enhancediterables.Validation.validateDrop;
import static dev.marksman.enhancediterables.Validation.validateSlide;
import static java.util.Objects.requireNonNull;

/**
 * An {@code EnhancedIterable} that is finite.
 *
 * @param <A> the element type
 */
public interface FiniteIterable<A> extends EnhancedIterable<A> {

    /**
     * Lazily appends an element to the end of this {@code FiniteIterable}, yielding a new {@code NonEmptyFiniteIterable}.
     *
     * @param element the element to append
     * @return a {@code NonEmptyFiniteIterable<A>}
     */
    @Override
    default NonEmptyFiniteIterable<A> append(A element) {
        return nonEmptyFiniteIterableOrThrow(Snoc.snoc(element, this));
    }

    /**
     * Lazily concatenates another {@code FiniteIterable} to the end of this {@code FiniteIterable},
     * yielding a new {@code FiniteIterable}.
     *
     * @param other the other {@link FiniteIterable}
     * @return a {@code FiniteIterable<A>}
     */
    default FiniteIterable<A> concat(FiniteIterable<A> other) {
        requireNonNull(other);
        return EnhancedIterables.finiteIterable(Concat.concat(this, other));
    }

    /**
     * Lazily concatenates a {@code Collection} to the end of this {@code FiniteIterable},
     * yielding a new {@code FiniteIterable}.
     *
     * @param other a {@link Collection}
     * @return a {@code FiniteIterable<A>}
     */
    default FiniteIterable<A> concat(Collection<A> other) {
        requireNonNull(other);
        return EnhancedIterables.finiteIterable(Concat.concat(this, other));
    }

    /**
     * Lazily concatenates a {@code NonEmptyFiniteIterable} to the end of this {@code FiniteIterable},
     * yielding a new {@code NonEmptyFiniteIterable}.
     *
     * @param other a {@link NonEmptyFiniteIterable}
     * @return a {@code NonEmptyFiniteIterable<A>}
     */
    default NonEmptyFiniteIterable<A> concat(NonEmptyFiniteIterable<A> other) {
        requireNonNull(other);
        return EnhancedIterables.nonEmptyFiniteIterableOrThrow(Concat.concat(this, other));
    }

    /**
     * Returns the lazily computed cartesian product of this {@code FiniteIterable} with another {@code FiniteIterable}.
     *
     * @param other a {@code FiniteIterable} of any type
     * @param <B>   the type of the other {@code FiniteIterable}
     * @return a {@code FiniteIterable<Tuple2<A, B>>}
     */
    default <B> FiniteIterable<Tuple2<A, B>> cross(FiniteIterable<B> other) {
        requireNonNull(other);
        return EnhancedIterables.finiteIterable(CartesianProduct.cartesianProduct(this, other));
    }

    /**
     * Returns the lazily computed cartesian product of this {@code FiniteIterable} with a {@code Collection}.
     *
     * @param other a {@link Collection} of any type
     * @param <B>   the type of the other {@code Collection}
     * @return a {@code FiniteIterable<Tuple2<A, B>>}
     */
    default <B> FiniteIterable<Tuple2<A, B>> cross(Collection<B> other) {
        requireNonNull(other);
        return EnhancedIterables.finiteIterable(CartesianProduct.cartesianProduct(this, other));
    }

    /**
     * Returns a new {@code FiniteIterable} that drops the first {@code count} elements of this {@code FiniteIterable}.
     *
     * @param count the number of elements to drop from this {@code FiniteIterable}.
     *              Must be &gt;= 0.
     *              May exceed size of this {@code FiniteIterable}, in which case, the result will be an
     *              empty {@code FiniteIterable}.
     * @return a {@code FiniteIterable<A>}
     */
    @Override
    default FiniteIterable<A> drop(int count) {
        validateDrop(count);
        return EnhancedIterables.finiteIterable(Drop.drop(count, this));
    }

    /**
     * Returns a new {@code FiniteIterable} that skips the first contiguous group of elements of this
     * {@code FiniteIterable} that satisfy a predicate.
     * <p>
     * Iteration begins at the first element for which the predicate evaluates to false.
     *
     * @param predicate a predicate; should be referentially transparent and not have side-effects
     * @return a {@code FiniteIterable<A>}
     */
    @Override
    default FiniteIterable<A> dropWhile(Fn1<? super A, ? extends Boolean> predicate) {
        requireNonNull(predicate);
        return EnhancedIterables.finiteIterable(DropWhile.dropWhile(predicate, this));
    }

    /**
     * Returns a new {@code FiniteIterable} that contains all elements of this {@code FiniteIterable}
     * that satisfy a predicate.
     *
     * @param predicate a predicate; should be referentially transparent and not have side-effects
     * @return a {@code FiniteIterable<A>}
     */
    @Override
    default FiniteIterable<A> filter(Fn1<? super A, ? extends Boolean> predicate) {
        requireNonNull(predicate);
        return EnhancedIterables.finiteIterable(Filter.<A>filter(predicate).apply(this));
    }

    /**
     * Returns a new {@code FiniteIterable} by applying a function to all elements of this {@code FiniteIterable}.
     *
     * @param f   a function from {@code A} to {@code B}.
     *            This function should be referentially transparent and not perform side-effects.
     *            It may be called zero or more times for each element.
     * @param <B> the type returned by {@code f}
     * @return a {@code FiniteIterableonEmptyFiniteIterable<B>}
     */
    @Override
    default <B> FiniteIterable<B> fmap(Fn1<? super A, ? extends B> f) {
        requireNonNull(f);
        return EnhancedIterables.finiteIterable(Map.map(f, this));
    }

    /**
     * Applies a binary operator to a start value and all elements of this {@code FiniteIterable}, going left to right.
     *
     * @param z   the start value
     * @param op  the binary operator
     * @param <B> the result type of the binary operator
     * @return the result of inserting {@code op} between consecutive elements of this {@code FiniteIterable},
     * going left to right with the start value {@code z} on the left:
     * <code>
     * op(...op(z, x_1), x_2, ..., x_n)
     * </code>
     * where <code>x,,1,,, ..., x,,n,,</code> are the elements of this {@code FiniteIterable}
     * Returns {@code z} if this {@code FiniteIterable} is empty.
     */
    default <B> B foldLeft(Fn2<? super B, ? super A, ? extends B> op, B z) {
        requireNonNull(op);
        return FoldLeft.<A, B>foldLeft(op, z).apply(this);
    }

    /**
     * Returns a {@code ImmutableNonEmptyIterable} containing all of the subsequences of initial
     * elements of this {@code FiniteIterable}, ordered by size, starting with the empty list.
     * Example:
     *
     * <code>FiniteIterable.of(1, 2, 3).inits(); // [[], [1], [1, 2], [1, 2, 3]]</code>
     *
     * @return a {@code ImmutableNonEmptyIterable<FiniteIterable<A>>}
     */
    default ImmutableNonEmptyIterable<? extends FiniteIterable<A>> inits() {
        return immutableNonEmptyIterableOrThrow(Map.map(EnhancedIterables::finiteIterable, Inits.inits(this)));
    }

    /**
     * Returns a new {@code FiniteIterable} with the provided separator value injected between each value of this
     * {@code FiniteIterable}.
     * <p>
     * If this {@code FiniteIterable} contains fewer than two elements, it is left untouched.
     *
     * @param separator the separator value
     * @return a {@code FiniteIterable<A>}
     */
    @Override
    default FiniteIterable<A> intersperse(A separator) {
        return EnhancedIterables.finiteIterable(Intersperse.intersperse(separator, this));
    }

    /**
     * Returns an {@code Iterable} of contiguous groups of elements in this {@code FiniteIterable} that match a
     * predicate pairwise.
     *
     * @param predicate the predicate function.
     *                  This function should be referentially transparent and not perform side-effects.
     *                  It may be called zero or more times for each element.
     * @return a {@code FiniteIterable<NonEmptyFiniteIterable<A>>} containing the contiguous groups
     */
    @Override
    default FiniteIterable<? extends NonEmptyFiniteIterable<A>> magnetizeBy(Fn2<A, A, Boolean> predicate) {
        requireNonNull(predicate);
        return EnhancedIterables.finiteIterable(MagnetizeBy.magnetizeBy(predicate, this))
                .fmap(EnhancedIterables::nonEmptyFiniteIterableOrThrow);
    }

    /**
     * Partitions this {@code FiniteIterable} given a disjoint mapping function.
     *
     * @param function the mapping function
     * @param <B>      the output left Iterable element type, as well as the CoProduct2 A type
     * @param <C>      the output right Iterable element type, as well as the CoProduct2 B type
     * @return a {@code Tuple2<FiniteIterable&lt;B&gt;, FiniteIterable&lt;C&gt;>}
     */
    @Override
    default <B, C> Tuple2<? extends FiniteIterable<B>, ? extends FiniteIterable<C>> partition(
            Fn1<? super A, ? extends CoProduct2<B, C, ?>> function) {
        requireNonNull(function);
        Tuple2<Iterable<B>, Iterable<C>> partitionResult = Partition.partition(function, this);
        return tuple(EnhancedIterables.finiteIterable(partitionResult._1()),
                EnhancedIterables.finiteIterable(partitionResult._2()));
    }

    /**
     * Lazily prepends an element to the front of this {@code FiniteIterable}, yielding a new {@code NonEmptyFiniteIterable}.
     *
     * @param element the element to prepend
     * @return a {@code NonEmptyFiniteIterable<A>}
     */
    @Override
    default NonEmptyFiniteIterable<A> prepend(A element) {
        return NonEmptyFiniteIterable.nonEmptyFiniteIterable(element, this);
    }

    /**
     * Returns a new {@code FiniteIterable} with the provided separator value injected before each value of this
     * {@code FiniteIterable}.
     * <p>
     * If this {@code FiniteIterable} is empty, it is left untouched.
     *
     * @param separator the separator value
     * @return a {@code FiniteIterable<A>}
     */
    @Override
    default FiniteIterable<A> prependAll(A separator) {
        return EnhancedIterables.finiteIterable(PrependAll.prependAll(separator, this));
    }

    /**
     * Returns a reversed representation of this {@code FiniteIterable}.
     * <p>
     * Note that reversing is deferred until the returned {@code Iterable} is iterated.
     *
     * @return a {@code FiniteIterable<A>}
     */
    default FiniteIterable<A> reverse() {
        return EnhancedIterables.finiteIterable(Reverse.reverse(this));
    }

    /**
     * "Slides" a window of {@code k} elements across the {@code FiniteIterable} by one element at a time.
     * <p>
     * Example:
     *
     * <code>FiniteIterable.of(1, 2, 3, 4, 5).slide(2); // [[1, 2], [2, 3], [3, 4], [4, 5]]</code>
     *
     * @param k the number of elements in the sliding window.  Must be &gt;= 1.
     * @return a {@code FiniteIterable<NonEmptyFiniteIterable<A>>}
     */
    @Override
    default FiniteIterable<? extends NonEmptyFiniteIterable<A>> slide(int k) {
        validateSlide(k);
        return EnhancedIterables.finiteIterable(Map.map(EnhancedIterables::nonEmptyFiniteIterableOrThrow,
                Slide.slide(k, this)));
    }

    /**
     * Returns a {@code Tuple2} where the first slot is the front contiguous elements of this
     * {@code FiniteIterable} matching a predicate and the second slot is all the remaining elements.
     *
     * @param predicate a predicate; should be referentially transparent and not have side-effects
     * @return a {@code Tuple2<FiniteIterable&lt;B&gt;, FiniteIterable&lt;C&gt;>}
     */
    @Override
    default Tuple2<? extends FiniteIterable<A>, ? extends FiniteIterable<A>> span(Fn1<? super A, ? extends Boolean> predicate) {
        requireNonNull(predicate);
        Tuple2<Iterable<A>, Iterable<A>> spanResult = Span.<A>span(predicate).apply(this);
        return tuple(EnhancedIterables.finiteIterable(spanResult._1()),
                EnhancedIterables.finiteIterable(spanResult._2()));
    }

    /**
     * Returns an {@code ImmutableNonEmptyIterable} containing all of the subsequences of tail
     * elements of this {@code FiniteIterable}, ordered by size, starting with the full list.
     * Example:
     *
     * <code>FiniteIterable.of(1, 2, 3).tails(); // [[1, 2, 3], [2, 3], [3], []]</code>
     *
     * @return an {@code ImmutableNonEmptyIterable<FiniteIterable<A>>}
     */
    @Override
    default ImmutableNonEmptyIterable<? extends FiniteIterable<A>> tails() {
        return immutableNonEmptyIterableOrThrow(Map.map(EnhancedIterables::finiteIterable, Tails.tails(this)));
    }

    /**
     * Returns a new {@code FiniteIterable} that limits to the first contiguous group of elements of this
     * {@code FiniteIterable} that satisfy a predicate.
     * <p>
     * Iteration ends at, but does not include, the first element for which the predicate evaluates to false.
     *
     * @param predicate a predicate; should be referentially transparent and not have side-effects
     * @return a {@code FiniteIterable<A>}
     */
    @Override
    default FiniteIterable<A> takeWhile(Fn1<? super A, ? extends Boolean> predicate) {
        requireNonNull(predicate);
        return EnhancedIterables.finiteIterable(TakeWhile.takeWhile(predicate, this));
    }

    /**
     * Always succeeds because {@code FiniteIterable}s are always finite.
     *
     * @return this {@code FiniteIterable} wrapped in a `just`
     */
    @Override
    default Maybe<? extends FiniteIterable<A>> toFinite() {
        return just(this);
    }

    /**
     * Converts this {@code FiniteIterable} to a {@code NonEmptyFiniteIterable} if it contains
     * one or more elements.
     *
     * @return a {@code Maybe<NonEmptyFiniteIterable<A>}
     */
    @Override
    default Maybe<? extends NonEmptyFiniteIterable<A>> toNonEmpty() {
        return EnhancedIterables.maybeNonEmpty(this)
                .fmap(EnhancedIterables::nonEmptyFiniteIterableOrThrow);
    }

    /**
     * Zips together this {@code FiniteIterable} with another {@code Iterable} by applying a zipping function.
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
     * @return an {@code FiniteIterable<C>}
     */
    default <B, C> FiniteIterable<C> zipWith(Fn2<A, B, C> fn, Iterable<B> other) {
        requireNonNull(fn);
        requireNonNull(other);
        return EnhancedIterables.finiteIterable(ZipWith.zipWith(fn, this, other));
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
     * Creates a {@code FiniteIterable} by wrapping a {@code Collection}.
     * <p>
     * Does not make a copy of the {@link Collection}.
     *
     * @param collection the source {@code Collection}
     * @param <A>        the element type
     * @return a {@code FiniteIterable<A>}
     */
    static <A> FiniteIterable<A> finiteIterable(Collection<A> collection) {
        requireNonNull(collection);
        return EnhancedIterables.finiteIterable(collection);
    }

    /**
     * Creates a {@code FiniteIterable} by wrapping an {@code Iterable}.
     *
     * @param maxCount the maximum number of elements to take from the supplied {@link Iterable}.
     *                 Must be &gt;= 0.
     *                 May exceed size of the {@code Iterable}, in which case, the result will contain
     *                 as many elements available.
     * @param iterable the source {@code Iterable}
     * @param <A>      the element type
     * @return a {@code FiniteIterable<A>}
     */
    static <A> FiniteIterable<A> finiteIterable(int maxCount, Iterable<A> iterable) {
        return enhance(iterable).take(maxCount);
    }

    /**
     * Creates a {@code FiniteIterable} containing the given elements.
     * <p>
     * Note that this method actually returns an {@link ImmutableNonEmptyFiniteIterable}, which is
     * also a {@link FiniteIterable}.
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
