package ar.edu.itba.paw.service;

import ar.edu.itba.paw.model.Category;
import ar.edu.itba.paw.model.Product;
import ar.edu.itba.paw.model.Subcategory;
import ar.edu.itba.paw.persistence.CategoryDao;
import ar.edu.itba.paw.persistence.ProductDao;
import ar.edu.itba.paw.persistence.SubcategoryDao;
import ar.edu.itba.paw.service.exception.NotFoundException;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class ProductServiceImplTest {

    private static final long PRODUCT_ID = 1L;
    private static final long NON_EXISTING_PRODUCT_ID = 9000L;
    private static final long SUBCATEGORY_ID = 1L;
    private static final long NON_EXISTING_SUBCATEGORY_ID = 9000L;
    private static final long CATEGORY_ID = 1L;

    private static final String BRAND = "Apple";
    private static final String MODEL = "MacBook Pro";
    private static final int YEAR = 2023;

    @InjectMocks
    private ProductServiceImpl productService;

    @Mock
    private ProductDao productDao;
    @Mock
    private CategoryDao categoryDao;
    @Mock
    private SubcategoryDao subcategoryDao;

    private Subcategory buildFakeSubcategory() {
        return Subcategory.builder()
            .id(SUBCATEGORY_ID)
            .name("Laptops")
            .category(Category.builder().id(CATEGORY_ID).name("Electronics").build())
            .build();
    }

    private Product buildFakeProduct() {
        return Product.builder()
            .id(PRODUCT_ID)
            .brand(BRAND)
            .model(MODEL)
            .year(YEAR)
            .subcategory(buildFakeSubcategory())
            .build();
    }

    /* ---------------------------------------------------------------------------------------------- */
    /* getById                                                                                         */
    /* ---------------------------------------------------------------------------------------------- */

    @Test
    public void testGetByIdExists() {
        // Arrange
        final Product product = buildFakeProduct();
        when(productDao.getById(eq(PRODUCT_ID))).thenReturn(Optional.of(product));

        // Act
        final Product result = productService.getById(PRODUCT_ID);

        // Assert
        Assert.assertEquals(PRODUCT_ID, (long) result.getId());
        Assert.assertEquals(BRAND, result.getBrand());
    }

    @Test(expected = NotFoundException.class)
    public void testGetByIdDoesNotExistThrows() {
        // Arrange
        when(productDao.getById(eq(NON_EXISTING_PRODUCT_ID))).thenReturn(Optional.empty());

        // Act
        productService.getById(NON_EXISTING_PRODUCT_ID);
    }

    /* ---------------------------------------------------------------------------------------------- */
    /* delegating reads                                                                                */
    /* ---------------------------------------------------------------------------------------------- */

    @Test
    public void testGetAllCategoriesDelegates() {
        // Arrange
        final List<Category> categories = List.of(Category.builder().id(CATEGORY_ID).name("Electronics").build());
        when(categoryDao.getAll()).thenReturn(categories);

        // Act
        final List<Category> result = productService.getAllCategories();

        // Assert
        Assert.assertEquals(categories, result);
    }

    @Test
    public void testGetSubcategoriesByCategoryDelegates() {
        // Arrange
        final List<Subcategory> subcategories = List.of(buildFakeSubcategory());
        when(subcategoryDao.getByCategoryId(eq(CATEGORY_ID))).thenReturn(subcategories);

        // Act
        final List<Subcategory> result = productService.getSubcategoriesByCategory(CATEGORY_ID);

        // Assert
        Assert.assertEquals(subcategories, result);
    }

    @Test
    public void testGetBrandsBySubcategoryDelegates() {
        // Arrange
        when(productDao.getBrandsBySubcategory(eq(SUBCATEGORY_ID))).thenReturn(List.of(BRAND));

        // Act
        final List<String> result = productService.getBrandsBySubcategory(SUBCATEGORY_ID);

        // Assert
        Assert.assertEquals(List.of(BRAND), result);
    }

    @Test
    public void testGetModelsBySubcategoryAndBrandDelegates() {
        // Arrange
        when(productDao.getModelsBySubcategoryAndBrand(eq(SUBCATEGORY_ID), eq(BRAND)))
            .thenReturn(List.of(MODEL));

        // Act
        final List<String> result = productService.getModelsBySubcategoryAndBrand(SUBCATEGORY_ID, BRAND);

        // Assert
        Assert.assertEquals(List.of(MODEL), result);
    }

    /* ---------------------------------------------------------------------------------------------- */
    /* findOrCreateByBrandModelYear                                                                     */
    /* ---------------------------------------------------------------------------------------------- */

    @Test(expected = NotFoundException.class)
    public void testFindOrCreateUnknownSubcategoryThrows() {
        // Arrange
        when(subcategoryDao.getById(eq(NON_EXISTING_SUBCATEGORY_ID))).thenReturn(Optional.empty());

        // Act
        productService.findOrCreateByBrandModelYear(BRAND, MODEL, YEAR, NON_EXISTING_SUBCATEGORY_ID);
    }
}
