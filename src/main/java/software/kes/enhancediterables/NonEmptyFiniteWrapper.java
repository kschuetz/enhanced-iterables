package software.kes.enhancediterables;

import com.jnape.palatable.lambda.functions.builtin.fn1.Tail;

class NonEmptyFiniteWrapper<A> extends Wrapped<A> implements NonEmptyFiniteIterable<A> {

    private NonEmptyFiniteWrapper(Iterable<A> underlying) {
        super(underlying);
    }

    static <A> NonEmptyFiniteWrapper<A> wrap(Iterable<A> underlying) {
        return new NonEmptyFiniteWrapper<>(underlying);
    }

    @Override
    public FiniteIterable<A> tail() {
        return FiniteWrapper.wrap(Tail.tail(this));
    }

    @Override
    public A head() {
        return iterator().next();
    }
}
