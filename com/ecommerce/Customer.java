package com.ecommerce;

import com.ecommerce.orders.Order;
import com.ecommerce.orders.OrderException;
import com.ecommerce.orders.OrderItem;
import com.ecommerce.orders.OrderStatus;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Customer - a person who shops in the online store.
 *
 * A customer has an ID, a name, a shopping cart and a list of orders. The
 * cart is a map from each product to the quantity wanted. The customer class
 * contains the shopping rules: it checks the stock before adding a product,
 * calculates the total cost, places orders and cancels them.
 *
 * @author Elyas Ghwiet
 * @version 1.0
 */
public class Customer {

    private final String customerID;
    private String name;
    private final Map<Product, Integer> shoppingCart = new LinkedHashMap<>();
    private final List<Order> orders = new ArrayList<>();

    /**
     * Creates a customer with an empty cart and no orders.
     *
     * @param customerID unique ID of the customer, for example "C001"
     * @param name       full name of the customer
     * @throws IllegalArgumentException if the ID or the name is empty
     */
    public Customer(String customerID, String name) {
        this.customerID = requireText(customerID, "Customer ID");
        this.name = requireText(name, "Customer name");
    }

    // ------------------------------------------------------------------
    // Getters and setters
    // ------------------------------------------------------------------

    public String getCustomerID() {
        return customerID;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = requireText(name, "Customer name");
    }

    /** Returns a read-only view of the cart (product and quantity). */
    public Map<Product, Integer> getShoppingCart() {
        return Collections.unmodifiableMap(shoppingCart);
    }

    /** Returns a read-only list of the orders that the customer placed. */
    public List<Order> getOrders() {
        return Collections.unmodifiableList(orders);
    }

    // ------------------------------------------------------------------
    // Shopping cart
    // ------------------------------------------------------------------

    /** Returns true if the cart has no products. */
    public boolean isCartEmpty() {
        return shoppingCart.isEmpty();
    }

    /** Returns how many units of the product are in the cart (0 if none). */
    public int getQuantityInCart(Product product) {
        return shoppingCart.getOrDefault(product, 0);
    }

    /**
     * Adds a product to the cart. The stock is checked together with the
     * units that are already in the cart.
     *
     * @param product  the product to add
     * @param quantity how many units to add; at least 1
     * @throws IllegalArgumentException if the product is null, the quantity
     *                                  is less than 1, or the stock is not
     *                                  enough
     */
    public void addToCart(Product product, int quantity) {
        if (product == null) {
            throw new IllegalArgumentException("The product cannot be null.");
        }
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be at least 1.");
        }
        if (!product.isInStock()) {
            throw new IllegalArgumentException(
                    product.getName() + " is out of stock.");
        }

        int alreadyInCart = getQuantityInCart(product);
        int stillAvailable = product.getStockQuantity() - alreadyInCart;
        if (quantity > stillAvailable) {
            throw new IllegalArgumentException(String.format(Locale.US,
                    "Only %d more of %s can be added "
                    + "(stock: %d, in your cart: %d).",
                    stillAvailable, product.getName(),
                    product.getStockQuantity(), alreadyInCart));
        }
        shoppingCart.put(product, alreadyInCart + quantity);
    }

    /**
     * Removes units of a product from the cart. When all the units are
     * removed, the product disappears from the cart.
     *
     * @param product  the product to remove
     * @param quantity how many units to remove; at least 1
     * @throws IllegalArgumentException if the product is not in the cart, the
     *                                  quantity is less than 1, or the
     *                                  quantity is larger than the cart has
     */
    public void removeFromCart(Product product, int quantity) {
        int inCart = getQuantityInCart(product);
        if (inCart == 0) {
            throw new IllegalArgumentException(
                    "That product is not in your cart.");
        }
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be at least 1.");
        }
        if (quantity > inCart) {
            throw new IllegalArgumentException(String.format(Locale.US,
                    "You only have %d unit(s) of %s in your cart.",
                    inCart, product.getName()));
        }

        if (quantity == inCart) {
            shoppingCart.remove(product);
        } else {
            shoppingCart.put(product, inCart - quantity);
        }
    }

    /** Returns the cost of the units of one product that are in the cart. */
    public double getLineTotal(Product product) {
        return Math.round(product.getPrice() * getQuantityInCart(product)
                * 100.0) / 100.0;
    }

    /** Returns the total cost of everything in the cart. */
    public double calculateTotal() {
        double total = 0;
        for (Product product : shoppingCart.keySet()) {
            total += getLineTotal(product);
        }
        return Math.round(total * 100.0) / 100.0;
    }

    // ------------------------------------------------------------------
    // Orders
    // ------------------------------------------------------------------

    /**
     * Places an order for everything in the cart. All the checks are done
     * before anything is changed, so a failed order leaves the cart and the
     * stock exactly as they were. After a successful order the stock is
     * reduced and the cart is emptied.
     *
     * @return the new order
     * @throws OrderException if the cart is empty or the stock is not enough
     */
    public Order placeOrder() throws OrderException {
        if (shoppingCart.isEmpty()) {
            throw new OrderException(
                    "Your shopping cart is empty. Add a product first.");
        }

        for (Map.Entry<Product, Integer> line : shoppingCart.entrySet()) {
            Product product = line.getKey();
            if (line.getValue() > product.getStockQuantity()) {
                throw new OrderException(String.format(Locale.US,
                        "Not enough stock for %s (you want %d, available %d).",
                        product.getName(), line.getValue(),
                        product.getStockQuantity()));
            }
        }

        List<OrderItem> items = new ArrayList<>();
        for (Map.Entry<Product, Integer> line : shoppingCart.entrySet()) {
            items.add(new OrderItem(line.getKey(), line.getValue()));
        }
        Order order = new Order(this, items);

        for (Map.Entry<Product, Integer> line : shoppingCart.entrySet()) {
            line.getKey().reduceStock(line.getValue());
        }
        orders.add(order);
        shoppingCart.clear();
        return order;
    }

    /**
     * Cancels one of the customer's own orders. The stock of the products is
     * returned to the store by the order itself.
     *
     * @param order the order to cancel
     * @throws OrderException if the order does not belong to this customer or
     *                        its status does not allow cancelling
     */
    public void cancelOrder(Order order) throws OrderException {
        if (order == null || !orders.contains(order)) {
            throw new OrderException(
                    "That order does not belong to " + name + ".");
        }
        order.updateStatus(OrderStatus.CANCELLED);
    }

    @Override
    public String toString() {
        return customerID + " - " + name;
    }

    // ------------------------------------------------------------------
    // Validation helper
    // ------------------------------------------------------------------

    private static String requireText(String value, String field) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(field + " cannot be empty.");
        }
        return value.trim();
    }
}
