package ar.edu.itba.paw.service.dto;
import ar.edu.itba.paw.model.Condition;
import ar.edu.itba.paw.model.ListingSort;
import ar.edu.itba.paw.model.ListingStatus;
import lombok.Builder;

import java.math.BigDecimal;

@Builder
public record ListingFilterDto(
    Long categoryId,
    Long subcategoryId,
    BigDecimal minPrice,
    BigDecimal maxPrice,
    Condition condition,
    Boolean acceptsTrade,
    Boolean onlyFree,
    String query,
    ListingSort sort,
    Long creatorId,
    ListingStatus status,
    Integer page,
    Integer pageSize,
    Boolean hasActiveOffers,
    Long provinceId,
    Boolean acceptsShipping,
    Long brandId,
    Long modelId,
    Integer yearMin,
    Integer yearMax
) {}
