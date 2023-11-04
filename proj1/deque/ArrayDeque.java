package deque;

import java.util.Iterator;

public class ArrayDeque<T> implements Deque<T>, Iterable<T>{
    private T[] items;
    private int size;
    private int base;
    private int end;

    @SuppressWarnings("unchecked")
    public ArrayDeque() {
        items = (T[]) new Object[8];
        size = 0;
        base = 4;
        end = 5;
    }

    @SuppressWarnings("unchecked")
    private void resize(int capacity) {
        T[] a = (T[]) new Object[capacity];
        int ind;
        for (int i = 0; i < size; i += 1) {
            ind = arrayIndex(i);
            a[capacity / 4 + i] = items[ind];
        }
        items = a;
        base = capacity / 4 - 1;
        end = base + size + 1;
    }

    private int arrayIndex(int ind) {
        if (base + 1 + ind >= items.length) {
            return base + 1 + ind - items.length;
        } else {
            return base + 1 + ind;
        }
    }

    @Override
    public void addFirst(T item) {
        if (size == items.length - 2) {
            resize((items.length * 2));
        }

        items[base] = item;
        if (base == 0) {
            base = items.length - 1;
        } else {
            base -= 1;
        }
        size = size + 1;
    }

    @Override
    public void addLast(T item) {
        if (size == items.length - 2) {
            resize((items.length * 2));
        }

        items[end] = item;
        if (end == items.length - 1) {
            end = 0;
        } else {
            end += 1;
        }
        size = size + 1;
    }

    @Override
    public int size() {
        return this.size;
    }

    @Override
    public void printDeque() {
        for (T i : this.items) {
            System.out.print(i + " ");
        }
    }

    @Override
    public T removeFirst() {
        if (isEmpty()) {
            return null;
        }
        if ((size < items.length / 4) && (size > 8)) {
            resize(items.length / 2);
        }
        T item = items[arrayIndex(0)];
        int ind = arrayIndex(0);
        items[ind] = null;
        size = size - 1;
        base = ind;
        return item;
    }

    @Override
    public T removeLast() {
        if (isEmpty()) {
            return null;
        }
        if ((size < items.length / 4) && (size > 8)) {
            resize(items.length / 2);
        }
        T item = items[arrayIndex(size - 1)];
        int ind = arrayIndex(size - 1);
        items[ind] = null;
        size = size - 1;
        end = ind;
        return item;
    }

    @Override
    public T get(int index) {
        int ind =  arrayIndex(index);
        return items[ind];
    }

    @Override
    public boolean equals(Object o) {
        if (o == null) {
            return false;
        }
        if (o == this) {
            return true;
        }
        if (!(o instanceof ArrayDeque)) {
            return false;
        }
        ArrayDeque<?> ad = (ArrayDeque<?>) o;
        if (ad.size() != size) {
            return false;
        }
        for (int i = 0; i < size; i++) {
            if (ad.get(i) != get(i)) {
                return false;
            }
        }
        return true;
    }

    public Iterator<T> iterator() {
        return new ArrayDequeIterator();
    }

    private class ArrayDequeIterator implements Iterator<T> {
        private int wizPos;

        private ArrayDequeIterator() {
            wizPos = 0;
        }

        public boolean hasNext() {
            return wizPos < size;
        }

        public T next() {
            T item = get(wizPos);
            wizPos += 1;
            return item;
        }
    }

}
