package deque;

public class ArrayDeque<T> {
    private T[] items;
    private int size;
    private int nextFirst;
    private int nextLast;

    public ArrayDeque() {
        items = (T[]) new Object[8];
        size = 0;
        nextFirst = 3;
        nextLast = 4;
    }


    private void resize(int newSize) {
        T[] temp = (T[]) new Object[newSize];
        int p = (nextFirst + 1) % items.length;
        int q = (newSize - size) / 2;
        nextFirst = q++;
        for (int i = 0; i < size; i++) {
            temp[q] = items[p];
            p = (p + 1) % items.length;
            q++;
        }
        nextLast = q;
        items = temp;
    }

    public void addFirst(T item) {
        if (size == items.length) {
            resize(size * 2);
        }
        items[nextFirst] = item;
        nextFirst = (nextFirst - 1 + items.length) % items.length;
        size++;
    }

    public void addLast(T item) {
        if (size == items.length) {
            resize(size * 2);
        }
        items[nextLast] = item;
        nextLast = (nextLast + 1) % items.length;
        size++;
    }

    public boolean isEmpty() {
        return size == 0;
    }

    public int size() {
        return size;
    }

    public void printDeque() {
        int p = (nextFirst + 1) % items.length;
        for (int i = 0; i < size; i++) {
            System.out.print(items[p] + " ");
            p = (p + 1) % items.length;
        }
        System.out.println();
    }

    public T removeFirst() {
        if (size == 0) return null;
        nextFirst = (nextFirst + 1) % items.length;
        T item = items[nextFirst];
        items[nextFirst] = null;
        size--;
        if (items.length >= 16 && size < items.length / 4) {
            resize(items.length / 2);
        }

        return item;
    }

    public T removeLast() {
        if (size == 0) return null;
        nextLast = (nextLast - 1 + items.length) % items.length;
        T item = items[nextLast];
        items[nextLast] = null;
        size--;
        if (items.length >= 16 && size < items.length / 4) {
            resize(items.length / 2);
        }
        return item;
    }

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
        if (!(o instanceof ArrayDeque)) {
            return false;
        }
        ArrayDeque<?> other = (ArrayDeque<?>) o;
        if (this.size() != other.size()) {
            return false;
        }
        for (int i = 0; i < this.size(); i++) {
            T myItem = this.get(i);
            Object otherItem = other.get(i);

            if (myItem == null) {
                if (otherItem != null) return false;
            } else {
                if (!myItem.equals(otherItem)) return false;
            }
        }
        return true;
    }

}
