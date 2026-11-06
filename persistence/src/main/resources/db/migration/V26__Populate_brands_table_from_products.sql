INSERT INTO brands(brand_name)
SELECT DISTINCT brand FROM products
ON CONFLICT (brand_name) DO NOTHING;
