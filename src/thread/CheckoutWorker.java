package thread;

import service.InventoryService;
import utility.AppLog;

/** One competing checkout, created as a Thread subclass for the race-condition demo. */
public class CheckoutWorker extends Thread {

    private final InventoryService inventory;
    private final String productId;
    private volatile boolean sold;

    public CheckoutWorker(String name, InventoryService inventory, String productId, int priority) {
        super(name);
        setPriority(priority);
        this.inventory = inventory;
        this.productId = productId;
    }

    @Override
    public void run() {
        AppLog.info(getName() + " running, priority=" + getPriority());
        if (isInterrupted()) {
            AppLog.info(getName() + " cancelled before deduct");
            return;
        }
        sold = inventory.tryDeductOne(productId);
        AppLog.info(getName() + " " + (sold ? "SOLD" : "REJECTED"));
    }

    public boolean isSold() {
        return sold;
    }
}
