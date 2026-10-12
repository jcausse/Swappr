package ar.edu.itba.paw.persistence;

import ar.edu.itba.paw.model.Category;
import ar.edu.itba.paw.model.Product;
import ar.edu.itba.paw.model.Subcategory;
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
import org.springframework.test.jdbc.JdbcTestUtils;

import javax.sql.DataSource;
import java.util.List;
import java.util.Optional;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { TestConfig.class })
@Sql({ "classpath:sql/product-data.sql" })
@Transactional
@Rollback
public class ProductJpaDaoTest {

    private static final long CATEGORY_ID = 1L;             // Electronics
    private static final String CATEGORY_NAME = "Electronics";
    private static final long PHONES_CATEGORY_ID = 2L;      // Mobile Devices

    private static final long SUBCATEGORY_ID = 1L;          // Laptops
    private static final String SUBCATEGORY_NAME = "Laptops";
    private static final long PHONES_SUBCATEGORY_ID = 2L;   // Phones
    private static final long NON_EXISTING_SUBCATEGORY_ID = 9000L;

    private static final long PRODUCT_ID = 1L;              // Apple MacBook Pro 2023
    private static final long NON_EXISTING_ID = 9000L;
    private static final String PRODUCT_BRAND = "Apple";
    private static final String PRODUCT_MODEL = "MacBook Pro";
    private static final int PRODUCT_YEAR = 2023;

    // Fixture totals (initial-data product 1 + sql/product-data products 2-8)
    private static final int LAPTOPS_PRODUCT_COUNT = 7;     // subcat 1
    private static final int ELECTRONICS_PRODUCT_COUNT = 7; // category 1 == all laptops

    @Autowired
    private ProductDao productDao;

    @Autowired
    private DataSource dataSource;

    private JdbcTemplate jdbcTemplate;

    @Before
    public void setUp() {
        jdbcTemplate = new JdbcTemplate(dataSource);
    }

    private Category buildFakeCategory() {
        return Category.builder()
            .id(CATEGORY_ID)
            .name(CATEGORY_NAME)
            .build();
    }

    private Subcategory buildFakeSubcategory() {
        return Subcategory.builder()
            .id(SUBCATEGORY_ID)
            .name(SUBCATEGORY_NAME)
            .category(buildFakeCategory())
            .build();
    }

    /* ---------------------------------------------------------------------------------------------- */
    /* getById                                                                                         */
    /* ---------------------------------------------------------------------------------------------- */

    @Test
    public void testGetByIdExists() {
        final Optional<Product> maybeProduct = productDao.getById(PRODUCT_ID);

        Assert.assertTrue(maybeProduct.isPresent());
        Assert.assertEquals(PRODUCT_ID, (long) maybeProduct.get().getId());
        Assert.assertEquals(PRODUCT_BRAND, maybeProduct.get().getBrand());
        Assert.assertEquals(PRODUCT_MODEL, maybeProduct.get().getModel());
        Assert.assertEquals(PRODUCT_YEAR, (int) maybeProduct.get().getYear());
        Assert.assertEquals(SUBCATEGORY_ID, (long) maybeProduct.get().getSubcategory().getId());
        Assert.assertEquals(CATEGORY_ID, (long) maybeProduct.get().getSubcategory().getCategory().getId());
    }

    @Test
    public void testGetByIdDoesNotExist() {
        final Optional<Product> maybeProduct = productDao.getById(NON_EXISTING_ID);

        Assert.assertFalse(maybeProduct.isPresent());
    }

    @Test
    public void testGetByIdInvalidId() {
        final Optional<Product> maybeProduct = productDao.getById(-1L);

        Assert.assertFalse(maybeProduct.isPresent());
    }

    /* ---------------------------------------------------------------------------------------------- */
    /* getByCategory                                                                                   */
    /* ---------------------------------------------------------------------------------------------- */

    @Test
    public void testGetByCategoryReturnsAllProductsInCategory() {
        final List<Product> products = productDao.getByCategory(CATEGORY_ID);

        Assert.assertEquals(ELECTRONICS_PRODUCT_COUNT, products.size());
    }

    @Test
    public void testGetByCategoryDoesNotExist() {
        final List<Product> products = productDao.getByCategory(NON_EXISTING_ID);

        Assert.assertTrue(products.isEmpty());
    }

