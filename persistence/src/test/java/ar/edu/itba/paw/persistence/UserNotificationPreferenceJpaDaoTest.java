package ar.edu.itba.paw.persistence;

import ar.edu.itba.paw.model.User;
import ar.edu.itba.paw.model.notification.NotificationChannel;
import ar.edu.itba.paw.model.notification.NotificationType;
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
            final NotificationChannel channel,
            final boolean enabled
    ) {
        jdbcTemplate.update(
                "INSERT INTO user_notification_preferences (user_id, notification_type, notification_channel, is_enabled) VALUES (?, ?, ?, ?)",
                userId,
                type.name(),
                channel.name(),
                enabled
        );
    }

    @Test
    public void testGetByUserTypeAndChannelReturnsEmptyWhenNotPresent() {
        final Optional<UserNotificationPreference> result =
                preferenceDao.getByUserTypeAndChannel(userRef(USER_ID), NotificationType.NEW_OFFER, NotificationChannel.SITE);

        Assert.assertTrue(result.isEmpty());
    }

    @Test
    public void testGetByUserTypeAndChannelReturnsStoredPreference() {
        insertPreference(USER_ID, NotificationType.NEW_OFFER, NotificationChannel.SITE, false);

        final Optional<UserNotificationPreference> result =
                preferenceDao.getByUserTypeAndChannel(userRef(USER_ID), NotificationType.NEW_OFFER, NotificationChannel.SITE);

        Assert.assertTrue(result.isPresent());
        Assert.assertEquals(NotificationType.NEW_OFFER, result.get().getId().getType());
        Assert.assertEquals(NotificationChannel.SITE, result.get().getId().getChannel());
        Assert.assertFalse(result.get().isEnabled());
    }

    @Test
    public void testGetByUserReturnsAllStoredPreferencesForUser() {
        insertPreference(USER_ID, NotificationType.NEW_OFFER, NotificationChannel.EMAIL, false);
        insertPreference(USER_ID, NotificationType.LISTING_PUBLISHED, NotificationChannel.EMAIL, true);

        final List<UserNotificationPreference> result = preferenceDao.getByUser(userRef(USER_ID));

        Assert.assertEquals(2, result.size());
    }

    @Test
    public void testSaveOrUpdateInsertsNewPreference() {
        preferenceDao.saveOrUpdate(userRef(USER_ID), NotificationType.NEW_OFFER, NotificationChannel.SITE, false);
        em.flush();

        Assert.assertEquals(1, JdbcTestUtils.countRowsInTableWhere(
                jdbcTemplate,
                TABLE,
                "user_id = " + USER_ID + " AND notification_type = 'NEW_OFFER' AND notification_channel = 'SITE' AND is_enabled = FALSE"
        ));
    }

    @Test
    public void testSaveOrUpdateUpdatesExistingPreference() {
        insertPreference(USER_ID, NotificationType.NEW_OFFER, NotificationChannel.EMAIL, true);

        preferenceDao.saveOrUpdate(userRef(USER_ID), NotificationType.NEW_OFFER, NotificationChannel.EMAIL, false);
        em.flush();

        Assert.assertEquals(1, JdbcTestUtils.countRowsInTableWhere(
                jdbcTemplate,
                TABLE,
                "user_id = " + USER_ID
        ));
        Assert.assertEquals(1, JdbcTestUtils.countRowsInTableWhere(
                jdbcTemplate,
                TABLE,
                "user_id = " + USER_ID + " AND notification_type = 'NEW_OFFER' AND notification_channel = 'EMAIL' AND is_enabled = FALSE"
        ));
    }
}
