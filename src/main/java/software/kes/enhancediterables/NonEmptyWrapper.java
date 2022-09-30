package software.kes.enhancediterables;

import com.jnape.palatable.lambda.functions.builtin.fn1.Tail;

class NonEmptyWrapper<A> extends Wrapped<A> implements NonEmptyIterable<A> {

    private NonEmptyWrapper(Iterable<A> underlying) {
        super(underlying);
    }

    static <A> NonEmptyWrapper<A> wrap(Iterable<A> underlying) {
        return new NonEmptyWrapper<>(underlying);
    }

    @Override
    public EnhancedIterable<A> tail() {
        return EnhancedWrapper.wrap(Tail.tail(this));
    }

    @Override
    public A head() {
        return iterator().next();
    }
}
