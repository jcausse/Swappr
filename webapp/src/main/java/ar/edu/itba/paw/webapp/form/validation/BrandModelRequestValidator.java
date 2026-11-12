package ar.edu.itba.paw.webapp.form.validation;

import ar.edu.itba.paw.model.BrandModelRequestType;
import ar.edu.itba.paw.webapp.form.BrandModelRequestForm;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class BrandModelRequestValidator implements ConstraintValidator<ValidBrandModelRequest, BrandModelRequestForm> {

    private static final int MAX_NAME_LENGTH = 100;

    @Override
    public boolean isValid(BrandModelRequestForm form, ConstraintValidatorContext context) {
        // Field validations live here instead of on the form fields so that category auto-submits can skip them.
        if (form == null || Boolean.TRUE.equals(form.getIsAutoSubmit())) {
            return true;
        }

        boolean valid = true;
        context.disableDefaultConstraintViolation();

        if (form.getType() == null) {
            addViolation(context, "type", "error.catalogRequest.typeRequired");
            valid = false;
        }

        if (form.getCategoryId() == null) {
            addViolation(context, "categoryId", "error.catalogRequest.categoryRequired");
            valid = false;
        }

        if (form.getSubcategoryId() == null) {
            addViolation(context, "subcategoryId", "error.catalogRequest.subcategoryRequired");
            valid = false;
        }

        if (form.getNewBrandName() != null && form.getNewBrandName().length() > MAX_NAME_LENGTH) {
            addViolation(context, "newBrandName", "error.catalogRequest.newBrandTooLong");
            valid = false;
        }

        if (form.getModelName() == null || form.getModelName().isBlank()) {
            addViolation(context, "modelName", "error.catalogRequest.modelRequired");
            valid = false;
        } else if (form.getModelName().length() > MAX_NAME_LENGTH) {
            addViolation(context, "modelName", "error.catalogRequest.modelTooLong");
            valid = false;
        }

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
            ConstraintValidatorContext context,
            String field,
            String messageKey
    ) {
        context.buildConstraintViolationWithTemplate("{" + messageKey + "}")
                .addPropertyNode(field)
                .addConstraintViolation();
    }
}
