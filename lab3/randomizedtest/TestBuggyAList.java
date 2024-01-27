package randomizedtest;

import edu.princeton.cs.algs4.StdRandom;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by hug.
 */
public class TestBuggyAList {
    // YOUR TESTS HERE
    @Test
    public void testThreeAddThreeRemove() {
        AListNoResizing<Integer> correct = new AListNoResizing<>();
        BuggyAList<Integer> buggy = new BuggyAList<>();
        correct.addLast(4);
        buggy.addLast(4);
        correct.addLast(5);
        buggy.addLast(5);
        correct.addLast(6);
        buggy.addLast(6);
        assertEquals(correct.removeLast(), buggy.removeLast());
        assertEquals(correct.removeLast(), buggy.removeLast());
        assertEquals(correct.removeLast(), buggy.removeLast());
    }

    @Test
    public void randomizedTest() {
        AListNoResizing<Integer> correct = new AListNoResizing<>();
        BuggyAList<Integer> buggy = new BuggyAList<>();

        int N = 5000;
        for (int i = 0; i < N; i += 1) {
            int operationNumber = StdRandom.uniform(0, 4);
            if (operationNumber == 0) {
                // addLast
                int randVal = StdRandom.uniform(0, 100);
                correct.addLast(randVal);
                buggy.addLast(randVal);
                System.out.println("addLast(" + randVal + ")");
            } else if (operationNumber == 1) {
                // size
                int sizeOfCorrect = correct.size();
                int sizeOfBuggy = buggy.size();
                System.out.println("Size of correct list: " + sizeOfCorrect);
                System.out.println("Size of buggy list: " + sizeOfBuggy);
                assertEquals(sizeOfCorrect, sizeOfBuggy);
            } else if (operationNumber == 2) {
                // getLast
                if (correct.size() == 0 || buggy.size() == 0) {
                    continue;
                }
                int correctGetLast = correct.getLast();
                int buggyGetLast = buggy.getLast();
                System.out.println("Last of correct list: " + correctGetLast);
                System.out.println("Last of buggy list: " + buggyGetLast);
                assertEquals(correctGetLast, buggyGetLast);
            } else {
                // removeLast
                if (correct.size() == 0 || buggy.size() == 0) {
                    continue;
                }
                int correctRemoveLast = correct.removeLast();
                int buggyRemoveLast = buggy.removeLast();
                System.out.println("Removed last of correct list: " + correctRemoveLast);
                System.out.println("Removed last of buggy list: " + buggyRemoveLast);
                assertEquals(correctRemoveLast, buggyRemoveLast);
            }
        }
    }
}
