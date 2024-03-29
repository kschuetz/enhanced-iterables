package software.kes.enhancediterables;

import com.jnape.palatable.lambda.adt.Maybe;
import com.jnape.palatable.lambda.adt.hlist.Tuple2;
import com.jnape.palatable.lambda.functions.Fn1;
import com.jnape.palatable.lambda.functions.Fn2;
import com.jnape.palatable.lambda.functions.builtin.fn1.Init;
import com.jnape.palatable.lambda.functions.builtin.fn1.Reverse;
import com.jnape.palatable.lambda.functions.builtin.fn2.CartesianProduct;
import com.jnape.palatable.lambda.functions.builtin.fn2.Cons;
import com.jnape.palatable.lambda.functions.builtin.fn2.Intersperse;
import com.jnape.palatable.lambda.functions.builtin.fn2.MagnetizeBy;
import com.jnape.palatable.lambda.functions.builtin.fn2.Map;
import com.jnape.palatable.lambda.functions.builtin.fn2.PrependAll;
import com.jnape.palatable.lambda.functions.builtin.fn3.ZipWith;
import com.jnape.palatable.lambda.monoid.builtin.Concat;

import static com.jnape.palatable.lambda.adt.Maybe.just;
import static java.util.Objects.requireNonNull;
import static software.kes.enhancediterables.Wrapped.unwrap;

/**
 * An {@code EnhancedIterable} that is finite, safe from mutation, and guaranteed to contain at least one element.
 *
 * @param <A> the element type
 */
