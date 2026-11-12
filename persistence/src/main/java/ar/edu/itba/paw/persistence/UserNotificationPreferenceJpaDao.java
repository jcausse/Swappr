package ar.edu.itba.paw.persistence;

import ar.edu.itba.paw.model.notification.NotificationMode;
import ar.edu.itba.paw.model.notification.NotificationType;
import ar.edu.itba.paw.model.User;
import ar.edu.itba.paw.model.notification.UserNotificationPreference;
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
    public Optional<UserNotificationPreference> getByUserAndType(final User user, final NotificationType type) {
        if (user == null || user.getId() == null || type == null) {
            return Optional.empty();
        }
        return em.createQuery(
                        "SELECT p FROM UserNotificationPreference p WHERE p.user.id = :userId AND p.type = :type",
                        UserNotificationPreference.class)
                .setParameter("userId", user.getId())
                .setParameter("type", type)
                .getResultStream()
                .findFirst();
    }

    @Override
    public List<UserNotificationPreference> getByUser(final User user) {
        if (user == null || user.getId() == null) {
            return List.of();
        }
        return em.createQuery(
                        "SELECT p FROM UserNotificationPreference p WHERE p.user.id = :userId",
                        UserNotificationPreference.class)
                .setParameter("userId", user.getId())
                .getResultList();
    }

    @Override
    public UserNotificationPreference saveOrUpdate(
            final User user,
            final NotificationType type,
            final NotificationMode mode
    ) {
        final int updated = em.createQuery(
                        "UPDATE UserNotificationPreference p SET p.mode = :mode WHERE p.user.id = :userId AND p.type = :type")
                .setParameter("mode", mode)
                .setParameter("userId", user.getId())
                .setParameter("type", type)
                .executeUpdate();

        final UserNotificationPreference preference = UserNotificationPreference.builder()
                .user(em.getReference(User.class, user.getId()))
                .type(type)
                .mode(mode)
                .build();
        if (updated == 0) {
            em.persist(preference);
        }
        return preference;
    }
}
