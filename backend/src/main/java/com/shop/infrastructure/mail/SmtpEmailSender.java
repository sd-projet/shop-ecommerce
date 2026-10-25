package com.shop.infrastructure.mail;

import com.shop.application.port.EmailSender;
import com.shop.domain.model.Order;
import com.shop.domain.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

/** Sends the invoice through SMTP (MailDev in docker-compose: http://localhost:1080). */
@Component
@ConditionalOnProperty(name = "app.mail.mode", havingValue = "smtp")
public class SmtpEmailSender implements EmailSender {

    private static final Logger log = LoggerFactory.getLogger(SmtpEmailSender.class);

    private final JavaMailSender mailSender;
    private final String from;

    public SmtpEmailSender(JavaMailSender mailSender, @Value("${app.mail.from}") String from) {
        this.mailSender = mailSender;
        this.from = from;
    }

    @Override
    public void sendOrderConfirmation(User user, Order order) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(from);
            message.setTo(user.getEmail());
            message.setSubject("Confirmation de votre commande " + order.getId());
            message.setText(InvoiceFormatter.format(user, order));
            mailSender.send(message);
        } catch (Exception e) {
            // an e-mail failure must never roll back a paid order
            log.error("Could not send the confirmation e-mail for order {}", order.getId(), e);
        }
    }
}
