package exceptions;

/**
 * Unchecked exception for invalid console input (empty text, negative numbers).
 */
public class InvalidInputException extends RuntimeException {

    public InvalidInputException(String message) {
        super(message);
    }
}
