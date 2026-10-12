-- HSQLDB version of V17__Add_user_suspension.sql
ALTER TABLE users ADD COLUMN IF NOT EXISTS suspended_at TIMESTAMP WITH TIME ZONE NULL;
