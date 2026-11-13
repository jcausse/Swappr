package ar.edu.itba.paw.model.notification;

import ar.edu.itba.paw.model.Offer;
import lombok.Getter;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

@Getter
public enum NotificationType {
    // Multi-channel notifications (in-site + email)
    NEW_OFFER(Set.of(NotificationChannel.SITE, NotificationChannel.EMAIL), true, offer -> offer == null || offer.isPending()),
    OFFER_ACCEPTED(Set.of(NotificationChannel.SITE, NotificationChannel.EMAIL), true, offer -> offer != null && offer.isPendingPayment()),
    OFFER_REJECTED(Set.of(NotificationChannel.SITE, NotificationChannel.EMAIL), true),
    OFFER_WITHDRAWN(Set.of(NotificationChannel.SITE, NotificationChannel.EMAIL), true),
    PROOF_OF_PAYMENT_UPLOADED(Set.of(NotificationChannel.SITE, NotificationChannel.EMAIL), true, offer -> offer != null && offer.isPendingPayment()),
    PROOF_OF_SHIPPING_UPLOADED(Set.of(NotificationChannel.SITE, NotificationChannel.EMAIL), true, offer -> offer != null && offer.isPendingPayment()),
    PURCHASE_CONFIRMED(Set.of(NotificationChannel.SITE, NotificationChannel.EMAIL), true),
    BRAND_MODEL_REQUEST_ACCEPTED(Set.of(NotificationChannel.SITE, NotificationChannel.EMAIL), true),
    BRAND_MODEL_REQUEST_REJECTED(Set.of(NotificationChannel.SITE, NotificationChannel.EMAIL), true),

    // Configurable email-only notifications (self-triggered receipts & transaction updates)
    LISTING_PUBLISHED(Set.of(NotificationChannel.EMAIL), true),
    PENDING_TRANSACTION(Set.of(NotificationChannel.EMAIL), true, offer -> offer != null && offer.isPendingPayment()),
    LISTING_SOLD(Set.of(NotificationChannel.EMAIL), true),

    // Non-configurable email-only notifications (onboarding, auth OTPs, account moderation)
    WELCOME(Set.of(NotificationChannel.EMAIL), false),
    PASSWORD_RECOVERY(Set.of(NotificationChannel.EMAIL), false),
    EMAIL_VERIFICATION(Set.of(NotificationChannel.EMAIL), false),
    ACCOUNT_SUSPENDED(Set.of(NotificationChannel.EMAIL), false),
    ACCOUNT_UNSUSPENDED(Set.of(NotificationChannel.EMAIL), false);

    private final Set<NotificationChannel> supportedChannels;
    private final boolean configurable;
    private final Predicate<Offer> callToActionPredicate;

    NotificationType(Set<NotificationChannel> supportedChannels, boolean configurable) {
        this(supportedChannels, configurable, offer -> true);
    }

    NotificationType(
            Set<NotificationChannel> supportedChannels,
            boolean configurable,
            Predicate<Offer> callToActionPredicate
    ) {
        this.supportedChannels = supportedChannels;
        this.configurable = configurable;
        this.callToActionPredicate = callToActionPredicate;
    }

    public boolean supportsChannel(NotificationChannel channel) {
        return channel != null && supportedChannels.contains(channel);
    }

    public boolean supportsSite() {
        return supportsChannel(NotificationChannel.SITE);
    }

    public boolean supportsEmail() {
        return supportsChannel(NotificationChannel.EMAIL);
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
