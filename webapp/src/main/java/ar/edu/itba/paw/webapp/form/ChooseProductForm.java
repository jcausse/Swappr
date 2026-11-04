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

    /** Value of the "other" option in the brand/model selects; the user then types it in otherBrand/otherModel. */
    public static final String OTHER_VALUE = "__OTHER__";

    private Integer step = 1;

    private Long categoryId;
    private Long subcategoryId;

    private String newProductBrand;
    private String newProductModel;
    private Integer newProductYear;

    private String otherBrand;
    private String otherModel;

    private Boolean isAutoSubmit = false;

    private Long previousCategoryId;
    private Long previousSubcategoryId;

    public boolean isOtherBrandSelected() {
        return OTHER_VALUE.equals(newProductBrand);
    }

    public boolean isOtherModelSelected() {
        return OTHER_VALUE.equals(newProductModel);
    }

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