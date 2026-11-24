package com.ecommerce;

import java.util.Locale;

/**
 * Product - an item that customers can buy from the online store.
 *
 * Every product has a fixed ID, a name, a category, a price and a stock
 * quantity. All fields are private, and every change goes through a method
 * that validates the new value, so a Product can never hold invalid data.
 *
 * @author Elyas Ghwiet
 * @version 1.0
 */
public class Product {

    private final String productID;
    private String name;
    private String category;
    private double price;
    private int stockQuantity;

    /**
     * Creates a product after validating every value.
     *
     * @param productID     unique ID of the product, for example "P101"
     * @param name          display name of the product
     * @param category      category of the product, for example "Electronics"
     * @param price         price of one unit; must be greater than zero
     * @param stockQuantity number of units in stock; cannot be negative
     * @throws IllegalArgumentException if any value is not valid
     */
    public Product(String productID, String name, String category,
            double price, int stockQuantity) {
        this.productID = requireText(productID, "Product ID");
        this.name = requireText(name, "Product name");
        this.category = requireText(category, "Category");
        this.price = requireValidPrice(price);
        this.stockQuantity = requireValidStock(stockQuantity);
    }

    // ------------------------------------------------------------------
    // Getters and setters
    // ------------------------------------------------------------------

    public String getProductID() {
        return productID;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = requireText(name, "Product name");
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = requireText(category, "Category");
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = requireValidPrice(price);
    }

    public int getStockQuantity() {
        return stockQuantity;
    }

    // ------------------------------------------------------------------
    // Stock operations
    // ------------------------------------------------------------------

    /** Returns true if at least one unit of the product is available. */
    public boolean isInStock() {
        return stockQuantity > 0;
    }

    /**
     * Adds units to the stock. It is used when the store receives new items
     * and when an order is cancelled.
     *
     * @throws IllegalArgumentException if the quantity is less than 1
     */
    public void addStock(int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException(
                    "The quantity to add must be at least 1.");
        }
        stockQuantity += quantity;
    }

    /**
     * Removes units from the stock. This method has no access modifier, so it
     * is package-private: only classes of com.ecommerce (such as Customer)
     * can reduce the stock. Code outside the package cannot call it.
     *
     * @throws IllegalArgumentException if the quantity is less than 1 or is
     *                                  larger than the stock
     */
    void reduceStock(int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException(
                    "The quantity to remove must be at least 1.");
        }
        if (quantity > stockQuantity) {
            throw new IllegalArgumentException(
                    "Not enough stock for " + name + ".");
        }
        stockQuantity -= quantity;
    }

    // ------------------------------------------------------------------
    // Object methods
    // ------------------------------------------------------------------

    /** Two products are equal when they have the same product ID. */
    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (other == null || getClass() != other.getClass()) {
            return false;
        }
        return productID.equals(((Product) other).productID);
    }

    @Override
    public int hashCode() {
        return productID.hashCode();
    }

    @Override
    public String toString() {
        return String.format(Locale.US, "%s - %s ($%.2f, %d in stock)",
                productID, name, price, stockQuantity);
    }

    // ------------------------------------------------------------------
    // Validation helpers
    // ------------------------------------------------------------------

    private static String requireText(String value, String field) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(field + " cannot be empty.");
        }
        return value.trim();
    }

    private static double requireValidPrice(double price) {
        if (Double.isNaN(price) || Double.isInfinite(price) || price <= 0) {
            throw new IllegalArgumentException(
                    "Price must be greater than zero.");
        }
        return Math.round(price * 100.0) / 100.0;
    }

    private static int requireValidStock(int stockQuantity) {
        if (stockQuantity < 0) {
            throw new IllegalArgumentException(
                    "Stock quantity cannot be negative.");
        }
        return stockQuantity;
    }
}
