package dev.marksman.enhancediterables;

import com.jnape.palatable.lambda.adt.Maybe;
import com.jnape.palatable.lambda.adt.coproduct.CoProduct2;
import com.jnape.palatable.lambda.adt.hlist.Tuple2;
import com.jnape.palatable.lambda.functions.Fn0;
import com.jnape.palatable.lambda.functions.Fn1;
import com.jnape.palatable.lambda.functions.Fn2;
import com.jnape.palatable.lambda.functions.builtin.fn1.Tails;
import com.jnape.palatable.lambda.functions.builtin.fn2.*;
import com.jnape.palatable.lambda.functions.builtin.fn3.ZipWith;
import com.jnape.palatable.lambda.functor.Functor;
import com.jnape.palatable.lambda.monoid.builtin.Concat;

import java.util.Collection;

import static com.jnape.palatable.lambda.adt.hlist.HList.tuple;
import static dev.marksman.enhancediterables.EnhancedIterables.*;
import static dev.marksman.enhancediterables.Validation.*;
import static java.util.Objects.requireNonNull;

/**
 * An {@code Iterable} with some additional methods.
 * <p>
 * May be infinite, finite, or empty.
 * <p>
 * Any {@link Iterable} can be upgraded to an {@code EnhancedIterable} by calling {@link EnhancedIterable#enhance(Iterable)}}.
 *
 * @param <A> the element type
 */
public interface EnhancedIterable<A> extends Iterable<A>, Functor<A, EnhancedIterable<?>> {

    /**
     * Lazily appends an element to the end of this {@code EnhancedIterable}, yielding a new {@code NonEmptyIterable}.
     *
     * @param element the element to append
     * @return a {@code NonEmptyIterable<A>}
     */
    default NonEmptyIterable<A> append(A element) {
        return nonEmptyIterableOrThrow(Snoc.snoc(element, this));
    }

    /**
     * Lazily concatenates another {@code Iterable} to the end of this {@code EnhancedIterable},
     * yielding a new {@code EnhancedIterable}.
     *
     * @param other the other {@link Iterable}
     * @return an {@code EnhancedIterable<A>}
     */
    default EnhancedIterable<A> concat(Iterable<A> other) {
        requireNonNull(other);
        return enhance(Concat.concat(this, other));
    }

    /**
     * Lazily concatenates a {@code NonEmptyIterable} to the end of this {@code EnhancedIterable},
     * yielding a new {@code NonEmptyIterable}.
     *
     * @param other a {@link NonEmptyIterable}
     * @return a {@code NonEmptyIterable<A>}
     */
    default NonEmptyIterable<A> concat(NonEmptyIterable<A> other) {
        requireNonNull(other);
        return nonEmptyIterableOrThrow(Concat.concat(this, other));
    }

    /**
     * Returns a new {@code EnhancedIterable} that drops the first {@code count} elements of this {@code EnhancedIterable}.
     *
     * @param count the number of elements to drop from this {@code EnhancedIterable}.
     *              Must be &gt;= 0.
     *              May exceed size of this {@code EnhancedIterable}, in which case, the result will be an
     *              empty {@code EnhancedIterable}.
     * @return an {@code EnhancedIterable<A>}
     */
    default EnhancedIterable<A> drop(int count) {
        validateDrop(count);
        return enhance(Drop.drop(count, this));
    }

    /**
     * Returns a new {@code EnhancedIterable} that skips the first contiguous group of elements of this
     * {@code EnhancedIterable} that satisfy a predicate.
     * <p>
     * Iteration begins at the first element for which the predicate evaluates to false.
     *
     * @param predicate a predicate; should be referentially transparent and not have side-effects
     * @return an {@code EnhancedIterable<A>}
     */
    default EnhancedIterable<A> dropWhile(Fn1<? super A, ? extends Boolean> predicate) {
        requireNonNull(predicate);
        return enhance(DropWhile.dropWhile(predicate, this));
    }

    /**
     * Returns a new {@code EnhancedIterable} that contains all elements of this {@code EnhancedIterable}
     * that satisfy a predicate.
     *
     * @param predicate a predicate; should be referentially transparent and not have side-effects
     * @return an {@code EnhancedIterable<A>}
     */
    default EnhancedIterable<A> filter(Fn1<? super A, ? extends Boolean> predicate) {
        requireNonNull(predicate);
        return enhance(Filter.<A>filter(predicate).apply(this));
    }

