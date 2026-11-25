package exceptions;

/**
 * Checked exception: wrong username/password or unknown user.
 */
public class InvalidLoginException extends Exception {

    public InvalidLoginException(String message) {
        super(message);
    }
}
