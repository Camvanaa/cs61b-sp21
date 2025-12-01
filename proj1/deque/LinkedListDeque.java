package deque;

import java.util.Iterator;

public class LinkedListDeque<T> implements Deque<T> {
    private final Node<T> sentinel;
    private int size;

    private static class Node<Item> {
        Item item;
        Node<Item> next;
        Node<Item> prev;

        Node(Item item, Node<Item> next, Node<Item> prev) {
            this.item = item;
            this.next = next;
            this.prev = prev;
        }
    }

    public LinkedListDeque() {
        size = 0;
        sentinel = new Node<>(null, null, null);
        sentinel.next = sentinel;
        sentinel.prev = sentinel;
    }

    @Override
    public void addFirst(T item) {
        Node<T> newNode = new Node<>(item, sentinel.next, sentinel);
        sentinel.next.prev = newNode;
        sentinel.next = newNode;
        size++;
    }

    @Override
    public void addLast(T item) {
        Node<T> newNode = new Node<>(item, sentinel, sentinel.prev);
        sentinel.prev.next = newNode;
        sentinel.prev = newNode;
        size++;
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public void printDeque() {
        Node<T> p = sentinel.next;
        while (p != sentinel) {
            System.out.print(p.item + " ");
            p = p.next;
        }
        System.out.println();
    }

    @Override
    public T removeFirst() {
        if (isEmpty()) {
            return null;
        }
        Node<T> p = sentinel.next;
        sentinel.next = p.next;
        p.next.prev = sentinel;
        size--;
        return p.item;
    }

    @Override
    public T removeLast() {
        if (isEmpty()) {
            return null;
        }
        Node<T> p = sentinel.prev;
        sentinel.prev = p.prev;
        p.prev.next = sentinel;
        size--;
        return p.item;
    }

    @Override
    public T get(int index) {
        if (index < 0 || index >= size || isEmpty()) {
            return null;
        }
        Node<T> p = sentinel.next;
        for (int i = 0; i < index; i++) {
            p = p.next;
        }
        return p.item;
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

        for (int i = 0; i < this.size(); i++) {
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
        Node<T> p = sentinel.next;
        while (p != sentinel) {
            if (p.item != null) {
                h += p.item.hashCode();
            }
            h = h * 31;
            p = p.next;
        }
        return h;
    }

    @SuppressWarnings("unused")
    public T getRecursive(int index) {
        if (index < 0 || index >= size) {
            return null;
        }
        return getRecursiveHelper(index, sentinel.next);
    }

    private T getRecursiveHelper(int index, Node<T> p) {
        if (index == 0) {
            return p.item;
        }
        return getRecursiveHelper(index - 1, p.next);
    }

    @Override
    public Iterator<T> iterator() {
        return new LinkedlistIterator();
    }

    private class LinkedlistIterator implements Iterator<T> {
        private Node<T> p = sentinel.next;

        @Override
        public boolean hasNext() {
            return p != sentinel;
        }

        @Override
        public T next() {
            Node<T> item = p;
            p = p.next;
            return item.item;
        }
    }

}
