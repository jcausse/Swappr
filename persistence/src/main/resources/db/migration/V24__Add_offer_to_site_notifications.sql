ALTER TABLE site_notifications
    ADD COLUMN offer_id INTEGER REFERENCES offers(offer_id) ON DELETE CASCADE;

CREATE INDEX idx_site_notifications_offer_id ON site_notifications(offer_id);
