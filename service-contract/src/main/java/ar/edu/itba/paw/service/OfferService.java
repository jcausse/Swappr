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

import java.util.List;
import java.util.Optional;

public interface OfferService {

    Optional<Offer> getById(Long id);

    Optional<Offer> getByListingAndBuyer(Listing listing, User buyer);

    Page<Offer> get(OfferFilterDto filter);

    Offer create(OfferCreationDto dto);

    Offer accept(Long offerId, Long currentUserId);

    Offer reject(Long offerId, Long currentUserId);

    Offer withdraw(Long offerId, Long currentUserId);

    List<Offer> rejectPendingOffersForListing(Long listingId, Long exceptOfferId);

    int getPendingOffersCount(User seller);

    Offer uploadProofOfPayment(Long offerId, Long buyerId, FileDto fileDto);

    Offer uploadProofOfShipping(Long offerId, Long sellerId, FileDto fileDto, String trackingNumber);

    Offer confirmPayment(Long offerId, Long sellerId);

    Optional<File> getProofOfPaymentFile(Long offerId, Long currentUserId);

    Optional<File> getProofOfShippingFile(Long offerId, Long currentUserId);

    Offer rate(Offer offer, User currentUser, OfferRating rating);

    Offer rate(Offer offer, User currentUser, OfferRating rating, String reviewText);

    @SuppressWarnings("unused")
    void autoRatePendingOffers();

    void cancelByAdmin(Long offerId);
}
