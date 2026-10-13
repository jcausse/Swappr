package ar.edu.itba.paw.persistence;

import ar.edu.itba.paw.model.OneTimePassword;
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
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { TestConfig.class })
@Sql({ "classpath:sql/otp-data.sql" })
@Transactional
@Rollback
public class OneTimePasswordJpaDaoTest {

    private static final String TABLE = "one_time_passwords";

    // Users: 1 from initial-data.sql, 2 from sql/otp-data.sql
    private static final long USER_ID = 1L;
    private static final long OTHER_USER_ID = 2L;
    private static final long NON_EXISTING_USER_ID = 9000L;

    private static final String OTP_VALUE = "123456";

    @Autowired
    private OneTimePasswordDao otpDao;

    @Autowired
    private DataSource dataSource;

    private JdbcTemplate jdbcTemplate;
    @PersistenceContext
    private EntityManager em;

    @Before
    public void setUp() {
        jdbcTemplate = new JdbcTemplate(dataSource);
    }

    /* ---------------------------------------------------------------------------------------------- */
    /* Fixtures & helpers                                                                              */
    /* ---------------------------------------------------------------------------------------------- */

    private static User userRef(final long id) {
        return User.builder()
            .id(id)
            .username("user_" + id)
            .displayName("User " + id)
            .email("user_" + id + "@example.com")
            .password("secret")
            .joinedAt(Instant.now())
            .build();
    }

    private void insertOtp(final long requesterId, final String value, final Instant createdAt) {
        jdbcTemplate.update(
            "INSERT INTO one_time_passwords (requester_id, otp_value, created_at) VALUES (?, ?, ?)",
            requesterId, value, Timestamp.from(createdAt)
        );
    }

    private int countOtpWhere(final String whereClause) {
        em.flush();
        return JdbcTestUtils.countRowsInTableWhere(jdbcTemplate, TABLE, whereClause);
    }

    /* ---------------------------------------------------------------------------------------------- */
    /* getByUser                                                                                       */
    /* ---------------------------------------------------------------------------------------------- */

    @Test
    public void testGetByUserExists() {
        // Arrange
        final Instant createdAt = Instant.now().truncatedTo(ChronoUnit.SECONDS);
        insertOtp(USER_ID, OTP_VALUE, createdAt);

        // Act
        final Optional<OneTimePassword> maybeOtp = otpDao.getByUser(userRef(USER_ID));

        // Assert
        Assert.assertTrue(maybeOtp.isPresent());
        Assert.assertEquals(USER_ID, (long) maybeOtp.get().getId());
        Assert.assertEquals(OTP_VALUE, maybeOtp.get().getOtpValue());
        Assert.assertEquals(createdAt, maybeOtp.get().getCreatedAt());
    }

    @Test
    public void testGetByUserDoesNotExist() {
        // Act
        final Optional<OneTimePassword> maybeOtp = otpDao.getByUser(userRef(USER_ID));

        // Assert
        Assert.assertFalse(maybeOtp.isPresent());
    }

    @Test
    public void testGetByUserReturnsOnlyThatUsersOtp() {
        // Arrange
        insertOtp(USER_ID, "111111", Instant.now());
        insertOtp(OTHER_USER_ID, "222222", Instant.now());

        // Act
        final OneTimePassword otp = otpDao.getByUser(userRef(OTHER_USER_ID)).orElseThrow();

        // Assert
        Assert.assertEquals(OTHER_USER_ID, (long) otp.getId());
        Assert.assertEquals("222222", otp.getOtpValue());
    }

    /* ---------------------------------------------------------------------------------------------- */
    /* create                                                                                          */
    /* ---------------------------------------------------------------------------------------------- */

    @Test
    public void testCreateReturnsCreatedOtp() {
        // Arrange
        final Instant createdAt = Instant.now();
        final User user = userRef(USER_ID);

        // Act
        final OneTimePassword otp = otpDao.create(user, OTP_VALUE, createdAt);

        // Assert
        Assert.assertEquals(USER_ID, (long) otp.getId());
        Assert.assertEquals(OTP_VALUE, otp.getOtpValue());
        Assert.assertEquals(createdAt, otp.getCreatedAt());
    }

    @Test
    public void testCreatePersistsOtp() {
        // Act
        otpDao.create(userRef(USER_ID), OTP_VALUE, Instant.now());

        // Assert
        Assert.assertEquals(1, countOtpWhere(
            "requester_id = " + USER_ID + " AND otp_value = '" + OTP_VALUE + "'"));
    }

    /* ---------------------------------------------------------------------------------------------- */
    /* deleteIfPresentByUser                                                                           */
    /* ---------------------------------------------------------------------------------------------- */

    @Test
    public void testDeleteIfPresentRemovesUsersOtp() {
        // Arrange
        insertOtp(USER_ID, OTP_VALUE, Instant.now());

        // Act
        otpDao.deleteIfPresentByUser(userRef(USER_ID));

        // Assert
        Assert.assertEquals(0, countOtpWhere("requester_id = " + USER_ID));
    }

    @Test
    public void testDeleteIfPresentOnlyRemovesGivenUsersOtp() {
        // Arrange
        insertOtp(USER_ID, "111111", Instant.now());
        insertOtp(OTHER_USER_ID, "222222", Instant.now());

        // Act
        otpDao.deleteIfPresentByUser(userRef(USER_ID));

        // Assert
        Assert.assertEquals(0, countOtpWhere("requester_id = " + USER_ID));
        Assert.assertEquals(1, countOtpWhere("requester_id = " + OTHER_USER_ID));
    }

    @Test
    public void testDeleteIfPresentNoOtpDoesNothing() {
        // Act (user has no OTP)
        otpDao.deleteIfPresentByUser(userRef(USER_ID));

        // Assert
        em.flush();
        Assert.assertEquals(0, JdbcTestUtils.countRowsInTable(jdbcTemplate, TABLE));
    }

    /* ---------------------------------------------------------------------------------------------- */
    /* deleteOlderThan                                                                                 */
    /* ---------------------------------------------------------------------------------------------- */

    @Test
    public void testDeleteOlderThanRemovesStaleOtps() {
        // Arrange
        final Instant now = Instant.now();
        insertOtp(USER_ID, "old", now.minus(2, ChronoUnit.HOURS));
        insertOtp(OTHER_USER_ID, "fresh", now);

        // Act: cutoff one hour ago -> only the 2h-old OTP is stale
        otpDao.deleteOlderThan(now.minus(1, ChronoUnit.HOURS));

        // Assert
        Assert.assertEquals(0, countOtpWhere("requester_id = " + USER_ID));
        Assert.assertEquals(1, countOtpWhere("requester_id = " + OTHER_USER_ID));
    }

    @Test
    public void testDeleteOlderThanKeepsRecentOtps() {
        // Arrange
        final Instant now = Instant.now();
        insertOtp(USER_ID, OTP_VALUE, now);

        // Act: cutoff in the past -> nothing deleted
        otpDao.deleteOlderThan(now.minus(1, ChronoUnit.HOURS));

        // Assert
        Assert.assertEquals(1, countOtpWhere("requester_id = " + USER_ID));
    }
}
