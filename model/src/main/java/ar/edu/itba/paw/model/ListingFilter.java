package ar.edu.itba.paw.model;

import java.math.BigDecimal;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public final class ListingFilter {

    private final Long categoryId;
    private final Long subcategoryId;
    private final BigDecimal minPrice;
    private final BigDecimal maxPrice;
    private final Condition condition;
    private final Boolean acceptsTrade;
    private final String query;
    private final ListingSort sort;
    private final Long creatorId;
    private final ListingStatus status;
    private final int page;
    private final int pageSize;
    private final Boolean hasActiveOffers;
    private final Long provinceId;
    private final Boolean acceptsShipping;
    private final String brand;
    private final String model;
    private final Integer yearMin;
    private final Integer yearMax;
}
