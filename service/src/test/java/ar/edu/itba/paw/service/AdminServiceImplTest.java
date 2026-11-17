package ar.edu.itba.paw.service;

import ar.edu.itba.paw.model.Role;
import ar.edu.itba.paw.model.User;
import ar.edu.itba.paw.service.exception.NotFoundException;
import ar.edu.itba.paw.service.exception.SelfSuspendException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Optional;

import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class AdminServiceImplTest {

    private static final long ADMIN_ID = 1L;
    private static final String ADMIN_USERNAME = "admin";
    private static final String ADMIN_EMAIL = "admin@example.com";

    @InjectMocks
    private AdminServiceImpl adminService;

    @Mock
    private ListingCancellationService listingCancellationService;
    @Mock
    private OfferService offerService;
    @Mock
    private UserService userService;
    @Mock
    private ListingService listingService;

    private User buildAdmin() {
        return User.builder()
            .id(ADMIN_ID)
            .username(ADMIN_USERNAME)
            .email(ADMIN_EMAIL)
            .build();
    }

    @Test(expected = SelfSuspendException.class)
    public void testSuspendSelfThrows() throws SelfSuspendException {
        adminService.suspendUser(buildAdmin(), ADMIN_USERNAME);
    }

    @Test(expected = NotFoundException.class)
    public void testGrantRoleToMissingUserThrows() {
        when(userService.getByUsernameOrEmail("ghost")).thenReturn(Optional.empty());

        adminService.grantRole("ghost", Role.ADMIN);
    }
}
