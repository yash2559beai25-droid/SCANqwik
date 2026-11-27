package main;

import exceptions.CheckoutException;
import exceptions.EmptyCartException;
import exceptions.InsufficientStockException;
import exceptions.InvalidInputException;
import exceptions.InvalidLoginException;
import exceptions.ProductNotFoundException;
import model.Admin;
import model.CartItem;
import model.Customer;
import model.Order;
import model.Product;
import model.User;
import service.AuthService;
import service.CartService;
import service.InventoryService;
import service.OrderService;
import service.ProductService;
import thread.ConcurrentCheckoutSimulator;
import thread.InventoryWatchdog;
import utility.ConsoleUI;
import utility.InputValidator;
import utility.StringTools;

import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.TreeMap;

/**
 * SCANqwik console entry point. Core Java only — no JDBC, no SQL.
 */
public class Main {

    private final Scanner scanner = new Scanner(System.in);
    private final AuthService authService = new AuthService();
    private final ProductService productService = new ProductService();
    private final InventoryService inventoryService = new InventoryService();
    private final OrderService orderService = new OrderService(inventoryService);
    private CartService cartService;
    private InventoryWatchdog watchdog;

    public static void main(String[] args) {
        new Main().run();
    }

    private void run() {
        watchdog = new InventoryWatchdog(inventoryService, "8901010");
        watchdog.start();
        try {
            while (true) {
                try {
                    ConsoleUI.welcomeHome();
                    System.out.println("1. Customer Login");
                    System.out.println("2. Customer Registration");
                    System.out.println("3. Admin Login");
                    System.out.println("4. Exit");
                    System.out.print("Enter choice: ");
                    int choice = InputValidator.parseMenuChoice(scanner.nextLine(), 1, 4);
                    switch (choice) {
                        case 1:
                            customerLogin();
                            break;
                        case 2:
                            registerCustomer();
                            break;
                        case 3:
                            adminLogin();
                            break;
                        case 4:
                            System.out.println("Thank you for using SCANqwik.");
                            return;
                        default:
                            break;
                    }
                } catch (InvalidInputException e) {
                    System.out.println("Input error: " + e.getMessage());
                } catch (Exception e) {
                    System.out.println("Unexpected error: " + e.getMessage());
                }
            }
        } finally {
            stopWatchdog();
            scanner.close();
        }
    }

    private void stopWatchdog() {
        if (watchdog != null) {
            watchdog.interrupt();
        }
    }

    private void customerLogin() {
        ConsoleUI.banner("CUSTOMER LOGIN");
        System.out.print("Username: ");
        String username = scanner.nextLine();
        System.out.print("Password: ");
        String password = scanner.nextLine();
        try {
            User user = authService.login(username, password);
            if (user instanceof Admin) {
                System.out.println("This is an admin account. Use Admin Login.");
                return;
            }
            user.displayWelcome();
            cartService = new CartService();
            customerDashboard((Customer) user);
        } catch (InvalidLoginException e) {
            System.out.println(e.getMessage());
        }
    }

    private void adminLogin() {
        ConsoleUI.banner("ADMIN LOGIN");
        System.out.print("Username: ");
        String username = scanner.nextLine();
        System.out.print("Password: ");
        String password = scanner.nextLine();
        try {
            User user = authService.loginAsAdmin(username, password);
            user.displayWelcome();
            adminDashboard((Admin) user);
        } catch (InvalidLoginException e) {
            System.out.println(e.getMessage());
        }
    }

    private void registerCustomer() {
        ConsoleUI.banner("CUSTOMER REGISTRATION");
        try {
            System.out.print("Full name: ");
            String name = scanner.nextLine();
            System.out.print("Username: ");
            String username = scanner.nextLine();
            System.out.print("Password: ");
            String password = scanner.nextLine();
            System.out.print("Email: ");
            String email = scanner.nextLine();
            System.out.print("Phone (10 digits): ");
            String phone = scanner.nextLine();
            Customer created = authService.register(username, password, name, email, phone);
            System.out.println("Registered successfully. Your user ID is " + created.getUserId());
            System.out.println("You can now log in from the main menu.");
        } catch (InvalidLoginException e) {
            System.out.println(e.getMessage());
        } catch (InvalidInputException e) {
            System.out.println(e.getMessage());
        }
    }

