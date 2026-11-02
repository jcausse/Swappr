package ar.edu.itba.paw.service.dto;
import ar.edu.itba.paw.model.Condition;
import ar.edu.itba.paw.model.Price;

public record ListingUpdateDto(
    Long listingId,
    String title,
    Price price,
    Long productId,
    Condition condition,
    boolean acceptsTrade,
    boolean acceptsShipping,
    String description
) {}