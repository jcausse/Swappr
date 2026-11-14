package ar.edu.itba.paw.service;

import ar.edu.itba.paw.model.Listing;
import ar.edu.itba.paw.model.Offer;
import ar.edu.itba.paw.model.User;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class PermissionServiceImplTest {

    private static final long OFFER_ID = 1L;
    private static final long NON_EXISTING_OFFER_ID = 9000L;

    private static final User SELLER = User.builder().id(1L).username("seller").build();
    private static final User BUYER = User.builder().id(2L).username("buyer").build();
    private static final User OUTSIDER = User.builder().id(3L).username("outsider").build();

    @InjectMocks
    private PermissionServiceImpl permissionService;

    @Mock
    private ListingService listingService;

    @Mock
    private OfferService offerService;

    @Mock
    private SiteNotificationService siteNotificationService;

    private void stubOffer() {
        final Offer offer = Offer.builder()
            .id(OFFER_ID)
            .listing(Listing.builder().id(1L).creator(SELLER).build())
            .buyer(BUYER)
            .build();
        when(offerService.getById(eq(OFFER_ID), eq(false))).thenReturn(Optional.of(offer));
    }

    @Test
    public void testCanAccessOfferAllowsSeller() {
        stubOffer();

        Assert.assertTrue(permissionService.canAccessOffer(SELLER, OFFER_ID));
    }

    @Test
    public void testCanAccessOfferAllowsBuyer() {
        stubOffer();

        Assert.assertTrue(permissionService.canAccessOffer(BUYER, OFFER_ID));
    }

    @Test
    public void testCanAccessOfferDeniesOutsider() {
        stubOffer();

        Assert.assertFalse(permissionService.canAccessOffer(OUTSIDER, OFFER_ID));
    }

    @Test
    public void testCanAccessOfferDeniesMissingOffer() {
        when(offerService.getById(eq(NON_EXISTING_OFFER_ID), eq(false))).thenReturn(Optional.empty());

        Assert.assertFalse(permissionService.canAccessOffer(SELLER, NON_EXISTING_OFFER_ID));
    }

    @Test
    public void testCanManageOfferAsSellerDeniesBuyer() {
        stubOffer();

        Assert.assertFalse(permissionService.canManageOfferAsSeller(BUYER, OFFER_ID));
    }

    @Test
    public void testCanManageOfferAsBuyerDeniesSeller() {
        stubOffer();

        Assert.assertFalse(permissionService.canManageOfferAsBuyer(SELLER, OFFER_ID));
    }
}
