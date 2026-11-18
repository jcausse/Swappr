package ar.edu.itba.paw.service.dto;
import java.math.BigDecimal;
import java.util.List;

import ar.edu.itba.paw.model.Condition;

public record ListingUpdateDto(
    Long listingId,
    String title,
    BigDecimal price,
    Long modelId,
    int year,
    Condition condition,
    boolean acceptsTrade,
    boolean acceptsShipping,
    String description,
    List<ImageData> newImages,
    List<Long> removedImageIds
) {}
