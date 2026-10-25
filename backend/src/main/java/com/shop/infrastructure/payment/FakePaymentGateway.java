package com.shop.infrastructure.payment;

import com.shop.application.port.PaymentGateway;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

/** Offline gateway used by default (no Stripe keys needed): every payment "succeeds" on confirmation. */
@Component
@ConditionalOnProperty(name = "app.payment.provider", havingValue = "fake", matchIfMissing = true)
public class FakePaymentGateway implements PaymentGateway {

    @Override
    public PaymentIntentInfo createPaymentIntent(long amountCents, String currency, UUID orderId) {
        String id = "pi_fake_" + UUID.randomUUID().toString().replace("-", "");
        return new PaymentIntentInfo(id, "fake_secret_" + id);
    }

    @Override
    public boolean isPaymentSucceeded(String paymentIntentId) {
        return paymentIntentId != null && paymentIntentId.startsWith("pi_fake_");
    }

    @Override
    public Optional<PaymentEvent> parseWebhook(String payload, String signatureHeader) {
        return Optional.empty();
    }
}
