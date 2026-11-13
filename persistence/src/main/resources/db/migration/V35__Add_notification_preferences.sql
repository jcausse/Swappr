CREATE TABLE IF NOT EXISTS user_notification_preferences (
    user_id                 INTEGER         NOT NULL        REFERENCES users(user_id) ON DELETE CASCADE,
    notification_type       VARCHAR(50)     NOT NULL,
    notification_channel    VARCHAR(50)     NOT NULL,
    is_enabled              BOOLEAN         NOT NULL        DEFAULT TRUE,
    PRIMARY KEY (user_id, notification_type, notification_channel)
);
