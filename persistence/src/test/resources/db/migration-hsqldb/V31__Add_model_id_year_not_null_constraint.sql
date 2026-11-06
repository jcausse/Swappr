-- HSQLDB version of V31__Add_model_id_year_not_null_constraint.sql (PostgreSQL compatibility mode).
ALTER TABLE listings ALTER COLUMN model_id SET NOT NULL;
ALTER TABLE listings ALTER COLUMN year SET NOT NULL;
