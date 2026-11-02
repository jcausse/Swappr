package ar.edu.itba.paw.service.dto;
import java.math.BigDecimal;
import java.util.List;
public record ListingCreationDto(
    String title,
    BigDecimal price,
    Long creatorId,
    Long productId,
    String condition,
    boolean acceptsTrade,
    boolean acceptsShipping,
    String description,
    List<ImageData> images
) {}
