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

import static dev.marksman.enhancediterables.EnhancedIterables.immutableFiniteIterable;
import static dev.marksman.enhancediterables.EnhancedIterables.immutableNonEmptyFiniteIterableOrThrow;
import static java.util.Objects.requireNonNull;

/**
 * An {@code EnhancedIterable} that is finite, safe from mutation, and guaranteed to contain at least one element.
 *
 * @param <A> the element type
 */
public interface ImmutableNonEmptyFiniteIterable<A> extends ImmutableFiniteIterable<A>, ImmutableNonEmptyIterable<A>,
        NonEmptyFiniteIterable<A> {

    @Override
    ImmutableFiniteIterable<A> tail();

    /**
     * Lazily concatenates another {@code ImmutableFiniteIterable} to the end of this {@code ImmutableNonEmptyFiniteIterable},
     * yielding a new {@code ImmutableIterable}.
     *
     * @param other the other {@link ImmutableIterable}
     * @return an {@code ImmutableIterable<A>}
     */
    @Override
    default ImmutableNonEmptyFiniteIterable<A> concat(ImmutableFiniteIterable<A> other) {
        requireNonNull(other);
        return immutableNonEmptyFiniteIterableOrThrow(Concat.concat(this, other));
    }

    /**
     * Returns the lazily computed cartesian product of this {@code ImmutableNonEmptyFiniteIterable} with another {@code ImmutableNonEmptyFiniteIterable}.
     *
     * @param other an {@code ImmutableNonEmptyFiniteIterable} of any type
     * @param <B>   the type of the other {@code ImmutableNonEmptyFiniteIterable}
     * @return a {@code ImmutableNonEmptyFiniteIterable<Tuple2<A, B>>}
     */
    default <B> ImmutableNonEmptyFiniteIterable<Tuple2<A, B>> cross(ImmutableNonEmptyFiniteIterable<B> other) {
        requireNonNull(other);
        return immutableNonEmptyFiniteIterableOrThrow(CartesianProduct.cartesianProduct(this, other));
    }

    /**
     * Returns a new {@code ImmutableNonEmptyFiniteIterable} by applying a function to all elements of this {@code ImmutableNonEmptyFiniteIterable}.
     *
     * @param f   a function from {@code A} to {@code B}.
     *            This function should be referentially transparent and not perform side-effects.
     *            It may be called zero or more times for each element.
     * @param <B> the type returned by {@code f}
     * @return an {@link ImmutableNonEmptyFiniteIterable<B>}
     */
    @Override
    default <B> ImmutableNonEmptyFiniteIterable<B> fmap(Fn1<? super A, ? extends B> f) {
        requireNonNull(f);
        return immutableNonEmptyFiniteIterableOrThrow(Map.map(f, this));
    }

    @Override
    default ImmutableFiniteIterable<A> init() {
        return immutableFiniteIterable(Init.init(this));
    }

    @Override
    default ImmutableNonEmptyFiniteIterable<A> intersperse(A a) {
        return immutableNonEmptyFiniteIterableOrThrow(Intersperse.intersperse(a, this));
    }

    @Override
    default ImmutableNonEmptyFiniteIterable<A> prepend(A element) {
        return immutableNonEmptyFiniteIterable(element, this);
    }

    @Override
    default ImmutableNonEmptyFiniteIterable<A> prependAll(A a) {
        return immutableNonEmptyFiniteIterableOrThrow(PrependAll.prependAll(a, this));
    }

    @Override
    default ImmutableNonEmptyFiniteIterable<A> reverse() {
        return immutableNonEmptyFiniteIterableOrThrow(Reverse.reverse(this));
    }

    default <B, C> ImmutableNonEmptyFiniteIterable<C> zipWith(Fn2<A, B, C> fn, ImmutableNonEmptyFiniteIterable<B> other) {
        requireNonNull(fn);
        requireNonNull(other);
        return immutableNonEmptyFiniteIterableOrThrow(ZipWith.zipWith(fn, this, other));
    }

    static <A> ImmutableNonEmptyFiniteIterable<A> immutableNonEmptyFiniteIterable(A head, ImmutableFiniteIterable<A> tail) {
        requireNonNull(tail);
        return new ImmutableNonEmptyFiniteIterable<A>() {
            @Override
            public A head() {
                return head;
            }

            @Override
            public ImmutableFiniteIterable<A> tail() {
                return tail;
            }
        };
    }

    @SafeVarargs
    static <A> ImmutableNonEmptyFiniteIterable<A> of(A first, A... more) {
        return EnhancedIterables.of(first, more);
    }
}
