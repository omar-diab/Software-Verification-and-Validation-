package library;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class MemberTest {

    @Test
    void newMemberStartsWithNoBorrowedItems() {
        Member member = new Member("M0001", "Sara");

        assertEquals("M0001", member.getId());
        assertEquals("Sara", member.getName());
        assertEquals(0, member.getBorrowedItemCount());
    }

    @Test
    void borrowedCountGoesUpAndDown() {
        Member member = new Member("M0001", "Sara");

        member.increaseBorrowedCount();
        member.increaseBorrowedCount();
        assertEquals(2, member.getBorrowedItemCount());

        member.decreaseBorrowedCount();
        assertEquals(1, member.getBorrowedItemCount());
    }
}
