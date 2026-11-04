package ar.edu.itba.paw.service;

import ar.edu.itba.paw.model.Listing;

import ar.edu.itba.paw.model.ListingStatus;
import ar.edu.itba.paw.model.Offer;
import ar.edu.itba.paw.model.OfferStatusGroup;
import ar.edu.itba.paw.model.Page;
import ar.edu.itba.paw.model.Role;
import ar.edu.itba.paw.model.User;
import ar.edu.itba.paw.service.dto.ListingFilterDto;
import ar.edu.itba.paw.service.dto.OfferFilterDto;
import ar.edu.itba.paw.service.exception.NotFoundException;
import ar.edu.itba.paw.service.exception.SelfSuspendException;
import java.util.List;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
@Transactional(readOnly = true)
public class AdminServiceImpl implements AdminService {

    private static final Logger LOGGER = LoggerFactory.getLogger(AdminServiceImpl.class);

    private final ListingService listingService;
    private final OfferService offerService;
    private final UserService userService;

    @Override
    @Transactional
    public void takeDownListing(@NonNull Long listingId) {
        LOGGER.info("Admin taking down listing id={}", listingId);
        listingService.cancelByAdmin(listingId);
    }

    @Override
    @Transactional
    public void takeDownOffer(@NonNull Long offerId) {
        LOGGER.info("Admin taking down offer id={}", offerId);
        offerService.cancelByAdmin(offerId);
    }

    @Override
    @Transactional
    public User grantRole(@NonNull String usernameOrEmail, @NonNull Role role) {
        LOGGER.info("Admin granting role {} to user '{}'", role, usernameOrEmail);
        final var user = userService.getByUsernameOrEmail(usernameOrEmail)
                .orElseThrow(() -> {
                    LOGGER.warn("User '{}' not found for granting role {}", usernameOrEmail, role);
                    return NotFoundException.createFor("User");
                });

        userService.addRole(user, role);
        return user;
    }

    // TODO: Bro, this is just AI slop
    @Override
    @Transactional
    public User suspendUser(@NonNull User currentUser, @NonNull String usernameOrEmail) throws SelfSuspendException {
        LOGGER.info("Admin {} suspending user '{}'", currentUser.getUsername(), usernameOrEmail);
        if (currentUser.matchesUsernameOrEmail(usernameOrEmail)) {
            LOGGER.warn("Admin {} prevented from suspending theirself", currentUser.getUsername());
            throw new SelfSuspendException("An admin cannot suspend their own account");
        }

        final var user = userService.getByUsernameOrEmail(usernameOrEmail)
                .orElseThrow(() -> {
                    LOGGER.warn("User '{}' not found for suspension", usernameOrEmail);
                    return NotFoundException.createFor("User");
                });

        final User suspendedUser = userService.suspend(user.getId());

        // Cancel all active listings of the user (also automatically rejects pending offers on them)
        try {
            final Page<Listing> activeListings = listingService.search(
                    ListingFilterDto.builder()
                            .creatorId(user.getId())
                            .status(ListingStatus.ACTIVE)
                            .page(1)
                            .pageSize(1000)
                            .build()
            );
            for (Listing listing : activeListings.getContent()) {
                try {
                    listingService.cancelByAdmin(listing.getId());
                } catch (Exception e) {
                    LOGGER.warn("Failed to cancel listing {} during suspension of user {}: {}",
                            listing.getId(), user.getId(), e.getMessage());
                }
            }
        } catch (Exception e) {
            LOGGER.warn("Failed to retrieve listings for user {}: {}", user.getId(), e.getMessage());
        }

        // Cancel all pending and pending-payment offers made by the user
        for (OfferStatusGroup group : List.of(OfferStatusGroup.PENDING, OfferStatusGroup.PENDING_PAYMENT)) {
            try {
                final Page<Offer> buyerOffers = offerService.get(
                        OfferFilterDto.builder()
                                .buyerId(user.getId())
                                .statusGroup(group)
                                .page(1)
                                .pageSize(1000)
                                .build()
                );
                for (Offer offer : buyerOffers.getContent()) {
                    try {
                        offerService.cancelByAdmin(offer.getId());
                    } catch (Exception e) {
                        LOGGER.warn("Failed to cancel buyer offer {} during suspension of user {}: {}",
                                offer.getId(), user.getId(), e.getMessage());
                    }
                }
            } catch (Exception e) {
                LOGGER.warn("Failed to retrieve buyer offers for user {} in group {}: {}",
                        user.getId(), group, e.getMessage());
            }
        }

        // Cancel any pending-payment offers where the user is the seller (releasing buyers)
        try {
            final Page<Offer> sellerPendingPaymentOffers = offerService.get(
                    OfferFilterDto.builder()
                            .sellerId(user.getId())
                            .statusGroup(OfferStatusGroup.PENDING_PAYMENT)
                            .page(1)
                            .pageSize(1000)
                            .build()
            );
            for (Offer offer : sellerPendingPaymentOffers.getContent()) {
                try {
                    offerService.cancelByAdmin(offer.getId());
                } catch (Exception e) {
                    LOGGER.warn("Failed to cancel seller pending-payment offer {} during suspension of user {}: {}",
                            offer.getId(), user.getId(), e.getMessage());
                }
            }
        } catch (Exception e) {
            LOGGER.warn("Failed to retrieve seller pending payment offers for user {}: {}", user.getId(), e.getMessage());
        }

        return suspendedUser;
    }

    @Override
    @Transactional
    public User unsuspendUser(@NonNull String usernameOrEmail) {
        LOGGER.info("Admin unsuspending user '{}'", usernameOrEmail);
        final var user = userService.getByUsernameOrEmail(usernameOrEmail)
                .orElseThrow(() -> {
                    LOGGER.warn("User '{}' not found for unsuspension", usernameOrEmail);
                    return NotFoundException.createFor("User");
                });

        return userService.unsuspend(user.getId());
    }
}

