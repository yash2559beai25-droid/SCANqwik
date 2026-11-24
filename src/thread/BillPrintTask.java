package thread;

import model.Order;
import model.User;
import utility.AppLog;
import utility.BillGenerator;

/**
 * Worker created by extending Thread (not only Runnable).
 */
public class BillPrintTask extends Thread {

    private final BillGenerator generator;
    private final Order order;
    private final User customer;
    private final double subtotal;
    private final double discount;
    private final double total;
    private volatile String billText;

    public BillPrintTask(BillGenerator generator, Order order, User customer,
                         double subtotal, double discount, double total) {
        super("BillPrint");
        this.generator = generator;
        this.order = order;
        this.customer = customer;
        this.subtotal = subtotal;
        this.discount = discount;
        this.total = total;
    }

    @Override
    public void run() {
        try {
            sleep(40);
        } catch (InterruptedException e) {
            interrupt();
            AppLog.info("Bill print interrupted");
            return;
        }
        billText = generator.generate(order, customer, subtotal, discount, total);
        AppLog.info("Bill ready for " + order.getOrderId());
    }

    public String getBillText() {
        return billText;
    }
}
