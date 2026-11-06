package ar.edu.itba.paw.persistence;

import ar.edu.itba.paw.model.Brand;
import ar.edu.itba.paw.model.BrandInfo;
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
import javax.sql.DataSource;
import java.util.List;
import java.util.Optional;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { TestConfig.class })
@Sql({ "classpath:sql/model-data.sql", "classpath:sql/brand-listing-data.sql" })
@Transactional
@Rollback
public class BrandJpaDaoTest {

    private static final long APPLE_BRAND_ID = 1L;
    private static final long DELL_BRAND_ID = 2L;
    private static final long SAMSUNG_BRAND_ID = 4L;
    private static final long SUBCATEGORY_ID = 1L;           // Laptops
    private static final long PHONES_SUBCATEGORY_ID = 2L;    // Phones
    private static final long NON_EXISTING_ID = 9000L;

    @Autowired
    private BrandDao brandDao;

    @Autowired
    private DataSource dataSource;

    @PersistenceContext
    private EntityManager em;

    private JdbcTemplate jdbcTemplate;

    @Before
    public void setUp() {
        jdbcTemplate = new JdbcTemplate(dataSource);
    }

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
    public void testGetByNameExists() {
        final Optional<Brand> maybeBrand = brandDao.getByName("Dell");

        Assert.assertTrue(maybeBrand.isPresent());
        Assert.assertEquals(DELL_BRAND_ID, (long) maybeBrand.get().getId());
    }

    @Test
    public void testGetByNameDoesNotExist() {
        Assert.assertFalse(brandDao.getByName("Toshiba").isPresent());
    }

    @Test
    public void testGetAllWithStatsIsSortedAndIncludesCounts() {
        final List<BrandInfo> brands = brandDao.getAllWithStats();

        Assert.assertEquals(List.of("Apple", "Dell", "Lenovo", "Samsung"),
                brands.stream().map(BrandInfo::getName).toList());
        Assert.assertEquals(2L, brands.get(0).getModelCount());
        Assert.assertFalse(brands.get(0).isHasListings());
        Assert.assertEquals(2L, brands.get(1).getModelCount());
        Assert.assertTrue(brands.get(1).isHasListings());
        Assert.assertEquals(1L, brands.get(2).getModelCount());
        Assert.assertFalse(brands.get(2).isHasListings());
        Assert.assertEquals(1L, brands.get(3).getModelCount());
        Assert.assertFalse(brands.get(3).isHasListings());
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

    @Test
    public void testRenameUpdatesBrand() {
        final Optional<Brand> renamed = brandDao.rename(APPLE_BRAND_ID, "Apple Inc.");

        Assert.assertTrue(renamed.isPresent());
        Assert.assertEquals("Apple Inc.", renamed.get().getName());
        em.flush();
        Assert.assertEquals(1, JdbcTestUtils.countRowsInTableWhere(
                jdbcTemplate, "brands", "brand_id = " + APPLE_BRAND_ID + " AND brand_name = 'Apple Inc.'"));
    }

    @Test
    public void testRenameDoesNotExist() {
        Assert.assertFalse(brandDao.rename(NON_EXISTING_ID, "New name").isPresent());
    }

    @Test
    public void testDeleteBrandRemovesItAndItsModels() {
        Assert.assertTrue(brandDao.delete(SAMSUNG_BRAND_ID));
        em.flush();

        Assert.assertEquals(0, JdbcTestUtils.countRowsInTableWhere(
                jdbcTemplate, "brands", "brand_id = " + SAMSUNG_BRAND_ID));
        Assert.assertEquals(0, JdbcTestUtils.countRowsInTableWhere(
                jdbcTemplate, "models", "brand_id = " + SAMSUNG_BRAND_ID));
    }

    @Test
    public void testDeleteDoesNotExist() {
        Assert.assertFalse(brandDao.delete(NON_EXISTING_ID));
    }

    @Test
    public void testHasListingsForBrandWithListings() {
        Assert.assertTrue(brandDao.hasListings(DELL_BRAND_ID));
    }

    @Test
    public void testHasListingsForBrandWithoutListings() {
        Assert.assertFalse(brandDao.hasListings(APPLE_BRAND_ID));
    }

    @Test
    public void testHasListingsForMissingBrand() {
        Assert.assertFalse(brandDao.hasListings(NON_EXISTING_ID));
    }
}
