package ar.edu.itba.paw.service;

import ar.edu.itba.paw.model.notification.NotificationMode;
import ar.edu.itba.paw.model.notification.NotificationType;
import ar.edu.itba.paw.model.User;

import java.util.Map;

public interface NotificationPreferenceService {

    NotificationMode getMode(User user, NotificationType type);

    Map<NotificationType, NotificationMode> getModesForUser(User user);

    void updatePreferences(User user, Map<NotificationType, NotificationMode> modesByType);
}
