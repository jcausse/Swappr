INSERT INTO users (user_id, username, display_name, email, password)
VALUES (2, 'buyer_user', 'Buyer User', 'buyer@example.com', 'fake_password');

INSERT INTO brands (brand_id, brand_name)
VALUES (2, 'Samsung');

INSERT INTO models (model_id, model_name, brand_id, subcategory_id)
VALUES (2, 'Galaxy S23', 2, 2);

INSERT INTO images (image_id, filename, alt, content_type, data)
VALUES (1, 'front.jpg', 'Front view', 'image/jpeg', X'01'),
       (2, 'back.jpg', 'Back view', 'image/jpeg', X'02'),
       (3, 'extra1.jpg', 'Extra view 1', 'image/jpeg', X'03'),
       (4, 'extra2.jpg', 'Extra view 2', 'image/jpeg', X'04');

INSERT INTO listings (listing_id, title, description, creator_id, model_id, year, price, status, condition, accepts_trade)
VALUES (1, 'MacBook Pro 2023', 'Barely used laptop', 1, 1, 2023, 1500.00, 'ACTIVE', 'GOOD', TRUE),
       (2, 'Galaxy S23', 'Phone in great shape', 1, 2, 2023, 800.00, 'ACTIVE', 'LIKE_NEW', FALSE),
       (3, 'Old MacBook', 'Only good for parts', 2, 1, 2015, 300.00, 'SOLD', 'FOR_PARTS', FALSE),
       (4, 'Gaming laptop', 'Fast machine', 2, 1, 2024, 2000.00, 'ACTIVE', 'EXCELLENT', TRUE);

INSERT INTO listing_images (listing_id, image_id, display_order)
VALUES (1, 2, 1),
       (1, 1, 0);

INSERT INTO offers (offer_id, listing_id, buyer_id, amount, is_full_price, status, message, created_at)
VALUES (1, 1, 2, 1400.00, FALSE, 'PENDING', NULL, TIMESTAMP '2026-01-01 10:00:00+00:00'),
       (2, 1, 2, 1500.00, TRUE, 'PENDING', NULL, TIMESTAMP '2026-01-02 10:00:00+00:00'),
       (3, 4, 1, 1900.00, FALSE, 'PENDING', NULL, TIMESTAMP '2026-01-05 10:00:00+00:00'),
       (4, 4, 1, 1000.00, FALSE, 'REJECTED', NULL, TIMESTAMP '2026-01-06 10:00:00+00:00');
