-- ============================================================
-- 002 - Create customers table
-- ============================================================
CREATE TABLE snackoverflow.customers
(
    id         BIGSERIAL    PRIMARY KEY,
    name       VARCHAR(200) NOT NULL,
    email      VARCHAR(200) NOT NULL UNIQUE,
    phone      VARCHAR(20),
    created_at TIMESTAMP    NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_customers_email ON snackoverflow.customers (email);

