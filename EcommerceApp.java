import com.ecommerce.Customer;
import com.ecommerce.Product;
import com.ecommerce.orders.Order;
import com.ecommerce.orders.OrderException;
import com.ecommerce.orders.OrderStatus;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Scanner;
import java.util.function.Function;

/**
 * EcommerceApp - the main program of the online store.
 *
 * This class is outside of the packages. It imports Customer and Product from
 * com.ecommerce, and Order, OrderException and OrderStatus from
 * com.ecommerce.orders, and uses them through a menu in the console. A
 * customer can browse the products, add them to a shopping cart, remove them,
 * place an order, view and cancel orders. A "store manager" option updates the
 * status of an order. Every value typed by the user is validated.
 *
 * @author Elyas Ghwiet
 * @version 1.0
 */
public class EcommerceApp {

    /** Width of the lines that are printed to the console. */
    private static final int LINE_WIDTH = 74;

    private final Scanner scanner = new Scanner(System.in);
    private final List<Product> catalog = new ArrayList<>();
    private final List<Customer> customers = new ArrayList<>();
    private Customer currentCustomer;

    /**
     * Program entry point. It runs the store and makes sure the Scanner is
     * closed, even when the input ends unexpectedly.
     */
    public static void main(String[] args) {
        EcommerceApp app = new EcommerceApp();
        try {
            app.run();
        } catch (NoSuchElementException e) {
            System.out.println("\nInput ended unexpectedly. Exiting the program.");
        } finally {
            app.scanner.close();
        }
    }

    // ------------------------------------------------------------------
    // Program flow
    // ------------------------------------------------------------------

    /** Creates the data, then repeats the main menu until the user exits. */
    private void run() {
        printBanner();
        loadProducts();
        loadCustomers();
        currentCustomer = selectCustomer();

        boolean running = true;
        while (running) {
            printMenu();
            int choice = readInt("Enter your choice (0-9): ", 0, 9);
            switch (choice) {
                case 1:
                    browseProducts();
                    break;
                case 2:
                    addProductToCart();
                    break;
                case 3:
                    removeProductFromCart();
                    break;
                case 4:
                    viewCart();
                    break;
                case 5:
                    placeOrder();
                    break;
                case 6:
                    viewMyOrders();
                    break;
                case 7:
                    updateOrderStatus();
                    break;
                case 8:
                    cancelOrder();
                    break;
                case 9:
                    currentCustomer = selectCustomer();
                    break;
                default:
                    running = false;
                    break;
            }
        }
        System.out.println();
        System.out.println("Thank you for visiting the store. Goodbye!");
    }

    /** Creates the products of the store. */
    private void loadProducts() {
        catalog.add(new Product("P101", "Wireless Mouse", "Accessories", 19.99, 25));
        catalog.add(new Product("P102", "Mechanical Keyboard",
                "Accessories", 59.50, 10));
        catalog.add(new Product("P103", "USB-C Hub", "Accessories", 34.90, 15));
        catalog.add(new Product("P104", "27-inch Monitor", "Electronics", 189.00, 5));
        catalog.add(new Product("P105", "Laptop Stand", "Accessories", 24.75, 12));
        catalog.add(new Product("P106", "Noise-Cancelling Headphones",
                "Electronics", 129.99, 8));
        catalog.add(new Product("P107", "Webcam 1080p", "Electronics", 45.00, 0));
        catalog.add(new Product("P108", "Portable SSD 1TB", "Storage", 99.95, 7));
    }

    /** Creates the first two customers of the store. */
    private void loadCustomers() {
        customers.add(new Customer("C001", "Sara Ahmed"));
        customers.add(new Customer("C002", "Omar Khalid"));
    }

    // ------------------------------------------------------------------
    // Menu actions
    // ------------------------------------------------------------------

    /** Lets the user choose an existing customer or register a new one. */
    private Customer selectCustomer() {
        printSectionTitle("WHO IS SHOPPING?");
        for (int i = 0; i < customers.size(); i++) {
            System.out.println("  " + (i + 1) + ". " + customers.get(i));
        }
        int newOption = customers.size() + 1;
        System.out.println("  " + newOption + ". Register a new customer");

        int choice = readInt("Enter your choice (1-" + newOption + "): ",
                1, newOption);
        if (choice < newOption) {
            Customer selected = customers.get(choice - 1);
            System.out.println("Welcome, " + selected.getName() + "!");
            return selected;
        }

        String name = readName("Enter your full name: ");
        Customer created = new Customer(
                String.format(Locale.US, "C%03d", customers.size() + 1), name);
        customers.add(created);
        System.out.println("Welcome, " + name + "! Your customer ID is "
                + created.getCustomerID() + ".");
        return created;
    }

