-- HSQLDB version of V34__Create_brand_model_request_additional_users.sql (PostgreSQL compatibility mode).
CREATE TABLE IF NOT EXISTS brand_model_request_additional_users (
    request_id  INTEGER                     NOT NULL REFERENCES brand_model_requests(request_id) ON DELETE CASCADE,
    user_id     INTEGER                     NOT NULL REFERENCES users(user_id) ON DELETE CASCADE,
    created_at  TIMESTAMP WITH TIME ZONE    NOT NULL DEFAULT now(),
    PRIMARY KEY (request_id, user_id)
);

CREATE INDEX IF NOT EXISTS brand_model_request_additional_users_user_id_idx
    ON brand_model_request_additional_users (user_id);

ALTER TABLE brand_model_requests
    ADD CONSTRAINT unique_brand_model_requests_brand_model UNIQUE (brand_id, model_name);

ALTER TABLE brand_model_requests
    ADD CONSTRAINT unique_brand_model_requests_new_brand_model UNIQUE (brand_name, model_name);
