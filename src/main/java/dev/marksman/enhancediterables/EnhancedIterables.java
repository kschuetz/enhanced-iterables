package dev.marksman.enhancediterables;

import com.jnape.palatable.lambda.adt.Maybe;
import com.jnape.palatable.lambda.functions.Fn0;
import com.jnape.palatable.lambda.functions.builtin.fn1.Uncons;
import com.jnape.palatable.lambda.functions.builtin.fn2.ToCollection;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;

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
            return finiteIterableFromCollection((Collection<A>) underlying);
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
        requireNonNull(underlying);
        if (underlying instanceof ImmutableIterable<?>) {
            return (ImmutableIterable<A>) underlying;
        } else if (underlying instanceof Collection<?>) {
            return immutableFiniteIterableFromCollection((Collection<A>) underlying);
        } else if (underlying.iterator().hasNext()) {
            return immutableNonEmptyIterableOrThrow(underlying);
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

    static <A> Maybe<NonEmptyFiniteIterable<A>> nonEmptyMaybeFinite(A head, Iterable<A> tail) {
        if (tail instanceof FiniteIterable<?>) {
            return just(nonEmptyFiniteIterable(head, (FiniteIterable<A>) tail));
        } else if (tail instanceof Collection<?>) {
            return just(nonEmptyFiniteIterable(head, finiteIterable(tail)));
        } else {
            return nothing();
        }
    }

    static <A> Maybe<ImmutableFiniteIterable<A>> immutableMaybeFinite(Iterable<A> iterable) {
        if (iterable instanceof ImmutableFiniteIterable<?>) {
            return just((ImmutableFiniteIterable<A>) iterable);
        } else if (iterable instanceof Collection<?>) {
            return just(immutableFiniteIterable(iterable));
        } else {
            return nothing();
        }
    }

    static <A> Maybe<ImmutableNonEmptyFiniteIterable<A>> immutableNonEmptyMaybeFinite(A head, Iterable<A> tail) {
        if (tail instanceof ImmutableFiniteIterable<?>) {
            return just(immutableNonEmptyFiniteIterable(head, (ImmutableFiniteIterable<A>) tail));
        } else if (tail instanceof Collection<?>) {
            return just(immutableNonEmptyFiniteIterable(head, immutableFiniteIterable(tail)));
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
            if (!underlying.iterator().hasNext()) {
                throw nonEmptyError().apply();
            }

            return new NonEmptyIterable<A>() {
                @Override
                public A head() {
                    return iterator().next();
                }

                @Override
                public EnhancedIterable<A> tail() {
                    return this.drop(1);
                }

                @Override
                public Iterator<A> iterator() {
                    return underlying.iterator();
                }
            };
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

    static <A> NonEmptyFiniteIterable<A> nonEmptyFiniteIterable(A head, Collection<A> tail) {
        return nonEmptyFiniteIterable(head, finiteIterable(tail));
    }

    static <A> NonEmptyFiniteIterable<A> nonEmptyFiniteIterableOrThrow(Iterable<A> underlying) {
        if (underlying instanceof NonEmptyFiniteIterable<?>) {
            return (NonEmptyFiniteIterable<A>) underlying;
        } else {
            if (!underlying.iterator().hasNext()) {
                throw nonEmptyError().apply();
            }

            return new NonEmptyFiniteIterable<A>() {
                @Override
                public A head() {
                    return iterator().next();
                }

                @Override
                public FiniteIterable<A> tail() {
                    return this.drop(1);
                }

                @Override
                public Iterator<A> iterator() {
                    return underlying.iterator();
                }
            };
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
            if (!underlying.iterator().hasNext()) {
                throw nonEmptyError().apply();
            }

            return new ImmutableNonEmptyIterable<A>() {
                @Override
                public A head() {
                    return iterator().next();
                }

                @Override
                public ImmutableIterable<A> tail() {
                    return this.drop(1);
                }

                @Override
                public Iterator<A> iterator() {
                    return underlying.iterator();
                }
            };
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
            if (!underlying.iterator().hasNext()) {
                throw nonEmptyError().apply();
            }

            return new ImmutableNonEmptyFiniteIterable<A>() {
                @Override
                public A head() {
                    return iterator().next();
                }

                @Override
                public ImmutableFiniteIterable<A> tail() {
                    return this.drop(1);
                }

                @Override
                public Iterator<A> iterator() {
                    return underlying.iterator();
                }
            };
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

    /**
     * Does not attempt to promote to NonEmpty.
     */
    private static <A> ImmutableIterable<A> simpleImmutableIterable(Iterable<A> underlying) {
        requireNonNull(underlying);
        if (underlying instanceof ImmutableIterable<?>) {
            return (ImmutableIterable<A>) underlying;
        } else if (underlying instanceof Collection<?>) {
            return immutableFiniteIterable(underlying);
        } else {
            return () -> protectedIterator(underlying.iterator());
        }
    }

    private static <A> FiniteIterable<A> finiteIterableFromCollection(Collection<A> collection) {
        if (collection.isEmpty()) {
            return finiteIterable(collection);
        } else {
            return nonEmptyFiniteIterableOrThrow(collection);
        }
    }

    private static <A> ImmutableIterable<A> immutableFiniteIterableFromCollection(Collection<A> collection) {
        if (collection.isEmpty()) {
            return immutableFiniteIterable(collection);
        } else {
            return immutableNonEmptyFiniteIterableOrThrow(collection);
        }
    }

    private static Fn0<IllegalArgumentException> nonEmptyError() {
        return () -> new IllegalArgumentException("Cannot construct NonEmptyIterable from empty input");
    }

}
