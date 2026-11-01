package ar.edu.itba.paw.persistence;

import ar.edu.itba.paw.model.Listing;
import ar.edu.itba.paw.model.Offer;
import ar.edu.itba.paw.model.OfferFilter;
import ar.edu.itba.paw.model.OfferStatus;
import ar.edu.itba.paw.model.Page;
import ar.edu.itba.paw.model.User;
import ar.edu.itba.paw.model.OfferRating;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface OfferDao {

    Optional<Offer> getById(Long id);

    Optional<Offer> getByListingAndBuyer(Listing listing, User buyer);

    Page<Offer> search(OfferFilter filter);

    Offer create(Long listingId, User buyer, BigDecimal amount, Boolean isFullPrice, OfferStatus status, String message, Instant createdAt, Long offeredListingId);

    boolean updateStatus(Long offerId, OfferStatus status);

    boolean withdraw(Long offerId, Long buyerId);

    List<Offer> rejectPendingOffers(Long listingId, Long exceptOfferId);

    int countPendingBySeller(User seller);

    Map<String, Long> countByStatus(User currentUser, Boolean incoming);

    boolean updateProofOfPaymentId(Long offerId, Long proofOfPaymentId);

    boolean updateProofOfShipping(Long offerId, Long proofOfShippingId, String trackingNumber);

    boolean markAccepted(Long offerId, Instant acceptedAt);

    boolean setSellerRating(Long offerId, OfferRating rating);

    boolean setBuyerRating(Long offerId, OfferRating rating);

    List<Offer> getAcceptedUnratedBefore(Instant cutoff);
}
