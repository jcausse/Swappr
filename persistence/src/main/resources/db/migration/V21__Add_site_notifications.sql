CREATE TABLE IF NOT EXISTS site_notifications (
    notification_id     SERIAL                      PRIMARY KEY,
    user_id             INTEGER                     NOT NULL REFERENCES users(user_id) ON DELETE CASCADE,
    title               VARCHAR(255)                NOT NULL,
    body                TEXT                        NOT NULL,
    action_url          TEXT,
    action_label_key    VARCHAR(100),
    is_read             BOOLEAN                     NOT NULL DEFAULT FALSE,
    notification_type   VARCHAR(50)                 NOT NULL,
    created_at          TIMESTAMP WITH TIME ZONE    NOT NULL DEFAULT now()
);

CREATE INDEX IF NOT EXISTS site_notifications_user_id_created_at_idx
    ON site_notifications (user_id, created_at DESC);

CREATE INDEX IF NOT EXISTS site_notifications_user_id_unread_idx
    ON site_notifications (user_id, created_at DESC)
    WHERE is_read = FALSE;
