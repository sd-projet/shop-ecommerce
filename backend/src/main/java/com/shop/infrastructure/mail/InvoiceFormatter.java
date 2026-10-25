package com.shop.infrastructure.mail;

import com.shop.domain.model.Order;
import com.shop.domain.model.OrderItem;
import com.shop.domain.model.User;

import java.util.Locale;

final class InvoiceFormatter {

    private InvoiceFormatter() {}

    static String format(User user, Order order) {
        StringBuilder sb = new StringBuilder();
        sb.append("Bonjour ").append(user.getFirstName()).append(",\n\n");
        sb.append("Merci pour votre commande, elle est confirmée.\n\n");
        sb.append("FACTURE - Commande ").append(order.getId()).append("\n");
        sb.append("--------------------------------------------\n");
        for (OrderItem i : order.getItems()) {
            sb.append(String.format(Locale.FRANCE, "%-30s x%d  %s%n", i.productName(), i.quantity(), euros(i.totalCents())));
        }
        sb.append(String.format(Locale.FRANCE, "Livraison (%s): %s%n", order.getShippingMethod().label(), euros(order.shippingCents())));
        sb.append("--------------------------------------------\n");
        sb.append("TOTAL: ").append(euros(order.totalCents())).append("\n\n");
        var a = order.getShippingAddress();
        sb.append("Adresse de livraison: ").append(a.fullName()).append(", ").append(a.street()).append(", ")
                .append(a.postalCode()).append(" ").append(a.city()).append(", ").append(a.country()).append("\n");
        return sb.toString();
    }

    static String euros(long cents) {
        return String.format(Locale.FRANCE, "%.2f €", cents / 100.0);
    }
}
