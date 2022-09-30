package software.kes.enhancediterables;

import com.jnape.palatable.lambda.adt.Maybe;
import com.jnape.palatable.lambda.functions.builtin.fn1.Cycle;
import com.jnape.palatable.lambda.functions.builtin.fn1.Distinct;
import com.jnape.palatable.lambda.functions.builtin.fn1.Size;
import com.jnape.palatable.lambda.functions.builtin.fn1.Uncons;
import com.jnape.palatable.lambda.functions.builtin.fn2.ToCollection;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;

import static com.jnape.palatable.lambda.adt.Maybe.just;
import static com.jnape.palatable.lambda.adt.Maybe.nothing;
import static java.util.Arrays.asList;
import static java.util.Objects.requireNonNull;
import static software.kes.enhancediterables.ProtectedIterator.protectedIterator;
import static software.kes.enhancediterables.Wrapped.unwrap;

final class EnhancedIterables {
    private static final ImmutableFiniteIterable<?> EMPTY = () -> protectedIterator(Collections.emptyIterator());

    private EnhancedIterables() {
    }

    @SuppressWarnings("unchecked")
    static <A> ImmutableFiniteIterable<A> emptyEnhancedIterable() {
        return (ImmutableFiniteIterable<A>) EMPTY;
    }

    static <A> EnhancedIterable<A> enhance(Iterable<A> underlying) {
        requireNonNull(underlying);
        if (underlying instanceof EnhancedIterable<?>) {
            return (EnhancedIterable<A>) underlying;
        } else if (underlying instanceof Collection<?>) {
            return finiteIterableFromCollection((Collection<A>) underlying);
        } else {
            return EnhancedWrapper.wrap(underlying);
        }
    }

    static <A> FiniteIterable<A> finiteIterable(Iterable<A> underlying) {
        requireNonNull(underlying);
        if (underlying instanceof FiniteIterable<?>) {
            return (FiniteIterable<A>) underlying;
        } else if (underlying instanceof Collection<?>) {
            return CollectionWrapper.wrap((Collection<A>) underlying);
        } else {
            return FiniteWrapper.wrap(underlying);
        }
    }

    static <A> ImmutableIterable<A> immutableIterable(Iterable<A> underlying) {
        requireNonNull(underlying);
        if (underlying instanceof ImmutableIterable<?>) {
            return (ImmutableIterable<A>) underlying;
        } else if (underlying instanceof Collection<?>) {
            return immutableFiniteIterableFromCollection((Collection<A>) underlying);
        } else {
            return ImmutableWrapper.wrap(underlying);
        }
    }

    static <A> ImmutableFiniteIterable<A> immutableFiniteIterable(Iterable<A> underlying) {
        if (underlying instanceof ImmutableFiniteIterable<?>) {
            return (ImmutableFiniteIterable<A>) underlying;
        } else if (underlying instanceof Collection<?>) {
            return immutableFiniteIterableFromCollection(((Collection<A>) underlying));
        } else {
            return ImmutableFiniteWrapper.wrap(underlying);
        }
    }

