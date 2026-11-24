package library;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.util.ArrayList;
import org.junit.jupiter.api.Test;

/**
 * Regression test.
 * This walks through the whole lending story: register, add, list,
 * borrow, return on time, borrow again, and return late.
 * Run the full suite with "mvn clean test" after every code change.
 * If this test fails, an earlier rule has been broken.
 */
class RegressionTest {

    @Test
    void neighboursCanLendBorrowAndReturn() {
        LendingService service = new LendingService();
        LocalDate borrowDate = LocalDate.of(2026, 11, 1);

        service.registerMember("M0001", "Sara");
        service.registerMember("M0002", "Ali");
        service.addItem("I0001", "Drill", "Tools", "M0001");
        service.addItem("I0002", "Novel", "Books", "M0001");

        ArrayList<Item> available = service.getAvailableItems();
        assertEquals(2, available.size());
        assertEquals("Drill", available.get(0).getName());
        assertEquals("Tools", available.get(0).getCategory());
        assertEquals("M0001", available.get(0).getOwnerId());

        Loan loan = service.borrowItem("M0002", "I0001", borrowDate, 7);
        assertEquals(LocalDate.of(2026, 11, 8), loan.getDueDate());
        assertEquals(1, service.getAvailableItems().size());

        int onTimeFine = service.returnItem("I0001", LocalDate.of(2026, 11, 8));
        assertEquals(0, onTimeFine);
        assertEquals(2, service.getAvailableItems().size());
        assertEquals(0, service.findMember("M0002").getBorrowedItemCount());

        service.borrowItem("M0002", "I0001", borrowDate, 7);
        int lateFine = service.returnItem("I0001", LocalDate.of(2026, 11, 9));
        assertEquals(5, lateFine);
        assertTrue(service.findItem("I0001").isAvailable());
        assertEquals(2, service.getAvailableItems().size());
    }

    /**
     * Defect D1 regression test.
     * Character.isDigit() also accepts Arabic-Indic digits, so "M١٢٣٤"
     * was accepted as a member ID. Only the digits 0 to 9 are allowed.
     */
    @Test
    void idsWithArabicIndicDigitsAreRejected() {
        assertFalse(Validator.isMemberId("M١٢٣٤"));
        assertFalse(Validator.isItemId("I٠٠٠١"));

        LendingService service = new LendingService();
        assertThrows(LibraryException.class,
                () -> service.registerMember("M١٢٣٤", "Sara"));
        assertEquals(0, service.getMemberCount());
    }
}
