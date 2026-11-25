package exceptions;

/** Checked exception: checkout thread was interrupted before the bill was ready. */
public class CheckoutException extends Exception {

    public CheckoutException(String message) {
        super(message);
    }
}
