package dev.marksman.enhancediterables;

import com.jnape.palatable.lambda.adt.Maybe;
import com.jnape.palatable.lambda.adt.hlist.Tuple2;
import com.jnape.palatable.lambda.functions.Fn1;
import com.jnape.palatable.lambda.functions.Fn2;
import com.jnape.palatable.lambda.functions.builtin.fn1.Init;
import com.jnape.palatable.lambda.functions.builtin.fn1.Reverse;
import com.jnape.palatable.lambda.functions.builtin.fn2.CartesianProduct;
import com.jnape.palatable.lambda.functions.builtin.fn2.Intersperse;
import com.jnape.palatable.lambda.functions.builtin.fn2.Map;
import com.jnape.palatable.lambda.functions.builtin.fn2.PrependAll;
import com.jnape.palatable.lambda.functions.builtin.fn3.ZipWith;
import com.jnape.palatable.lambda.monoid.builtin.Concat;

import java.util.Collection;

import static com.jnape.palatable.lambda.adt.Maybe.just;
import static dev.marksman.enhancediterables.EnhancedIterables.nonEmptyFiniteIterableOrThrow;
import static java.util.Objects.requireNonNull;

/**
 * An {@code EnhancedIterable} that is finite and guaranteed to contain at least one element.
 *
 * @param <A> the element type
 */
public interface NonEmptyFiniteIterable<A> extends FiniteIterable<A>, NonEmptyIterable<A> {

    /**
     * Returns an {@code FiniteIterable} containing all subsequent elements of this one beyond the first.
     *
     * @return an {@code FiniteIterable<A>}
     */
    @Override
    FiniteIterable<A> tail();

    /**
     * Lazily concatenates a {@code FiniteIterable} to the end of this {@code NonEmptyFiniteIterable},
     * yielding a new {@code NonEmptyFiniteIterable}.
     *
     * @param other a {@link FiniteIterable}
     * @return an {@code NonEmptyFiniteIterable<A>}
     */
    @Override
    default NonEmptyFiniteIterable<A> concat(FiniteIterable<A> other) {
        requireNonNull(other);
        return nonEmptyFiniteIterableOrThrow(Concat.concat(this, other));
    }

    /**
     * Lazily concatenates a {@code Collection} to the end of this {@code NonEmptyFiniteIterable},
     * yielding a new {@code NonEmptyFiniteIterable}.
     *
     * @param other a {@link Collection}
     * @return an {@code NonEmptyFiniteIterable<A>}
     */
    @Override
    default NonEmptyFiniteIterable<A> concat(Collection<A> other) {
        requireNonNull(other);
        return nonEmptyFiniteIterableOrThrow(Concat.concat(this, other));
    }

    /**
     * Returns the lazily computed cartesian product of this {@code NonEmptyFiniteIterable} with another {@code NonEmptyFiniteIterable}.
     *
     * @param other a {@code NonEmptyFiniteIterable} of any type
     * @param <B>   the type of the other {@code NonEmptyFiniteIterable}
     * @return a {@code NonEmptyFiniteIterable<Tuple2<A, B>>}
     */
    default <B> NonEmptyFiniteIterable<Tuple2<A, B>> cross(NonEmptyFiniteIterable<B> other) {
        requireNonNull(other);
        return nonEmptyFiniteIterableOrThrow(CartesianProduct.cartesianProduct(this, other));
    }

    /**
     * Returns a new {@code NonEmptyFiniteIterable} by applying a function to all elements of this {@code NonEmptyFiniteIterable}.
     *
     * @param f   a function from {@code A} to {@code B}.
     *            This function should be referentially transparent and not perform side-effects.
     *            It may be called zero or more times for each element.
     * @param <B> the type returned by {@code f}
     * @return a <code>NonEmptyFiniteIterable&lt;B&gt;</code>
     */
    @Override
    default <B> NonEmptyFiniteIterable<B> fmap(Fn1<? super A, ? extends B> f) {
        requireNonNull(f);
        return nonEmptyFiniteIterableOrThrow(Map.map(f, this));
    }

    /**
     * Returns a {@code FiniteIterable} containing all of elements of this one, except for the last element.
     *
     * @return a {@code FiniteIterable<A>}
     */
    default FiniteIterable<A> init() {
        return EnhancedIterables.finiteIterable(Init.init(this));
    }

