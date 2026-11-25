package model;

/**
 * One catalog item. Natural order (Comparable) is by product name.
 */
public class Product implements Comparable<Product>, Reportable {

    private String productId;
    private String name;
    private String category;
    private double price;
    private int stock;
    private String description;

    public Product() {
    }

    public Product(String productId, String name, String category, double price,
                   int stock, String description) {
        this.productId = productId;
        this.name = name;
        this.category = category;
        this.price = price;
        this.stock = stock;
        this.description = description;
    }

    @Override
    public int compareTo(Product other) {
        return this.name.compareToIgnoreCase(other.name);
    }

    @Override
    public String generateSummary() {
        return productId + " | " + name + " | Rs." + price + " | stock: " + stock;
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public int getStock() {
        return stock;
    }

    public void setStock(int stock) {
        this.stock = stock;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public String toString() {
        return String.format("%-10s %-22s %-14s %8.2f %6d",
                productId, name, category, price, stock);
    }
}
