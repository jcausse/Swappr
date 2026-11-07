package ar.edu.itba.paw.service;

import ar.edu.itba.paw.model.Brand;
import ar.edu.itba.paw.model.Category;
import ar.edu.itba.paw.model.Condition;
import ar.edu.itba.paw.model.Model;
import ar.edu.itba.paw.model.ModelListingInfo;
import ar.edu.itba.paw.model.ModelListingInfoSummary;
import ar.edu.itba.paw.model.Page;
import ar.edu.itba.paw.model.Price;
import ar.edu.itba.paw.model.Subcategory;
import ar.edu.itba.paw.persistence.BrandDao;
import ar.edu.itba.paw.persistence.CategoryDao;
import ar.edu.itba.paw.persistence.ModelDao;
import ar.edu.itba.paw.persistence.SubcategoryDao;
import ar.edu.itba.paw.service.exception.NotFoundException;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class CatalogServiceImplTest {

    private static final long CATEGORY_ID = 1L;
    private static final long SUBCATEGORY_ID = 1L;
    private static final long BRAND_ID = 1L;
    private static final long MODEL_ID = 1L;
    private static final long AIR_MODEL_ID = 2L;
    private static final long NON_EXISTING_MODEL_ID = 9000L;

    private static final String BRAND_NAME = "Apple";
    private static final String MODEL_NAME = "MacBook Pro";

    @InjectMocks
    private CatalogServiceImpl catalogService;

    @Mock
    private CategoryDao categoryDao;
    @Mock
    private SubcategoryDao subcategoryDao;
    @Mock
    private BrandDao brandDao;
    @Mock
    private ModelDao modelDao;

    private Subcategory buildFakeSubcategory() {
        return Subcategory.builder()
            .id(SUBCATEGORY_ID)
            .name("Laptops")
            .category(Category.builder().id(CATEGORY_ID).name("Electronics").build())
            .build();
    }

    private Brand buildFakeBrand() {
        return Brand.builder()
            .id(BRAND_ID)
            .name(BRAND_NAME)
            .build();
    }

    private Model buildFakeModel(final long id, final String name) {
        return Model.builder()
            .id(id)
            .name(name)
            .brand(buildFakeBrand())
            .subcategory(buildFakeSubcategory())
            .build();
    }

    /* ---------------------------------------------------------------------------------------------- */
    /* getModelById                                                                                    */
    /* ---------------------------------------------------------------------------------------------- */

    @Test
    public void testGetModelByIdExists() {
        final Model model = buildFakeModel(MODEL_ID, MODEL_NAME);
        when(modelDao.getById(eq(MODEL_ID))).thenReturn(Optional.of(model));

        final Model result = catalogService.getModelById(MODEL_ID);

        Assert.assertEquals(MODEL_ID, (long) result.getId());
        Assert.assertEquals(MODEL_NAME, result.getName());
        Assert.assertEquals(BRAND_NAME, result.getBrand().getName());
    }

    @Test(expected = NotFoundException.class)
    public void testGetModelByIdDoesNotExistThrows() {
        when(modelDao.getById(eq(NON_EXISTING_MODEL_ID))).thenReturn(Optional.empty());

        catalogService.getModelById(NON_EXISTING_MODEL_ID);
    }

    /* ---------------------------------------------------------------------------------------------- */
    /* delegating reads                                                                                */
    /* ---------------------------------------------------------------------------------------------- */

    @Test
    public void testGetAllCategoriesDelegates() {
        final List<Category> categories = List.of(Category.builder().id(CATEGORY_ID).name("Electronics").build());
        when(categoryDao.getAll()).thenReturn(categories);

        final List<Category> result = catalogService.getAllCategories();

        Assert.assertEquals(categories, result);
    }

    @Test
    public void testGetSubcategoriesByCategoryDelegates() {
        final List<Subcategory> subcategories = List.of(buildFakeSubcategory());
        when(subcategoryDao.getByCategoryId(eq(CATEGORY_ID))).thenReturn(subcategories);

        final List<Subcategory> result = catalogService.getSubcategoriesByCategory(CATEGORY_ID);

        Assert.assertEquals(subcategories, result);
    }

    @Test
    public void testGetBrandsBySubcategoryDelegates() {
        final List<Brand> brands = List.of(buildFakeBrand());
        when(brandDao.getBySubcategoryId(eq(SUBCATEGORY_ID))).thenReturn(brands);

        final List<Brand> result = catalogService.getBrandsBySubcategory(SUBCATEGORY_ID);

        Assert.assertEquals(brands, result);
    }

    @Test
    public void testGetModelsBySubcategoryAndBrandDelegates() {
        final List<Model> models = List.of(buildFakeModel(MODEL_ID, MODEL_NAME));
        when(modelDao.getBySubcategoryIdAndBrandId(eq(SUBCATEGORY_ID), eq(BRAND_ID))).thenReturn(models);

        final List<Model> result = catalogService.getModelsBySubcategoryAndBrand(SUBCATEGORY_ID, BRAND_ID);

        Assert.assertEquals(models, result);
    }

    /* ---------------------------------------------------------------------------------------------- */
    /* getListingInfoSummary                                                                            */
    /* ---------------------------------------------------------------------------------------------- */

    @Test
    public void testGetListingInfoSummaryGroupsByModelAndKeepsPageMetadata() {
        final Model air = buildFakeModel(AIR_MODEL_ID, "MacBook Air");
        final Model pro = buildFakeModel(MODEL_ID, MODEL_NAME);
        final ModelListingInfo airFair = buildListingInfo(air, Condition.FAIR);
        final ModelListingInfo proLikeNew = buildListingInfo(pro, Condition.LIKE_NEW);
        final ModelListingInfo proGood = buildListingInfo(pro, Condition.GOOD);
        final Page<ModelListingInfo> daoPage = new Page<>(List.of(airFair, proLikeNew, proGood), 2, 10, 25);
        when(modelDao.getListingInfoSummary(BRAND_ID, MODEL_ID, 2020, 2024, SUBCATEGORY_ID, 2, 10))
            .thenReturn(daoPage);

        final Page<ModelListingInfoSummary> result = catalogService.getListingInfoSummary(
            BRAND_ID, MODEL_ID, 2020, 2024, SUBCATEGORY_ID, 2, 10
        );

        Assert.assertEquals(2, result.getPage());
        Assert.assertEquals(10, result.getPageSize());
        Assert.assertEquals(25, result.getTotalCount());
        Assert.assertEquals(2, result.getContent().size());
        Assert.assertEquals("MacBook Air", result.getContent().get(0).getModel().getName());
        Assert.assertEquals("MacBook Pro", result.getContent().get(1).getModel().getName());
        Assert.assertEquals(
            List.of(Condition.LIKE_NEW, Condition.GOOD),
            result.getContent().get(1).getListingInfoByCondition().stream()
                .map(ModelListingInfo::getCondition)
                .toList()
        );
    }

    @Test
    public void testGetListingInfoSummaryDefaultsNullPageToOne() {
        final Page<ModelListingInfo> daoPage = new Page<>(List.of(), 1, 10, 0);
        when(modelDao.getListingInfoSummary(BRAND_ID, MODEL_ID, null, null, SUBCATEGORY_ID, 1, 10))
            .thenReturn(daoPage);

        final Page<ModelListingInfoSummary> result = catalogService.getListingInfoSummary(
            BRAND_ID, MODEL_ID, null, null, SUBCATEGORY_ID, null, 10
        );

        Assert.assertEquals(1, result.getPage());
        Assert.assertTrue(result.getContent().isEmpty());
        Assert.assertEquals(0, result.getTotalCount());
    }

    /* ---------------------------------------------------------------------------------------------- */
    /* getListingInfoSummaryForModel                                                                   */
    /* ---------------------------------------------------------------------------------------------- */

    @Test
    public void testGetListingInfoSummaryForModelSortsConditions() {
        final Model model = buildFakeModel(MODEL_ID, MODEL_NAME);
        final ModelListingInfo good = buildListingInfo(model, Condition.GOOD);
        final ModelListingInfo likeNew = buildListingInfo(model, Condition.LIKE_NEW);
        final ModelListingInfoSummary summary = ModelListingInfoSummary.builder()
            .brand(model.getBrand())
            .model(model)
            .listingInfoByCondition(List.of(good, likeNew))
            .build();
        when(modelDao.getListingInfoSummaryForModel(eq(MODEL_ID))).thenReturn(Optional.of(summary));

        final Optional<ModelListingInfoSummary> result = catalogService.getListingInfoSummaryForModel(MODEL_ID);

        Assert.assertTrue(result.isPresent());
        Assert.assertEquals(
            List.of(Condition.LIKE_NEW, Condition.GOOD),
            result.get().getListingInfoByCondition().stream()
                .map(ModelListingInfo::getCondition)
                .toList()
        );
    }

    @Test
    public void testGetListingInfoSummaryForModelEmpty() {
        when(modelDao.getListingInfoSummaryForModel(eq(MODEL_ID))).thenReturn(Optional.empty());

        Assert.assertTrue(catalogService.getListingInfoSummaryForModel(MODEL_ID).isEmpty());
    }

    private ModelListingInfo buildListingInfo(final Model model, final Condition condition) {
        return ModelListingInfo.builder()
            .brand(model.getBrand())
            .model(model)
            .condition(condition)
            .listingCount(1L)
            .minPrice(new Price(BigDecimal.TEN))
            .maxPrice(new Price(BigDecimal.TEN))
            .avgPrice(new Price(BigDecimal.TEN))
            .build();
    }
}
