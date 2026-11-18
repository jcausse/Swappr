-- HSQLDB version of V28__Add_model_id_year_to_listings.sql (PostgreSQL compatibility mode).
ALTER TABLE listings
    ADD COLUMN model_id INTEGER
        REFERENCES models(model_id);

ALTER TABLE listings
    ADD COLUMN year INTEGER;
