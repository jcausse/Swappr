package ar.edu.itba.paw.persistence;

import ar.edu.itba.paw.model.OfferRating;
import ar.edu.itba.paw.model.Language;
import ar.edu.itba.paw.model.User;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.jdbc.JdbcTestUtils;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.time.Instant;
import java.util.Optional;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { TestConfig.class })
@Sql({ "classpath:sql/user-data.sql" })
@Transactional
@Rollback
public class UserJpaDaoTest {

    // Fixtures: user 1 from initial-data.sql, users 2-3 from sql/user-data.sql
    private static final long FAKE_USER_ID = 1L;            // fake_user, unverified, no province
    private static final long VERIFIED_USER_ID = 2L;        // verified_user, image 10, CABA + "Belgrano", ratings
    private static final long PLAIN_USER_ID = 3L;           // plain_user, unverified, no image, no province
    private static final long NON_EXISTING_USER_ID = 9000L;

    private static final String FAKE_USERNAME = "fake_user";
    private static final String FAKE_EMAIL = "fake@example.com";
    private static final String VERIFIED_USERNAME = "verified_user";
    private static final String VERIFIED_EMAIL = "verified@example.com";

    private static final long AVATAR_IMAGE_ID = 10L;
    private static final int TOTAL_USERS = 3;

    @Autowired
    private UserDao userDao;

    @Autowired
    private DataSource dataSource;

    private JdbcTemplate jdbcTemplate;

    @Before
    public void setUp() {
        jdbcTemplate = new JdbcTemplate(dataSource);
    }

    private int countUsersWhere(final String whereClause) {
        return JdbcTestUtils.countRowsInTableWhere(jdbcTemplate, "users", whereClause);
    }

    /* ---------------------------------------------------------------------------------------------- */
    /* create                                                                                          */
    /* ---------------------------------------------------------------------------------------------- */

    @Test
    public void testCreateReturnsUserWithId() {
        final User created = userDao.create(
            "new_user", "New User", "new@example.com", "secret", null, Instant.now(), Language.ENGLISH);

        Assert.assertNotNull(created.getId());
        Assert.assertEquals("new_user", created.getUsername());
        Assert.assertEquals("new@example.com", created.getEmail());
        Assert.assertFalse(created.isVerified());
    }

    @Test
    public void testCreatePersistsUser() {
        userDao.create("new_user", "New User", "new@example.com", "secret", null, Instant.now(), Language.ENGLISH);

        final int count = countUsersWhere(
            "username = 'new_user' AND email = 'new@example.com'");
        Assert.assertEquals(1, count);
    }

    @Test
    public void testCreateInitializesRatingCountersToZero() {
        final User created = userDao.create(
            "new_user", "New User", "new@example.com", "secret", null, Instant.now(), Language.ENGLISH);

        Assert.assertEquals(0, created.getSellerTotalRatings());
        Assert.assertEquals(0, created.getBuyerTotalRatings());
    }

    /* ---------------------------------------------------------------------------------------------- */
    /* update                                                                                          */
    /* ---------------------------------------------------------------------------------------------- */

    @Test
    public void testUpdateNonExistingReturnsEmpty() {
        final Optional<User> updated = userDao.update(NON_EXISTING_USER_ID, "Nope", null, null, null);

        Assert.assertFalse(updated.isPresent());
    }

    @Test
    public void testUpdateWithNoFieldsReturnsEmpty() {
        final Optional<User> updated = userDao.update(FAKE_USER_ID, null, null, null, null);

        Assert.assertFalse(updated.isPresent());
    }

    /* ---------------------------------------------------------------------------------------------- */
    /* verifyEmail                                                                                     */
    /* ---------------------------------------------------------------------------------------------- */

    @Test
    public void testVerifyEmailNonExistingReturnsEmpty() {
        final Optional<User> verified = userDao.verifyEmail(NON_EXISTING_USER_ID, Instant.now());

        Assert.assertFalse(verified.isPresent());
    }

    /* ---------------------------------------------------------------------------------------------- */
    /* updateLocation                                                                                  */
    /* ---------------------------------------------------------------------------------------------- */

    private Long provinceId(final String name) {
        return jdbcTemplate.queryForObject(
            "SELECT province_id FROM provinces WHERE name = ?", Long.class, name);
    }

    @Test
    public void testUpdateLocationNonExistingReturnsEmpty() {
        final Long cordoba = provinceId("cordoba");

        final Optional<User> updated = userDao.updateLocation(NON_EXISTING_USER_ID, cordoba, null);

        Assert.assertFalse(updated.isPresent());
    }

    /* ---------------------------------------------------------------------------------------------- */
    /* isUsernameTaken / isEmailTaken                                                                  */
    /* ---------------------------------------------------------------------------------------------- */

    @Test
    public void testIsUsernameTakenTrue() {
        Assert.assertTrue(userDao.isUsernameTaken(FAKE_USERNAME));
    }

    @Test
    public void testIsUsernameTakenFalse() {
        Assert.assertFalse(userDao.isUsernameTaken("ghost"));
    }

    @Test
    public void testIsEmailTakenTrue() {
        Assert.assertTrue(userDao.isEmailTaken(FAKE_EMAIL));
    }

    @Test
    public void testIsEmailTakenFalse() {
        Assert.assertFalse(userDao.isEmailTaken("ghost@example.com"));
    }

    /* ---------------------------------------------------------------------------------------------- */
    /* rating counters                                                                                 */
    /* ---------------------------------------------------------------------------------------------- */

    @Test
    public void testIncrementSellerRatingCounter() {
        userDao.incrementSellerRatingCounter(VERIFIED_USER_ID, OfferRating.POSITIVE);

        // started at 3 positive seller ratings
        Assert.assertEquals(1, countUsersWhere(
            "user_id = " + VERIFIED_USER_ID + " AND seller_positive_ratings = 4"));
    }

    @Test
    public void testIncrementBuyerRatingCounter() {
        userDao.incrementBuyerRatingCounter(VERIFIED_USER_ID, OfferRating.NEGATIVE);

        // started at 1 negative buyer rating
        Assert.assertEquals(1, countUsersWhere(
            "user_id = " + VERIFIED_USER_ID + " AND buyer_negative_ratings = 2"));
    }

    /* ---------------------------------------------------------------------------------------------- */
    /* preferredLanguage                                                                               */
    /* ---------------------------------------------------------------------------------------------- */

    @Test
    public void testCreatePersistsPreferredLanguage() {
        final User created = userDao.create(
            "es_user", "ES User", "es@example.com", "secret", null, Instant.now(), Language.SPANISH);

        Assert.assertEquals(Language.SPANISH, created.getPreferredLanguage());
        Assert.assertEquals(1, countUsersWhere(
            "user_id = " + created.getId() + " AND preferred_language = 'SPANISH'"));
    }

    @Test
    public void testUpdatePreferredLanguageNonExistingReturnsEmpty() {
        final Optional<User> updated = userDao.updatePreferredLanguage(NON_EXISTING_USER_ID, Language.SPANISH);

        Assert.assertFalse(updated.isPresent());
    }
}
