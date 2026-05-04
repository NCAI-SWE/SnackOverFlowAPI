-- ============================================================
-- 005 - Create order_items table
-- ============================================================
CREATE TABLE snackoverflow.order_items
(
    id           BIGSERIAL      PRIMARY KEY,
    order_id     BIGINT         NOT NULL,
    menu_item_id BIGINT         NOT NULL,
    quantity     INT            NOT NULL,
    unit_price   NUMERIC(10, 2) NOT NULL,
    subtotal     NUMERIC(10, 2) NOT NULL,

    CONSTRAINT fk_order_items_order FOREIGN KEY (order_id)
        REFERENCES snackoverflow.orders (id) ON DELETE CASCADE,

    CONSTRAINT fk_order_items_menu FOREIGN KEY (menu_item_id)
        REFERENCES snackoverflow.menu_items (id) ON DELETE RESTRICT,

    CONSTRAINT chk_order_items_qty      CHECK (quantity > 0),
    CONSTRAINT chk_order_items_price    CHECK (unit_price > 0),
    CONSTRAINT chk_order_items_subtotal CHECK (subtotal > 0)
);

CREATE INDEX idx_order_items_order_id     ON snackoverflow.order_items (order_id);
CREATE INDEX idx_order_items_menu_item_id ON snackoverflow.order_items (menu_item_id);

