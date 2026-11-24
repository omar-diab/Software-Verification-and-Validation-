package library;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

/**
 * Boundary value analysis.
 * Faults often sit on the line between allowed and rejected values,
 * so these tests use the value just below, the boundary, and the value just above.
 */
class BoundaryValueTest {

    private LendingService service;
    private LocalDate borrowDate;

    @BeforeEach
    void setUp() {
        service = new LendingService();
        borrowDate = LocalDate.of(2026, 11, 1);
        service.registerMember("M0001", "Sara");
        service.registerMember("M0002", "Ali");
        service.addItem("I0001", "Drill", "Tools", "M0001");
        service.addItem("I0002", "Ladder", "Tools", "M0001");
        service.addItem("I0003", "Hammer", "Tools", "M0001");
        service.addItem("I0004", "Book", "Books", "M0001");
    }

    @Test
    void zeroDaysIsRejected() {
        assertThrows(LibraryException.class,
                () -> service.borrowItem("M0002", "I0001", borrowDate, 0));
        assertTrue(service.findItem("I0001").isAvailable());
        assertEquals(0, service.getLoanCount());
    }

    @Test
    void oneDayIsAccepted() {
        Loan loan = service.borrowItem("M0002", "I0001", borrowDate, 1);

        assertEquals(LocalDate.of(2026, 11, 2), loan.getDueDate());
        assertFalse(service.findItem("I0001").isAvailable());
    }

    @Test
    void fourteenDaysIsAccepted() {
        Loan loan = service.borrowItem("M0002", "I0001", borrowDate, 14);

        assertEquals(LocalDate.of(2026, 11, 15), loan.getDueDate());
    }

    @Test
    void fifteenDaysIsRejected() {
        assertThrows(LibraryException.class,
                () -> service.borrowItem("M0002", "I0001", borrowDate, 15));
        assertEquals(0, service.getLoanCount());
        assertTrue(service.findItem("I0001").isAvailable());
    }

    @Test
    void oneBorrowedItemIsAllowed() {
        service.borrowItem("M0002", "I0001", borrowDate, 7);

        assertEquals(1, service.findMember("M0002").getBorrowedItemCount());
    }

    @Test
    void twoBorrowedItemsAreAllowed() {
        service.borrowItem("M0002", "I0001", borrowDate, 7);
        service.borrowItem("M0002", "I0002", borrowDate, 7);

        assertEquals(2, service.findMember("M0002").getBorrowedItemCount());
        assertEquals(2, service.getLoanCount());
    }

    @Test
    void thirdBorrowedItemIsRejected() {
        service.borrowItem("M0002", "I0001", borrowDate, 7);
        service.borrowItem("M0002", "I0002", borrowDate, 7);

        assertThrows(LibraryException.class,
                () -> service.borrowItem("M0002", "I0003", borrowDate, 7));

        assertEquals(2, service.findMember("M0002").getBorrowedItemCount());
        assertTrue(service.findItem("I0003").isAvailable());
        assertEquals(2, service.getLoanCount());
    }

    @ParameterizedTest(name = "return {0} -> fine {1} TL")
    @CsvSource({
        "2026-11-07, 0",
        "2026-11-08, 0",
        "2026-11-09, 5",
        "2026-11-10, 10"
    })
    void lateFeeBoundaries(String returnText, int expectedFine) {
        // Borrowed on 1 Nov for 7 days, so the due date is 8 Nov.
        service.borrowItem("M0002", "I0001", borrowDate, 7);
        LocalDate returnDate = LocalDate.parse(returnText);

        int fine = service.returnItem("I0001", returnDate);

        assertEquals(expectedFine, fine);
        assertTrue(service.findItem("I0001").isAvailable());
        assertEquals(0, service.findMember("M0002").getBorrowedItemCount());
    }
}
