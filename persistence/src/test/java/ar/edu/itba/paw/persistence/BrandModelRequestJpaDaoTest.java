package ar.edu.itba.paw.persistence;

import ar.edu.itba.paw.model.Brand;
import ar.edu.itba.paw.model.BrandModelRequest;
import ar.edu.itba.paw.model.BrandModelRequestType;
import ar.edu.itba.paw.model.Page;
import ar.edu.itba.paw.model.Subcategory;
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
import java.util.Optional;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { TestConfig.class })
@Transactional
@Rollback
public class BrandModelRequestJpaDaoTest {

    private static final String TABLE = "brand_model_requests";
    private static final long USER_ID = 1L;
    private static final long SUBCATEGORY_ID = 1L;
    private static final long BRAND_ID = 1L;
    private static final long NON_EXISTING_ID = 9000L;

    @Autowired
    private BrandModelRequestDao brandModelRequestDao;

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

    private void insertRequest(
            final long id,
            final BrandModelRequestType type,
            final Long brandId,
            final String brandName,
            final String modelName,
            final Instant createdAt
    ) {
        jdbcTemplate.update(
                "INSERT INTO brand_model_requests"
                + " (request_id, request_type, brand_id, brand_name, model_name, subcategory_id, requester_id, created_at)"
                + " VALUES (?, ?, ?, ?, ?, ?, ?, ?)",
                id,
                type.name(),
                brandId,
                brandName,
                modelName,
                SUBCATEGORY_ID,
                USER_ID,
                Timestamp.from(createdAt)
        );
    }

    private void insertSampleRequests() {
        insertRequest(701L, BrandModelRequestType.NEW_BRAND, null, "Acme", "Acme One",
                Instant.parse("2026-01-01T10:00:00Z"));
        insertRequest(702L, BrandModelRequestType.EXISTING_BRAND, BRAND_ID, null, "MacBook Pro",
                Instant.parse("2026-01-03T10:00:00Z"));
        insertRequest(703L, BrandModelRequestType.NEW_BRAND, null, "Zenith", "Zenith X",
                Instant.parse("2026-01-02T10:00:00Z"));
    }

    @Test
    public void testGetMatchingFirstPageIsOrderedNewestFirst() {
        insertSampleRequests();

        final Page<BrandModelRequest> page = brandModelRequestDao.getMatching(1, 2);

        Assert.assertEquals(3, page.getTotalCount());
        Assert.assertEquals(2, page.getContent().size());
        Assert.assertEquals(List.of(702L, 703L),
                page.getContent().stream().map(BrandModelRequest::getId).toList());
        Assert.assertEquals("MacBook Pro", page.getContent().get(0).getModelName());
    }

    @Test
    public void testGetMatchingSecondPage() {
        insertSampleRequests();

        final Page<BrandModelRequest> page = brandModelRequestDao.getMatching(2, 2);

        Assert.assertEquals(3, page.getTotalCount());
        Assert.assertEquals(List.of(701L),
                page.getContent().stream().map(BrandModelRequest::getId).toList());
    }

    @Test
    public void testGetMatchingEmpty() {
        final Page<BrandModelRequest> page = brandModelRequestDao.getMatching(1, 10);

        Assert.assertEquals(0, page.getTotalCount());
        Assert.assertTrue(page.getContent().isEmpty());
    }

    @Test
    public void testGetByIdFetchesAssociations() {
        insertSampleRequests();

        final Optional<BrandModelRequest> maybeRequest = brandModelRequestDao.getById(702L);

        Assert.assertTrue(maybeRequest.isPresent());
        final BrandModelRequest request = maybeRequest.get();
        Assert.assertEquals(BrandModelRequestType.EXISTING_BRAND, request.getType());
        Assert.assertEquals(BRAND_ID, (long) request.getBrandId());
        Assert.assertEquals("Apple", request.getRequestedBrandName());
        Assert.assertEquals("MacBook Pro", request.getModelName());
        Assert.assertEquals(SUBCATEGORY_ID, (long) request.getSubcategory().getId());
        Assert.assertEquals(USER_ID, (long) request.getRequester().getId());
    }

    @Test
    public void testGetByIdDoesNotExist() {
        Assert.assertFalse(brandModelRequestDao.getById(NON_EXISTING_ID).isPresent());
    }

    @Test
    public void testCreate() {
        final Brand brand = em.getReference(Brand.class, BRAND_ID);
        final Subcategory subcategory = em.getReference(Subcategory.class, SUBCATEGORY_ID);
        final User requester = em.getReference(User.class, USER_ID);

        final BrandModelRequest created = brandModelRequestDao.create(
                BrandModelRequestType.EXISTING_BRAND,
                brand,
                null,
                "MacBook Air",
                subcategory,
                requester
        );
        em.flush();

        Assert.assertNotNull(created.getId());
        Assert.assertEquals(1, JdbcTestUtils.countRowsInTableWhere(
                jdbcTemplate,
                TABLE,
                "model_name = 'MacBook Air' AND request_type = 'EXISTING_BRAND' AND brand_id = " + BRAND_ID
        ));
    }

    @Test
    public void testCreateNewBrand() {
        final Subcategory subcategory = em.getReference(Subcategory.class, SUBCATEGORY_ID);
        final User requester = em.getReference(User.class, USER_ID);

        brandModelRequestDao.create(
                BrandModelRequestType.NEW_BRAND,
                null,
                "Acme",
                "Acme One",
                subcategory,
                requester
        );
        em.flush();

        Assert.assertEquals(1, JdbcTestUtils.countRowsInTableWhere(
                jdbcTemplate,
                TABLE,
                "model_name = 'Acme One' AND request_type = 'NEW_BRAND' AND brand_id IS NULL AND brand_name = 'Acme'"
        ));
    }

    @Test
    public void testDelete() {
        insertRequest(701L, BrandModelRequestType.NEW_BRAND, null, "Acme", "Acme One",
                Instant.parse("2026-01-01T10:00:00Z"));

        Assert.assertTrue(brandModelRequestDao.delete(701L));
        em.flush();
        Assert.assertEquals(0, JdbcTestUtils.countRowsInTable(jdbcTemplate, TABLE));
    }

    @Test
    public void testDeleteDoesNotExist() {
        Assert.assertFalse(brandModelRequestDao.delete(NON_EXISTING_ID));
    }
}
