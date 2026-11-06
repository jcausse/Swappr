package ar.edu.itba.paw.persistence;

import ar.edu.itba.paw.model.Condition;
import ar.edu.itba.paw.model.Model;
import ar.edu.itba.paw.model.ModelListingInfo;
import ar.edu.itba.paw.model.Page;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { TestConfig.class })
@Sql({ "classpath:sql/model-data.sql", "classpath:sql/model-listing-summary-data.sql" })
@Transactional
@Rollback
public class ModelJpaDaoTest {

    private static final long APPLE_BRAND_ID = 1L;
    private static final long DELL_BRAND_ID = 2L;
    private static final long LENOVO_BRAND_ID = 3L;
    private static final long SAMSUNG_BRAND_ID = 4L;

    private static final long SUBCATEGORY_ID = 1L;
    private static final long PHONES_SUBCATEGORY_ID = 2L;
    private static final long NON_EXISTING_ID = 9000L;

    private static final long MACBOOK_PRO_MODEL_ID = 1L;
    private static final long MACBOOK_AIR_MODEL_ID = 2L;

    @Autowired
    private ModelDao modelDao;

    /* ---------------------------------------------------------------------------------------------- */
    /* getById                                                                                         */
    /* ---------------------------------------------------------------------------------------------- */

    @Test
    public void testGetByIdExists() {
        final Optional<Model> maybeModel = modelDao.getById(MACBOOK_PRO_MODEL_ID);

        Assert.assertTrue(maybeModel.isPresent());
        final Model model = maybeModel.get();
        Assert.assertEquals(MACBOOK_PRO_MODEL_ID, (long) model.getId());
        Assert.assertEquals("MacBook Pro", model.getName());
        Assert.assertEquals(APPLE_BRAND_ID, (long) model.getBrand().getId());
        Assert.assertEquals("Apple", model.getBrand().getName());
        Assert.assertEquals(SUBCATEGORY_ID, (long) model.getSubcategory().getId());
        Assert.assertEquals("Laptops", model.getSubcategory().getName());
    }

    @Test
    public void testGetByIdDoesNotExist() {
        Assert.assertFalse(modelDao.getById(NON_EXISTING_ID).isPresent());
    }

    @Test
    public void testGetByIdInvalidId() {
        Assert.assertFalse(modelDao.getById(-1L).isPresent());
    }

    /* ---------------------------------------------------------------------------------------------- */
    /* getBySubcategoryIdAndBrandId                                                                    */
    /* ---------------------------------------------------------------------------------------------- */

    @Test
    public void testGetBySubcategoryIdAndBrandIdIsSorted() {
        final List<Model> models = modelDao.getBySubcategoryIdAndBrandId(SUBCATEGORY_ID, APPLE_BRAND_ID);

        Assert.assertEquals(List.of("MacBook Air", "MacBook Pro"),
                models.stream().map(Model::getName).toList());
    }

    @Test
    public void testGetBySubcategoryIdAndBrandIdOtherSubcategory() {
        final List<Model> models = modelDao.getBySubcategoryIdAndBrandId(PHONES_SUBCATEGORY_ID, SAMSUNG_BRAND_ID);

        Assert.assertEquals(List.of("Galaxy S23"), models.stream().map(Model::getName).toList());
    }

    @Test
    public void testGetBySubcategoryIdAndBrandIdEmptyForUnknownBrand() {
        Assert.assertTrue(modelDao.getBySubcategoryIdAndBrandId(SUBCATEGORY_ID, NON_EXISTING_ID).isEmpty());
    }

    @Test
    public void testGetBySubcategoryIdAndBrandIdEmptyForWrongSubcategory() {
        Assert.assertTrue(modelDao.getBySubcategoryIdAndBrandId(PHONES_SUBCATEGORY_ID, APPLE_BRAND_ID).isEmpty());
    }

    /* ---------------------------------------------------------------------------------------------- */
    /* getListingInfoSummary                                                                           */
    /* ---------------------------------------------------------------------------------------------- */

