-- Backfill legacy listings without a creator with the oldest user, then enforce the invariant
UPDATE listings
SET creator_id = (SELECT MIN(user_id) FROM users)
WHERE creator_id IS NULL;

ALTER TABLE listings ALTER COLUMN creator_id SET NOT NULL;
