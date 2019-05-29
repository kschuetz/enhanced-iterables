package dev.marksman.enhancediterables;

import com.jnape.palatable.lambda.adt.coproduct.CoProduct2;
import com.jnape.palatable.lambda.adt.hlist.Tuple2;
import com.jnape.palatable.lambda.functions.Fn1;
import com.jnape.palatable.lambda.functions.Fn2;
import com.jnape.palatable.lambda.functions.builtin.fn1.Tails;
import com.jnape.palatable.lambda.functions.builtin.fn2.*;
import com.jnape.palatable.lambda.functions.builtin.fn3.ZipWith;
import com.jnape.palatable.lambda.monoid.builtin.Concat;

import static com.jnape.palatable.lambda.adt.hlist.HList.tuple;
import static dev.marksman.enhancediterables.EnhancedIterables.*;
import static dev.marksman.enhancediterables.Validation.*;
import static java.util.Objects.requireNonNull;

/**
 * An {@code EnhancedIterable} that is safe from mutation.
 * <p>
 * May be infinite, finite, or empty.
 *
 * @param <A> the element type
 */
public interface ImmutableIterable<A> extends EnhancedIterable<A> {

    /**
     * Lazily appends an element to the end of this {@code ImmutableIterable}, yielding a new {@code ImmutableNonEmptyIterable}.
     *
     * @param element the element to append
     * @return an {@link ImmutableNonEmptyIterable<A>}
     */
    @Override
    default ImmutableNonEmptyIterable<A> append(A element) {
        return immutableNonEmptyIterableOrThrow(Snoc.snoc(element, this));
    }

    /**
     * Lazily concatenates another {@code ImmutableIterable} to the end of this {@code ImmutableIterable},
     * yielding a new {@code ImmutableIterable}.
     *
     * @param other the other {@link ImmutableIterable}
     * @return an {@code ImmutableIterable<A>}
     */
    default ImmutableIterable<A> concat(ImmutableIterable<A> other) {
        requireNonNull(other);
        return EnhancedIterables.immutableIterable(Concat.concat(this, other));
    }

    /**
     * Lazily concatenates an {@code ImmutableNonEmptyIterable} to the end of this {@code ImmutableIterable},
     * yielding a new {@code ImmutableNonEmptyIterable}.
     *
     * @param other an {@link ImmutableNonEmptyIterable}
     * @return an {@code ImmutableNonEmptyIterable<A>}
     */
    default ImmutableNonEmptyIterable<A> concat(ImmutableNonEmptyIterable<A> other) {
        requireNonNull(other);
        return EnhancedIterables.immutableNonEmptyIterableOrThrow(Concat.concat(this, other));
    }

    /**
     * Returns a new {@code ImmutableIterable} that drops the first {@code count} elements of this {@code ImmutableIterable}.
     *
     * @param count the number of elements to drop from this {@code ImmutableIterable}.
     *              Must be &gt;= 0.
     *              May exceed size of this {@code ImmutableIterable}, in which case, the result will be an
     *              empty {@code ImmutableIterable}.
     * @return an {@code ImmutableIterable<A>}
     */
    @Override
    default ImmutableIterable<A> drop(int count) {
        validateDrop(count);
        return immutableIterable(Drop.drop(count, this));
    }

    /**
     * Returns a new {@code ImmutableIterable} that skips the first contiguous group of elements of this
     * {@code ImmutableIterable} that satisfy a predicate.
     * <p>
     * Iteration begins at the first element for which the predicate evaluates to false.
     *
     * @param predicate the predicate; should be referentially transparent and not have side-effects
     * @return an {@link ImmutableIterable<A>}
     */
    @Override
    default ImmutableIterable<A> dropWhile(Fn1<? super A, ? extends Boolean> predicate) {
        requireNonNull(predicate);
        return immutableIterable(DropWhile.dropWhile(predicate, this));
    }

    /**
     * Returns a new {@code ImmutableIterable} that contains all elements of this {@code ImmutableIterable}
     * that satisfy a predicate.
     *
     * @param predicate the predicate; should be referentially transparent and not have side-effects
     * @return an {@link ImmutableIterable<A>}
     */
    @Override
    default ImmutableIterable<A> filter(Fn1<? super A, ? extends Boolean> predicate) {
        requireNonNull(predicate);
        return immutableIterable(Filter.<A>filter(predicate).apply(this));
    }

    /**
     * Returns a new {@code ImmutableIterable} by applying a function to all elements of this {@code ImmutableIterable}.
     *
     * @param f   a function from {@code A} to {@code B}.
     *            This function should be referentially transparent and not perform side-effects.
     *            It may be called zero or more times for each element.
     * @param <B> the type returned by {@code f}
     * @return an {@link ImmutableIterable<B>}
     */
    @Override
    default <B> ImmutableIterable<B> fmap(Fn1<? super A, ? extends B> f) {
        requireNonNull(f);
        return immutableIterable(Map.map(f, this));
    }

