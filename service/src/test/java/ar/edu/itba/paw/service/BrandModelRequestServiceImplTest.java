package ar.edu.itba.paw.service;

import ar.edu.itba.paw.model.Brand;
import ar.edu.itba.paw.model.BrandModelRequest;
import ar.edu.itba.paw.model.BrandModelRequestType;
import ar.edu.itba.paw.model.Model;
import ar.edu.itba.paw.model.Subcategory;
import ar.edu.itba.paw.model.User;
import ar.edu.itba.paw.persistence.BrandModelRequestDao;
import ar.edu.itba.paw.service.dto.BrandModelRequestAcceptanceDto;
import ar.edu.itba.paw.service.dto.BrandModelRequestCreationDto;
import ar.edu.itba.paw.service.exception.BrandModelRequestAlreadyExistsException;
import ar.edu.itba.paw.service.exception.ModelAlreadyExistsException;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class BrandModelRequestServiceImplTest {

    private static final long SUBCATEGORY_ID = 1L;
    private static final long BRAND_ID = 2L;
    private static final long MODEL_ID = 3L;
    private static final long REQUESTER_ID = 4L;
    private static final long OTHER_USER_ID = 5L;
    private static final long REQUEST_ID = 6L;
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

    private User buildOtherUser() {
        return User.builder().id(OTHER_USER_ID).build();
    }

    private void stubCommonCatalog() {
        when(catalogService.getSubcategoryById(SUBCATEGORY_ID)).thenReturn(buildSubcategory());
        when(userService.getById(REQUESTER_ID)).thenReturn(Optional.of(buildRequester()));
    }

    private void stubNoMatchingRequest() {
        when(brandModelRequestDao.findMatching(any(), any())).thenReturn(Optional.empty());
    }

    private BrandModelRequest buildRequest(User requester, Set<User> additionalRequesters) {
        return BrandModelRequest.builder()
            .id(REQUEST_ID)
            .type(BrandModelRequestType.EXISTING_BRAND)
            .brand(buildBrand())
            .modelName(MODEL_NAME)
            .subcategory(buildSubcategory())
            .requester(requester)
            .additionalRequesters(new HashSet<>(additionalRequesters))
            .build();
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
        stubNoMatchingRequest();
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
        stubNoMatchingRequest();
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
        stubNoMatchingRequest();
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

    /* ---------------------------------------------------------------------------------------------- */
    /* create: duplicate / attach to existing request                                                  */
    /* ---------------------------------------------------------------------------------------------- */

    @Test
    public void testCreateFailsWhenUserAlreadyRequestedTheModel() {
        stubCommonCatalog();
        final Brand existingBrand = buildBrand();
        final User requester = buildRequester();
        final BrandModelRequest existingRequest = buildRequest(requester, Set.of());
        when(catalogService.getBrandById(BRAND_ID)).thenReturn(existingBrand);
        when(catalogService.findModelByNameAndBrand(MODEL_NAME, BRAND_ID)).thenReturn(Optional.empty());
        when(brandModelRequestDao.findMatching(BRAND_NAME, MODEL_NAME))
            .thenReturn(Optional.of(existingRequest));

        try {
            service.create(new BrandModelRequestCreationDto(
                REQUESTER_ID,
                BrandModelRequestType.EXISTING_BRAND,
                SUBCATEGORY_ID,
                BRAND_ID,
                null,
                MODEL_NAME
            ));
            Assert.fail("Expected BrandModelRequestAlreadyExistsException");
        } catch (BrandModelRequestAlreadyExistsException ex) {
            Assert.assertEquals(MODEL_NAME, ex.getRequest().modelName());
        }
    }

    @Test
    public void testCreateFailsWhenUserIsAlreadyAnAdditionalRequester() {
        stubCommonCatalog();
        final Brand existingBrand = buildBrand();
        final BrandModelRequest existingRequest = buildRequest(buildOtherUser(), Set.of(buildRequester()));
        when(catalogService.getBrandById(BRAND_ID)).thenReturn(existingBrand);
        when(catalogService.findModelByNameAndBrand(MODEL_NAME, BRAND_ID)).thenReturn(Optional.empty());
        when(brandModelRequestDao.findMatching(BRAND_NAME, MODEL_NAME))
            .thenReturn(Optional.of(existingRequest));
        when(brandModelRequestDao.isAdditionalRequester(REQUEST_ID, REQUESTER_ID)).thenReturn(true);

        try {
            service.create(new BrandModelRequestCreationDto(
                REQUESTER_ID,
                BrandModelRequestType.EXISTING_BRAND,
                SUBCATEGORY_ID,
                BRAND_ID,
                null,
                MODEL_NAME
            ));
            Assert.fail("Expected BrandModelRequestAlreadyExistsException");
        } catch (BrandModelRequestAlreadyExistsException ex) {
            Assert.assertEquals(MODEL_NAME, ex.getRequest().modelName());
        }
    }

    @Test
    public void testCreateAttachesNewRequesterToExistingRequest() {
        stubCommonCatalog();
        final Brand existingBrand = buildBrand();
        final BrandModelRequest existingRequest = buildRequest(buildOtherUser(), Set.of());
        when(catalogService.getBrandById(BRAND_ID)).thenReturn(existingBrand);
        when(catalogService.findModelByNameAndBrand(MODEL_NAME, BRAND_ID)).thenReturn(Optional.empty());
        when(brandModelRequestDao.findMatching(BRAND_NAME, MODEL_NAME))
            .thenReturn(Optional.of(existingRequest));
        when(brandModelRequestDao.isAdditionalRequester(REQUEST_ID, REQUESTER_ID)).thenReturn(false);

        final AtomicReference<Long> attachedRequestId = new AtomicReference<>();
        final AtomicReference<Long> attachedUserId = new AtomicReference<>();
        doAnswer(invocation -> {
            attachedRequestId.set(invocation.getArgument(0));
            attachedUserId.set(invocation.getArgument(1));
            return null;
        }).when(brandModelRequestDao).addAdditionalRequester(any(), any());

        final BrandModelRequest result = service.create(new BrandModelRequestCreationDto(
            REQUESTER_ID,
            BrandModelRequestType.EXISTING_BRAND,
            SUBCATEGORY_ID,
            BRAND_ID,
            null,
            MODEL_NAME
        ));

        Assert.assertSame(existingRequest, result);
        Assert.assertEquals(Long.valueOf(REQUEST_ID), attachedRequestId.get());
        Assert.assertEquals(Long.valueOf(REQUESTER_ID), attachedUserId.get());
    }

    /* ---------------------------------------------------------------------------------------------- */
    /* accept / reject notify all requesters                                                           */
    /* ---------------------------------------------------------------------------------------------- */

    @Test
    public void testAcceptNotifiesPrimaryAndAdditionalRequesters() {
        final User primary = buildRequester();
        final User additional = buildOtherUser();
        final BrandModelRequest existingRequest = buildRequest(primary, Set.of(additional));
        final Model model = Model.builder()
            .id(MODEL_ID)
            .name(MODEL_NAME)
            .brand(buildBrand())
            .subcategory(buildSubcategory())
            .build();
        when(brandModelRequestDao.getById(REQUEST_ID)).thenReturn(Optional.of(existingRequest));
        when(catalogService.getBrandById(BRAND_ID)).thenReturn(buildBrand());
        when(catalogService.createModel(MODEL_NAME, BRAND_ID, SUBCATEGORY_ID)).thenReturn(model);
        when(brandModelRequestDao.delete(REQUEST_ID)).thenReturn(true);

        final AtomicReference<List<User>> recipients = new AtomicReference<>();
        doAnswer(invocation -> {
            recipients.set(invocation.getArgument(1));
            return null;
        }).when(notificationDispatcherService)
            .notifyBrandModelRequestAccepted(any(), anyList(), any(), any());

        service.accept(REQUEST_ID, new BrandModelRequestAcceptanceDto(
            BrandModelRequestType.EXISTING_BRAND,
            BRAND_ID,
            null,
            MODEL_NAME
        ));

        Assert.assertEquals(2, recipients.get().size());
        Assert.assertTrue(recipients.get().contains(primary));
        Assert.assertTrue(recipients.get().contains(additional));
    }

    @Test
    public void testRejectNotifiesPrimaryAndAdditionalRequesters() {
        final User primary = buildRequester();
        final User additional = buildOtherUser();
        final BrandModelRequest existingRequest = buildRequest(primary, Set.of(additional));
        when(brandModelRequestDao.getById(REQUEST_ID)).thenReturn(Optional.of(existingRequest));
        when(brandModelRequestDao.delete(REQUEST_ID)).thenReturn(true);

        final AtomicReference<List<User>> recipients = new AtomicReference<>();
        doAnswer(invocation -> {
            recipients.set(invocation.getArgument(1));
            return null;
        }).when(notificationDispatcherService).notifyBrandModelRequestRejected(any(), anyList());

        service.reject(REQUEST_ID);

        Assert.assertEquals(2, recipients.get().size());
        Assert.assertTrue(recipients.get().contains(primary));
        Assert.assertTrue(recipients.get().contains(additional));
    }
}
