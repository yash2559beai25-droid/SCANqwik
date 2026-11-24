package service;

import exceptions.CheckoutException;
import exceptions.EmptyCartException;
import exceptions.InsufficientStockException;
import model.Order;
import model.OrderAmountComparator;
import model.User;
import store.DataStore;
import thread.BillPrintTask;
import thread.OrderSaveTask;
import utility.BillGenerator;

import java.text.SimpleDateFormat;
import java.util.ArrayDeque;
import java.util.Date;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Queue;

/**
 * Checkout and history. Queue = orders being processed.
 * PriorityQueue = biggest bills first. Deque = last few completed orders.
 */
public class OrderService {

    private final DataStore store = DataStore.getInstance();
    private final InventoryService inventoryService;
    private final BillGenerator billGenerator = new BillGenerator();
    private final Queue<Order> processingQueue = new LinkedList<Order>();
    private final PriorityQueue<Order> highValueOrders = new PriorityQueue<Order>(11, new OrderAmountComparator());
    private final Deque<Order> recentOrders = new ArrayDeque<Order>();

    public OrderService(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    public String checkout(User customer, CartService cart)
            throws EmptyCartException, InsufficientStockException, CheckoutException {
        cart.requireNotEmpty();

        double subtotal = cart.getSubtotal();
        double total = customer.applyDiscount(subtotal);
        double discount = subtotal - total;

        Order order = new Order();
        order.setOrderId(generateOrderId());
        order.setUserId(customer.getUserId());
        order.setTotalAmount(total);
        order.setStatus("COMPLETED");
        order.setOrderDate(new Date());
        order.setItems(cart.getItems());

        processingQueue.add(order);
        inventoryService.deductStock(order.getItems());

        OrderSaveTask saveTask = new OrderSaveTask(store, order);
        Thread saveThread = new Thread(saveTask, "OrderSave");
        saveThread.setPriority(Thread.NORM_PRIORITY + 1);

        BillPrintTask billThread = new BillPrintTask(billGenerator, order, customer, subtotal, discount, total);
        billThread.setPriority(Thread.NORM_PRIORITY);

        saveThread.start();
        billThread.start();
        try {
            saveThread.join();
            billThread.join();
        } catch (InterruptedException e) {
            saveThread.interrupt();
            billThread.interrupt();
            Thread.currentThread().interrupt();
            throw new CheckoutException("Checkout was interrupted.");
        }

        if (!saveTask.isSaved()) {
            throw new CheckoutException("Order was not saved. Try again.");
        }

        processingQueue.poll();
        highValueOrders.offer(order);
        recentOrders.addFirst(order);
        while (recentOrders.size() > 5) {
            recentOrders.removeLast();
        }
        cart.clear();

        String bill = billThread.getBillText();
        if (bill == null) {
            bill = billGenerator.generate(order, customer, subtotal, discount, total);
        }
        return bill;
    }

    public LinkedList<Order> getHistory(int userId) {
        return store.getHistory(userId);
    }

    public List<Order> getAllOrders() {
        return store.getAllOrders();
    }

    public double getTotalSales() {
        double sum = 0;
        for (Order order : store.getAllOrders()) {
            sum += order.getTotalAmount();
        }
        return sum;
    }

    public Queue<Order> getProcessingQueue() {
        return processingQueue;
    }

    public PriorityQueue<Order> getHighValueOrders() {
        return highValueOrders;
    }

    public Deque<Order> getRecentOrders() {
        return recentOrders;
    }

    public static String generateOrderId() {
        String time = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
        int suffix = (int) (Math.random() * 9000) + 1000;
        return "SQ-" + time + "-" + suffix;
    }
}
