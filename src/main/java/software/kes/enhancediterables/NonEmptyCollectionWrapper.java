package software.kes.enhancediterables;

import com.jnape.palatable.lambda.functions.builtin.fn1.Tail;

import java.util.Collection;

class NonEmptyCollectionWrapper<A> extends Wrapped<A> implements NonEmptyFiniteIterable<A> {

    private NonEmptyCollectionWrapper(Collection<A> underlying) {
        super(underlying);
    }

    @Override
    public FiniteIterable<A> tail() {
        return FiniteWrapper.wrap(Tail.tail(this));
    }

    @Override
    public A head() {
        return iterator().next();
    }

    @Override
    public int size() {
        return ((Collection<A>) getUnderlying()).size();
    }

    static <A> NonEmptyCollectionWrapper<A> wrap(Collection<A> underlying) {
        return new NonEmptyCollectionWrapper<>(underlying);
    }

}
