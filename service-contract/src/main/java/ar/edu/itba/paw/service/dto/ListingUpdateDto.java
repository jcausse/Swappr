package ar.edu.itba.paw.service.dto;
import java.math.BigDecimal;

import ar.edu.itba.paw.model.Condition;

public record ListingUpdateDto(
    Long listingId,
    String title,
    BigDecimal price,
    Long productId,
    Condition condition,
    boolean acceptsTrade,
    boolean acceptsShipping,
    String description
) {}