    /**
     * Finds the first element of this {@code EnhancedIterable} that satisfies a predicate, if any.
     *
     * @param predicate a predicate; not null
     * @return an element wrapped in a {@link Maybe#just} if a matching element is found;
     * {@link Maybe#nothing} otherwise.
     */
    default Maybe<A> find(Fn1<? super A, ? extends Boolean> predicate) {
        requireNonNull(predicate);
        return Find.find(predicate, this);
    }

    /**
     * Returns a new {@code EnhancedIterable} by applying a function to all elements of this {@code EnhancedIterable}.
     *
     * @param f   a function from {@code A} to {@code B}.
     *            This function should be referentially transparent and not perform side-effects.
     *            It may be called zero or more times for each element.
     * @param <B> the type returned by {@code f}
     * @return an {@code EnhancedIterable<B>}
     */
    default <B> EnhancedIterable<B> fmap(Fn1<? super A, ? extends B> f) {
        requireNonNull(f);
        return enhance(Map.map(f, this));
    }

    /**
     * Returns a new {@code EnhancedIterable} with the provided separator value injected between each value of this
     * {@code EnhancedIterable}.
     * <p>
     * If this {@link EnhancedIterable} contains fewer than two elements, it is left untouched.
     *
     * @param separator the separator value
     * @return an {@code EnhancedIterable<A>}
     */
    default EnhancedIterable<A> intersperse(A separator) {
        return enhance(Intersperse.intersperse(separator, this));
    }

    /**
     * Tests whether this {@code EnhancedIterable} is empty.
     *
     * @return true if this {@link EnhancedIterable} contains no elements, false otherwise
     */
    default boolean isEmpty() {
        return !iterator().hasNext();
    }

    /**
     * Partitions this {@code EnhancedIterable} given a disjoint mapping function.
     * <p>
     * Note that while the returned tuple must be constructed eagerly, the left and right iterables contained therein
     * are both lazy, so comprehension over infinite iterables is supported.
     *
     * @param function the mapping function
     * @param <B>      the output left Iterable element type, as well as the CoProduct2 A type
     * @param <C>      the output right Iterable element type, as well as the CoProduct2 B type
     * @return a {@code Tuple2<EnhancedIterable&lt;B&gt;, EnhancedIterable&lt;C&gt;>}
     */
    default <B, C> Tuple2<? extends EnhancedIterable<B>, ? extends EnhancedIterable<C>> partition(
            Fn1<? super A, ? extends CoProduct2<B, C, ?>> function) {
        requireNonNull(function);
        Tuple2<Iterable<B>, Iterable<C>> partitionResult = Partition.partition(function, this);
        return tuple(enhance(partitionResult._1()), enhance(partitionResult._2()));
    }

    /**
     * Lazily prepends an element to the front of this {@code EnhancedIterable}, yielding a new {@code NonEmptyIterable}.
     *
     * @param element the element to prepend
     * @return a {@code NonEmptyIterable<A>}
     */
    default NonEmptyIterable<A> prepend(A element) {
        return NonEmptyIterable.nonEmptyIterable(element, this);
    }

    /**
     * Returns a new {@code EnhancedIterable} with the provided separator value injected before each value of this
     * {@code EnhancedIterable}.
     * <p>
     * If this {@link EnhancedIterable} is empty, it is left untouched.
     *
     * @param separator the separator value
     * @return an {@code EnhancedIterable<A>}
     */
    default EnhancedIterable<A> prependAll(A separator) {
        return enhance(PrependAll.prependAll(separator, this));
    }

    /**
     * "Slides" a window of {@code k} elements across the {@code EnhancedIterable} by one element at a time.
     * <p>
     * Example:
     *
     * <code>EnhancedIterable.of(1, 2, 3, 4, 5).slide(2); // [[1, 2], [2, 3], [3, 4], [4, 5]]</code>
     *
     * @param k the number of elements in the sliding window.  Must be &gt;= 1.
     * @return an {@code EnhancedIterable<NonEmptyFiniteIterable<A>>}
     */
    default EnhancedIterable<? extends NonEmptyFiniteIterable<A>> slide(int k) {
        validateSlide(k);
        return enhance(Map.map(EnhancedIterables::nonEmptyFiniteIterableOrThrow,
                Slide.slide(k, this)));
    }

    /**
     * Returns a {@code Tuple2} where the first slot is the front contiguous elements of this
     * {@code EnhancedIterable} matching a predicate and the second slot is all the remaining elements.
     *
     * @param predicate a predicate; should be referentially transparent and not have side-effects
     * @return a {@code Tuple2<EnhancedIterable&lt;B&gt;, EnhancedIterable&lt;C&gt;>}
     */
    default Tuple2<? extends EnhancedIterable<A>, ? extends EnhancedIterable<A>> span(Fn1<? super A, ? extends Boolean> predicate) {
        requireNonNull(predicate);
        Tuple2<Iterable<A>, Iterable<A>> spanResult = Span.<A>span(predicate).apply(this);
        return tuple(enhance(spanResult._1()), enhance(spanResult._2()));
    }

