package dev.marksman.enhancediterables;

import com.jnape.palatable.lambda.adt.Maybe;
import com.jnape.palatable.lambda.adt.hlist.Tuple2;
import com.jnape.palatable.lambda.functions.Fn0;
import com.jnape.palatable.lambda.functions.Fn1;
import com.jnape.palatable.lambda.functions.Fn2;
import com.jnape.palatable.lambda.functions.builtin.fn1.Tails;
import com.jnape.palatable.lambda.functions.builtin.fn2.*;
import com.jnape.palatable.lambda.functions.builtin.fn3.ZipWith;
import com.jnape.palatable.lambda.monoid.builtin.Concat;

import java.util.Collection;

import static com.jnape.palatable.lambda.adt.hlist.HList.tuple;
import static dev.marksman.enhancediterables.EnhancedIterables.finiteIterable;
import static dev.marksman.enhancediterables.EnhancedIterables.nonEmptyIterableOrThrow;
import static dev.marksman.enhancediterables.Validation.validateDrop;
import static dev.marksman.enhancediterables.Validation.validateTake;
import static dev.marksman.enhancediterables.internal.ProtectedIterator.protectedIterator;
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
public interface EnhancedIterable<A> extends Iterable<A> {

    default NonEmptyIterable<A> append(A element) {
        return nonEmptyIterableOrThrow(Snoc.snoc(element, this));
    }

    default EnhancedIterable<A> concat(Iterable<A> other) {
        requireNonNull(other);
        return enhance(Concat.concat(this, other));
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

    default EnhancedIterable<A> dropWhile(Fn1<? super A, ? extends Boolean> predicate) {
        requireNonNull(predicate);
        return enhance(DropWhile.dropWhile(predicate, this));
    }

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

    default <B> EnhancedIterable<B> fmap(Fn1<? super A, ? extends B> f) {
        requireNonNull(f);
        return enhance(Map.map(f, this));
    }

    default EnhancedIterable<A> intersperse(A a) {
        return enhance(Intersperse.intersperse(a, this));
    }

    default boolean isEmpty() {
        return !iterator().hasNext();
    }

    default NonEmptyIterable<A> prepend(A element) {
        return NonEmptyIterable.nonEmptyIterable(element, this);
    }

    default EnhancedIterable<A> prependAll(A a) {
        return enhance(PrependAll.prependAll(a, this));
    }

    default Tuple2<? extends EnhancedIterable<A>, ? extends EnhancedIterable<A>> span(Fn1<? super A, ? extends Boolean> predicate) {
        requireNonNull(predicate);
        Tuple2<Iterable<A>, Iterable<A>> spanResult = Span.<A>span(predicate).apply(this);
        return tuple(enhance(spanResult._1()), enhance(spanResult._2()));
    }

    default NonEmptyIterable<? extends EnhancedIterable<A>> tails() {
        return nonEmptyIterableOrThrow(Map.map(EnhancedIterable::enhance, Tails.tails(this)));
    }

    default FiniteIterable<A> take(int count) {
        validateTake(count);
        return finiteIterable(Take.take(count, this));
    }

    default EnhancedIterable<A> takeWhile(Fn1<? super A, ? extends Boolean> predicate) {
        requireNonNull(predicate);
        return enhance(TakeWhile.takeWhile(predicate, this));
    }

    default A[] toArray(Class<A[]> arrayType) {
        requireNonNull(arrayType);
        return ToArray.toArray(arrayType).apply(this);
    }

    default <C extends Collection<A>> C toCollection(Fn0<C> cSupplier) {
        requireNonNull(cSupplier);
        return ToCollection.toCollection(cSupplier).apply(this);
    }

    default <B, C> EnhancedIterable<C> zipWith(Fn2<A, B, C> fn, Iterable<B> other) {
        requireNonNull(fn);
        requireNonNull(other);
        return enhance(ZipWith.zipWith(fn, this, other));
    }

    static <A> EnhancedIterable<A> enhance(Iterable<A> underlying) {
        requireNonNull(underlying);
        if (underlying instanceof EnhancedIterable<?>) {
            return (EnhancedIterable<A>) underlying;
        } else {
            return () -> protectedIterator(underlying.iterator());
        }
    }

    @SafeVarargs
    static <A> ImmutableNonEmptyFiniteIterable<A> of(A first, A... more) {
        return EnhancedIterables.of(first, more);
    }

}