    /** Shows every product of the store with its price and stock. */
    private void browseProducts() {
        printSectionTitle("PRODUCT CATALOG");
        System.out.printf(Locale.US, "%-6s %-28s %-12s %9s  %s%n",
                "ID", "Name", "Category", "Price", "Stock");
        System.out.println(repeatChar('-', LINE_WIDTH));
        for (Product product : catalog) {
            String stock = product.isInStock()
                    ? String.valueOf(product.getStockQuantity())
                    : "OUT OF STOCK";
            System.out.printf(Locale.US, "%-6s %-28s %-12s %9s  %s%n",
                    product.getProductID(), product.getName(),
                    product.getCategory(), money(product.getPrice()), stock);
        }
    }

    /** Adds a product to the cart of the current customer. */
    private void addProductToCart() {
        printSectionTitle("ADD A PRODUCT TO THE CART");
        Product product = readById("Enter the product ID (or 0 to cancel): ",
                "product", "in the catalog", catalog, Product::getProductID);
        if (product == null) {
            System.out.println("Nothing was added.");
            return;
        }
        if (!product.isInStock()) {
            System.out.println("Sorry, " + product.getName()
                    + " is out of stock.");
            return;
        }

        int quantity = readInt("Enter the quantity (1-"
                + product.getStockQuantity() + "): ",
                1, product.getStockQuantity());
        try {
            currentCustomer.addToCart(product, quantity);
            System.out.println("Added " + quantity + " x " + product.getName()
                    + " to your cart.");
            System.out.println("Cart total is now "
                    + money(currentCustomer.calculateTotal()) + ".");
        } catch (IllegalArgumentException e) {
            System.out.println("Could not add: " + e.getMessage());
        }
    }

    /** Removes units of a product from the cart of the current customer. */
    private void removeProductFromCart() {
        if (currentCustomer.isCartEmpty()) {
            System.out.println();
            System.out.println("Your shopping cart is empty. Nothing to remove.");
            return;
        }

        printSectionTitle("REMOVE A PRODUCT FROM THE CART");
        printCartLines();
        List<Product> productsInCart = new ArrayList<>(
                currentCustomer.getShoppingCart().keySet());
        Product product = readById(
                "Enter the product ID to remove (or 0 to cancel): ",
                "product", "in your cart", productsInCart,
                Product::getProductID);
        if (product == null) {
            System.out.println("Nothing was removed.");
            return;
        }

        int inCart = currentCustomer.getQuantityInCart(product);
        int quantity = readInt("Enter the quantity to remove (1-" + inCart
                + "): ", 1, inCart);
        try {
            currentCustomer.removeFromCart(product, quantity);
            System.out.println("Removed " + quantity + " x " + product.getName()
                    + " from your cart.");
            System.out.println("Cart total is now "
                    + money(currentCustomer.calculateTotal()) + ".");
        } catch (IllegalArgumentException e) {
            System.out.println("Could not remove: " + e.getMessage());
        }
    }

    /** Shows the cart of the current customer and its total cost. */
    private void viewCart() {
        printSectionTitle("YOUR SHOPPING CART");
        if (currentCustomer.isCartEmpty()) {
            System.out.println("Your shopping cart is empty.");
            return;
        }
        printCartLines();
    }

    /** Places an order for the cart of the current customer. */
    private void placeOrder() {
        printSectionTitle("PLACE AN ORDER");
        if (!currentCustomer.isCartEmpty()) {
            printCartLines();
            if (!askYesNo("Place an order for "
                    + money(currentCustomer.calculateTotal()) + "?")) {
                System.out.println("The order was not placed. "
                        + "Your cart was not changed.");
                return;
            }
        }

        try {
            Order order = currentCustomer.placeOrder();
            System.out.println();
            System.out.println("Your order was placed successfully!");
            System.out.println(order.generateSummary());
        } catch (OrderException e) {
            System.out.println("The order could not be placed: "
                    + e.getMessage());
        }
    }

