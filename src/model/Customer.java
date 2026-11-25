package model;

/**
 * Customer role. Overrides welcome message and applies a small loyalty discount.
 */
public class Customer extends User {

    public static final double LOYALTY_RATE = 0.02; // 2%

    public Customer() {
        super();
    }

    public Customer(int userId, String username, String password, String fullName,
                    String email, String phone) {
        super(userId, username, password, fullName, email, phone);
    }

    @Override
    public String getRole() {
        return "CUSTOMER";
    }

    @Override
    public void displayWelcome() {
        System.out.println("Welcome, " + getFullName() + " (Customer)");
    }

    @Override
    public double applyDiscount(double amount) {
        return amount - (amount * LOYALTY_RATE);
    }
}
