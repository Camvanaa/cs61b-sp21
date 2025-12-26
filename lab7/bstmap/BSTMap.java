package bstmap;

import java.util.Iterator;
import java.util.Set;
import java.util.Stack;

public class BSTMap<K extends Comparable<K>, V> implements Map61B<K, V> {
    private Node<K, V> root;
    private int size;

    private static class Node<K, V> {
        K key;
        V value;
        Node<K, V> left;
        Node<K, V> right;

        public Node(K key, V value, Node<K, V> left, Node<K, V> right) {
            this.key = key;
            this.value = value;
            this.left = left;
            this.right = right;
        }
    }

    public BSTMap() {
        root = null;
        size = 0;
    }

    /* Removes all of the mappings from this map.*/
    @Override
    public void clear() {
        root = null;
        size = 0;
    }

    /* Returns true if this map contains a mapping for the specified key. */
    @Override
    public boolean containsKey(K key) {
        return containsKey(root, key);
    }

    private boolean containsKey(Node<K, V> node, K key) {
        if (node == null) {
            return false;
        }

        int cmp = key.compareTo(node.key);
        if (cmp < 0) {
            return containsKey(node.left, key);
        } else if (cmp > 0) {
            return containsKey(node.right, key);
        } else {
            return true;
        }
    }
    /* Returns the value to which the specified key is mapped, or null if this
     * map contains no mapping for the key.
     */
    @Override
    public V get(K key) {
        return get(root, key);
    }

    private V get(Node<K, V> node, K key) {
        if (node == null) {
            return null;
        }

        if (node.key.equals(key)) {
            return node.value;
        }

        int cmp = key.compareTo(node.key);
        if (cmp < 0) {
            return get(node.left, key);
        } else {
            return get(node.right, key);
        }
    }

    /* Returns the number of key-value mappings in this map. */
    @Override
    public int size() {
        return size;
    }

    /* Associates the specified value with the specified key in this map. */
    @Override
    public void put(K key, V value) {
        root = put(root, key, value);
    }

    private Node<K, V> put(Node<K, V> node, K key, V value) {
        if (node == null) {
            size++;
            return new Node<>(key, value, null, null);
        }

        int cmp = key.compareTo(node.key);
        if (cmp < 0) {
            node.left = put(node.left, key, value);
        } else if (cmp > 0) {
            node.right = put(node.right, key, value);
        } else {
            node.value = value;
        }
        return node;
    }

    /* Returns a Set view of the keys contained in this map. Not required for Lab 7.
     * If you don't implement this, throw an UnsupportedOperationException. */
    @Override
    public Set<K> keySet() {
        return new java.util.AbstractSet<>() {
            @Override
            public Iterator<K> iterator() {
                return BSTMap.this.iterator();
            }

            @Override
            public int size() {
                return BSTMap.this.size;
            }

            @Override
            public boolean contains(Object o) {
                return BSTMap.this.containsKey((K) o);
            }
        };
    }

    /* Removes the mapping for the specified key from this map if present.
     * Not required for Lab 7. If you don't implement this, throw an
     * UnsupportedOperationException. */
    @Override
    public V remove(K key) {
        V targetValue = get(key);
        if (targetValue != null) {
            root = remove(root, key);
            size--;
        }
        return targetValue;
    }

    private Node<K, V> remove(Node<K, V> node, K key) {
        if (node == null) return null;

        int cmp = key.compareTo(node.key);
        if (cmp < 0) {
            node.left = remove(node.left, key);
        } else if (cmp > 0) {
            node.right = remove(node.right, key);
        } else {
            if (node.left == null) {
                return node.right;
            }
            if (node.right == null) {
                return node.left;
            }

            Node<K, V> t = node;
            node = min(t.right);
            node.right = deleteMin(t.right);
            node.left = t.left;
        }
        return node;
    }

    private Node<K, V> min(Node<K, V> node) {
        if (node.left == null) return node;
        return min(node.left);
    }

    private Node<K, V> deleteMin(Node<K, V> node) {
        if (node.left == null) return node.right;
        node.left = deleteMin(node.left);
        return node;
    }



    /* Removes the entry for the specified key only if it is currently mapped to
     * the specified value. Not required for Lab 7. If you don't implement this,
     * throw an UnsupportedOperationException.*/
    @Override
    public V remove(K key, V value) {
        V targetValue = get(key);
        if (targetValue == null || !targetValue.equals(value)) {
            return null;
        }
        root = remove(root, key);
        size--;
        return targetValue;
    }

    @Override
    public Iterator<K> iterator() {
        return new BSTMapIterator();
    }

    private class BSTMapIterator implements Iterator<K> {
        private final Stack<Node<K, V>> stack = new Stack<>();

        public BSTMapIterator() {
            pushLeftNodes(root);
        }

        private void pushLeftNodes(Node<K, V> curr) {
            while (curr != null) {
                stack.push(curr);
                curr = curr.left;
            }
        }

        public boolean hasNext() {
            return !stack.isEmpty();
        }

        public K next() {
            Node<K, V> curr = stack.pop();
            pushLeftNodes(curr.right);
            return curr.key;
        }
    }



    public void printInOrder() {
        printInOrder(root);
    }

    private void printInOrder(Node<K, V> x) {
        if (x == null) return;
        printInOrder(x.left);
        System.out.println(x.key);
        printInOrder(x.right);
    }
}
