INSERT INTO models(model_name, brand_id, subcategory_id)
SELECT DISTINCT p.model, b.brand_id, p.subcategory_id
FROM brands AS b INNER JOIN products AS p on b.brand_name = p.brand
ON CONFLICT DO NOTHING;
