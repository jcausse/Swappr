UPDATE listings AS l
SET brand_id = b.brand_id
FROM brands AS b INNER JOIN products AS p
    ON p.brand = b.brand_name
WHERE l.product_id = p.product_id;

UPDATE listings AS l
SET model_id = m.model_id
FROM models AS m INNER JOIN products AS p
    ON p.model = m.model_name
WHERE l.product_id = p.product_id
  AND m.brand_id = l.brand_id;

UPDATE listings AS l
SET year = p.year
FROM products AS p
WHERE l.product_id = p.product_id;
