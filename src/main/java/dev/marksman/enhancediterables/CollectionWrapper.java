package dev.marksman.enhancediterables;

import java.util.Collection;

class CollectionWrapper<A> extends Wrapped<A> implements FiniteIterable<A> {

    private CollectionWrapper(Collection<A> underlying) {
        super(underlying);
    }

    @Override
    public int size() {
        return ((Collection<A>) getUnderlying()).size();
    }

    static <A> CollectionWrapper<A> wrap(Collection<A> underlying) {
        return new CollectionWrapper<>(underlying);
    }

}
