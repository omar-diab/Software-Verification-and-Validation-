package library;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Scanner;

/**
 * Text menu. All lending rules stay in LendingService so they can be tested.
 * Main only reads the keyboard, calls the service, and prints the result.
 */
public class Main {

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        LendingService service = new LendingService();
        run(scanner, service);
        scanner.close();
    }

    /**
     * Shows the menu until the user chooses 6.
     * It takes the Scanner as a parameter so a test can give it typed input.
     */
    public static void run(Scanner scanner, LendingService service) {
        System.out.println("Library of Things");
        boolean running = true;

        while (running) {
            printMenu();
            System.out.print("Choose an option: ");

            // No more input (for example Ctrl+D): stop instead of crashing. See defect D2.
            if (!scanner.hasNextLine()) {
                System.out.println();
                System.out.println("No more input. Goodbye.");
                break;
            }
            String choice = scanner.nextLine().trim();

            try {
                if (choice.equals("1")) {
                    registerMember(scanner, service);
                } else if (choice.equals("2")) {
                    addItem(scanner, service);
                } else if (choice.equals("3")) {
                    borrowItem(scanner, service);
                } else if (choice.equals("4")) {
                    returnItem(scanner, service);
                } else if (choice.equals("5")) {
                    showAvailableItems(service);
                } else if (choice.equals("6")) {
                    running = false;
                    System.out.println("Goodbye.");
                } else {
                    System.out.println("Please enter a number from 1 to 6.");
                }
            } catch (LibraryException ex) {
                // A rejected operation: show the reason and go back to the menu.
                System.out.println("Error: " + ex.getMessage());
            }
        }
    }

    private static void printMenu() {
        System.out.println();
        System.out.println("1. Register member");
        System.out.println("2. Add item");
        System.out.println("3. Borrow item");
        System.out.println("4. Return item");
        System.out.println("5. Display available items");
        System.out.println("6. Exit");
    }

    private static void registerMember(Scanner scanner, LendingService service) {
        String id = readLine(scanner, "Member ID (M0001): ");
        String name = readLine(scanner, "Name: ");
        Member member = service.registerMember(id, name);
        System.out.println("Registered " + member.getName() + " (" + member.getId() + ").");
    }

    private static void addItem(Scanner scanner, LendingService service) {
        String id = readLine(scanner, "Item ID (I0001): ");
        String name = readLine(scanner, "Item name: ");
        String category = readLine(scanner, "Category: ");
        String ownerId = readLine(scanner, "Owner ID: ");
        Item item = service.addItem(id, name, category, ownerId);
        System.out.println("Added " + item.getName() + " (" + item.getId() + ").");
    }

    private static void borrowItem(Scanner scanner, LendingService service) {
        String memberId = readLine(scanner, "Member ID: ");
        String itemId = readLine(scanner, "Item ID: ");
        LocalDate borrowDate = readDate(scanner, "Borrow date (yyyy-MM-dd): ");
        int days = readInt(scanner, "Loan length in days (1-14): ");
        Loan loan = service.borrowItem(memberId, itemId, borrowDate, days);
        System.out.println("Borrowed. Due date: " + loan.getDueDate() + ".");
    }

    private static void returnItem(Scanner scanner, LendingService service) {
        String itemId = readLine(scanner, "Item ID: ");
        LocalDate returnDate = readDate(scanner, "Return date (yyyy-MM-dd): ");
        int fine = service.returnItem(itemId, returnDate);
        System.out.println("Returned. Late fee: " + fine + " TL.");
    }

    /** FR5: prints ID, name, category, owner, and availability of each free item. */
    private static void showAvailableItems(LendingService service) {
        ArrayList<Item> available = service.getAvailableItems();
        if (available.size() == 0) {
            System.out.println("No items are available.");
            return;
        }

        System.out.println("ID | Name | Category | Owner | Availability");
        for (int i = 0; i < available.size(); i++) {
            Item item = available.get(i);
            Member owner = service.findMember(item.getOwnerId());
            // Every item in this list is available, so the status is always "Available".
            System.out.println(item.getId()
                    + " | " + item.getName()
                    + " | " + item.getCategory()
                    + " | " + owner.getName() + " (" + item.getOwnerId() + ")"
                    + " | Available");
        }
    }

    /** Prints the prompt and returns what the user typed, without extra spaces. */
    private static String readLine(Scanner scanner, String prompt) {
        System.out.print(prompt);
        if (!scanner.hasNextLine()) {
            throw new LibraryException("No input was entered.");
        }
        return scanner.nextLine().trim();
    }

    private static int readInt(Scanner scanner, String prompt) {
        String text = readLine(scanner, prompt);
        try {
            return Integer.parseInt(text);
        } catch (NumberFormatException ex) {
            throw new LibraryException("Please enter a whole number.");
        }
    }

    private static LocalDate readDate(Scanner scanner, String prompt) {
        String text = readLine(scanner, prompt);
        try {
            return LocalDate.parse(text);
        } catch (DateTimeParseException ex) {
            throw new LibraryException("Date must be written as yyyy-MM-dd.");
        }
    }
}
