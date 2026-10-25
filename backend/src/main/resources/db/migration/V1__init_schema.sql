CREATE TABLE users (
    id            UUID PRIMARY KEY,
    email         VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    first_name    VARCHAR(100),
    last_name     VARCHAR(100),
    role          VARCHAR(30)  NOT NULL,
    created_at    TIMESTAMPTZ  NOT NULL
);

CREATE TABLE products (
    id          UUID PRIMARY KEY,
    name        VARCHAR(255) NOT NULL,
    description TEXT,
    price_cents BIGINT       NOT NULL CHECK (price_cents >= 0),
    image_url   VARCHAR(500),
    category    VARCHAR(100),
    stock       INTEGER      NOT NULL CHECK (stock >= 0),
    sales_count INTEGER      NOT NULL DEFAULT 0,
    archived    BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at  TIMESTAMPTZ  NOT NULL
);
CREATE INDEX idx_products_category ON products (category);
CREATE INDEX idx_products_price ON products (price_cents);
CREATE INDEX idx_products_name_lower ON products (lower(name));

CREATE TABLE orders (
    id                UUID PRIMARY KEY,
    user_id           UUID         NOT NULL REFERENCES users (id),
    status            VARCHAR(20)  NOT NULL,
    shipping_method   VARCHAR(20)  NOT NULL,
    items_total_cents BIGINT       NOT NULL,
    shipping_cents    BIGINT       NOT NULL,
    payment_intent_id VARCHAR(255),
    ship_full_name    VARCHAR(255),
    ship_street       VARCHAR(255),
    ship_city         VARCHAR(255),
    ship_postal_code  VARCHAR(20),
    ship_country      VARCHAR(100),
    created_at        TIMESTAMPTZ  NOT NULL,
    updated_at        TIMESTAMPTZ  NOT NULL
);
CREATE INDEX idx_orders_user ON orders (user_id, created_at DESC);
CREATE INDEX idx_orders_status ON orders (status);
CREATE UNIQUE INDEX idx_orders_payment_intent ON orders (payment_intent_id) WHERE payment_intent_id IS NOT NULL;

CREATE TABLE order_items (
    id               UUID PRIMARY KEY,
    order_id         UUID         NOT NULL REFERENCES orders (id) ON DELETE CASCADE,
    product_id       UUID         NOT NULL REFERENCES products (id),
    product_name     VARCHAR(255) NOT NULL,
    unit_price_cents BIGINT       NOT NULL,
    quantity         INTEGER      NOT NULL CHECK (quantity > 0)
);
CREATE INDEX idx_order_items_order ON order_items (order_id);
