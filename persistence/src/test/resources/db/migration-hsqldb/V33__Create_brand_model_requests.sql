-- HSQLDB version of V33__Create_brand_model_requests.sql (PostgreSQL compatibility mode).
CREATE TABLE IF NOT EXISTS brand_model_requests (
    request_id      SERIAL                      PRIMARY KEY,
    request_type    VARCHAR(20)                 NOT NULL,
    brand_id        INTEGER                     REFERENCES brands(brand_id) ON DELETE CASCADE,
    brand_name      VARCHAR(100),
    model_name      VARCHAR(100)                NOT NULL,
    subcategory_id  INTEGER                     NOT NULL REFERENCES subcategories(subcategory_id) ON DELETE CASCADE,
    requester_id    INTEGER                     NOT NULL REFERENCES users(user_id) ON DELETE CASCADE,
    created_at      TIMESTAMP WITH TIME ZONE    NOT NULL DEFAULT now(),
    CONSTRAINT brand_model_requests_type_check CHECK (
        (request_type = 'NEW_BRAND' AND brand_id IS NULL AND brand_name IS NOT NULL)
        OR (request_type = 'EXISTING_BRAND' AND brand_id IS NOT NULL AND brand_name IS NULL)
    )
);

CREATE INDEX IF NOT EXISTS brand_model_requests_created_at_idx
    ON brand_model_requests (created_at DESC, request_id DESC);
