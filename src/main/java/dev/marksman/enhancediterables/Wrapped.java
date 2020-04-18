package dev.marksman.enhancediterables;

import java.util.Iterator;

import static dev.marksman.enhancediterables.ProtectedIterator.protectedIterator;

abstract class Wrapped<A> implements Iterable<A> {
    private final Iterable<A> underlying;

    protected Wrapped(Iterable<A> underlying) {
        this.underlying = underlying;
    }

    static <A> Iterable<A> unwrap(Iterable<A> iterable) {
        if (iterable instanceof Wrapped<?>) {
            return ((Wrapped<A>) iterable).getUnderlying();
        } else {
            return iterable;
        }
    }

    Iterable<A> getUnderlying() {
        return underlying;
    }

    @Override
    public Iterator<A> iterator() {
        return protectedIterator(underlying.iterator());
    }

}
