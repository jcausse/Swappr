package ar.edu.itba.paw.persistence;

import ar.edu.itba.paw.model.Model;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { TestConfig.class })
@Sql("classpath:sql/model-data.sql")
@Transactional
@Rollback
public class ModelJpaDaoTest {

    private static final long APPLE_BRAND_ID = 1L;
    private static final long SAMSUNG_BRAND_ID = 4L;

    private static final long SUBCATEGORY_ID = 1L;
    private static final long PHONES_SUBCATEGORY_ID = 2L;
    private static final long NON_EXISTING_ID = 9000L;

    private static final long MACBOOK_PRO_MODEL_ID = 1L;

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
}
