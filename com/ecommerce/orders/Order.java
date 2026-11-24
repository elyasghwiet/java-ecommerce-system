package com.ecommerce.orders;

import com.ecommerce.Customer;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

/**
 * Order - an order that a customer placed in the online store.
 *
 * An order stores its ID, the customer, the items that were bought, the
 * total price, the date and the current status. The items and the total
 * cannot be changed after the order is created. Only the status can change,
 * and only through updateStatus, which enforces the rules of OrderStatus.
 *
 * @author Elyas Ghwiet
 * @version 1.0
 */
public class Order {

    private static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm", Locale.US);

    /** Number used to build the ID of the next order. */
    private static int nextOrderNumber = 1001;

    private final String orderID;
    private final Customer customer;
    private final List<OrderItem> items;
    private final double orderTotal;
    private final LocalDateTime orderDate;
    private OrderStatus status;

    /**
     * Creates a new order with the status PLACED. The constructor is public
     * because Customer, which creates orders, is in a different package.
     *
     * @param customer the customer who places the order
     * @param items    the items of the order; at least one
     * @throws IllegalArgumentException if the customer is null or the list of
     *                                  items is null or empty
     */
    public Order(Customer customer, List<OrderItem> items) {
        if (customer == null) {
            throw new IllegalArgumentException("An order needs a customer.");
        }
        if (items == null || items.isEmpty()) {
            throw new IllegalArgumentException(
                    "An order must contain at least one item.");
        }
        this.orderID = "ORD-" + nextOrderNumber++;
        this.customer = customer;
        this.items = Collections.unmodifiableList(new ArrayList<>(items));
        this.orderTotal = calculateTotal(this.items);
        this.orderDate = LocalDateTime.now();
        this.status = OrderStatus.PLACED;
    }

    // ------------------------------------------------------------------
    // Getters (there are no setters: an order is changed only by
    // updateStatus)
    // ------------------------------------------------------------------

    public String getOrderID() {
        return orderID;
    }

    public Customer getCustomer() {
        return customer;
    }

    public List<OrderItem> getItems() {
        return items;
    }

    public double getOrderTotal() {
        return orderTotal;
    }

    public LocalDateTime getOrderDate() {
        return orderDate;
    }

    public OrderStatus getStatus() {
        return status;
    }

    // ------------------------------------------------------------------
    // Order management
    // ------------------------------------------------------------------

    /**
     * Changes the status of the order. When an order is cancelled, the
     * products of the order are returned to the stock of the store.
     *
     * @param newStatus the status that is requested
     * @throws OrderException if the status cannot change from the current
     *                        status to the requested one
     */
    public void updateStatus(OrderStatus newStatus) throws OrderException {
        if (newStatus == null) {
            throw new OrderException("The new status cannot be empty.");
        }
        if (!status.canChangeTo(newStatus)) {
            throw new OrderException("Order " + orderID
                    + " cannot change from " + status.getLabel() + " to "
                    + newStatus.getLabel() + ".");
        }
        status = newStatus;

        if (newStatus == OrderStatus.CANCELLED) {
            for (OrderItem item : items) {
                item.getProduct().addStock(item.getQuantity());
            }
        }
    }

    /**
     * Builds a text summary of the order, ready to be printed.
     *
     * @return the summary with one line for each item
     */
    public String generateSummary() {
        String newLine = System.lineSeparator();
        StringBuilder summary = new StringBuilder();

        summary.append("Order ID    : ").append(orderID).append(newLine);
        summary.append("Date        : ").append(orderDate.format(DATE_FORMAT))
                .append(newLine);
        summary.append("Customer    : ").append(customer.getCustomerID())
                .append(" - ").append(customer.getName()).append(newLine);
        summary.append("Status      : ").append(status.getLabel())
                .append(newLine);
        summary.append("Items       :").append(newLine);

        int lineNumber = 1;
        for (OrderItem item : items) {
            summary.append(String.format(Locale.US,
                    "  %d. %-5s %-27s %3d x %9s = %10s%n",
                    lineNumber++,
                    item.getProduct().getProductID(),
                    item.getProduct().getName(),
                    item.getQuantity(),
                    money(item.getUnitPrice()),
                    money(item.getSubtotal())));
        }
        summary.append("Order total : ").append(money(orderTotal));
        return summary.toString();
    }

    // ------------------------------------------------------------------
    // Helpers
    // ------------------------------------------------------------------

    /** Formats an amount of money, for example $19.99. */
    private static String money(double amount) {
        return String.format(Locale.US, "$%.2f", amount);
    }

    /** Adds the subtotals of all the items and rounds to two decimals. */
    private static double calculateTotal(List<OrderItem> orderItems) {
        double total = 0;
        for (OrderItem item : orderItems) {
            total += item.getSubtotal();
        }
        return Math.round(total * 100.0) / 100.0;
    }
}