    static <A> Maybe<FiniteIterable<A>> maybeFinite(Iterable<A> iterable) {
        if (iterable instanceof FiniteIterable<?>) {
            return just((FiniteIterable<A>) iterable);
        } else if (iterable instanceof Collection<?>) {
            return just(finiteIterableFromCollection((Collection<A>) iterable));
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
            return just(immutableFiniteIterableFromCollection((Collection<A>) iterable));
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
        EnhancedIterable<A> enhancedTail = enhance(tail);
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

    static <A> NonEmptyIterable<A> unsafeNonEmptyIterable(Iterable<A> underlying) {
        if (underlying instanceof NonEmptyIterable<?>) {
            return (NonEmptyIterable<A>) underlying;
        } else if (underlying instanceof Collection<?>) {
            return NonEmptyCollectionWrapper.wrap((Collection<A>) underlying);
        } else if (underlying instanceof FiniteIterable<?>) {
            return NonEmptyFiniteWrapper.wrap(underlying);
        } else {
            return NonEmptyWrapper.wrap(underlying);
        }
    }

    static <A> ImmutableNonEmptyIterable<A> unsafeImmutableNonEmptyIterable(Iterable<A> underlying) {
        if (underlying instanceof ImmutableNonEmptyIterable<?>) {
            return (ImmutableNonEmptyIterable<A>) underlying;
        } else if (underlying instanceof Collection<?>) {
            return ImmutableNonEmptyCollectionWrapper.wrap((Collection<A>) underlying);
        } else if (underlying instanceof FiniteIterable<?>) {
            return ImmutableNonEmptyFiniteWrapper.wrap(underlying);
        } else {
            return ImmutableNonEmptyWrapper.wrap(underlying);
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

    static <A> NonEmptyFiniteIterable<A> unsafeNonEmptyFiniteIterable(Iterable<A> underlying) {
        if (underlying instanceof Collection<?>) {
            return NonEmptyCollectionWrapper.wrap((Collection<A>) underlying);
        } else if (underlying instanceof NonEmptyFiniteIterable<?>) {
            return (NonEmptyFiniteIterable<A>) underlying;
        } else {
            return NonEmptyFiniteWrapper.wrap(underlying);
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

    static <A> ImmutableNonEmptyFiniteIterable<A> unsafeImmutableNonEmptyFiniteIterable(Iterable<A> underlying) {
        if (underlying instanceof ImmutableNonEmptyFiniteIterable<?>) {
            return (ImmutableNonEmptyFiniteIterable<A>) underlying;
        } else {
            if (underlying instanceof Collection<?>) {
                return ImmutableNonEmptyCollectionWrapper.wrap((Collection<A>) underlying);
            } else {
                return ImmutableNonEmptyFiniteWrapper.wrap(underlying);
            }
        }
    }

    @SuppressWarnings("varargs")
    @SafeVarargs
    static <A> ImmutableNonEmptyFiniteIterable<A> of(A first, A... more) {
        if (more.length > 0) {
            ImmutableFiniteIterable<A> tail = immutableFiniteIterable(asList(more));
            return immutableNonEmptyFiniteIterable(first, tail);
        } else {
            return singleton(first);
        }
    }

    static <A> ImmutableNonEmptyFiniteIterable<A> singleton(A value) {
        return new Singleton<>(value);
    }

    static <A> ImmutableFiniteIterable<A> copyFrom(FiniteIterable<A> source) {
        if (source instanceof ImmutableFiniteIterable<?>) {
            return (ImmutableFiniteIterable<A>) source;
        } else {
            ArrayList<A> underlying = ToCollection.toCollection(ArrayList::new, source);
            if (underlying.isEmpty()) {
                return Collections::emptyIterator;
            } else {
                return ImmutableNonEmptyCollectionWrapper.wrap(underlying);
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

    static <A> ImmutableNonEmptyIterable<A> repeat(A element) {
        return new ImmutableNonEmptyIterable<A>() {
            @Override
            public ImmutableIterable<A> tail() {
                return this;
            }

            @Override
            public A head() {
                return element;
            }

            @Override
            public Iterator<A> iterator() {
                return new Iterator<A>() {
                    @Override
                    public boolean hasNext() {
                        return true;
                    }

                    @Override
                    public A next() {
                        return element;
                    }
                };
            }
        };
    }

    static <A> EnhancedIterable<A> cycle(FiniteIterable<A> underlying) {
        requireNonNull(underlying);
        return underlying.toNonEmpty()
                .match(__ -> emptyEnhancedIterable(),
                        EnhancedIterables::nonEmptyCycle);
    }

    static <A> ImmutableIterable<A> cycle(ImmutableFiniteIterable<A> underlying) {
        requireNonNull(underlying);
        return underlying.toNonEmpty()
                .match(__ -> emptyEnhancedIterable(),
                        EnhancedIterables::nonEmptyCycle);
    }

    static <A> NonEmptyIterable<A> nonEmptyCycle(NonEmptyFiniteIterable<A> underlying) {
        requireNonNull(underlying);
        return NonEmptyWrapper.wrap(Cycle.cycle(underlying));
    }

    static <A> ImmutableNonEmptyIterable<A> nonEmptyCycle(ImmutableNonEmptyFiniteIterable<A> underlying) {
        requireNonNull(underlying);
        return ImmutableNonEmptyWrapper.wrap(Cycle.cycle(underlying));
    }

    static <A> FiniteIterable<A> distinct(FiniteIterable<A> underlying) {
        requireNonNull(underlying);
        return underlying.toNonEmpty()
                .match(__ -> emptyEnhancedIterable(),
                        EnhancedIterables::nonEmptyDistinct);
    }

    static <A> ImmutableFiniteIterable<A> distinct(ImmutableFiniteIterable<A> underlying) {
        requireNonNull(underlying);
        return underlying.toNonEmpty()
                .match(__ -> emptyEnhancedIterable(),
                        EnhancedIterables::nonEmptyDistinct);
    }

    static <A> NonEmptyFiniteIterable<A> nonEmptyDistinct(NonEmptyFiniteIterable<A> underlying) {
        requireNonNull(underlying);
        return unsafeNonEmptyFiniteIterable(Distinct.distinct(unwrap(underlying)));
    }

    static <A> ImmutableNonEmptyFiniteIterable<A> nonEmptyDistinct(ImmutableNonEmptyFiniteIterable<A> underlying) {
        requireNonNull(underlying);
        return unsafeImmutableNonEmptyFiniteIterable(Distinct.distinct(unwrap(underlying)));
    }

    static <A> int size(FiniteIterable<A> as) {
        requireNonNull(as);
        Long longSize = Size.size(as);
        if (longSize > Integer.MAX_VALUE) {
            return Integer.MAX_VALUE;
        } else {
            return longSize.intValue();
        }
    }

    private static <A> FiniteIterable<A> finiteIterableFromCollection(Collection<A> collection) {
        if (collection.isEmpty()) {
            return finiteIterable(collection);
        } else {
            return NonEmptyCollectionWrapper.wrap(collection);
        }
    }

    private static <A> ImmutableFiniteIterable<A> immutableFiniteIterableFromCollection(Collection<A> collection) {
        if (collection.isEmpty()) {
            return ImmutableFiniteIterable.emptyImmutableFiniteIterable();
        } else {
            return ImmutableNonEmptyCollectionWrapper.wrap(collection);
        }
    }

    private static class Singleton<A> implements ImmutableNonEmptyFiniteIterable<A> {
        private final A value;

        private Singleton(A value) {
            this.value = value;
        }

        @Override
        public ImmutableFiniteIterable<A> tail() {
            return EnhancedIterables.emptyEnhancedIterable();
        }

        @Override
        public A head() {
            return value;
        }
    }

}