    /**
     * Returns an {@code ImmutableNonEmptyIterable} containing all of the subsequences of tail
     * elements of this {@code EnhancedIterable}, ordered by size, starting with the full list.
     * Example:
     *
     * <code>EnhancedIterable.of(1, 2, 3).tails(); // [[1, 2, 3], [2, 3], [3], []]</code>
     *
     * @return an {@code ImmutableNonEmptyIterable<EnhancedIterable<A>>}
     */
    default ImmutableNonEmptyIterable<? extends EnhancedIterable<A>> tails() {
        return immutableNonEmptyIterableOrThrow(Map.map(EnhancedIterable::enhance, Tails.tails(this)));
    }

    /**
     * Returns a new {@code FiniteIterable} that takes the first {@code count} elements of this {@code EnhancedIterable}.
     *
     * @param count the number of elements to take from this {@link EnhancedIterable}.
     *              Must be &gt;= 0.
     *              May exceed size of this {@code EnhancedIterable}, in which case, the result will contain
     *              as many elements available.
     * @return a {@code FiniteIterable<A>}
     */
    default FiniteIterable<A> take(int count) {
        validateTake(count);
        return finiteIterable(Take.take(count, this));
    }

    /**
     * Returns a new {@code EnhancedIterable} that limits to the first contiguous group of elements of this
     * {@code EnhancedIterable} that satisfy a predicate.
     * <p>
     * Iteration ends at, but does not include, the first element for which the predicate evaluates to false.
     *
     * @param predicate a predicate; should be referentially transparent and not have side-effects
     * @return an {@code EnhancedIterable<A>}
     */
    default EnhancedIterable<A> takeWhile(Fn1<? super A, ? extends Boolean> predicate) {
        requireNonNull(predicate);
        return enhance(TakeWhile.takeWhile(predicate, this));
    }

    /**
     * Writes all the elements of this {@code EnhancedIterable} directly into an array of the specified type.
     *
     * @param arrayType the type of the array
     * @return a new array
     */
    default A[] toArray(Class<A[]> arrayType) {
        requireNonNull(arrayType);
        return ToArray.toArray(arrayType).apply(this);
    }

    /**
     * Creates an instance of a {@code Collection} of type {@code C}, and adds to it all elements in this {@code EnhancedIterable}.
     * <p>
     * Note that instances of {@code C} must support {@link Collection#add} (which is to say, must not throw on invocation).
     *
     * @param cSupplier a function that instantiates a new {@link Collection} of type {@code C}
     * @param <C>       the resulting collection type
     * @return a new {@code Collection} of type {@code C}
     */
    default <C extends Collection<A>> C toCollection(Fn0<C> cSupplier) {
        requireNonNull(cSupplier);
        return ToCollection.toCollection(cSupplier).apply(this);
    }

    /**
     * Converts this {@code EnhancedIterable} to a {@code FiniteIterable} if there is enough
     * information to do so without iterating it.
     * <p>
     * Note that if this method returns {@code nothing()}, it does NOT necessarily mean this
     * {@code EnhancedIterable} is infinite.
     *
     * @return a {@code Maybe<FiniteIterable<A>}
     */
    default Maybe<? extends FiniteIterable<A>> toFinite() {
        return EnhancedIterables.maybeFinite(this);
    }

    /**
     * Converts this {@code EnhancedIterable} to a {@code NonEmptyIterable} if it contains
     * one or more elements.
     *
     * @return a {@code Maybe<NonEmptyIterable<A>}
     */
    default Maybe<? extends NonEmptyIterable<A>> toNonEmpty() {
        return EnhancedIterables.maybeNonEmpty(this);
    }

    /**
     * Zips together this {@code EnhancedIterable} with another {@code Iterable} by applying a zipping function.
     * <p>
     * Applies the function to the successive elements of each {@link Iterable} until one of them runs out of elements.
     *
     * @param fn    the zipping function.
     *              Not null.
     *              This function should be referentially transparent and not perform side-effects.
     *              It may be called zero or more times for each element.
     * @param other the other {@code Iterable}
     * @param <B>   the element type of the other {@code Iterable}
     * @param <C>   the element type of the result
     * @return an {@code EnhancedIterable<C>}
     */
    default <B, C> EnhancedIterable<C> zipWith(Fn2<A, B, C> fn, Iterable<B> other) {
        requireNonNull(fn);
        requireNonNull(other);
        return enhance(ZipWith.zipWith(fn, this, other));
    }

