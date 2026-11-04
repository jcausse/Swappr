package ar.edu.itba.paw.webapp.auth;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import ar.edu.itba.paw.service.PermissionService;
import lombok.RequiredArgsConstructor;

// Adapter for PermissionService
@Service("webPermissionService")
@RequiredArgsConstructor
public class WebPermissionService {
	private final PermissionService permissionService;
	private final AuthHelper authHelper;

	public boolean canManageListing(final Authentication authentication, final long listingId) {
	    return authHelper.getCurrentUser(authentication).map(u -> permissionService.canManageListing(u, listingId)).orElse(false);
	}

	public boolean canManageOfferAsSeller(final Authentication authentication, final long offerId) {
	    return authHelper.getCurrentUser(authentication).map(u -> permissionService.canManageOfferAsSeller(u, offerId)).orElse(false);
	}

	public boolean canManageOfferAsBuyer(final Authentication authentication, final long offerId) {
	    return authHelper.getCurrentUser(authentication).map(u -> permissionService.canManageOfferAsBuyer(u, offerId)).orElse(false);
	}

	public boolean canAccessOffer(final Authentication authentication, final long offerId) {
	    return authHelper.getCurrentUser(authentication).map(u -> permissionService.canAccessOffer(u, offerId)).orElse(false);
	}

	public boolean canManageNotification(final Authentication authentication, final long notificationId) {
	    return authHelper.getCurrentUser(authentication).map(u -> permissionService.canManageNotification(u, notificationId)).orElse(false);
	}
}
