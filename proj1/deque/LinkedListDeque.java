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
        Node<T> newNode = new Node<>(item, head.next, head);
        head.next.prev = newNode;
        head.next = newNode;
        size++;
    }

    public void addLast(T item) {
        Node<T> newNode = new Node<>(item, tail, tail.prev);
        tail.prev.next = newNode;
        tail.prev = newNode;
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
        while (p != tail) {
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
        if (index < 0 || index >= size || isEmpty()) {
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

        LinkedListDeque<?> other = (LinkedListDeque<?>) o;
        if (this.size() != other.size()) return false;

        Node<T> p1 = this.head.next;
        Node<?> p2 = other.head.next;

        while (p1 != this.tail) {
            if (p1.item == null) {
                if (p2.item != null) return false;
            } else {
                if (!p1.item.equals(p2.item)) return false;
            }
            p1 = p1.next;
            p2 = p2.next;
        }
        return true;
    }


}
