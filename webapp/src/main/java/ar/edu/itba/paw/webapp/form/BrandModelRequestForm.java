package ar.edu.itba.paw.webapp.form;

import ar.edu.itba.paw.model.BrandModelRequestType;
import ar.edu.itba.paw.service.dto.BrandModelRequestCreationDto;
import ar.edu.itba.paw.webapp.form.validation.ValidBrandModelRequest;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// Field validations live in BrandModelRequestValidator so automatic category submits can skip them.
@NoArgsConstructor
@Getter
@Setter
@ValidBrandModelRequest
public class BrandModelRequestForm {

    private BrandModelRequestType type = BrandModelRequestType.NEW_BRAND;

    private Long categoryId;

    private Long subcategoryId;

    private Boolean isAutoSubmit = false;

    private Long brandId;

    private String newBrandName;

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

    public BrandModelRequestCreationDto toDto(Long requesterId) {
        // REVIEW: requesterId comes from the authenticated user, not the form.
        return new BrandModelRequestCreationDto(
                requesterId,
                type,
                subcategoryId,
                brandId,
                newBrandName,
                modelName
        );
    }
}
