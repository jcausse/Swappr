-- Extra catalog data for brand replacement conflict tests.
INSERT INTO brands (brand_id, brand_name) VALUES
    (90, 'Old Brand'),
    (91, 'New Brand');

INSERT INTO models (model_id, model_name, brand_id, subcategory_id) VALUES
    (90, 'Duplicate Model', 90, 1),
    (91, 'Duplicate Model', 91, 1),
    (92, 'Unique Model', 90, 1);
