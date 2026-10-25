package com.shop.domain.model;

public enum ShippingMethod {
    STANDARD(490, "Standard (3-5 jours)"),
    EXPRESS(990, "Express (24h)"),
    PICKUP(0, "Retrait en magasin");

    private final long costCents;
    private final String label;

    ShippingMethod(long costCents, String label) {
        this.costCents = costCents;
        this.label = label;
    }

    public long costCents() { return costCents; }
    public String label() { return label; }
}
