package thread;

import service.InventoryService;
import utility.AppLog;

/**
 * Two threads try to buy the last unit.
 * Synchronization in InventoryService means only one SOLD if stock is 1.
 * Also traces lifecycle: NEW → RUNNABLE → TERMINATED, join, interrupt, priority.
 */
public class ConcurrentCheckoutSimulator {

    public void simulateLastUnit(InventoryService inventory, String productId)
            throws InterruptedException {
        AppLog.clear();
        System.out.println("Starting two checkout threads for product " + productId + " ...");

        CheckoutWorker first = new CheckoutWorker("Checkout-A", inventory, productId, Thread.MAX_PRIORITY);
        CheckoutWorker second = new CheckoutWorker("Checkout-B", inventory, productId, Thread.NORM_PRIORITY);

        System.out.println("Before start: A is " + first.getState() + ", B is " + second.getState());
        first.start();
        second.start();
        System.out.println("After start: A is " + first.getState() + ", B is " + second.getState());

        first.join();
        second.join();
        System.out.println("After join: A is " + first.getState() + ", B is " + second.getState());
        System.out.println(first.getName() + ": " + (first.isSold() ? "SOLD 1 unit" : "REJECTED"));
        System.out.println(second.getName() + ": " + (second.isSold() ? "SOLD 1 unit" : "REJECTED"));
        System.out.println("If stock was 1, only one thread should report SOLD.");

        // interrupt demo: a third worker is started then cancelled
        CheckoutWorker extra = new CheckoutWorker("Checkout-C", inventory, productId, Thread.MIN_PRIORITY);
        extra.start();
        extra.interrupt();
        extra.join();
        System.out.println("Checkout-C interrupted; sold=" + extra.isSold()
                + " interrupted-flag=" + extra.isInterrupted());
        System.out.println("--- thread log ---");
        System.out.print(AppLog.snapshot());
    }
}
