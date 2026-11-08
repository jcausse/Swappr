-- A single listing on model 3 (Dell XPS 15), used to test brand deletion guards.
INSERT INTO listings (listing_id, title, description, creator_id, model_id, year, price, status, condition, accepts_trade)
VALUES (900, 'Dell listing', NULL, 1, 3, 2023, 1000.00, 'ACTIVE', 'GOOD', FALSE);