    /**
     * Zips together this {@code EnhancedIterable} with a {@code FiniteIterable} by applying a zipping function.
     * <p>
     * Applies the function to the successive elements of each {@link Iterable} until one of them runs out of elements.
     *
     * @param fn    the zipping function.
     *              Not null.
     *              This function should be referentially transparent and not perform side-effects.
     *              It may be called zero or more times for each element.
     * @param other the other {@code Iterable}
     * @param <B>   the element type of the other {@code Iterable}
     * @param <C>   the element type of the result
     * @return a {@code FiniteIterable<C>}
     */
    default <B, C> FiniteIterable<C> zipWith(Fn2<A, B, C> fn, FiniteIterable<B> other) {
        requireNonNull(fn);
        requireNonNull(other);
        return EnhancedIterables.finiteIterable(ZipWith.zipWith(fn, this, other));
    }

    /**
     * Zips together this {@code EnhancedIterable} with a {@code Collection} by applying a zipping function.
     * <p>
     * Applies the function to the successive elements of each {@link Iterable} until one of them runs out of elements.
     *
     * @param fn    the zipping function.
     *              Not null.
     *              This function should be referentially transparent and not perform side-effects.
     *              It may be called zero or more times for each element.
     * @param other the other {@code Iterable}
     * @param <B>   the element type of the other {@code Iterable}
     * @param <C>   the element type of the result
     * @return a {@code FiniteIterable<C>}
     */
    default <B, C> FiniteIterable<C> zipWith(Fn2<A, B, C> fn, Collection<B> other) {
        requireNonNull(fn);
        requireNonNull(other);
        return EnhancedIterables.finiteIterable(ZipWith.zipWith(fn, this, other));
    }

    /**
     * Creates an {@code ImmutableFiniteIterable} by copying elements from a {@code FiniteIterable}.
     * <p>
     * If {@code source} is already an {@link ImmutableFiniteIterable}, this method will return it without copying.
     *
     * @param source the source to copy from
     * @param <A>    the element type
     * @return an {@code ImmutableFiniteIterable<A>}
     */
    static <A> ImmutableFiniteIterable<A> copyFrom(FiniteIterable<A> source) {
        return EnhancedIterables.copyFrom(source);
    }

    /**
     * Creates an {@code ImmutableFiniteIterable} by copying elements from a {@code Collection}.
     *
     * @param source the source to copy from
     * @param <A>    the element type
     * @return an {@code ImmutableFiniteIterable<A>}
     */
    static <A> ImmutableFiniteIterable<A> copyFrom(Collection<A> source) {
        return EnhancedIterables.copyFrom(source);
    }

    /**
     * Creates an {@code ImmutableFiniteIterable} by copying elements from an {@code Iterable}.
     * <p>
     * If {@code source} is already an {@link ImmutableIterable}, no copying will be performed.
     *
     * @param maxCount the maximum number of elements to take from the supplied {@link Iterable}.
     *                 Must be &gt;= 0.
     *                 May exceed size of the {@code Iterable}, in which case, the result will contain
     *                 as many elements available.
     * @param source   the source to copy from
     * @param <A>      the element type
     * @return an {@code ImmutableFiniteIterable<A>}
     */
    static <A> ImmutableFiniteIterable<A> copyFrom(int maxCount, Iterable<A> source) {
        return EnhancedIterables.copyFrom(maxCount, source);
    }

    /**
     * Wraps an existing {@code Iterable} in an {@code EnhancedIterable}.
     * <p>
     * If {@code underlying} is already an {@link EnhancedIterable}, returns it directly.
     *
     * @param underlying the {@link Iterable} to wrap.
     *                   May be finite or infinite.
     * @param <A>        the element type
     * @return an {@code EnhancedIterable<A>}
     */
    static <A> EnhancedIterable<A> enhance(Iterable<A> underlying) {
        requireNonNull(underlying);
        return EnhancedIterables.enhance(underlying);
    }

    /**
     * Creates an {@code EnhancedIterable} containing the given elements.
     * <p>
     * Note that this method actually returns an {@link ImmutableNonEmptyFiniteIterable}, which is
     * also an {@link EnhancedIterable}.
     *
     * @param first the first element
     * @param more  the remaining elements
     * @param <A>   the element type
     * @return an {@code ImmutableNonEmptyFiniteIterable<A>}
     */
    @SafeVarargs
    static <A> ImmutableNonEmptyFiniteIterable<A> of(A first, A... more) {
        return EnhancedIterables.of(first, more);
    }

}
