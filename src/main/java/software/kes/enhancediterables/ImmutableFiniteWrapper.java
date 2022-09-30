package software.kes.enhancediterables;

class ImmutableFiniteWrapper<A> extends Wrapped<A> implements ImmutableFiniteIterable<A> {

    private ImmutableFiniteWrapper(Iterable<A> underlying) {
        super(underlying);
    }

    static <A> ImmutableFiniteWrapper<A> wrap(Iterable<A> underlying) {
        return new ImmutableFiniteWrapper<>(underlying);
    }

}