    /**
     * Returns a new {@code ImmutableIterable} with the provided separator value injected between each value of this
     * {@code ImmutableIterable}.
     * <p>
     * If this {@code ImmutableIterable} contains fewer than two elements, it is left untouched.
     *
     * @param separator the separator value
     * @return an {@link ImmutableIterable<A>}
     */
    @Override
    default ImmutableIterable<A> intersperse(A separator) {
        return immutableIterable(Intersperse.intersperse(separator, this));
    }

    /**
     * Partitions this {@code ImmutableIterable} given a disjoint mapping function.
     * <p>
     * Note that while the returned tuple must be constructed eagerly, the left and right iterables contained therein
     * are both lazy, so comprehension over infinite iterables is supported.
     *
     * @param function the mapping function
     * @param <B>      The output left Iterable element type, as well as the CoProduct2 A type
     * @param <C>      The output right Iterable element type, as well as the CoProduct2 B type
     * @return a <code>Tuple2&lt;ImmutableIterable&lt;B&gt;, ImmutableIterable&lt;C&gt;&gt;</code>
     */
    @Override
    default <B, C> Tuple2<? extends ImmutableIterable<B>, ? extends ImmutableIterable<C>> partition(
            Fn1<? super A, ? extends CoProduct2<B, C, ?>> function) {
        requireNonNull(function);
        Tuple2<Iterable<B>, Iterable<C>> partitionResult = Partition.partition(function, this);
        return tuple(immutableIterable(partitionResult._1()),
                immutableIterable(partitionResult._2()));
    }

    /**
     * Lazily prepends an element to the front of this {@code ImmutableIterable}, yielding a new {@code ImmutableNonEmptyIterable}.
     *
     * @param element the element to prepend
     * @return a {@link ImmutableNonEmptyIterable<A>}
     */
    @Override
    default ImmutableNonEmptyIterable<A> prepend(A element) {
        return ImmutableNonEmptyIterable.immutableNonEmptyIterable(element, this);
    }

    /**
     * Returns a new {@code ImmutableIterable} with the provided separator value injected before each value of this
     * {@code ImmutableIterable}.
     * <p>
     * If this {@code ImmutableIterable} is empty, it is left untouched.
     *
     * @param separator the separator value
     * @return an {@link ImmutableIterable<A>}
     */
    @Override
    default ImmutableIterable<A> prependAll(A separator) {
        return immutableIterable(PrependAll.prependAll(separator, this));
    }

    /**
     * "Slide" a window of {@code k} elements across the {@code ImmutableIterable} by one element at a time.
     * <p>
     * Example:
     *
     * <code>ImmutableIterable.of(1, 2, 3, 4, 5).slide(2); // [[1, 2], [2, 3], [3, 4], [4, 5]]</code>
     *
     * @param k the number of elements in the sliding window.  Must be &gt;= 1.
     * @return an {@code ImmutableIterable<ImmutableNonEmptyFiniteIterable<A>>}
     */
    @Override
    default ImmutableIterable<? extends ImmutableNonEmptyFiniteIterable<A>> slide(int k) {
        validateSlide(k);
        return immutableIterable(Map.map(EnhancedIterables::immutableNonEmptyFiniteIterableOrThrow,
                Slide.slide(k, this)));
    }

    @Override
    default Tuple2<? extends ImmutableIterable<A>, ? extends ImmutableIterable<A>> span(Fn1<? super A, ? extends Boolean> predicate) {
        requireNonNull(predicate);
        Tuple2<Iterable<A>, Iterable<A>> spanResult = Span.<A>span(predicate).apply(this);
        return tuple(immutableIterable(spanResult._1()),
                immutableIterable(spanResult._2()));
    }

    @Override
    default NonEmptyIterable<? extends ImmutableIterable<A>> tails() {
        return nonEmptyIterableOrThrow(Map.map(EnhancedIterables::immutableIterable, Tails.tails(this)));
    }

    @Override
    default ImmutableFiniteIterable<A> take(int count) {
        validateTake(count);
        return immutableFiniteIterable(Take.take(count, this));
    }

    @Override
    default ImmutableIterable<A> takeWhile(Fn1<? super A, ? extends Boolean> predicate) {
        requireNonNull(predicate);
        return immutableIterable(TakeWhile.takeWhile(predicate, this));
    }

    default <B, C> ImmutableIterable<C> zipWith(Fn2<A, B, C> fn, ImmutableIterable<B> other) {
        requireNonNull(fn);
        requireNonNull(other);
        return immutableIterable(ZipWith.zipWith(fn, this, other));
    }

    default <B, C> ImmutableFiniteIterable<C> zipWith(Fn2<A, B, C> fn, ImmutableFiniteIterable<B> other) {
        requireNonNull(fn);
        requireNonNull(other);
        return immutableFiniteIterable(ZipWith.zipWith(fn, this, other));
    }

    @SafeVarargs
    static <A> ImmutableNonEmptyFiniteIterable<A> of(A first, A... more) {
        return EnhancedIterables.of(first, more);
    }

}
