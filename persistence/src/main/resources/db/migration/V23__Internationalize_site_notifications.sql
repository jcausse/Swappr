CREATE TABLE IF NOT EXISTS site_notification_args (
    notification_id     INTEGER         NOT NULL REFERENCES site_notifications(notification_id) ON DELETE CASCADE,
    arg_index           INTEGER         NOT NULL,
    arg_value           TEXT            NOT NULL,
    PRIMARY KEY (notification_id, arg_index)
);

DELETE FROM site_notifications;

ALTER TABLE site_notifications ADD COLUMN title_key VARCHAR(100) NOT NULL;
ALTER TABLE site_notifications ADD COLUMN body_key VARCHAR(100) NOT NULL;
ALTER TABLE site_notifications DROP COLUMN title;
ALTER TABLE site_notifications DROP COLUMN body;
