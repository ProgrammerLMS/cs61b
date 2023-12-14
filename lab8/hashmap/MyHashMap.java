package hashmap;

import java.util.*;

/**
 *  A hash table-backed Map implementation. Provides amortized constant time
 *  access to elements via get(), remove(), and put() in the best case.
 *
 *  Assumes null keys will never be inserted, and does not resize down upon remove().
 *
 *  Before we start on this lab, we have to talk about the ideas in this lab
 *  The purpose of the starter code is to have an eaiser way to try out different bucket types with MyHashMap.
 *  It accomplishes this through polymorphism and inheritance, which we learned about earlier this semester.
 *
 *  It also makes use of factory methods, which are used to create objects.
 *  We will use factory methods to create the buckets.
 *  The inheritance structure of the starter files is as follows:
 *
 *  Map61B.java
 * └── MyHashMap.java
 *     ├── MyHashMapALBuckets.java
 *     ├── MyHashMapHSBuckets.java
 *     ├── MyHashMapLLBuckets.java
 *     ├── MyHashMapPQBuckets.java
 *     └── MyHashMapTSBuckets.java
 *
 *  @author LMS
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

    /* Instance Variables, which is the underlying data structure of the hash table.  */
    /*  when we build a hash table, we can choose a number of
        different data structures to be the buckets.  */
    /* It is an array (or table) of Collection<Node> objects,
       where each Collection of Nodes represents a single bucket in the hash table */
    /* java.util.Collection is an interface which most data structures inherit from,
       and it represents a group of objects. The Collection interface supprots methods
       like add to the group, remove from the group, and iterate over a group.
       Many data structures in java.util implement Collection,
       including ArrayList, LinkedList, TreeSet, HashSet, PriorityQueue, and many others.
       Note that because these data structures implement Collection,
       we can assign them to a variable of static type Collection with polymorphism */
    /* Therefore, our array of Collection<Node> objects can be instantated
       by many different types of data structures, e.g. LinkedList<Node> or ArrayList<Node> */
    private Collection<Node>[] buckets;

    private int initialSize;
    /* You should increase the size of your MyHashMap
       when the load factor exceeds the set loadFactor.
       Recall that the load factor can be computed as loadFactor = N/M,
       where N is the number of elements in the map and M is the number of buckets */
    private final double loadFactor;

    private final Set<K> hashKeys;

    private int size = 0;

    /** Constructors */
    /* In java, you cannot create an array of parameterized type.
       Collection<Node> is a parameterized type,
       because we parameterize the Collection class with the Node class.
       Therefore, the expression new Collection<Node>[size] is illegal, for any given size.
       To get around this, you should instead create a new Collection[size],
       where size is the desired size.
       The elements of a Collection[] can be a collection of any type,
       like a Collection<Integer> or a Collection<Node>.
       For our purposes, we will only add elements of type Collection<Node> to our Collection[]. */
    public MyHashMap() {
        this.initialSize = 16;
        this.loadFactor = 0.75;
        buckets = createTable(this.initialSize);
        hashKeys = new HashSet<>();
    }

    /* During this lab, we will try out hash tables with
       different data structures for each of the buckets, and
       see empirically if there is an asymptotic difference
       between using different data structures as hash table buckets. */
    public MyHashMap(int initialSize) {
        this.initialSize = initialSize;
        this.loadFactor = 0.75;
        buckets = createTable(initialSize);
        hashKeys = new HashSet<>();
    }

    /**
     * MyHashMap constructor that creates a backing array of initialSize.
     * The load factor (# items / # buckets) should always be <= loadFactor
     *
     * @param initialSize initial size of backing array
     * @param maxLoad maximum load factor
     */
    public MyHashMap(int initialSize, double maxLoad) {
        this.initialSize = initialSize;
        this.loadFactor = maxLoad;
        buckets = createTable(initialSize);
        hashKeys = new HashSet<>();
    }

    /**
     * Returns a table to back our hash table. As per the comment
     * above, this table can be an array of Collection objects
     *
     * BE SURE TO CALL THIS FACTORY METHOD WHEN CREATING A TABLE SO
     * THAT ALL BUCKET TYPES ARE OF JAVA.UTIL.COLLECTION
     *
     * @param tableSize the size of the table to create
     */
    private Collection<Node>[] createTable(int tableSize) {
        Collection<Node>[] table = new Collection[tableSize];
        for (int i = 0;i < tableSize; i ++) {
            table[i] = createBucket();
        }
        return table;
    }


    /**
     * Returns a data structure to be a hash table bucket
     *
     * The only requirements of a hash table bucket are that we can:
     *  1. Insert items (`add` method)
     *  2. Remove items (`remove` method)
     *  3. Iterate through items (`iterator` method)
     *
     * Each of these methods is supported by java.util.Collection,
     * Most data structures in Java inherit from Collection, so we
     * can use almost any data structure as our buckets.
     *
     * The mechanism by which this happens is a factory method
     * protected Collection<Node> createBucket(),
     * which simply returns a data structure that implements Collection.
     * For MyHashMap.java, you can choose any data structure you’d like.
     *
     * Override this method to use different data structures as
     * the underlying bucket type
     *
     * Instead of creating new bucket data structures with the new operator,
     * you must use the createBucket method instead.
     * This might seem useless at first, but it allows the MyHashMap*Buckets.java classes
     * to override the createBucket method and
     * provide different data structures as each of the buckets.
     *
     * BE SURE TO CALL THIS FACTORY METHOD INSTEAD OF CREATING YOUR
     * OWN BUCKET DATA STRUCTURES WITH THE NEW OPERATOR!
     */
    protected Collection<Node> createBucket() {
        return new LinkedList<Node>();
    }

    /* We provide additional factory methods createTable
       to create the backing array of the hash table
       and createNode to create new Node objects as well.
       It’s okay if you use new operators to create the backing array and Node objects
       instead of the factory method, but we added them for uniformity.*/

    /**
     * Returns a new node to be placed in a hash table bucket
     */
    private Node createNode(K key, V value) {
        return new Node(key, value);
    }

    /* For these methods, we recommend you simply create
       a HashSet instance variable that holds all your keys.*/

    @Override
    public void clear() {
        int bucketSize = buckets.length;
        buckets = createTable(bucketSize);
        hashKeys.clear();
        size = 0;
    }

    private int getHashIndex(K key) {
        int hashCode = key.hashCode();
        return Math.floorMod(hashCode, initialSize);
    }

    @Override
    public boolean containsKey(K key) {
        return hashKeys.contains(key);
    }

    private Node getNode(K key) {
        if (key == null) {
            return null;
        }
        int index = getHashIndex(key);
        for (Node node : buckets[index]) {
            if (node != null && node.key.equals(key)) {
                return node;
            }
        }
        return null;
    }

    @Override
    public V get(K key) {
        Node node = getNode(key);
        if (node == null) {
            return null;
        }
        return node.value;
    }

    @Override
    public int size() {
        return this.size;
    }

    private void resize(int newTableSize) {
        LinkedList<Node> nodes = new LinkedList<>();
        for (K key : hashKeys) {
            V value = get(key);
            Node node = createNode(key, value);
            nodes.add(node);
        }
        // here a bug is that i change initialSize first
        initialSize = newTableSize;
        Collection<Node>[] newBuckets = createTable(newTableSize);
        for (Node node : nodes) {
            int newHashIndex = getHashIndex(node.key);
            newBuckets[newHashIndex].add(node);
        }
        buckets = newBuckets;
    }

    @Override
    public void put(K key, V value) {
        if (key == null || value == null) {
            return;
        }
        Node node = getNode(key);
        if (node == null) {
            int hashIndex = getHashIndex(key);
            Node newNode = createNode(key, value);

            buckets[hashIndex].add(newNode);
            hashKeys.add(key);
            size ++;

            if (size * 1.0 / buckets.length > loadFactor) {
                resize(2 * buckets.length);
            }
        } else {
            node.value = value;
        }
    }

    @Override
    public Set<K> keySet() {
        return hashKeys;
    }

    @Override
    public V remove(K key) {
        return remove(key, get(key));
    }

    @Override
    public V remove(K key, V value) {
        if (key == null) {
            return null;
        }
        int hashIndex = getHashIndex(key);
        Node node = getNode(key);
        if (node == null || !node.value.equals(value)) {
            return null;
        }
        buckets[hashIndex].remove(node);
        hashKeys.remove(key);
        size -= 1;
        return value;
    }

    /* iterator returns an Iterator that iterates over the stored keys */
    @Override
    public Iterator<K> iterator() {
        return hashKeys.iterator();
    }
}
