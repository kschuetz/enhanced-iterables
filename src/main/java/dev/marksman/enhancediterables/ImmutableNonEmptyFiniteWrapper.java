package dev.marksman.enhancediterables;

import com.jnape.palatable.lambda.functions.builtin.fn1.Tail;

class ImmutableNonEmptyFiniteWrapper<A> extends Wrapped<A> implements ImmutableNonEmptyFiniteIterable<A> {

    private ImmutableNonEmptyFiniteWrapper(Iterable<A> underlying) {
        super(underlying);
    }

    static <A> ImmutableNonEmptyFiniteWrapper<A> wrap(Iterable<A> underlying) {
        return new ImmutableNonEmptyFiniteWrapper<>(underlying);
    }

    @Override
    public ImmutableFiniteIterable<A> tail() {
        return ImmutableFiniteWrapper.wrap(Tail.tail(this));
    }

    @Override
    public A head() {
        return iterator().next();
    }
}
