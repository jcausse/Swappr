-- Each offer can have one buyer rating and one seller rating
ALTER TABLE ratings ADD CONSTRAINT unique_ratings_offer_id_role UNIQUE (offer_id, role);
