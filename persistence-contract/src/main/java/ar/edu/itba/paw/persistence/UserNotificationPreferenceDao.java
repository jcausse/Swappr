package ar.edu.itba.paw.persistence;

import ar.edu.itba.paw.model.notification.NotificationMode;
import ar.edu.itba.paw.model.notification.NotificationType;
import ar.edu.itba.paw.model.User;
import ar.edu.itba.paw.model.notification.UserNotificationPreference;

import java.util.List;
import java.util.Optional;

public interface UserNotificationPreferenceDao {

    Optional<UserNotificationPreference> getByUserAndType(User user, NotificationType type);

    List<UserNotificationPreference> getByUser(User user);

    UserNotificationPreference saveOrUpdate(User user, NotificationType type, NotificationMode mode);
}
