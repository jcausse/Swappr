package ar.edu.itba.paw.persistence;

import ar.edu.itba.paw.model.notification.NotificationMode;
import ar.edu.itba.paw.model.notification.NotificationType;
import ar.edu.itba.paw.model.User;
import ar.edu.itba.paw.model.notification.UserNotificationPreference;
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
import java.util.List;
import java.util.Optional;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { TestConfig.class })
@Transactional
@Rollback
public class UserNotificationPreferenceJpaDaoTest {

    private static final String TABLE = "user_notification_preferences";
    private static final long USER_ID = 1L; // fake_user from initial-data.sql

    @Autowired
    private UserNotificationPreferenceDao preferenceDao;

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

    private User userRef(final Long id) {
        return em.getReference(User.class, id);
    }

    private void insertPreference(
            final long userId,
            final NotificationType type,
            final NotificationMode mode
    ) {
        jdbcTemplate.update(
                "INSERT INTO user_notification_preferences (user_id, notification_type, mode) VALUES (?, ?, ?)",
                userId,
                type.name(),
                mode.name()
        );
    }

    @Test
    public void testGetByUserAndTypeReturnsEmptyWhenNotPresent() {
        final Optional<UserNotificationPreference> result =
                preferenceDao.getByUserAndType(userRef(USER_ID), NotificationType.NEW_OFFER);

        Assert.assertTrue(result.isEmpty());
    }

    @Test
    public void testGetByUserAndTypeReturnsStoredPreference() {
        insertPreference(USER_ID, NotificationType.NEW_OFFER, NotificationMode.SITE_ONLY);

        final Optional<UserNotificationPreference> result =
                preferenceDao.getByUserAndType(userRef(USER_ID), NotificationType.NEW_OFFER);

        Assert.assertTrue(result.isPresent());
        Assert.assertEquals(NotificationType.NEW_OFFER, result.get().getType());
        Assert.assertEquals(NotificationMode.SITE_ONLY, result.get().getMode());
    }

    @Test
    public void testGetByUserReturnsAllStoredPreferencesForUser() {
        insertPreference(USER_ID, NotificationType.NEW_OFFER, NotificationMode.EMAIL_ONLY);
        insertPreference(USER_ID, NotificationType.LISTING_PUBLISHED, NotificationMode.DISABLED);

        final List<UserNotificationPreference> result = preferenceDao.getByUser(userRef(USER_ID));

        Assert.assertEquals(2, result.size());
    }

    @Test
    public void testSaveOrUpdateInsertsNewPreference() {
        preferenceDao.saveOrUpdate(userRef(USER_ID), NotificationType.NEW_OFFER, NotificationMode.DISABLED);
        em.flush();

        Assert.assertEquals(1, JdbcTestUtils.countRowsInTableWhere(
                jdbcTemplate,
                TABLE,
                "user_id = " + USER_ID + " AND notification_type = 'NEW_OFFER' AND mode = 'DISABLED'"
        ));
    }

    @Test
    public void testSaveOrUpdateUpdatesExistingPreference() {
        insertPreference(USER_ID, NotificationType.NEW_OFFER, NotificationMode.SITE_AND_EMAIL);

        preferenceDao.saveOrUpdate(userRef(USER_ID), NotificationType.NEW_OFFER, NotificationMode.SITE_ONLY);
        em.flush();

        Assert.assertEquals(1, JdbcTestUtils.countRowsInTableWhere(
                jdbcTemplate,
                TABLE,
                "user_id = " + USER_ID
        ));
        Assert.assertEquals(1, JdbcTestUtils.countRowsInTableWhere(
                jdbcTemplate,
                TABLE,
                "user_id = " + USER_ID + " AND notification_type = 'NEW_OFFER' AND mode = 'SITE_ONLY'"
        ));
    }
}
