package com.ecommerce.orders;

import com.ecommerce.Product;

/**
 * OrderItem - one line of an order: a product, the quantity that was bought
 * and the price of one unit at the moment of the purchase.
 *
 * The unit price is copied when the item is created, so changing the price of
 * the product later does not change an order that was already placed. The
 * class is immutable: it has no setters.
 *
 * @author Elyas Ghwiet
 * @version 1.0
 */
public class OrderItem {

    private final Product product;
    private final int quantity;
    private final double unitPrice;

    /**
     * Creates an order item and records the current price of the product.
     *
     * @param product  the product that was bought
     * @param quantity how many units were bought; at least 1
     * @throws IllegalArgumentException if the product is null or the
     *                                  quantity is less than 1
     */
    public OrderItem(Product product, int quantity) {
        if (product == null) {
            throw new IllegalArgumentException(
                    "An order item needs a product.");
        }
        if (quantity <= 0) {
            throw new IllegalArgumentException(
                    "Quantity must be at least 1.");
        }
        this.product = product;
        this.quantity = quantity;
        this.unitPrice = product.getPrice();
    }

    public Product getProduct() {
        return product;
    }

    public int getQuantity() {
        return quantity;
    }

    public double getUnitPrice() {
        return unitPrice;
    }

    /** Returns the price of this line (unit price times quantity). */
    public double getSubtotal() {
        return Math.round(unitPrice * quantity * 100.0) / 100.0;
    }
}
