package com.shop.application.port;

import com.shop.domain.model.Order;
import com.shop.domain.model.User;

public interface EmailSender {
    void sendOrderConfirmation(User user, Order order);
}
