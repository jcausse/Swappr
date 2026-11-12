package ar.edu.itba.paw.service.notification;

import ar.edu.itba.paw.model.User;
import ar.edu.itba.paw.model.notification.NotificationChannel;
import ar.edu.itba.paw.model.notification.NotificationType;
import ar.edu.itba.paw.model.notification.UserNotificationPreference;
import ar.edu.itba.paw.persistence.UserNotificationPreferenceDao;
import ar.edu.itba.paw.service.NotificationPreferenceService;
import ar.edu.itba.paw.service.exception.BadParameterException;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NotificationPreferenceServiceImpl implements NotificationPreferenceService {

    private static final Logger LOGGER = LoggerFactory.getLogger(NotificationPreferenceServiceImpl.class);

    private final UserNotificationPreferenceDao preferenceDao;

    @Override
    public boolean isNotificationEnabled(
            final User user,
            final NotificationType type,
            final NotificationChannel channel
    ) {
        // Reject missing inputs or channels not supported by this notification type
        if (user == null || type == null || !type.supportsChannel(channel)) {
            return false;
        }

        // Mandatory notifications (security, moderation, onboarding) cannot be disabled by user preferences
        if (!type.isConfigurable()) {
            return true;
        }

        // Default to enabled when the user has no explicit preference stored for this type and channel
        return preferenceDao.getByUserTypeAndChannel(user, type, channel)
                .map(UserNotificationPreference::isEnabled)
                .orElse(true);
    }

    @Override
    public Map<NotificationType, Set<NotificationChannel>> getEnabledChannelsForUser(@NonNull final User user) {
        LOGGER.debug("Fetching notification preferences for user id={}", user.getId());

        // Populate the map with the defaults (all supported channels enabled for each configurable type)
        final Map<NotificationType, Set<NotificationChannel>> enabledByType =
                new EnumMap<>(NotificationType.class);
        for (final NotificationType type : NotificationType.getConfigurableTypes()) {
            final EnumSet<NotificationChannel> defaults = EnumSet.noneOf(NotificationChannel.class);
            defaults.addAll(type.getSupportedChannels());
            enabledByType.put(type, defaults);
        }

        // Apply stored opt-outs by removing channels the user has explicitly disabled
        for (final UserNotificationPreference pref : preferenceDao.getByUser(user)) {
            final NotificationType type = pref.getId().getType();
            final NotificationChannel channel = pref.getId().getChannel();
            if (!pref.isEnabled() && type.isConfigurable() && type.supportsChannel(channel)) {
                enabledByType.get(type).remove(channel);
            }
        }

        return enabledByType;
    }

    @Override
    @Transactional
    public void updatePreferences(
            @NonNull final User user,
            @NonNull final Map<NotificationType, Set<NotificationChannel>> enabledChannelsByType
    ) {
        LOGGER.info("Updating notification preferences for user id={}", user.getId());

        // Validate that every submitted notification type is configurable and every channel is supported
        validateSubmittedPreferences(enabledChannelsByType);

        // Persist every configurable (type, channel) pair so unchecked checkboxes are stored as disabled
        for (final NotificationType type : NotificationType.getConfigurableTypes()) {
            final Set<NotificationChannel> enabledChannels = enabledChannelsByType.get(type);
            for (final NotificationChannel channel : type.getSupportedChannels()) {
                final boolean enabled = enabledChannels != null && enabledChannels.contains(channel);
                preferenceDao.saveOrUpdate(user, type, channel, enabled);
            }
        }
    }

    private static void validateSubmittedPreferences(
            final Map<NotificationType, Set<NotificationChannel>> enabledChannelsByType
    ) {
        // Check each submitted entry to reject non-configurable types or unsupported channels
        for (final var entry : enabledChannelsByType.entrySet()) {
            final NotificationType type = entry.getKey();
            if (type == null || !type.isConfigurable()) {
                throw new BadParameterException("Invalid notification type: " + type);
            }

            final Set<NotificationChannel> channels = entry.getValue();
            if (channels != null) {
                for (final NotificationChannel channel : channels) {
                    if (!type.supportsChannel(channel)) {
                        throw new BadParameterException(
                                "Unsupported channel " + channel + " for notification type " + type);
                    }
                }
            }
        }
    }
}