    /** Shows the summary of every order of the current customer. */
    private void viewMyOrders() {
        printSectionTitle("MY ORDERS");
        List<Order> myOrders = currentCustomer.getOrders();
        if (myOrders.isEmpty()) {
            System.out.println("You have not placed any orders yet.");
            return;
        }
        for (Order order : myOrders) {
            System.out.println(order.generateSummary());
            System.out.println(repeatChar('-', LINE_WIDTH));
        }
    }

    /** Lets the store manager change the status of any order. */
    private void updateOrderStatus() {
        printSectionTitle("UPDATE ORDER STATUS (STORE MANAGER)");
        List<Order> allOrders = new ArrayList<>();
        for (Customer customer : customers) {
            allOrders.addAll(customer.getOrders());
        }
        if (allOrders.isEmpty()) {
            System.out.println("There are no orders yet.");
            return;
        }

        printOrderList(allOrders);
        Order order = readById("Enter the order ID (or 0 to cancel): ",
                "order", "in the list", allOrders, Order::getOrderID);
        if (order == null) {
            System.out.println("No status was changed.");
            return;
        }

        OrderStatus[] choices = {OrderStatus.PROCESSING, OrderStatus.SHIPPED,
            OrderStatus.DELIVERED, OrderStatus.CANCELLED};
        StringBuilder prompt = new StringBuilder("Choose the new status (");
        for (int i = 0; i < choices.length; i++) {
            if (i > 0) {
                prompt.append(", ");
            }
            prompt.append(i + 1).append(' ').append(choices[i].getLabel());
        }
        prompt.append("): ");

        System.out.println("Current status of " + order.getOrderID() + ": "
                + order.getStatus().getLabel());
        int choice = readInt(prompt.toString(), 1, choices.length);

        try {
            order.updateStatus(choices[choice - 1]);
            System.out.println("Order " + order.getOrderID()
                    + " is now " + order.getStatus().getLabel() + ".");
        } catch (OrderException e) {
            System.out.println("The status was not changed: " + e.getMessage());
        }
    }

    /** Lets the current customer cancel one of his or her orders. */
    private void cancelOrder() {
        printSectionTitle("CANCEL AN ORDER");
        List<Order> myOrders = currentCustomer.getOrders();
        if (myOrders.isEmpty()) {
            System.out.println("You have no orders to cancel.");
            return;
        }

        printOrderList(myOrders);
        Order order = readById("Enter the order ID to cancel (or 0 to go back): ",
                "order", "in your orders", myOrders, Order::getOrderID);
        if (order == null) {
            System.out.println("No order was cancelled.");
            return;
        }
        if (!askYesNo("Cancel order " + order.getOrderID() + "?")) {
            System.out.println("The order was not cancelled.");
            return;
        }

        try {
            currentCustomer.cancelOrder(order);
            System.out.println("Order " + order.getOrderID()
                    + " was cancelled and its products returned to the stock.");
        } catch (OrderException e) {
            System.out.println("The order could not be cancelled: "
                    + e.getMessage());
        }
    }

    // ------------------------------------------------------------------
    // Input methods (all of them validate what the user types)
    // ------------------------------------------------------------------

    /**
     * Asks for a whole number and keeps asking until the input is a number
     * between min and max (both included).
     */
    private int readInt(String prompt, int min, int max) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();