    private void customerDashboard(Customer customer) {
        boolean loggedIn = true;
        while (loggedIn) {
            try {
                ConsoleUI.banner("CUSTOMER DASHBOARD");
                System.out.println("1. View All Products");
                System.out.println("2. Search Product");
                System.out.println("3. Scan Product (Enter Product ID)");
                System.out.println("4. View Shopping Cart");
                System.out.println("5. Remove / Update Item in Cart");
                System.out.println("6. Checkout");
                System.out.println("7. Order History");
                System.out.println("8. Logout");
                System.out.print("Enter choice: ");
                int choice = InputValidator.parseMenuChoice(scanner.nextLine(), 1, 8);
                switch (choice) {
                    case 1:
                        viewProducts();
                        break;
                    case 2:
                        searchProducts();
                        break;
                    case 3:
                        scanAndAdd();
                        break;
                    case 4:
                        viewCart();
                        break;
                    case 5:
                        editCart();
                        break;
                    case 6:
                        checkout(customer);
                        break;
                    case 7:
                        showHistory(customer.getUserId());
                        break;
                    case 8:
                        loggedIn = false;
                        cartService = null;
                        System.out.println("Logged out.");
                        break;
                    default:
                        break;
                }
            } catch (InvalidInputException e) {
                System.out.println(e.getMessage());
            } catch (ProductNotFoundException e) {
                System.out.println(e.getMessage());
            } catch (InsufficientStockException e) {
                System.out.println(e.getMessage());
            } catch (EmptyCartException e) {
                System.out.println(e.getMessage());
            } catch (CheckoutException e) {
                System.out.println(e.getMessage());
            }
        }
    }

    private void adminDashboard(Admin admin) {
        boolean loggedIn = true;
        while (loggedIn) {
            try {
                ConsoleUI.banner("ADMIN DASHBOARD");
                System.out.println("1. View All Products");
                System.out.println("2. Add Product");
                System.out.println("3. Update Product");
                System.out.println("4. Delete Product");
                System.out.println("5. View Customer Orders");
                System.out.println("6. View Sales Summary");
                System.out.println("7. Simulate Concurrent Checkout");
                System.out.println("8. Logout");
                System.out.print("Enter choice: ");
                int choice = InputValidator.parseMenuChoice(scanner.nextLine(), 1, 8);
                switch (choice) {
                    case 1:
                        viewProductsSorted();
                        break;
                    case 2:
                        addProduct();
                        break;
                    case 3:
                        updateProduct();
                        break;
                    case 4:
                        deleteProduct();
                        break;
                    case 5:
                        viewAllOrders();
                        break;
                    case 6:
                        viewSales();
                        break;
                    case 7:
                        runConcurrencyDemo();
                        break;
                    case 8:
                        loggedIn = false;
                        System.out.println("Admin logged out.");
                        break;
                    default:
                        break;
                }
            } catch (InvalidInputException e) {
                System.out.println(e.getMessage());
            } catch (ProductNotFoundException e) {
                System.out.println(e.getMessage());
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                System.out.println("Demo interrupted.");
            }
        }
    }

    private void viewProducts() {
        List<Product> products = productService.getAllProducts();
        ConsoleUI.banner("PRODUCT CATALOG (by name)");
        System.out.println("Categories: " + productService.getCategories());
        ConsoleUI.productTableHeader();
        for (Product product : products) {
            System.out.println(product);
        }
        if (!productService.getRecentlyScanned().isEmpty()) {
            System.out.println("Recently scanned IDs: " + productService.getRecentlyScanned());
        }
    }

    private void viewProductsSorted() {
        System.out.println("1. Sort by name   2. Sort by price   3. Sort by ID");
        System.out.print("Choice: ");
        int mode = InputValidator.parseMenuChoice(scanner.nextLine(), 1, 3);
        if (mode == 3) {
            TreeMap<String, Product> map = productService.getCatalogSortedById();
            ConsoleUI.productTableHeader();
            for (Map.Entry<String, Product> entry : map.entrySet()) {
                System.out.println(entry.getValue());
            }
            return;
        }
        List<Product> products = mode == 2
                ? productService.getAllSortedByPrice()
                : productService.getAllProducts();
        ConsoleUI.productTableHeader();
        for (Product product : products) {
            System.out.println(product);
        }
    }

