package ar.edu.itba.paw.webapp.form.validation;

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

        context.disableDefaultConstraintViolation();
        return BrandModelRequestBrandRule.isValid(form, context);
    }
}
