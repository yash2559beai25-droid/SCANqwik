package service;

import exceptions.EmptyCartException;
import exceptions.InsufficientStockException;
import exceptions.ProductNotFoundException;
import model.CartItem;
import model.Product;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

/**
 * Cart uses LinkedList (easy insert/remove) plus LinkedHashMap for ID lookup.
 * ListIterator walks the cart forward or backward when printing.
 */
public class CartService {

    private final LinkedList<CartItem> lines = new LinkedList<CartItem>();
    private final LinkedHashMap<String, CartItem> byId = new LinkedHashMap<String, CartItem>();

    public void addProduct(Product product, int quantity) throws InsufficientStockException {
        if (quantity <= 0) {
            throw new InsufficientStockException("Quantity must be at least 1.");
        }
        CartItem existing = byId.get(product.getProductId());
        int alreadyInCart = existing == null ? 0 : existing.getQuantity();
        int wanted = alreadyInCart + quantity;
        if (wanted > product.getStock()) {
            throw new InsufficientStockException(
                    "Only " + product.getStock() + " units of " + product.getName()
                            + " are in stock. You already have " + alreadyInCart + " in the cart.");
        }
        if (existing == null) {
            CartItem item = new CartItem(product, quantity);
            lines.add(item);
            byId.put(product.getProductId(), item);
        } else {
            existing.setQuantity(wanted);
        }
    }

    public void updateQuantity(String productId, int quantity)
            throws ProductNotFoundException, InsufficientStockException {
        CartItem item = byId.get(productId);
        if (item == null) {
            throw new ProductNotFoundException("That product is not in your cart.");
        }
        if (quantity <= 0) {
            removeProduct(productId);
            return;
        }
        if (quantity > item.getProduct().getStock()) {
            throw new InsufficientStockException(
                    "Only " + item.getProduct().getStock() + " units available.");
        }
        item.setQuantity(quantity);
    }

    public void removeProduct(String productId) throws ProductNotFoundException {
        CartItem item = byId.remove(productId);
        if (item == null) {
            throw new ProductNotFoundException("That product is not in your cart.");
        }
        lines.remove(item);
    }

    public List<CartItem> getItems() {
        return new ArrayList<CartItem>(lines);
    }

    /** Print helper: walk from last scanned item back to the first. */
    public List<CartItem> getItemsNewestFirst() {
        List<CartItem> reverse = new ArrayList<CartItem>();
        ListIterator<CartItem> it = lines.listIterator(lines.size());
        while (it.hasPrevious()) {
            reverse.add(it.previous());
        }
        return reverse;
    }

    public double getSubtotal() {
        double sum = 0;
        ListIterator<CartItem> it = lines.listIterator();
        while (it.hasNext()) {
            sum += it.next().getLineTotal();
        }
        return sum;
    }

    public boolean isEmpty() {
        return lines.isEmpty();
    }

    public void requireNotEmpty() throws EmptyCartException {
        if (lines.isEmpty()) {
            throw new EmptyCartException("Your cart is empty. Scan a product first.");
        }
    }

    public void clear() {
        lines.clear();
        byId.clear();
    }
}
