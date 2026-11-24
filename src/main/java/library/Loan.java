package library;

import java.time.LocalDate;

/**
 * One borrow of one item.
 * dueDate is the borrow date plus the number of loan days.
 * returned stays false until the item is given back.
 */
public class Loan {

    private String itemId;
    private String memberId;
    private LocalDate borrowDate;
    private LocalDate dueDate;
    private boolean returned;

    public Loan(String itemId, String memberId, LocalDate borrowDate, int days) {
        this.itemId = itemId;
        this.memberId = memberId;
        this.borrowDate = borrowDate;
        this.dueDate = borrowDate.plusDays(days);
        this.returned = false;
    }

    public String getItemId() {
        return itemId;
    }

    public String getMemberId() {
        return memberId;
    }

    public LocalDate getBorrowDate() {
        return borrowDate;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public boolean isReturned() {
        return returned;
    }

    public void setReturned(boolean returned) {
        this.returned = returned;
    }
}
