package library;

import java.time.LocalDate;
import java.util.ArrayList;

/**
 * The rules of the library.
 * Members, items, and loans are stored in memory. There is no database.
 * Dates are passed in by the caller so tests can choose any day.
 */
public class LendingService {

    public static final int MAX_BORROWED_ITEMS = 2;

    private ArrayList<Member> members;
    private ArrayList<Item> items;
    private ArrayList<Loan> loans;
    private FineCalculator fineCalculator;

    public LendingService() {
        members = new ArrayList<Member>();
        items = new ArrayList<Item>();
        loans = new ArrayList<Loan>();
        fineCalculator = new FineCalculator();
    }

    /** FR1: register a neighbour. */
    public Member registerMember(String memberId, String name) {
        if (!Validator.isMemberId(memberId)) {
            throw new LibraryException("Member ID must look like M0001.");
        }
        if (name == null || name.trim().isEmpty()) {
            throw new LibraryException("Member name must not be empty.");
        }
        if (findMember(memberId) != null) {
            throw new LibraryException("Member ID already exists: " + memberId);
        }

        Member member = new Member(memberId, name.trim());
        members.add(member);
        return member;
    }

    /** FR2: add an item. The owner must already be registered. */
    public Item addItem(String itemId, String name, String category, String ownerId) {
        if (!Validator.isItemId(itemId)) {
            throw new LibraryException("Item ID must look like I0001.");
        }
        if (name == null || name.trim().isEmpty()) {
            throw new LibraryException("Item name must not be empty.");
        }
        if (category == null || category.trim().isEmpty()) {
            throw new LibraryException("Category must not be empty.");
        }
        if (findItem(itemId) != null) {
            throw new LibraryException("Item ID already exists: " + itemId);
        }

        Member owner = findMember(ownerId);
        if (owner == null) {
            throw new LibraryException("Owner is not a registered member: " + ownerId);
        }

        Item item = new Item(itemId, name.trim(), category.trim(), owner.getId());
        items.add(item);
        return item;
    }

    /**
     * FR3: borrow an item.
     * Checks run in this order. The first failure stops the method,
     * so nothing is saved when the request is rejected.
     * 1. Member ID and item ID have the right format.
     * 2. The member and the item exist.
     * 3. The loan length is from 1 to 14 days.
     * 4. The member is not the owner.
     * 5. The item is available.
     * 6. The member has fewer than 2 borrowed items.
     */
    public Loan borrowItem(String memberId, String itemId, LocalDate borrowDate, int days) {
        if (!Validator.isMemberId(memberId)) {
            throw new LibraryException("Member ID must look like M0001.");
        }
        if (!Validator.isItemId(itemId)) {
            throw new LibraryException("Item ID must look like I0001.");
        }
        if (borrowDate == null) {
            throw new LibraryException("Borrow date is required.");
        }

        Member member = findMember(memberId);
        if (member == null) {
            throw new LibraryException("Member was not found: " + memberId);
        }
        Item item = findItem(itemId);
        if (item == null) {
            throw new LibraryException("Item was not found: " + itemId);
        }
        if (!Validator.isValidLoanDays(days)) {
            throw new LibraryException("Loan length must be from 1 to 14 days.");
        }
        if (item.getOwnerId().equals(memberId)) {
            throw new LibraryException("A member cannot borrow their own item.");
        }
        if (!item.isAvailable()) {
            throw new LibraryException("Item is not available: " + itemId);
        }
        if (member.getBorrowedItemCount() >= MAX_BORROWED_ITEMS) {
            throw new LibraryException("A member can borrow at most 2 items.");
        }

        Loan loan = new Loan(itemId, memberId, borrowDate, days);
        loans.add(loan);
        item.setAvailable(false);
        member.increaseBorrowedCount();
        return loan;
    }

    /**
     * FR4: return a borrowed item.
     * The method returns the late fee in TL.
     * After a successful return the item is available again.
     */
    public int returnItem(String itemId, LocalDate returnDate) {
        if (!Validator.isItemId(itemId)) {
            throw new LibraryException("Item ID must look like I0001.");
        }
        if (returnDate == null) {
            throw new LibraryException("Return date is required.");
        }

        Item item = findItem(itemId);
        if (item == null) {
            throw new LibraryException("Item was not found: " + itemId);
        }

        Loan loan = findActiveLoan(itemId);
        if (loan == null) {
            throw new LibraryException("This item is not currently borrowed.");
        }
        if (returnDate.isBefore(loan.getBorrowDate())) {
            throw new LibraryException("Return date cannot be before the borrow date.");
        }

        int fine = fineCalculator.calculateFine(loan.getDueDate(), returnDate);
        loan.setReturned(true);
        item.setAvailable(true);

        Member member = findMember(loan.getMemberId());
        member.decreaseBorrowedCount();
        return fine;
    }

    /** FR5: every item that is free to borrow. */
    public ArrayList<Item> getAvailableItems() {
        ArrayList<Item> available = new ArrayList<Item>();
        for (int i = 0; i < items.size(); i++) {
            Item item = items.get(i);
            if (item.isAvailable()) {
                available.add(item);
            }
        }
        return available;
    }

    public Member findMember(String memberId) {
        for (int i = 0; i < members.size(); i++) {
            Member member = members.get(i);
            if (member.getId().equals(memberId)) {
                return member;
            }
        }
        return null;
    }

    public Item findItem(String itemId) {
        for (int i = 0; i < items.size(); i++) {
            Item item = items.get(i);
            if (item.getId().equals(itemId)) {
                return item;
            }
        }
        return null;
    }

    public Loan findActiveLoan(String itemId) {
        for (int i = 0; i < loans.size(); i++) {
            Loan loan = loans.get(i);
            if (loan.getItemId().equals(itemId) && !loan.isReturned()) {
                return loan;
            }
        }
        return null;
    }

    public int getMemberCount() {
        return members.size();
    }

    public int getItemCount() {
        return items.size();
    }

    public int getLoanCount() {
        return loans.size();
    }
}
