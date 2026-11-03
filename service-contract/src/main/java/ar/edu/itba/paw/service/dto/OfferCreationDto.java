package ar.edu.itba.paw.service.dto;

import ar.edu.itba.paw.model.OfferType;
import java.math.BigDecimal;

public record OfferCreationDto(
    Long listingId,
    Long buyerId,
    OfferType offerType,
    BigDecimal customAmount,
    BigDecimal tradeAmount,
    String message,
    Long offeredListingId
) {}
