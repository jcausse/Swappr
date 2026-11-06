package ar.edu.itba.paw.persistence;

import ar.edu.itba.paw.model.Brand;
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
public class BrandJpaDaoTest {

    private static final long APPLE_BRAND_ID = 1L;
    private static final long SUBCATEGORY_ID = 1L;           // Laptops
    private static final long PHONES_SUBCATEGORY_ID = 2L;    // Phones
    private static final long NON_EXISTING_ID = 9000L;

    @Autowired
    private BrandDao brandDao;

    @Test
    public void testGetByIdExists() {
        final Optional<Brand> maybeBrand = brandDao.getById(APPLE_BRAND_ID);

        Assert.assertTrue(maybeBrand.isPresent());
        Assert.assertEquals(APPLE_BRAND_ID, (long) maybeBrand.get().getId());
        Assert.assertEquals("Apple", maybeBrand.get().getName());
    }

    @Test
    public void testGetByIdDoesNotExist() {
        Assert.assertFalse(brandDao.getById(NON_EXISTING_ID).isPresent());
    }

    @Test
    public void testGetByIdInvalidId() {
        Assert.assertFalse(brandDao.getById(-1L).isPresent());
    }

    @Test
    public void testGetBySubcategoryIdIsDistinctAndSorted() {
        final List<Brand> brands = brandDao.getBySubcategoryId(SUBCATEGORY_ID);

        Assert.assertEquals(List.of("Apple", "Dell", "Lenovo"),
                brands.stream().map(Brand::getName).toList());
    }

    @Test
    public void testGetBySubcategoryIdOnlyReturnsBrandsWithModelsInSubcategory() {
        final List<Brand> brands = brandDao.getBySubcategoryId(PHONES_SUBCATEGORY_ID);

        Assert.assertEquals(List.of("Samsung"), brands.stream().map(Brand::getName).toList());
    }

    @Test
    public void testGetBySubcategoryIdEmptyWhenNoModels() {
        Assert.assertTrue(brandDao.getBySubcategoryId(NON_EXISTING_ID).isEmpty());
    }
}
