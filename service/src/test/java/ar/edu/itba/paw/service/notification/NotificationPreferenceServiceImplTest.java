package ar.edu.itba.paw.service.notification;

import ar.edu.itba.paw.model.User;
import ar.edu.itba.paw.model.notification.NotificationChannel;
import ar.edu.itba.paw.model.notification.NotificationType;
import ar.edu.itba.paw.model.notification.UserNotificationPreference;
import ar.edu.itba.paw.model.notification.UserNotificationPreferenceId;
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
import java.util.Set;

import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
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

    private UserNotificationPreference buildPref(
            final User user,
            final NotificationType type,
            final NotificationChannel channel,
            final boolean enabled
    ) {
        return UserNotificationPreference.builder()
                .id(new UserNotificationPreferenceId(user.getId(), type, channel))
                .user(user)
                .enabled(enabled)
                .build();
    }

    @Test
    public void testIsNotificationEnabledReturnsTrueByDefaultWhenNotStored() {
        final User user = buildUser(10L);
        when(preferenceDao.getByUserTypeAndChannel(user, NotificationType.NEW_OFFER, NotificationChannel.SITE))
                .thenReturn(Optional.empty());

        Assert.assertTrue(service.isNotificationEnabled(
                user, NotificationType.NEW_OFFER, NotificationChannel.SITE));
    }

    @Test
    public void testIsNotificationEnabledReturnsStoredValueWhenPresent() {
        final User user = buildUser(10L);
        when(preferenceDao.getByUserTypeAndChannel(user, NotificationType.NEW_OFFER, NotificationChannel.SITE))
                .thenReturn(Optional.of(buildPref(user, NotificationType.NEW_OFFER, NotificationChannel.SITE, false)));

        Assert.assertFalse(service.isNotificationEnabled(
                user, NotificationType.NEW_OFFER, NotificationChannel.SITE));
    }

    @Test
    public void testIsNotificationEnabledReturnsFalseForUnsupportedChannel() {
        final User user = buildUser(10L);

        Assert.assertFalse(service.isNotificationEnabled(
                user, NotificationType.LISTING_PUBLISHED, NotificationChannel.SITE));
    }

    @Test
    public void testIsNotificationEnabledReturnsTrueForNonConfigurableTypeWithoutQueryingDao() {
        final User user = buildUser(10L);

        Assert.assertTrue(service.isNotificationEnabled(
                user, NotificationType.PASSWORD_RECOVERY, NotificationChannel.EMAIL));
        Assert.assertTrue(service.isNotificationEnabled(
                user, NotificationType.WELCOME, NotificationChannel.EMAIL));
    }

    @Test
    public void testGetEnabledChannelsForUserCombinesDefaultsAndStoredOverrides() {
        final User user = buildUser(10L);
        when(preferenceDao.getByUser(user)).thenReturn(List.of(
                buildPref(user, NotificationType.NEW_OFFER, NotificationChannel.EMAIL, false),
                buildPref(user, NotificationType.LISTING_PUBLISHED, NotificationChannel.EMAIL, false)
        ));

        final Map<NotificationType, Set<NotificationChannel>> enabled = service.getEnabledChannelsForUser(user);

        Assert.assertEquals(Set.of(NotificationChannel.SITE), enabled.get(NotificationType.NEW_OFFER));
        Assert.assertEquals(
                Set.of(NotificationChannel.SITE, NotificationChannel.EMAIL),
                enabled.get(NotificationType.OFFER_ACCEPTED));
        Assert.assertTrue(enabled.get(NotificationType.LISTING_PUBLISHED).isEmpty());
        Assert.assertNull(enabled.get(NotificationType.PASSWORD_RECOVERY));
    }

    @Test
    public void testUpdatePreferencesSavesAllConfigurableTypeChannelPairsIncludingUnchecked() {
        final User user = buildUser(10L);
        final Map<NotificationType, Set<NotificationChannel>> submitted = new EnumMap<>(NotificationType.class);
        submitted.put(NotificationType.NEW_OFFER, Set.of(NotificationChannel.SITE));

        final ArgumentCaptor<NotificationType> typeCaptor = ArgumentCaptor.forClass(NotificationType.class);
        final ArgumentCaptor<NotificationChannel> channelCaptor = ArgumentCaptor.forClass(NotificationChannel.class);
        final ArgumentCaptor<Boolean> enabledCaptor = ArgumentCaptor.forClass(Boolean.class);
        lenient().when(preferenceDao.saveOrUpdate(
                eq(user), typeCaptor.capture(), channelCaptor.capture(), enabledCaptor.capture()))
                .thenReturn(new UserNotificationPreference());

        service.updatePreferences(user, submitted);

        final List<NotificationType> types = typeCaptor.getAllValues();
        final List<NotificationChannel> channels = channelCaptor.getAllValues();
        final List<Boolean> flags = enabledCaptor.getAllValues();

        final int expectedPairs = NotificationType.getConfigurableTypes().stream()
                .mapToInt(t -> t.getSupportedChannels().size())
                .sum();
        Assert.assertEquals(expectedPairs, types.size());

        for (int i = 0; i < types.size(); i++) {
            final boolean expected = types.get(i) == NotificationType.NEW_OFFER
                    && channels.get(i) == NotificationChannel.SITE;
            Assert.assertEquals(
                    "Unexpected flag for " + types.get(i) + " / " + channels.get(i),
                    expected,
                    flags.get(i)
            );
        }
    }

    @Test(expected = BadParameterException.class)
    public void testUpdatePreferencesThrowsForNonConfigurableType() {
        final User user = buildUser(10L);
        final Map<NotificationType, Set<NotificationChannel>> submitted = new EnumMap<>(NotificationType.class);
        submitted.put(NotificationType.PASSWORD_RECOVERY, Set.of(NotificationChannel.EMAIL));

        service.updatePreferences(user, submitted);
    }

    @Test(expected = BadParameterException.class)
    public void testUpdatePreferencesThrowsForUnsupportedChannelOnEmailOnlyType() {
        final User user = buildUser(10L);
        final Map<NotificationType, Set<NotificationChannel>> submitted = new EnumMap<>(NotificationType.class);
        submitted.put(NotificationType.LISTING_PUBLISHED, Set.of(NotificationChannel.SITE));

        service.updatePreferences(user, submitted);
    }
}
