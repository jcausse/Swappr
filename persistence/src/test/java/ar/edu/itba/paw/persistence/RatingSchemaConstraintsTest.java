package ar.edu.itba.paw.persistence;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.jdbc.JdbcTestUtils;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.sql.Timestamp;
import java.time.Instant;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { TestConfig.class })
@Sql({ "classpath:sql/listing-data.sql", "classpath:sql/offer-data.sql" })
@Transactional
@Rollback
public class RatingSchemaConstraintsTest {

    private static final long OFFER_ID = 7L;
    private static final long CREATOR_ID = 1L;
    private static final long RATED_ID = 2L;

    @Autowired
    private DataSource dataSource;

    private JdbcTemplate jdbcTemplate;

    @Before
    public void setUp() {
        jdbcTemplate = new JdbcTemplate(dataSource);
    }

    private void insertRating(final String role, final String type) {
        jdbcTemplate.update(
            "INSERT INTO ratings (creator_id, rated_id, offer_id, role, type, review_text, created_at)"
                + " VALUES (?, ?, ?, ?, ?, ?, ?)",
            CREATOR_ID, RATED_ID, OFFER_ID, role, type, "Review", Timestamp.from(Instant.now())
        );
    }

    @Test
    public void testExistingUppercaseValuesSatisfyConstraints() {
        insertRating("BUYER", "POSITIVE");

        Assert.assertEquals(1, JdbcTestUtils.countRowsInTable(jdbcTemplate, "ratings"));
    }

    @Test(expected = DataIntegrityViolationException.class)
    public void testInvalidRoleIsRejected() {
        insertRating("INVALID", "POSITIVE");
    }

    @Test(expected = DataIntegrityViolationException.class)
    public void testInvalidTypeIsRejected() {
        insertRating("BUYER", "INVALID");
    }
}
