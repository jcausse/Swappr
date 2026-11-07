package ar.edu.itba.paw.persistence;

import ar.edu.itba.paw.model.Brand;
import ar.edu.itba.paw.model.Model;
import ar.edu.itba.paw.model.ModelInfo;
import ar.edu.itba.paw.model.ModelSort;
import ar.edu.itba.paw.model.Page;
import ar.edu.itba.paw.model.Subcategory;
import org.junit.Assert;
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
import java.util.List;
import java.util.Optional;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { TestConfig.class })
@Sql({ "classpath:sql/model-data.sql", "classpath:sql/brand-listing-data.sql" })
@Transactional
@Rollback
public class ModelJpaDaoTest {

    private static final long APPLE_BRAND_ID = 1L;
    private static final long DELL_BRAND_ID = 2L;
    private static final long SAMSUNG_BRAND_ID = 4L;

    private static final long SUBCATEGORY_ID = 1L;
    private static final long PHONES_SUBCATEGORY_ID = 2L;
    private static final long NON_EXISTING_ID = 9000L;

    private static final long MACBOOK_PRO_MODEL_ID = 1L;

    @Autowired
    private ModelDao modelDao;

    @Autowired
    private DataSource dataSource;

    private JdbcTemplate jdbcTemplate;

    @javax.persistence.PersistenceContext
    private javax.persistence.EntityManager em;

    @org.junit.Before
    public void setUp() {
        jdbcTemplate = new JdbcTemplate(dataSource);
    }

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
    /* getWithInfo                                                                                     */
    /* ---------------------------------------------------------------------------------------------- */

    @Test
    public void testGetWithInfoFirstPageSortedByName() {
        final Page<ModelInfo> page = modelDao.getWithInfo(null, null, ModelSort.NAME_ASC, 1, 2);

        Assert.assertEquals(1, page.getPage());
        Assert.assertEquals(2, page.getPageSize());
        Assert.assertEquals(6, page.getTotalCount());
        Assert.assertEquals(3, page.getTotalPages());
        Assert.assertEquals(List.of("Galaxy S23", "Inspiron 14"),
                page.getContent().stream().map(ModelInfo::getName).toList());
        Assert.assertEquals(List.of("Samsung", "Dell"),
                page.getContent().stream().map(ModelInfo::getBrandName).toList());
    }

    @Test
    public void testGetWithInfoFiltersByBrand() {
        final Page<ModelInfo> page = modelDao.getWithInfo(DELL_BRAND_ID, null, ModelSort.NAME_ASC, 1, 10);

        Assert.assertEquals(2, page.getTotalCount());
        Assert.assertEquals(List.of("Inspiron 14", "XPS 15"),
                page.getContent().stream().map(ModelInfo::getName).toList());
        Assert.assertTrue(page.getContent().get(1).isHasListings());
    }

    @Test
    public void testGetWithInfoFiltersBySubcategory() {
        final Page<ModelInfo> page = modelDao.getWithInfo(null, PHONES_SUBCATEGORY_ID, ModelSort.NAME_ASC, 1, 10);

        Assert.assertEquals(1, page.getTotalCount());
        Assert.assertEquals("Galaxy S23", page.getContent().get(0).getName());
        Assert.assertEquals("Phones", page.getContent().get(0).getSubcategoryName());
    }

    @Test
    public void testGetWithInfoSortedById() {
        final Page<ModelInfo> page = modelDao.getWithInfo(null, null, ModelSort.ID_ASC, 1, 2);

        Assert.assertEquals(List.of("MacBook Pro", "MacBook Air"),
                page.getContent().stream().map(ModelInfo::getName).toList());
    }

    @Test
    public void testGetWithInfoSortedByNameDescending() {
        final Page<ModelInfo> page = modelDao.getWithInfo(null, null, ModelSort.NAME_DESC, 1, 2);

        Assert.assertEquals(List.of("XPS 15", "ThinkPad X1"),
                page.getContent().stream().map(ModelInfo::getName).toList());
    }

    @Test
    public void testGetAllWithBrandLoadsBrands() {
        final List<Model> models = modelDao.getAllWithBrand();

        Assert.assertEquals(6, models.size());
        Assert.assertEquals("MacBook Air", models.get(0).getName());
        Assert.assertEquals("Apple", models.get(0).getBrand().getName());
        Assert.assertEquals("Galaxy S23", models.get(5).getName());
        Assert.assertEquals("Samsung", models.get(5).getBrand().getName());
    }

