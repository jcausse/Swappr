package ar.edu.itba.paw.service;

import ar.edu.itba.paw.model.Brand;
import ar.edu.itba.paw.model.Category;
import ar.edu.itba.paw.model.Condition;
import ar.edu.itba.paw.model.Image;
import ar.edu.itba.paw.model.Listing;
import ar.edu.itba.paw.model.ListingFilter;
import ar.edu.itba.paw.model.ListingSort;
import ar.edu.itba.paw.model.ListingStatus;
import ar.edu.itba.paw.model.Page;
import ar.edu.itba.paw.model.Model;
import ar.edu.itba.paw.model.Price;
import ar.edu.itba.paw.model.Subcategory;
import ar.edu.itba.paw.model.User;
import ar.edu.itba.paw.persistence.ListingDao;
import ar.edu.itba.paw.service.dto.ImageData;
import ar.edu.itba.paw.service.dto.ListingCreationDto;
import ar.edu.itba.paw.service.dto.ListingFilterDto;
import ar.edu.itba.paw.service.dto.ListingUpdateDto;
import ar.edu.itba.paw.service.exception.BadParameterException;
import ar.edu.itba.paw.service.exception.NotFoundException;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class ListingServiceImplTest {

    private static final long LISTING_ID = 1L;
    private static final long NON_EXISTING_ID = 9000L;
    private static final String LISTING_TITLE = "MacBook Pro 2023";
    private static final String LISTING_DESCRIPTION = "Barely used";
    private static final Price LISTING_PRICE = new Price(new BigDecimal("1500.00"));

    private static final long SELLER_ID = 1L;
    private static final long BUYER_ID = 2L;
    private static final String PURCHASE_MESSAGE = "Can we meet tomorrow?";

    private static final long CATEGORY_ID = 1L;
    private static final long SUBCATEGORY_ID = 1L;
    private static final long BRAND_ID = 1L;
    private static final long MODEL_ID = 1L;
    private static final int YEAR = 2023;
    private static final long IMAGE_ID = 10L;
    private static final long FOREIGN_IMAGE_ID = 999L;
    private static final int PAGE_SIZE = 12;

    @InjectMocks
    private ListingServiceImpl listingService;
    @Mock
    private ListingDao listingDao;
    @Mock
    private UserService userService;
    @Mock
    private CatalogService catalogService;
    @Mock
    private ImageService imageService;
    @Mock
    private NotificationDispatcherService notificationDispatcherService;
    @Mock
    private OfferService offerService;

    /* ---------------------------------------------------------------------------------------------- */
    /* Fixtures                                                                                        */
    /* ---------------------------------------------------------------------------------------------- */

    private static User buildFakeUser(final long id, final String username) {
        return User.builder()
            .id(id)
            .username(username)
            .displayName(username)
            .email(username + "@example.com")
            .password("fake_password")
            .joinedAt(Instant.now())
            .build();
    }

    private static Model buildFakeModel() {
        final Category category = Category.builder().id(CATEGORY_ID).name("Electronics").build();
        final Subcategory subcategory = Subcategory.builder().id(SUBCATEGORY_ID).name("Laptops").category(category).build();
        final Brand brand = Brand.builder().id(BRAND_ID).name("Apple").build();
        return Model.builder()
            .id(MODEL_ID)
            .name("MacBook Pro")
            .brand(brand)
            .subcategory(subcategory)
            .build();
    }

    private static Listing buildFakeListing(final User creator) {
        return buildFakeListing(creator, ListingStatus.ACTIVE);
    }

    private static Listing buildFakeListing(final User creator, final ListingStatus status) {
        return Listing.builder()
            .id(LISTING_ID)
            .title(LISTING_TITLE)
            .creator(creator)
            .price(LISTING_PRICE)
            .model(buildFakeModel())
            .year(YEAR)
            .description(LISTING_DESCRIPTION)
            .status(status)
            .condition(Condition.GOOD)
            .acceptsTrade(true)
            .build();
    }

    private static ListingCreationDto buildCreationDto(final Condition condition, final List<ImageData> images) {
        return new ListingCreationDto(
            LISTING_TITLE, LISTING_PRICE.getAmount(), SELLER_ID, MODEL_ID, YEAR, condition, true, false, LISTING_DESCRIPTION, images
        );
    }

    private static ListingUpdateDto buildUpdateDto(final long listingId, final Condition condition) {
        return new ListingUpdateDto(
            listingId, LISTING_TITLE, LISTING_PRICE.getAmount(), MODEL_ID, YEAR, condition, true, false, LISTING_DESCRIPTION,
            List.of(), List.of()
        );
    }

    private static ListingFilterDto buildFilterDto(
        final Condition condition,
        final ListingSort sort,
        final ListingStatus status,
        final Integer page,
        final BigDecimal minPrice,
        final BigDecimal maxPrice,
        final Boolean acceptsTrade
    ) {
        return new ListingFilterDto(
            null, null, minPrice, maxPrice, condition, acceptsTrade, null, sort, null, status, page, PAGE_SIZE, null, null, null, null, null, null, null
        );
    }

    /* ---------------------------------------------------------------------------------------------- */
    /* getById                                                                                         */
    /* ---------------------------------------------------------------------------------------------- */

    @Test
    public void testGetByIdExists() {
        // Arrange
        final Listing listing = buildFakeListing(buildFakeUser(SELLER_ID, "seller"));
        when(listingDao.getById(eq(LISTING_ID))).thenReturn(Optional.of(listing));

        // Act
        final Listing result = listingService.getById(LISTING_ID);

        // Assert
        Assert.assertEquals(LISTING_ID, (long) result.getId());
        Assert.assertEquals(LISTING_TITLE, result.getTitle());
    }

    @Test(expected = NotFoundException.class)
    public void testGetByIdDoesNotExistThrows() {
        // Arrange
        when(listingDao.getById(eq(NON_EXISTING_ID))).thenReturn(Optional.empty());

        // Act
        listingService.getById(NON_EXISTING_ID);
    }

    /* ---------------------------------------------------------------------------------------------- */
    /* search                                                                                          */
    /* ---------------------------------------------------------------------------------------------- */

    @Test
    public void testSearchReturnsDaoPage() {
        // Arrange
        final Page<Listing> page = new Page<>(List.of(buildFakeListing(buildFakeUser(SELLER_ID, "seller"))), 1, PAGE_SIZE, 1);
        when(listingDao.search(any(ListingFilter.class))).thenReturn(page);

        // Act
        final Page<Listing> result = listingService.search(buildFilterDto(null, null, null, 1, null, null, null));

        // Assert
        Assert.assertSame(page, result);
    }

    @Test(expected = NullPointerException.class)
    public void testSearchNullDtoThrows() {
        // Act
        listingService.search(null);
    }

    /* ---------------------------------------------------------------------------------------------- */
    /* create                                                                                          */
    /* ---------------------------------------------------------------------------------------------- */

    @Test
    public void testCreateReturnsCreatedListing() {
        // Arrange
        final User seller = buildFakeUser(SELLER_ID, "seller");
        final Model model = buildFakeModel();
        final Listing listing = buildFakeListing(seller);
        when(userService.getById(eq(SELLER_ID))).thenReturn(Optional.of(seller));
        when(catalogService.getModelById(eq(MODEL_ID))).thenReturn(model);
        when(listingDao.create(
            eq(LISTING_TITLE), eq(LISTING_PRICE), eq(seller), eq(model), eq(YEAR),
            eq(Condition.GOOD), eq(true), anyBoolean(), eq(LISTING_DESCRIPTION), eq(List.of())
        )).thenReturn(listing);

        // Act
        final Listing result = listingService.create(buildCreationDto(Condition.GOOD, null));

        // Assert
        Assert.assertEquals(LISTING_ID, (long) result.getId());
    }

    @Test
    public void testCreateStoresOnlyNonEmptyImages() {
        // Arrange
        final User seller = buildFakeUser(SELLER_ID, "seller");
        final Listing listing = buildFakeListing(seller);
        final List<ImageData> images = Arrays.asList(
            new ImageData(new byte[]{ 1, 2, 3 }, "photo.jpg", "image/jpeg"),
            new ImageData(new byte[0], "empty.jpg", "image/jpeg"),
            null
        );
        final Image storedImage = Image.builder().id(IMAGE_ID).filename("photo.jpg").alt("alt").build();
        when(userService.getById(eq(SELLER_ID))).thenReturn(Optional.of(seller));
        when(catalogService.getModelById(eq(MODEL_ID))).thenReturn(buildFakeModel());
        when(imageService.create(
            eq("photo.jpg"), eq("Image 1 for listing: " + LISTING_TITLE), eq("image/jpeg"), any(byte[].class)
        )).thenReturn(storedImage);
        when(listingDao.create(any(), any(), any(), any(), anyInt(), any(), anyBoolean(), anyBoolean(), any(), eq(List.of(IMAGE_ID))))
            .thenReturn(listing);

        // Act
        final Listing result = listingService.create(buildCreationDto(Condition.GOOD, images));

        // Assert
        Assert.assertEquals(LISTING_ID, (long) result.getId());
    }

    @Test(expected = BadParameterException.class)
    public void testCreateUnknownCreatorThrows() {
        // Arrange
        when(userService.getById(eq(SELLER_ID))).thenReturn(Optional.empty());

        // Act
        listingService.create(buildCreationDto(Condition.GOOD, null));
    }

    @Test(expected = BadParameterException.class)
    public void testCreateUnknownModelThrows() {
        // Arrange
        when(userService.getById(eq(SELLER_ID))).thenReturn(Optional.of(buildFakeUser(SELLER_ID, "seller")));
        when(catalogService.getModelById(eq(MODEL_ID))).thenThrow(NotFoundException.createFor("Model with ID " + MODEL_ID));

        // Act
        listingService.create(buildCreationDto(Condition.GOOD, null));
    }

    @Test(expected = BadParameterException.class)
    public void testCreateMissingConditionThrows() {
        // Arrange
        when(userService.getById(eq(SELLER_ID))).thenReturn(Optional.of(buildFakeUser(SELLER_ID, "seller")));
        when(catalogService.getModelById(eq(MODEL_ID))).thenReturn(buildFakeModel());

        // Act
        listingService.create(buildCreationDto(null, null));
    }

    @Test(expected = NullPointerException.class)
    public void testCreateNullDtoThrows() {
        // Act
        listingService.create(null);
    }

    /* ---------------------------------------------------------------------------------------------- */
    /* purchase                                                                                        */
    /* ---------------------------------------------------------------------------------------------- */

    @Test
    public void testPurchaseReturnsListing() {
        // Arrange
        final User seller = buildFakeUser(SELLER_ID, "seller");
        final Listing listing = buildFakeListing(seller, ListingStatus.PENDING_TRANSACTION);
        when(listingDao.getById(eq(LISTING_ID))).thenReturn(Optional.of(listing));
        when(userService.getById(eq(BUYER_ID))).thenReturn(Optional.of(buildFakeUser(BUYER_ID, "buyer")));

        // Act
        final Listing result = listingService.purchase(LISTING_ID, BUYER_ID, PURCHASE_MESSAGE);

        // Assert
        Assert.assertEquals(LISTING_ID, (long) result.getId());
    }

    @Test(expected = NotFoundException.class)
    public void testPurchaseNonExistingListingThrows() {
        // Arrange
        when(listingDao.getById(eq(NON_EXISTING_ID))).thenReturn(Optional.empty());

        // Act
        listingService.purchase(NON_EXISTING_ID, BUYER_ID, PURCHASE_MESSAGE);
    }

    @Test(expected = BadParameterException.class)
    public void testPurchaseUnknownBuyerThrows() {
        // Arrange
        when(listingDao.getById(eq(LISTING_ID))).thenReturn(Optional.of(buildFakeListing(buildFakeUser(SELLER_ID, "seller"), ListingStatus.PENDING_TRANSACTION)));
        when(userService.getById(eq(BUYER_ID))).thenReturn(Optional.empty());

        // Act
        listingService.purchase(LISTING_ID, BUYER_ID, PURCHASE_MESSAGE);
    }

    /* ---------------------------------------------------------------------------------------------- */
    /* pendingTransaction                                                                              */
    /* ---------------------------------------------------------------------------------------------- */

    @Test(expected = NotFoundException.class)
    public void testPendingTransactionNonExistingListingThrows() {
        // Arrange
        when(listingDao.getById(eq(NON_EXISTING_ID))).thenReturn(Optional.empty());

        // Act
        listingService.pendingTransaction(NON_EXISTING_ID, BUYER_ID, PURCHASE_MESSAGE);
    }

    /* ---------------------------------------------------------------------------------------------- */
    /* update                                                                                          */
    /* ---------------------------------------------------------------------------------------------- */

    @Test
    public void testUpdateReturnsUpdatedListing() {
        // Arrange
        final Listing listing = buildFakeListing(buildFakeUser(SELLER_ID, "seller"));
        final Model model = buildFakeModel();
        when(listingDao.getById(eq(LISTING_ID))).thenReturn(Optional.of(listing));
        when(catalogService.getModelById(eq(MODEL_ID))).thenReturn(model);
        when(listingDao.update(
            eq(LISTING_ID), eq(LISTING_TITLE), eq(LISTING_PRICE), eq(model), eq(YEAR),
            eq(Condition.FAIR), eq(true), anyBoolean(), eq(LISTING_DESCRIPTION)
        )).thenReturn(listing);

        // Act
        final Listing result = listingService.update(buildUpdateDto(LISTING_ID, Condition.FAIR), SELLER_ID);

        // Assert
        Assert.assertEquals(LISTING_ID, (long) result.getId());
    }

    @Test(expected = NotFoundException.class)
    public void testUpdateNonExistingListingThrows() {
        // Arrange
        when(listingDao.getById(eq(NON_EXISTING_ID))).thenReturn(Optional.empty());

        // Act
        listingService.update(buildUpdateDto(NON_EXISTING_ID, Condition.GOOD), SELLER_ID);
    }

    @Test(expected = BadParameterException.class)
    public void testUpdateUnknownModelThrows() {
        // Arrange
        when(listingDao.getById(eq(LISTING_ID))).thenReturn(Optional.of(buildFakeListing(buildFakeUser(SELLER_ID, "seller"))));
        when(catalogService.getModelById(eq(MODEL_ID))).thenThrow(NotFoundException.createFor("Model with ID " + MODEL_ID));

        // Act
        listingService.update(buildUpdateDto(LISTING_ID, Condition.GOOD), SELLER_ID);
    }

    @Test(expected = BadParameterException.class)
    public void testUpdateMissingConditionThrows() {
        // Arrange
        when(listingDao.getById(eq(LISTING_ID))).thenReturn(Optional.of(buildFakeListing(buildFakeUser(SELLER_ID, "seller"))));
        when(catalogService.getModelById(eq(MODEL_ID))).thenReturn(buildFakeModel());

        // Act
        listingService.update(buildUpdateDto(LISTING_ID, null), SELLER_ID);
    }

    @Test
    public void testUpdateIgnoresRemovedImagesFromOtherListings() {
        // Arrange
        final Listing listing = buildFakeListing(buildFakeUser(SELLER_ID, "seller"));
        final Model model = buildFakeModel();
        when(listingDao.getById(eq(LISTING_ID))).thenReturn(Optional.of(listing));
        when(catalogService.getModelById(eq(MODEL_ID))).thenReturn(model);
        when(listingDao.update(
            eq(LISTING_ID), eq(LISTING_TITLE), eq(LISTING_PRICE), eq(model), eq(YEAR),
            eq(Condition.GOOD), eq(true), anyBoolean(), eq(LISTING_DESCRIPTION)
        )).thenReturn(listing);
        lenient().doThrow(new AssertionError("Foreign image must not be deleted"))
            .when(imageService).delete(eq(FOREIGN_IMAGE_ID));

        // Act
        final Listing result = listingService.update(new ListingUpdateDto(
            LISTING_ID, LISTING_TITLE, LISTING_PRICE.getAmount(), MODEL_ID, YEAR, Condition.GOOD, true, false,
            LISTING_DESCRIPTION, List.of(), List.of(FOREIGN_IMAGE_ID)
        ), SELLER_ID);

        // Assert
        Assert.assertEquals(LISTING_ID, (long) result.getId());
    }

    /* ---------------------------------------------------------------------------------------------- */
    /* cancel / updateStatus                                                                           */
    /* ---------------------------------------------------------------------------------------------- */

    @Test(expected = NotFoundException.class)
    public void testCancelNonExistingListingThrows() {
        // Arrange
        when(listingDao.getById(eq(NON_EXISTING_ID))).thenReturn(Optional.empty());

        // Act
        listingService.cancel(NON_EXISTING_ID, SELLER_ID);
    }
}
