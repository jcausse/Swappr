package ar.edu.itba.paw.service.dto;
import ar.edu.itba.paw.model.Condition;
import ar.edu.itba.paw.model.Price;
import java.util.List;
public record ListingCreationDto(
    String title,
    Price price,
    Long creatorId,
    Long productId,
    Condition condition,
    boolean acceptsTrade,
    boolean acceptsShipping,
    String description,
    List<ImageData> images
) {}
