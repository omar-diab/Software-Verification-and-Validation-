package library;

/**
 * A neighbour who can lend and borrow items.
 * borrowedItemCount is how many items this member currently has.
 */
public class Member {

    private String id;
    private String name;
    private int borrowedItemCount;

    public Member(String id, String name) {
        this.id = id;
        this.name = name;
        this.borrowedItemCount = 0;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getBorrowedItemCount() {
        return borrowedItemCount;
    }

    public void increaseBorrowedCount() {
        borrowedItemCount = borrowedItemCount + 1;
    }

    public void decreaseBorrowedCount() {
        borrowedItemCount = borrowedItemCount - 1;
    }
}
