-- HSQLDB version of V35__Add_notification_preferences.sql (PostgreSQL compatibility mode).
CREATE TABLE IF NOT EXISTS user_notification_preferences (
    user_id             INTEGER         NOT NULL        REFERENCES users(user_id) ON DELETE CASCADE,
    notification_type   VARCHAR(50)     NOT NULL,
    mode                VARCHAR(20)     NOT NULL,
    PRIMARY KEY (user_id, notification_type)
);
