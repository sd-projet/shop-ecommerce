package com.shop.infrastructure.payment;

public class InvalidWebhookSignatureException extends RuntimeException {
    public InvalidWebhookSignatureException() {
        super("Invalid webhook signature");
    }
}
