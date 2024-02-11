package bstmap;
import org.junit.Test;
import static org.junit.Assert.*;

public class BSTMapTest {
    @Test
    public void testInsertRandom() {
        BSTMap<String, Integer> b = new BSTMap<String, Integer>();
        double time = InsertRandomSpeedTest.insertRandom(b, 1000, 10);
        assertTrue(time < 0.1);
    }

    @Test
    public void printInOrder() {
        BSTMap<String, Integer> b = new BSTMap<String, Integer>();
        b.put("c", 1);
        b.put("b", 2);
        b.put("a", 3);
        b.put("d", 4);
        b.put("e", 5);
        b.printInOrder();
    }
}
