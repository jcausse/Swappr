-- HSQLDB version of V25__Create_brands_and_models_tables.sql (PostgreSQL compatibility mode).
CREATE TABLE IF NOT EXISTS brands (
    brand_id    SERIAL        PRIMARY KEY,
    brand_name  VARCHAR(100)  NOT NULL UNIQUE
);

CREATE INDEX IF NOT EXISTS brands_brand_name_asc_idx ON brands(brand_name);
CREATE INDEX IF NOT EXISTS brands_brand_name_desc_idx ON brands(brand_name DESC);

CREATE TABLE IF NOT EXISTS models (
    model_id        SERIAL        PRIMARY KEY,
    model_name      VARCHAR(100)  NOT NULL,
    brand_id        INTEGER       NOT NULL REFERENCES brands(brand_id) ON DELETE CASCADE,
    subcategory_id  INTEGER       NOT NULL REFERENCES subcategories(subcategory_id) ON DELETE CASCADE,
    UNIQUE (model_name, brand_id)
);

CREATE INDEX IF NOT EXISTS subcategory_brand_idx ON models(subcategory_id, brand_id);
