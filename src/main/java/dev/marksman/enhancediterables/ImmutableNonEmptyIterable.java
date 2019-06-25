package dev.marksman.enhancediterables;

import com.jnape.palatable.lambda.adt.Maybe;
import com.jnape.palatable.lambda.functions.Fn1;
import com.jnape.palatable.lambda.functions.Fn2;
import com.jnape.palatable.lambda.functions.builtin.fn2.Intersperse;
import com.jnape.palatable.lambda.functions.builtin.fn2.MagnetizeBy;
import com.jnape.palatable.lambda.functions.builtin.fn2.Map;
import com.jnape.palatable.lambda.functions.builtin.fn2.PrependAll;
import com.jnape.palatable.lambda.functions.builtin.fn3.ZipWith;
import com.jnape.palatable.lambda.monoid.builtin.Concat;

import static com.jnape.palatable.lambda.adt.Maybe.just;
import static dev.marksman.enhancediterables.EnhancedIterables.immutableNonEmptyFiniteIterableOrThrow;
import static dev.marksman.enhancediterables.EnhancedIterables.immutableNonEmptyIterableOrThrow;
import static java.util.Objects.requireNonNull;

/**
 * An {@code EnhancedIterable} that is safe from mutation, and guaranteed to contain at least one element.
 * <p>
 * May be infinite or finite.
 *
 * @param <A> the element type
 */
public interface ImmutableNonEmptyIterable<A> extends ImmutableIterable<A>, NonEmptyIterable<A> {

    /**
     * Returns an {@code ImmutableIterable} containing all subsequent elements of this one beyond the first.
     *
     * @return an {@code ImmutableIterable<A>}
     */
    @Override
    ImmutableIterable<A> tail();

    /**
     * Lazily concatenates an {@code ImmutableIterable} to the end of this {@code ImmutableNonEmptyIterable},
     * yielding a new {@code ImmutableNonEmptyIterable}.
     *
     * @param other an {@link ImmutableIterable}
     * @return an {@code ImmutableNonEmptyIterable<A>}
     */
    @Override
    default ImmutableNonEmptyIterable<A> concat(ImmutableIterable<A> other) {
        requireNonNull(other);
        return immutableNonEmptyIterableOrThrow(Concat.concat(this, other));
    }

    /**
     * Returns a new {@code ImmutableNonEmptyIterable} by applying a function to all elements of this {@code ImmutableNonEmptyIterable}.
     *
     * @param f   a function from {@code A} to {@code B}.
     *            This function should be referentially transparent and not perform side-effects.
     *            It may be called zero or more times for each element.
     * @param <B> the type returned by {@code f}
     * @return an {@code ImmutableNonEmptyIterable<B>}
     */
    @Override
    default <B> ImmutableNonEmptyIterable<B> fmap(Fn1<? super A, ? extends B> f) {
        requireNonNull(f);
        return immutableNonEmptyIterableOrThrow(Map.map(f, this));
    }

    /**
     * Returns a new {@code ImmutableNonEmptyIterable} with the provided separator value injected between each value of this
     * {@code ImmutableNonEmptyIterable}.
     * <p>
     * If this {@code ImmutableNonEmptyIterable} contains only one element, it is left untouched.
     *
     * @param separator the separator value
     * @return a {@code ImmutableNonEmptyIterable<A>}
     */
    @Override
    default ImmutableNonEmptyIterable<A> intersperse(A separator) {
        return immutableNonEmptyIterableOrThrow(Intersperse.intersperse(separator, this));
    }

