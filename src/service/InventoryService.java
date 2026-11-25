package service;

import exceptions.InsufficientStockException;
import model.CartItem;
import model.Product;
import store.DataStore;
import utility.AppLog;

import java.util.List;

/**
 * Shared stock updates.
 * synchronized method + synchronized block (intrinsic lock) stop two checkouts
 * from selling the same last unit (race condition on the critical section).
 */
public class InventoryService {

    private final DataStore store = DataStore.getInstance();

    public void deductStock(List<CartItem> cartItems) throws InsufficientStockException {
        synchronized (this) { // synchronized method equivalent via block on this
            for (CartItem item : cartItems) {
                if (!decrease(item.getProduct().getProductId(), item.getQuantity())) {
                    throw new InsufficientStockException(
                            "Stock changed before checkout. Not enough "
                                    + item.getProduct().getName() + " left.");
                }
            }
        }
    }

    public boolean tryDeductOne(String productId) {
        synchronized (store.getInventoryLock()) { // different intrinsic lock
            return decrease(productId, 1);
        }
    }

    private boolean decrease(String productId, int quantity) {
        Product product = store.getProductsById().get(productId);
        if (product == null) {
            return false;
        }
        int stock = product.getStock();
        if (stock < quantity) {
            AppLog.info("Reject deduct " + quantity + " of " + productId + " (stock=" + stock + ")");
            return false;
        }
        product.setStock(stock - quantity); // single write: atomic enough under the lock
        AppLog.info("Deducted " + quantity + " of " + productId + ", left " + product.getStock());
        return true;
    }

    public int peekStock(String productId) {
        Product product = store.getProductsById().get(productId);
        return product == null ? 0 : product.getStock();
    }
}
