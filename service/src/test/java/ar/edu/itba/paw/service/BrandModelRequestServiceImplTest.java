package ar.edu.itba.paw.service;

import ar.edu.itba.paw.model.Brand;
import ar.edu.itba.paw.model.BrandModelRequest;
import ar.edu.itba.paw.model.BrandModelRequestType;
import ar.edu.itba.paw.model.Model;
import ar.edu.itba.paw.model.Subcategory;
import ar.edu.itba.paw.model.User;
import ar.edu.itba.paw.persistence.BrandModelRequestDao;
import ar.edu.itba.paw.service.dto.BrandModelRequestCreationDto;
import ar.edu.itba.paw.service.exception.ModelAlreadyExistsException;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class BrandModelRequestServiceImplTest {

    private static final long SUBCATEGORY_ID = 1L;
    private static final long BRAND_ID = 2L;
    private static final long MODEL_ID = 3L;
    private static final long REQUESTER_ID = 4L;
    private static final String BRAND_NAME = "Acme";
    private static final String MODEL_NAME = "Phone";

    @InjectMocks
    private BrandModelRequestServiceImpl service;

    @Mock
    private BrandModelRequestDao brandModelRequestDao;
    @Mock
    private CatalogService catalogService;
    @Mock
    private UserService userService;
    @Mock
    private NotificationDispatcherService notificationDispatcherService;

    private Subcategory buildSubcategory() {
        return Subcategory.builder().id(SUBCATEGORY_ID).name("Phones").build();
    }

    private Brand buildBrand() {
        return Brand.builder().id(BRAND_ID).name(BRAND_NAME).build();
    }

    private User buildRequester() {
        return User.builder().id(REQUESTER_ID).build();
    }

    private void stubCommonCatalog() {
        when(catalogService.getSubcategoryById(SUBCATEGORY_ID)).thenReturn(buildSubcategory());
        when(userService.getById(REQUESTER_ID)).thenReturn(Optional.of(buildRequester()));
    }

    private void stubCreate() {
        when(brandModelRequestDao.create(any(), any(), any(), any(), any(), any()))
            .thenAnswer(invocation -> BrandModelRequest.builder()
                .type(invocation.getArgument(0))
                .brand(invocation.getArgument(1))
                .brandName(invocation.getArgument(2))
                .modelName(invocation.getArgument(3))
                .subcategory(invocation.getArgument(4))
                .requester(invocation.getArgument(5))
                .build());
    }

    /* ---------------------------------------------------------------------------------------------- */
    /* create: new brand                                                                               */
    /* ---------------------------------------------------------------------------------------------- */

    @Test
    public void testCreateNewBrandKeepsNewBrandWhenNameIsFree() {
        stubCommonCatalog();
        stubCreate();
        when(catalogService.findBrandByName(BRAND_NAME)).thenReturn(Optional.empty());

        final BrandModelRequest request = service.create(new BrandModelRequestCreationDto(
            REQUESTER_ID,
            BrandModelRequestType.NEW_BRAND,
            SUBCATEGORY_ID,
            null,
            "  " + BRAND_NAME + "  ",
            "  " + MODEL_NAME + "  "
        ));

        Assert.assertEquals(BrandModelRequestType.NEW_BRAND, request.getType());
        Assert.assertNull(request.getBrand());
        Assert.assertEquals(BRAND_NAME, request.getBrandName());
        Assert.assertEquals(MODEL_NAME, request.getModelName());
    }

    @Test
    public void testCreateNewBrandConvertsToExistingBrandWhenNameExists() {
        stubCommonCatalog();
        stubCreate();
        final Brand existingBrand = buildBrand();
        when(catalogService.findBrandByName(BRAND_NAME)).thenReturn(Optional.of(existingBrand));
        when(catalogService.findModelByNameAndBrand(MODEL_NAME, BRAND_ID)).thenReturn(Optional.empty());

        final BrandModelRequest request = service.create(new BrandModelRequestCreationDto(
            REQUESTER_ID,
            BrandModelRequestType.NEW_BRAND,
            SUBCATEGORY_ID,
            null,
            BRAND_NAME,
            MODEL_NAME
        ));

        Assert.assertEquals(BrandModelRequestType.EXISTING_BRAND, request.getType());
        Assert.assertSame(existingBrand, request.getBrand());
        Assert.assertNull(request.getBrandName());
        Assert.assertEquals(MODEL_NAME, request.getModelName());
    }

    @Test
    public void testCreateNewBrandFailsWhenConvertedBrandAlreadyHasTheModel() {
        stubCommonCatalog();
        final Brand existingBrand = buildBrand();
        final Model existingModel = Model.builder()
            .id(MODEL_ID)
            .name(MODEL_NAME)
            .brand(existingBrand)
            .build();
        when(catalogService.findBrandByName(BRAND_NAME)).thenReturn(Optional.of(existingBrand));
        when(catalogService.findModelByNameAndBrand(MODEL_NAME, BRAND_ID)).thenReturn(Optional.of(existingModel));

        try {
            service.create(new BrandModelRequestCreationDto(
                REQUESTER_ID,
                BrandModelRequestType.NEW_BRAND,
                SUBCATEGORY_ID,
                null,
                BRAND_NAME,
                MODEL_NAME
            ));
            Assert.fail("Expected ModelAlreadyExistsException");
        } catch (ModelAlreadyExistsException ex) {
            Assert.assertEquals(Long.valueOf(MODEL_ID), ex.getModelId());
            Assert.assertEquals(BRAND_NAME, ex.getBrandName());
            Assert.assertEquals(MODEL_NAME, ex.getModelName());
        }
    }

    /* ---------------------------------------------------------------------------------------------- */
    /* create: existing brand                                                                          */
    /* ---------------------------------------------------------------------------------------------- */

    @Test
    public void testCreateExistingBrandCreatesRequestWhenModelIsFree() {
        stubCommonCatalog();
        stubCreate();
        final Brand existingBrand = buildBrand();
        when(catalogService.getBrandById(BRAND_ID)).thenReturn(existingBrand);
        when(catalogService.findModelByNameAndBrand(MODEL_NAME, BRAND_ID)).thenReturn(Optional.empty());

        final BrandModelRequest request = service.create(new BrandModelRequestCreationDto(
            REQUESTER_ID,
            BrandModelRequestType.EXISTING_BRAND,
            SUBCATEGORY_ID,
            BRAND_ID,
            null,
            MODEL_NAME
        ));

        Assert.assertEquals(BrandModelRequestType.EXISTING_BRAND, request.getType());
        Assert.assertSame(existingBrand, request.getBrand());
        Assert.assertNull(request.getBrandName());
    }

    @Test
    public void testCreateExistingBrandFailsWhenModelAlreadyExists() {
        stubCommonCatalog();
        final Brand existingBrand = buildBrand();
        final Model existingModel = Model.builder()
            .id(MODEL_ID)
            .name(MODEL_NAME)
            .brand(existingBrand)
            .build();
        when(catalogService.getBrandById(BRAND_ID)).thenReturn(existingBrand);
        when(catalogService.findModelByNameAndBrand(MODEL_NAME, BRAND_ID)).thenReturn(Optional.of(existingModel));

        try {
            service.create(new BrandModelRequestCreationDto(
                REQUESTER_ID,
                BrandModelRequestType.EXISTING_BRAND,
                SUBCATEGORY_ID,
                BRAND_ID,
                null,
                MODEL_NAME
            ));
            Assert.fail("Expected ModelAlreadyExistsException");
        } catch (ModelAlreadyExistsException ex) {
            Assert.assertEquals(Long.valueOf(MODEL_ID), ex.getModelId());
        }
    }
}
