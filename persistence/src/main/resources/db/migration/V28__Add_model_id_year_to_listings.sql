ALTER TABLE listings
    ADD COLUMN model_id INTEGER
        REFERENCES models(model_id);

ALTER TABLE listings
    ADD COLUMN year INTEGER;
