package model;

/**
 * Admin role. No purchase discount. Welcome message is overridden.
 */
public class Admin extends User {

    public Admin() {
        super();
    }

    public Admin(int userId, String username, String password, String fullName,
                 String email, String phone) {
        super(userId, username, password, fullName, email, phone);
    }

    @Override
    public String getRole() {
        return "ADMIN";
    }

    @Override
    public void displayWelcome() {
        System.out.println("Welcome, " + getFullName() + " (Administrator)");
    }

    @Override
    public double applyDiscount(double amount) {
        return amount; // staff checkout has no loyalty discount
    }
}
