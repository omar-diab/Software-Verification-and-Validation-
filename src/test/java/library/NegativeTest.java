package library;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Negative testing.
 * Invalid input and illegal operations must be rejected,
 * and a rejection must leave the stored data as it was.
 */
class NegativeTest {

    private LendingService service;
    private LocalDate day;

    @BeforeEach
    void setUp() {
        service = new LendingService();
        day = LocalDate.of(2026, 11, 1);
        service.registerMember("M0001", "Sara");
        service.registerMember("M0002", "Ali");
        service.addItem("I0001", "Drill", "Tools", "M0001");
    }

    @Test
    void emptyAndNullMemberDetailsAreRejected() {
        assertThrows(LibraryException.class, () -> service.registerMember("", "Sara"));
        assertThrows(LibraryException.class, () -> service.registerMember(null, "Sara"));
        assertThrows(LibraryException.class, () -> service.registerMember("M0003", ""));
        assertThrows(LibraryException.class, () -> service.registerMember("M0003", "   "));
        assertThrows(LibraryException.class, () -> service.registerMember("M0003", null));

        assertEquals(2, service.getMemberCount());
        assertNull(service.findMember("M0003"));
    }

    @Test
    void emptyItemDetailsAreRejected() {
        assertThrows(LibraryException.class,
                () -> service.addItem("", "Saw", "Tools", "M0001"));
        assertThrows(LibraryException.class,
                () -> service.addItem(null, "Saw", "Tools", "M0001"));
        assertThrows(LibraryException.class,
                () -> service.addItem("I0002", "", "Tools", "M0001"));
        assertThrows(LibraryException.class,
                () -> service.addItem("I0002", "Saw", "", "M0001"));
        assertThrows(LibraryException.class,
                () -> service.addItem("I0002", "Saw", null, "M0001"));

        assertEquals(1, service.getItemCount());
    }

    @Test
    void duplicateIdsAreRejected() {
        assertThrows(LibraryException.class, () -> service.registerMember("M0001", "Copy"));
        assertThrows(LibraryException.class,
                () -> service.addItem("I0001", "Other drill", "Tools", "M0001"));

        assertEquals(2, service.getMemberCount());
        assertEquals(1, service.getItemCount());
        assertEquals("Sara", service.findMember("M0001").getName());
        assertEquals("Drill", service.findItem("I0001").getName());
    }

    @Test
    void unknownMemberAndUnknownItemCannotBorrow() {
        assertThrows(LibraryException.class,
                () -> service.borrowItem("M0099", "I0001", day, 3));
        assertThrows(LibraryException.class,
                () -> service.borrowItem("M0002", "I0099", day, 3));
        assertThrows(LibraryException.class,
                () -> service.borrowItem("M0002", "I0001", null, 3));

        assertEquals(0, service.getLoanCount());
        assertTrue(service.findItem("I0001").isAvailable());
        assertEquals(0, service.findMember("M0002").getBorrowedItemCount());
    }

    @Test
    void invalidLoanLengthDoesNotCreateALoan() {
        assertThrows(LibraryException.class,
                () -> service.borrowItem("M0002", "I0001", day, 0));
        assertThrows(LibraryException.class,
                () -> service.borrowItem("M0002", "I0001", day, 15));

        assertEquals(0, service.getLoanCount());
        assertTrue(service.findItem("I0001").isAvailable());
    }

    @Test
    void borrowingOwnItemOrUnavailableItemChangesNothing() {
        assertThrows(LibraryException.class,
                () -> service.borrowItem("M0001", "I0001", day, 5));

        service.borrowItem("M0002", "I0001", day, 5);
        int loans = service.getLoanCount();

        assertThrows(LibraryException.class,
                () -> service.borrowItem("M0002", "I0001", day, 5));

        assertEquals(loans, service.getLoanCount());
        assertEquals(1, service.findMember("M0002").getBorrowedItemCount());
        assertFalse(service.findItem("I0001").isAvailable());
    }

    @Test
    void returningTwiceDoesNotChangeTheResultOfTheFirstReturn() {
        service.borrowItem("M0002", "I0001", day, 7);
        int fine = service.returnItem("I0001", LocalDate.of(2026, 11, 10));

        assertEquals(10, fine);

        assertThrows(LibraryException.class,
                () -> service.returnItem("I0001", LocalDate.of(2026, 11, 12)));

        assertTrue(service.findItem("I0001").isAvailable());
        assertEquals(0, service.findMember("M0002").getBorrowedItemCount());
        assertNull(service.findActiveLoan("I0001"));
        assertEquals(1, service.getLoanCount());
    }

    @Test
    void returnBeforeBorrowDateChangesNothing() {
        service.borrowItem("M0002", "I0001", day, 7);

        assertThrows(LibraryException.class,
                () -> service.returnItem("I0001", LocalDate.of(2026, 10, 31)));

        assertFalse(service.findItem("I0001").isAvailable());
        assertEquals(1, service.findMember("M0002").getBorrowedItemCount());
        assertFalse(service.findActiveLoan("I0001").isReturned());
    }

    @Test
    void badlyFormattedIdsAreRejectedWhenBorrowingAndReturning() {
        assertThrows(LibraryException.class,
                () -> service.borrowItem("M12", "I0001", day, 3));
        assertThrows(LibraryException.class,
                () -> service.borrowItem("M0002", "X1", day, 3));

        service.borrowItem("M0002", "I0001", day, 3);
        assertThrows(LibraryException.class,
                () -> service.returnItem("abc", day));

        // Only the one good loan exists, and it is still active.
        assertEquals(1, service.getLoanCount());
        assertFalse(service.findItem("I0001").isAvailable());
        assertEquals(1, service.findMember("M0002").getBorrowedItemCount());
    }

    @Test
    void missingReturnDateChangesNothing() {
        service.borrowItem("M0002", "I0001", day, 3);

        assertThrows(LibraryException.class,
                () -> service.returnItem("I0001", null));

        assertFalse(service.findItem("I0001").isAvailable());
        assertFalse(service.findActiveLoan("I0001").isReturned());
    }

    @Test
    void nullItemNameIsRejected() {
        assertThrows(LibraryException.class,
                () -> service.addItem("I0002", null, "Tools", "M0001"));
        assertNull(service.findItem("I0002"));
    }

    @Test
    void unknownItemCannotBeReturned() {
        assertThrows(LibraryException.class,
                () -> service.returnItem("I0099", day));
        assertThrows(LibraryException.class,
                () -> service.returnItem("I0001", day));

        assertEquals(0, service.getLoanCount());
    }
}
