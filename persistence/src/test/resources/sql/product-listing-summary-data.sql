-- Active/inactive listings used by ProductJpaDaoTest#getListingInfoSummary.
-- Products 2, 3 and 4 share the Apple brand; products 2 and 3 also share a model
-- but have different years, which lets pagination be asserted per product id.

INSERT INTO listings (listing_id, title, description, creator_id, product_id, price, status, condition, accepts_trade)
VALUES
    (101, 'MacBook Pro 2021 - good', NULL, 1, 2, 1000.00, 'ACTIVE', 'GOOD', FALSE),
    (102, 'MacBook Pro 2021 - like new', NULL, 1, 2, 1200.00, 'ACTIVE', 'LIKE_NEW', FALSE),
    (103, 'MacBook Pro 2021 - sold', NULL, 1, 2, 900.00, 'SOLD', 'GOOD', FALSE),
    (104, 'MacBook Pro 2024', NULL, 1, 3, 1500.00, 'ACTIVE', 'EXCELLENT', FALSE),
    (105, 'MacBook Air 2022', NULL, 1, 4, 800.00, 'ACTIVE', 'FAIR', FALSE),
    (106, 'Dell XPS 15', NULL, 1, 5, 1300.00, 'ACTIVE', 'GOOD', FALSE),
    (107, 'Dell Inspiron 14 - sold', NULL, 1, 7, 400.00, 'SOLD', 'AS_IS', FALSE),
    (108, 'Galaxy S23 - other subcategory', NULL, 1, 8, 700.00, 'ACTIVE', 'GOOD', FALSE);
