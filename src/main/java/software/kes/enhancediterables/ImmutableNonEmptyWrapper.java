package software.kes.enhancediterables;

import com.jnape.palatable.lambda.functions.builtin.fn1.Tail;

class ImmutableNonEmptyWrapper<A> extends Wrapped<A> implements ImmutableNonEmptyIterable<A> {

    private ImmutableNonEmptyWrapper(Iterable<A> underlying) {
        super(underlying);
    }

    static <A> ImmutableNonEmptyWrapper<A> wrap(Iterable<A> underlying) {
        return new ImmutableNonEmptyWrapper<>(underlying);
    }

    @Override
    public ImmutableIterable<A> tail() {
        return ImmutableWrapper.wrap(Tail.tail(this));
    }

    @Override
    public A head() {
        return iterator().next();
    }

}
