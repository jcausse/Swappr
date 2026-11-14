package ar.edu.itba.paw.webapp.form.validation;

import ar.edu.itba.paw.model.BrandModelRequestType;
import ar.edu.itba.paw.webapp.form.BrandModelRequestFields;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

// Rules that depend on the request type, so they cannot be field constraints:
// an existing brand needs its id, a new brand needs its name.
public class BrandModelRequestValidator implements ConstraintValidator<ValidBrandModelRequest, BrandModelRequestFields> {

    @Override
    public boolean isValid(BrandModelRequestFields form, ConstraintValidatorContext context) {
        if (form == null) {
            return true;
        }

        boolean valid = true;
        context.disableDefaultConstraintViolation();

        if (form.getType() == BrandModelRequestType.EXISTING_BRAND && form.getBrandId() == null) {
            addViolation(context, "brandId", "error.catalogRequest.existingBrandRequired");
            valid = false;
        }

        if (form.getType() == BrandModelRequestType.NEW_BRAND
                && (form.getNewBrandName() == null || form.getNewBrandName().isBlank())) {
            addViolation(context, "newBrandName", "error.catalogRequest.newBrandRequired");
            valid = false;
        }

        return valid;
    }

    private static void addViolation(ConstraintValidatorContext context, String field, String messageKey) {
        context.buildConstraintViolationWithTemplate("{" + messageKey + "}")
                .addPropertyNode(field)
                .addConstraintViolation();
    }
}