            if (input.isEmpty()) {
                System.out.println("Error: the input cannot be empty.");
                continue;
            }
            try {
                int value = Integer.parseInt(input);
                if (value >= min && value <= max) {
                    return value;
                }
                System.out.println("Error: please enter a number between "
                        + min + " and " + max + ".");
            } catch (NumberFormatException e) {
                System.out.println("Error: \"" + input
                        + "\" is not a whole number.");
            }
        }
    }

    /**
     * Asks for a full name and keeps asking until it has 2 to 40 characters,
     * starts with a letter and contains only letters, spaces, dots, hyphens
     * and apostrophes.
     */
    private String readName(String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim().replaceAll("\\s+", " ");

            if (input.isEmpty()) {
                System.out.println("Error: the name cannot be empty.");
            } else if (input.length() < 2 || input.length() > 40) {
                System.out.println(
                        "Error: the name must have between 2 and 40 characters.");
            } else if (!input.matches("\\p{L}[\\p{L} .'-]*")) {
                System.out.println("Error: the name must start with a letter "
                        + "and use only letters, spaces, . ' and -");
            } else {
                return input;
            }
        }
    }

    /**
     * Asks for an ID and returns the matching item of the list. The ID is not
     * case-sensitive. The method returns null when the user types 0, and
     * keeps asking while the input is empty or matches nothing.
     *
     * @param prompt the question shown to the user
     * @param noun   what the ID belongs to, such as "product"
     * @param where  where the item is searched, such as "in the catalog"
     * @param options the items that can be chosen
     * @param idOf   a function that returns the ID of an item
     */
    private <T> T readById(String prompt, String noun, String where,
            List<T> options, Function<T, String> idOf) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();

            if (input.equals("0")) {
                return null;
            }
            if (input.isEmpty()) {
                System.out.println("Error: the " + noun
                        + " ID cannot be empty.");
                continue;
            }
            for (T option : options) {
                if (idOf.apply(option).equalsIgnoreCase(input)) {
                    return option;
                }
            }
            System.out.println("Error: no " + noun + " with ID \"" + input
                    + "\" was found " + where + ". Please try again.");
        }
    }

    /** Asks a yes/no question and keeps asking until the answer is valid. */
    private boolean askYesNo(String question) {
        while (true) {
            System.out.print(question + " (yes/no): ");
            String answer = scanner.nextLine().trim().toLowerCase(Locale.ROOT);

            if (answer.equals("yes") || answer.equals("y")) {
                return true;
            } else if (answer.equals("no") || answer.equals("n")) {
                return false;
            }
            System.out.println("Error: please answer with yes or no.");
        }
    }

    // ------------------------------------------------------------------
    // Display helpers
    // ------------------------------------------------------------------

    /** Prints the lines of the cart and the cart total. */
    private void printCartLines() {
        for (Map.Entry<Product, Integer> line
                : currentCustomer.getShoppingCart().entrySet()) {
            Product product = line.getKey();
            System.out.printf(Locale.US, "%-6s %-28s %3d x %9s = %10s%n",
                    product.getProductID(), product.getName(), line.getValue(),
                    money(product.getPrice()),
                    money(currentCustomer.getLineTotal(product)));
        }
        System.out.println(repeatChar('-', LINE_WIDTH));
        System.out.println("Cart total : "
                + money(currentCustomer.calculateTotal()));
    }

    /** Prints a short table with the ID, customer, status and total. */
    private void printOrderList(List<Order> orders) {
        System.out.printf(Locale.US, "%-10s %-20s %-11s %10s%n",
                "Order ID", "Customer", "Status", "Total");
        System.out.println(repeatChar('-', 54));
        for (Order order : orders) {
            System.out.printf(Locale.US, "%-10s %-20.20s %-11s %10s%n",
                    order.getOrderID(), order.getCustomer().getName(),
                    order.getStatus().getLabel(),
                    money(order.getOrderTotal()));
        }
    }

    /** Prints the main menu in two compact lines. */
    private void printMenu() {
        printSectionTitle("MAIN MENU: " + currentCustomer.getName() + " ("
                + currentCustomer.getCustomerID() + ")");
        System.out.println(" 1 Browse   2 Add to cart   3 Remove   "
                + "4 View cart   5 Place order");
        System.out.println(" 6 My orders   7 Update status   8 Cancel order   "
                + "9 Switch customer   0 Exit");
    }

    /** Prints the title of the program. */
    private void printBanner() {
        System.out.println(repeatChar('=', LINE_WIDTH));
        System.out.println("            ONLINE STORE - SIMPLE E-COMMERCE SYSTEM");
        System.out.println("       Packages: com.ecommerce and com.ecommerce.orders");
        System.out.println(repeatChar('=', LINE_WIDTH));
    }

    /** Prints a section heading followed by a line of dashes. */
    private void printSectionTitle(String title) {
        int dashes = Math.max(3, LINE_WIDTH - title.length() - 5);
        System.out.println();
        System.out.println("--- " + title + " " + repeatChar('-', dashes));
    }

    /** Formats an amount of money, for example $19.99. */
    private static String money(double amount) {
        return String.format(Locale.US, "$%.2f", amount);
    }

    /** Returns a string made of the given character repeated count times. */
    private static String repeatChar(char symbol, int count) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < count; i++) {
            builder.append(symbol);
        }
        return builder.toString();
    }
}