    @Test
    public void testExistsByNameAndBrandId() {
        Assert.assertTrue(modelDao.existsByNameAndBrandId("MacBook Pro", APPLE_BRAND_ID));
        Assert.assertFalse(modelDao.existsByNameAndBrandId("MacBook Pro", DELL_BRAND_ID));
    }

    @Test
    public void testExistsByNameAndBrandIdAndIdNot() {
        Assert.assertTrue(modelDao.existsByNameAndBrandIdAndIdNot("MacBook Pro", APPLE_BRAND_ID, 2L));
        Assert.assertFalse(modelDao.existsByNameAndBrandIdAndIdNot("MacBook Pro", APPLE_BRAND_ID, 1L));
    }

    @Test
    public void testCreatePersistsModel() {
        final Model created = modelDao.create("New Model",
                Brand.builder().id(APPLE_BRAND_ID).build(),
                Subcategory.builder().id(SUBCATEGORY_ID).build());
        em.flush();

        Assert.assertNotNull(created.getId());
        Assert.assertEquals("New Model", created.getName());
        Assert.assertEquals(1, JdbcTestUtils.countRowsInTableWhere(
                jdbcTemplate, "models", "model_id = " + created.getId()
                        + " AND model_name = 'New Model' AND brand_id = " + APPLE_BRAND_ID
                        + " AND subcategory_id = " + SUBCATEGORY_ID));
    }

    @Test
    public void testUpdateChangesModelFields() {
        final Optional<Model> updated = modelDao.update(MACBOOK_PRO_MODEL_ID, "MacBook Pro M4",
                Brand.builder().id(DELL_BRAND_ID).build(),
                Subcategory.builder().id(SUBCATEGORY_ID).build());
        em.flush();

        Assert.assertTrue(updated.isPresent());
        Assert.assertEquals("MacBook Pro M4", updated.get().getName());
        Assert.assertEquals(1, JdbcTestUtils.countRowsInTableWhere(
                jdbcTemplate, "models", "model_id = " + MACBOOK_PRO_MODEL_ID
                        + " AND model_name = 'MacBook Pro M4' AND brand_id = " + DELL_BRAND_ID));
    }

    @Test
    public void testUpdateMissingModelReturnsEmpty() {
        Assert.assertFalse(modelDao.update(NON_EXISTING_ID, "Name",
                Brand.builder().id(APPLE_BRAND_ID).build(),
                Subcategory.builder().id(SUBCATEGORY_ID).build()).isPresent());
    }

    @Test
    public void testDeleteModel() {
        Assert.assertTrue(modelDao.deleteModel(6L));
        em.flush();

        Assert.assertEquals(0, JdbcTestUtils.countRowsInTableWhere(
                jdbcTemplate, "models", "model_id = 6"));
    }

    @Test
    public void testDeleteMissingModelReturnsFalse() {
        Assert.assertFalse(modelDao.deleteModel(NON_EXISTING_ID));
    }

    @Test
    public void testHasListings() {
        Assert.assertTrue(modelDao.hasListings(3L));
        Assert.assertFalse(modelDao.hasListings(MACBOOK_PRO_MODEL_ID));
    }

    /* ---------------------------------------------------------------------------------------------- */
    /* replaceBrand                                                                                    */
    /* ---------------------------------------------------------------------------------------------- */

    @Test
    @Sql("classpath:sql/brand-model-merge-data.sql")
    public void testReplaceBrandDeletesSameNameModelsAndMovesRemaining() {
        final int updated = modelDao.replaceBrand(90L, 91L);

        Assert.assertEquals(1, updated);
        Assert.assertEquals(0, JdbcTestUtils.countRowsInTableWhere(
                jdbcTemplate, "models", "brand_id = 90"));
        Assert.assertEquals(0, JdbcTestUtils.countRowsInTableWhere(
                jdbcTemplate, "models", "model_id = 90"));
        Assert.assertEquals(1, JdbcTestUtils.countRowsInTableWhere(
                jdbcTemplate, "models", "model_id = 92 AND brand_id = 91"));
    }

    @Test
    public void testReplaceBrandMovesModelsAndReturnsCount() {
        final int updated = modelDao.replaceBrand(DELL_BRAND_ID, APPLE_BRAND_ID);

        Assert.assertEquals(2, updated);
        Assert.assertEquals(4, JdbcTestUtils.countRowsInTableWhere(
                jdbcTemplate, "models", "brand_id = " + APPLE_BRAND_ID));
        Assert.assertEquals(0, JdbcTestUtils.countRowsInTableWhere(
                jdbcTemplate, "models", "brand_id = " + DELL_BRAND_ID));
    }
}
