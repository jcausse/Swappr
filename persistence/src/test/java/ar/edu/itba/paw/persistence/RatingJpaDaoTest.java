package ar.edu.itba.paw.persistence;

import ar.edu.itba.paw.model.OfferRating;
import ar.edu.itba.paw.model.Page;
import ar.edu.itba.paw.model.Rating;
import ar.edu.itba.paw.model.RatingFilter;
import ar.edu.itba.paw.model.RatingRole;
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

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceException;
import javax.sql.DataSource;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { TestConfig.class })
@Sql({ "classpath:sql/listing-data.sql", "classpath:sql/offer-data.sql" })
@Transactional
@Rollback
public class RatingJpaDaoTest {

    // Fixture ids (see sql/listing-data.sql and sql/offer-data.sql)
    private static final long ACCEPTED_OFFER_ID = 7L;
    private static final long CREATOR_ID = 1L;
    private static final long RATED_ID = 2L;
    private static final String TABLE = "ratings";

    @Autowired
    private RatingDao ratingDao;

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

    private void insertRating(final long offerId, final String role) {
        insertRating(offerId, role, OfferRating.POSITIVE, Instant.now());
    }

    private void insertRating(final long offerId, final String role, final OfferRating type, final Instant createdAt) {
        jdbcTemplate.update(
            "INSERT INTO ratings (creator_id, rated_id, offer_id, role, type, review_text, created_at) VALUES (?, ?, ?, ?, ?, ?, ?)",
            CREATOR_ID, RATED_ID, offerId, role, type.name(), "Existing review", Timestamp.from(createdAt)
        );
    }

    /* ---------------------------------------------------------------------------------------------- */
    /* create                                                                                          */
    /* ---------------------------------------------------------------------------------------------- */

    @Test
    public void testCreatePersistsRating() {
        // Act
        ratingDao.create(CREATOR_ID, RATED_ID, ACCEPTED_OFFER_ID, RatingRole.BUYER,
            OfferRating.POSITIVE, "Great trade", Instant.now());

        // Assert
        em.flush();
        Assert.assertEquals(1, JdbcTestUtils.countRowsInTableWhere(jdbcTemplate, TABLE,
            "offer_id = " + ACCEPTED_OFFER_ID + " AND role = 'BUYER' AND review_text = 'Great trade'"));
    }

    @Test
    public void testCreateAllowsBothRolesForSameOffer() {
        // Arrange
        insertRating(ACCEPTED_OFFER_ID, RatingRole.BUYER.name());

        // Act
        ratingDao.create(CREATOR_ID, RATED_ID, ACCEPTED_OFFER_ID, RatingRole.SELLER,
            OfferRating.POSITIVE, "Seller review", Instant.now());

        // Assert
        em.flush();
        Assert.assertEquals(2, JdbcTestUtils.countRowsInTableWhere(jdbcTemplate, TABLE,
            "offer_id = " + ACCEPTED_OFFER_ID));
    }

    @Test(expected = PersistenceException.class)
    public void testCreateRejectsDuplicateOfferAndRole() {
        // Arrange
        insertRating(ACCEPTED_OFFER_ID, RatingRole.BUYER.name());

        // Act
        ratingDao.create(CREATOR_ID, RATED_ID, ACCEPTED_OFFER_ID, RatingRole.BUYER,
            OfferRating.POSITIVE, "Duplicate review", Instant.now());
        em.flush();
    }

    /* ---------------------------------------------------------------------------------------------- */
    /* search                                                                                          */
    /* ---------------------------------------------------------------------------------------------- */

    @Test
    public void testSearchFirstPageOrdersByCreatedAtDesc() {
        insertRating(5L, RatingRole.BUYER.name(), OfferRating.POSITIVE, Instant.parse("2026-01-01T10:00:00Z"));
        insertRating(6L, RatingRole.BUYER.name(), OfferRating.NEUTRAL, Instant.parse("2026-01-02T10:00:00Z"));
        insertRating(7L, RatingRole.SELLER.name(), OfferRating.NEGATIVE, Instant.parse("2026-01-03T10:00:00Z"));
        final RatingFilter filter = RatingFilter.builder()
            .ratedId(RATED_ID)
            .page(1)
            .pageSize(2)
            .build();

        final Page<Rating> page = ratingDao.search(filter);

        Assert.assertEquals(3, page.getTotalCount());
        Assert.assertEquals(2, page.getTotalPages());
        Assert.assertEquals(List.of(7L, 6L), offerIdsOf(page));
    }

    @Test
    public void testSearchSecondPageReturnsRemainingMatches() {
        insertRating(5L, RatingRole.BUYER.name(), OfferRating.POSITIVE, Instant.parse("2026-01-01T10:00:00Z"));
        insertRating(6L, RatingRole.BUYER.name(), OfferRating.NEUTRAL, Instant.parse("2026-01-02T10:00:00Z"));
        insertRating(7L, RatingRole.SELLER.name(), OfferRating.NEGATIVE, Instant.parse("2026-01-03T10:00:00Z"));
        final RatingFilter filter = RatingFilter.builder()
            .ratedId(RATED_ID)
            .page(2)
            .pageSize(2)
            .build();

        final Page<Rating> page = ratingDao.search(filter);

        Assert.assertEquals(3, page.getTotalCount());
        Assert.assertEquals(2, page.getPage());
        Assert.assertEquals(List.of(5L), offerIdsOf(page));
    }

    @Test
    public void testSearchOutOfRangePageIsEmptyButKeepsTotal() {
        insertRating(5L, RatingRole.BUYER.name(), OfferRating.POSITIVE, Instant.parse("2026-01-01T10:00:00Z"));
        final RatingFilter filter = RatingFilter.builder()
            .ratedId(RATED_ID)
            .page(10)
            .pageSize(2)
            .build();

        final Page<Rating> page = ratingDao.search(filter);

        Assert.assertTrue(page.isEmpty());
        Assert.assertEquals(1, page.getTotalCount());
    }

    private static List<Long> offerIdsOf(final Page<Rating> page) {
        return page.getContent().stream()
            .map(rating -> rating.getOffer().getId())
            .toList();
    }
}
