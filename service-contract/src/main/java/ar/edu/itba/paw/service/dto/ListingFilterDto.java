package ar.edu.itba.paw.service.dto;
import lombok.Builder;

import java.math.BigDecimal;

@Builder
public record ListingFilterDto(
    Long categoryId,
    Long subcategoryId,
    BigDecimal minPrice,
    BigDecimal maxPrice,
    String condition,
    Boolean acceptsTrade,
    String query,
    String sort,
    Long creatorId,
    String status,
    Integer page,
    Integer pageSize,
    Boolean hasActiveOffers,
    Long provinceId,
    Boolean acceptsShipping,
    String brand,
    String model,
    Integer yearMin,
    Integer yearMax
) {}
