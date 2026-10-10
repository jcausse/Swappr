package ar.edu.itba.paw.service;

import ar.edu.itba.paw.model.Condition;
import ar.edu.itba.paw.model.File;
import ar.edu.itba.paw.model.Listing;
import ar.edu.itba.paw.model.ListingStatus;
import ar.edu.itba.paw.model.Offer;
import ar.edu.itba.paw.model.OfferRating;
import ar.edu.itba.paw.model.OfferStatus;
import ar.edu.itba.paw.model.Price;
import ar.edu.itba.paw.model.User;
import ar.edu.itba.paw.persistence.FileDao;
import ar.edu.itba.paw.persistence.OfferDao;
import ar.edu.itba.paw.persistence.UserDao;
import ar.edu.itba.paw.service.dto.OfferCreationDto;
import ar.edu.itba.paw.service.exception.BadParameterException;
import ar.edu.itba.paw.service.exception.ForbiddenException;
import ar.edu.itba.paw.service.exception.NotFoundException;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class OfferServiceImplTest {

    private static final long SELLER_ID = 1L;
    private static final long BUYER_ID = 2L;
    private static final long OUTSIDER_ID = 3L;

    private static final long LISTING_ID = 10L;
    private static final long OFFERED_LISTING_ID = 20L;
    private static final long OFFER_ID = 100L;
    private static final long OTHER_OFFER_ID = 101L;
    private static final long NON_EXISTING_ID = 9000L;
    private static final long FILE_ID = 50L;

    private static final BigDecimal AMOUNT = new BigDecimal("1400.00");
    private static final String MESSAGE = "Is it still available?";

    private static final String FILENAME = "receipt.png";
    private static final String ALT = "Proof";
    private static final String PNG = "image/png";
    private static final String PDF = "application/pdf";
    private static final byte[] DATA = new byte[]{ 1, 2, 3 };
    private static final String TRACKING_NUMBER = "AR123456789";

    private static final int PAGE_SIZE = 10;

    // Duplicated on purpose: if the business rule changes, these tests must be updated consciously
    private static final Duration RATING_AUTO_ASSIGN_DELAY = Duration.ofDays(14);

    @InjectMocks
    private OfferServiceImpl offerService;
    @Mock
    private OfferDao offerDao;
    @Mock
    private FileDao fileDao;
    @Mock
    private UserDao userDao;
    @Mock
    private UserService userService;
    @Mock
    private ListingService listingService;
    @Mock
    private MailingService mailingService;
    @Mock
    private RatingService ratingService;

    /* ---------------------------------------------------------------------------------------------- */
    /* Fixtures                                                                                        */
    /* ---------------------------------------------------------------------------------------------- */

    private static User buildFakeUser(final long id) {
        return User.builder()
            .id(id)
            .username("user_" + id)
            .displayName("User " + id)
            .email("user_" + id + "@example.com")
            .password("fake_password")
            .joinedAt(Instant.now())
            .build();
    }

    private static Listing buildFakeListing(final long id, final User creator, final ListingStatus status, final boolean acceptsTrade) {
        return Listing.builder()
            .id(id)
            .title("Listing " + id)
            .creator(creator)
            .price(new Price(new BigDecimal("1500.00")))
            .status(status)
            .condition(Condition.GOOD)
            .acceptsTrade(acceptsTrade)
            .build();
    }


    private static Listing buildSellerListing() {
        return buildFakeListing(LISTING_ID, buildFakeUser(SELLER_ID), ListingStatus.ACTIVE, true);
    }


    private static Offer.OfferBuilder offerBuilder(final OfferStatus status) {
        return Offer.builder()
            .id(OFFER_ID)
            .listing(buildSellerListing())
            .buyer(buildFakeUser(BUYER_ID))
            .amount(AMOUNT)
            .isFullPrice(false)
            .status(status)
            .message(MESSAGE)
            .createdAt(Instant.now());
    }

    private static Offer buildFakeOffer(final OfferStatus status) {
        return offerBuilder(status).build();
    }

    private static File buildFakeFile() {
        return File.builder().id(FILE_ID).filename(FILENAME).alt(ALT).contentType(PNG).data(DATA).build();
    }

    private static OfferCreationDto buildCreationDto(final long buyerId, final Long offeredListingId) {
        return new OfferCreationDto(LISTING_ID, buyerId, AMOUNT, false, MESSAGE, offeredListingId);
    }

    private void givenOffer(final Offer offer) {
        when(offerDao.getById(eq(OFFER_ID))).thenReturn(Optional.of(offer));
    }

    /* ---------------------------------------------------------------------------------------------- */
    /* getByListingAndBuyer                                                                            */
    /* ---------------------------------------------------------------------------------------------- */

    @Test
    public void testGetByListingAndBuyerWithoutBuyerReturnsEmpty() {
        // Act
        final Optional<Offer> maybeOffer = offerService.getByListingAndBuyer(buildSellerListing(), null);

        // Assert
        Assert.assertFalse(maybeOffer.isPresent());
    }

    @Test
    public void testGetByListingAndBuyerReturnsDaoResult() {
        // Arrange
        final Listing listing = buildSellerListing();
        final User buyer = buildFakeUser(BUYER_ID);
        when(offerDao.getByListingAndBuyer(eq(listing), eq(buyer))).thenReturn(Optional.of(buildFakeOffer(OfferStatus.PENDING)));

        // Act
        final Optional<Offer> maybeOffer = offerService.getByListingAndBuyer(listing, buyer);

        // Assert
        Assert.assertTrue(maybeOffer.isPresent());
        Assert.assertEquals(OFFER_ID, (long) maybeOffer.get().getId());
    }

    /* ---------------------------------------------------------------------------------------------- */
    /* get (filters)                                                                                   */
    /* ---------------------------------------------------------------------------------------------- */

    @Test(expected = NullPointerException.class)
    public void testGetNullDtoThrows() {
        // Act
        offerService.get(null);
    }

    /* ---------------------------------------------------------------------------------------------- */
    /* create                                                                                          */
    /* ---------------------------------------------------------------------------------------------- */

    @Test
    public void testCreateReturnsPendingOffer() {
        // Arrange
        final Listing listing = buildSellerListing();
        final User buyer = buildFakeUser(BUYER_ID);
        final Offer offer = buildFakeOffer(OfferStatus.PENDING);
        when(listingService.getById(eq(LISTING_ID))).thenReturn(listing);
        when(userService.getById(eq(BUYER_ID))).thenReturn(Optional.of(buyer));
        when(offerDao.create(
            eq(LISTING_ID), eq(buyer), eq(AMOUNT), eq(false), eq(OfferStatus.PENDING),
            eq(MESSAGE), any(Instant.class), isNull()
        )).thenReturn(offer);

        // Act
        final Offer result = offerService.create(buildCreationDto(BUYER_ID, null));

        // Assert
        Assert.assertEquals(OFFER_ID, (long) result.getId());
    }

    @Test(expected = BadParameterException.class)
    public void testCreateOnOwnListingThrows() {
        // Arrange
        when(listingService.getById(eq(LISTING_ID))).thenReturn(buildSellerListing());

        // Act
        offerService.create(buildCreationDto(SELLER_ID, null));
    }

    @Test(expected = BadParameterException.class)
    public void testCreateWithUnknownBuyerThrows() {
        // Arrange
        when(listingService.getById(eq(LISTING_ID))).thenReturn(buildSellerListing());
        when(userService.getById(eq(BUYER_ID))).thenReturn(Optional.empty());

        // Act
        offerService.create(buildCreationDto(BUYER_ID, null));
    }

    @Test(expected = BadParameterException.class)
    public void testCreateTradeOnListingThatDoesNotAcceptTradesThrows() {
        // Arrange
        when(listingService.getById(eq(LISTING_ID)))
            .thenReturn(buildFakeListing(LISTING_ID, buildFakeUser(SELLER_ID), ListingStatus.ACTIVE, false));
        when(userService.getById(eq(BUYER_ID))).thenReturn(Optional.of(buildFakeUser(BUYER_ID)));

        // Act
        offerService.create(buildCreationDto(BUYER_ID, OFFERED_LISTING_ID));
    }

    @Test(expected = BadParameterException.class)
    public void testCreateTradeWithSomeoneElsesListingThrows() {
        // Arrange
        when(listingService.getById(eq(LISTING_ID))).thenReturn(buildSellerListing());
        when(listingService.getById(eq(OFFERED_LISTING_ID)))
            .thenReturn(buildFakeListing(OFFERED_LISTING_ID, buildFakeUser(OUTSIDER_ID), ListingStatus.ACTIVE, false));
        when(userService.getById(eq(BUYER_ID))).thenReturn(Optional.of(buildFakeUser(BUYER_ID)));

        // Act
        offerService.create(buildCreationDto(BUYER_ID, OFFERED_LISTING_ID));
    }

    @Test(expected = BadParameterException.class)
    public void testCreateTradeWithInactiveListingThrows() {
        // Arrange
        final User buyer = buildFakeUser(BUYER_ID);
        when(listingService.getById(eq(LISTING_ID))).thenReturn(buildSellerListing());
        when(listingService.getById(eq(OFFERED_LISTING_ID)))
            .thenReturn(buildFakeListing(OFFERED_LISTING_ID, buyer, ListingStatus.SOLD, false));
        when(userService.getById(eq(BUYER_ID))).thenReturn(Optional.of(buyer));

        // Act
        offerService.create(buildCreationDto(BUYER_ID, OFFERED_LISTING_ID));
    }

    @Test(expected = NullPointerException.class)
    public void testCreateNullDtoThrows() {
        // Act
        offerService.create(null);
    }

    /* ---------------------------------------------------------------------------------------------- */
    /* accept                                                                                          */
    /* ---------------------------------------------------------------------------------------------- */

    @Test
    public void testAcceptReturnsOffer() {
        // Arrange
        givenOffer(buildFakeOffer(OfferStatus.PENDING));

        // Act
        final Offer result = offerService.accept(OFFER_ID, SELLER_ID);

        // Assert
        Assert.assertEquals(OFFER_ID, (long) result.getId());
    }

    @Test(expected = ForbiddenException.class)
    public void testAcceptByNonSellerThrows() {
        // Arrange
        givenOffer(buildFakeOffer(OfferStatus.PENDING));

        // Act
        offerService.accept(OFFER_ID, OUTSIDER_ID);
    }

    @Test(expected = BadParameterException.class)
    public void testAcceptNonPendingOfferThrows() {
        // Arrange
        givenOffer(buildFakeOffer(OfferStatus.REJECTED));

        // Act
        offerService.accept(OFFER_ID, SELLER_ID);
    }

    @Test(expected = NotFoundException.class)
    public void testAcceptNonExistingOfferThrows() {
        // Arrange
        when(offerDao.getById(eq(NON_EXISTING_ID))).thenReturn(Optional.empty());

        // Act
        offerService.accept(NON_EXISTING_ID, SELLER_ID);
    }

    /* ---------------------------------------------------------------------------------------------- */
    /* reject                                                                                          */
    /* ---------------------------------------------------------------------------------------------- */

    @Test(expected = ForbiddenException.class)
    public void testRejectByNonSellerThrows() {
        // Arrange
        givenOffer(buildFakeOffer(OfferStatus.PENDING));

        // Act
        offerService.reject(OFFER_ID, BUYER_ID);
    }

    @Test(expected = BadParameterException.class)
    public void testRejectAcceptedOfferThrows() {
        // Arrange
        givenOffer(buildFakeOffer(OfferStatus.ACCEPTED));

        // Act
        offerService.reject(OFFER_ID, SELLER_ID);
    }

    /* ---------------------------------------------------------------------------------------------- */
    /* withdraw                                                                                        */
    /* ---------------------------------------------------------------------------------------------- */

    @Test(expected = BadParameterException.class)
    public void testWithdrawByNonBuyerThrows() {
        // Arrange
        givenOffer(buildFakeOffer(OfferStatus.PENDING));

        // Act
        offerService.withdraw(OFFER_ID, SELLER_ID);
    }

    @Test(expected = BadParameterException.class)
    public void testWithdrawNonPendingOfferThrows() {
        // Arrange
        givenOffer(buildFakeOffer(OfferStatus.PENDING_PAYMENT));

        // Act
        offerService.withdraw(OFFER_ID, BUYER_ID);
    }

    /* ---------------------------------------------------------------------------------------------- */
    /* rejectPendingOffersForListing                                                                   */
    /* ---------------------------------------------------------------------------------------------- */

    @Test
    public void testRejectPendingOffersReturnsRejectedOffers() {
        // Arrange
        final List<Offer> rejected = List.of(
            buildFakeOffer(OfferStatus.REJECTED),
            offerBuilder(OfferStatus.REJECTED).id(OTHER_OFFER_ID).build()
        );
        when(offerDao.rejectPendingOffers(eq(LISTING_ID), isNull())).thenReturn(rejected);

        // Act
        final List<Offer> result = offerService.rejectPendingOffersForListing(LISTING_ID, null);

        // Assert
        Assert.assertEquals(rejected, result);
    }

    /* ---------------------------------------------------------------------------------------------- */
    /* uploadProofOfPayment                                                                            */
    /* ---------------------------------------------------------------------------------------------- */

    @Test
    public void testUploadProofOfPaymentAcceptsPdf() {
        // Arrange
        givenOffer(buildFakeOffer(OfferStatus.PENDING_PAYMENT));
        when(fileDao.create(any(), any(), eq(PDF), any(byte[].class))).thenReturn(buildFakeFile());

        // Act
        final Offer result = offerService.uploadProofOfPayment(OFFER_ID, BUYER_ID, "receipt.pdf", ALT, PDF, DATA);

        // Assert
        Assert.assertEquals(OFFER_ID, (long) result.getId());
    }

    @Test(expected = BadParameterException.class)
    public void testUploadProofOfPaymentByNonBuyerThrows() {
        // Arrange
        givenOffer(buildFakeOffer(OfferStatus.PENDING_PAYMENT));

        // Act
        offerService.uploadProofOfPayment(OFFER_ID, SELLER_ID, FILENAME, ALT, PNG, DATA);
    }

    @Test(expected = BadParameterException.class)
    public void testUploadProofOfPaymentWhenNotPendingPaymentThrows() {
        // Arrange
        givenOffer(buildFakeOffer(OfferStatus.PENDING));

        // Act
        offerService.uploadProofOfPayment(OFFER_ID, BUYER_ID, FILENAME, ALT, PNG, DATA);
    }

    @Test(expected = BadParameterException.class)
    public void testUploadProofOfPaymentEmptyFileThrows() {
        // Arrange
        givenOffer(buildFakeOffer(OfferStatus.PENDING_PAYMENT));

        // Act
        offerService.uploadProofOfPayment(OFFER_ID, BUYER_ID, FILENAME, ALT, PNG, new byte[0]);
    }

    @Test(expected = BadParameterException.class)
    public void testUploadProofOfPaymentInvalidContentTypeThrows() {
        // Arrange
        givenOffer(buildFakeOffer(OfferStatus.PENDING_PAYMENT));

        // Act
        offerService.uploadProofOfPayment(OFFER_ID, BUYER_ID, "notes.txt", ALT, "text/plain", DATA);
    }

    /* ---------------------------------------------------------------------------------------------- */
    /* uploadProofOfShipping                                                                           */
    /* ---------------------------------------------------------------------------------------------- */

    @Test(expected = BadParameterException.class)
    public void testUploadProofOfShippingWithoutFileOrTrackingThrows() {
        // Arrange
        givenOffer(buildFakeOffer(OfferStatus.PENDING_PAYMENT));

        // Act
        offerService.uploadProofOfShipping(OFFER_ID, SELLER_ID, null, null, null, null, "  ");
    }

    @Test(expected = BadParameterException.class)
    public void testUploadProofOfShippingByNonSellerThrows() {
        // Arrange
        givenOffer(buildFakeOffer(OfferStatus.PENDING_PAYMENT));

        // Act
        offerService.uploadProofOfShipping(OFFER_ID, BUYER_ID, null, null, null, null, TRACKING_NUMBER);
    }

    @Test(expected = BadParameterException.class)
    public void testUploadProofOfShippingInvalidContentTypeThrows() {
        // Arrange
        givenOffer(buildFakeOffer(OfferStatus.PENDING_PAYMENT));

        // Act
        offerService.uploadProofOfShipping(OFFER_ID, SELLER_ID, "notes.txt", ALT, "text/plain", DATA, TRACKING_NUMBER);
    }

    /* ---------------------------------------------------------------------------------------------- */
    /* confirmPayment                                                                                  */
    /* ---------------------------------------------------------------------------------------------- */

    @Test(expected = BadParameterException.class)
    public void testConfirmPaymentByNonSellerThrows() {
        // Arrange
        givenOffer(buildFakeOffer(OfferStatus.PENDING_PAYMENT));

        // Act
        offerService.confirmPayment(OFFER_ID, BUYER_ID);
    }

    @Test(expected = BadParameterException.class)
    public void testConfirmPaymentWhenNotPendingPaymentThrows() {
        // Arrange
        givenOffer(buildFakeOffer(OfferStatus.PENDING));

        // Act
        offerService.confirmPayment(OFFER_ID, SELLER_ID);
    }

    /* ---------------------------------------------------------------------------------------------- */
    /* proof files / pending count                                                                     */
    /* ---------------------------------------------------------------------------------------------- */

    @Test
    public void testGetProofOfPaymentFileWithoutProofReturnsEmpty() {
        // Arrange
        givenOffer(buildFakeOffer(OfferStatus.PENDING_PAYMENT));

        // Act
        final Optional<File> maybeFile = offerService.getProofOfPaymentFile(OFFER_ID, SELLER_ID);

        // Assert
        Assert.assertFalse(maybeFile.isPresent());
    }

    @Test
    public void testGetProofOfPaymentFileReturnsStoredFile() {
        // Arrange
        givenOffer(offerBuilder(OfferStatus.PENDING_PAYMENT).proofOfPayment(ar.edu.itba.paw.model.File.builder().id(FILE_ID).build()).build());
        when(fileDao.getById(eq(FILE_ID))).thenReturn(Optional.of(buildFakeFile()));

        // Act
        final Optional<File> maybeFile = offerService.getProofOfPaymentFile(OFFER_ID, SELLER_ID);

        // Assert
        Assert.assertTrue(maybeFile.isPresent());
        Assert.assertEquals(FILE_ID, (long) maybeFile.get().getId());
    }

    @Test
    public void testGetProofOfShippingFileReturnsStoredFile() {
        // Arrange
        givenOffer(offerBuilder(OfferStatus.PENDING_PAYMENT).proofOfShipping(ar.edu.itba.paw.model.File.builder().id(FILE_ID).build()).build());
        when(fileDao.getById(eq(FILE_ID))).thenReturn(Optional.of(buildFakeFile()));

        // Act
        final Optional<File> maybeFile = offerService.getProofOfShippingFile(OFFER_ID, SELLER_ID);

        // Assert
        Assert.assertTrue(maybeFile.isPresent());
    }

    @Test(expected = NotFoundException.class)
    public void testGetProofOfPaymentFileForNonExistingOfferThrows() {
        // Arrange
        when(offerDao.getById(eq(NON_EXISTING_ID))).thenReturn(Optional.empty());

        // Act
        offerService.getProofOfPaymentFile(NON_EXISTING_ID, SELLER_ID);
    }

    @Test
    public void testGetPendingOffersCount() {
        // Arrange
        final User seller = buildFakeUser(SELLER_ID);
        when(offerDao.countPendingBySeller(eq(seller))).thenReturn(3);

        // Act
        final int count = offerService.getPendingOffersCount(seller);

        // Assert
        Assert.assertEquals(3, count);
    }

    /* ---------------------------------------------------------------------------------------------- */
    /* rate                                                                                            */
    /* ---------------------------------------------------------------------------------------------- */

    @Test(expected = BadParameterException.class)
    public void testRateTwiceThrows() {
        // Arrange
        final Offer offer = buildFakeOffer(OfferStatus.ACCEPTED);
        when(offerDao.setSellerRating(eq(OFFER_ID), eq(OfferRating.POSITIVE))).thenReturn(false);

        // Act
        offerService.rate(offer, buildFakeUser(BUYER_ID), OfferRating.POSITIVE);
    }

    @Test(expected = BadParameterException.class)
    public void testRateNonAcceptedOfferThrows() {
        // Arrange
        final Offer offer = buildFakeOffer(OfferStatus.PENDING_PAYMENT);

        // Act
        offerService.rate(offer, buildFakeUser(BUYER_ID), OfferRating.POSITIVE);
    }

    @Test(expected = ForbiddenException.class)
    public void testRateByOutsiderThrows() {
        // Arrange
        final Offer offer = buildFakeOffer(OfferStatus.ACCEPTED);

        // Act
        offerService.rate(offer, buildFakeUser(OUTSIDER_ID), OfferRating.POSITIVE);
    }
}
