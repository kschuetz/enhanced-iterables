package testsupport;

import com.jnape.palatable.lambda.adt.Maybe;

import java.util.Iterator;
import java.util.Objects;

import static com.jnape.palatable.lambda.functions.builtin.fn2.Tupler2.tupler;

public final class IterablesContainSameElements {

    private IterablesContainSameElements() {
    }

    public static boolean iterablesContainSameElements(Iterable<?> as, Iterable<?> bs) {
        Iterator<?> xs = as.iterator();
        Iterator<?> ys = bs.iterator();

        while (xs.hasNext() && ys.hasNext())
            if (!Objects.equals(xs.next(), ys.next()))
                return false;

        return xs.hasNext() == ys.hasNext();
    }

    public static boolean maybeIterablesContainSameElements(Maybe<? extends Iterable<?>> as, Maybe<? extends Iterable<?>> bs) {
        return as.equals(bs) ||
                as.zip(bs.fmap(tupler()))
                        .fmap(t -> iterablesContainSameElements(t._1(), t._2()))
                        .orElse(false);
    }

}
