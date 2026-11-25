package model;

/**
 * Interface: objects that can print a one-line summary (products, orders).
 */
public interface Reportable {

    String generateSummary();
}
