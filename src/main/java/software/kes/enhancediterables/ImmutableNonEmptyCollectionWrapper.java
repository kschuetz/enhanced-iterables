package software.kes.enhancediterables;

import com.jnape.palatable.lambda.functions.builtin.fn1.Tail;

import java.util.Collection;

class ImmutableNonEmptyCollectionWrapper<A> extends Wrapped<A> implements ImmutableNonEmptyFiniteIterable<A> {

    private ImmutableNonEmptyCollectionWrapper(Collection<A> underlying) {
        super(underlying);
    }

    @Override
    public ImmutableFiniteIterable<A> tail() {
        return ImmutableFiniteWrapper.wrap(Tail.tail(this));
    }

    @Override
    public A head() {
        return iterator().next();
    }

    @Override
    public int size() {
        return ((Collection<A>) getUnderlying()).size();
    }

    static <A> ImmutableNonEmptyCollectionWrapper<A> wrap(Collection<A> underlying) {
        return new ImmutableNonEmptyCollectionWrapper<>(underlying);
    }

}
