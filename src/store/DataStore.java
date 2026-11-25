package store;

import model.Admin;
import model.Customer;
import model.Order;
import model.Product;
import model.User;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * In-memory shop data for the whole program (no SQL / JDBC).
 * static singleton so every service shares the same catalog and orders.
 */
public class DataStore {

    private static final DataStore INSTANCE = new DataStore();

    private int nextUserId = 1;
    private final Map<String, User> usersByUsername = new HashMap<String, User>();
    private final Map<String, Product> productsById = new LinkedHashMap<String, Product>();
    private final List<Order> orders = new ArrayList<Order>();
    private final Map<Integer, LinkedList<Order>> ordersByUser = new HashMap<Integer, LinkedList<Order>>();
    private final Object inventoryLock = new Object();

    private DataStore() {
        seed();
    }

    public static DataStore getInstance() {
        return INSTANCE;
    }

    public Object getInventoryLock() {
        return inventoryLock;
    }

    public synchronized User findUser(String username) {
        return usersByUsername.get(username);
    }

    public synchronized boolean usernameExists(String username) {
        return usersByUsername.containsKey(username);
    }

    public synchronized int nextUserId() {
        return nextUserId++;
    }

    public synchronized void saveUser(User user) {
        usersByUsername.put(user.getUsername(), user);
    }

    public Map<String, Product> getProductsById() {
        return productsById;
    }

    public synchronized void saveOrder(Order order) {
        orders.add(order);
        LinkedList<Order> history = ordersByUser.get(Integer.valueOf(order.getUserId()));
        if (history == null) {
            history = new LinkedList<Order>();
            ordersByUser.put(Integer.valueOf(order.getUserId()), history);
        }
        history.addFirst(order); // newest first
    }

    public synchronized List<Order> getAllOrders() {
        return new ArrayList<Order>(orders);
    }

    public synchronized LinkedList<Order> getHistory(int userId) {
        LinkedList<Order> history = ordersByUser.get(Integer.valueOf(userId));
        if (history == null) {
            return new LinkedList<Order>();
        }
        return new LinkedList<Order>(history);
    }

    private void seed() {
        Admin admin = new Admin(nextUserId(), "admin", "admin123",
                "Store Administrator", "admin@scanqwik.local", "9999999999");
        saveUser(admin);

        addProduct(new Product("8901001", "Amul Milk 500ml", "Dairy", 28.00, 40, "Toned milk"));
        addProduct(new Product("8901002", "Brown Bread", "Bakery", 45.00, 25, "Whole wheat loaf"));
        addProduct(new Product("8901003", "Tata Salt 1kg", "Grocery", 28.00, 50, "Iodised salt"));
        addProduct(new Product("8901004", "Surf Excel 1kg", "Household", 210.00, 20, "Detergent powder"));
        addProduct(new Product("8901005", "Colgate 200g", "Personal Care", 95.00, 30, "Toothpaste"));
        addProduct(new Product("8901006", "Good Day Biscuits", "Snacks", 40.00, 60, "Butter cookies"));
        addProduct(new Product("8901007", "Fortune Oil 1L", "Grocery", 145.00, 18, "Sunflower oil"));
        addProduct(new Product("8901008", "Maggi Noodles", "Snacks", 14.00, 80, "Masala pack"));
        addProduct(new Product("8901009", "Rice 5kg", "Grocery", 320.00, 15, "Sonamasuri"));
        addProduct(new Product("8901010", "Mineral Water 1L", "Beverages", 20.00, 100, "Packaged drinking water"));
    }

    private void addProduct(Product product) {
        productsById.put(product.getProductId().intern(), product);
    }

    public List<User> getAllUsers() {
        return Collections.unmodifiableList(new ArrayList<User>(usersByUsername.values()));
    }
}
