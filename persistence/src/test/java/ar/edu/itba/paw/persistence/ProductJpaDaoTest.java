package ar.edu.itba.paw.persistence;

import ar.edu.itba.paw.model.Category;
import ar.edu.itba.paw.model.Condition;
import ar.edu.itba.paw.model.Page;
import ar.edu.itba.paw.model.Product;
import ar.edu.itba.paw.model.ProductListingInfo;
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
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { TestConfig.class })
@Sql({ "classpath:sql/product-data.sql", "classpath:sql/product-listing-summary-data.sql" })
@Transactional
@Rollback
public class ProductJpaDaoTest {

    private static final long CATEGORY_ID = 1L;             // Electronics
    private static final String CATEGORY_NAME = "Electronics";

    private static final long SUBCATEGORY_ID = 1L;          // Laptops
    private static final String SUBCATEGORY_NAME = "Laptops";
    private static final long NON_EXISTING_SUBCATEGORY_ID = 9000L;

    private static final long PRODUCT_ID = 1L;              // Apple MacBook Pro 2023
    private static final long NON_EXISTING_ID = 9000L;
    private static final String PRODUCT_BRAND = "Apple";
    private static final String PRODUCT_MODEL = "MacBook Pro";
    private static final int PRODUCT_YEAR = 2023;

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
    /* getListingInfoSummary                                                                           */
    /* ---------------------------------------------------------------------------------------------- */

    @Test
    public void testGetListingInfoSummaryPaginatesProductsAndGroupsByCondition() {
        final Page<ProductListingInfo> firstPage = productDao.getListingInfoSummary(
            null, null, null, null, SUBCATEGORY_ID, 1, 2
        );

        Assert.assertEquals(1, firstPage.getPage());
        Assert.assertEquals(2, firstPage.getPageSize());
        Assert.assertEquals(4, firstPage.getTotalCount());
        Assert.assertEquals(2, firstPage.getTotalPages());

        final List<ProductListingInfo> content = firstPage.getContent();
        Assert.assertEquals(3, content.size());

        final ProductListingInfo air = content.get(0);
        Assert.assertEquals("Apple", air.getBrand());
        Assert.assertEquals("MacBook Air", air.getModel());
        Assert.assertEquals(Condition.FAIR, air.getCondition());
        Assert.assertEquals(1L, (long) air.getListingCount());
        Assert.assertEquals(0, new BigDecimal("800.00").compareTo(air.getMinPrice().getAmount()));

        final ProductListingInfo proGood = findInfo(content, "MacBook Pro", Condition.GOOD);
        Assert.assertNotNull(proGood);
        Assert.assertEquals(1L, (long) proGood.getListingCount());
        Assert.assertEquals(0, new BigDecimal("1000.00").compareTo(proGood.getMinPrice().getAmount()));
        Assert.assertEquals(0, new BigDecimal("1000.00").compareTo(proGood.getMaxPrice().getAmount()));

        final ProductListingInfo proLikeNew = findInfo(content, "MacBook Pro", Condition.LIKE_NEW);
        Assert.assertNotNull(proLikeNew);
        Assert.assertEquals(1L, (long) proLikeNew.getListingCount());
        Assert.assertEquals(0, new BigDecimal("1200.00").compareTo(proLikeNew.getMinPrice().getAmount()));
    }

    @Test
    public void testGetListingInfoSummarySecondPageFetchesRemainingProductIds() {
        final Page<ProductListingInfo> secondPage = productDao.getListingInfoSummary(
            null, null, null, null, SUBCATEGORY_ID, 2, 2
        );

        Assert.assertEquals(2, secondPage.getPage());
        Assert.assertEquals(4, secondPage.getTotalCount());
        Assert.assertEquals(2, secondPage.getContent().size());
        Assert.assertEquals("Apple", secondPage.getContent().get(0).getBrand());
        Assert.assertEquals("MacBook Pro", secondPage.getContent().get(0).getModel());
        Assert.assertEquals("Dell", secondPage.getContent().get(1).getBrand());
        Assert.assertEquals("XPS 15", secondPage.getContent().get(1).getModel());
    }

    @Test
    public void testGetListingInfoSummaryAppliesFiltersToProductIdQuery() {
        final Page<ProductListingInfo> apple = productDao.getListingInfoSummary(
            "Apple", null, null, null, SUBCATEGORY_ID, 1, 10
        );
        Assert.assertEquals(3, apple.getTotalCount());

        final Page<ProductListingInfo> macBookPro = productDao.getListingInfoSummary(
            "Apple", "MacBook Pro", null, null, SUBCATEGORY_ID, 1, 10
        );
        Assert.assertEquals(2, macBookPro.getTotalCount());

        final Page<ProductListingInfo> inYearRange = productDao.getListingInfoSummary(
            null, null, 2021, 2023, SUBCATEGORY_ID, 1, 10
        );
        Assert.assertEquals(3, inYearRange.getTotalCount());
    }

    @Test
    public void testGetListingInfoSummaryEmptyWhenNoActiveListingsMatch() {
        final Page<ProductListingInfo> page = productDao.getListingInfoSummary(
            "Lenovo", null, null, null, SUBCATEGORY_ID, 1, 10
        );

        Assert.assertEquals(0, page.getTotalCount());
        Assert.assertTrue(page.getContent().isEmpty());
    }

    @Test
    public void testGetListingInfoSummaryPageBeyondLastKeepsTotalCount() {
        final Page<ProductListingInfo> page = productDao.getListingInfoSummary(
            null, null, null, null, SUBCATEGORY_ID, 3, 2
        );

        Assert.assertTrue(page.getContent().isEmpty());
        Assert.assertEquals(4, page.getTotalCount());
    }

    private static ProductListingInfo findInfo(final List<ProductListingInfo> content,
            final String model, final Condition condition) {
        return content.stream()
            .filter(info -> model.equals(info.getModel()) && info.getCondition() == condition)
            .findFirst()
            .orElse(null);
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
