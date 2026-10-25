package com.shop.application.port;

import java.util.Optional;
import java.util.UUID;

/** Outbound port towards the payment provider (Stripe in production, a fake in dev/tests). */
public interface PaymentGateway {

    record PaymentIntentInfo(String id, String clientSecret) {}

    /** Event received from the provider, already authenticated (signature verified). */
    record PaymentEvent(String paymentIntentId, boolean succeeded) {}

    PaymentIntentInfo createPaymentIntent(long amountCents, String currency, UUID orderId);

    boolean isPaymentSucceeded(String paymentIntentId);

    /** Verifies the signature and parses a webhook payload. Empty if the event is irrelevant. */
    Optional<PaymentEvent> parseWebhook(String payload, String signatureHeader);
}