    /**
     * Returns an {@code Iterable} of contiguous groups of elements in this {@code ImmutableNonEmptyIterable} that match a
     * predicate pairwise.
     *
     * @param predicate the predicate function.
     *                  This function should be referentially transparent and not perform side-effects.
     *                  It may be called zero or more times for each element.
     * @return an {@code ImmutableNonEmptyIterable<ImmutableNonEmptyIterable<A>>} containing the contiguous groups
     */
    @Override
    default ImmutableNonEmptyIterable<? extends ImmutableNonEmptyIterable<A>> magnetizeBy(Fn2<A, A, Boolean> predicate) {
        requireNonNull(predicate);
        return immutableNonEmptyIterableOrThrow(MagnetizeBy.magnetizeBy(predicate, this))
                .fmap(EnhancedIterables::immutableNonEmptyIterableOrThrow);
    }

    /**
     * Returns a new {@code ImmutableNonEmptyIterable} with the provided separator value injected before each value of this
     * {@code ImmutableNonEmptyIterable}.
     *
     * @param separator the separator value
     * @return a {@code ImmutableNonEmptyIterable<A>}
     */
    @Override
    default ImmutableNonEmptyIterable<A> prependAll(A separator) {
        return immutableNonEmptyIterableOrThrow(PrependAll.prependAll(separator, this));
    }

    /**
     * Converts this {@code ImmutableNonEmptyIterable} to an {@code ImmutableNonEmptyFiniteIterable} if there is enough
     * information to do so without iterating it.
     * <p>
     * Note that if this method returns {@code nothing()}, it does NOT necessarily mean this
     * {@code ImmutableNonEmptyIterable} is infinite.
     *
     * @return a {@code Maybe<ImmutableNonEmptyFiniteIterable<A>}
     */
    @Override
    default Maybe<? extends ImmutableNonEmptyFiniteIterable<A>> toFinite() {
        return EnhancedIterables.immutableNonEmptyMaybeFinite(head(), tail());
    }

    /**
     * Always succeeds because {@code ImmutableNonEmptyFiniteIterable}s are always non-empty.
     *
     * @return this {@code ImmutableNonEmptyFiniteIterable} wrapped in a `just`
     */
    @Override
    default Maybe<? extends ImmutableNonEmptyIterable<A>> toNonEmpty() {
        return just(this);
    }

    /**
     * Zips together this {@code ImmutableNonEmptyIterable} with another {@code ImmutableNonEmptyIterable} by applying a zipping function.
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
     * @return an {@code ImmutableNonEmptyIterable<C>}
     */
    default <B, C> ImmutableNonEmptyIterable<C> zipWith(Fn2<A, B, C> fn, ImmutableNonEmptyIterable<B> other) {
        requireNonNull(fn);
        requireNonNull(other);
        return immutableNonEmptyIterableOrThrow(ZipWith.zipWith(fn, this, other));
    }

    /**
     * Zips together this {@code ImmutableNonEmptyIterable} with an {@code ImmutableNonEmptyFiniteIterable} by applying a zipping function.
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
     * @return an {@code ImmutableNonEmptyFiniteIterable<C>}
     */
    default <B, C> ImmutableNonEmptyFiniteIterable<C> zipWith(Fn2<A, B, C> fn, ImmutableNonEmptyFiniteIterable<B> other) {
        requireNonNull(fn);
        requireNonNull(other);
        return immutableNonEmptyFiniteIterableOrThrow(ZipWith.zipWith(fn, this, other));
    }

    /**
     * Creates an {@code ImmutableNonEmptyIterable}.
     *
     * @param head the first element
     * @param tail the remaining elements.  May be empty.
     * @param <A>  the element type
     * @return a {@code ImmutableNonEmptyIterable<A>}
     */
    static <A> ImmutableNonEmptyIterable<A> immutableNonEmptyIterable(A head, ImmutableIterable<A> tail) {
        return EnhancedIterables.immutableNonEmptyIterable(head, tail);
    }

    /**
     * Creates an {@code ImmutableNonEmptyIterable} containing the given elements.
     * <p>
     * Note that this method actually returns an {@link ImmutableNonEmptyFiniteIterable}, which is
     * also an {@link ImmutableNonEmptyIterable}.
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
