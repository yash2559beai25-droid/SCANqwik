package model;

import java.util.Comparator;

/** Highest bill first – used by PriorityQueue in OrderService. */
public class OrderAmountComparator implements Comparator<Order> {

    @Override
    public int compare(Order a, Order b) {
        return Double.compare(b.getTotalAmount(), a.getTotalAmount());
    }
}
