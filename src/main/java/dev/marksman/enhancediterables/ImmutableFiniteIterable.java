package dev.marksman.enhancediterables;

import com.jnape.palatable.lambda.adt.coproduct.CoProduct2;
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

    /**
     * Lazily appends an element to the end of this {@code ImmutableFiniteIterable}, yielding a new {@code ImmutableNonEmptyFiniteIterable}.
     *
     * @param element the element to append
     * @return an {@link ImmutableNonEmptyFiniteIterable<A>}
     */
    @Override
    default ImmutableNonEmptyFiniteIterable<A> append(A element) {
        return immutableNonEmptyFiniteIterableOrThrow(Snoc.snoc(element, this));
    }

    /**
     * Lazily concatenates another {@code ImmutableFiniteIterable} to the end of this {@code ImmutableFiniteIterable},
     * yielding a new {@code ImmutableFiniteIterable}.
     *
     * @param other the other {@link ImmutableFiniteIterable}
     * @return an {@code ImmutableFiniteIterable<A>}
     */
    default ImmutableFiniteIterable<A> concat(ImmutableFiniteIterable<A> other) {
        requireNonNull(other);
        return immutableFiniteIterable(Concat.concat(this, other));
    }

    /**
     * Lazily concatenates an {@code ImmutableNonEmptyFiniteIterable} to the end of this {@code ImmutableFiniteIterable},
     * yielding a new {@code ImmutableNonEmptyFiniteIterable}.
     *
     * @param other an {@link ImmutableNonEmptyFiniteIterable}
     * @return an {@code ImmutableNonEmptyFiniteIterable<A>}
     */
    default ImmutableNonEmptyFiniteIterable<A> concat(ImmutableNonEmptyFiniteIterable<A> other) {
        requireNonNull(other);
        return immutableNonEmptyFiniteIterableOrThrow(Concat.concat(this, other));
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

    /**
     * Returns a new {@code ImmutableFiniteIterable} that skips the first contiguous group of elements of this
     * {@code ImmutableFiniteIterable} that satisfy a predicate.
     * <p>
     * Iteration begins at the first element for which the predicate evaluates to false.
     *
     * @param predicate the predicate; should be referentially transparent and not have side-effects
     * @return an {@link EnhancedIterable}
     */
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

    /**
     * Partitions this {@code ImmutableFiniteIterable} given a disjoint mapping function.
     *
     * @param function the mapping function
     * @param <B>      The output left Iterable element type, as well as the CoProduct2 A type
     * @param <C>      The output right Iterable element type, as well as the CoProduct2 B type
     * @return a <code>Tuple2&lt;ImmutableFiniteIterable&lt;B&gt;, ImmutableFiniteIterable&lt;C&gt;&gt;</code>
     */
    @Override
    default <B, C> Tuple2<? extends ImmutableFiniteIterable<B>, ? extends ImmutableFiniteIterable<C>> partition(
            Fn1<? super A, ? extends CoProduct2<B, C, ?>> function) {
        requireNonNull(function);
        Tuple2<Iterable<B>, Iterable<C>> partitionResult = Partition.partition(function, this);
        return tuple(immutableFiniteIterable(partitionResult._1()),
                immutableFiniteIterable(partitionResult._2()));
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
     * @param k the number of elements in the sliding window.  Must be &gt;= 1.
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