    /**
     * Returns a new {@code NonEmptyFiniteIterable} with the provided separator value injected between each value of this
     * {@code NonEmptyFiniteIterable}.
     * <p>
     * If this {@code NonEmptyFiniteIterable} contains only one element, it is left untouched.
     *
     * @param separator the separator value
     * @return a <code>NonEmptyFiniteIterable&lt;A&gt;</code>
     */
    @Override
    default NonEmptyFiniteIterable<A> intersperse(A separator) {
        return nonEmptyFiniteIterableOrThrow(Intersperse.intersperse(separator, this));
    }

    /**
     * Returns a new {@code NonEmptyFiniteIterable} with the provided separator value injected before each value of this
     * {@code NonEmptyFiniteIterable}.
     *
     * @param separator the separator value
     * @return a <code>NonEmptyFiniteIterable&lt;A&gt;</code>
     */
    @Override
    default NonEmptyFiniteIterable<A> prependAll(A separator) {
        return nonEmptyFiniteIterableOrThrow(PrependAll.prependAll(separator, this));
    }

    /**
     * Returns a reversed representation of this {@code NonEmptyFiniteIterable}.
     * <p>
     * Note that reversing is deferred until the returned {@code Iterable} is iterated.
     *
     * @return a {@code NonEmptyFiniteIterable<A>}
     */
    @Override
    default NonEmptyFiniteIterable<A> reverse() {
        return nonEmptyFiniteIterableOrThrow(Reverse.reverse(this));
    }

    /**
     * Always succeeds because {@code NonEmptyFiniteIterable}s are always finite.
     *
     * @return this {@code NonEmptyFiniteIterable} wrapped in a `just`
     */
    @Override
    default Maybe<? extends NonEmptyFiniteIterable<A>> toFinite() {
        return just(this);
    }

    /**
     * Always succeeds because {@code NonEmptyFiniteIterable}s are always non-empty.
     *
     * @return this {@code NonEmptyFiniteIterable} wrapped in a `just`
     */
    @Override
    default Maybe<? extends NonEmptyFiniteIterable<A>> toNonEmpty() {
        return just(this);
    }

    /**
     * Zips together this {@code NonEmptyFiniteIterable} with an {@code NonEmptyIterable} by applying a zipping function.
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
     * @return an {@code NonEmptyFiniteIterable<C>}
     */
    default <B, C> NonEmptyFiniteIterable<C> zipWith(Fn2<A, B, C> fn, NonEmptyIterable<B> other) {
        requireNonNull(fn);
        requireNonNull(other);
        return nonEmptyFiniteIterableOrThrow(ZipWith.zipWith(fn, this, other));
    }

    /**
     * Creates a {@code NonEmptyFiniteIterable}.
     *
     * @param head the first element
     * @param tail the remaining elements.  May be empty.
     * @param <A>  the element type
     * @return a {@code NonEmptyFiniteIterable<A>}
     */
    static <A> NonEmptyFiniteIterable<A> nonEmptyFiniteIterable(A head, FiniteIterable<A> tail) {
        return EnhancedIterables.nonEmptyFiniteIterable(head, tail);
    }

    /**
     * Creates a {@code NonEmptyFiniteIterable}.
     *
     * @param head the first element
     * @param tail the remaining elements.  May be empty.
     * @param <A>  the element type
     * @return a {@code NonEmptyFiniteIterable<A>}
     */
    static <A> NonEmptyFiniteIterable<A> nonEmptyFiniteIterable(A head, Collection<A> tail) {
        return EnhancedIterables.nonEmptyFiniteIterable(head, tail);
    }

    /**
     * Creates a {@code NonEmptyFiniteIterable} containing the given elements.
     * <p>
     * Note that this method actually returns an {@link ImmutableNonEmptyFiniteIterable}, which is
     * also an {@link NonEmptyFiniteIterable}.
     *
     * @param first the first element
     * @param more  the remaining elements
     * @param <A>   the element type
     * @return an {@code ImmutableNonEmptyFiniteIterable<A>}
     */
    @SafeVarargs
    static <A> ImmutableNonEmptyFiniteIterable<A> of(A first, A... more) {
        return EnhancedIterables.of(first, more);
    }

}
