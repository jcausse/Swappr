ALTER TABLE site_notifications
    ADD COLUMN offer_id INTEGER REFERENCES offers(offer_id) ON DELETE CASCADE;

CREATE INDEX site_notifications_offer_id_idx ON site_notifications(offer_id);
