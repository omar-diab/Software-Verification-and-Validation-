package library;

/**
 * An item that a member offers to neighbours.
 * available is true when nobody is currently borrowing it.
 */
public class Item {

    private String id;
    private String name;
    private String category;
    private String ownerId;
    private boolean available;

    public Item(String id, String name, String category, String ownerId) {
        this.id = id;
        this.name = name;
        this.category = category;
        this.ownerId = ownerId;
        this.available = true;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getCategory() {
        return category;
    }

    public String getOwnerId() {
        return ownerId;
    }

    public boolean isAvailable() {
        return available;
    }

    public void setAvailable(boolean available) {
        this.available = available;
    }
}
