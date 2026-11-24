package library;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

/**
 * Direct tests of the late-fee rule.
 * 0 days late = 0 TL, 1 day late = 5 TL, 2 days late = 10 TL.
 */
class FineCalculatorTest {

    private FineCalculator calculator = new FineCalculator();

    @ParameterizedTest(name = "due {0}, return {1} -> {2} TL")
    @CsvSource({
        "2026-11-08, 2026-11-07, 0",
        "2026-11-08, 2026-11-08, 0",
        "2026-11-08, 2026-11-09, 5",
        "2026-11-08, 2026-11-10, 10"
    })
    void fineAtTheDueDateBoundary(String dueText, String returnText, int expectedFine) {
        LocalDate dueDate = LocalDate.parse(dueText);
        LocalDate returnDate = LocalDate.parse(returnText);

        assertEquals(expectedFine, calculator.calculateFine(dueDate, returnDate));
    }

    @Test
    void nullDateIsRejected() {
        LocalDate dueDate = LocalDate.of(2026, 11, 8);

        assertThrows(LibraryException.class, () -> calculator.calculateFine(null, dueDate));
        assertThrows(LibraryException.class, () -> calculator.calculateFine(dueDate, null));
    }
}
