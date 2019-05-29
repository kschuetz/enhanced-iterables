package dev.marksman.enhancediterables;

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

import static dev.marksman.enhancediterables.EnhancedIterables.nonEmptyFiniteIterableOrThrow;
import static dev.marksman.enhancediterables.FiniteIterable.finiteIterable;
import static java.util.Objects.requireNonNull;

/**
 * An {@code EnhancedIterable} that is finite and guaranteed to contain at least one element.
 *
 * @param <A> the element type
 */
public interface NonEmptyFiniteIterable<A> extends FiniteIterable<A>, NonEmptyIterable<A> {

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
     * @return an {@link NonEmptyFiniteIterable<B>}
     */
    @Override
    default <B> NonEmptyFiniteIterable<B> fmap(Fn1<? super A, ? extends B> f) {
        requireNonNull(f);
        return nonEmptyFiniteIterableOrThrow(Map.map(f, this));
    }

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
     * @return an {@link NonEmptyFiniteIterable<A>}
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
     * @return an {@link NonEmptyFiniteIterable<A>}
     */
    @Override
    default NonEmptyFiniteIterable<A> prependAll(A separator) {
        return nonEmptyFiniteIterableOrThrow(PrependAll.prependAll(separator, this));
    }

    @Override
    default NonEmptyFiniteIterable<A> reverse() {
        return nonEmptyFiniteIterableOrThrow(Reverse.reverse(this));
    }

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
        requireNonNull(tail);
        return new NonEmptyFiniteIterable<A>() {
            @Override
            public A head() {
                return head;
            }

            @Override
            public FiniteIterable<A> tail() {
                return tail;
            }
        };
    }

    static <A> NonEmptyFiniteIterable<A> nonEmptyFiniteIterable(A head, Collection<A> tail) {
        return nonEmptyFiniteIterable(head, finiteIterable(tail));
    }

}
