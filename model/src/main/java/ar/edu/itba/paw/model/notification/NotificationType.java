package ar.edu.itba.paw.model.notification;

import ar.edu.itba.paw.model.Offer;
import lombok.Getter;

import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;

@Getter
public enum NotificationType {
    // Multi-channel notifications (in-site + email)
    NEW_OFFER(NotificationMode.SITE_AND_EMAIL, true, offer -> offer == null || offer.isPending()),
    OFFER_ACCEPTED(NotificationMode.SITE_AND_EMAIL, true, offer -> offer != null && offer.isPendingPayment()),
    OFFER_REJECTED(NotificationMode.SITE_AND_EMAIL, true),
    OFFER_WITHDRAWN(NotificationMode.SITE_AND_EMAIL, true),
    PROOF_OF_PAYMENT_UPLOADED(NotificationMode.SITE_AND_EMAIL, true, offer -> offer != null && offer.isPendingPayment()),
    PROOF_OF_SHIPPING_UPLOADED(NotificationMode.SITE_AND_EMAIL, true, offer -> offer != null && offer.isPendingPayment()),
    PURCHASE_CONFIRMED(NotificationMode.SITE_AND_EMAIL, true),
    BRAND_MODEL_REQUEST_ACCEPTED(NotificationMode.SITE_AND_EMAIL, true),
    BRAND_MODEL_REQUEST_REJECTED(NotificationMode.SITE_AND_EMAIL, true),

    // Configurable email-only notifications (self-triggered receipts & transaction updates)
    LISTING_PUBLISHED(NotificationMode.EMAIL_ONLY, true),
    PENDING_TRANSACTION(NotificationMode.EMAIL_ONLY, true, offer -> offer != null && offer.isPendingPayment()),
    LISTING_SOLD(NotificationMode.EMAIL_ONLY, true),

    // Non-configurable email-only notifications (onboarding, auth OTPs, account moderation)
    WELCOME(NotificationMode.EMAIL_ONLY, false),
    PASSWORD_RECOVERY(NotificationMode.EMAIL_ONLY, false),
    EMAIL_VERIFICATION(NotificationMode.EMAIL_ONLY, false),
    ACCOUNT_SUSPENDED(NotificationMode.EMAIL_ONLY, false),
    ACCOUNT_UNSUSPENDED(NotificationMode.EMAIL_ONLY, false);

    private final NotificationMode defaultMode;
    private final boolean configurable;
    private final Predicate<Offer> callToActionPredicate;

    NotificationType(NotificationMode defaultMode, boolean configurable) {
        this(defaultMode, configurable, offer -> true);
    }

    NotificationType(
            NotificationMode defaultMode,
            boolean configurable,
            Predicate<Offer> callToActionPredicate
    ) {
        this.defaultMode = defaultMode;
        this.configurable = configurable;
        this.callToActionPredicate = callToActionPredicate;
    }

    public boolean supportsSite() {
        return defaultMode.isSiteEnabled();
    }

    public boolean supportsEmail() {
        return defaultMode.isEmailEnabled();
    }

    public boolean isModeSupported(NotificationMode mode) {
        if (mode == null || !configurable) {
            return false;
        }
        return (!mode.isSiteEnabled() || supportsSite())
                && (!mode.isEmailEnabled() || supportsEmail());
    }

    public List<NotificationMode> getSupportedModes() {
        if (!configurable) {
            return List.of();
        }
        return Arrays.stream(NotificationMode.values())
                .filter(this::isModeSupported)
                .toList();
    }

    public static List<NotificationType> getConfigurableTypes() {
        return Arrays.stream(values())
                .filter(NotificationType::isConfigurable)
                .toList();
    }

    public boolean shouldShowCallToAction(Offer offer) {
        return callToActionPredicate.test(offer);
    }
}
