package library;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Decision table for borrowing (Y = condition is true, N = false, - = not checked).
 *
 * Conditions:
 * C1 member exists
 * C2 item exists
 * C3 loan length is 1 to 14 days
 * C4 member is NOT the owner
 * C5 item is available
 * C6 member has fewer than 2 borrowed items
 *
 * Rule | C1 C2 C3 C4 C5 C6 | Result                 | Test
 * R1   | Y  Y  Y  Y  Y  Y  | loan created           | allConditionsPass
 * R2   | N  -  -  -  -  -  | "Member was not found" | missingMemberIsRejected
 * R3   | Y  N  -  -  -  -  | "Item was not found"   | missingItemIsRejected
 * R4   | Y  Y  N  -  -  -  | "1 to 14 days"         | severalFailuresReportTheFirstOneAndChangeNothing
 * R5   | Y  Y  Y  N  -  -  | "own item"             | ownerCannotBorrowOwnItem, ownItemBeatsUnavailable...
 * R6   | Y  Y  Y  Y  N  -  | "not available"        | unavailableItemIsRejected, unavailableBeatsLimit...
 * R7   | Y  Y  Y  Y  Y  N  | "at most 2"            | memberAtTheLimitIsRejected
 *
 * The service checks C1, C2, C3, C4, C5, C6 in this order.
 * When several conditions are false, only the first false one is reported,
 * so "-" means the value does not change the result. That is why 7 rules
 * are enough instead of all 2^6 = 64 combinations.
 * A rejected request never changes member, item, or loan data.
 */
class BorrowDecisionTableTest {

    private LendingService service;
    private LocalDate day;

    @BeforeEach
    void setUp() {
        service = new LendingService();
        day = LocalDate.of(2026, 11, 1);
        service.registerMember("M0001", "Sara");
        service.registerMember("M0002", "Ali");
        service.registerMember("M0003", "Nour");
        service.addItem("I0001", "Drill", "Tools", "M0001");
        service.addItem("I0002", "Ladder", "Tools", "M0001");
        service.addItem("I0003", "Hammer", "Tools", "M0001");
    }

    @Test
    void allConditionsPass() {
        Loan loan = service.borrowItem("M0002", "I0001", day, 7);

        assertEquals("M0002", loan.getMemberId());
        assertEquals(LocalDate.of(2026, 11, 8), loan.getDueDate());
        assertFalse(service.findItem("I0001").isAvailable());
        assertEquals(1, service.findMember("M0002").getBorrowedItemCount());
    }

    @Test
    void missingMemberIsRejected() {
        LibraryException error = assertThrows(LibraryException.class,
                () -> service.borrowItem("M0009", "I0001", day, 7));

        assertTrue(error.getMessage().contains("Member was not found"));
        assertEquals(0, service.getLoanCount());
        assertTrue(service.findItem("I0001").isAvailable());
    }

    @Test
    void missingItemIsRejected() {
        LibraryException error = assertThrows(LibraryException.class,
                () -> service.borrowItem("M0002", "I0009", day, 7));

        assertTrue(error.getMessage().contains("Item was not found"));
        assertEquals(0, service.getLoanCount());
        assertEquals(0, service.findMember("M0002").getBorrowedItemCount());
    }

    @Test
    void ownerCannotBorrowOwnItem() {
        LibraryException error = assertThrows(LibraryException.class,
                () -> service.borrowItem("M0001", "I0001", day, 7));

        assertTrue(error.getMessage().contains("own item"));
        assertEquals(0, service.findMember("M0001").getBorrowedItemCount());
        assertTrue(service.findItem("I0001").isAvailable());
    }

    @Test
    void unavailableItemIsRejected() {
        service.borrowItem("M0002", "I0001", day, 7);

        LibraryException error = assertThrows(LibraryException.class,
                () -> service.borrowItem("M0003", "I0001", day, 7));

        assertTrue(error.getMessage().contains("not available"));
        assertEquals(0, service.findMember("M0003").getBorrowedItemCount());
        assertEquals(1, service.getLoanCount());
    }

    @Test
    void memberAtTheLimitIsRejected() {
        service.borrowItem("M0002", "I0001", day, 7);
        service.borrowItem("M0002", "I0002", day, 7);

        LibraryException error = assertThrows(LibraryException.class,
                () -> service.borrowItem("M0002", "I0003", day, 7));

        assertTrue(error.getMessage().contains("at most 2"));
        assertEquals(2, service.findMember("M0002").getBorrowedItemCount());
        assertTrue(service.findItem("I0003").isAvailable());
    }

    @Test
    void severalFailuresReportTheFirstOneAndChangeNothing() {
        // Ali already holds two items. The drill is also unavailable,
        // and Sara is the owner. The length 0 is checked first.
        service.borrowItem("M0002", "I0002", day, 7);
        service.borrowItem("M0002", "I0003", day, 7);
        service.borrowItem("M0003", "I0001", day, 7);

        int loansBefore = service.getLoanCount();
        LibraryException error = assertThrows(LibraryException.class,
                () -> service.borrowItem("M0002", "I0001", day, 0));

        assertTrue(error.getMessage().contains("1 to 14"));
        assertEquals(loansBefore, service.getLoanCount());
        assertEquals(2, service.findMember("M0002").getBorrowedItemCount());
        assertFalse(service.findItem("I0001").isAvailable());
    }

    @Test
    void ownItemBeatsUnavailableWhenLengthIsValid() {
        // Someone else has the drill, and the owner tries to borrow it.
        // Both "own item" and "unavailable" are true. Own item is checked first.
        service.borrowItem("M0002", "I0001", day, 7);

        LibraryException error = assertThrows(LibraryException.class,
                () -> service.borrowItem("M0001", "I0001", day, 7));

        assertTrue(error.getMessage().contains("own item"));
        assertEquals(0, service.findMember("M0001").getBorrowedItemCount());
        assertEquals(1, service.getLoanCount());
    }

    @Test
    void unavailableBeatsLimitWhenBothAreTrue() {
        // Ali holds two items (at the limit) and Nour has the hammer.
        // Ali asks for the hammer: "unavailable" is checked before "limit".
        service.borrowItem("M0002", "I0001", day, 7);
        service.borrowItem("M0002", "I0002", day, 7);
        service.borrowItem("M0003", "I0003", day, 7);

        LibraryException error = assertThrows(LibraryException.class,
                () -> service.borrowItem("M0002", "I0003", day, 7));

        assertTrue(error.getMessage().contains("not available"));
        assertEquals(3, service.getLoanCount());
        assertEquals(2, service.findMember("M0002").getBorrowedItemCount());
    }
}
