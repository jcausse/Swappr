package ar.edu.itba.paw.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ar.edu.itba.paw.model.User;
import ar.edu.itba.paw.service.exception.NotFoundException;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
@Transactional(readOnly = true)
public class PermissionServiceImpl implements PermissionService {
    private final ListingService listingService;
    private final OfferService offerService;
    private final SiteNotificationService siteNotificationService;

	@Override
	public boolean canManageListing(User user, long listingId) {
    	var listing = listingService.getById(listingId);
		return listing.isCreator(user) && !listing.isCanceled();
	}

	@Override
	public boolean canManageOfferAsSeller(User user, long offerId) {
		return offerService.getById(offerId)
				.map(offer -> offer.isSeller(user))
				.orElse(false);
	}

	@Override
	public boolean canManageOfferAsBuyer(User user, long offerId) {
		return offerService.getById(offerId)
				.map(offer -> offer.isBuyer(user))
				.orElse(false);
	}

	@Override
	public boolean canAccessOffer(User user, long offerId) {
		return offerService.getById(offerId)
				.map(offer -> offer.isSeller(user) || offer.isBuyer(user))
				.orElse(false);
	}

	@Override
	public boolean canManageNotification(User user, long notificationId) {
		var notification = siteNotificationService.getById(notificationId)
				.orElseThrow(() -> NotFoundException.createFor("Notification with ID " + notificationId));
		return notification.isRecipient(user);
	}
}
