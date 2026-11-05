-- HSQLDB version of V23__Internationalize_site_notifications.sql (PostgreSQL compatibility mode).
CREATE TABLE IF NOT EXISTS site_notification_args (
    notification_id     INTEGER         NOT NULL REFERENCES site_notifications(notification_id) ON DELETE CASCADE,
    arg_index           INTEGER         NOT NULL,
    arg_value           TEXT            NOT NULL,
    PRIMARY KEY (notification_id, arg_index)
);

ALTER TABLE site_notifications ADD COLUMN title_key VARCHAR(100) NOT NULL;
ALTER TABLE site_notifications ADD COLUMN body_key VARCHAR(100) NOT NULL;
ALTER TABLE site_notifications DROP COLUMN title;
ALTER TABLE site_notifications DROP COLUMN body;
