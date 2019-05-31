package dev.marksman.enhancediterables;

import com.jnape.palatable.lambda.adt.Maybe;
import com.jnape.palatable.lambda.adt.hlist.Tuple2;
import com.jnape.palatable.lambda.functions.builtin.fn1.Uncons;
import com.jnape.palatable.lambda.functions.builtin.fn2.ToCollection;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import static com.jnape.palatable.lambda.adt.Maybe.just;
import static dev.marksman.enhancediterables.ProtectedIterator.protectedIterator;
import static java.util.Arrays.asList;
import static java.util.Objects.requireNonNull;

final class EnhancedIterables {

    private EnhancedIterables() {
    }

    static <A> EnhancedIterable<A> enhance(Iterable<A> underlying) {
        requireNonNull(underlying);
        if (underlying instanceof EnhancedIterable<?>) {
            return (EnhancedIterable<A>) underlying;
        } else if (underlying instanceof Collection<?>) {
            return finiteIterable(underlying);
        } else if (underlying.iterator().hasNext()) {
            return nonEmptyIterableOrThrow(underlying);
        } else {
            return () -> protectedIterator(underlying.iterator());
        }
    }

    static <A> FiniteIterable<A> finiteIterable(Iterable<A> underlying) {
        if (underlying instanceof FiniteIterable<?>) {
            return (FiniteIterable<A>) underlying;
        } else {
            return () -> protectedIterator(underlying.iterator());
        }
    }

    static <A> ImmutableIterable<A> immutableIterable(Iterable<A> underlying) {
        if (underlying instanceof ImmutableIterable<?>) {
            return (ImmutableIterable<A>) underlying;
        } else {
            return () -> protectedIterator(underlying.iterator());
        }
    }

    static <A> ImmutableFiniteIterable<A> immutableFiniteIterable(Iterable<A> underlying) {
        if (underlying instanceof ImmutableFiniteIterable<?>) {
            return (ImmutableFiniteIterable<A>) underlying;
        } else {
            return () -> protectedIterator(underlying.iterator());
        }
    }

    static <A> Maybe<NonEmptyIterable<A>> maybeNonEmpty(Iterable<A> iterable) {
        if (iterable instanceof NonEmptyIterable<?>) {
            return just((NonEmptyIterable<A>) iterable);
        } else {
            return Uncons.uncons(iterable)
                    .fmap(headTail -> NonEmptyIterable.nonEmptyIterable(headTail._1(), headTail._2()));
        }
    }

    static <A> NonEmptyIterable<A> nonEmptyIterableOrThrow(Iterable<A> underlying) {
        if (underlying instanceof NonEmptyIterable<?>) {
            return (NonEmptyIterable<A>) underlying;
        } else {
            Tuple2<A, Iterable<A>> headTail = unconsOrThrow(underlying);
            return NonEmptyIterable.nonEmptyIterable(headTail._1(), headTail._2());
        }
    }

    static <A> ImmutableNonEmptyIterable<A> immutableNonEmptyIterableOrThrow(Iterable<A> underlying) {
        if (underlying instanceof ImmutableNonEmptyIterable<?>) {
            return (ImmutableNonEmptyIterable<A>) underlying;
        } else {
            Tuple2<A, Iterable<A>> headTail = unconsOrThrow(underlying);
            return ImmutableNonEmptyIterable.immutableNonEmptyIterable(headTail._1(), immutableIterable(headTail._2()));
        }
    }

    static <A> NonEmptyFiniteIterable<A> nonEmptyFiniteIterableOrThrow(Iterable<A> underlying) {
        if (underlying instanceof NonEmptyFiniteIterable<?>) {
            return (NonEmptyFiniteIterable<A>) underlying;
        } else {
            Tuple2<A, Iterable<A>> headTail = unconsOrThrow(underlying);
            return NonEmptyFiniteIterable.nonEmptyFiniteIterable(headTail._1(), finiteIterable(headTail._2()));
        }
    }

    static <A> ImmutableNonEmptyFiniteIterable<A> immutableNonEmptyFiniteIterableOrThrow(Iterable<A> underlying) {
        if (underlying instanceof ImmutableNonEmptyFiniteIterable<?>) {
            return (ImmutableNonEmptyFiniteIterable<A>) underlying;
        } else {
            Tuple2<A, Iterable<A>> headTail = unconsOrThrow(underlying);
            return ImmutableNonEmptyFiniteIterable.immutableNonEmptyFiniteIterable(headTail._1(),
                    immutableFiniteIterable(headTail._2()));
        }
    }

    @SafeVarargs
    static <A> ImmutableNonEmptyFiniteIterable<A> of(A first, A... more) {
        ImmutableFiniteIterable<A> tail = immutableFiniteIterable(asList(more));
        return new ImmutableNonEmptyFiniteIterable<A>() {
            @Override
            public A head() {
                return first;
            }

            @Override
            public ImmutableFiniteIterable<A> tail() {
                return tail;
            }

        };
    }

    static <A> ImmutableFiniteIterable<A> copyFrom(FiniteIterable<A> source) {
        if (source instanceof ImmutableFiniteIterable<?>) {
            return (ImmutableFiniteIterable<A>) source;
        } else {
            ArrayList<A> underlying = ToCollection.toCollection(ArrayList::new, source);
            if (underlying.isEmpty()) {
                return Collections::emptyIterator;
            } else {
                return immutableNonEmptyFiniteIterableOrThrow(underlying);
            }
        }
    }

    static <A> ImmutableFiniteIterable<A> copyFrom(Collection<A> source) {
        return copyFrom(FiniteIterable.finiteIterable(source));
    }

    static <A> ImmutableFiniteIterable<A> copyFrom(int maxCount, Iterable<A> source) {
        if (source instanceof ImmutableIterable<?>) {
            return ((ImmutableIterable<A>) source).take(maxCount);
        } else {
            return copyFrom(FiniteIterable.finiteIterable(maxCount, source));
        }
    }

    private static <A> Tuple2<A, Iterable<A>> unconsOrThrow(Iterable<A> iterable) {
        return Uncons.uncons(iterable)
                .orElseThrow(() -> new IllegalArgumentException("iterable is empty"));
    }

}
