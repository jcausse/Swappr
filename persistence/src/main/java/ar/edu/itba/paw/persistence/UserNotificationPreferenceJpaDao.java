package ar.edu.itba.paw.persistence;

import ar.edu.itba.paw.model.User;
import ar.edu.itba.paw.model.notification.NotificationChannel;
import ar.edu.itba.paw.model.notification.NotificationType;
import ar.edu.itba.paw.model.notification.UserNotificationPreference;
import ar.edu.itba.paw.model.notification.UserNotificationPreferenceId;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;
import java.util.Optional;

@Repository
public class UserNotificationPreferenceJpaDao implements UserNotificationPreferenceDao {

    @PersistenceContext
    private EntityManager em;

    @Override
    public Optional<UserNotificationPreference> getByUserTypeAndChannel(
            final User user,
            final NotificationType type,
            final NotificationChannel channel
    ) {
        if (user == null || user.getId() == null || type == null || channel == null) {
            return Optional.empty();
        }
        return em.createQuery(
                        "SELECT p FROM UserNotificationPreference p " +
                        "WHERE p.id.userId = :userId AND p.id.type = :type AND p.id.channel = :channel",
                        UserNotificationPreference.class)
                .setParameter("userId", user.getId())
                .setParameter("type", type)
                .setParameter("channel", channel)
                .getResultStream()
                .findFirst();
    }

    @Override
    public List<UserNotificationPreference> getByUser(final User user) {
        if (user == null || user.getId() == null) {
            return List.of();
        }
        return em.createQuery(
                        "SELECT p FROM UserNotificationPreference p WHERE p.id.userId = :userId",
                        UserNotificationPreference.class)
                .setParameter("userId", user.getId())
                .getResultList();
    }

    @Override
    public UserNotificationPreference saveOrUpdate(
            final User user,
            final NotificationType type,
            final NotificationChannel channel,
            final boolean enabled
    ) {
        final UserNotificationPreferenceId id = new UserNotificationPreferenceId(user.getId(), type, channel);
        final UserNotificationPreference existing = em.find(UserNotificationPreference.class, id);
        if (existing != null) {
            existing.setEnabled(enabled);
            return existing;
        }
        final UserNotificationPreference created = UserNotificationPreference.builder()
                .id(id)
                .user(em.getReference(User.class, user.getId()))
                .enabled(enabled)
                .build();
        em.persist(created);
        return created;
    }
}