    @Test
    public void testGetListingInfoSummaryPaginatesModelsAndGroupsByCondition() {
        final Page<ModelListingInfo> firstPage = modelDao.getListingInfoSummary(
            null, null, null, null, SUBCATEGORY_ID, 1, 2
        );

        Assert.assertEquals(1, firstPage.getPage());
        Assert.assertEquals(2, firstPage.getPageSize());
        Assert.assertEquals(3, firstPage.getTotalCount());
        Assert.assertEquals(2, firstPage.getTotalPages());

        final List<ModelListingInfo> content = firstPage.getContent();
        // Size is > totalCount, because the DAO returns one item per condition which is grouped in the service
        Assert.assertEquals(4, content.size());

        final ModelListingInfo air = content.get(0);
        Assert.assertEquals(APPLE_BRAND_ID, (long) air.getBrand().getId());
        Assert.assertEquals("Apple", air.getBrand().getName());
        Assert.assertEquals(MACBOOK_AIR_MODEL_ID, (long) air.getModel().getId());
        Assert.assertEquals("MacBook Air", air.getModel().getName());
        Assert.assertEquals(Condition.FAIR, air.getCondition());
        Assert.assertEquals(1L, (long) air.getListingCount());
        Assert.assertEquals(0, new BigDecimal("800.00").compareTo(air.getMinPrice().getAmount()));

        final ModelListingInfo proGood = findInfo(content, "MacBook Pro", Condition.GOOD);
        Assert.assertNotNull(proGood);
        Assert.assertEquals(MACBOOK_PRO_MODEL_ID, (long) proGood.getModel().getId());
        Assert.assertEquals(1L, (long) proGood.getListingCount());
        Assert.assertEquals(0, new BigDecimal("1000.00").compareTo(proGood.getMinPrice().getAmount()));
        Assert.assertEquals(0, new BigDecimal("1000.00").compareTo(proGood.getMaxPrice().getAmount()));

        final ModelListingInfo proLikeNew = findInfo(content, "MacBook Pro", Condition.LIKE_NEW);
        Assert.assertNotNull(proLikeNew);
        Assert.assertEquals(1L, (long) proLikeNew.getListingCount());
        Assert.assertEquals(0, new BigDecimal("1200.00").compareTo(proLikeNew.getMinPrice().getAmount()));
    }

    @Test
    public void testGetListingInfoSummarySecondPageFetchesRemainingModelIds() {
        final Page<ModelListingInfo> secondPage = modelDao.getListingInfoSummary(
            null, null, null, null, SUBCATEGORY_ID, 2, 2
        );

        Assert.assertEquals(2, secondPage.getPage());
        Assert.assertEquals(3, secondPage.getTotalCount());
        Assert.assertEquals(1, secondPage.getContent().size());
        Assert.assertEquals("Dell", secondPage.getContent().get(0).getBrand().getName());
        Assert.assertEquals("XPS 15", secondPage.getContent().get(0).getModel().getName());
    }

    @Test
    public void testGetListingInfoSummaryAppliesFiltersToModelIdQuery() {
        final Page<ModelListingInfo> apple = modelDao.getListingInfoSummary(
            APPLE_BRAND_ID, null, null, null, SUBCATEGORY_ID, 1, 10
        );
        Assert.assertEquals(2, apple.getTotalCount());

        final Page<ModelListingInfo> macBookPro = modelDao.getListingInfoSummary(
            null, MACBOOK_PRO_MODEL_ID, null, null, SUBCATEGORY_ID, 1, 10
        );
        Assert.assertEquals(1, macBookPro.getTotalCount());

        final Page<ModelListingInfo> inYearRange = modelDao.getListingInfoSummary(
            null, null, 2021, 2023, SUBCATEGORY_ID, 1, 10
        );
        Assert.assertEquals(3, inYearRange.getTotalCount());
    }

    @Test
    public void testGetListingInfoSummaryEmptyWhenNoActiveListingsMatch() {
        final Page<ModelListingInfo> page = modelDao.getListingInfoSummary(
            LENOVO_BRAND_ID, null, null, null, SUBCATEGORY_ID, 1, 10
        );

        Assert.assertEquals(0, page.getTotalCount());
        Assert.assertTrue(page.getContent().isEmpty());
    }

    @Test
    public void testGetListingInfoSummaryExcludesOtherSubcategories() {
        final Page<ModelListingInfo> page = modelDao.getListingInfoSummary(
            SAMSUNG_BRAND_ID, null, null, null, SUBCATEGORY_ID, 1, 10
        );

        Assert.assertEquals(0, page.getTotalCount());
        Assert.assertTrue(page.getContent().isEmpty());
    }

    @Test
    public void testGetListingInfoSummaryPageBeyondLastKeepsTotalCount() {
        final Page<ModelListingInfo> page = modelDao.getListingInfoSummary(
            null, null, null, null, SUBCATEGORY_ID, 3, 2
        );

        Assert.assertTrue(page.getContent().isEmpty());
        Assert.assertEquals(3, page.getTotalCount());
    }

    private static ModelListingInfo findInfo(final List<ModelListingInfo> content,
            final String model, final Condition condition) {
        return content.stream()
            .filter(info -> model.equals(info.getModel().getName()) && info.getCondition() == condition)
            .findFirst()
            .orElse(null);
    }
}
