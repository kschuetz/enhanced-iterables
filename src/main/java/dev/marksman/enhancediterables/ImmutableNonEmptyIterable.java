package dev.marksman.enhancediterables;

import com.jnape.palatable.lambda.functions.Fn1;
import com.jnape.palatable.lambda.functions.Fn2;
import com.jnape.palatable.lambda.functions.builtin.fn2.Intersperse;
import com.jnape.palatable.lambda.functions.builtin.fn2.Map;
import com.jnape.palatable.lambda.functions.builtin.fn2.PrependAll;
import com.jnape.palatable.lambda.functions.builtin.fn3.ZipWith;
import com.jnape.palatable.lambda.monoid.builtin.Concat;

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
     * @return an {@link ImmutableNonEmptyIterable<B>}
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
     * @return an {@link ImmutableNonEmptyIterable<A>}
     */
    @Override
    default ImmutableNonEmptyIterable<A> intersperse(A separator) {
        return immutableNonEmptyIterableOrThrow(Intersperse.intersperse(separator, this));
    }

    @Override
    default ImmutableNonEmptyIterable<A> prependAll(A a) {
        return immutableNonEmptyIterableOrThrow(PrependAll.prependAll(a, this));
    }

    default <B, C> ImmutableNonEmptyIterable<C> zipWith(Fn2<A, B, C> fn, ImmutableNonEmptyIterable<B> other) {
        requireNonNull(fn);
        requireNonNull(other);
        return immutableNonEmptyIterableOrThrow(ZipWith.zipWith(fn, this, other));
    }

    default <B, C> ImmutableNonEmptyFiniteIterable<C> zipWith(Fn2<A, B, C> fn, ImmutableNonEmptyFiniteIterable<B> other) {
        requireNonNull(fn);
        requireNonNull(other);
        return immutableNonEmptyFiniteIterableOrThrow(ZipWith.zipWith(fn, this, other));
    }

    static <A> ImmutableNonEmptyIterable<A> immutableNonEmptyIterable(A head, ImmutableIterable<A> tail) {
        requireNonNull(tail);
        return new ImmutableNonEmptyIterable<A>() {
            @Override
            public A head() {
                return head;
            }

            @Override
            public ImmutableIterable<A> tail() {
                return tail;
            }
        };
    }

    @SafeVarargs
    static <A> ImmutableNonEmptyFiniteIterable<A> of(A first, A... more) {
        return EnhancedIterables.of(first, more);
    }

}
