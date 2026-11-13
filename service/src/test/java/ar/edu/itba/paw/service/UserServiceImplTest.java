package ar.edu.itba.paw.service;

import ar.edu.itba.paw.model.Image;
import ar.edu.itba.paw.model.User;
import ar.edu.itba.paw.model.Language;
import ar.edu.itba.paw.model.OneTimePassword;
import ar.edu.itba.paw.persistence.UserDao;
import ar.edu.itba.paw.persistence.UserRoleDao;
import ar.edu.itba.paw.service.dto.UserCreationDto;
import ar.edu.itba.paw.service.enumeration.OneTimePasswordVerificationResult;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.util.Optional;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class UserServiceImplTest {

    private static final long USER_ID = 1L;
    private static final String USER_USERNAME = "fake_user";
    private static final String USER_DISPLAY_NAME = "Fake User";
    private static final String USER_EMAIL = "fake@example.com";
    private static final String USER_PASSWORD = "fake_password";
    private static final String UNKNOWN_USERNAME = "unknown_user";
    private static final String ENCODED_PASSWORD = "encoded-password";

    private static final String PLAIN_OTP = "Ab3dE5gH9k";

    private static final long PROVINCE_ID = 1L;
    private static final long NON_EXISTING_PROVINCE_ID = 9000L;
    private static final long IMAGE_ID = 50L;
    private static final long OLD_IMAGE_ID = 40L;

    @InjectMocks
    private UserServiceImpl userService;

    @Mock
    private UserDao userDao;
    @Mock
    private UserRoleDao userRoleDao;
    @Mock
    private ImageService imageService;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private ProvinceService provinceService;
    @Mock
    private NotificationDispatcherService notificationDispatcherService;
    @Mock
    private OneTimePasswordService otpService;


    private User buildFakeUser() {
        return User.builder()
            .id(USER_ID)
            .username(USER_USERNAME)
            .displayName(USER_DISPLAY_NAME)
            .email(USER_EMAIL)
            .password(USER_PASSWORD)
            .joinedAt(Instant.now())
            .build();
    }

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

    private User buildUserWithImage(final Long imageId) {
        return User.builder()
            .id(USER_ID)
            .username(USER_USERNAME)
            .displayName(USER_DISPLAY_NAME)
            .email(USER_EMAIL)
            .password(USER_PASSWORD)
            .image(imageId != null ? Image.builder().id(imageId).build() : null)
            .joinedAt(Instant.now())
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
        when(userDao.getByUsername(eq(USER_USERNAME))).thenReturn(Optional.of(user));
        return user;
    }

    /* ---------------------------------------------------------------------------------------------- */
    /* getById / getByUsername / getByEmail                                                            */
    /* ---------------------------------------------------------------------------------------------- */

    @Test
    public void testGetByIdUserExists() {
        when(userDao.getById(eq(USER_ID))).thenReturn(Optional.of(buildFakeUser()));

        final Optional<User> maybeUser = userService.getById(USER_ID);

        Assert.assertTrue(maybeUser.isPresent());
        Assert.assertEquals(USER_ID, (long) maybeUser.get().getId());
        Assert.assertEquals(USER_USERNAME, maybeUser.get().getUsername());
    }

    @Test
    public void testGetByIdUserDoesNotExist() {
        when(userDao.getById(eq(USER_ID))).thenReturn(Optional.empty());

        Assert.assertFalse(userService.getById(USER_ID).isPresent());
    }

    @Test
    public void testGetByUsernameUserExists() {
        when(userDao.getByUsername(eq(USER_USERNAME))).thenReturn(Optional.of(buildFakeUser()));

        Assert.assertTrue(userService.getByUsername(USER_USERNAME).isPresent());
    }

    @Test
    public void testGetByEmailUserExists() {
        when(userDao.getByEmail(eq(USER_EMAIL))).thenReturn(Optional.of(buildFakeUser()));

        Assert.assertTrue(userService.getByEmail(USER_EMAIL).isPresent());
    }

    /* ---------------------------------------------------------------------------------------------- */
    /* isUsernameTaken / isEmailTaken                                                                  */
    /* ---------------------------------------------------------------------------------------------- */

    @Test
    public void testIsUsernameTakenTrue() {
        when(userDao.isUsernameTaken(eq(USER_USERNAME))).thenReturn(true);

        Assert.assertTrue(userService.isUsernameTaken(USER_USERNAME));
    }

    @Test
    public void testIsEmailTakenTrue() {
        when(userDao.isEmailTaken(eq(USER_EMAIL))).thenReturn(true);

        Assert.assertTrue(userService.isEmailTaken(USER_EMAIL));
    }

    /* ---------------------------------------------------------------------------------------------- */
    /* create                                                                                          */
    /* ---------------------------------------------------------------------------------------------- */

    @Test(expected = IllegalArgumentException.class)
    public void testCreateRejectsUsernameWithAt() {
        userService.create(new UserCreationDto("user@bad", USER_DISPLAY_NAME, USER_EMAIL, USER_PASSWORD, null));
    }

    /* ---------------------------------------------------------------------------------------------- */
    /* updatePreferredLanguage                                                                         */
    /* ---------------------------------------------------------------------------------------------- */

    @Test
    public void testUpdatePreferredLanguagePersistsChosenLanguage() {
        // Arrange
        final User user = buildFakeUser();
        when(userDao.updatePreferredLanguage(eq(USER_ID), eq(Language.SPANISH))).thenReturn(Optional.of(user));

        // Act
        final User result = userService.updatePreferredLanguage(user, Language.SPANISH);

        // Assert
        Assert.assertSame(user, result);
    }

    @Test
    public void testUpdatePreferredLanguageReturnsUpdatedUser() {
        // Arrange
        final User user = buildFakeUser();
        when(userDao.updatePreferredLanguage(eq(USER_ID), eq(Language.ENGLISH))).thenReturn(Optional.of(user));

        // Act
        final User result = userService.updatePreferredLanguage(user, Language.ENGLISH);

        // Assert
        Assert.assertSame(user, result);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testUpdatePreferredLanguageNonExistingUserThrows() {
        // Arrange
        final User user = buildFakeUser();
        when(userDao.updatePreferredLanguage(eq(USER_ID), eq(Language.SPANISH))).thenReturn(Optional.empty());

        // Act
        userService.updatePreferredLanguage(user, Language.SPANISH);
    }

    /* ---------------------------------------------------------------------------------------------- */
    /* resendVerificationEmail                                                                         */
    /* ---------------------------------------------------------------------------------------------- */

    @Test
    public void testResendVerificationEmailUnknownUserReturnsEmpty() {
        // Arrange
        when(userDao.getByUsername(eq(UNKNOWN_USERNAME))).thenReturn(Optional.empty());

        // Act
        final Optional<OneTimePassword> result = userService.resendVerificationEmail(UNKNOWN_USERNAME);

        // Assert
        Assert.assertFalse(result.isPresent());
    }

    @Test
    public void testResendVerificationEmailVerifiedUserReturnsEmpty() {
        // Arrange
        givenUser(true);

        // Act
        final Optional<OneTimePassword> result = userService.resendVerificationEmail(USER_USERNAME);

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
        final Optional<OneTimePassword> result = userService.resendVerificationEmail(USER_USERNAME);

        // Assert
        Assert.assertTrue(result.isPresent());
        Assert.assertSame(otp, result.get());
    }

    @Test(expected = NullPointerException.class)
    public void testResendVerificationEmailNullIdentifierThrows() {
        // Act
        userService.resendVerificationEmail(null);
    }

    /* ---------------------------------------------------------------------------------------------- */
    /* verifyEmail                                                                                     */
    /* ---------------------------------------------------------------------------------------------- */

    @Test
    public void testVerifyEmailUnknownUserIsRejected() {
        // Arrange
        when(userDao.getByUsername(eq(UNKNOWN_USERNAME))).thenReturn(Optional.empty());

        // Act
        final OneTimePasswordVerificationResult result = userService.verifyEmail(UNKNOWN_USERNAME, PLAIN_OTP);

        // Assert
        Assert.assertEquals(OneTimePasswordVerificationResult.REJECTED, result);
    }

    @Test
    public void testVerifyEmailAlreadyVerifiedUserIsAccepted() {
        // Arrange
        givenUser(true);

        // Act
        final OneTimePasswordVerificationResult result = userService.verifyEmail(USER_USERNAME, PLAIN_OTP);

        // Assert
        Assert.assertEquals(OneTimePasswordVerificationResult.ACCEPTED, result);
    }

    @Test
    public void testVerifyEmailCorrectOtpIsAccepted() {
        // Arrange
        final User user = givenUser(false);
        when(otpService.verify(eq(user), eq(PLAIN_OTP))).thenReturn(OneTimePasswordVerificationResult.ACCEPTED);
        when(userDao.verifyEmail(eq(user.getId()), any())).thenReturn(Optional.of(user));

        // Act
        final OneTimePasswordVerificationResult result = userService.verifyEmail(USER_USERNAME, PLAIN_OTP);

        // Assert
        Assert.assertEquals(OneTimePasswordVerificationResult.ACCEPTED, result);
    }

    @Test
    public void testVerifyEmailWrongOtpIsRejected() {
        // Arrange
        final User user = givenUser(false);
        when(otpService.verify(eq(user), eq(PLAIN_OTP))).thenReturn(OneTimePasswordVerificationResult.REJECTED);

        // Act
        final OneTimePasswordVerificationResult result = userService.verifyEmail(USER_USERNAME, PLAIN_OTP);

        // Assert
        Assert.assertEquals(OneTimePasswordVerificationResult.REJECTED, result);
    }

    @Test
    public void testVerifyEmailExpiredOtpIsReportedAsExpired() {
        // Arrange
        final User user = givenUser(false);
        when(otpService.verify(eq(user), eq(PLAIN_OTP))).thenReturn(OneTimePasswordVerificationResult.EXPIRED);

        // Act
        final OneTimePasswordVerificationResult result = userService.verifyEmail(USER_USERNAME, PLAIN_OTP);

        // Assert
        Assert.assertEquals(OneTimePasswordVerificationResult.EXPIRED, result);
    }

    @Test(expected = NullPointerException.class)
    public void testVerifyEmailNullIdentifierThrows() {
        // Act
        userService.verifyEmail(null, PLAIN_OTP);
    }

    @Test(expected = NullPointerException.class)
    public void testVerifyEmailNullOtpThrows() {
        // Act
        userService.verifyEmail(USER_USERNAME, null);
    }
}
