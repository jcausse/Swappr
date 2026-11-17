package ar.edu.itba.paw.service;

import ar.edu.itba.paw.model.User;
import ar.edu.itba.paw.service.enumeration.OneTimePasswordVerificationResult;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Optional;

import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class PasswordRecoveryServiceImplTest {

    private static final long USER_ID = 1L;
    private static final String USERNAME_OR_EMAIL = "user@example.com";
    private static final String PASSWORD = "new-password";
    private static final String OTP = "ABC123";

    @InjectMocks
    private PasswordRecoveryServiceImpl passwordRecoveryService;

    @Mock
    private UserService userService;
    @Mock
    private OneTimePasswordService otpService;
    @Mock
    private NotificationDispatcherService notificationDispatcherService;

    private User buildUser() {
        return User.builder().id(USER_ID).username("user").email(USERNAME_OR_EMAIL).build();
    }

    @Test
    public void testStartRecoveryForUnknownUserReturnsEmpty() {
        when(userService.getByUsernameOrEmail(USERNAME_OR_EMAIL)).thenReturn(Optional.empty());

        Assert.assertTrue(passwordRecoveryService.startAndSendRecoveryEmail(USERNAME_OR_EMAIL).isEmpty());
    }

    @Test
    public void testVerificationUpdatesPasswordWhenOtpAccepted() {
        final User user = buildUser();
        when(userService.getByUsernameOrEmail(USERNAME_OR_EMAIL)).thenReturn(Optional.of(user));
        when(otpService.verify(user, OTP)).thenReturn(OneTimePasswordVerificationResult.ACCEPTED);
        when(userService.update(org.mockito.ArgumentMatchers.any())).thenReturn(user);

        final var result = passwordRecoveryService.verifyAndUpdatePassword(USERNAME_OR_EMAIL, PASSWORD, OTP);

        Assert.assertEquals(OneTimePasswordVerificationResult.ACCEPTED, result);
    }

    @Test
    public void testVerificationForUnknownUserIsRejected() {
        when(userService.getByUsernameOrEmail(USERNAME_OR_EMAIL)).thenReturn(Optional.empty());

        Assert.assertEquals(
            OneTimePasswordVerificationResult.REJECTED,
            passwordRecoveryService.verifyAndUpdatePassword(USERNAME_OR_EMAIL, PASSWORD, OTP));
    }
}
