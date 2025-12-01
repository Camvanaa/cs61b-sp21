package deque;

import java.util.Iterator;

public class ArrayDeque<T> implements Deque<T> {
    private T[] items;
    private int size;
    private int nextFirst;
    private int nextLast;

    @SuppressWarnings("unchecked")
    public ArrayDeque() {
        items = (T[]) new Object[8];
        size = 0;
        nextFirst = 3;
        nextLast = 4;
    }

    @SuppressWarnings("unchecked")
    private void resize(int newSize) {
        T[] temp = (T[]) new Object[newSize];
        int p = (nextFirst + 1) % items.length;
        for (int i = 0; i < size; i++) {
            temp[i] = items[p];
            p = (p + 1) % items.length;
        }
        items = temp;
        nextFirst = newSize - 1;
        nextLast = size;
    }

    @Override
    public void addFirst(T item) {
        if (size == items.length) {
            resize(size * 2);
        }
        items[nextFirst] = item;
        nextFirst = (nextFirst - 1 + items.length) % items.length;
        size++;
    }

    @Override
    public void addLast(T item) {
        if (size == items.length) {
            resize(size * 2);
        }
        items[nextLast] = item;
        nextLast = (nextLast + 1) % items.length;
        size++;
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public void printDeque() {
        int p = (nextFirst + 1) % items.length;
        for (int i = 0; i < size; i++) {
            System.out.print(items[p] + " ");
            p = (p + 1) % items.length;
        }
        System.out.println();
    }

    @Override
    public T removeFirst() {
        if (size == 0) {
            return null;
        }
        nextFirst = (nextFirst + 1) % items.length;
        return setT(nextFirst);
    }

    @Override
    public T removeLast() {
        if (size == 0) {
            return null;
        }
        nextLast = (nextLast - 1 + items.length) % items.length;
        return setT(nextLast);
    }

    private T setT(int next) {
        T item = items[next];
        items[next] = null;
        size--;
        if (items.length >= 16 && size < items.length / 4) {
            resize(items.length / 2);
        }
        return item;
    }

    @Override
    public T get(int index) {
        if (index < 0 || index >= size || isEmpty()) {
            return null;
        }
        int p = (nextFirst + index + 1) % items.length;
        return items[p];
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Deque)) {
            return false;
        }
        Deque<?> other = (Deque<?>) o;

        if (this.size() != other.size()) {
            return false;
        }

        for (int i = 0; i < size; i++) {
            Object item1 = this.get(i);
            Object item2 = other.get(i);
            if (item1 == null) {
                if (item2 != null) {
                    return false;
                }
            } else {
                if (!item1.equals(item2)) {
                    return false;
                }
            }
        }
        return true;
    }

    public int hashCode() {
        int h = 0;
        for (int i = 0; i < size; i++) {
            T item = get(i);
            if (item != null) {
                h += item.hashCode();
            }
            h = h * 31;
        }
        return h;
    }

    @Override
    public Iterator<T> iterator() {
        return new ArrayIterator();
    }

    private class ArrayIterator implements Iterator<T> {
        int position;

        ArrayIterator() {
            position = 0;
        }

        @Override
        public boolean hasNext() {
            return position < size;
        }

        @Override
        public T next() {
            T item = get(position);
            position++;
            return item;
        }
    }

}
