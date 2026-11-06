package ar.edu.itba.paw.service.dto;
import java.math.BigDecimal;
import java.util.List;

import ar.edu.itba.paw.model.Condition;

public record ListingCreationDto(
    String title,
    BigDecimal price,
    Long creatorId,
    Long modelId,
    int year,
    Condition condition,
    boolean acceptsTrade,
    boolean acceptsShipping,
    String description,
    List<ImageData> images
) {}
