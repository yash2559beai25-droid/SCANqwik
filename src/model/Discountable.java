package model;

/**
 * Interface: different user types can apply different checkout discounts.
 * Runtime polymorphism: call applyDiscount() on a User reference.
 */
public interface Discountable {

    double applyDiscount(double amount);
}
