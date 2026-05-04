-- ============================================================
-- 003 - Create menu_items table
-- ============================================================
CREATE TABLE snackoverflow.menu_items
(
    id                       BIGSERIAL      PRIMARY KEY,
    name                     VARCHAR(200)   NOT NULL,
    description              TEXT,
    price                    NUMERIC(10, 2) NOT NULL,
    category                 VARCHAR(50)    NOT NULL,
    preparation_time_minutes INT            NOT NULL,
    is_available             BOOLEAN        NOT NULL DEFAULT TRUE,
    calories_per_serving     INT,
    image_url                VARCHAR(500),
    is_deleted               BOOLEAN        NOT NULL DEFAULT FALSE,
    created_at               TIMESTAMP      NOT NULL DEFAULT NOW(),
    updated_at               TIMESTAMP      NOT NULL DEFAULT NOW(),

    CONSTRAINT chk_menu_price    CHECK (price > 0),
    CONSTRAINT chk_menu_prep     CHECK (preparation_time_minutes > 0),
    CONSTRAINT chk_menu_calories CHECK (calories_per_serving IS NULL OR calories_per_serving >= 0),
    CONSTRAINT chk_menu_category CHECK (category IN ('APPETIZER', 'MAIN', 'DESSERT', 'BEVERAGE'))
);

CREATE INDEX idx_menu_items_category    ON snackoverflow.menu_items (category)    WHERE is_deleted = FALSE;
CREATE INDEX idx_menu_items_available   ON snackoverflow.menu_items (is_available) WHERE is_deleted = FALSE;
CREATE INDEX idx_menu_items_is_deleted  ON snackoverflow.menu_items (is_deleted);