    private void searchProducts() throws ProductNotFoundException {
        System.out.println("1. Search by Product ID");
        System.out.println("2. Search by Name");
        System.out.print("Choice: ");
        int mode = InputValidator.parseMenuChoice(scanner.nextLine(), 1, 2);
        System.out.print("Enter search text: ");
        String query = scanner.nextLine();
        List<Product> found = productService.search(query, mode);
        ConsoleUI.productTableHeader();
        for (Product product : found) {
            System.out.println(product);
            System.out.println("  " + product.generateSummary());
        }
    }

    private void scanAndAdd() throws ProductNotFoundException, InsufficientStockException {
        System.out.print("Enter Product ID / barcode: ");
        String id = scanner.nextLine();
        Product product = productService.searchById(id);
        System.out.println("Found: " + StringTools.displayName(product.getName())
                + " | " + StringTools.money(product.getPrice())
                + " | Stock: " + product.getStock());
        System.out.print("Quantity: ");
        int qty = InputValidator.parsePositiveInt(scanner.nextLine(), "Quantity");
        cartService.addProduct(product, qty);
        System.out.println("Added to cart.");
    }

    private void viewCart() throws EmptyCartException {
        cartService.requireNotEmpty();
        printCart();
    }

    private void printCart() {
        ConsoleUI.banner("SHOPPING CART");
        System.out.printf("%-10s %-22s %4s %8s %10s%n", "ID", "NAME", "QTY", "PRICE", "AMOUNT");
        System.out.println("------------------------------------------------------------------");
        for (CartItem item : cartService.getItems()) {
            System.out.printf("%-10s %-22s %4d %8.2f %10.2f%n",
                    item.getProduct().getProductId(),
                    item.getProduct().getName(),
                    item.getQuantity(),
                    item.getProduct().getPrice(),
                    item.getLineTotal());
        }
        System.out.println("------------------------------------------------------------------");
        System.out.printf("Subtotal: %s%n", StringTools.money(cartService.getSubtotal()));
    }

    private void editCart() throws EmptyCartException, ProductNotFoundException, InsufficientStockException {
        cartService.requireNotEmpty();
        printCart();
        System.out.println("1. Remove item");
        System.out.println("2. Change quantity");
        System.out.print("Choice: ");
        int choice = InputValidator.parseMenuChoice(scanner.nextLine(), 1, 2);
        System.out.print("Product ID: ");
        String id = scanner.nextLine().trim();
        if (choice == 1) {
            cartService.removeProduct(id);
            System.out.println("Item removed.");
        } else {
            System.out.print("New quantity (0 removes the item): ");
            int qty = InputValidator.parseMenuChoice(scanner.nextLine(), 0, 9999);
            cartService.updateQuantity(id, qty);
            System.out.println("Cart updated.");
        }
    }

    private void checkout(Customer customer)
            throws EmptyCartException, InsufficientStockException, CheckoutException {
        cartService.requireNotEmpty();
        printCart();
        double subtotal = cartService.getSubtotal();
        double total = customer.applyDiscount(subtotal);
        System.out.printf("Loyalty 2%% discount applied. Payable: %s%n", StringTools.money(total));
        System.out.print("Confirm checkout? (Y/N): ");
        Boolean ok = InputValidator.parseYesNo(scanner.nextLine());
        if (!ok.booleanValue()) {
            System.out.println("Checkout cancelled.");
            return;
        }
        String bill = orderService.checkout(customer, cartService);
        System.out.println(bill);
    }

    private void showHistory(int userId) {
        List<Order> orders = orderService.getHistory(userId);
        ConsoleUI.banner("ORDER HISTORY");
        if (orders.isEmpty()) {
            System.out.println("No previous orders.");
            return;
        }
        for (Order order : orders) {
            System.out.println(order.generateSummary());
            for (CartItem item : order.getItems()) {
                System.out.println("   - " + item.getProduct().getName()
                        + " x" + item.getQuantity()
                        + " @ " + item.getProduct().getPrice());
            }
        }
    }

