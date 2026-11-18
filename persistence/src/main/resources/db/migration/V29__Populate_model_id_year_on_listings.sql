UPDATE listings AS l
SET model_id = m.model_id
FROM products AS p
INNER JOIN brands AS b
    ON b.brand_name = p.brand
INNER JOIN models AS m
    ON m.model_name = p.model
   AND m.brand_id = b.brand_id
WHERE l.product_id = p.product_id;

UPDATE listings AS l
SET year = p.year
FROM products AS p
WHERE l.product_id = p.product_id;
