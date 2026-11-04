package ar.edu.itba.paw.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ar.edu.itba.paw.model.User;
import ar.edu.itba.paw.service.exception.NotFoundException;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class PermissionServiceImpl implements PermissionService {
    private final ListingService listingService;
    private final OfferService offerService;
    private final SiteNotificationService siteNotificationService;

	@Override
	@Transactional
	public boolean canManageListing(User user, long listingId) {
    	var listing = listingService.getById(listingId);
		return listing.isCreator(user) && !listing.isCanceled();
	}

	@Override
	@Transactional
	public boolean canManageOfferAsSeller(User user, long offerId) {
		return isOfferSeller(user, offerId);
	}

	@Override
	@Transactional
	public boolean canManageOfferAsBuyer(User user, long offerId) {
		return isOfferBuyer(user, offerId);
	}

	@Override
	@Transactional
	public boolean canAccessOffer(User user, long offerId) {
		return isOfferSeller(user, offerId) || isOfferBuyer(user, offerId);
	}

	@Override
	@Transactional(readOnly = true)
	public boolean canManageNotification(User user, long notificationId) {
		var notification = siteNotificationService.getById(notificationId)
				.orElseThrow(() -> NotFoundException.createFor("Notification with ID " + notificationId));
		return notification.isRecipient(user);
	}

	@Override
	@Transactional
	public boolean isListingCreator(User user, long listingId) {
        var listing = listingService.getById(listingId);
        return listing.isCreator(user);
	}

	@Override
	@Transactional
	public boolean isOfferSeller(User user, long offerId) {
		var offer = offerService.getById(offerId);
        return offer.map(value -> value.isSeller(user)).orElse(false);
    }

	@Override
	@Transactional
	public boolean isOfferBuyer(User user, long offerId) {
    	var offer = offerService.getById(offerId);
        return offer.map(value -> value.isBuyer(user)).orElse(false);
    }
}
