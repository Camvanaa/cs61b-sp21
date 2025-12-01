package deque;

import java.util.Comparator;

public class MaxArrayDeque<T> extends ArrayDeque<T> {

    private final Comparator<T> cmp;

    public MaxArrayDeque(Comparator<T> c) {
        cmp = c;
    }

    public T max() {
        return getMax(cmp);
    }

    public T max(Comparator<T> c) {
        return getMax(c);
    }

    private T getMax(Comparator<T> comparator) {
        if (isEmpty()) {
            return null;
        }
        T max = get(0);
        for (int i = 1; i < size(); i++) {
            if (comparator.compare(max, get(i)) < 0) {
                max = get(i);
            }
        }
        return max;
    }

}
