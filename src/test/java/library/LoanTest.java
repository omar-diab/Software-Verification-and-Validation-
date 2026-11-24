package library;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import org.junit.jupiter.api.Test;

class LoanTest {

    @Test
    void dueDateIsBorrowDatePlusLoanDays() {
        LocalDate borrowDate = LocalDate.of(2026, 11, 1);
        Loan loan = new Loan("I0001", "M0002", borrowDate, 14);

        assertEquals("I0001", loan.getItemId());
        assertEquals("M0002", loan.getMemberId());
        assertEquals(borrowDate, loan.getBorrowDate());
        assertEquals(LocalDate.of(2026, 11, 15), loan.getDueDate());
        assertFalse(loan.isReturned());
    }

    @Test
    void loanCanBeMarkedReturned() {
        Loan loan = new Loan("I0001", "M0002", LocalDate.of(2026, 11, 1), 1);

        loan.setReturned(true);

        assertTrue(loan.isReturned());
    }
}
