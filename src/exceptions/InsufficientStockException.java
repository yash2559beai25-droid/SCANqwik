package exceptions;

/**
 * Checked exception: requested quantity is more than stock on the shelf.
 */
public class InsufficientStockException extends Exception {

    public InsufficientStockException(String message) {
        super(message);
    }
}
