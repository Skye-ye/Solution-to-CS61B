package deque;

import org.junit.Test;

import static org.junit.Assert.*;

public class ArrayDequeTest {
    @Test
    public void addAndRemoveTest() {
        ArrayDeque<Integer> test = new ArrayDeque<>();
        test.addFirst(1);
        test.addFirst(2);
        test.addFirst(3);
        test.addFirst(4);
        test.addLast(5);
        assertEquals(5, test.size());
        assertEquals(4, (int) test.removeFirst());
        assertEquals(5, (int) test.removeLast());
        assertEquals(1, (int) test.removeLast());
        test.addLast(7);
    }

    @Test
    public void resizeTest() {
        ArrayDeque<Integer> test = new ArrayDeque<>();
        for (int i = 0; i < 100; i++) {
            if (i % 2 == 0) {
                test.addFirst(i);
            } else {
                test.addLast(i);
            }
        }
    }

    @Test
    public void resizeWhenRemoveTest() {
        ArrayDeque<Integer> test = new ArrayDeque<>();
        for (int i = 0; i < 100; i++) {
            if (i % 2 == 0) {
                test.addFirst(i);
            } else {
                test.addLast(i);
            }
        }
        for (int i = 0; i < 100; i++) {
            if (i % 2 == 0) {
                test.removeFirst();
            } else {
                test.removeLast();
            }
        }
    }

    @Test
    public void  equalsTest() {
        ArrayDeque<Integer> test1 = new ArrayDeque<>();
        ArrayDeque<Integer> test2 = new ArrayDeque<>();
        for (int i = 0; i < 100; i++) {
            if (i % 2 == 0) {
                test1.addFirst(i);
                test2.addFirst(i);
            } else {
                test1.addLast(i);
                test2.addLast(i);
            }
        }
        assertTrue(test1.equals(test2));
        assertTrue(test2.equals(test1));
        test2.addLast(100);
        assertFalse(test1.equals(test2));
        assertFalse(test2.equals(test1));
    }
}