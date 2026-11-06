package ar.edu.itba.paw.webapp.form;

import ar.edu.itba.paw.webapp.form.validation.ValidChooseProduct;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
@ValidChooseProduct
public class ChooseProductForm {

    private Integer step = 1;

    private Long categoryId;
    private Long subcategoryId;

    private Long brandId;
    private Long modelId;
    private Integer year;

    private Boolean isAutoSubmit = false;

    private Long previousCategoryId;
    private Long previousSubcategoryId;

    public boolean isCategoryChanged() {
        return categoryId != null && !categoryId.equals(previousCategoryId);
    }

    public boolean isSubcategoryChanged() {
        return subcategoryId != null && !subcategoryId.equals(previousSubcategoryId);
    }

    public void updatePreviousValues() {
        this.previousCategoryId = this.categoryId;
        this.previousSubcategoryId = this.subcategoryId;
    }
}
