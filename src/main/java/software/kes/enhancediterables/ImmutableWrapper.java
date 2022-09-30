package software.kes.enhancediterables;

class ImmutableWrapper<A> extends Wrapped<A> implements ImmutableIterable<A> {

    private ImmutableWrapper(Iterable<A> underlying) {
        super(underlying);
    }

    static <A> ImmutableWrapper<A> wrap(Iterable<A> underlying) {
        return new ImmutableWrapper<>(underlying);
    }

}
