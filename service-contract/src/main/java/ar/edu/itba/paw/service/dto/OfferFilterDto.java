package ar.edu.itba.paw.service.dto;

import ar.edu.itba.paw.model.OfferStatusGroup;
import lombok.Builder;

@Builder
public record OfferFilterDto(
    Long sellerId,
    Long buyerId,
    OfferStatusGroup statusGroup,
    Integer page,
    Integer pageSize
) {}
