package com.shop.infrastructure.mail;

import com.shop.application.port.EmailSender;
import com.shop.domain.model.Order;
import com.shop.domain.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/** Simulated e-mail: the invoice is written to the logs. */
@Component
@ConditionalOnProperty(name = "app.mail.mode", havingValue = "log", matchIfMissing = true)
public class LoggingEmailSender implements EmailSender {

    private static final Logger log = LoggerFactory.getLogger(LoggingEmailSender.class);

    @Override
    public void sendOrderConfirmation(User user, Order order) {
        log.info("[SIMULATED EMAIL] To: {}\nSubject: Confirmation de votre commande {}\n\n{}",
                user.getEmail(), order.getId(), InvoiceFormatter.format(user, order));
    }
}
