package ar.edu.itba.paw.persistence;

import ar.edu.itba.paw.model.Condition;
import ar.edu.itba.paw.model.ModelListingInfo;
import ar.edu.itba.paw.model.ModelListingInfoSummary;
import ar.edu.itba.paw.model.Page;
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
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { TestConfig.class })
@Sql("classpath:sql/listing-data.sql")
@Transactional
@Rollback
public class ModelListingInfoJpaDaoTest {

    private static final long MACBOOK_MODEL_ID = 1L;
    private static final long EMPTY_MODEL_ID = 100L;
    private static final long NON_EXISTING_MODEL_ID = 9000L;

    private static final long APPLE_BRAND_ID = 1L;
    private static final long LAPTOPS_SUBCATEGORY_ID = 1L;

    @Autowired
    private ModelDao modelDao;

    @Autowired
    private DataSource dataSource;

    private JdbcTemplate jdbcTemplate;

    @Before
    public void setUp() {
        jdbcTemplate = new JdbcTemplate(dataSource);
    }

    /* ---------------------------------------------------------------------------------------------- */
    /* getListingInfoSummaryForModel                                                                   */
    /* ---------------------------------------------------------------------------------------------- */

    @Test
    public void testGetListingInfoSummaryForModelAggregatesActiveListingsByCondition() {
        final Optional<ModelListingInfoSummary> maybeSummary =
                modelDao.getListingInfoSummaryForModel(MACBOOK_MODEL_ID, null);

        Assert.assertTrue(maybeSummary.isPresent());
        final ModelListingInfoSummary summary = maybeSummary.get();
        Assert.assertEquals("Apple", summary.getBrandName());
        Assert.assertEquals("MacBook Pro", summary.getModelName());
        Assert.assertEquals(2, summary.getListingInfoByCondition().size());

        final Map<Condition, ModelListingInfo> byCondition = summary.getListingInfoByCondition().stream()
                .collect(Collectors.toMap(ModelListingInfo::getCondition, Function.identity()));

        final ModelListingInfo good = byCondition.get(Condition.GOOD);
        Assert.assertNotNull(good);
        Assert.assertEquals(1L, (long) good.getListingCount());
        Assert.assertEquals(0, new BigDecimal("1500.00").compareTo(good.getMinPrice().getAmount()));
        Assert.assertEquals(0, new BigDecimal("1500.00").compareTo(good.getMaxPrice().getAmount()));

        final ModelListingInfo excellent = byCondition.get(Condition.EXCELLENT);
        Assert.assertNotNull(excellent);
        Assert.assertEquals(1L, (long) excellent.getListingCount());
        Assert.assertEquals(0, new BigDecimal("2000.00").compareTo(excellent.getMinPrice().getAmount()));
        Assert.assertEquals(0, new BigDecimal("2000.00").compareTo(excellent.getMaxPrice().getAmount()));

        Assert.assertFalse(byCondition.containsKey(Condition.FOR_PARTS));
        Assert.assertEquals(
                List.of(Condition.EXCELLENT, Condition.GOOD),
                summary.getListingInfoByCondition().stream()
                        .map(ModelListingInfo::getCondition)
                        .toList()
        );
    }

    @Test
    public void testGetListingInfoSummaryForModelExcludesListingBeingEdited() {
        final Optional<ModelListingInfoSummary> maybeSummary =
                modelDao.getListingInfoSummaryForModel(MACBOOK_MODEL_ID, 1L);

        Assert.assertTrue(maybeSummary.isPresent());
        final ModelListingInfoSummary summary = maybeSummary.get();
        Assert.assertEquals(1, summary.getListingInfoByCondition().size());
        final ModelListingInfo info = summary.getListingInfoByCondition().getFirst();
        Assert.assertEquals(Condition.EXCELLENT, info.getCondition());
        Assert.assertEquals(1L, (long) info.getListingCount());
        Assert.assertEquals(0, new BigDecimal("2000.00").compareTo(info.getMinPrice().getAmount()));
    }

    @Test
    public void testGetListingInfoSummaryForModelEmptyWhenModelHasNoActiveListings() {
        jdbcTemplate.update(
                "INSERT INTO models (model_id, model_name, brand_id, subcategory_id) VALUES (?, ?, ?, ?)",
                EMPTY_MODEL_ID, "No Listings", APPLE_BRAND_ID, LAPTOPS_SUBCATEGORY_ID);

        Assert.assertTrue(modelDao.getListingInfoSummaryForModel(EMPTY_MODEL_ID, null).isEmpty());
    }

    @Test
    public void testGetListingInfoSummaryForModelEmptyWhenModelDoesNotExist() {
        Assert.assertTrue(modelDao.getListingInfoSummaryForModel(NON_EXISTING_MODEL_ID, null).isEmpty());
    }

    /* ---------------------------------------------------------------------------------------------- */
    /* getListingInfoSummary                                                                           */
    /* ---------------------------------------------------------------------------------------------- */

    @Test
    public void testGetListingInfoSummaryAppliesAllFiltersToCountAndContent() {
        final Page<ModelListingInfo> page = modelDao.getListingInfoSummary(
                APPLE_BRAND_ID, MACBOOK_MODEL_ID, 2024, 2024, LAPTOPS_SUBCATEGORY_ID, 1, 10);

        Assert.assertEquals(1, page.getTotalCount());
        Assert.assertEquals(1, page.getContent().size());
        final ModelListingInfo info = page.getContent().getFirst();
        Assert.assertEquals(MACBOOK_MODEL_ID, (long) info.getModelId());
        Assert.assertEquals(Condition.EXCELLENT, info.getCondition());
        Assert.assertEquals(0, new BigDecimal("2000.00").compareTo(info.getMinPrice().getAmount()));
    }

    @Test
    public void testGetListingInfoSummaryPaginatesModelsAndOutOfRangeKeepsTotal() {
        jdbcTemplate.update(
                "INSERT INTO models (model_id, model_name, brand_id, subcategory_id) VALUES (?, ?, ?, ?)",
                101L, "A Model", APPLE_BRAND_ID, LAPTOPS_SUBCATEGORY_ID);
        jdbcTemplate.update(
                "INSERT INTO listings (listing_id, title, description, creator_id, model_id, year, price, status, condition, accepts_trade)"
                + " VALUES (?, ?, NULL, ?, ?, ?, ?, 'ACTIVE', 'GOOD', FALSE)",
                901L, "A listing", 1L, 101L, 2023, new BigDecimal("100.00"));

        final Page<ModelListingInfo> first = modelDao.getListingInfoSummary(
                null, null, null, null, LAPTOPS_SUBCATEGORY_ID, 1, 1);
        final Page<ModelListingInfo> second = modelDao.getListingInfoSummary(
                null, null, null, null, LAPTOPS_SUBCATEGORY_ID, 2, 1);
        final Page<ModelListingInfo> outOfRange = modelDao.getListingInfoSummary(
                null, null, null, null, LAPTOPS_SUBCATEGORY_ID, 10, 1);

        Assert.assertEquals(2, first.getTotalCount());
        Assert.assertEquals(101L, (long) first.getContent().getFirst().getModelId());
        Assert.assertEquals(1L, (long) second.getContent().getFirst().getModelId());
        Assert.assertTrue(outOfRange.isEmpty());
        Assert.assertEquals(2, outOfRange.getTotalCount());
    }
}
