package com.ecommerce.orders;

/**
 * OrderStatus - the stages in the life of an order.
 *
 * An order normally moves from PLACED to PROCESSING, then SHIPPED, then
 * DELIVERED. It can be CANCELLED only before it is shipped. The method
 * canChangeTo keeps these rules in one place.
 *
 * @author Elyas Ghwiet
 * @version 1.0
 */
public enum OrderStatus {

    PLACED("Placed"),
    PROCESSING("Processing"),
    SHIPPED("Shipped"),
    DELIVERED("Delivered"),
    CANCELLED("Cancelled");

    private final String label;

    OrderStatus(String label) {
        this.label = label;
    }

    /** Returns the text that is shown to the user, such as "Processing". */
    public String getLabel() {
        return label;
    }

    /**
     * Tells whether an order with this status may change to the next status.
     *
     * @param next the status that is requested
     * @return true if the change is allowed
     */
    public boolean canChangeTo(OrderStatus next) {
        switch (this) {
            case PLACED:
                return next == PROCESSING || next == CANCELLED;
            case PROCESSING:
                return next == SHIPPED || next == CANCELLED;
            case SHIPPED:
                return next == DELIVERED;
            default:
                return false;
        }
    }
}
