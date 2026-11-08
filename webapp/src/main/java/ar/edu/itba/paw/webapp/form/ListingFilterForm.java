package ar.edu.itba.paw.webapp.form;

import ar.edu.itba.paw.model.Condition;
import ar.edu.itba.paw.model.ListingSort;
import ar.edu.itba.paw.model.ListingStatus;

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
}
