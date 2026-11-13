package ar.edu.itba.paw.webapp.form.validation;

import ar.edu.itba.paw.model.BrandModelRequestType;
import ar.edu.itba.paw.webapp.form.BrandModelRequestFields;

import javax.validation.ConstraintValidatorContext;

// The brand rule both brand/model request validators apply: an existing brand needs its id, a new brand its name.
final class BrandModelRequestBrandRule {

    private BrandModelRequestBrandRule() {
        throw new AssertionError("Utility class");
    }

    // Adds one violation per broken rule; the caller must have disabled the default constraint violation.
    static boolean isValid(final BrandModelRequestFields form, final ConstraintValidatorContext context) {
        boolean valid = true;

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

    private static void addViolation(
            final ConstraintValidatorContext context,
            final String field,
            final String messageKey
    ) {
        context.buildConstraintViolationWithTemplate("{" + messageKey + "}")
                .addPropertyNode(field)
                .addConstraintViolation();
    }
}
