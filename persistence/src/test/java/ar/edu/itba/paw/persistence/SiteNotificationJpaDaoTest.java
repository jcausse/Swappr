package ar.edu.itba.paw.persistence;

import ar.edu.itba.paw.model.notification.NotificationType;
import ar.edu.itba.paw.model.Offer;
import org.springframework.test.context.jdbc.Sql;
import ar.edu.itba.paw.model.Page;
import ar.edu.itba.paw.model.notification.SiteNotification;
import ar.edu.itba.paw.model.notification.SiteNotificationFilter;
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
import java.util.List;
import java.util.Map;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { TestConfig.class })
@Sql({ "classpath:sql/listing-data.sql", "classpath:sql/offer-data.sql" })
@Transactional
@Rollback
public class SiteNotificationJpaDaoTest {

    private static final String TABLE = "site_notifications";
    private static final String ARGS_TABLE = "site_notification_args";
    private static final long USER_ID = 1L; // fake_user from initial-data.sql

    private static final NotificationType TYPE = NotificationType.NEW_OFFER;
    private static final String TITLE_KEY = "notification.newOffer.title";
    private static final String BODY_KEY = "notification.newOffer.body";
    private static final List<String> BODY_ARGS = List.of("John", "250.00", "Laptop");
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
        JdbcTestUtils.deleteFromTables(jdbcTemplate, ARGS_TABLE, TABLE);
    }

    private User userRef(Long id) {
        return em.getReference(User.class, id);
    }

    private void insertNotification(
            final long id,
            final long userId,
            final NotificationType type,
            final String titleKey,
            final String bodyKey,
            final List<String> bodyArgs,
            final String actionUrl,
            final String actionLabelKey,
            final boolean isRead,
            final Instant createdAt,
            final Long offerId
    ) {
        jdbcTemplate.update(
                "INSERT INTO site_notifications (notification_id, user_id, notification_type, title_key, body_key, action_url, action_label_key, is_read, created_at, offer_id) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                id,
                userId,
                type.name(),
                titleKey,
                bodyKey,
                actionUrl,
                actionLabelKey,
                isRead,
                Timestamp.from(createdAt),
                offerId
        );
        if (bodyArgs != null) {
            for (int i = 0; i < bodyArgs.size(); i++) {
                jdbcTemplate.update(
                        "INSERT INTO site_notification_args (notification_id, arg_index, arg_value) VALUES (?, ?, ?)",
                        id,
                        i,
                        bodyArgs.get(i)
                );
            }
        }
    }

    private void insertNotification(
            final long id,
            final long userId,
            final NotificationType type,
            final String titleKey,
            final String bodyKey,
            final List<String> bodyArgs,
            final String actionUrl,
            final String actionLabelKey,
            final boolean isRead,
            final Instant createdAt
    ) {
        insertNotification(id, userId, type, titleKey, bodyKey, bodyArgs, actionUrl, actionLabelKey, isRead, createdAt, null);
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
                user, TYPE, TITLE_KEY, BODY_KEY, BODY_ARGS, ACTION_URL, ACTION_LABEL_KEY, now
        );

        // Assert
        Assert.assertNotNull(notification);
        Assert.assertNotNull(notification.getId());
        Assert.assertEquals(USER_ID, (long) notification.getUser().getId());
        Assert.assertEquals(TYPE, notification.getType());
        Assert.assertEquals(TITLE_KEY, notification.getTitleKey());
        Assert.assertEquals(BODY_KEY, notification.getBodyKey());
        Assert.assertEquals(BODY_ARGS, notification.getBodyArgs());
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
                user, TYPE, TITLE_KEY, BODY_KEY, BODY_ARGS, ACTION_URL, ACTION_LABEL_KEY, now
        );
        em.flush();

        // Assert directly querying the database tables (no other DAO methods used)
        Assert.assertEquals(1, JdbcTestUtils.countRowsInTableWhere(
                jdbcTemplate, TABLE, "notification_id = " + notification.getId()
        ));

        final Map<String, Object> row = jdbcTemplate.queryForMap(
                "SELECT * FROM site_notifications WHERE notification_id = ?", notification.getId()
        );
        Assert.assertEquals(USER_ID, ((Number) row.get("user_id")).longValue());
        Assert.assertEquals(TYPE.name(), row.get("notification_type"));
        Assert.assertEquals(TITLE_KEY, row.get("title_key"));
        Assert.assertEquals(BODY_KEY, row.get("body_key"));
        Assert.assertEquals(ACTION_URL, row.get("action_url"));
        Assert.assertEquals(ACTION_LABEL_KEY, row.get("action_label_key"));
        Assert.assertEquals(Boolean.FALSE, row.get("is_read"));

        final List<String> persistedArgs = jdbcTemplate.queryForList(
                "SELECT arg_value FROM site_notification_args WHERE notification_id = ? ORDER BY arg_index ASC",
                String.class,
                notification.getId()
        );
        Assert.assertEquals(BODY_ARGS, persistedArgs);
    }

    @Test
    public void testCreateWithOfferPersistsOfferId() {
        // Arrange
        final User user = userRef(USER_ID);
        final Offer offer = em.getReference(Offer.class, 6L);
        final Instant now = Instant.now();

        // Act
        final SiteNotification notification = notificationDao.create(
                user, NotificationType.OFFER_ACCEPTED, TITLE_KEY, BODY_KEY, BODY_ARGS, ACTION_URL, ACTION_LABEL_KEY, now, offer
        );
        em.flush();

        // Assert directly querying database table (no other DAO methods used)
        Assert.assertEquals(1, JdbcTestUtils.countRowsInTableWhere(
                jdbcTemplate, TABLE, "notification_id = " + notification.getId() + " AND offer_id = 6"
        ));
        Assert.assertNotNull(notification.getOffer().orElse(null));
        Assert.assertEquals(Long.valueOf(6L), notification.getOffer().get().getId());
    }

    /* ---------------------------------------------------------------------------------------------- */
    /* getMatching                                                                                    */
    /* ---------------------------------------------------------------------------------------------- */

    @Test
    public void testGetMatchingPaginationAndOrdering() {
        // Arrange with direct database inserts (including multi-element bodyArgs to verify no duplicate entities)
        final Instant baseTime = Instant.parse("2026-11-01T10:00:00Z");
        insertNotification(101L, USER_ID, NotificationType.NEW_OFFER, "k.t1", "k.b1", List.of("A1", "A2"), null, null, false, baseTime.plusSeconds(10));
        insertNotification(102L, USER_ID, NotificationType.OFFER_ACCEPTED, "k.t2", "k.b2", List.of("B1", "B2", "B3"), null, null, false, baseTime.plusSeconds(20));
        insertNotification(103L, USER_ID, NotificationType.OFFER_REJECTED, "k.t3", "k.b3", List.of("C1", "C2"), null, null, false, baseTime.plusSeconds(30));

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
        Assert.assertEquals(List.of("C1", "C2"), page1.getContent().get(0).getBodyArgs());
        Assert.assertEquals(102L, (long) page1.getContent().get(1).getId());
        Assert.assertEquals(List.of("B1", "B2", "B3"), page1.getContent().get(1).getBodyArgs());
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
        Assert.assertEquals(List.of("A1", "A2"), page2.getContent().getFirst().getBodyArgs());
        Assert.assertFalse(page2.isHasNext());
        Assert.assertTrue(page2.isHasPrevious());
    }

    @Test
    public void testGetMatchingUnreadOnly() {
        // Arrange with direct database inserts
        final Instant now = Instant.now();
        insertNotification(201L, USER_ID, NotificationType.NEW_OFFER, "k.unread", "k.body", List.of("X"), null, null, false, now.plusSeconds(10));
        insertNotification(202L, USER_ID, NotificationType.OFFER_ACCEPTED, "k.read", "k.body", List.of("Y"), null, null, true, now);

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
        insertNotification(301L, USER_ID, NotificationType.OFFER_ACCEPTED, "k.accepted", "k.body", List.of("150.00", "Item"), null, null, false, now.plusSeconds(10));
        insertNotification(302L, USER_ID, NotificationType.NEW_OFFER, "k.newOffer", "k.body", List.of("Bob", "150.00", "Item"), null, null, false, now);

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

    @Test
    public void testGetMatchingEagerlyFetchesOffer() {
        // Arrange with direct database inserts
        final Instant now = Instant.now();
        // Offer 6 in offer-data.sql is PENDING_PAYMENT -> showCallToAction should be true
        insertNotification(401L, USER_ID, NotificationType.OFFER_ACCEPTED, "k.accepted", "k.body", List.of(), "/payment", "cta", false, now.plusSeconds(10), 6L);
        // Offer 5 in offer-data.sql is ACCEPTED -> showCallToAction should be false
        insertNotification(402L, USER_ID, NotificationType.OFFER_ACCEPTED, "k.accepted", "k.body", List.of(), "/payment", "cta", false, now, 5L);

        // Act
        final SiteNotificationFilter filter = SiteNotificationFilter.builder()
                .userId(USER_ID)
                .page(1)
                .pageSize(10)
                .build();
        final Page<SiteNotification> page = notificationDao.getMatching(filter);

        // Assert
        Assert.assertEquals(2, page.getTotalCount());
        final SiteNotification pendingNotification = page.getContent().get(0);
        Assert.assertTrue(pendingNotification.getOffer().isPresent());
        Assert.assertEquals(Long.valueOf(6L), pendingNotification.getOffer().get().getId());
        Assert.assertTrue(pendingNotification.showCallToAction());

        final SiteNotification acceptedNotification = page.getContent().get(1);
        Assert.assertTrue(acceptedNotification.getOffer().isPresent());
        Assert.assertEquals(Long.valueOf(5L), acceptedNotification.getOffer().get().getId());
        Assert.assertFalse(acceptedNotification.showCallToAction());
    }

    /* ---------------------------------------------------------------------------------------------- */
    /* markAllAsReadForUser                                                                           */
    /* ---------------------------------------------------------------------------------------------- */

    @Test
    public void testMarkAllAsReadForUser() {
        // Arrange with direct database inserts
        final Instant now = Instant.now();
        insertNotification(501L, USER_ID, NotificationType.NEW_OFFER, "k.t1", "k.b1", List.of(), null, null, false, now);
        insertNotification(502L, USER_ID, NotificationType.NEW_OFFER, "k.t2", "k.b2", List.of(), null, null, false, now.plusSeconds(1));

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
        insertNotification(601L, USER_ID, TYPE, TITLE_KEY, BODY_KEY, BODY_ARGS, ACTION_URL, ACTION_LABEL_KEY, false, now);
        final SiteNotification notification = em.find(SiteNotification.class, 601L);

        // Act
        notificationDao.delete(notification);
        em.flush();

        // Assert directly querying database tables
        Assert.assertEquals(0, JdbcTestUtils.countRowsInTableWhere(
                jdbcTemplate, TABLE, "notification_id = 601"
        ));
        Assert.assertEquals(0, JdbcTestUtils.countRowsInTableWhere(
                jdbcTemplate, ARGS_TABLE, "notification_id = 601"
        ));
    }

    /* ---------------------------------------------------------------------------------------------- */
    /* getById                                                                                         */
    /* ---------------------------------------------------------------------------------------------- */

    @Test
    public void testGetByIdReturnsExistingNotification() {
        // Arrange with direct database insert
        final Instant now = Instant.now();
        insertNotification(701L, USER_ID, TYPE, TITLE_KEY, BODY_KEY, BODY_ARGS, ACTION_URL, ACTION_LABEL_KEY, false, now);

        // Act
        final var maybeNotification = notificationDao.getById(701L);

        // Assert
        Assert.assertTrue(maybeNotification.isPresent());
        Assert.assertEquals(701L, (long) maybeNotification.get().getId());
        Assert.assertEquals(TITLE_KEY, maybeNotification.get().getTitleKey());
        Assert.assertEquals(BODY_KEY, maybeNotification.get().getBodyKey());
        Assert.assertEquals(BODY_ARGS, maybeNotification.get().getBodyArgs());
    }

    @Test
    public void testGetByIdNonExistentReturnsEmpty() {
        // Act
        final var maybeNotification = notificationDao.getById(99999L);

        // Assert
        Assert.assertTrue(maybeNotification.isEmpty());
    }

    /* ---------------------------------------------------------------------------------------------- */
    /* markAsRead                                                                                      */
    /* ---------------------------------------------------------------------------------------------- */

    @Test
    public void testMarkAsReadMarksOnlyTargetNotification() {
        // Arrange with direct database inserts
        final Instant now = Instant.now();
        insertNotification(801L, USER_ID, TYPE, "k.target", BODY_KEY, List.of(), null, null, false, now);
        insertNotification(802L, USER_ID, TYPE, "k.other", BODY_KEY, List.of(), null, null, false, now.plusSeconds(1));
        final SiteNotification target = em.find(SiteNotification.class, 801L);

        // Act
        notificationDao.markAsRead(target);
        em.flush();
        em.clear();

        // Assert directly querying database table
        Assert.assertEquals(1, JdbcTestUtils.countRowsInTableWhere(
                jdbcTemplate, TABLE, "notification_id = 801 AND is_read = true"
        ));
        Assert.assertEquals(1, JdbcTestUtils.countRowsInTableWhere(
                jdbcTemplate, TABLE, "notification_id = 802 AND is_read = false"
        ));
    }
}
