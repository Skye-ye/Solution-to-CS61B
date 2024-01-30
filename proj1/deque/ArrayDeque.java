package deque;

public class ArrayDeque<T> {
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

    public void addFirst(T item) {
        if (size == items.length) {
            resize(size * 2);
        }
        items[nextFirst] = item;
        nextFirst = (nextFirst - 1 + items.length) % items.length;
        size += 1;
    }

    public void addLast(T item) {
        if (size == items.length) {
            resize(size * 2);
        }
        items[nextLast] = item;
        nextLast = (nextLast + 1) % items.length;
        size += 1;
    }

    public boolean isEmpty() {
        return size == 0;
    }

    public int size() {
        return size;
    }

    public void printDeque() {
        for (int i = (nextFirst + 1) % items.length; i != nextLast; i = (i + 1) % items.length) {
            System.out.print(items[i] + " ");
        }
    }

    public T removeFirst() {
        if (size == 0) {
            return null;
        }
        nextFirst = (nextFirst + 1) % items.length;
        T temp = items[nextFirst];
        items[nextFirst] = null;
        size -= 1;
        if (items.length >= 16 && size < items.length / 4) {
            resize(items.length / 2);
        }
        return temp;
    }

    public T removeLast() {
        if (size == 0) {
            return null;
        }
        nextLast = (nextLast - 1 + items.length) % items.length;
        T temp = items[nextLast];
        items[nextLast] = null;
        size -= 1;
        if (items.length >= 16 && size < items.length / 4) {
            resize(items.length / 2);
        }
        return temp;
    }

    public T get(int index) {
        if (index >= size || index < 0) {
            return null;
        }
        return items[(nextFirst + 1 + index) % items.length];
    }


}