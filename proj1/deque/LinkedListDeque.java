package deque;
import java.util.Iterator;
/*
* double-ended queue
* @author: LiMeng
* */
public class LinkedListDeque<T> implements Deque<T>{
    private int size;

    private class DequeNode {
        private T item;
        private DequeNode next;
        private DequeNode pre;

        public DequeNode (T _item, DequeNode _pre, DequeNode _next) {
            item = _item;
            next = _next;
            pre = _pre;
        }
    }

    private final DequeNode sentinel;

    public LinkedListDeque() {
        size = 0;
        sentinel = new DequeNode(null, null, null);
        sentinel.pre = sentinel;
        sentinel.next = sentinel;
    }

    /* item is never null */
    @Override
    public void addFirst(T item) {
        if(item == null) {
            return;
        }
        DequeNode newNode = new DequeNode(item, null, null);
        newNode.pre = sentinel;
        newNode.next = sentinel.next;
        sentinel.next.pre = newNode;
        sentinel.next = newNode;
        size ++;
    }

    /* item is never null */
    @Override
    public void addLast(T item) {
        if(item == null) {
            return;
        }
        DequeNode newNode = new DequeNode(item, null, null);
        DequeNode lastNode = sentinel.pre;
        lastNode.next = newNode;
        newNode.pre = lastNode;
        newNode.next = sentinel;
        sentinel.pre = newNode;
        size ++;
    }

    @Override
    public boolean isEmpty() {
        return size == 0;
    }

    @Override
    public int size() {
        return this.size;
    }

    /*
    * Prints the items in the deque from first to last, separated by a space.
    * Once all the items have been printed, print out a new line.
    *  */
    @Override
    public void printDeque() {
        for (DequeNode p = sentinel.next; p.item != null; p = p.next) {
            System.out.print(p.item + " ");
        }
    }

    @Override
    public T removeFirst() {
        if (isEmpty()) {
            return null;
        }
        DequeNode firstNode = sentinel.next;
        sentinel.next = firstNode.next;
        firstNode.next.pre = sentinel;
        T item = firstNode.item;
        firstNode.next = null;
        firstNode.pre = null;
        firstNode.item = null;
        size --;
        return item;
    }

    @Override
    public T removeLast() {
        if (isEmpty()) {
            return null;
        }
        DequeNode lastNode = sentinel.pre;
        sentinel.pre = lastNode.pre;
        lastNode.pre.next = sentinel;
        T item = lastNode.item;
        lastNode.next = null;
        lastNode.pre = null;
        lastNode.item = null;
        size --;
        return item;
    }

    @Override
    public T get(int index) {
        if(index < 0) {
            return null;
        }
        int i = 0;
        for (DequeNode p = sentinel.next; p.item != null; p = p.next) {
            if (i != index) {
                i ++;
            } else {
                return p.item;
            }
        }
        return null;
    }

    public T getRecursive(int index) {
        if(index < 0) {
            return null;
        }
        return getRecursive(index, 0, sentinel.next);
    }

    private T getRecursive (int index, int i, DequeNode p) {
        if (p.item == null) {
            return null;
        }
        if (i == index) {
            return p.item;
        }
        return getRecursive(index,i+1, p.next);
    }

    public Iterator<T> iterator() {
        return null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null) {
            return false;
        }
        if (!(o instanceof Deque)) {
            return false;
        }
        Deque<T> od = (Deque<T>) o;
        if (od.size() != this.size()) {
            return false;
        }
        for (int i = 0; i < size; i++) {
            if ( !(od.get(i).equals(this.get(i))) ) {
                return false;
            }
        }
        return true;
    }
}
