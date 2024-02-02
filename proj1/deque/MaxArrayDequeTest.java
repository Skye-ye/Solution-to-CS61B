package deque;

import org.junit.Test;

import java.util.Optional;

import static org.junit.Assert.*;

public class MaxArrayDequeTest {
    @Test
    public void maxTest() {
        MaxArrayDeque<Integer> test = new MaxArrayDeque<>(new IntegerComparator());
        test.addFirst(1);
        test.addFirst(2);
        test.addFirst(3);
        test.addFirst(4);
        test.addLast(5);
        int expected = 5;
        int result1 = test.max();
        int result2 = test.max(new IntegerComparator());
        assertEquals(expected, result1);
        assertEquals(expected, result2);
    }
}