public interface ImmutableNonEmptyFiniteIterable<A> extends ImmutableFiniteIterable<A>, ImmutableNonEmptyIterable<A>,
        NonEmptyFiniteIterable<A> {

    /**
     * Returns an {@code ImmutableFiniteIterable} containing all subsequent elements of this one beyond the first.
     *
     * @return an {@code ImmutableFiniteIterable<A>}
     */
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
        return EnhancedIterables.unsafeImmutableNonEmptyFiniteIterable(Concat.concat(unwrap(this), unwrap(other)));
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
        return EnhancedIterables.unsafeImmutableNonEmptyFiniteIterable(CartesianProduct.cartesianProduct(unwrap(this), unwrap(other)));
    }

    /**
     * Returns an infinite {@code ImmutableNonEmptyIterable} that repeatedly cycles this {@code ImmutableNonEmptyFiniteIterable}'s elements,
     * in order.
     *
     * @return an {@code ImmutableNonEmptyIterable<A>}
     */
    default ImmutableNonEmptyIterable<A> cycle() {
        return EnhancedIterables.nonEmptyCycle(this);
    }

    /**
     * Returns a {@code ImmutableNonEmptyFiniteIterable} of the distinct values from this {@link ImmutableNonEmptyFiniteIterable}.
     *
     * @return a {@code ImmutableNonEmptyFiniteIterable<A>}
     */
    default ImmutableNonEmptyFiniteIterable<A> distinct() {
        return EnhancedIterables.nonEmptyDistinct(this);
    }

    /**
     * Returns a new {@code ImmutableNonEmptyFiniteIterable} by applying a function to all elements of this {@code ImmutableNonEmptyFiniteIterable}.
     *
     * @param f   a function from {@code A} to {@code B}.
     *            This function should be referentially transparent and not perform side-effects.
     *            It may be called zero or more times for each element.
     * @param <B> the type returned by {@code f}
     * @return an {@code ImmutableNonEmptyFiniteIterable<B>}
     */
    @Override
    default <B> ImmutableNonEmptyFiniteIterable<B> fmap(Fn1<? super A, ? extends B> f) {
        requireNonNull(f);
        return EnhancedIterables.unsafeImmutableNonEmptyFiniteIterable(Map.map(f, unwrap(this)));
    }

    /**
     * Returns an {@code ImmutableFiniteIterable} containing all of elements of this one, except for the last element.
     *
     * @return an {@code ImmutableFiniteIterable<A>}
     */
    @Override
    default ImmutableFiniteIterable<A> init() {
        return EnhancedIterables.immutableFiniteIterable(Init.init(unwrap(this)));
    }

    /**
     * Returns a new {@code ImmutableNonEmptyFiniteIterable} with the provided separator value injected between each value of this
     * {@code ImmutableNonEmptyFiniteIterable}.
     * <p>
     * If this {@code ImmutableNonEmptyFiniteIterable} contains only one element, it is left untouched.
     *
     * @param separator the separator value
     * @return an {@code ImmutableNonEmptyFiniteIterable<A>}
     */
    @Override
    default ImmutableNonEmptyFiniteIterable<A> intersperse(A separator) {
        return EnhancedIterables.unsafeImmutableNonEmptyFiniteIterable(Intersperse.intersperse(separator, unwrap(this)));
    }

    /**
     * Returns an {@code Iterable} of contiguous groups of elements in this {@code ImmutableNonEmptyFiniteIterable} that match a
     * predicate pairwise.
     *
     * @param predicate the predicate function.
     *                  This function should be referentially transparent and not perform side-effects.
     *                  It may be called zero or more times for each element.
     * @return an {@code ImmutableNonEmptyFiniteIterable<ImmutableNonEmptyFiniteIterable<A>>} containing the contiguous groups
     */
    @Override
    default ImmutableNonEmptyFiniteIterable<? extends ImmutableNonEmptyFiniteIterable<A>> magnetizeBy(Fn2<A, A, Boolean> predicate) {
        requireNonNull(predicate);
        return EnhancedIterables.unsafeImmutableNonEmptyFiniteIterable(MagnetizeBy.magnetizeBy(predicate, unwrap(this)))
                .fmap(EnhancedIterables::unsafeImmutableNonEmptyFiniteIterable);
    }

    /**
     * Lazily prepends an element to the front of this {@code ImmutableNonEmptyFiniteIterable}, yielding a new {@code ImmutableNonEmptyFiniteIterable}.
     *
     * @param element the element to prepend
     * @return a {@code ImmutableNonEmptyFiniteIterable<A>}
     */
    @Override
    default ImmutableNonEmptyFiniteIterable<A> prepend(A element) {
        return EnhancedIterables.unsafeImmutableNonEmptyFiniteIterable(Cons.cons(element, unwrap(this)));
    }

    /**
     * Returns a new {@code ImmutableNonEmptyFiniteIterable} with the provided separator value injected before each value of this
     * {@code ImmutableNonEmptyFiniteIterable}.
     *
     * @param separator the separator value
     * @return an {@code ImmutableNonEmptyFiniteIterable<A>}
     */
    @Override
    default ImmutableNonEmptyFiniteIterable<A> prependAll(A separator) {
        return EnhancedIterables.unsafeImmutableNonEmptyFiniteIterable(PrependAll.prependAll(separator, unwrap(this)));
    }

    /**
     * Returns a reversed representation of this {@code ImmutableNonEmptyFiniteIterable}.
     * <p>
     * Note that reversing is deferred until the returned {@code Iterable} is iterated.
     *
     * @return an {@code ImmutableNonEmptyFiniteIterable<A>}
     */
    @Override
    default ImmutableNonEmptyFiniteIterable<A> reverse() {
        return EnhancedIterables.unsafeImmutableNonEmptyFiniteIterable(Reverse.reverse(unwrap(this)));
    }

    /**
     * Always succeeds because {@code ImmutableNonEmptyFiniteIterable}s are always finite.
     *
     * @return this {@code ImmutableNonEmptyFiniteIterable} wrapped in a `just`
     */
    @Override
    default Maybe<? extends ImmutableNonEmptyFiniteIterable<A>> toFinite() {
        return just(this);
    }

    /**
     * Always succeeds because {@code ImmutableNonEmptyFiniteIterable}s are always non-empty.
     *
     * @return this {@code ImmutableNonEmptyFiniteIterable} wrapped in a `just`
     */
    @Override
    default Maybe<? extends ImmutableNonEmptyFiniteIterable<A>> toNonEmpty() {
        return just(this);
    }

    /**
     * Zips together this {@code ImmutableNonEmptyFiniteIterable} with another {@code ImmutableNonEmptyIterable} by applying a zipping function.
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
    default <B, C> ImmutableNonEmptyFiniteIterable<C> zipWith(Fn2<A, B, C> fn, ImmutableNonEmptyIterable<B> other) {
        requireNonNull(fn);
        requireNonNull(other);
        return EnhancedIterables.unsafeImmutableNonEmptyFiniteIterable(ZipWith.zipWith(fn, unwrap(this), unwrap(other)));
    }

    /**
     * Creates an {@code ImmutableNonEmptyFiniteIterable}.
     *
     * @param head the first element
     * @param tail the remaining elements.  May be empty.
     * @param <A>  the element type
     * @return a {@code ImmutableNonEmptyFiniteIterable<A>}
     */
    static <A> ImmutableNonEmptyFiniteIterable<A> immutableNonEmptyFiniteIterable(A head, ImmutableFiniteIterable<A> tail) {
        return EnhancedIterables.immutableNonEmptyFiniteIterable(head, tail);
    }

    /**
     * Creates an {@code ImmutableNonEmptyFiniteIterable} containing the given elements.
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

}
