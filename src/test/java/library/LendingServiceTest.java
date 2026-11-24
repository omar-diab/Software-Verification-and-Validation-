package library;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.util.ArrayList;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** Normal success paths for the five functional requirements. */
class LendingServiceTest {

    private LendingService service;

    @BeforeEach
    void setUp() {
        service = new LendingService();
        service.registerMember("M0001", "Sara");
        service.registerMember("M0002", "Ali");
        service.addItem("I0001", "Drill", "Tools", "M0001");
        service.addItem("I0002", "Ladder", "Garden", "M0001");
    }

    @Test
    void namesAreStoredWithoutExtraSpaces() {
        Member member = service.registerMember("M0003", "  Nour  ");
        Item item = service.addItem("I0003", "  Saw  ", "  Tools  ", "M0001");

        assertEquals("Nour", member.getName());
        assertEquals("Saw", item.getName());
        assertEquals("Tools", item.getCategory());
    }

    @Test
    void availableListHidesBorrowedItems() {
        service.borrowItem("M0002", "I0001", LocalDate.of(2026, 11, 1), 3);

        ArrayList<Item> available = service.getAvailableItems();

        assertEquals(1, available.size());
        assertEquals("I0002", available.get(0).getId());
        assertEquals("Ladder", available.get(0).getName());
        assertEquals("Garden", available.get(0).getCategory());
        assertEquals("M0001", available.get(0).getOwnerId());
        assertTrue(available.get(0).isAvailable());
    }

    @Test
    void returnOnTheDueDateHasNoFeeAndFreesTheItem() {
        service.borrowItem("M0002", "I0001", LocalDate.of(2026, 11, 1), 3);

        int fine = service.returnItem("I0001", LocalDate.of(2026, 11, 4));

        assertEquals(0, fine);
        assertTrue(service.findItem("I0001").isAvailable());
        assertEquals(0, service.findMember("M0002").getBorrowedItemCount());
        assertEquals(2, service.getAvailableItems().size());
    }

    @Test
    void returningOneItemKeepsTheOtherLoanActive() {
        service.borrowItem("M0002", "I0001", LocalDate.of(2026, 11, 1), 3);
        service.borrowItem("M0002", "I0002", LocalDate.of(2026, 11, 1), 3);

        service.returnItem("I0002", LocalDate.of(2026, 11, 2));

        assertTrue(service.findItem("I0002").isAvailable());
        assertFalse(service.findItem("I0001").isAvailable());
        assertFalse(service.findActiveLoan("I0001").isReturned());
        assertEquals(1, service.findMember("M0002").getBorrowedItemCount());
    }
}
