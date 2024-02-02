package tester;

import static org.junit.Assert.*;

import edu.princeton.cs.algs4.StdRandom;
import org.junit.Test;
import student.StudentArrayDeque;

import java.util.ArrayDeque;

public class TestArrayDequeEC{
    @Test
    public void studentArrayDequeTest() {
        StudentArrayDeque<Integer> buggy = new StudentArrayDeque<>();
        ArrayDequeSolution<Integer> correct = new ArrayDequeSolution<>();
        ArrayDeque<String> record = new ArrayDeque<>();

        for (int i = 0; i < 100; i += 1) {
            int value = StdRandom.uniform(2);
            if (value == 0) {
                buggy.addFirst(i);
                correct.addFirst(i);
                record.addLast("addFirst(" + i + ")");
            } else {
                buggy.addLast(i);
                correct.addLast(i);
                record.addLast("addLast(" + i + ")");
            }
        }

        for (int i = 0; i < 100; i += 1) {
            int operation = StdRandom.uniform(2);
            Integer correctResult = operation == 0 ? correct.removeFirst() : correct.removeLast();
            Integer buggyResult = operation == 0 ? buggy.removeFirst() : buggy.removeLast();
            String operationRecord = operation == 0 ? "removeFirst()" : "removeLast()";
            record.addLast(operationRecord);

            String errorMessage = buildErrorMessage(record);
            assertEquals(errorMessage, correctResult, buggyResult);
        }
    }

    private String buildErrorMessage(ArrayDeque<String> record) {
        StringBuilder errorMessage = new StringBuilder("\n");
        for (String op : record) {
            errorMessage.append(op).append("\n");
        }
        return errorMessage.toString();
    }
}