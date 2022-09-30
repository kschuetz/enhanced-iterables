package software.kes.enhancediterables;

class FiniteWrapper<A> extends Wrapped<A> implements FiniteIterable<A> {

    private FiniteWrapper(Iterable<A> underlying) {
        super(underlying);
    }

    static <A> FiniteWrapper<A> wrap(Iterable<A> underlying) {
        return new FiniteWrapper<>(underlying);
    }

}
