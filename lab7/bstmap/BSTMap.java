package bstmap;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class BSTMap<K extends Comparable<K>, V> implements Map61B<K, V> {

    private class BSTNode {
        public K key;
        public V value;
        public BSTNode left;
        public BSTNode right;

        public BSTNode(K k, V v) {
            key = k;
            value = v;
        }
    }

    private BSTNode root;
    int size;

    public BSTMap() {
        root = null;
        size = 0;
    }

    public void printInOrder() {
        printInOrderHelper(root);
    }

    private void printInOrderHelper(BSTNode node) {
        if (node == null) {
            return;
        }
        printInOrderHelper(node.left);
        System.out.println(node.key.toString() + " -> " + node.value.toString());
        printInOrderHelper(node.right);
    }

    @Override
    public void clear() {
        root = null;
        size = 0;
    }

    @Override
    public boolean containsKey(K key) {
        return containsKeyHelper(root, key);
    }

    private boolean containsKeyHelper(BSTNode node, K key) {
        if (node == null) {
            return false;
        }
        int cmp = key.compareTo(node.key);
        if (cmp > 0) {
            return containsKeyHelper(node.right, key);
        } else if (cmp < 0) {
            return containsKeyHelper(node.left, key);
        } else {
            return true;
        }
    }

    @Override
    public V get(K key) {
        return getHelper(root, key);
    }

    private V getHelper(BSTNode node, K key) {
        if (node == null) {
            return null;
        }
        int cmp = key.compareTo(node.key);
        if (cmp > 0) {
            return getHelper(node.right, key);
        } else if (cmp < 0) {
            return getHelper(node.left, key);
        } else {
            return node.value;
        }
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public void put(K key, V value) {
        root = putHelper(root, key, value);
    }

    private BSTNode putHelper(BSTNode node, K key, V value) {
        if (node == null) {
            size += 1;
            return new BSTNode(key, value);
        }
        int cmp = key.compareTo(node.key);
        if (cmp > 0) {
            node.right = putHelper(node.right, key, value);
        } else if (cmp < 0) {
            node.left = putHelper(node.left, key, value);
        } else {
            node.value = value;
        }
        return node;
    }

    @Override
    public Set<K> keySet() {
        HashSet<K> set = new HashSet<>();
        keySetHelper(root, set);
        return set;
    }

    private void keySetHelper(BSTNode node, Set<K> set) {
        if (node == null) {
            return;
        }
        set.add(node.key);
        keySetHelper(node.left, set);
        keySetHelper(node.right, set);
    }

    @Override
    public V remove(K key) {
        if (!containsKey(key)) {
            return null;
        }
        V value = get(key);
        root = removeHelper(root, key);
        size -= 1;
        return value;
    }

    private BSTNode removeHelper(BSTNode node, K key) {
        if (node == null) {
            return null;
        }
        int cmp = key.compareTo(node.key);
        if (cmp > 0) {
            node.right = removeHelper(node.right, key);
        } else if (cmp < 0) {
            node.left = removeHelper(node.left, key);
        } else {
            if (node.left == null) {
                return node.right;
            }
            if (node.right == null) {
                return node.left;
            }
            BSTNode temp = node;
            node = minNode(temp.right);
            node.right = removeMin(temp.right);
            node.left = temp.left;
        }
        return node;
    }

    private BSTNode minNode(BSTNode node) {
        if (node.left == null) {
            return node;
        }
        return minNode(node.left);
    }

    private BSTNode removeMin(BSTNode node) {
        if (node.left == null) {
            return node.right;
        }
        node.left = removeMin(node.left);
        return node;
    }

    @Override
    public V remove(K key, V value) {
        if (!containsKey(key) || !get(key).equals(value)) {
            return null;
        }
        root = removeHelper(root, key);
        size -= 1;
        return value;
    }

    @Override
    public Iterator<K> iterator() {
        return keySet().iterator();
    }
}
