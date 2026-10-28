package ar.edu.itba.paw.service;

import ar.edu.itba.paw.model.User;

public interface PermissionService {

	boolean canModifyListing(User user, long listingId);
	boolean canManageOfferAsSeller(User user, long offerId);
	boolean canManageOfferAsBuyer(User user, long offerId);
	boolean canAccessOffer(User user, long offerId);

	boolean isAdmin(User user);
	boolean isListingCreator(User user, long listingId);
	boolean isOfferSeller(User user, long offerId);
	boolean isOfferBuyer(User user, long offerId);
}
