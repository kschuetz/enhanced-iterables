package testsupport;

import java.util.ArrayList;

public final class IntSequence {

    public static ArrayList<Integer> integers(int from, int to) {
        int size = 1 + to - from;
        ArrayList<Integer> result = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            result.add(from + i);
        }
        return result;
    }

}
