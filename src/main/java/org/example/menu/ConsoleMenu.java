package org.example.menu;

import org.example.config.AppConfig;
import org.example.model.Order;
import org.example.model.OrderItem;
import org.example.model.PaymentResult;
import org.example.payment.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class ConsoleMenu {
    private final Scanner scanner = new Scanner(System.in);
    private final PaymentProcessor paymentProcessor = new PaymentProcessor();
    private final List<Order> completedOrders = new ArrayList<>();

    private Order currentOrder;

    public void start() {
        AppConfig config = AppConfig.getInstance();
        System.out.println("Welcome to " + config.getApplicationName());

        boolean running = true;
        while (running) {
            printMenu();

            try {
                int option = Integer.parseInt(scanner.nextLine().trim());

                switch (option) {
                    case 1 -> createOrder();
                    case 2 -> addItem();
                    case 3 -> viewOrder();
                    case 4 -> payOrder();
                    case 5 -> viewCompletedOrders();
                    case 0 -> running = false;
                    default -> System.out.println("Invalid option");
                }
            } catch (NumberFormatException e) {
                System.out.println("Please enter a valid number.");
            } catch (IllegalStateException | IllegalArgumentException e) {
                System.out.println(e.getMessage());
            }
        }
    }

    private void createOrder() {
        System.out.println("Customer name:");
        String customerName = scanner.nextLine().trim();

        currentOrder = Order.builder()
                .customerName(customerName)
                .build();
        System.out.println("Order created for " + customerName);
    }

    private void addItem() {
        if (currentOrder == null) {
            System.out.println("Create an order first.");
            return;
        }

        String itemName = "";
        do{
            System.out.println("Item name:");
            itemName = scanner.nextLine().trim();

            if(itemName.isEmpty()){
                System.out.println("Item name cannot be blank!");
            }
        }while(itemName.isEmpty());

        double price;
        do {
            System.out.println("Price:");
            price = Double.parseDouble(scanner.nextLine().trim());
            if(price <= 0){
                System.out.println("Price can't be 0 or less");
            }
        }while(price <= 0);
        System.out.println("Quantity:");
        int quantity = Integer.parseInt(scanner.nextLine().trim());

        currentOrder.addItem(new OrderItem(itemName, price, quantity));
        System.out.println("Item added to order");
    }

    private void viewOrder() {
        if (currentOrder == null) {
            System.out.println("No active order.");
            return;
        }

        System.out.println("Customer: " + currentOrder.getCustomerName());
        System.out.println("Status: " + currentOrder.getStatus());
        System.out.println("Items:");

        for (OrderItem item : currentOrder.getItems()) {
            System.out.println("- " + item);
        }

        System.out.println("Total: " + currentOrder.calculateTotal());
        System.out.println("Total (incl. 21% tax): " + currentOrder.calculateTotalWithTax(0.21));
    }

    private void payOrder() {
        if (currentOrder == null) {
            System.out.println("Create an order first.");
            return;
        }

        System.out.println("""
                Select payment method:
                1. Credit Card
                2. PayPal
                3. Gift Card
                4. Bank Transfer
                """);
        int option = Integer.parseInt(scanner.nextLine().trim());

        PaymentMethod paymentMethod = switch (option) {
            case 1 -> createCreditCardPayment();
            case 2 -> createPaypalPayment();
            case 3 -> createGiftCardPayment();
            case 4 -> createBankTransferPayment();
            default -> throw new IllegalArgumentException("Invalid payment method");
        };

        PaymentResult result = paymentProcessor.process(currentOrder, paymentMethod);
        System.out.println(result.getMessage());
        if (result.isSuccessful() && !completedOrders.contains(currentOrder)) {
            completedOrders.add(currentOrder);
        }
    }

    private void viewCompletedOrders() {
        if (completedOrders.isEmpty()) {
            System.out.println("No completed orders yet.");
            return;
        }

        String currency = AppConfig.getInstance().getCurrency();
        System.out.println("--- Completed Orders ---");
        for (int i = 0; i < completedOrders.size(); i++) {
            Order o = completedOrders.get(i);
            System.out.printf("%d. %s | Items: %d | Total (incl. tax): %.2f %s%n",
                    i + 1,
                    o.getCustomerName(),
                    o.getItems().size(),
                    o.calculateTotalWithTax(0.21),
                    currency);
        }
    }

    private PaymentMethod createCreditCardPayment() {
        System.out.println("Card number:");
        String cardNumber = scanner.nextLine().trim();

        System.out.println("Card holder name:");
        String cardHolderName = scanner.nextLine().trim();

        return PaymentMethodFactory.createCreditCardPayment(cardNumber, cardHolderName);
    }

    private PaymentMethod createPaypalPayment() {
        String email;
        while (true) {
            System.out.print("Enter PayPal email address: ");
            email = scanner.nextLine().trim();
            if (!email.isEmpty()) {
                break;
            }
            System.out.println("Email address is required.");
        }
        return PaymentMethodFactory.createPayPalPayment(email);
    }

    private PaymentMethod createGiftCardPayment() {
        String code;
        while (true) {
            System.out.print("Enter gift card code: ");
            code = scanner.nextLine().trim();
            if (!code.isEmpty()) {
                break;
            }
            System.out.println("Gift card code is required.");
        }

        double balance;
        while (true) {
            System.out.print("Enter available balance: ");
            String input = scanner.nextLine().trim();
            try {
                balance = Double.parseDouble(input);
                if (Double.isFinite(balance) && balance >= 0) {
                    break;
                }
            } catch (NumberFormatException ignored) {
            }
            System.out.println("Enter a valid non-negative number for balance.");
        }

        return PaymentMethodFactory.createGiftCardPayment(code, balance);
    }

    private PaymentMethod createBankTransferPayment() {
        String iban;
        while (true) {
            System.out.print("Enter IBAN or account number: ");
            iban = scanner.nextLine().trim();
            if (!iban.isEmpty()) {
                break;
            }
            System.out.println("IBAN or account number is required.");
        }
        return PaymentMethodFactory.createBankTransferPayment(iban);
    }

    private void printMenu() {
        System.out.println("""
                1. Create order
                2. Add item to order
                3. View order
                4. Pay order
                5. View completed orders
                0. Exit
                """);
    }
}