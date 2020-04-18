package dev.marksman.enhancediterables;

class EnhancedWrapper<A> extends Wrapped<A> implements EnhancedIterable<A> {

    private EnhancedWrapper(Iterable<A> underlying) {
        super(underlying);
    }

    static <A> EnhancedWrapper<A> wrap(Iterable<A> underlying) {
        return new EnhancedWrapper<>(underlying);
    }

}
