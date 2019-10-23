package dev.marksman.enhancediterables;

import com.jnape.palatable.lambda.adt.Maybe;
import com.jnape.palatable.lambda.functions.Fn1;
import com.jnape.palatable.lambda.functions.Fn2;
import com.jnape.palatable.lambda.functions.builtin.fn2.*;
import com.jnape.palatable.lambda.functions.builtin.fn3.ZipWith;
import com.jnape.palatable.lambda.monoid.builtin.Concat;

import java.util.Iterator;

import static com.jnape.palatable.lambda.adt.Maybe.just;
import static dev.marksman.enhancediterables.EnhancedIterables.nonEmptyFiniteIterableOrThrow;
import static dev.marksman.enhancediterables.EnhancedIterables.nonEmptyIterableOrThrow;
import static java.util.Objects.requireNonNull;

/**
 * An {@code EnhancedIterable} that is guaranteed to contain at least one element.
 * <p>
 * May be infinite or finite.
 *
 * @param <A> the element type
 */
public interface NonEmptyIterable<A> extends EnhancedIterable<A> {

    /**
     * Returns the first element.
     *
     * @return an element of type {@code A}
     */
    A head();

    /**
     * Returns an {@code EnhancedIterable} containing all subsequent elements of this one beyond the first.
     *
     * @return an {@code EnhancedIterable<A>}
     */
    EnhancedIterable<A> tail();

    /**
     * Lazily concatenates an {@code Iterable} to the end of this {@code NonEmptyIterable},
     * yielding a new {@code NonEmptyIterable}.
     *
     * @param other an {@link Iterable}
     * @return a {@code NonEmptyIterable<A>}
     */
    @Override
    default NonEmptyIterable<A> concat(Iterable<A> other) {
        requireNonNull(other);
        return nonEmptyIterableOrThrow(Concat.concat(this, other));
    }

    /**
     * Returns a new {@code NonEmptyIterable} by applying a function to all elements of this {@code NonEmptyIterable}.
     *
     * @param f   a function from {@code A} to {@code B}.
     *            This function should be referentially transparent and not perform side-effects.
     *            It may be called zero or more times for each element.
     * @param <B> the type returned by {@code f}
     * @return a {@code NonEmptyIterable<B>}
     */
    @Override
    default <B> NonEmptyIterable<B> fmap(Fn1<? super A, ? extends B> f) {
        requireNonNull(f);
        return nonEmptyIterable(f.apply(head()), Map.map(f, tail()));
    }

    /**
     * Always returns false, as a {@code NonEmptyIterable} is never empty.
     *
     * @return false
     */
    @Override
    default boolean isEmpty() {
        return false;
    }

