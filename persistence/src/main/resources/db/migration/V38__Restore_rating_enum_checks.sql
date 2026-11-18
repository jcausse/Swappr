-- V18 dropped these checks to uppercase the values but never re-added them.
ALTER TABLE ratings ADD CONSTRAINT ratings_role_check CHECK (role IN ('BUYER', 'SELLER'));
ALTER TABLE ratings ADD CONSTRAINT ratings_type_check CHECK (type IN ('POSITIVE', 'NEUTRAL', 'NEGATIVE'));
