package ar.edu.itba.paw.service;

import ar.edu.itba.paw.model.User;
import ar.edu.itba.paw.model.notification.NotificationChannel;
import ar.edu.itba.paw.model.notification.NotificationType;

import java.util.Map;
import java.util.Set;

public interface NotificationPreferenceService {

    boolean isNotificationEnabled(User user, NotificationType type, NotificationChannel channel);

    Map<NotificationType, Set<NotificationChannel>> getEnabledChannelsForUser(User user);

    void updatePreferences(User user, Map<NotificationType, Set<NotificationChannel>> enabledChannelsByType);
}
