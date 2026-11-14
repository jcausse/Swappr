package ar.edu.itba.paw.service;

import ar.edu.itba.paw.model.Role;
import ar.edu.itba.paw.model.User;
import ar.edu.itba.paw.persistence.ListingDao;
import ar.edu.itba.paw.persistence.OfferDao;
import ar.edu.itba.paw.service.exception.NotFoundException;
import ar.edu.itba.paw.service.exception.SelfSuspendException;
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

    private final ListingCancellationService listingCancellationService;
    private final OfferService offerService;
    private final UserService userService;
    private final ListingDao listingDao;
    private final OfferDao offerDao;

    @Override
    @Transactional
    public void takeDownListing(@NonNull Long listingId) {
        LOGGER.info("Admin taking down listing id={}", listingId);
        listingCancellationService.cancelByAdmin(listingId);
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

    @Override
    @Transactional
    public User suspendUser(@NonNull User currentUser, @NonNull String usernameOrEmail) throws SelfSuspendException {
        LOGGER.info("Admin id={} suspending user", currentUser.getId());

        // Prevent an admin from suspending their own account
        if (currentUser.matchesUsernameOrEmail(usernameOrEmail)) {
            LOGGER.warn("Admin id={} prevented from suspending their own account", currentUser.getId());
            throw new SelfSuspendException("An admin cannot suspend their own account");
        }

        // Resolve the target user before mutating any state
        final var user = userService.getByUsernameOrEmail(usernameOrEmail)
                .orElseThrow(() -> {
                    LOGGER.warn("User not found for suspension");
                    return NotFoundException.createFor("User");
                });
        final Long userId = user.getId();

        // Release trade listings offered in the offers that are about to be rejected
        final int releasedOfferedListings = listingDao.releaseOfferedListingsForParticipant(userId);

        // Release listings held by the user's pending-payment offers (target listings belong to other sellers)
        final int releasedHeldListings = listingDao.releaseListingsHeldByBuyerPaymentOffers(userId);

        // Silently reject every pending or pending-payment offer the user participates in
        final int rejectedOffers = offerDao.rejectAllForParticipant(userId);

        final User suspendedUser = userService.suspend(userId);

        // Cancel every listing still owned by the user, leaving completed sales untouched
        final int canceledListings = listingDao.cancelAllByCreator(userId);

        LOGGER.info("Suspension cleanup for user id={}: releasedOfferedListings={}, releasedHeldListings={}, "
                        + "rejectedOffers={}, canceledListings={}",
                userId, releasedOfferedListings, releasedHeldListings, rejectedOffers, canceledListings);

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
