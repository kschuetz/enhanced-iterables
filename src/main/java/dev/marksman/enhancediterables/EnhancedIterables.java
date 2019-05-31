package dev.marksman.enhancediterables;

import com.jnape.palatable.lambda.adt.Maybe;
import com.jnape.palatable.lambda.adt.hlist.Tuple2;
import com.jnape.palatable.lambda.functions.builtin.fn1.Uncons;
import com.jnape.palatable.lambda.functions.builtin.fn2.ToCollection;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import static com.jnape.palatable.lambda.adt.Maybe.just;
import static com.jnape.palatable.lambda.adt.Maybe.nothing;
import static dev.marksman.enhancediterables.ProtectedIterator.protectedIterator;
import static java.util.Arrays.asList;
import static java.util.Objects.requireNonNull;

final class EnhancedIterables {

    private EnhancedIterables() {
    }

    /**
     * Note: Includes an attempt to promote to NonEmptyIterable, so be careful
     * not to call this recursively in the non-empty constructors.  Call simpleEnhance
     * in those cases.
     */
    static <A> EnhancedIterable<A> enhance(Iterable<A> underlying) {
        requireNonNull(underlying);
        if (underlying instanceof EnhancedIterable<?>) {
            return (EnhancedIterable<A>) underlying;
        } else if (underlying instanceof Collection<?>) {
            Collection<A> collection = (Collection<A>) underlying;
            if (collection.isEmpty()) {
                return finiteIterable(collection);
            } else {
                return nonEmptyFiniteIterableOrThrow(collection);
            }
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

    static <A> Maybe<FiniteIterable<A>> maybeFinite(Iterable<A> iterable) {
        if (iterable instanceof FiniteIterable<?>) {
            return just((FiniteIterable<A>) iterable);
        } else if (iterable instanceof Collection<?>) {
            return just(finiteIterable(iterable));
        } else {
            return nothing();
        }
    }

    static <A> Maybe<NonEmptyIterable<A>> maybeNonEmpty(Iterable<A> iterable) {
        if (iterable instanceof NonEmptyIterable<?>) {
            return just((NonEmptyIterable<A>) iterable);
        } else {
            return Uncons.uncons(iterable)
                    .fmap(headTail -> nonEmptyIterable(headTail._1(), headTail._2()));
        }
    }

    static <A> NonEmptyIterable<A> nonEmptyIterable(A head, Iterable<A> tail) {
        EnhancedIterable<A> enhancedTail = simpleEnhance(tail);
        return new NonEmptyIterable<A>() {
            @Override
            public A head() {
                return head;
            }

            @Override
            public EnhancedIterable<A> tail() {
                return enhancedTail;
            }
        };
    }

    static <A> NonEmptyIterable<A> nonEmptyIterableOrThrow(Iterable<A> underlying) {
        if (underlying instanceof NonEmptyIterable<?>) {
            return (NonEmptyIterable<A>) underlying;
        } else {
            Tuple2<A, Iterable<A>> headTail = unconsOrThrow(underlying);
            return nonEmptyIterable(headTail._1(), headTail._2());
        }
    }

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

    static <A> NonEmptyFiniteIterable<A> nonEmptyFiniteIterableOrThrow(Iterable<A> underlying) {
        if (underlying instanceof NonEmptyFiniteIterable<?>) {
            return (NonEmptyFiniteIterable<A>) underlying;
        } else {
            Tuple2<A, Iterable<A>> headTail = unconsOrThrow(underlying);
            return nonEmptyFiniteIterable(headTail._1(), finiteIterable(headTail._2()));
        }
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

    static <A> ImmutableNonEmptyIterable<A> immutableNonEmptyIterableOrThrow(Iterable<A> underlying) {
        if (underlying instanceof ImmutableNonEmptyIterable<?>) {
            return (ImmutableNonEmptyIterable<A>) underlying;
        } else {
            Tuple2<A, Iterable<A>> headTail = unconsOrThrow(underlying);
            return immutableNonEmptyIterable(headTail._1(), immutableIterable(headTail._2()));
        }
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

    static <A> ImmutableNonEmptyFiniteIterable<A> immutableNonEmptyFiniteIterableOrThrow(Iterable<A> underlying) {
        if (underlying instanceof ImmutableNonEmptyFiniteIterable<?>) {
            return (ImmutableNonEmptyFiniteIterable<A>) underlying;
        } else {
            Tuple2<A, Iterable<A>> headTail = unconsOrThrow(underlying);
            return immutableNonEmptyFiniteIterable(headTail._1(),
                    immutableFiniteIterable(headTail._2()));
        }
    }

    @SafeVarargs
    static <A> ImmutableNonEmptyFiniteIterable<A> of(A first, A... more) {
        ImmutableFiniteIterable<A> tail = immutableFiniteIterable(asList(more));
        return immutableNonEmptyFiniteIterable(first, tail);
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
        return copyFrom(finiteIterable(source));
    }

    static <A> ImmutableFiniteIterable<A> copyFrom(int maxCount, Iterable<A> source) {
        if (source instanceof ImmutableIterable<?>) {
            return ((ImmutableIterable<A>) source).take(maxCount);
        } else {
            return copyFrom(FiniteIterable.finiteIterable(maxCount, source));
        }
    }

    /**
     * Does not attempt to promote to NonEmpty.
     */
    private static <A> EnhancedIterable<A> simpleEnhance(Iterable<A> underlying) {
        requireNonNull(underlying);
        if (underlying instanceof EnhancedIterable<?>) {
            return (EnhancedIterable<A>) underlying;
        } else if (underlying instanceof Collection<?>) {
            return finiteIterable(underlying);
        } else {
            return () -> protectedIterator(underlying.iterator());
        }
    }

    private static <A> Tuple2<A, Iterable<A>> unconsOrThrow(Iterable<A> iterable) {
        return Uncons.uncons(iterable)
                .orElseThrow(() -> new IllegalArgumentException("iterable is empty"));
    }

}
