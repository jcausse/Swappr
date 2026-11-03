package ar.edu.itba.paw.persistence;

import ar.edu.itba.paw.model.OfferRating;
import ar.edu.itba.paw.model.Page;
import ar.edu.itba.paw.model.Rating;
import ar.edu.itba.paw.model.RatingFilter;
import ar.edu.itba.paw.model.RatingRole;

import java.time.Instant;

public interface RatingDao {

    Page<Rating> search(RatingFilter filter);

    Rating create(Long creatorId, Long ratedId, Long offerId, RatingRole role, OfferRating type, String reviewText, Instant createdAt);
}