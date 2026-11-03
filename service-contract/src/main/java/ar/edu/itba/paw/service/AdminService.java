package ar.edu.itba.paw.service;

import ar.edu.itba.paw.model.Role;
import ar.edu.itba.paw.model.User;
import ar.edu.itba.paw.service.exception.SelfSuspendException;

public interface AdminService {

    void takeDownListing(Long listingId);

    void takeDownOffer(Long offerId);

    User grantRole(String usernameOrEmail, Role role);

    User suspendUser(User currentUser, String usernameOrEmail) throws SelfSuspendException;

    User unsuspendUser(String usernameOrEmail);
}

