-- Extra catalog rows for ModelJpaDaoTest.
-- Model 1 (Apple MacBook Pro, subcategory 1) comes from initial-data.sql.

INSERT INTO brands (brand_id, brand_name) VALUES
    (2, 'Dell'),
    (3, 'Lenovo'),
    (4, 'Samsung');

INSERT INTO models (model_id, model_name, brand_id, subcategory_id) VALUES
    -- Same brand (Apple), different model -> for DISTINCT/ORDER on models
    (2, 'MacBook Air', 1, 1),
    -- Other brands in the same subcategory -> for DISTINCT/ORDER on brands
    (3, 'XPS 15', 2, 1),
    (4, 'ThinkPad X1', 3, 1),
    -- Duplicate brand (Dell) to ensure brands are de-duplicated
    (5, 'Inspiron 14', 2, 1),
    -- A model in a different subcategory (Phones, subcat 2 / category 2) -> must not leak
    (6, 'Galaxy S23', 4, 2);
