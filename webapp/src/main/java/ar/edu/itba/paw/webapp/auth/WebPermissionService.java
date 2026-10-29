package ar.edu.itba.paw.webapp.auth;

import java.util.Optional;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import ar.edu.itba.paw.model.User;
import ar.edu.itba.paw.service.PermissionService;
import lombok.RequiredArgsConstructor;

// Adapter for PermissionService
@Service("webPermissionService")
@RequiredArgsConstructor
public class WebPermissionService {
	private final PermissionService permissionService;

	private Optional<User> getCurrentUser(final Authentication auth) {
        if (auth == null || !(auth.getPrincipal() instanceof AuthUserDetails)) {
            return Optional.empty();
        }
        return Optional.of(((AuthUserDetails) auth.getPrincipal()).getDomainUser());
	}

	public boolean canModifyListing(final Authentication authentication, final long listingId) {
	    return getCurrentUser(authentication).map(u -> permissionService.canModifyListing(u, listingId)).orElse(false);
	}

	public boolean canManageOfferAsSeller(final Authentication authentication, final long offerId) {
	    return getCurrentUser(authentication).map(u -> permissionService.canManageOfferAsSeller(u, offerId)).orElse(false);
	}

	public boolean canManageOfferAsBuyer(final Authentication authentication, final long offerId) {
	    return getCurrentUser(authentication).map(u -> permissionService.canManageOfferAsBuyer(u, offerId)).orElse(false);
	}

	public boolean canAccessOffer(final Authentication authentication, final long offerId) {
	    return getCurrentUser(authentication).map(u -> permissionService.canAccessOffer(u, offerId)).orElse(false);
	}
}
