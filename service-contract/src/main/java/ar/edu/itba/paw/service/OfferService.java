package ar.edu.itba.paw.service;

import ar.edu.itba.paw.model.Listing;
import ar.edu.itba.paw.model.Offer;
import ar.edu.itba.paw.model.File;
import ar.edu.itba.paw.model.Page;
import ar.edu.itba.paw.model.User;
import ar.edu.itba.paw.service.dto.FileDto;
import ar.edu.itba.paw.service.dto.OfferCreationDto;
import ar.edu.itba.paw.service.dto.OfferFilterDto;
import ar.edu.itba.paw.model.OfferRating;
import ar.edu.itba.paw.model.OfferStatusGroup;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface OfferService {

    Optional<Offer> getById(Long id, boolean populateTransientFlags);

    Offer getForDocumentUpload(Long offerId);

    Offer getRateableBy(Long offerId, User currentUser);

    Optional<Offer> getByListingAndBuyer(Listing listing, User buyer);

    Page<Offer> get(OfferFilterDto filter, boolean populateTransientFlags);

    Offer create(OfferCreationDto dto);

    Offer createInterest(Long listingId, Long buyerId);

    Offer accept(Long offerId, Long currentUserId);

    Offer reject(Long offerId, Long currentUserId);

    Offer withdraw(Long offerId, Long currentUserId);

    /**
     * Used by listing cancellation: rejects PENDING offers on the listing and, when
     * {@code includePendingPayment} is true (admin cancellation), PENDING_PAYMENT offers too.
     */
    void rejectPendingOffersForListing(Long listingId, Long exceptOfferId, boolean includePendingPayment);

    /**
     * Admin-cancellation edge case for OFFERED_IN_TRADE listings: silently rejects the trade
     * offers that reference this listing as their offered item, releasing any held target listings.
     */
    void rejectTradeOffersForOfferedListing(Long offeredListingId);

    int getPendingOffersCount(User seller);

    Map<OfferStatusGroup, Long> getOfferCountByStatus(User currentUser, Boolean incoming);

    Offer uploadProofOfPayment(Long offerId, Long buyerId, FileDto fileDto);

    Offer uploadProofOfShipping(Long offerId, Long sellerId, FileDto fileDto, String trackingNumber);

    Offer confirmPayment(Long offerId, Long sellerId);

    Optional<File> getProofOfPaymentFile(Long offerId, Long currentUserId);

    Optional<File> getProofOfShippingFile(Long offerId, Long currentUserId);

    Offer rate(Long offerId, User currentUser, OfferRating rating);

    Offer rate(Long offerId, User currentUser, OfferRating rating, String reviewText);

    @SuppressWarnings("unused")
    void autoRatePendingOffers();

    void cancelByAdmin(Long offerId);

    int rejectAllForParticipant(Long userId);
}
