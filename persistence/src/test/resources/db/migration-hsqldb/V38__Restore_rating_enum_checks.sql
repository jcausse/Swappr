-- HSQLDB version of V38__Restore_rating_enum_checks.sql (PostgreSQL compatibility mode).
ALTER TABLE ratings ADD CONSTRAINT ratings_role_check CHECK (role IN ('BUYER', 'SELLER'));
ALTER TABLE ratings ADD CONSTRAINT ratings_type_check CHECK (type IN ('POSITIVE', 'NEUTRAL', 'NEGATIVE'));
