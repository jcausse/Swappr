package ar.edu.itba.paw.service.dto;

import java.math.BigDecimal;

public record OfferCreationDto(
    Long listingId,
    Long buyerId,
    String offerType,
    BigDecimal customAmount,
    BigDecimal tradeAmount,
    String message,
    Long offeredListingId
) {}
