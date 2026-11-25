package model;

import java.util.Comparator;

/** Sort products by price (low to high). Used with Collections.sort. */
public class PriceComparator implements Comparator<Product> {

    @Override
    public int compare(Product a, Product b) {
        return Double.compare(a.getPrice(), b.getPrice());
    }
}