    @Test
    public void testGetByCategoryExcludesOtherCategories() {
        // Category 2 (Mobile Devices) only has the Samsung phone (product 8)
        final List<Product> products = productDao.getByCategory(PHONES_CATEGORY_ID);

        Assert.assertEquals(1, products.size());
        Assert.assertEquals("Samsung", products.getFirst().getBrand());
    }

    /* ---------------------------------------------------------------------------------------------- */
    /* getBySubcategory                                                                                */
    /* ---------------------------------------------------------------------------------------------- */

    @Test
    public void testGetBySubcategoryReturnsAllProductsInSubcategory() {
        final List<Product> products = productDao.getBySubcategory(SUBCATEGORY_ID);

        Assert.assertEquals(LAPTOPS_PRODUCT_COUNT, products.size());
    }

    @Test
    public void testGetBySubcategoryDoesNotExist() {
        final List<Product> products = productDao.getBySubcategory(NON_EXISTING_SUBCATEGORY_ID);

        Assert.assertTrue(products.isEmpty());
    }

    @Test
    public void testGetBySubcategoryExcludesOtherSubcategories() {
        final List<Product> products = productDao.getBySubcategory(PHONES_SUBCATEGORY_ID);

        Assert.assertEquals(1, products.size());
        Assert.assertEquals("Samsung", products.getFirst().getBrand());
    }

    /* ---------------------------------------------------------------------------------------------- */
    /* getBySubcategoryBrandModel                                                                      */
    /* ---------------------------------------------------------------------------------------------- */

    @Test
    public void testGetBySubcategoryBrandModelMatchesBrandAndModel() {
        // Apple MacBook Pro exists in 3 years (2021/2023/2024) within subcat 1
        final List<Product> products = productDao.getBySubcategoryBrandModel(SUBCATEGORY_ID, PRODUCT_BRAND, PRODUCT_MODEL);

        Assert.assertEquals(3, products.size());
        Assert.assertTrue(products.stream().allMatch(p -> p.getBrand().equals("Apple") && p.getModel().equals("MacBook Pro")));
    }

    @Test
    public void testGetBySubcategoryBrandModelEmptyFiltersMatchesAll() {
        final List<Product> products = productDao.getBySubcategoryBrandModel(SUBCATEGORY_ID, "", "");

        Assert.assertEquals(LAPTOPS_PRODUCT_COUNT, products.size());
    }

    @Test
    public void testGetBySubcategoryBrandModelBrandOnlyFilter() {
        // Only brand given (model empty) -> all Apple products in subcat 1 (3x Pro + 1 Air)
        final List<Product> products = productDao.getBySubcategoryBrandModel(SUBCATEGORY_ID, "Apple", "");

        Assert.assertEquals(4, products.size());
        Assert.assertTrue(products.stream().allMatch(p -> p.getBrand().equals("Apple")));
    }

    @Test
    public void testGetBySubcategoryBrandModelNoMatch() {
        final List<Product> products = productDao.getBySubcategoryBrandModel(SUBCATEGORY_ID, "Samsung", PRODUCT_MODEL);

        Assert.assertTrue(products.isEmpty());
    }

    /* ---------------------------------------------------------------------------------------------- */
    /* getByBrandModelYearSubcategory                                                                  */
    /* ---------------------------------------------------------------------------------------------- */

    @Test
    public void testGetByBrandModelYearSubcategoryExists() {
        final Optional<Product> maybeProduct = productDao.getByBrandModelYearSubcategory(
            PRODUCT_BRAND, PRODUCT_MODEL, PRODUCT_YEAR, SUBCATEGORY_ID
        );

        Assert.assertTrue(maybeProduct.isPresent());
        Assert.assertEquals(PRODUCT_ID, (long) maybeProduct.get().getId());
    }

    @Test
    public void testGetByBrandModelYearSubcategoryDoesNotExist() {
        final Optional<Product> maybeProduct = productDao.getByBrandModelYearSubcategory(
            "Samsung", "Galaxy Book", 2023, SUBCATEGORY_ID
        );

        Assert.assertFalse(maybeProduct.isPresent());
    }

    @Test
    public void testGetByBrandModelYearSubcategoryWrongYearDoesNotMatch() {
        // Apple MacBook Pro exists, but not for 2099
        final Optional<Product> maybeProduct = productDao.getByBrandModelYearSubcategory(
            PRODUCT_BRAND, PRODUCT_MODEL, 2099, SUBCATEGORY_ID
        );

        Assert.assertFalse(maybeProduct.isPresent());
    }

