package ar.edu.itba.paw.service.notification;

import ar.edu.itba.paw.model.NotificationMode;
import ar.edu.itba.paw.model.NotificationType;
import ar.edu.itba.paw.model.User;
import ar.edu.itba.paw.model.UserNotificationPreference;
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
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NotificationPreferenceServiceImpl implements NotificationPreferenceService {

    private static final Logger LOGGER = LoggerFactory.getLogger(NotificationPreferenceServiceImpl.class);

    private final UserNotificationPreferenceDao preferenceDao;

    @Override
    public NotificationMode getMode(final User user, @NonNull final NotificationType type) {
        if (user == null || !type.isConfigurable()) {
            return type.getDefaultMode();
        }
        return preferenceDao.getByUserAndType(user, type)
                .map(UserNotificationPreference::getMode)
                .orElse(type.getDefaultMode());
    }

    @Override
    public Map<NotificationType, NotificationMode> getModesForUser(@NonNull final User user) {
        LOGGER.debug("Fetching notification preferences for user id={}", user.getId());
        final Map<NotificationType, NotificationMode> modes = new EnumMap<>(NotificationType.class);
        for (final NotificationType type : NotificationType.getConfigurableTypes()) {
            modes.put(type, type.getDefaultMode());
        }
        for (final UserNotificationPreference pref : preferenceDao.getByUser(user)) {
            if (pref.getType().isConfigurable()) {
                modes.put(pref.getType(), pref.getMode());
            }
        }
        return modes;
    }

    @Override
    @Transactional
    public void updatePreferences(
            @NonNull final User user,
            @NonNull final Map<NotificationType, NotificationMode> modesByType
    ) {
        LOGGER.info("Updating notification preferences for user id={}", user.getId());
        for (final var entry : modesByType.entrySet()) {
            final NotificationType type = entry.getKey();
            final NotificationMode mode = entry.getValue();
            if (type == null || !type.isModeSupported(mode)) {
                throw new BadParameterException("Invalid notification preference: type=" + type + ", mode=" + mode);
            }
            preferenceDao.saveOrUpdate(user, type, mode);
        }
    }
}
