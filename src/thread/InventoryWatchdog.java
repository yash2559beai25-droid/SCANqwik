package thread;

import service.InventoryService;
import utility.AppLog;

/**
 * Daemon thread: JVM can exit even if this is still sleeping.
 * Watches stock in the background; interrupt() stops it cleanly.
 */
public class InventoryWatchdog extends Thread {

    private final InventoryService inventory;
    private final String productId;

    public InventoryWatchdog(InventoryService inventory, String productId) {
        super("InventoryWatchdog");
        setDaemon(true);
        setPriority(Thread.MIN_PRIORITY);
        this.inventory = inventory;
        this.productId = productId;
    }

    @Override
    public void run() {
        AppLog.info("Watchdog started for " + productId + " state=" + getState());
        while (!isInterrupted()) {
            try {
                sleep(2000);
                AppLog.info("Watchdog stock of " + productId + " = " + inventory.peekStock(productId));
            } catch (InterruptedException e) {
                interrupt();
                AppLog.info("Watchdog stopping");
                break;
            }
        }
    }
}
