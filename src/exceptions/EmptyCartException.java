package exceptions;

/**
 * Checked exception: checkout was attempted with no items.
 */
public class EmptyCartException extends Exception {

    public EmptyCartException(String message) {
        super(message);
    }
}
