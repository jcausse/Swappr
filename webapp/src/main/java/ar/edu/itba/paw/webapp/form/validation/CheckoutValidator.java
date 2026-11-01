package ar.edu.itba.paw.webapp.form.validation;

import ar.edu.itba.paw.webapp.form.CheckoutForm;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.math.BigDecimal;
import java.util.Locale;

@RequiredArgsConstructor
public class CheckoutValidator implements ConstraintValidator<ValidCheckout, CheckoutForm> {

    private final MessageSource messageSource;

    @Override
    public boolean isValid(CheckoutForm form, ConstraintValidatorContext context) {
        if (form == null) {
            return true;
        }

        boolean valid = true;
        final Locale locale = LocaleContextHolder.getLocale();

        // Validate offerType
        if (form.getOfferType() == null || form.getOfferType().isBlank()) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(messageSource.getMessage("NotNull.checkoutForm.offerType", null, locale))
                    .addPropertyNode("offerType")
                    .addConstraintViolation();
            valid = false;
        } else if (!"full".equals(form.getOfferType()) && !"custom".equals(form.getOfferType()) && !"trade".equals(form.getOfferType())) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(messageSource.getMessage("Invalid.checkoutForm.offerType", null, locale))
                    .addPropertyNode("offerType")
                    .addConstraintViolation();
            valid = false;
        }

        // Validate customAmount when offerType is "custom"
        if ("custom".equals(form.getOfferType())) {
            if (form.getCustomAmount() == null) {
                context.disableDefaultConstraintViolation();
                context.buildConstraintViolationWithTemplate(messageSource.getMessage("NotNull.checkoutForm.customAmount", null, locale))
                        .addPropertyNode("customAmount")
                        .addConstraintViolation();
                valid = false;
            } else if (form.getCustomAmount().compareTo(BigDecimal.ZERO) <= 0) {
                context.disableDefaultConstraintViolation();
                context.buildConstraintViolationWithTemplate(messageSource.getMessage("NotNull.checkoutForm.customAmount", null, locale))
                        .addPropertyNode("customAmount")
                        .addConstraintViolation();
                valid = false;
            } else if (form.getListingPrice() != null && form.getCustomAmount().compareTo(form.getListingPrice()) > 0) {
                context.disableDefaultConstraintViolation();
                context.buildConstraintViolationWithTemplate(messageSource.getMessage("Max.checkoutForm.customAmount", new Object[]{form.getListingPrice()}, locale))
                        .addPropertyNode("customAmount")
                        .addConstraintViolation();
                valid = false;
            }
        }

        // Validate trade offer fields when offerType is "trade"
        if ("trade".equals(form.getOfferType())) {
            if (form.getOfferedListingId() == null) {
                context.disableDefaultConstraintViolation();
                context.buildConstraintViolationWithTemplate(messageSource.getMessage("NotNull.checkoutForm.offeredListingId", null, locale))
                        .addPropertyNode("offeredListingId")
                        .addConstraintViolation();
                valid = false;
            }
            if (form.getTradeAmount() != null && form.getTradeAmount().compareTo(BigDecimal.ZERO) < 0) {
                context.disableDefaultConstraintViolation();
                context.buildConstraintViolationWithTemplate(messageSource.getMessage("NotNull.checkoutForm.tradeAmount", null, locale))
                        .addPropertyNode("tradeAmount")
                        .addConstraintViolation();
                valid = false;
            } else if (form.getTradeAmount() != null && form.getListingPrice() != null && form.getTradeAmount().compareTo(form.getListingPrice()) > 0) {
                context.disableDefaultConstraintViolation();
                context.buildConstraintViolationWithTemplate(messageSource.getMessage("Max.checkoutForm.tradeAmount", new Object[]{form.getListingPrice()}, locale))
                        .addPropertyNode("tradeAmount")
                        .addConstraintViolation();
                valid = false;
            }
        }

        return valid;
    }
}