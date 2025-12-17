package tester;

import static org.junit.Assert.*;
import org.junit.Test;
import edu.princeton.cs.introcs.StdRandom;
import student.StudentArrayDeque;

public class TestArrayDequeEC {
    @Test
    public void testRandom() {
        StudentArrayDeque<Integer> buggyDq = new StudentArrayDeque<>();
        ArrayDequeSolution<Integer> correctDq = new ArrayDequeSolution<>();

        String message = "";
        int testTimes = 5000;

        for (int i = 0; i < testTimes; i++) {
            int operationNumber = StdRandom.uniform(0, 4);
            Integer randVal = StdRandom.uniform(0, 100);

            if (operationNumber == 0) {
                // addFirst
                buggyDq.addFirst(randVal);
                correctDq.addFirst(randVal);
                message += "addFirst(" + randVal + ")\n";

            } else if (operationNumber == 1) {
                // addLast
                buggyDq.addLast(randVal);
                correctDq.addLast(randVal);
                message += "addLast(" + randVal + ")\n";

            } else if (operationNumber == 2) {
                // removeFirst
                if (!correctDq.isEmpty()) {
                    Integer expected = correctDq.removeFirst();
                    Integer actual = buggyDq.removeFirst();
                    message += "removeFirst()\n";
                    assertEquals(message, expected, actual);
                }
            } else if (operationNumber == 3) {
                // removeLast
                if (!correctDq.isEmpty()) {
                    Integer expected = correctDq.removeLast();
                    Integer actual = buggyDq.removeLast();
                    message += "removeLast()\n";

                    assertEquals(message, expected, actual);
                }
            }
        }
    }
}
