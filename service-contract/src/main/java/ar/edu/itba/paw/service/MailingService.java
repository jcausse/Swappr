package ar.edu.itba.paw.service;

import ar.edu.itba.paw.model.Listing;
import ar.edu.itba.paw.model.Offer;
import ar.edu.itba.paw.model.User;

public interface MailingService {

    /** Send a welcome email to a newly registered user. */
    void sendWelcomeEmail(User user);

    /** Notify the seller that their listing has been published. */
    void sendListingPublishedEmail(User seller, Listing listing);

    /** Notify the seller that their listing was purchased, including the buyer contact email and message. */
    void sendPurchaseSellerEmail(User seller, User buyer, Listing listing, String message);

    /** Confirm the purchase to the buyer, including the seller contact email. */
    void sendPurchaseBuyerEmail(User buyer, User seller, Listing listing);

    /** Notify the seller that they received a new offer on their listing. */
    void sendNewOfferEmail(Long offerId);

    /** Notify the buyer that their offer was rejected. */
    void sendOfferRejectedEmail(Long offerId);

    /** Notify the seller that an offer on their listing was withdrawn by the buyer. */
    void sendOfferWithdrawnEmail(User seller, User buyer, Listing listing, Offer offer);

    /** Notify the buyer that their offer was accepted and is pending payment. */
    void sendOfferPendingPaymentEmail(Long offerId);

    /** Notify the seller that their listing has a pending transaction. */
    void sendPendingTransactionEmail(Long offerId);

    /** Notify the seller that proof of payment was uploaded. */
    void sendProofOfPaymentUploadedEmail(User seller, User buyer, Listing listing, Offer offer);

    /** Notify the buyer that proof of shipping was uploaded. */
    void sendProofOfShippingUploadedEmail(User buyer, User seller, Listing listing, Offer offer);

    /** Send password recovery email containing the one-time password code. */
    void sendPasswordRecoveryEmail(User user, String otpValue);

    /** Send email verification email containing the one-time password code. */
    void sendVerificationEmail(User user, String otpValue);

    /** Notify user that their account has been suspended by an administrator. */
    void sendAccountSuspendedEmail(User user);

    /** Notify user that their account has been unsuspended by an administrator. */
    void sendAccountUnsuspendedEmail(User user);
}
