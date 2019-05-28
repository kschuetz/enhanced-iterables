package testsupport;

import java.util.Iterator;
import java.util.Objects;

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

}

