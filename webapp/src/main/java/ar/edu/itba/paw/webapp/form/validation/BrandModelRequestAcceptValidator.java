package ar.edu.itba.paw.webapp.form.validation;

import ar.edu.itba.paw.model.BrandModelRequestType;
import ar.edu.itba.paw.webapp.form.AdminBrandModelRequestAcceptForm;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class BrandModelRequestAcceptValidator
        implements ConstraintValidator<ValidBrandModelRequestAccept, AdminBrandModelRequestAcceptForm> {

    @Override
    public boolean isValid(AdminBrandModelRequestAcceptForm form, ConstraintValidatorContext context) {
        if (form == null) {
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