    /**
     * Returns a new {@code NonEmptyIterable} with the provided separator value injected between each value of this
     * {@code NonEmptyIterable}.
     * <p>
     * If this {@code NonEmptyIterable} contains only one element, it is left untouched.
     *
     * @param separator the separator value
     * @return a {@code NonEmptyIterable<A>}
     */
    @Override
    default NonEmptyIterable<A> intersperse(A separator) {
        return nonEmptyIterableOrThrow(Intersperse.intersperse(separator, this));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    default Iterator<A> iterator() {
        return Cons.cons(head(), tail()).iterator();
    }

    /**
     * Returns an {@code Iterable} of contiguous groups of elements in this {@code NonEmptyIterable} that match a
     * predicate pairwise.
     *
     * @param predicate the predicate function.
     *                  This function should be referentially transparent and not perform side-effects.
     *                  It may be called zero or more times for each element.
     * @return an {@code NonEmptyIterable<NonEmptyIterable<A>>} containing the contiguous groups
     */
    @Override
    default NonEmptyIterable<? extends NonEmptyIterable<A>> magnetizeBy(Fn2<A, A, Boolean> predicate) {
        requireNonNull(predicate);
        return nonEmptyIterableOrThrow(MagnetizeBy.magnetizeBy(predicate, this))
                .fmap(EnhancedIterables::nonEmptyIterableOrThrow);
    }

    /**
     * Returns a new {@code NonEmptyIterable} with the provided separator value injected before each value of this
     * {@code NonEmptyIterable}.
     *
     * @param separator the separator value
     * @return a {@code NonEmptyIterable<A>}
     */
    @Override
    default NonEmptyIterable<A> prependAll(A separator) {
        return nonEmptyIterableOrThrow(PrependAll.prependAll(separator, this));
    }

    /**
     * Converts this {@code NonEmptyIterable} to an {@code NonEmptyFiniteIterable} if there is enough
     * information to do so without iterating it.
     * <p>
     * Note that if this method returns {@code nothing()}, it does NOT necessarily mean this
     * {@code NonEmptyIterable} is infinite.
     *
     * @return a {@code Maybe<NonEmptyFiniteIterable<A>}
     */
    @Override
    default Maybe<? extends NonEmptyFiniteIterable<A>> toFinite() {
        return EnhancedIterables.nonEmptyMaybeFinite(head(), tail());
    }

    /**
     * Always succeeds because {@code NonEmptyIterable}s are always non-empty.
     *
     * @return this {@code NonEmptyIterable} wrapped in a `just`
     */
    @Override
    default Maybe<? extends NonEmptyIterable<A>> toNonEmpty() {
        return just(this);
    }

    /**
     * Zips together this {@code NonEmptyIterable} with another {@code NonEmptyIterable} by applying a zipping function.
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
     * @return an {@code NonEmptyIterable<C>}
     */
    default <B, C> NonEmptyIterable<C> zipWith(Fn2<A, B, C> fn, NonEmptyIterable<B> other) {
        requireNonNull(fn);
        requireNonNull(other);
        return nonEmptyIterable(fn.apply(head(), other.head()),
                ZipWith.zipWith(fn, tail(), other.tail()));
    }

    /**
     * Zips together this {@code NonEmptyIterable} with a {@code NonEmptyFiniteIterable} by applying a zipping function.
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
    default <B, C> NonEmptyFiniteIterable<C> zipWith(Fn2<A, B, C> fn, NonEmptyFiniteIterable<B> other) {
        requireNonNull(fn);
        requireNonNull(other);
        return nonEmptyFiniteIterableOrThrow(ZipWith.zipWith(fn, this, other));
    }

    /**
     * Creates a {@link NonEmptyIterable}.
     *
     * @param head the first element
     * @param tail the remaining elements.  May be empty.
     * @param <A>  the element type
     * @return a {@code NonEmptyIterable<A>}
     */
    static <A> NonEmptyIterable<A> nonEmptyIterable(A head, Iterable<A> tail) {
        return EnhancedIterables.nonEmptyIterable(head, tail);
    }

    /**
     * Creates a {@code NonEmptyIterable} containing the given elements.
     * <p>
     * Note that this method actually returns an {@link ImmutableNonEmptyFiniteIterable}, which is
     * also a {@link NonEmptyIterable}.
     *
     * @param first the first element
     * @param more  the remaining elements
     * @param <A>   the element type
     * @return an {@code ImmutableNonEmptyFiniteIterable<A>}
     */
    @SuppressWarnings("varargs")
    @SafeVarargs
    static <A> ImmutableNonEmptyFiniteIterable<A> of(A first, A... more) {
        if (more.length > 0) {
            return EnhancedIterables.of(first, more);
        } else {
            return EnhancedIterables.singleton(first);
        }
    }

    /**
     * Returns an infinite {@code ImmutableNonEmptyIterable} that repeatedly iterates a given element.
     *
     * @param element the value to repeat
     * @param <A>     the element type
     * @return an {@code ImmutableNonEmptyIterable<A>}
     */
    static <A> ImmutableNonEmptyIterable<A> repeat(A element) {
        return EnhancedIterables.repeat(element);
    }

}
