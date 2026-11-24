package com.ecommerce.orders;

/**
 * OrderException - a checked exception that reports a problem while an order
 * is being placed, changed or cancelled (for example an empty cart or an
 * invalid status change). Because it is checked, the compiler forces the
 * calling code to handle it with try-catch or to declare it with throws.
 *
 * @author Elyas Ghwiet
 * @version 1.0
 */
public class OrderException extends Exception {

    private static final long serialVersionUID = 1L;

    /**
     * Creates the exception with a message that can be shown to the user.
     *
     * @param message explanation of what went wrong
     */
    public OrderException(String message) {
        super(message);
    }
}
