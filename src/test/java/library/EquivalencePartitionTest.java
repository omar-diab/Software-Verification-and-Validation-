package library;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

/**
 * Equivalence partitioning.
 * Each input is split into a valid group and one or more invalid groups.
 * One example from each group is enough, because every value in a group
 * should be treated in the same way.
 */
class EquivalencePartitionTest {

    @ParameterizedTest(name = "member ID {0} valid={1}")
    @CsvSource({
        "M0001, true",
        "M9999, true",
        "M001, false",
        "M00001, false",
        "m0001, false",
        "X0001, false",
        "M000A, false",
        "M00-1, false"
    })
    void memberIdPartitions(String id, boolean valid) {
        assertEquals(valid, Validator.isMemberId(id));
    }

    @Test
    void nullAndEmptyMemberIdsAreInvalid() {
        assertFalse(Validator.isMemberId(null));
        assertFalse(Validator.isMemberId(""));
    }

    @ParameterizedTest(name = "item ID {0} valid={1}")
    @CsvSource({
        "I0001, true",
        "I1234, true",
        "I01, false",
        "I00001, false",
        "i0001, false",
        "M0001, false"
    })
    void itemIdPartitions(String id, boolean valid) {
        assertEquals(valid, Validator.isItemId(id));
    }

    @ParameterizedTest(name = "{0} days valid={1}")
    @CsvSource({
        "7, true",
        "1, true",
        "14, true",
        "0, false",
        "15, false",
        "-3, false"
    })
    void loanDayPartitions(int days, boolean valid) {
        assertEquals(valid, Validator.isValidLoanDays(days));
    }

    @Test
    void registeredMemberCanOwnAnItem() {
        LendingService service = new LendingService();
        service.registerMember("M0001", "Sara");

        Item item = service.addItem("I0001", "Drill", "Tools", "M0001");

        assertEquals("M0001", item.getOwnerId());
        assertEquals(1, service.getItemCount());
    }

    @Test
    void unregisteredOwnerIsRejected() {
        LendingService service = new LendingService();

        assertThrows(LibraryException.class,
                () -> service.addItem("I0001", "Drill", "Tools", "M0009"));
        assertEquals(0, service.getItemCount());
    }

    @Test
    void validMemberCanBeRegistered() {
        LendingService service = new LendingService();

        Member member = service.registerMember("M0001", "Sara");

        assertEquals("Sara", member.getName());
        assertEquals(1, service.getMemberCount());
    }

    @Test
    void invalidMemberIdCannotBeRegistered() {
        LendingService service = new LendingService();

        assertThrows(LibraryException.class, () -> service.registerMember("M12", "Sara"));
        assertEquals(0, service.getMemberCount());
    }
}