    /* ---------------------------------------------------------------------------------------------- */
    /* getBrandsBySubcategory (DISTINCT + ORDER BY brand)                                              */
    /* ---------------------------------------------------------------------------------------------- */

    @Test
    public void testGetBrandsBySubcategoryIsDistinctAndSorted() {
        // subcat 1 has Apple (x4), Dell (x2), Lenovo (x1) -> distinct & alphabetical
        final List<String> brands = productDao.getBrandsBySubcategory(SUBCATEGORY_ID);

        Assert.assertEquals(List.of("Apple", "Dell", "Lenovo"), brands);
    }

    @Test
    public void testGetBrandsBySubcategoryEmptyWhenNoProducts() {
        final List<String> brands = productDao.getBrandsBySubcategory(NON_EXISTING_SUBCATEGORY_ID);

        Assert.assertTrue(brands.isEmpty());
    }

    /* ---------------------------------------------------------------------------------------------- */
    /* getModelsBySubcategoryAndBrand (DISTINCT + ORDER BY model)                                      */
    /* ---------------------------------------------------------------------------------------------- */

    @Test
    public void testGetModelsBySubcategoryAndBrandIsDistinctAndSorted() {
        // Apple in subcat 1: "MacBook Pro" (x3) and "MacBook Air" (x1) -> distinct & alphabetical
        final List<String> models = productDao.getModelsBySubcategoryAndBrand(SUBCATEGORY_ID, "Apple");

        Assert.assertEquals(List.of("MacBook Air", "MacBook Pro"), models);
    }

    @Test
    public void testGetModelsBySubcategoryAndBrandEmptyForUnknownBrand() {
        final List<String> models = productDao.getModelsBySubcategoryAndBrand(SUBCATEGORY_ID, "Toshiba");

        Assert.assertTrue(models.isEmpty());
    }

    /* ---------------------------------------------------------------------------------------------- */
    /* getYearsBySubcategoryAndBrandAndModel (DISTINCT + ORDER BY year DESC)                           */
    /* ---------------------------------------------------------------------------------------------- */

    @Test
    public void testGetYearsBySubcategoryAndBrandAndModelIsDistinctAndSortedDescending() {
        // Apple MacBook Pro years: 2021, 2023, 2024 -> distinct & descending
        final List<Integer> years = productDao.getYearsBySubcategoryAndBrandAndModel(
            SUBCATEGORY_ID, PRODUCT_BRAND, PRODUCT_MODEL
        );

        Assert.assertEquals(List.of(2024, 2023, 2021), years);
    }

    @Test
    public void testGetYearsBySubcategoryAndBrandAndModelEmptyForUnknownModel() {
        final List<Integer> years = productDao.getYearsBySubcategoryAndBrandAndModel(
            SUBCATEGORY_ID, PRODUCT_BRAND, "Nonexistent"
        );

        Assert.assertTrue(years.isEmpty());
    }

    /* ---------------------------------------------------------------------------------------------- */
    /* create                                                                                          */
    /* ---------------------------------------------------------------------------------------------- */

    @Test
    public void testCreateReturnsCreatedProduct() {
        final Subcategory subcategory = buildFakeSubcategory();

        final Product created = productDao.create("HP", "Spectre", 2024, subcategory);

        Assert.assertNotNull(created.getId());
        Assert.assertEquals("HP", created.getBrand());
        Assert.assertEquals("Spectre", created.getModel());
        Assert.assertEquals(2024, (int) created.getYear());
        Assert.assertEquals(SUBCATEGORY_ID, (long) created.getSubcategory().getId());
    }

    @Test
    public void testCreatePersistsProduct() {
        final Subcategory subcategory = buildFakeSubcategory();

        final Product created = productDao.create("HP", "Spectre", 2024, subcategory);

        final int count = JdbcTestUtils.countRowsInTableWhere(
            jdbcTemplate,
            "products",
            "product_id = " + created.getId()
                + " AND brand = 'HP' AND model = 'Spectre' AND year = 2024"
                + " AND subcategory_id = " + SUBCATEGORY_ID
        );
        Assert.assertEquals(1, count);
    }
}
