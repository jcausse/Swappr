package ar.edu.itba.paw.model;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum NotificationType {
    // Multi-channel notifications (in-site + email)
    NEW_OFFER(true, true),
    OFFER_ACCEPTED(true, true),
    OFFER_REJECTED(true, true),
    OFFER_WITHDRAWN(true, true),
    PROOF_OF_PAYMENT_UPLOADED(true, true),
    PROOF_OF_SHIPPING_UPLOADED(true, true),
    PURCHASE_CONFIRMED(true, true),

    // Email-only notifications (self-triggered receipts, auth OTPs, account moderation)
    WELCOME(false, true),
    LISTING_PUBLISHED(false, true),
    PENDING_TRANSACTION(false, true),
    LISTING_SOLD(false, true),
    PASSWORD_RECOVERY(false, true),
    EMAIL_VERIFICATION(false, true),
    ACCOUNT_SUSPENDED(false, true),
    ACCOUNT_UNSUSPENDED(false, true);

    private final boolean supportsSite;
    private final boolean supportsEmail;

    public boolean supportsSite() {
        return supportsSite;
    }

    public boolean supportsEmail() {
        return supportsEmail;
    }
}
