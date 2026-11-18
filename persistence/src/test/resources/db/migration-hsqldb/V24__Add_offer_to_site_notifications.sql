-- HSQLDB version of V24__Add_offer_to_site_notifications.sql (PostgreSQL compatibility mode).
ALTER TABLE site_notifications ADD COLUMN offer_id INTEGER;
ALTER TABLE site_notifications ADD CONSTRAINT fk_site_notifications_offer FOREIGN KEY (offer_id) REFERENCES offers(offer_id) ON DELETE CASCADE;
CREATE INDEX IF NOT EXISTS idx_site_notifications_offer_id ON site_notifications(offer_id);
