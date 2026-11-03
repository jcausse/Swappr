package ar.edu.itba.paw.persistence;

import ar.edu.itba.paw.model.NotificationType;
import ar.edu.itba.paw.model.Page;
import ar.edu.itba.paw.model.SiteNotification;
import ar.edu.itba.paw.model.SiteNotificationFilter;
import ar.edu.itba.paw.model.User;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.jdbc.JdbcTestUtils;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.sql.DataSource;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Map;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { TestConfig.class })
@Transactional
@Rollback
public class SiteNotificationJpaDaoTest {

    private static final String TABLE = "site_notifications";
    private static final long USER_ID = 1L; // fake_user from initial-data.sql

    private static final NotificationType TYPE = NotificationType.NEW_OFFER;
    private static final String TITLE = "New Offer Received";
    private static final String BODY = "User John made an offer on your listing";
    private static final String ACTION_URL = "/offers/10";
    private static final String ACTION_LABEL_KEY = "notification.action.viewOffer";

    @Autowired
    private SiteNotificationDao notificationDao;

    @Autowired
    private DataSource dataSource;

    @PersistenceContext
    private EntityManager em;

    private JdbcTemplate jdbcTemplate;

    @Before
    public void setUp() {
        jdbcTemplate = new JdbcTemplate(dataSource);
        JdbcTestUtils.deleteFromTables(jdbcTemplate, TABLE);
    }

    private User userRef(Long id) {
        return em.getReference(User.class, id);
    }

    private void insertNotification(
            final long id,
            final long userId,
            final NotificationType type,
            final String title,
            final String body,
            final String actionUrl,
            final String actionLabelKey,
            final boolean isRead,
            final Instant createdAt
    ) {
        jdbcTemplate.update(
                "INSERT INTO site_notifications (notification_id, user_id, notification_type, title, body, action_url, action_label_key, is_read, created_at) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)",
                id,
                userId,
                type.name(),
                title,
                body,
                actionUrl,
                actionLabelKey,
                isRead,
                Timestamp.from(createdAt)
        );
    }

    /* ---------------------------------------------------------------------------------------------- */
    /* create                                                                                          */
    /* ---------------------------------------------------------------------------------------------- */

    @Test
    public void testCreateReturnsNotification() {
        // Arrange
        final User user = userRef(USER_ID);
        final Instant now = Instant.now();

        // Act
        final SiteNotification notification = notificationDao.create(
                user, TYPE, TITLE, BODY, ACTION_URL, ACTION_LABEL_KEY, now
        );

        // Assert
        Assert.assertNotNull(notification);
        Assert.assertNotNull(notification.getId());
        Assert.assertEquals(USER_ID, (long) notification.getUser().getId());
        Assert.assertEquals(TYPE, notification.getType());
        Assert.assertEquals(TITLE, notification.getTitle());
        Assert.assertEquals(BODY, notification.getBody());
        Assert.assertEquals(ACTION_URL, notification.getActionUrl().orElse(null));
        Assert.assertEquals(ACTION_LABEL_KEY, notification.getActionLabelKey().orElse(null));
        Assert.assertFalse(notification.isRead());
    }

    @Test
    public void testCreatePersistsInDatabase() {
        // Arrange
        final User user = userRef(USER_ID);
        final Instant now = Instant.now();

        // Act
        final SiteNotification notification = notificationDao.create(
                user, TYPE, TITLE, BODY, ACTION_URL, ACTION_LABEL_KEY, now
        );
        em.flush();

        // Assert directly querying the database table (no other DAO methods used)
        Assert.assertEquals(1, JdbcTestUtils.countRowsInTableWhere(
                jdbcTemplate, TABLE, "notification_id = " + notification.getId()
        ));

        final Map<String, Object> row = jdbcTemplate.queryForMap(
                "SELECT * FROM site_notifications WHERE notification_id = ?", notification.getId()
        );
        Assert.assertEquals(USER_ID, ((Number) row.get("user_id")).longValue());
        Assert.assertEquals(TYPE.name(), row.get("notification_type"));
        Assert.assertEquals(TITLE, row.get("title"));
        Assert.assertEquals(BODY, row.get("body"));
        Assert.assertEquals(ACTION_URL, row.get("action_url"));
        Assert.assertEquals(ACTION_LABEL_KEY, row.get("action_label_key"));
        Assert.assertEquals(Boolean.FALSE, row.get("is_read"));
    }

    /* ---------------------------------------------------------------------------------------------- */
    /* getMatching                                                                                    */
    /* ---------------------------------------------------------------------------------------------- */

    @Test
    public void testGetMatchingPaginationAndOrdering() {
        // Arrange with direct database inserts
        final Instant baseTime = Instant.parse("2026-11-01T10:00:00Z");
        insertNotification(101L, USER_ID, NotificationType.NEW_OFFER, "Title 1", "Body 1", null, null, false, baseTime.plusSeconds(10));
        insertNotification(102L, USER_ID, NotificationType.OFFER_ACCEPTED, "Title 2", "Body 2", null, null, false, baseTime.plusSeconds(20));
        insertNotification(103L, USER_ID, NotificationType.OFFER_REJECTED, "Title 3", "Body 3", null, null, false, baseTime.plusSeconds(30));

        // Act - page 1, size 2 (newest first: 103, 102)
        final SiteNotificationFilter filterPage1 = SiteNotificationFilter.builder()
                .userId(USER_ID)
                .page(1)
                .pageSize(2)
                .build();
        final Page<SiteNotification> page1 = notificationDao.getMatching(filterPage1);

        // Assert
        Assert.assertEquals(3, page1.getTotalCount());
        Assert.assertEquals(2, page1.getContent().size());
        Assert.assertEquals(103L, (long) page1.getContent().get(0).getId());
        Assert.assertEquals(102L, (long) page1.getContent().get(1).getId());
        Assert.assertTrue(page1.isHasNext());
        Assert.assertFalse(page1.isHasPrevious());

        // Act - page 2, size 2 (101)
        final SiteNotificationFilter filterPage2 = SiteNotificationFilter.builder()
                .userId(USER_ID)
                .page(2)
                .pageSize(2)
                .build();
        final Page<SiteNotification> page2 = notificationDao.getMatching(filterPage2);
        Assert.assertEquals(1, page2.getContent().size());
        Assert.assertEquals(101L, (long) page2.getContent().getFirst().getId());
        Assert.assertFalse(page2.isHasNext());
        Assert.assertTrue(page2.isHasPrevious());
    }

