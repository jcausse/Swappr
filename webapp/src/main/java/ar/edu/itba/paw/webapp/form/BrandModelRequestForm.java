package ar.edu.itba.paw.webapp.form;

import ar.edu.itba.paw.model.BrandModelRequestType;
import ar.edu.itba.paw.webapp.form.validation.ValidBrandModelRequest;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@NoArgsConstructor
@Getter
@Setter
@ValidBrandModelRequest
public class BrandModelRequestForm {

    @NotNull
    private BrandModelRequestType type = BrandModelRequestType.NEW_BRAND;

    @NotNull
    private Long categoryId;

    @NotNull
    private Long subcategoryId;

    private Boolean isAutoSubmit = false;

    private Long brandId;

    @Size(max = 100)
    private String newBrandName;

    @NotBlank
    @Size(max = 100)
    private String modelName;

    public void applyDefaults(Long categoryId, Long subcategoryId, Long brandId) {
        if (this.categoryId == null) {
            this.categoryId = categoryId;
        }
        if (this.subcategoryId == null) {
            this.subcategoryId = subcategoryId;
        }
        if (this.brandId == null) {
            this.brandId = brandId;
        }
        if (brandId != null) {
            this.type = BrandModelRequestType.EXISTING_BRAND;
        }
    }
}
