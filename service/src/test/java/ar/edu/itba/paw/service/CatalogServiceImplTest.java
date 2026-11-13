package ar.edu.itba.paw.service;

import ar.edu.itba.paw.model.Brand;
import ar.edu.itba.paw.model.BrandInfo;
import ar.edu.itba.paw.model.BrandSort;
import ar.edu.itba.paw.model.Category;
import ar.edu.itba.paw.model.Condition;
import ar.edu.itba.paw.model.Model;
import ar.edu.itba.paw.model.ModelInfo;
import ar.edu.itba.paw.model.ModelListingInfo;
import ar.edu.itba.paw.model.ModelListingInfoSummary;
import ar.edu.itba.paw.model.ModelSort;
import ar.edu.itba.paw.model.Page;
import ar.edu.itba.paw.model.Price;
import ar.edu.itba.paw.model.Subcategory;
import ar.edu.itba.paw.persistence.BrandDao;
import ar.edu.itba.paw.persistence.CategoryDao;
import ar.edu.itba.paw.persistence.ListingDao;
import ar.edu.itba.paw.persistence.ModelDao;
import ar.edu.itba.paw.persistence.SubcategoryDao;
import ar.edu.itba.paw.service.exception.BadParameterException;
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

import static org.mockito.ArgumentMatchers.isNull;
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
    @Mock
    private ListingDao listingDao;

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
    public void testFindBrandByNameDelegates() {
        final Brand brand = buildFakeBrand();
        when(brandDao.getByName(eq(BRAND_NAME))).thenReturn(Optional.of(brand));

        final Optional<Brand> result = catalogService.findBrandByName(BRAND_NAME);

        Assert.assertTrue(result.isPresent());
        Assert.assertSame(brand, result.get());
    }

    @Test
    public void testFindModelByNameAndBrandDelegates() {
        final Model model = buildFakeModel(MODEL_ID, MODEL_NAME);
        when(modelDao.getByNameAndBrandId(eq(MODEL_NAME), eq(BRAND_ID))).thenReturn(Optional.of(model));

        final Optional<Model> result = catalogService.findModelByNameAndBrand(MODEL_NAME, BRAND_ID);

        Assert.assertTrue(result.isPresent());
        Assert.assertSame(model, result.get());
    }

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
    /* brand management                                                                                */
    /* ---------------------------------------------------------------------------------------------- */

    @Test
    public void testGetBrandsDelegatesToDao() {
        final Page<BrandInfo> brandsPage = new Page<>(List.of(
                BrandInfo.builder().id(1L).name("Apple").modelCount(2L).listingCount(1L).build(),
                BrandInfo.builder().id(2L).name("Dell").modelCount(0L).listingCount(0L).build()), 2, 10, 12);
        when(brandDao.getWithStats(eq(BrandSort.BRAND_ASC), eq(2), eq(10))).thenReturn(brandsPage);

        final Page<BrandInfo> result = catalogService.getBrands(BrandSort.BRAND_ASC, 2, 10);

        Assert.assertSame(brandsPage, result);
    }

    @Test
    public void testGetBrandsDefaultsNullPageAndSort() {
        final Page<BrandInfo> brandsPage = new Page<>(List.of(), 1, 10, 0);
        when(brandDao.getWithStats(eq(BrandSort.BRAND_ASC), eq(1), eq(10))).thenReturn(brandsPage);

        final Page<BrandInfo> result = catalogService.getBrands(null, null, 10);

        Assert.assertSame(brandsPage, result);
    }

    @Test
    public void testGetAllBrandsDelegatesToDao() {
        final List<Brand> brands = List.of(buildFakeBrand());
        when(brandDao.getAll()).thenReturn(brands);

        final List<Brand> result = catalogService.getAllBrands();

        Assert.assertSame(brands, result);
    }

    @Test(expected = BadParameterException.class)
    public void testReplaceBrandWithSameBrandThrows() {
        catalogService.replaceBrand(BRAND_ID, BRAND_ID);
    }

    @Test(expected = NotFoundException.class)
    public void testReplaceMissingBrandThrows() {
        when(brandDao.getById(eq(NON_EXISTING_MODEL_ID))).thenReturn(Optional.empty());

        catalogService.replaceBrand(NON_EXISTING_MODEL_ID, 2L);
    }

    @Test(expected = NotFoundException.class)
    public void testReplaceWithMissingBrandThrows() {
        when(brandDao.getById(eq(BRAND_ID))).thenReturn(Optional.of(buildFakeBrand()));
        when(brandDao.getById(eq(NON_EXISTING_MODEL_ID))).thenReturn(Optional.empty());

        catalogService.replaceBrand(BRAND_ID, NON_EXISTING_MODEL_ID);
    }

    @Test
    public void testGetBrandReplacementOptionsExcludeSelf() {
        final Brand apple = buildFakeBrand();
        final Brand dell = Brand.builder().id(2L).name("Dell").build();
        when(brandDao.getAll()).thenReturn(List.of(apple, dell));

        final var options = catalogService.getBrandReplacementOptions();

        Assert.assertEquals(List.of(dell), options.get(BRAND_ID));
        Assert.assertEquals(List.of(apple), options.get(2L));
    }

    @Test
    public void testGetModelReplacementOptionsExcludeSelfAndLabelBrand() {
        final Model pro = buildFakeModel(MODEL_ID, MODEL_NAME);
        final Model air = buildFakeModel(AIR_MODEL_ID, "MacBook Air");
        when(modelDao.getAllWithBrand()).thenReturn(List.of(pro, air));

        final var options = catalogService.getModelReplacementOptions();

        Assert.assertEquals(1, options.get(MODEL_ID).size());
        Assert.assertEquals(AIR_MODEL_ID, (long) options.get(MODEL_ID).get(0).getId());
        Assert.assertEquals("Apple — MacBook Air", options.get(MODEL_ID).get(0).getLabel());
        Assert.assertEquals(1, options.get(AIR_MODEL_ID).size());
        Assert.assertEquals(MODEL_ID, (long) options.get(AIR_MODEL_ID).get(0).getId());
    }

    @Test
    public void testCreateBrandTrimsName() {
        final Brand created = Brand.builder().id(10L).name("HP").build();
        when(brandDao.getByName(eq("HP"))).thenReturn(Optional.empty());
        when(brandDao.create(eq("HP"))).thenReturn(created);

        final Brand result = catalogService.createBrand("  HP  ");

        Assert.assertSame(created, result);
    }

    @Test(expected = BadParameterException.class)
    public void testCreateBrandWithExistingNameThrows() {
        when(brandDao.getByName(eq("Apple"))).thenReturn(Optional.of(buildFakeBrand()));

        catalogService.createBrand("Apple");
    }

    @Test(expected = BadParameterException.class)
    public void testCreateBrandWithBlankNameThrows() {
        catalogService.createBrand("   ");
    }

    @Test(expected = BadParameterException.class)
    public void testRenameBrandToExistingNameThrows() {
        when(brandDao.existsByNameAndIdNot(eq("Dell"), eq(BRAND_ID))).thenReturn(true);

        catalogService.renameBrand(BRAND_ID, "Dell");
    }

    @Test(expected = BadParameterException.class)
    public void testRenameBrandToBlankNameThrows() {
        catalogService.renameBrand(BRAND_ID, "   ");
    }

    @Test(expected = NotFoundException.class)
    public void testRenameMissingBrandThrows() {
        when(brandDao.existsByNameAndIdNot(eq("New name"), eq(NON_EXISTING_MODEL_ID))).thenReturn(false);
        when(brandDao.rename(eq(NON_EXISTING_MODEL_ID), eq("New name"))).thenReturn(Optional.empty());

        catalogService.renameBrand(NON_EXISTING_MODEL_ID, "New name");
    }

    @Test(expected = BadParameterException.class)
    public void testDeleteBrandWithListingsThrows() {
        when(brandDao.hasListings(eq(BRAND_ID))).thenReturn(true);

        catalogService.deleteBrand(BRAND_ID);
    }

    @Test(expected = NotFoundException.class)
    public void testDeleteMissingBrandThrows() {
        when(brandDao.hasListings(eq(NON_EXISTING_MODEL_ID))).thenReturn(false);
        when(brandDao.delete(eq(NON_EXISTING_MODEL_ID))).thenReturn(false);

        catalogService.deleteBrand(NON_EXISTING_MODEL_ID);
    }

    /* ---------------------------------------------------------------------------------------------- */
    /* model management                                                                                */
    /* ---------------------------------------------------------------------------------------------- */

    @Test
    public void testGetModelsDefaultsPageAndSort() {
        final Page<ModelInfo> modelsPage = new Page<>(List.of(), 1, 10, 0);
        when(modelDao.getWithInfo(null, null, ModelSort.NAME_ASC, 1, 10)).thenReturn(modelsPage);

        final Page<ModelInfo> result = catalogService.getModels(null, null, null, null, 10);

        Assert.assertSame(modelsPage, result);
    }

    @Test
    public void testGetAllModelsDelegates() {
        final List<Model> models = List.of(buildFakeModel(MODEL_ID, MODEL_NAME));
        when(modelDao.getAllWithBrand()).thenReturn(models);

        Assert.assertSame(models, catalogService.getAllModels());
    }

    @Test
    public void testGetAllSubcategoriesDelegates() {
        final List<Subcategory> subcategories = List.of(buildFakeSubcategory());
        when(subcategoryDao.getAll()).thenReturn(subcategories);

        Assert.assertSame(subcategories, catalogService.getAllSubcategories());
    }

    @Test
    public void testCreateModelTrimsName() {
        final Brand brand = buildFakeBrand();
        final Subcategory subcategory = buildFakeSubcategory();
        final Model created = buildFakeModel(MODEL_ID, "New Model");
        when(brandDao.getById(eq(BRAND_ID))).thenReturn(Optional.of(brand));
        when(subcategoryDao.getById(eq(SUBCATEGORY_ID))).thenReturn(Optional.of(subcategory));
        when(modelDao.existsByNameAndBrandId(eq("New Model"), eq(BRAND_ID))).thenReturn(false);
        when(modelDao.create(eq("New Model"), eq(brand), eq(subcategory))).thenReturn(created);

        final Model result = catalogService.createModel("  New Model  ", BRAND_ID, SUBCATEGORY_ID);

        Assert.assertSame(created, result);
    }

    @Test(expected = BadParameterException.class)
    public void testCreateModelWithExistingNameThrows() {
        when(brandDao.getById(eq(BRAND_ID))).thenReturn(Optional.of(buildFakeBrand()));
        when(subcategoryDao.getById(eq(SUBCATEGORY_ID))).thenReturn(Optional.of(buildFakeSubcategory()));
        when(modelDao.existsByNameAndBrandId(eq(MODEL_NAME), eq(BRAND_ID))).thenReturn(true);

        catalogService.createModel(MODEL_NAME, BRAND_ID, SUBCATEGORY_ID);
    }

    @Test(expected = NotFoundException.class)
    public void testCreateModelWithMissingBrandThrows() {
        when(brandDao.getById(eq(BRAND_ID))).thenReturn(Optional.empty());

        catalogService.createModel("New Model", BRAND_ID, SUBCATEGORY_ID);
    }

    @Test(expected = BadParameterException.class)
    public void testDeleteModelWithListingsThrows() {
        when(modelDao.hasListings(eq(MODEL_ID))).thenReturn(true);

        catalogService.deleteModel(MODEL_ID);
    }

    @Test(expected = NotFoundException.class)
    public void testDeleteMissingModelThrows() {
        when(modelDao.hasListings(eq(NON_EXISTING_MODEL_ID))).thenReturn(false);
        when(modelDao.deleteModel(eq(NON_EXISTING_MODEL_ID))).thenReturn(false);

        catalogService.deleteModel(NON_EXISTING_MODEL_ID);
    }

    @Test(expected = BadParameterException.class)
    public void testReplaceModelWithSameModelThrows() {
        catalogService.replaceModel(MODEL_ID, MODEL_ID);
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
    public void testGetListingInfoSummaryForModelKeepsQueryConditionOrder() {
        final Model model = buildFakeModel(MODEL_ID, MODEL_NAME);
        final ModelListingInfo good = buildListingInfo(model, Condition.GOOD);
        final ModelListingInfo likeNew = buildListingInfo(model, Condition.LIKE_NEW);
        final ModelListingInfoSummary summary = ModelListingInfoSummary.builder()
            .brand(model.getBrand())
            .model(model)
            .listingInfoByCondition(List.of(likeNew, good))
            .build();
        when(modelDao.getListingInfoSummaryForModel(eq(MODEL_ID), isNull())).thenReturn(Optional.of(summary));

        final Optional<ModelListingInfoSummary> result = catalogService.getListingInfoSummaryForModel(MODEL_ID, null);

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
        when(modelDao.getListingInfoSummaryForModel(eq(MODEL_ID), isNull())).thenReturn(Optional.empty());

        Assert.assertTrue(catalogService.getListingInfoSummaryForModel(MODEL_ID, null).isEmpty());
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
