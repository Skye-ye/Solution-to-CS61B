package hashmap;

import java.util.*;

/**
 * A hash-table-backed Map implementation.
 * Provides amortized constant time
 * access to elements via get(), remove(), and put() in the best case.
 * <p>
 * Assumes null keys will never be inserted, and does not resize down upon remove().
 *
 * @author YOUR NAME HERE
 */
public class MyHashMap<K, V> implements Map61B<K, V> {
    /**
     * Protected helper class to store key/value pairs
     * The protected qualifier allows subclass access
     */
    protected class Node {
        K key;
        V value;

        Node(K k, V v) {
            key = k;
            value = v;
        }
    }

    /* Instance Variables */
    private Collection<Node>[] buckets;
    // You should probably define some more!
    private int size = 0;
    private int capacity;
    private double loadFactor;

    private static final int DEFAULT_INITIAL_SIZE = 16;
    private static final double DEFAULT_LOAD_FACTOR = 0.75;

    /**
     * Constructors
     */
    public MyHashMap() {
        this(DEFAULT_INITIAL_SIZE, DEFAULT_LOAD_FACTOR);
    }

    public MyHashMap(int initialSize) {
        this(initialSize, DEFAULT_LOAD_FACTOR);
    }

    /**
     * MyHashMap constructor that creates a backing array of initialSize.
     * The load factor (# items / # buckets) should always be <= loadFactor
     *
     * @param initialSize initial size of a backing array
     * @param maxLoad     maximum load factor
     */
    public MyHashMap(int initialSize, double maxLoad) {
        capacity = initialSize;
        loadFactor = maxLoad;
        buckets = createTable(capacity);
    }

    /**
     * Returns a new node to be placed in a hash table bucket
     */
    private Node createNode(K key, V value) {
        return new Node(key, value);
    }

    /**
     * Returns a data structure to be a hash table bucket
     * <p>
     * The only requirements of a hash table bucket are that we can:
     * 1. Insert items (`add` method)
     * 2. Remove items (`remove` method)
     * 3. Iterate through items (`iterator` method)
     * <p>
     * Java.util.Collection supports each of these methods,
     * Most data structures in Java inherit from Collection, so we
     * can use almost any data structure as our buckets.
     * <p>
     * Override this method to use different data structures as
     * the underlying bucket type
     * <p>
     * BE SURE TO CALL THIS FACTORY METHOD INSTEAD OF CREATING YOUR
     * OWN BUCKET DATA STRUCTURES WITH THE NEW OPERATOR!
     */
    protected Collection<Node> createBucket() {
        return new LinkedList<>();
    }

    /**
     * Returns a table to back our hash table. As per the comment
     * above, this table can be an array of Collection objects
     * <p>
     * BE SURE TO CALL THIS FACTORY METHOD WHEN CREATING A TABLE SO
     * THAT ALL BUCKET TYPES ARE OF JAVA.UTIL.COLLECTION
     *
     * @param tableSize the size of the table to create
     */
    @SuppressWarnings("unchecked")
    private Collection<Node>[] createTable(int tableSize) {
        Collection<Node>[] table = new Collection[tableSize];
        for (int i = 0; i < tableSize; i += 1) {
            table[i] = createBucket();
        }
        return table;
    }

    private int getIndex(K key) {
        return Math.floorMod(key.hashCode(), capacity);
    }

    @Override
    public void clear() {
        buckets = createTable(DEFAULT_INITIAL_SIZE);
        size = 0;
    }

    @Override
    public boolean containsKey(K key) {
        int index = getIndex(key);
        if (buckets[index].isEmpty()) {
            return false;
        }
        for (Node node : buckets[index]) {
            if (node.key.equals(key)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public V get(K key) {
        int index = getIndex(key);
        if (buckets[index].isEmpty()) {
            return null;
        }
        for (Node node : buckets[index]) {
            if (node.key.equals(key)) {
                return node.value;
            }
        }
        return null;
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public void put(K key, V value) {
        int index = getIndex(key);
        if (get(key) != null) {
            for (Node node : buckets[index]) {
                if (node.key.equals(key)) {
                    node.value = value;
                }
            }
        } else {
            buckets[index].add(createNode(key, value));
            size += 1;
        }
    }

    @Override
    public Set<K> keySet() {
        HashSet<K> set = new HashSet<>();
        for (Collection<Node> bucket : buckets) {
            for (Node node : bucket) {
                set.add(node.key);
            }
        }
        return set;
    }

    @Override
    public V remove(K key) {
        int index = getIndex(key);
        if (buckets[index].isEmpty()) {
            return null;
        }
        for (Node node : buckets[index]) {
            if (node.key.equals(key)) {
                buckets[index].remove(node);
                size -= 1;
                return node.value;
            }
        }
        return null;
    }

    @Override
    public V remove(K key, V value) {
        int index = getIndex(key);
        if (buckets[index].isEmpty()) {
            return null;
        }
        for (Node node : buckets[index]) {
            if (node.key.equals(key) && node.value.equals(value)) {
                buckets[index].remove(node);
                size -= 1;
                return node.value;
            }
        }
        return null;
    }

    @Override
    public Iterator<K> iterator() {
        return keySet().iterator();
    }
}
