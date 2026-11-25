package thread;

import model.Order;
import store.DataStore;
import utility.AppLog;

/**
 * Runnable worker that stores the order in memory.
 */
public class OrderSaveTask implements Runnable {

    private final DataStore store;
    private final Order order;
    private volatile boolean saved;

    public OrderSaveTask(DataStore store, Order order) {
        this.store = store;
        this.order = order;
    }

    @Override
    public void run() {
        try {
            Thread.sleep(80);
            store.saveOrder(order);
            saved = true;
            AppLog.info("Saved order " + order.getOrderId());
        } catch (InterruptedException e) {
            AppLog.info("Order save interrupted");
            Thread.currentThread().interrupt();
        }
    }

    public boolean isSaved() {
        return saved;
    }
}
