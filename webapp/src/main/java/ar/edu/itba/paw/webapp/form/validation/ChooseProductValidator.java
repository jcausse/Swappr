package ar.edu.itba.paw.webapp.form.validation;

import ar.edu.itba.paw.model.Listing;
import ar.edu.itba.paw.webapp.form.ChooseProductForm;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.time.Year;

@RequiredArgsConstructor
public class ChooseProductValidator implements ConstraintValidator<ValidChooseProduct, ChooseProductForm> {


    private final MessageSource messageSource;

    @Override
    public boolean isValid(ChooseProductForm form, ConstraintValidatorContext context) {
        if (form == null) {
            return true;
        }

        // Auto-submits are triggered by dropdown change events to dynamically load options; skip validation
        if (Boolean.TRUE.equals(form.getIsAutoSubmit())) {
            return true;
        }

        // Field change resets; skip validation
        if (form.isCategoryChanged() || form.isSubcategoryChanged()) {
            return true;
        }

        int step = form.getStep() != null ? form.getStep() : 1;
        boolean valid = true;

        if (step == 1) {
            if (form.getCategoryId() == null) {
                context.disableDefaultConstraintViolation();
                context.buildConstraintViolationWithTemplate("{NotNull.chooseProductForm.categoryId}")
                        .addPropertyNode("categoryId")
                        .addConstraintViolation();
                valid = false;
            }
        } else if (step == 2) {
            if (form.getSubcategoryId() == null) {
                context.disableDefaultConstraintViolation();
                context.buildConstraintViolationWithTemplate("{NotNull.chooseProductForm.subcategoryId}")
                        .addPropertyNode("subcategoryId")
                        .addConstraintViolation();
                valid = false;
            }
        } else if (step == 3) {
            final Long brandId = form.getBrandId();
            final Long modelId = form.getModelId();
            final Integer year = form.getYear();

            context.disableDefaultConstraintViolation();

            if (brandId == null) {
                context.buildConstraintViolationWithTemplate("{NotNull.chooseProductForm.brandId}")
                        .addPropertyNode("brandId")
                        .addConstraintViolation();
                valid = false;
            }
            if (modelId == null) {
                context.buildConstraintViolationWithTemplate("{NotNull.chooseProductForm.modelId}")
                        .addPropertyNode("modelId")
                        .addConstraintViolation();
                valid = false;
            }

            int currentYear = Year.now().getValue();
            if (year == null) {
                context.buildConstraintViolationWithTemplate("{NotNull.chooseProductForm.year}")
                        .addPropertyNode("year")
                        .addConstraintViolation();
                valid = false;
            } else if (!Listing.isValidYear(year)) {
                final String message = messageSource.getMessage(
                    "Range.chooseProductForm.year",
                    new Object[]{String.valueOf(Listing.MIN_YEAR), String.valueOf(currentYear)},
                    LocaleContextHolder.getLocale()
                );
                context.buildConstraintViolationWithTemplate(message)
                        .addPropertyNode("year")
                        .addConstraintViolation();
                valid = false;
            }
        }

        return valid;
    }
}
