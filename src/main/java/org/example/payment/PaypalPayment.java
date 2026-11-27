package org.example.payment;

import org.example.model.PaymentResult;

public class PaypalPayment extends PaymentMethod {
    private final String email;

    public PaypalPayment(String email) {
        super("PayPal");
        if (email == null || email.isBlank() || !email.contains("@")) {
            throw new IllegalArgumentException("Invalid PayPal email address");
        }
        this.email = email.trim();
    }

    @Override
    public PaymentResult processPayment(double amount) {
        if (amount <= 0) {
            return new PaymentResult(false, "Payment amount must be greater than zero");
        }
        return new PaymentResult(true, "Paid " + amount + " using PayPal (" + email + ")");
    }
}
