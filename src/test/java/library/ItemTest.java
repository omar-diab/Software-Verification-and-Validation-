package library;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class ItemTest {

    @Test
    void newItemIsAvailable() {
        Item item = new Item("I0001", "Drill", "Tools", "M0001");

        assertEquals("I0001", item.getId());
        assertEquals("Drill", item.getName());
        assertEquals("Tools", item.getCategory());
        assertEquals("M0001", item.getOwnerId());
        assertTrue(item.isAvailable());
    }

    @Test
    void availabilityCanChange() {
        Item item = new Item("I0001", "Drill", "Tools", "M0001");

        item.setAvailable(false);
        assertFalse(item.isAvailable());

        item.setAvailable(true);
        assertTrue(item.isAvailable());
    }
}
