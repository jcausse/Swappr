package ar.edu.itba.paw.service;

import ar.edu.itba.paw.model.Offer;
import ar.edu.itba.paw.model.OfferRating;
import ar.edu.itba.paw.model.Page;
import ar.edu.itba.paw.model.Rating;
import ar.edu.itba.paw.model.RatingFilter;
import ar.edu.itba.paw.model.RatingRole;
import ar.edu.itba.paw.model.User;
import ar.edu.itba.paw.persistence.RatingDao;
import ar.edu.itba.paw.service.dto.RatingFilterDto;

import java.time.Instant;
import java.util.Objects;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class RatingServiceImpl implements RatingService {

    private final RatingDao ratingDao;

    @Autowired
    public RatingServiceImpl(RatingDao ratingDao) {
        this.ratingDao = ratingDao;
    }

    @Override
    public Page<Rating> get(RatingFilterDto dto) {
        Objects.requireNonNull(dto, "RatingFilterDto cannot be null");

        final var filter = RatingFilter.builder()
            .ratedId(dto.ratedId())
            .roles(dto.roles())
            .types(dto.types())
            .page(sanitizePage(dto.page()))
            .pageSize(dto.pageSize())
            .build();

        return ratingDao.search(filter);
    }

    private static int sanitizePage(final Integer page) {
        return page == null || page < 1 ? 1 : page;
    }

    @Override
    @Transactional
    public Rating create(User creator, User rated, Offer offer, RatingRole role, OfferRating type, String reviewText) {
        Objects.requireNonNull(creator, "creator cannot be null");
        Objects.requireNonNull(rated, "rated cannot be null");
        Objects.requireNonNull(offer, "offer cannot be null");
        Objects.requireNonNull(role, "role cannot be null");
        Objects.requireNonNull(type, "type cannot be null");

        return ratingDao.create(creator.getId(), rated.getId(), offer.getId(), role, type, reviewText, Instant.now());
    }
}