    @Test
    public void testGetMatchingUnreadOnly() {
        // Arrange with direct database inserts
        final Instant now = Instant.now();
        insertNotification(201L, USER_ID, NotificationType.NEW_OFFER, "Unread", "Body", null, null, false, now.plusSeconds(10));
        insertNotification(202L, USER_ID, NotificationType.OFFER_ACCEPTED, "Read", "Body", null, null, true, now);

        // Act
        final SiteNotificationFilter filter = SiteNotificationFilter.builder()
                .userId(USER_ID)
                .unreadOnly(true)
                .page(1)
                .pageSize(10)
                .build();
        final Page<SiteNotification> unreadPage = notificationDao.getMatching(filter);

        // Assert
        Assert.assertEquals(1, unreadPage.getTotalCount());
        Assert.assertEquals(1, unreadPage.getContent().size());
        Assert.assertEquals(201L, (long) unreadPage.getContent().getFirst().getId());
    }

    @Test
    public void testGetMatchingByType() {
        // Arrange with direct database inserts
        final Instant now = Instant.now();
        insertNotification(301L, USER_ID, NotificationType.OFFER_ACCEPTED, "Accepted", "Body", null, null, false, now.plusSeconds(10));
        insertNotification(302L, USER_ID, NotificationType.NEW_OFFER, "New Offer", "Body", null, null, false, now);

        // Act - filter by OFFER_ACCEPTED
        final SiteNotificationFilter filter = SiteNotificationFilter.builder()
                .userId(USER_ID)
                .type(NotificationType.OFFER_ACCEPTED)
                .page(1)
                .pageSize(10)
                .build();
        final Page<SiteNotification> result = notificationDao.getMatching(filter);

        // Assert
        Assert.assertEquals(1, result.getTotalCount());
        Assert.assertEquals(1, result.getContent().size());
        Assert.assertEquals(301L, (long) result.getContent().getFirst().getId());
        Assert.assertEquals(NotificationType.OFFER_ACCEPTED, result.getContent().getFirst().getType());
    }

    /* ---------------------------------------------------------------------------------------------- */
    /* countUnreadForUser                                                                             */
    /* ---------------------------------------------------------------------------------------------- */

    @Test
    public void testCountUnreadForUser() {
        // Arrange with direct database inserts
        final Instant now = Instant.now();
        insertNotification(401L, USER_ID, NotificationType.NEW_OFFER, "T1", "B1", null, null, false, now);
        insertNotification(402L, USER_ID, NotificationType.NEW_OFFER, "T2", "B2", null, null, false, now.plusSeconds(1));
        insertNotification(403L, USER_ID, NotificationType.NEW_OFFER, "T3", "B3", null, null, true, now.plusSeconds(2));

        // Act
        final int unreadCount = notificationDao.countUnreadForUser(userRef(USER_ID));

        // Assert
        Assert.assertEquals(2, unreadCount);
    }

    /* ---------------------------------------------------------------------------------------------- */
    /* markAllAsReadForUser                                                                           */
    /* ---------------------------------------------------------------------------------------------- */

    @Test
    public void testMarkAllAsReadForUser() {
        // Arrange with direct database inserts
        final Instant now = Instant.now();
        insertNotification(501L, USER_ID, NotificationType.NEW_OFFER, "T1", "B1", null, null, false, now);
        insertNotification(502L, USER_ID, NotificationType.NEW_OFFER, "T2", "B2", null, null, false, now.plusSeconds(1));

        // Act
        notificationDao.markAllAsReadForUser(userRef(USER_ID));
        em.flush();
        em.clear();

        // Assert directly querying database table (no other DAO method called)
        Assert.assertEquals(0, JdbcTestUtils.countRowsInTableWhere(
                jdbcTemplate, TABLE, "user_id = " + USER_ID + " AND is_read = false"
        ));
        Assert.assertEquals(2, JdbcTestUtils.countRowsInTableWhere(
                jdbcTemplate, TABLE, "user_id = " + USER_ID + " AND is_read = true"
        ));
    }

    /* ---------------------------------------------------------------------------------------------- */
    /* delete                                                                                          */
    /* ---------------------------------------------------------------------------------------------- */

    @Test
    public void testDelete() {
        // Arrange with direct database insert
        final Instant now = Instant.now();
        insertNotification(601L, USER_ID, TYPE, TITLE, BODY, ACTION_URL, ACTION_LABEL_KEY, false, now);
        final SiteNotification notification = em.find(SiteNotification.class, 601L);

        // Act
        notificationDao.delete(notification);
        em.flush();

        // Assert directly querying database table
        Assert.assertEquals(0, JdbcTestUtils.countRowsInTableWhere(
                jdbcTemplate, TABLE, "notification_id = 601"
        ));
    }
}
