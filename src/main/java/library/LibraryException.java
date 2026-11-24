package library;

/**
 * A simple error for rejected input or a rejected lending operation.
 * The service throws this exception and does not change its data.
 */
public class LibraryException extends RuntimeException {

    public LibraryException(String message) {
        super(message);
    }
}
