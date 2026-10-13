package ar.edu.itba.paw.service;

import ar.edu.itba.paw.model.OneTimePassword;
import ar.edu.itba.paw.model.User;
import ar.edu.itba.paw.service.enumeration.OneTimePasswordVerificationResult;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.time.Instant;
import java.util.Optional;

import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class EmailVerificationServiceImplTest {

    private static final long USER_ID = 1L;
    private static final String USER_USERNAME = "fake_user";
    private static final String USER_DISPLAY_NAME = "Fake User";
    private static final String USER_EMAIL = "fake@example.com";
    private static final String USER_PASSWORD = "fake_password";
    private static final String UNKNOWN_USERNAME = "unknown_user";

    private static final String PLAIN_OTP = "Ab3dE5gH9k";

    @InjectMocks
    private EmailVerificationServiceImpl emailVerificationService;
    @Mock
    private UserService userService;
    @Mock
    private OneTimePasswordService otpService;
    @Mock
    private MailingService mailingService;

    /* ---------------------------------------------------------------------------------------------- */
    /* Fixtures & helpers                                                                              */
    /* ---------------------------------------------------------------------------------------------- */

    private User buildFakeUser(final boolean verified) {
        return User.builder()
            .id(USER_ID)
            .username(USER_USERNAME)
            .displayName(USER_DISPLAY_NAME)
            .email(USER_EMAIL)
            .password(USER_PASSWORD)
            .joinedAt(Instant.now())
            .emailVerifiedAt(verified ? Instant.now() : null)
            .build();
    }

    private OneTimePassword buildFakeOtp() {
        return OneTimePassword.builder()
            .id(USER_ID)
            .user(buildFakeUser(false))
            .otpValue(PLAIN_OTP)
            .createdAt(Instant.now())
            .build();
    }

    private User givenUser(final boolean verified) {
        final User user = buildFakeUser(verified);
        when(userService.getByUsernameOrEmail(eq(USER_USERNAME))).thenReturn(Optional.of(user));
        return user;
    }

    /* ---------------------------------------------------------------------------------------------- */
    /* sendVerificationEmail                                                                           */
    /* ---------------------------------------------------------------------------------------------- */

    @Test
    public void testSendVerificationEmailReturnsCreatedOtp() {
        // Arrange
        final User user = buildFakeUser(false);
        final OneTimePassword otp = buildFakeOtp();
        when(otpService.create(eq(user))).thenReturn(otp);

        // Act
        final OneTimePassword result = emailVerificationService.sendVerificationEmail(user);

        // Assert
        Assert.assertSame(otp, result);
    }

    @Test(expected = NullPointerException.class)
    public void testSendVerificationEmailNullUserThrows() {
        // Act
        emailVerificationService.sendVerificationEmail(null);
    }

    /* ---------------------------------------------------------------------------------------------- */
    /* resendVerificationEmail                                                                         */
    /* ---------------------------------------------------------------------------------------------- */

    @Test
    public void testResendVerificationEmailUnknownUserReturnsEmpty() {
        // Arrange
        when(userService.getByUsernameOrEmail(eq(UNKNOWN_USERNAME))).thenReturn(Optional.empty());

        // Act
        final Optional<OneTimePassword> result = emailVerificationService.resendVerificationEmail(UNKNOWN_USERNAME);

        // Assert
        Assert.assertFalse(result.isPresent());
    }

    @Test
    public void testResendVerificationEmailVerifiedUserReturnsEmpty() {
        // Arrange
        givenUser(true);

        // Act
        final Optional<OneTimePassword> result = emailVerificationService.resendVerificationEmail(USER_USERNAME);

        // Assert
        Assert.assertFalse(result.isPresent());
    }

    @Test
    public void testResendVerificationEmailUnverifiedUserReturnsNewOtp() {
        // Arrange
        final User user = givenUser(false);
        final OneTimePassword otp = buildFakeOtp();
        when(otpService.create(eq(user))).thenReturn(otp);

        // Act
        final Optional<OneTimePassword> result = emailVerificationService.resendVerificationEmail(USER_USERNAME);

        // Assert
        Assert.assertTrue(result.isPresent());
        Assert.assertSame(otp, result.get());
    }

    @Test(expected = NullPointerException.class)
    public void testResendVerificationEmailNullIdentifierThrows() {
        // Act
        emailVerificationService.resendVerificationEmail(null);
    }

    /* ---------------------------------------------------------------------------------------------- */
    /* verifyEmail                                                                                     */
    /* ---------------------------------------------------------------------------------------------- */

    @Test
    public void testVerifyEmailUnknownUserIsRejected() {
        // Arrange
        when(userService.getByUsernameOrEmail(eq(UNKNOWN_USERNAME))).thenReturn(Optional.empty());

        // Act
        final OneTimePasswordVerificationResult result = emailVerificationService.verifyEmail(UNKNOWN_USERNAME, PLAIN_OTP);

        // Assert
        Assert.assertEquals(OneTimePasswordVerificationResult.REJECTED, result);
    }

    @Test
    public void testVerifyEmailAlreadyVerifiedUserIsAccepted() {
        // Arrange
        givenUser(true);

        // Act
        final OneTimePasswordVerificationResult result = emailVerificationService.verifyEmail(USER_USERNAME, PLAIN_OTP);

        // Assert
        Assert.assertEquals(OneTimePasswordVerificationResult.ACCEPTED, result);
    }

    @Test
    public void testVerifyEmailCorrectOtpIsAccepted() {
        // Arrange
        final User user = givenUser(false);
        when(otpService.verify(eq(user), eq(PLAIN_OTP))).thenReturn(OneTimePasswordVerificationResult.ACCEPTED);

        // Act
        final OneTimePasswordVerificationResult result = emailVerificationService.verifyEmail(USER_USERNAME, PLAIN_OTP);

        // Assert
        Assert.assertEquals(OneTimePasswordVerificationResult.ACCEPTED, result);
    }

    @Test
    public void testVerifyEmailWrongOtpIsRejected() {
        // Arrange
        final User user = givenUser(false);
        when(otpService.verify(eq(user), eq(PLAIN_OTP))).thenReturn(OneTimePasswordVerificationResult.REJECTED);

        // Act
        final OneTimePasswordVerificationResult result = emailVerificationService.verifyEmail(USER_USERNAME, PLAIN_OTP);

        // Assert
        Assert.assertEquals(OneTimePasswordVerificationResult.REJECTED, result);
    }

    @Test
    public void testVerifyEmailExpiredOtpIsReportedAsExpired() {
        // Arrange
        final User user = givenUser(false);
        when(otpService.verify(eq(user), eq(PLAIN_OTP))).thenReturn(OneTimePasswordVerificationResult.EXPIRED);

        // Act
        final OneTimePasswordVerificationResult result = emailVerificationService.verifyEmail(USER_USERNAME, PLAIN_OTP);

        // Assert
        Assert.assertEquals(OneTimePasswordVerificationResult.EXPIRED, result);
    }

    @Test(expected = NullPointerException.class)
    public void testVerifyEmailNullIdentifierThrows() {
        // Act
        emailVerificationService.verifyEmail(null, PLAIN_OTP);
    }

    @Test(expected = NullPointerException.class)
    public void testVerifyEmailNullOtpThrows() {
        // Act
        emailVerificationService.verifyEmail(USER_USERNAME, null);
    }
}
