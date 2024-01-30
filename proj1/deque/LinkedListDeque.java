package deque;

public class LinkedListDeque<T> {
    private class Node {
        public T item;
        public Node next;
        public Node prev;

        public Node(T i, Node n, Node p) {
            item = i;
            next = n;
            prev = p;
        }
    }

    private final Node sentinel;
    private int size;

    public LinkedListDeque() {
        sentinel = new Node(null, null, null);
        sentinel.next = sentinel;
        sentinel.prev = sentinel;
        size = 0;
    }

    public LinkedListDeque(T x) {
        sentinel = new Node(null, null, null);
        sentinel.next = new Node(x, sentinel, sentinel);
        sentinel.prev = sentinel.next;
        size = 1;
    }

    public void addFirst(T x) {
        Node temp = new Node(x, sentinel.next, sentinel);
        sentinel.next.prev = temp;
        sentinel.next = temp;
        size += 1;
    }

    public void addLast(T x) {
        Node temp = new Node(x, sentinel, sentinel.prev);
        sentinel.prev.next = temp;
        sentinel.prev = temp;
        size += 1;
    }

    public boolean isEmpty() {
        return size == 0;
    }

    public int size() {
        return size;
    }

    public void printDeque() {
        if (!this.isEmpty()) {
            Node curr = sentinel.next;
            while (curr.next != sentinel) {
                System.out.print(curr.item + " ");
                curr = curr.next;
            }
            System.out.print(curr.item);
        }
        System.out.print('\n');
    }

    public T removeFirst() {
        if (this.isEmpty()) {
            return null;
        }
        Node temp = sentinel.next;
        sentinel.next = temp.next;
        temp.next.prev = sentinel;
        size -= 1;
        return temp.item;
    }

    public T removeLast() {
        if (this.isEmpty()) {
            return null;
        }
        Node temp = sentinel.prev;
        sentinel.prev = temp.prev;
        temp.prev.next = sentinel;
        size -= 1;
        return temp.item;
    }

    public T get(int index) {
        if (index >= size || index < 0) {
            return null;
        }
        Node curr;
        if (index < size / 2) {
            curr = sentinel.next;
            for (int i = 0; i < index; i++) {
                curr = curr.next;
            }
        } else {
            curr = sentinel.prev;
            for (int i = size - 1; i > index; i--) {
                curr = curr.prev;
            }
        }
        return curr.item;
    }

    public T getRecursive(int index) {
        if (index >= size || index < 0) {
            return null;
        }
        return getRecursiveHelper(index, sentinel.next);
    }

    // Helper method for getRecursive
    private T getRecursiveHelper(int index, Node curr) {
        if (index == 0) {
            return curr.item;
        }
        return getRecursiveHelper(index - 1, curr.next);
    }
}