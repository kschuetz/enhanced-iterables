package dev.marksman.enhancediterables;

import com.jnape.palatable.lambda.functions.Fn1;
import com.jnape.palatable.lambda.functions.Fn2;
import com.jnape.palatable.lambda.functions.builtin.fn2.Cons;
import com.jnape.palatable.lambda.functions.builtin.fn2.Intersperse;
import com.jnape.palatable.lambda.functions.builtin.fn2.Map;
import com.jnape.palatable.lambda.functions.builtin.fn2.PrependAll;
import com.jnape.palatable.lambda.functions.builtin.fn3.ZipWith;
import com.jnape.palatable.lambda.monoid.builtin.Concat;

import java.util.Iterator;

import static dev.marksman.enhancediterables.EnhancedIterable.enhance;
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
     * Returns an {@link EnhancedIterable} containing all subsequent elements beyond the first.
     *
     * @return an {@code EnhancedIterable<A>}.  May be empty.
     */
    EnhancedIterable<A> tail();

    /**
     * Lazily concatenates an {@code Iterable} to the end of this {@code NonEmptyIterable},
     * yielding a new {@code NonEmptyIterable}.
     *
     * @param other an {@link Iterable}
     * @return a <code>NonEmptyIterable&lt;A&gt;</code>
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
     * @return a <code>NonEmptyIterable&lt;B&gt;</code>
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
     * @return a <code>NonEmptyIterable&lt;A&gt;</code>
     */
    @Override
    default NonEmptyIterable<A> intersperse(A separator) {
        return nonEmptyIterableOrThrow(Intersperse.intersperse(separator, this));
    }

    @Override
    default Iterator<A> iterator() {
        return Cons.cons(head(), tail()).iterator();
    }

    /**
     * Returns a new {@code NonEmptyIterable} with the provided separator value injected before each value of this
     * {@code NonEmptyIterable}.
     *
     * @param separator the separator value
     * @return a <code>NonEmptyIterable&lt;A&gt;</code>
     */
    @Override
    default NonEmptyIterable<A> prependAll(A separator) {
        return nonEmptyIterableOrThrow(PrependAll.prependAll(separator, this));
    }

    default <B, C> NonEmptyIterable<C> zipWith(Fn2<A, B, C> fn, NonEmptyIterable<B> other) {
        requireNonNull(fn);
        requireNonNull(other);
        return nonEmptyIterable(fn.apply(head(), other.head()),
                ZipWith.zipWith(fn, tail(), other.tail()));
    }

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
        EnhancedIterable<A> enhancedTail = enhance(tail);
        return new NonEmptyIterable<A>() {
            @Override
            public A head() {
                return head;
            }

            @Override
            public EnhancedIterable<A> tail() {
                return enhancedTail;
            }
        };
    }

}
