package com.shop.infrastructure.payment;

import com.shop.application.port.PaymentGateway;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.exception.StripeException;
import com.stripe.model.Event;
import com.stripe.model.PaymentIntent;
import com.stripe.model.StripeObject;
import com.stripe.net.RequestOptions;
import com.stripe.net.Webhook;
import com.stripe.param.PaymentIntentCreateParams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

/** Real Stripe integration (use test keys sk_test_... for the sandbox). Enabled with app.payment.provider=stripe. */
@Component
@ConditionalOnProperty(name = "app.payment.provider", havingValue = "stripe")
public class StripePaymentGateway implements PaymentGateway {

    private static final Logger log = LoggerFactory.getLogger(StripePaymentGateway.class);

    private final StripeProperties properties;
    private final RequestOptions options;

    public StripePaymentGateway(StripeProperties properties) {
        this.properties = properties;
        this.options = RequestOptions.builder().setApiKey(properties.secretKey()).build();
    }

    @Override
    public PaymentIntentInfo createPaymentIntent(long amountCents, String currency, UUID orderId) {
        try {
            PaymentIntent intent = PaymentIntent.create(PaymentIntentCreateParams.builder()
                    .setAmount(amountCents)
                    .setCurrency(currency)
                    .putMetadata("orderId", orderId.toString())
                    .setAutomaticPaymentMethods(PaymentIntentCreateParams.AutomaticPaymentMethods.builder().setEnabled(true).build())
                    .build(), options);
            return new PaymentIntentInfo(intent.getId(), intent.getClientSecret());
        } catch (StripeException e) {
            throw new IllegalStateException("Unable to create the Stripe payment intent", e);
        }
    }

    @Override
    public boolean isPaymentSucceeded(String paymentIntentId) {
        try {
            return "succeeded".equals(PaymentIntent.retrieve(paymentIntentId, options).getStatus());
        } catch (StripeException e) {
            throw new IllegalStateException("Unable to retrieve the Stripe payment intent", e);
        }
    }

    @Override
    public Optional<PaymentEvent> parseWebhook(String payload, String signatureHeader) {
        try {
            Event event = Webhook.constructEvent(payload, signatureHeader, properties.webhookSecret());
            boolean succeeded = "payment_intent.succeeded".equals(event.getType());
            boolean failed = "payment_intent.payment_failed".equals(event.getType());
            if (!succeeded && !failed) {
                return Optional.empty();
            }
            StripeObject object = event.getDataObjectDeserializer().getObject()
                    .orElseGet(() -> {
                        try {
                            return event.getDataObjectDeserializer().deserializeUnsafe();
                        } catch (Exception e) {
                            return null;
                        }
                    });
            if (object instanceof PaymentIntent pi) {
                return Optional.of(new PaymentEvent(pi.getId(), succeeded));
            }
            log.warn("Unreadable Stripe event payload for {}", event.getId());
            return Optional.empty();
        } catch (SignatureVerificationException e) {
            throw new InvalidWebhookSignatureException();
        }
    }
}
