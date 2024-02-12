package deque;

import java.util.Iterator;

public class ArrayDeque<T> implements Iterable<T>, Deque<T> {
    private T[] items;
    private int size;
    private int nextFirst;
    private int nextLast;

    @SuppressWarnings("unchecked")
    public ArrayDeque() {
        items = (T[]) new Object[8];
        size = 0;
        nextFirst = 7;
        nextLast = 0;
    }

    @SuppressWarnings("unchecked")
    private void resize(int capacity) {
        T[] a = (T[]) new Object[capacity];
        int oldIndex = (nextFirst + 1) % items.length;
        for (int newIndex = 0; newIndex < size; newIndex++) {
            a[newIndex] = items[oldIndex];
            oldIndex = (oldIndex + 1) % items.length;
        }
        items = a;
        nextFirst = capacity - 1;
        nextLast = size;
    }

    @Override
    public void addFirst(T item) {
        if (size == items.length) {
            resize(size * 2);
        }
        items[nextFirst] = item;
        nextFirst = (nextFirst - 1 + items.length) % items.length;
        size += 1;
    }

    @Override
    public void addLast(T item) {
        if (size == items.length) {
            resize(size * 2);
        }
        items[nextLast] = item;
        nextLast = (nextLast + 1) % items.length;
        size += 1;
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public void printDeque() {
        for (int i = (nextFirst + 1) % items.length; i != nextLast; i = (i + 1) % items.length) {
            System.out.print(items[i] + " ");
        }
    }

    private T removeTemplate(int nextFirst) {
        T temp = items[nextFirst];
        items[nextFirst] = null;
        size -= 1;
        if (items.length >= 16 && size < items.length / 4) {
            resize(items.length / 2);
        }
        return temp;
    }

    @Override
    public T removeFirst() {
        if (size == 0) {
            return null;
        }
        nextFirst = (nextFirst + 1) % items.length;
        return removeTemplate(nextFirst);
    }

    @Override
    public T removeLast() {
        if (size == 0) {
            return null;
        }
        nextLast = (nextLast - 1 + items.length) % items.length;
        return removeTemplate(nextLast);
    }

    @Override
    public T get(int index) {
        if (index >= size || index < 0) {
            return null;
        }
        return items[(nextFirst + 1 + index) % items.length];
    }

    public Iterator<T> iterator() {
        return new ArrayDequeIterator();
    }

    private class ArrayDequeIterator implements Iterator<T> {
        private int wizPos;

        public ArrayDequeIterator() {
            wizPos = 0;
        }

        public boolean hasNext() {
            return wizPos < size;
        }

        public T next() {
            T returnItem = items[(nextFirst + 1 + wizPos) % items.length];
            wizPos += 1;
            return returnItem;
        }
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

}
