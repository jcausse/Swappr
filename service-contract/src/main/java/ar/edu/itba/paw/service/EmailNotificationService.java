package ar.edu.itba.paw.service;

import ar.edu.itba.paw.service.dto.mail.ListingMailDto;
import ar.edu.itba.paw.service.dto.mail.OfferMailDto;
import ar.edu.itba.paw.service.dto.mail.UserMailDto;

public interface EmailNotificationService {

    /** Send a welcome email to a newly registered user. */
    void sendWelcomeEmail(UserMailDto user);

    /** Notify the seller that their listing has been published. */
    void sendListingPublishedEmail(UserMailDto seller, ListingMailDto listing);

    /** Notify the seller that their listing was purchased, including the buyer contact email and message. */
    void sendPurchaseSellerEmail(UserMailDto seller, UserMailDto buyer, ListingMailDto listing, String message);

    /** Confirm the purchase to the buyer, including the seller contact email. */
    void sendPurchaseBuyerEmail(UserMailDto buyer, UserMailDto seller, ListingMailDto listing);

    /** Notify the seller that they received a new offer on their listing. */
    void sendNewOfferEmail(OfferMailDto offer);

    /** Notify the buyer that their offer was rejected. */
    void sendOfferRejectedEmail(OfferMailDto offer);

    /** Notify the seller that an offer on their listing was withdrawn by the buyer. */
    void sendOfferWithdrawnEmail(OfferMailDto offer);

    /** Notify the buyer that their offer was accepted and is pending payment. */
    void sendOfferPendingPaymentEmail(OfferMailDto offer);

    /** Notify the seller that their listing has a pending transaction. */
    void sendPendingTransactionEmail(OfferMailDto offer);

    /** Notify the seller that proof of payment was uploaded. */
    void sendProofOfPaymentUploadedEmail(OfferMailDto offer);

    /** Notify the buyer that proof of shipping was uploaded. */
    void sendProofOfShippingUploadedEmail(OfferMailDto offer);

    /** Send password recovery email containing the one-time password code. */
    void sendPasswordRecoveryEmail(UserMailDto user, String otpValue);

    /** Send email verification email containing the one-time password code. */
    void sendVerificationEmail(UserMailDto user, String otpValue);

    /** Notify user that their account has been suspended by an administrator. */
    void sendAccountSuspendedEmail(UserMailDto user);

    /** Notify user that their account has been unsuspended by an administrator. */
    void sendAccountUnsuspendedEmail(UserMailDto user);
}
