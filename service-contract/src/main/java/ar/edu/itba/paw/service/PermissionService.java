package ar.edu.itba.paw.service;

import ar.edu.itba.paw.model.User;

public interface PermissionService {

    boolean canManageListing(User user, long listingId);
    boolean canManageOfferAsSeller(User user, long offerId);
    boolean canManageOfferAsBuyer(User user, long offerId);
    boolean canAccessOffer(User user, long offerId);
    boolean canManageNotification(User user, long notificationId);
}
