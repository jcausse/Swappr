package ar.edu.itba.paw.webapp.form;

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
    private String condition;
    private Boolean acceptsTrade;
    private String query;
    private String sort;
    private String status;
    @Min(1)
    private Integer page;
    private Long provinceId;
    private Boolean acceptsShipping;
    private String brand;
    private String model;
    private Integer yearMin;
    private Integer yearMax;
}
