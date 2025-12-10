package deque;

public class LinkedListDeque<T> {
    private Node<T> head;
    private Node<T> tail;
    private int size;

    private static class Node<Item> {
        Item item;
        Node<Item> next;
        Node<Item> prev;

        public Node(Item item, Node<Item> next, Node<Item> prev) {
            this.item = item;
            this.next = next;
            this.prev = prev;
        }
    }

    public LinkedListDeque() {
        size = 0;
        head = new Node<>(null, null, null);
        tail = new Node<>(null, null, null);
        head.next = tail;
        tail.prev = head;
    }

    public void addFirst(T item) {
        head.next = new Node<T>(item, head.next, head);
        head.next.prev = head;
        size++;
    }

    public void addLast(T item) {
        tail.prev = new Node<T>(item, tail, tail.prev);
        tail.prev.next = tail;
        size++;
    }

    public boolean isEmpty() {
        return size == 0;
    }

    public int size() {
        return size;
    }

    public void printDeque() {
        Node<T> p = head.next;
        while (p != null) {
            System.out.print(p.item + " ");
            p = p.next;
        }
        System.out.println();
    }

    public T removeFirst() {
        if (isEmpty()) {
            return null;
        }
        Node<T> p = head.next;
        head.next = p.next;
        p.next.prev = head;
        size--;
        return p.item;
    }

    public T removeLast() {
        if (isEmpty()) {
            return null;
        }
        Node<T> p = tail.prev;
        tail.prev = p.prev;
        p.prev.next = tail;
        size--;
        return p.item;
    }

    public T get(int index) {
        if (index < 0 || index >= size) {
            return null;
        }
        Node<T> p = head.next;
        for (int i = 0; i < index; i++) {
            p = p.next;
        }
        return p.item;
    }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof LinkedListDeque)) return false;
        LinkedListDeque<T> other = (LinkedListDeque<T>) o;
        if (this.size() != other.size()) return false;
        Node<T> p1 = head.next;
        Node<T> p2 = other.head.next;
        for (int i = 0; i < this.size(); i++) {
            if (p1.item != p2.item) {
                return false;
            }
            p1 = p1.next;
            p2 = p2.next;
        }
        return true;
    }



}
