package ar.edu.itba.paw.webapp.form.validation;

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
            String brand = form.getNewProductBrand();
            String model = form.getNewProductModel();
            String otherBrand = form.getOtherBrand();
            String otherModel = form.getOtherModel();
            Integer year = form.getNewProductYear();

            boolean hasBrand = brand != null && !brand.isBlank();
            boolean hasModel = model != null && !model.isBlank();

            context.disableDefaultConstraintViolation();

            if (!hasBrand) {
                context.buildConstraintViolationWithTemplate("{NotNull.chooseProductForm.newProductBrand}")
                        .addPropertyNode("newProductBrand")
                        .addConstraintViolation();
                valid = false;
            } else if (form.isOtherBrandSelected()) {
                if (otherBrand == null || otherBrand.isBlank()) {
                    context.buildConstraintViolationWithTemplate("{NotNull.chooseProductForm.otherBrand}")
                            .addPropertyNode("otherBrand")
                            .addConstraintViolation();
                    valid = false;
                }
                if (otherModel == null || otherModel.isBlank()) {
                    context.buildConstraintViolationWithTemplate("{NotNull.chooseProductForm.otherModel}")
                            .addPropertyNode("otherModel")
                            .addConstraintViolation();
                    valid = false;
                }
            } else {
                if (!hasModel) {
                    context.buildConstraintViolationWithTemplate("{NotNull.chooseProductForm.newProductModel}")
                            .addPropertyNode("newProductModel")
                            .addConstraintViolation();
                    valid = false;
                } else if (form.isOtherModelSelected()) {
                    if (otherModel == null || otherModel.isBlank()) {
                        context.buildConstraintViolationWithTemplate("{NotNull.chooseProductForm.otherModel}")
                                .addPropertyNode("otherModel")
                                .addConstraintViolation();
                        valid = false;
                    }
                }
            }

            int currentYear = Year.now().getValue();
            if (year == null) {
                context.buildConstraintViolationWithTemplate("{NotNull.chooseProductForm.newProductYear}")
                        .addPropertyNode("newProductYear")
                        .addConstraintViolation();
                valid = false;
            } else if (year < 1900 || year > currentYear) {
                final String message = messageSource.getMessage(
                    "Range.chooseProductForm.newProductYear",
                    new Object[]{String.valueOf(1900), String.valueOf(currentYear)},
                    LocaleContextHolder.getLocale()
                );
                context.buildConstraintViolationWithTemplate(message)
                        .addPropertyNode("newProductYear")
                        .addConstraintViolation();
                valid = false;
            }
        }

        return valid;
    }
}
