package ar.edu.itba.paw.webapp.form.validation;

import ar.edu.itba.paw.model.BrandModelRequestType;
import ar.edu.itba.paw.webapp.form.BrandModelRequestForm;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class BrandModelRequestValidator implements ConstraintValidator<ValidBrandModelRequest, BrandModelRequestForm> {

    @Override
    public boolean isValid(BrandModelRequestForm form, ConstraintValidatorContext context) {
        if (form == null || Boolean.TRUE.equals(form.getIsAutoSubmit())) {
            return true;
        }

        boolean valid = true;

        if (form.getType() == BrandModelRequestType.EXISTING_BRAND && form.getBrandId() == null) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("{error.catalogRequest.existingBrandRequired}")
                    .addPropertyNode("brandId")
                    .addConstraintViolation();
            valid = false;
        }

        if (form.getType() == BrandModelRequestType.NEW_BRAND
                && (form.getNewBrandName() == null || form.getNewBrandName().isBlank())) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("{error.catalogRequest.newBrandRequired}")
                    .addPropertyNode("newBrandName")
                    .addConstraintViolation();
            valid = false;
        }

        return valid;
    }
}
