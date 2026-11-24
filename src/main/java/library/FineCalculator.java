package library;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/**
 * Late fee rule: 5 TL for each day after the due date.
 * A return on or before the due date costs 0 TL.
 */
public class FineCalculator {

    public static final int FEE_PER_DAY = 5;

    public int calculateFine(LocalDate dueDate, LocalDate returnDate) {
        if (dueDate == null || returnDate == null) {
            throw new LibraryException("Due date and return date are required.");
        }

        // On time, or early: no fee.
        if (!returnDate.isAfter(dueDate)) {
            return 0;
        }

        long lateDays = ChronoUnit.DAYS.between(dueDate, returnDate);
        return (int) lateDays * FEE_PER_DAY;
    }
}
