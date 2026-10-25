package com.shop.presentation.controller;

import com.shop.application.port.PaymentGateway;
import com.shop.application.usecase.order.ConfirmPaymentUseCase;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/** Receives Stripe webhooks: the order is validated only after the provider confirms the payment. */
@RestController
@RequestMapping("/api/payments")
public class PaymentWebhookController {

    private final PaymentGateway gateway;
    private final ConfirmPaymentUseCase confirmPayment;

    public PaymentWebhookController(PaymentGateway gateway, ConfirmPaymentUseCase confirmPayment) {
        this.gateway = gateway;
        this.confirmPayment = confirmPayment;
    }

    @PostMapping("/webhook")
    public ResponseEntity<Void> webhook(@RequestBody String payload,
                                        @RequestHeader(value = "Stripe-Signature", required = false) String signature) {
        gateway.parseWebhook(payload, signature)
                .filter(PaymentGateway.PaymentEvent::succeeded)
                .ifPresent(event -> confirmPayment.confirmFromWebhook(event.paymentIntentId()));
        return ResponseEntity.ok().build();
    }
}
