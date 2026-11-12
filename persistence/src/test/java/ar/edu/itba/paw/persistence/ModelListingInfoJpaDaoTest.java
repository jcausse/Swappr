package ar.edu.itba.paw.persistence;

import ar.edu.itba.paw.model.Condition;
import ar.edu.itba.paw.model.ModelListingInfo;
import ar.edu.itba.paw.model.ModelListingInfoSummary;
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
                modelDao.getListingInfoSummaryForModel(MACBOOK_MODEL_ID);

        Assert.assertTrue(maybeSummary.isPresent());
        final ModelListingInfoSummary summary = maybeSummary.get();
        Assert.assertEquals("Apple", summary.getBrand().getName());
        Assert.assertEquals("MacBook Pro", summary.getModel().getName());
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
    public void testGetListingInfoSummaryForModelEmptyWhenModelHasNoActiveListings() {
        jdbcTemplate.update(
                "INSERT INTO models (model_id, model_name, brand_id, subcategory_id) VALUES (?, ?, ?, ?)",
                EMPTY_MODEL_ID, "No Listings", APPLE_BRAND_ID, LAPTOPS_SUBCATEGORY_ID);

        Assert.assertTrue(modelDao.getListingInfoSummaryForModel(EMPTY_MODEL_ID).isEmpty());
    }

    @Test
    public void testGetListingInfoSummaryForModelEmptyWhenModelDoesNotExist() {
        Assert.assertTrue(modelDao.getListingInfoSummaryForModel(NON_EXISTING_MODEL_ID).isEmpty());
    }
}
