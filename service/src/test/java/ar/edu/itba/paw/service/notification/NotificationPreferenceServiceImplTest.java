package ar.edu.itba.paw.service.notification;

import ar.edu.itba.paw.model.NotificationMode;
import ar.edu.itba.paw.model.NotificationType;
import ar.edu.itba.paw.model.User;
import ar.edu.itba.paw.model.UserNotificationPreference;
import ar.edu.itba.paw.persistence.UserNotificationPreferenceDao;
import ar.edu.itba.paw.service.exception.BadParameterException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class NotificationPreferenceServiceImplTest {

    @Mock
    private UserNotificationPreferenceDao preferenceDao;

    private NotificationPreferenceServiceImpl service;

    @Before
    public void setUp() {
        service = new NotificationPreferenceServiceImpl(preferenceDao);
    }

    private User buildUser(final Long id) {
        return User.builder()
                .id(id)
                .username("user_" + id)
                .displayName("User " + id)
                .email("user" + id + "@swappr.com")
                .build();
    }

    @Test
    public void testGetModeReturnsDefaultWhenNotStoredInDatabase() {
        final User user = buildUser(10L);
        when(preferenceDao.getByUserAndType(user, NotificationType.NEW_OFFER)).thenReturn(Optional.empty());

        Assert.assertEquals(NotificationMode.SITE_AND_EMAIL, service.getMode(user, NotificationType.NEW_OFFER));
    }

    @Test
    public void testGetModeReturnsStoredPreferenceWhenPresent() {
        final User user = buildUser(10L);
        final UserNotificationPreference stored = UserNotificationPreference.builder()
                .user(user)
                .type(NotificationType.NEW_OFFER)
                .mode(NotificationMode.SITE_ONLY)
                .build();
        when(preferenceDao.getByUserAndType(user, NotificationType.NEW_OFFER)).thenReturn(Optional.of(stored));

        Assert.assertEquals(NotificationMode.SITE_ONLY, service.getMode(user, NotificationType.NEW_OFFER));
    }

    @Test
    public void testGetModeReturnsDefaultForNonConfigurableTypesWithoutQueryingDao() {
        final User user = buildUser(10L);

        Assert.assertEquals(NotificationMode.EMAIL_ONLY, service.getMode(user, NotificationType.PASSWORD_RECOVERY));
        Assert.assertEquals(NotificationMode.EMAIL_ONLY, service.getMode(user, NotificationType.WELCOME));
    }

    @Test
    public void testGetModesForUserCombinesDefaultsAndStoredPreferences() {
        final User user = buildUser(10L);
        when(preferenceDao.getByUser(user)).thenReturn(List.of(
                UserNotificationPreference.builder()
                        .user(user)
                        .type(NotificationType.NEW_OFFER)
                        .mode(NotificationMode.DISABLED)
                        .build()
        ));

        final Map<NotificationType, NotificationMode> modes = service.getModesForUser(user);

        Assert.assertEquals(NotificationMode.DISABLED, modes.get(NotificationType.NEW_OFFER));
        Assert.assertEquals(NotificationMode.SITE_AND_EMAIL, modes.get(NotificationType.OFFER_ACCEPTED));
        Assert.assertEquals(NotificationMode.EMAIL_ONLY, modes.get(NotificationType.LISTING_PUBLISHED));
        Assert.assertNull(modes.get(NotificationType.PASSWORD_RECOVERY));
    }

    @Test
    public void testUpdatePreferencesSavesEachConfigurableEntry() {
        final User user = buildUser(10L);
        final Map<NotificationType, NotificationMode> modes = new EnumMap<>(NotificationType.class);
        modes.put(NotificationType.NEW_OFFER, NotificationMode.SITE_ONLY);
        modes.put(NotificationType.LISTING_PUBLISHED, NotificationMode.DISABLED);

        final ArgumentCaptor<NotificationType> typeCaptor = ArgumentCaptor.forClass(NotificationType.class);
        final ArgumentCaptor<NotificationMode> modeCaptor = ArgumentCaptor.forClass(NotificationMode.class);
        when(preferenceDao.saveOrUpdate(eq(user), typeCaptor.capture(), modeCaptor.capture()))
                .thenReturn(new UserNotificationPreference());

        service.updatePreferences(user, modes);

        Assert.assertEquals(List.of(NotificationType.NEW_OFFER, NotificationType.LISTING_PUBLISHED), typeCaptor.getAllValues());
        Assert.assertEquals(List.of(NotificationMode.SITE_ONLY, NotificationMode.DISABLED), modeCaptor.getAllValues());
    }

    @Test(expected = BadParameterException.class)
    public void testUpdatePreferencesThrowsForNonConfigurableType() {
        final User user = buildUser(10L);
        final Map<NotificationType, NotificationMode> modes = new EnumMap<>(NotificationType.class);
        modes.put(NotificationType.PASSWORD_RECOVERY, NotificationMode.EMAIL_ONLY);

        service.updatePreferences(user, modes);
    }

    @Test(expected = BadParameterException.class)
    public void testUpdatePreferencesThrowsForUnsupportedModeOnEmailOnlyType() {
        final User user = buildUser(10L);
        final Map<NotificationType, NotificationMode> modes = new EnumMap<>(NotificationType.class);
        modes.put(NotificationType.LISTING_PUBLISHED, NotificationMode.SITE_ONLY);

        service.updatePreferences(user, modes);
    }
}
