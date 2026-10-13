package ar.edu.itba.paw.service;

import ar.edu.itba.paw.model.Image;
import ar.edu.itba.paw.model.User;
import ar.edu.itba.paw.model.Language;
import ar.edu.itba.paw.persistence.UserDao;
import ar.edu.itba.paw.persistence.UserRoleDao;
import ar.edu.itba.paw.service.dto.UserCreationDto;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.util.Optional;

import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class UserServiceImplTest {

    private static final long USER_ID = 1L;
    private static final String USER_USERNAME = "fake_user";
    private static final String USER_DISPLAY_NAME = "Fake User";
    private static final String USER_EMAIL = "fake@example.com";
    private static final String USER_PASSWORD = "fake_password";
    private static final String ENCODED_PASSWORD = "encoded-password";

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
    private MailingService mailingService;


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
        userService.updatePreferredLanguage(user, Language.SPANISH);

        // Assert
        verify(userDao).updatePreferredLanguage(USER_ID, Language.SPANISH);
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
    /* updateEmail / markEmailAsVerified                                                               */
    /* ---------------------------------------------------------------------------------------------- */

    @Test(expected = IllegalArgumentException.class)
    public void testUpdateEmailNonExistingUserThrows() {
        final User user = buildFakeUser();
        when(userDao.update(eq(USER_ID), isNull(), any(), isNull(), isNull())).thenReturn(Optional.empty());

        userService.updateEmail(user, USER_EMAIL);
    }
}
