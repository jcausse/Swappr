package ar.edu.itba.paw.persistence;

import ar.edu.itba.paw.model.NotificationType;
import ar.edu.itba.paw.model.Offer;
import ar.edu.itba.paw.model.Page;
import ar.edu.itba.paw.model.SiteNotification;
import ar.edu.itba.paw.model.SiteNotificationFilter;
import ar.edu.itba.paw.model.User;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface SiteNotificationDao {

    Optional<SiteNotification> getById(long id);

    Page<SiteNotification> getMatching(SiteNotificationFilter filter);

    SiteNotification create(
            User user,
            NotificationType type,
            String titleKey,
            String bodyKey,
            List<String> bodyArgs,
            String actionUrl,
            String actionLabelKey,
            Instant createdAt,
            Offer offer
    );

    default SiteNotification create(
            User user,
            NotificationType type,
            String titleKey,
            String bodyKey,
            List<String> bodyArgs,
            String actionUrl,
            String actionLabelKey,
            Instant createdAt
    ) {
        return create(user, type, titleKey, bodyKey, bodyArgs, actionUrl, actionLabelKey, createdAt, null);
    }

    void markAsRead(SiteNotification notification);

    void markAllAsReadForUser(User user);

    void delete(SiteNotification notification);
}
