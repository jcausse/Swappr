package ar.edu.itba.paw.persistence;

import ar.edu.itba.paw.model.User;
import ar.edu.itba.paw.model.notification.NotificationChannel;
import ar.edu.itba.paw.model.notification.NotificationType;
import ar.edu.itba.paw.model.notification.UserNotificationPreference;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface UserNotificationPreferenceDao {

    Optional<UserNotificationPreference> getByUserTypeAndChannel(
            User user,
            NotificationType type,
            NotificationChannel channel
    );

    List<UserNotificationPreference> getByUser(User user);

    List<UserNotificationPreference> getByUsersAndType(Collection<User> users, NotificationType type);

    UserNotificationPreference saveOrUpdate(
            User user,
            NotificationType type,
            NotificationChannel channel,
            boolean enabled
    );
}
