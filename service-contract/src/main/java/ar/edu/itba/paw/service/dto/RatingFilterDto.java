package ar.edu.itba.paw.service.dto;

import ar.edu.itba.paw.model.OfferRating;
import ar.edu.itba.paw.model.RatingRole;
import lombok.Builder;

import java.util.List;

@Builder
public record RatingFilterDto(
    Long ratedId,
    List<RatingRole> roles,
    List<OfferRating> types,
    Integer page,
    Integer pageSize
) {}
