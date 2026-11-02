package ar.edu.itba.paw.service.dto;
import java.math.BigDecimal;

public record ListingUpdateDto(
    Long listingId,
    String title,
    BigDecimal price,
    Long productId,
    String condition,
    boolean acceptsTrade,
    boolean acceptsShipping,
    String description
) {}