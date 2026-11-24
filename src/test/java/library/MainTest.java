package library;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Scanner;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Tests of the console menu.
 * Each test types a script of answers (one per line) and checks the printed text.
 */
class MainTest {

    private LendingService service;

    @BeforeEach
    void setUp() {
        service = new LendingService();
    }

    /** Runs the menu with the given input and returns everything it printed. */
    private String runMenu(String input) {
        PrintStream originalOut = System.out;
        ByteArrayOutputStream printed = new ByteArrayOutputStream();
        System.setOut(new PrintStream(printed));
        try {
            Main.run(new Scanner(input), service);
        } finally {
            System.setOut(originalOut);
        }
        return printed.toString();
    }

    @Test
    void registerAddAndDisplayAnItem() {
        String output = runMenu("1\n M0001 \nSara\n"
                + "2\nI0001\nDrill\nTools\nM0001\n"
                + "5\n6\n");

        assertTrue(output.contains("Registered Sara (M0001)."));
        assertTrue(output.contains("Added Drill (I0001)."));
        // FR5: ID, name, category, owner, and availability are shown.
        assertTrue(output.contains("I0001 | Drill | Tools | Sara (M0001) | Available"));
        assertTrue(output.contains("Goodbye."));
    }

    @Test
    void borrowAndReturnLateThroughTheMenu() {
        service.registerMember("M0001", "Sara");
        service.registerMember("M0002", "Ali");
        service.addItem("I0001", "Drill", "Tools", "M0001");

        String output = runMenu("3\nM0002\nI0001\n2026-11-01\n7\n"
                + "5\n"
                + "4\nI0001\n2026-11-10\n"
                + "6\n");

        assertTrue(output.contains("Borrowed. Due date: 2026-11-08."));
        assertTrue(output.contains("No items are available."));
        assertTrue(output.contains("Returned. Late fee: 10 TL."));
        assertTrue(service.findItem("I0001").isAvailable());
    }

    @Test
    void wrongInputShowsAMessageAndDoesNotCrash() {
        service.registerMember("M0001", "Sara");
        service.registerMember("M0002", "Ali");
        service.addItem("I0001", "Drill", "Tools", "M0001");

        String output = runMenu("9\n"
                + "3\nM0002\nI0001\n01/11/2026\n"
                + "3\nM0002\nI0001\n2026-11-01\nseven\n"
                + "3\nM0001\nI0001\n2026-11-01\n7\n"
                + "6\n");

        assertTrue(output.contains("Please enter a number from 1 to 6."));
        assertTrue(output.contains("Error: Date must be written as yyyy-MM-dd."));
        assertTrue(output.contains("Error: Please enter a whole number."));
        assertTrue(output.contains("Error: A member cannot borrow their own item."));
        assertEquals(0, service.getLoanCount());
    }

    /**
     * Defect D2 regression test.
     * When the input ended (for example Ctrl+D) before option 6,
     * scanner.nextLine() threw NoSuchElementException and the program crashed.
     */
    @Test
    void endOfInputStopsTheMenuWithoutCrashing() {
        String output = runMenu("1\nM0001\n");

        assertTrue(output.contains("Goodbye."));
        assertEquals(0, service.getMemberCount());
        assertFalse(output.contains("Registered"));
    }
}
