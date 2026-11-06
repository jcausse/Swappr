-- Active/inactive listings used by ModelJpaDaoTest#getListingInfoSummary.
-- Models 1 and 2 share the Apple brand; model 1 has listings from different years,
-- which lets year filtering and pagination be asserted per model id.

INSERT INTO listings (listing_id, title, description, creator_id, model_id, year, price, status, condition, accepts_trade)
VALUES
    (101, 'MacBook Pro 2021 - good', NULL, 1, 1, 2021, 1000.00, 'ACTIVE', 'GOOD', FALSE),
    (102, 'MacBook Pro 2021 - like new', NULL, 1, 1, 2021, 1200.00, 'ACTIVE', 'LIKE_NEW', FALSE),
    (103, 'MacBook Pro 2021 - sold', NULL, 1, 1, 2021, 900.00, 'SOLD', 'GOOD', FALSE),
    (104, 'MacBook Pro 2024', NULL, 1, 1, 2024, 1500.00, 'ACTIVE', 'EXCELLENT', FALSE),
    (105, 'MacBook Air 2022', NULL, 1, 2, 2022, 800.00, 'ACTIVE', 'FAIR', FALSE),
    (106, 'Dell XPS 15', NULL, 1, 3, 2023, 1300.00, 'ACTIVE', 'GOOD', FALSE),
    (107, 'Dell Inspiron 14 - sold', NULL, 1, 5, 2022, 400.00, 'SOLD', 'AS_IS', FALSE),
    (108, 'Galaxy S23 - other subcategory', NULL, 1, 6, 2023, 700.00, 'ACTIVE', 'GOOD', FALSE);
