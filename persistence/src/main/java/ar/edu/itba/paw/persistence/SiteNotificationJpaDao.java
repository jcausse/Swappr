package ar.edu.itba.paw.persistence;

import ar.edu.itba.paw.model.NotificationType;
import ar.edu.itba.paw.model.Page;
import ar.edu.itba.paw.model.SiteNotification;
import ar.edu.itba.paw.model.SiteNotificationFilter;
import ar.edu.itba.paw.model.User;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public class SiteNotificationJpaDao implements SiteNotificationDao {

    @PersistenceContext
    private EntityManager em;

    @Override
    public Optional<SiteNotification> getById(long id) {
        return Optional.ofNullable(em.find(SiteNotification.class, id));
    }

    @Override
    public SiteNotification create(
            User user,
            NotificationType type,
            String title,
            String body,
            String actionUrl,
            String actionLabelKey,
            Instant createdAt
    ) {
        final User userRef = em.getReference(User.class, user.getId());
        final SiteNotification notification = SiteNotification.builder()
                .user(userRef)
                .type(type)
                .title(title)
                .body(body)
                .actionUrl(actionUrl)
                .actionLabelKey(actionLabelKey)
                .read(false)
                .createdAt(createdAt != null ? createdAt : Instant.now())
                .build();

        em.persist(notification);
        return notification;
    }

    @Override
    public Page<SiteNotification> getMatching(SiteNotificationFilter filter) {
        if (filter == null || filter.getUserId() == null || filter.getPage() < 1 || filter.getPageSize() <= 0) {
            final int page = filter != null ? Math.max(filter.getPage(), 1) : 1;
            final int pageSize = filter != null ? Math.max(filter.getPageSize(), 1) : 10;
            return new Page<>(List.of(), page, pageSize, 0);
        }

        final int page = filter.getPage();
        final int pageSize = filter.getPageSize();

        // 1. Count query
        final StringBuilder countHql = new StringBuilder(
                "SELECT COUNT(n) FROM SiteNotification n WHERE n.user.id = :userId");
        final Map<String, Object> hqlParams = new HashMap<>();
        hqlParams.put("userId", filter.getUserId());

        if (filter.isUnreadOnly()) {
            countHql.append(" AND n.read = false");
        }
        if (filter.getType() != null) {
            countHql.append(" AND n.type = :type");
            hqlParams.put("type", filter.getType());
        }

        final TypedQuery<Long> countQuery = em.createQuery(countHql.toString(), Long.class);
        hqlParams.forEach(countQuery::setParameter);
        final long totalCount = countQuery.getSingleResult();

        if (totalCount == 0) {
            return new Page<>(List.of(), page, pageSize, 0);
        }

        // 2. Native ID query (1+1 model - Step 1: select IDs with filters and ordering)
        final StringBuilder nativeSql = new StringBuilder(
                "SELECT notification_id FROM site_notifications WHERE user_id = :userId");
        final Map<String, Object> nativeParams = new HashMap<>();
        nativeParams.put("userId", filter.getUserId());

        if (filter.isUnreadOnly()) {
            nativeSql.append(" AND is_read = false");
        }
        if (filter.getType() != null) {
            nativeSql.append(" AND notification_type = :type");
            nativeParams.put("type", filter.getType().name());
        }
        nativeSql.append(" ORDER BY created_at DESC, notification_id DESC");

        final Query idQuery = em.createNativeQuery(nativeSql.toString())
                .setFirstResult((page - 1) * pageSize)
                .setMaxResults(pageSize);
        nativeParams.forEach(idQuery::setParameter);

        @SuppressWarnings("unchecked")
        final List<Long> ids = ((List<Number>) idQuery.getResultList()).stream().map(Number::longValue).toList();
        if (ids.isEmpty()) {
            return new Page<>(List.of(), page, pageSize, totalCount);
        }

        // 3. Population query (1+1 model - Step 2: load entities and preserve order)
        final List<SiteNotification> entities = em.createQuery(
                "FROM SiteNotification n WHERE n.id IN :ids ORDER BY n.createdAt DESC, n.id DESC",
                SiteNotification.class)
                .setParameter("ids", ids)
                .getResultList();

        return new Page<>(entities, page, pageSize, totalCount);
    }

    @Override
    public void markAsRead(SiteNotification notification) {
        if (notification != null) {
            final SiteNotification managed = em.contains(notification) ? notification : em.merge(notification);
            managed.markAsRead();
        }
    }

    @Override
    public void markAllAsReadForUser(User user) {
        if (user == null || user.getId() == null) {
            return;
        }
        em.createQuery(
                "UPDATE SiteNotification n SET n.read = true WHERE n.user.id = :userId AND n.read = false"
            ).setParameter("userId", user.getId())
            .executeUpdate();
    }

    @Override
    public void delete(SiteNotification notification) {
        if (notification != null) {
            final SiteNotification managed = em.contains(notification) ? notification : em.merge(notification);
            em.remove(managed);
        }
    }
}
