package ar.edu.itba.paw.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ar.edu.itba.paw.model.ListingStatus;
import ar.edu.itba.paw.model.User;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class PermissionServiceImpl implements PermissionService {
    private final ListingService listingService;
    private final OfferService offerService;

	@Override
	@Transactional
	public boolean canManageListing(User user, long listingId) {
    	var listing = listingService.getById(listingId);
		return isListingCreator(user, listingId) && listing.getStatus() != ListingStatus.CANCELED;
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
	@Transactional
	public boolean isListingCreator(User user, long listingId) {
    	var listing = listingService.getById(listingId);
    	return user.getId().equals(listing.getCreator().getId());
	}

	@Override
	@Transactional
	public boolean isOfferSeller(User user, long offerId) {
		var offer = offerService.getById(offerId);
        return offer.filter(value -> user.getId().equals(value.getListing().getCreator().getId())).isPresent();
    }

	@Override
	@Transactional
	public boolean isOfferBuyer(User user, long offerId) {
    	var offer = offerService.getById(offerId);
        return offer.filter(value -> user.getId().equals(value.getBuyer().getId())).isPresent();
    }
}
