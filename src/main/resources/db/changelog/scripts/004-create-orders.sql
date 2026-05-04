-- ============================================================
-- 004 - Create orders table
-- ============================================================
CREATE TABLE snackoverflow.orders
(
    id                       BIGSERIAL      PRIMARY KEY,
    customer_id              BIGINT         NOT NULL,
    status                   VARCHAR(50)    NOT NULL DEFAULT 'PENDING',
    total_amount             NUMERIC(10, 2) NOT NULL,
    preparation_time_minutes INT,
    notes                    TEXT,
    cancellation_reason      TEXT,
    created_at               TIMESTAMP      NOT NULL DEFAULT NOW(),
    updated_at               TIMESTAMP      NOT NULL DEFAULT NOW(),

    CONSTRAINT fk_orders_customer FOREIGN KEY (customer_id)
        REFERENCES snackoverflow.customers (id) ON DELETE RESTRICT,

    CONSTRAINT chk_orders_status CHECK (
        status IN ('PENDING', 'CONFIRMED', 'PREPARING', 'READY', 'CANCELLED')
    ),
    CONSTRAINT chk_orders_total CHECK (total_amount > 0)
);

CREATE INDEX idx_orders_customer_id ON snackoverflow.orders (customer_id);
CREATE INDEX idx_orders_status      ON snackoverflow.orders (status);
CREATE INDEX idx_orders_created_at  ON snackoverflow.orders (created_at);
-- Used by auto-cancellation scheduler: find PENDING orders older than 10 min
CREATE INDEX idx_orders_status_created ON snackoverflow.orders (status, created_at)
    WHERE status = 'PENDING';

