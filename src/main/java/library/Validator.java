package library;

/**
 * Checks the format of member IDs, item IDs, and loan lengths.
 * A valid member ID looks like M0001. A valid item ID looks like I0001.
 */
public class Validator {

    public static final int MIN_LOAN_DAYS = 1;
    public static final int MAX_LOAN_DAYS = 14;

    public static boolean isMemberId(String id) {
        return hasPrefixAndFourDigits(id, 'M');
    }

    public static boolean isItemId(String id) {
        return hasPrefixAndFourDigits(id, 'I');
    }

    public static boolean isValidLoanDays(int days) {
        return days >= MIN_LOAN_DAYS && days <= MAX_LOAN_DAYS;
    }

    /**
     * True when the text is a letter followed by exactly four digits.
     * Example: M0001 is valid. M001, M00001, and m0001 are not.
     */
    private static boolean hasPrefixAndFourDigits(String id, char prefix) {
        if (id == null || id.length() != 5) {
            return false;
        }
        if (id.charAt(0) != prefix) {
            return false;
        }
        for (int i = 1; i < id.length(); i++) {
            char c = id.charAt(i);
            // Only the normal digits 0 to 9 are allowed.
            // (Character.isDigit() was not used because it also accepts
            // other digits such as Arabic-Indic digits. See defect D1.)
            if (c < '0' || c > '9') {
                return false;
            }
        }
        return true;
    }
}
