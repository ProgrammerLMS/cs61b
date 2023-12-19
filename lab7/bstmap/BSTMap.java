package bstmap;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;


/* In your implementation you should assume that
   generic keys K in BSTMap<K,V> extend Comparable.
   In other words, you can assume that generic keys K have a compareTo method.
   This can be enforced in Java with a bounded type parameter. */
/* See K extends Comparable<K> below */
public class BSTMap<K extends Comparable<K>, V> implements Map61B<K, V>{

    private class TreeNode {
        public final K key;
        public V value;
        public TreeNode left;
        public TreeNode right;

        public TreeNode(K k, V v) {
            key = k;
            value = v;
        }
    }

    private TreeNode root;
    private int size;

    public BSTMap() {
        root = null;
        size = 0;
    }

    @Override
    public void put(K key, V value) {
        size += 1;
        root = put(key, value, root);
    }

    private TreeNode put(K key, V value, TreeNode node) {
        if (node == null) {
            return new TreeNode(key, value);
        }
        int cmp = key.compareTo(node.key);
        if (cmp < 0) {
            node.left = put(key, value, node.left);
        } else if (cmp > 0) {
            node.right = put(key, value, node.right);
        } else {
            node.value = value;
        }
        return node;
    }

    @Override
    public void clear() {
        root = null;
        size = 0;
    }

    @Override
    public boolean containsKey(K key) {
        return containsKey(key, root);
    }

    private boolean containsKey(K key, TreeNode node) {
        if (node == null) {
            return false;
        }
        int cmp = key.compareTo(node.key);
        if (cmp > 0) {
            return containsKey(key, node.right);
        }
        if (cmp < 0) {
            return containsKey(key, node.left);
        }
        return true;
    }

    @Override
    public V get(K key) {
        return get(root, key);
    }

    private V get(TreeNode node, K key) {
        if (node == null) {
            return null;
        }
        int cmp = key.compareTo(node.key);
        if (cmp < 0) {
            return get(node.left, key);
        } else if (cmp > 0) {
            return get(node.right, key);
        }
        return node.value;
    }

    @Override
    public int size() {
        return this.size;
    }

    @Override
    public Set<K> keySet() {
        Set<K> set = new HashSet<K>();
        addKeySet(root, set);
        return set;
    }

    private void addKeySet(TreeNode node, Set<K> set) {
        if (node == null) {
            return;
        }
        set.add(node.key);
        addKeySet(node.left, set);
        addKeySet(node.right, set);
    }

    /* delete the node in BST */
    @Override
    public V remove(K key) {
        if (!containsKey(key)) {
            return null;
        }
        V value = get(key);
        size -= 1;
        root = remove(root, key);
        return value;
    }

    private TreeNode remove(TreeNode node, K key) {
        if (node == null) {
            return null;
        }
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
            TreeNode originalNode = node;
            node = getMinChild(node.right);
            node.left = originalNode.left;
            node.right = remove(originalNode.right, node.key);
        }
        return node;
    }

    private TreeNode getMinChild(TreeNode node) {
        if (node.left == null) {
            return node;
        }
        return getMinChild(node.left);
    }

    @Override
    public V remove(K key, V value) {
        if (!containsKey(key)) {
            return null;
        }
        V targetValue = get(key);
        if (!targetValue.equals(value)) {
            return null;
        }
        size -= 1;
        root = remove(root, key);
        return targetValue;
    }

    private void printInOrder(TreeNode node) {
        if (node == null) {
            return;
        }
        printInOrder(node.left);
        System.out.println(node.key.toString() + " -> " + node.value.toString());
        printInOrder(node.right);
    }

    @Override
    public Iterator<K> iterator() {
        return keySet().iterator();
    }
}
