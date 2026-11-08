package ar.edu.itba.paw.model;

import lombok.Getter;

import java.util.function.Predicate;

@Getter
public enum NotificationType {
    // Multi-channel notifications (in-site + email)
    NEW_OFFER(true, true, offer -> offer == null || offer.isPending()),
    OFFER_ACCEPTED(true, true, offer -> offer != null && offer.isPendingPayment()),
    OFFER_REJECTED(true, true),
    OFFER_WITHDRAWN(true, true),
    PROOF_OF_PAYMENT_UPLOADED(true, true, offer -> offer != null && offer.isPendingPayment()),
    PROOF_OF_SHIPPING_UPLOADED(true, true, offer -> offer != null && offer.isPendingPayment()),
    PURCHASE_CONFIRMED(true, true),

    // Email-only notifications (self-triggered receipts, auth OTPs, account moderation)
    WELCOME(false, true),
    LISTING_PUBLISHED(false, true),
    PENDING_TRANSACTION(false, true, offer -> offer != null && offer.isPendingPayment()),
    LISTING_SOLD(false, true),
    PASSWORD_RECOVERY(false, true),
    EMAIL_VERIFICATION(false, true),
    ACCOUNT_SUSPENDED(false, true),
    ACCOUNT_UNSUSPENDED(false, true);

    private final boolean supportsSite;
    private final boolean supportsEmail;
    private final Predicate<Offer> callToActionPredicate;

    NotificationType(boolean supportsSite, boolean supportsEmail) {
        this(supportsSite, supportsEmail, offer -> true);
    }

    NotificationType(boolean supportsSite, boolean supportsEmail, Predicate<Offer> callToActionPredicate) {
        this.supportsSite = supportsSite;
        this.supportsEmail = supportsEmail;
        this.callToActionPredicate = callToActionPredicate != null ? callToActionPredicate : offer -> true;
    }

    public boolean supportsSite() {
        return supportsSite;
    }

    public boolean supportsEmail() {
        return supportsEmail;
    }

    public boolean shouldShowCallToAction(Offer offer) {
        return callToActionPredicate.test(offer);
    }
}
