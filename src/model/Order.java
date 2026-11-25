package model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/** A completed purchase kept in memory. */
public class Order implements Reportable {

    private String orderId;
    private int userId;
    private double totalAmount;
    private Date orderDate;
    private String status;
    private List<CartItem> items;

    public Order() {
        this.items = new ArrayList<CartItem>();
        this.status = "COMPLETED";
    }

    public Order(String orderId, int userId, double totalAmount, Date orderDate, String status) {
        this();
        this.orderId = orderId;
        this.userId = userId;
        this.totalAmount = totalAmount;
        this.orderDate = orderDate;
        this.status = status;
    }

    @Override
    public String generateSummary() {
        return orderId + " | " + String.format("Rs. %.2f", totalAmount) + " | " + orderDate;
    }

    @Override
    public String toString() {
        return generateSummary();
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public double getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(double totalAmount) {
        this.totalAmount = totalAmount;
    }

    public Date getOrderDate() {
        return orderDate;
    }

    public void setOrderDate(Date orderDate) {
        this.orderDate = orderDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public List<CartItem> getItems() {
        return items;
    }

    public void setItems(List<CartItem> items) {
        this.items = items;
    }
}
