package exceptions;

/**
 * Checked exception: the typed product ID / barcode is not in the catalog.
 */
public class ProductNotFoundException extends Exception {

    public ProductNotFoundException(String message) {
        super(message);
    }
}
