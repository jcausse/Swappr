package ar.edu.itba.paw.webapp.form.validation;

import ar.edu.itba.paw.model.OfferType;
import ar.edu.itba.paw.webapp.form.CheckoutForm;
import ar.edu.itba.paw.service.ListingService;
import ar.edu.itba.paw.service.exception.NotFoundException;
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
    private final ListingService listingService;

    @Override
    public boolean isValid(CheckoutForm form, ConstraintValidatorContext context) {
        if (form == null) {
            return true;
        }

        boolean valid = true;
        final Locale locale = LocaleContextHolder.getLocale();
        final var listingPrice = resolveListingPrice(form.getListingId());

        // Validate offerType
        if (form.getOfferType() == null) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(messageSource.getMessage("NotNull.checkoutForm.offerType", null, locale))
                    .addPropertyNode("offerType")
                    .addConstraintViolation();
            valid = false;
        }

        // Validate customAmount when offerType is custom
        if (form.getOfferType() == OfferType.CUSTOM) {
            if (form.getCustomAmount() == null) {
                context.disableDefaultConstraintViolation();
                context.buildConstraintViolationWithTemplate(messageSource.getMessage("NotNull.checkoutForm.customAmount", null, locale))
                        .addPropertyNode("customAmount")
                        .addConstraintViolation();
                valid = false;
            } else if (listingPrice != null && form.getCustomAmount().compareTo(listingPrice) > 0) {
                context.disableDefaultConstraintViolation();
                context.buildConstraintViolationWithTemplate(messageSource.getMessage("Max.checkoutForm.customAmount", new Object[]{listingPrice}, locale))
                        .addPropertyNode("customAmount")
                        .addConstraintViolation();
                valid = false;
            }
        }

        // Validate trade offer fields when offerType is trade
        if (form.getOfferType() == OfferType.TRADE) {
            if (form.getOfferedListingId() == null) {
                context.disableDefaultConstraintViolation();
                context.buildConstraintViolationWithTemplate(messageSource.getMessage("NotNull.checkoutForm.offeredListingId", null, locale))
                        .addPropertyNode("offeredListingId")
                        .addConstraintViolation();
                valid = false;
            }
            if (listingPrice != null && form.getTradeAmount() != null && form.getTradeAmount().compareTo(listingPrice) > 0) {
                context.disableDefaultConstraintViolation();
                context.buildConstraintViolationWithTemplate(messageSource.getMessage("Max.checkoutForm.tradeAmount", new Object[]{listingPrice}, locale))
                        .addPropertyNode("tradeAmount")
                        .addConstraintViolation();
                valid = false;
            }
        }

        return valid;
    }

    private BigDecimal resolveListingPrice(final Long listingId) {
        if (listingId == null) {
            return null;
        }
        try {
            return listingService.getById(listingId).getPrice().getAmount();
        } catch (NotFoundException e) {
            return null;
        }
    }
}
