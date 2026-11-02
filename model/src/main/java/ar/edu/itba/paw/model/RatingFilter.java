package ar.edu.itba.paw.model;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public final class RatingFilter {

    private final Long ratedId;
    private final List<RatingRole> roles;
    private final List<OfferRating> types;
    private final int page;
    private final int pageSize;
}