    private void addProduct() {
        ConsoleUI.banner("ADD PRODUCT");
        System.out.print("Product ID / barcode: ");
        String id = scanner.nextLine().trim();
        System.out.print("Name: ");
        String name = scanner.nextLine().trim();
        System.out.print("Category: ");
        String category = scanner.nextLine().trim();
        System.out.print("Price: ");
        double price = InputValidator.parsePositiveDouble(scanner.nextLine(), "Price");
        System.out.print("Stock: ");
        int stock = InputValidator.parsePositiveInt(scanner.nextLine(), "Stock");
        System.out.print("Description: ");
        String desc = scanner.nextLine().trim();
        productService.addProduct(new Product(id, name, category, price, stock, desc));
        System.out.println("Product added.");
    }

    private void updateProduct() throws ProductNotFoundException {
        System.out.print("Product ID to update: ");
        String id = scanner.nextLine().trim();
        Product existing = productService.searchById(id);
        System.out.println("Current: " + existing.generateSummary());
        System.out.print("New name [" + existing.getName() + "]: ");
        String name = scanner.nextLine().trim();
        if (!name.isEmpty()) {
            existing.setName(name);
        }
        System.out.print("New category [" + existing.getCategory() + "]: ");
        String category = scanner.nextLine().trim();
        if (!category.isEmpty()) {
            existing.setCategory(category);
        }
        System.out.print("New price [" + existing.getPrice() + "] (blank = keep): ");
        String priceText = scanner.nextLine().trim();
        if (!priceText.isEmpty()) {
            existing.setPrice(InputValidator.parsePositiveDouble(priceText, "Price"));
        }
        System.out.print("New stock [" + existing.getStock() + "] (blank = keep): ");
        String stockText = scanner.nextLine().trim();
        if (!stockText.isEmpty()) {
            existing.setStock(InputValidator.parsePositiveInt(stockText, "Stock"));
        }
        System.out.print("New description: ");
        String desc = scanner.nextLine().trim();
        if (!desc.isEmpty()) {
            existing.setDescription(desc);
        }
        productService.updateProduct(existing);
        System.out.println("Product updated.");
    }

    private void deleteProduct() throws ProductNotFoundException {
        System.out.print("Product ID to delete: ");
        String id = scanner.nextLine().trim();
        Product existing = productService.searchById(id);
        System.out.println("Delete " + existing.getName() + "? (Y/N)");
        if (!InputValidator.parseYesNo(scanner.nextLine()).booleanValue()) {
            System.out.println("Delete cancelled.");
            return;
        }
        productService.deleteProduct(id);
        System.out.println("Product deleted.");
    }

    private void viewAllOrders() {
        List<Order> orders = orderService.getAllOrders();
        ConsoleUI.banner("ALL CUSTOMER ORDERS");
        if (orders.isEmpty()) {
            System.out.println("No orders yet.");
            return;
        }
        for (Order order : orders) {
            System.out.println("User #" + order.getUserId() + " | " + order.generateSummary());
        }
        System.out.println("Recent (deque): " + orderService.getRecentOrders());
    }

    private void viewSales() {
        ConsoleUI.banner("SALES SUMMARY");
        List<Order> orders = orderService.getAllOrders();
        System.out.println("Completed orders: " + orders.size());
        System.out.println("Total sales: " + StringTools.money(orderService.getTotalSales()));
        Order top = orderService.getHighValueOrders().peek();
        if (top != null) {
            System.out.println("Highest bill (PriorityQueue): " + top.generateSummary());
        }
    }

    private void runConcurrencyDemo() throws ProductNotFoundException, InterruptedException {
        System.out.print("Product ID to test (use one with small stock, e.g. 8901009): ");
        String id = scanner.nextLine().trim();
        Product product = productService.searchById(id);
        System.out.println("Current stock: " + product.getStock());
        new ConcurrentCheckoutSimulator().simulateLastUnit(inventoryService, id);
        System.out.println("Stock after demo: " + productService.searchById(id).getStock());
    }
}
