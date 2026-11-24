package service;

import exceptions.InvalidInputException;
import exceptions.InvalidLoginException;
import model.Customer;
import model.User;
import store.DataStore;
import utility.InputValidator;

/**
 * Registration and login against the in-memory user map.
 * InvalidLoginException is thrown (throw) and declared (throws) so Main must catch it.
 */
public class AuthService {

    private final DataStore store = DataStore.getInstance();

    public Customer register(String username, String password, String fullName, String email, String phone)
            throws InvalidLoginException {
        InputValidator.requireNonEmpty(fullName, "Full name");
        if (!InputValidator.isValidUsername(username)) {
            throw new InvalidInputException(
                    "Username must start with a letter and be 3-20 letters, digits or underscore.");
        }
        if (!InputValidator.isValidPassword(password)) {
            throw new InvalidInputException("Password must be at least 4 characters.");
        }
        if (!InputValidator.isValidEmail(email)) {
            throw new InvalidInputException("Enter a valid email (must contain @ and .).");
        }
        if (!InputValidator.isValidPhone(phone)) {
            throw new InvalidInputException("Phone must be exactly 10 digits.");
        }
        if (store.usernameExists(username)) {
            throw new InvalidLoginException("Username already taken. Choose another.");
        }
        Customer customer = new Customer(store.nextUserId(), username, password,
                fullName.trim(), email.trim(), phone.trim());
        store.saveUser(customer);
        return customer;
    }

    public User login(String username, String password) throws InvalidLoginException {
        InputValidator.requireNonEmpty(username, "Username");
        InputValidator.requireNonEmpty(password, "Password");
        User user = store.findUser(username.trim());
        if (user == null) {
            throw new InvalidLoginException("No account found for that username.");
        }
        if (!user.getPassword().equals(password)) {
            throw new InvalidLoginException("Incorrect password.");
        }
        return user;
    }

    public User loginAsAdmin(String username, String password) throws InvalidLoginException {
        User user = login(username, password); // exception propagates if login fails
        if (!"ADMIN".equals(user.getRole())) {
            throw new InvalidLoginException("This account is not an administrator.");
        }
        return user;
    }
}
