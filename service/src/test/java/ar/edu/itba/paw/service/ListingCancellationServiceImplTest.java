package ar.edu.itba.paw.service;

import ar.edu.itba.paw.model.Listing;
import ar.edu.itba.paw.model.ListingStatus;
import ar.edu.itba.paw.model.User;
import ar.edu.itba.paw.persistence.ListingDao;
import ar.edu.itba.paw.service.exception.ForbiddenException;
import ar.edu.itba.paw.service.exception.ListingNotCancelableException;
import ar.edu.itba.paw.service.exception.NotFoundException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ListingCancellationServiceImplTest {

    private static final long LISTING_ID = 1L;
    private static final long NON_EXISTING_ID = 9000L;
    private static final long SELLER_ID = 1L;
    private static final long OTHER_USER_ID = 2L;

    @InjectMocks
    private ListingCancellationServiceImpl listingCancellationService;

    @Mock
    private ListingDao listingDao;

    @Mock
    private OfferService offerService;

    private static Listing buildFakeListing() {
        return buildFakeListing(ListingStatus.ACTIVE);
    }

    private static Listing buildFakeListing(final ListingStatus status) {
        return Listing.builder()
            .id(LISTING_ID)
            .creator(User.builder().id(SELLER_ID).username("seller").build())
            .status(status)
            .build();
    }

    @Test(expected = NotFoundException.class)
    public void testCancelNonExistingListingThrows() {
        // Arrange
        when(listingDao.getById(eq(NON_EXISTING_ID))).thenReturn(Optional.empty());

        // Act
        listingCancellationService.cancel(NON_EXISTING_ID, SELLER_ID);
    }

    @Test(expected = ForbiddenException.class)
    public void testCancelByNonCreatorThrows() {
        // Arrange
        when(listingDao.getById(eq(LISTING_ID))).thenReturn(Optional.of(buildFakeListing()));

        // Act
        listingCancellationService.cancel(LISTING_ID, OTHER_USER_ID);
    }

    @Test(expected = NotFoundException.class)
    public void testCancelByAdminNonExistingListingThrows() {
        // Arrange
        when(listingDao.getById(eq(NON_EXISTING_ID))).thenReturn(Optional.empty());

        // Act
        listingCancellationService.cancelByAdmin(NON_EXISTING_ID);
    }

    @Test(expected = ListingNotCancelableException.class)
    public void testCancelSoldListingThrows() {
        // Arrange
        when(listingDao.getById(eq(LISTING_ID))).thenReturn(Optional.of(buildFakeListing(ListingStatus.SOLD)));

        // Act
        listingCancellationService.cancel(LISTING_ID, SELLER_ID);
    }

    @Test(expected = ListingNotCancelableException.class)
    public void testCancelPendingTransactionListingThrows() {
        // Arrange
        when(listingDao.getById(eq(LISTING_ID)))
            .thenReturn(Optional.of(buildFakeListing(ListingStatus.PENDING_TRANSACTION)));

        // Act
        listingCancellationService.cancel(LISTING_ID, SELLER_ID);
    }

    @Test(expected = ListingNotCancelableException.class)
    public void testCancelByAdminSoldListingThrows() {
        // Arrange
        when(listingDao.getById(eq(LISTING_ID))).thenReturn(Optional.of(buildFakeListing(ListingStatus.SOLD)));

        // Act
        listingCancellationService.cancelByAdmin(LISTING_ID);
    }

    @Test(expected = ListingNotCancelableException.class)
    public void testCancelByAdminCanceledListingThrows() {
        // Arrange
        when(listingDao.getById(eq(LISTING_ID))).thenReturn(Optional.of(buildFakeListing(ListingStatus.CANCELED)));

        // Act
        listingCancellationService.cancelByAdmin(LISTING_ID);
    }
}
