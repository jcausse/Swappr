package ar.edu.itba.paw.webapp.form;

import javax.validation.constraints.Min;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class ProductSearchForm {

    private Long categoryId;
    private Long subcategoryId;
    private Long brandId;
    private Long modelId;
    private Integer yearMin;
    private Integer yearMax;
    @Min(1)
    private Integer page;

    public Integer getPage() {
        return page == null || page < 1 ? 1 : page;
    }
}
