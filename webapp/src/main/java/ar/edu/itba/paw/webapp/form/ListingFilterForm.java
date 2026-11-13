package ar.edu.itba.paw.webapp.form;

import ar.edu.itba.paw.model.Condition;
import ar.edu.itba.paw.model.ListingSort;
import ar.edu.itba.paw.model.ListingStatus;
import ar.edu.itba.paw.service.dto.ListingFilterDto;

import java.math.BigDecimal;
import javax.validation.constraints.Min;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class ListingFilterForm {

    private Long categoryId;
    private Long subcategoryId;
    private BigDecimal minPrice;
    private BigDecimal maxPrice;
    private Condition condition;
    private Boolean acceptsTrade;
    private Boolean onlyFree;
    private String query;
    private ListingSort sort = ListingSort.RECENT;
    private ListingStatus status;
    @Min(1)
    private Integer page;
    private Long provinceId;
    private Boolean acceptsShipping;
    private Long brandId;
    private Long modelId;
    private Integer yearMin;
    private Integer yearMax;

    public Integer getPage() {
        return page == null || page < 1 ? 1 : page;
    }

    public ListingFilterDto toDto(Long creatorId, ListingStatus status, int pageSize) {
        // REVIEW: creatorId, status and pageSize are supplied by each controller, not the form.
        return ListingFilterDto.builder()
                .categoryId(categoryId)
                .subcategoryId(subcategoryId)
                .minPrice(minPrice)
                .maxPrice(maxPrice)
                .condition(condition)
                .acceptsTrade(acceptsTrade)
                .onlyFree(onlyFree)
                .query(query)
                .sort(sort)
                .creatorId(creatorId)
                .status(status)
                .page(getPage())
                .pageSize(pageSize)
                .provinceId(provinceId)
                .acceptsShipping(acceptsShipping)
                .brandId(brandId)
                .modelId(modelId)
                .yearMin(yearMin)
                .yearMax(yearMax)
                .build();
    }
}
