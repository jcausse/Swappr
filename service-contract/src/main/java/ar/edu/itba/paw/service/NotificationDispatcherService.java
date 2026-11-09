package ar.edu.itba.paw.service;

import ar.edu.itba.paw.model.Listing;
import ar.edu.itba.paw.model.Offer;
import ar.edu.itba.paw.model.User;
import ar.edu.itba.paw.model.Brand;
import ar.edu.itba.paw.model.BrandModelRequest;
import ar.edu.itba.paw.model.Model;
import lombok.NonNull;

public interface NotificationDispatcherService {

    /** Notify a newly verified user with a welcome notification. */
    void notifyWelcome(@NonNull User user);

    /** Notify the seller that their listing has been published. */
    void notifyListingPublished(@NonNull Listing listing);

    /** Notify the seller that their listing was sold. */
    void notifyListingSold(@NonNull Listing listing, @NonNull User buyer, String message);

    /** Notify the buyer that their purchase payment was confirmed by the seller. */
    void notifyPurchaseConfirmed(@NonNull Listing listing, @NonNull User buyer);

    /** Notify the seller that they received a new offer on their listing. */
    void notifyNewOffer(@NonNull Offer offer);

    /** Notify the buyer that their offer was rejected. */
    void notifyOfferRejected(@NonNull Offer offer);

    /** Notify the seller that an offer on their listing was withdrawn by the buyer. */
    void notifyOfferWithdrawn(@NonNull Offer offer);

    /** Notify the buyer that their offer was accepted and is pending payment. */
    void notifyOfferAccepted(@NonNull Offer offer);

    /** Notify the seller that their listing has an accepted offer pending payment. */
    void notifyPendingTransaction(@NonNull Offer offer);

    /** Notify the seller that proof of payment was uploaded by the buyer. */
    void notifyProofOfPaymentUploaded(@NonNull Offer offer);

    /** Notify the buyer that proof of shipping was uploaded by the seller. */
    void notifyProofOfShippingUploaded(@NonNull Offer offer);

    /** Send password recovery notification containing the one-time password code. */
    void notifyPasswordRecovery(@NonNull User user, @NonNull String otpValue);

    /** Send email verification notification containing the one-time password code. */
    void notifyEmailVerification(@NonNull User user, @NonNull String otpValue);

    /** Notify user that their account has been suspended by an administrator. */
    void notifyAccountSuspended(@NonNull User user);

    /** Notify user that their account has been unsuspended by an administrator. */
    void notifyAccountUnsuspended(@NonNull User user);

    /** Notify the requester that their brand/model request was accepted and the catalog item now exists. */
    void notifyBrandModelRequestAccepted(@NonNull BrandModelRequest request, @NonNull Brand brand, @NonNull Model model);

    /** Notify the requester that their brand/model request was rejected by an administrator. */
    void notifyBrandModelRequestRejected(@NonNull BrandModelRequest request);
}
