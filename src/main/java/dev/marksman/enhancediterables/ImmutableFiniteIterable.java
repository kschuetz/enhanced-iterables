package dev.marksman.enhancediterables;

import com.jnape.palatable.lambda.adt.hlist.Tuple2;
import com.jnape.palatable.lambda.functions.Fn1;
import com.jnape.palatable.lambda.functions.Fn2;
import com.jnape.palatable.lambda.functions.builtin.fn1.Inits;
import com.jnape.palatable.lambda.functions.builtin.fn1.Reverse;
import com.jnape.palatable.lambda.functions.builtin.fn1.Tails;
import com.jnape.palatable.lambda.functions.builtin.fn2.*;
import com.jnape.palatable.lambda.functions.builtin.fn3.ZipWith;
import com.jnape.palatable.lambda.monoid.builtin.Concat;

import static com.jnape.palatable.lambda.adt.hlist.HList.tuple;
import static dev.marksman.enhancediterables.EnhancedIterables.*;
import static dev.marksman.enhancediterables.Validation.*;
import static java.util.Objects.requireNonNull;

/**
 * An {@code EnhancedIterable} that is both finite and safe from mutation.
 *
 * @param <A> the element type
 */
public interface ImmutableFiniteIterable<A> extends ImmutableIterable<A>, FiniteIterable<A> {

    @Override
    default ImmutableNonEmptyFiniteIterable<A> append(A element) {
        return immutableNonEmptyFiniteIterableOrThrow(Snoc.snoc(element, this));
    }

    default ImmutableFiniteIterable<A> concat(ImmutableFiniteIterable<A> other) {
        requireNonNull(other);
        return EnhancedIterables.immutableFiniteIterable(Concat.concat(this, other));
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

    @Override
    default ImmutableFiniteIterable<A> dropWhile(Fn1<? super A, ? extends Boolean> predicate) {
        requireNonNull(predicate);
        return immutableFiniteIterable(DropWhile.dropWhile(predicate, this));
    }

    @Override
    default ImmutableFiniteIterable<A> filter(Fn1<? super A, ? extends Boolean> predicate) {
        requireNonNull(predicate);
        return immutableFiniteIterable(Filter.<A>filter(predicate).apply(this));
    }

    @Override
    default <B> ImmutableFiniteIterable<B> fmap(Fn1<? super A, ? extends B> f) {
        requireNonNull(f);
        return immutableFiniteIterable(Map.map(f, this));
    }

    default NonEmptyIterable<? extends ImmutableFiniteIterable<A>> inits() {
        return nonEmptyIterableOrThrow(Map.map(EnhancedIterables::immutableFiniteIterable, Inits.inits(this)));
    }

    @Override
    default ImmutableFiniteIterable<A> intersperse(A a) {
        return immutableFiniteIterable(Intersperse.intersperse(a, this));
    }

    @Override
    default ImmutableNonEmptyFiniteIterable<A> prepend(A element) {
        return ImmutableNonEmptyFiniteIterable.immutableNonEmptyFiniteIterable(element, this);
    }

    @Override
    default ImmutableFiniteIterable<A> prependAll(A a) {
        return immutableFiniteIterable(PrependAll.prependAll(a, this));
    }

    @Override
    default ImmutableFiniteIterable<A> reverse() {
        return immutableFiniteIterable(Reverse.reverse(this));
    }

    /**
     * "Slide" a window of {@code k} elements across the {@code ImmutableFiniteIterable} by one element at a time.
     * <p>
     * Example:
     *
     * <code>ImmutableFiniteIterable.of(1, 2, 3, 4, 5).slide(2); // [[1, 2], [2, 3], [3, 4], [4, 5]]</code>
     *
     * @param k the number of elements in the sliding window.  Must be >= 1.
     * @return an {@code ImmutableFiniteIterable<ImmutableNonEmptyFiniteIterable<A>>}
     */
    @Override
    default ImmutableFiniteIterable<? extends ImmutableNonEmptyFiniteIterable<A>> slide(int k) {
        validateSlide(k);
        return immutableFiniteIterable(Map.map(EnhancedIterables::immutableNonEmptyFiniteIterableOrThrow,
                Slide.slide(k, this)));
    }

    @Override
    default Tuple2<? extends ImmutableFiniteIterable<A>, ? extends ImmutableFiniteIterable<A>> span(Fn1<? super A, ? extends Boolean> predicate) {
        requireNonNull(predicate);
        Tuple2<Iterable<A>, Iterable<A>> spanResult = Span.<A>span(predicate).apply(this);
        return tuple(immutableFiniteIterable(spanResult._1()),
                immutableFiniteIterable(spanResult._2()));
    }

    @Override
    default NonEmptyIterable<? extends ImmutableFiniteIterable<A>> tails() {
        return nonEmptyIterableOrThrow(Map.map(EnhancedIterables::immutableFiniteIterable, Tails.tails(this)));
    }

    @Override
    default ImmutableFiniteIterable<A> take(int count) {
        validateTake(count);
        return immutableFiniteIterable(Take.take(count, this));
    }

    @Override
    default ImmutableFiniteIterable<A> takeWhile(Fn1<? super A, ? extends Boolean> predicate) {
        requireNonNull(predicate);
        return immutableFiniteIterable(TakeWhile.takeWhile(predicate, this));
    }

    default <B, C> ImmutableFiniteIterable<C> zipWith(Fn2<A, B, C> fn, ImmutableIterable<B> other) {
        requireNonNull(fn);
        requireNonNull(other);
        return immutableFiniteIterable(ZipWith.zipWith(fn, this, other));
    }

    @SafeVarargs
    static <A> ImmutableNonEmptyFiniteIterable<A> of(A first, A... more) {
        return EnhancedIterables.of(first, more);
    }

}
