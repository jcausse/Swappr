package ar.edu.itba.paw.service.notification;

import ar.edu.itba.paw.model.*;
import ar.edu.itba.paw.model.notification.NotificationType;
import ar.edu.itba.paw.service.dto.SiteNotificationCreationDto;
import org.junit.Assert;
import org.junit.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public class SiteNotificationCreationDtoFactoryTest {

    private static final User SELLER = buildUser(10L, "Seller Ana");
    private static final User BUYER = buildUser(20L, "Buyer Bob");
    private static final Listing LISTING = buildListing(100L, "MacBook Pro 14", SELLER);
    private static final Listing OFFERED_LISTING = buildListing(200L, "iPad Pro M2", BUYER);

    @Test
    public void testPurchaseConfirmedBuildsExpectedDto() {
        final SiteNotificationCreationDto dto =
                SiteNotificationCreationDtoFactory.purchaseConfirmed(LISTING, BUYER);

        Assert.assertSame(BUYER, dto.notificationRecipient());
        Assert.assertEquals(NotificationType.PURCHASE_CONFIRMED, dto.notificationType());
        Assert.assertEquals("notification.purchaseConfirmed.title", dto.titleKey());
        Assert.assertEquals("notification.purchaseConfirmed.body", dto.bodyKey());
        Assert.assertEquals(List.of("Seller Ana", "MacBook Pro 14"), dto.bodyArgs());
        Assert.assertEquals("/account/my-offers?statusGroup=RESOLVED", dto.actionUrl());
        Assert.assertEquals("notification.purchaseConfirmed.cta", dto.actionLabelKey());
        Assert.assertNull(dto.offer());
    }

    @Test
    public void testNewOfferCashPureTradeAndTradeWithCash() {
        final SiteNotificationCreationDto cashDto =
                SiteNotificationCreationDtoFactory.newOffer(buildOffer(500L, null, new BigDecimal("250"), null));
        Assert.assertSame(SELLER, cashDto.notificationRecipient());
        Assert.assertEquals("notification.newOffer.body", cashDto.bodyKey());
        Assert.assertEquals(List.of("Buyer Bob", "250.00", "MacBook Pro 14"), cashDto.bodyArgs());
        Assert.assertEquals("/account/incoming-offers/500", cashDto.actionUrl());
        Assert.assertNotNull(cashDto.offer());
        Assert.assertEquals(Long.valueOf(500L), cashDto.offer().getId());

        final SiteNotificationCreationDto pureTradeDto =
                SiteNotificationCreationDtoFactory.newOffer(buildOffer(501L, OFFERED_LISTING, BigDecimal.ZERO, null));
        Assert.assertEquals("notification.newOffer.body.trade", pureTradeDto.bodyKey());
        Assert.assertEquals(List.of("Buyer Bob", "iPad Pro M2", "MacBook Pro 14"), pureTradeDto.bodyArgs());

        final SiteNotificationCreationDto tradeWithCashDto =
                SiteNotificationCreationDtoFactory.newOffer(buildOffer(502L, OFFERED_LISTING, new BigDecimal("75.5"), null));
        Assert.assertEquals("notification.newOffer.body.tradeWithCash", tradeWithCashDto.bodyKey());
        Assert.assertEquals(List.of("Buyer Bob", "iPad Pro M2", "75.50", "MacBook Pro 14"), tradeWithCashDto.bodyArgs());
    }

    @Test
    public void testOfferAcceptedForFreeListingUsesDeliveryKeysAndSkipsPaymentPage() {
        final Listing freeListing = Listing.builder()
                .id(101L)
                .title("Free Bookshelf")
                .creator(SELLER)
                .price(new Price(BigDecimal.ZERO))
                .condition(Condition.LIKE_NEW)
                .status(ListingStatus.ACTIVE)
                .build();
        final Offer interest = Offer.builder()
                .id(603L)
                .listing(freeListing)
                .buyer(BUYER)
                .amount(BigDecimal.ZERO)
                .isFullPrice(true)
                .status(OfferStatus.PENDING_PAYMENT)
                .createdAt(Instant.now())
                .build();

        final SiteNotificationCreationDto dto = SiteNotificationCreationDtoFactory.offerAccepted(interest);

        Assert.assertSame(BUYER, dto.notificationRecipient());
        Assert.assertEquals("notification.offerAccepted.title", dto.titleKey());
        Assert.assertEquals("notification.offerAccepted.free.body", dto.bodyKey());
        Assert.assertEquals(List.of("Free Bookshelf", "Seller Ana"), dto.bodyArgs());
        Assert.assertEquals("/account/my-offers?statusGroup=PENDING_PAYMENT", dto.actionUrl());
        Assert.assertEquals("notification.offerAccepted.free.cta", dto.actionLabelKey());
    }

    @Test
    public void testOfferAcceptedCashPureTradeAndTradeWithCash() {
        final SiteNotificationCreationDto cashDto =
                SiteNotificationCreationDtoFactory.offerAccepted(buildOffer(600L, null, new BigDecimal("280.00"), null));
        Assert.assertSame(BUYER, cashDto.notificationRecipient());
        Assert.assertEquals("notification.offerAccepted.body", cashDto.bodyKey());
        Assert.assertEquals(List.of("280.00", "MacBook Pro 14", "Seller Ana"), cashDto.bodyArgs());
        Assert.assertEquals("/account/my-offers/600/payment", cashDto.actionUrl());
        Assert.assertNotNull(cashDto.offer());
        Assert.assertEquals(Long.valueOf(600L), cashDto.offer().getId());

        final SiteNotificationCreationDto pureTradeDto =
                SiteNotificationCreationDtoFactory.offerAccepted(buildOffer(601L, OFFERED_LISTING, null, null));
        Assert.assertEquals("notification.offerAccepted.body.trade", pureTradeDto.bodyKey());
        Assert.assertEquals(List.of("iPad Pro M2", "MacBook Pro 14", "Seller Ana"), pureTradeDto.bodyArgs());

        final SiteNotificationCreationDto tradeWithCashDto =
                SiteNotificationCreationDtoFactory.offerAccepted(buildOffer(602L, OFFERED_LISTING, new BigDecimal("100.00"), null));
        Assert.assertEquals("notification.offerAccepted.body.tradeWithCash", tradeWithCashDto.bodyKey());
        Assert.assertEquals(List.of("iPad Pro M2", "100.00", "MacBook Pro 14", "Seller Ana"), tradeWithCashDto.bodyArgs());
    }

    @Test
    public void testOfferRejectedAndWithdrawnTradeBranches() {
        final SiteNotificationCreationDto rejectedTradeWithCash =
                SiteNotificationCreationDtoFactory.offerRejected(buildOffer(700L, OFFERED_LISTING, new BigDecimal("50.00"), null));
        Assert.assertSame(BUYER, rejectedTradeWithCash.notificationRecipient());
        Assert.assertEquals("notification.offerRejected.body.tradeWithCash", rejectedTradeWithCash.bodyKey());
        Assert.assertEquals(List.of("iPad Pro M2", "50.00", "MacBook Pro 14"), rejectedTradeWithCash.bodyArgs());
        Assert.assertEquals("/listing", rejectedTradeWithCash.actionUrl());
        Assert.assertNotNull(rejectedTradeWithCash.offer());
        Assert.assertEquals(Long.valueOf(700L), rejectedTradeWithCash.offer().getId());

        final SiteNotificationCreationDto withdrawnPureTrade =
                SiteNotificationCreationDtoFactory.offerWithdrawn(buildOffer(701L, OFFERED_LISTING, BigDecimal.ZERO, null));
        Assert.assertSame(SELLER, withdrawnPureTrade.notificationRecipient());
        Assert.assertEquals("notification.offerWithdrawn.body.trade", withdrawnPureTrade.bodyKey());
        Assert.assertEquals(List.of("Buyer Bob", "iPad Pro M2", "MacBook Pro 14"), withdrawnPureTrade.bodyArgs());
        Assert.assertEquals("/listing/100", withdrawnPureTrade.actionUrl());
    }

    @Test
    public void testProofOfPaymentAndProofOfShippingWithAndWithoutTracking() {
        final SiteNotificationCreationDto paymentDto =
                SiteNotificationCreationDtoFactory.proofOfPaymentUploaded(buildOffer(800L, null, new BigDecimal("300.00"), null));
        Assert.assertSame(SELLER, paymentDto.notificationRecipient());
        Assert.assertEquals("notification.proofOfPaymentUploaded.body", paymentDto.bodyKey());
        Assert.assertEquals(List.of("Buyer Bob", "MacBook Pro 14"), paymentDto.bodyArgs());
        Assert.assertEquals("/account/incoming-offers/800", paymentDto.actionUrl());
        Assert.assertNotNull(paymentDto.offer());
        Assert.assertEquals(Long.valueOf(800L), paymentDto.offer().getId());

        final SiteNotificationCreationDto shippingNoTrackingDto =
                SiteNotificationCreationDtoFactory.proofOfShippingUploaded(buildOffer(801L, null, new BigDecimal("300.00"), "   "));
        Assert.assertSame(BUYER, shippingNoTrackingDto.notificationRecipient());
        Assert.assertEquals("notification.proofOfShippingUploaded.body", shippingNoTrackingDto.bodyKey());
        Assert.assertEquals(List.of("Seller Ana", "MacBook Pro 14"), shippingNoTrackingDto.bodyArgs());

        final SiteNotificationCreationDto shippingWithTrackingDto =
                SiteNotificationCreationDtoFactory.proofOfShippingUploaded(buildOffer(802L, null, new BigDecimal("300.00"), " AR-12345 "));
        Assert.assertEquals("notification.proofOfShippingUploaded.body.tracking", shippingWithTrackingDto.bodyKey());
        Assert.assertEquals(List.of("Seller Ana", "MacBook Pro 14", "AR-12345"), shippingWithTrackingDto.bodyArgs());
        Assert.assertEquals("/account/my-offers?statusGroup=PENDING_PAYMENT", shippingWithTrackingDto.actionUrl());
    }

    @Test
    public void testBrandModelRequestAcceptedBuildsExpectedDto() {
        final Brand brand = Brand.builder().id(30L).name("Acme").build();
        final Model model = Model.builder().id(40L).name("Phone").brand(brand).build();
        final BrandModelRequest request = BrandModelRequest.builder()
                .id(50L)
                .type(BrandModelRequestType.NEW_BRAND)
                .brandName("Acme")
                .modelName("Phone")
                .build();

        final SiteNotificationCreationDto dto =
                SiteNotificationCreationDtoFactory.brandModelRequestAccepted(request, BUYER, brand, model);

        Assert.assertSame(BUYER, dto.notificationRecipient());
        Assert.assertEquals(NotificationType.BRAND_MODEL_REQUEST_ACCEPTED, dto.notificationType());
        Assert.assertEquals("notification.brandModelRequest.accepted.title", dto.titleKey());
        Assert.assertEquals("notification.brandModelRequest.accepted.body", dto.bodyKey());
        Assert.assertEquals(List.of("Acme", "Phone"), dto.bodyArgs());
        Assert.assertEquals("/listing/new/choose-product?modelId=40", dto.actionUrl());
        Assert.assertEquals("notification.brandModelRequest.accepted.cta", dto.actionLabelKey());
        Assert.assertNull(dto.offer());
    }

    @Test
    public void testBrandModelRequestRejectedBuildsExpectedDto() {
        final BrandModelRequest request = BrandModelRequest.builder()
                .id(51L)
                .type(BrandModelRequestType.NEW_BRAND)
                .brandName("Acme")
                .modelName("Phone")
                .build();

        final SiteNotificationCreationDto dto =
                SiteNotificationCreationDtoFactory.brandModelRequestRejected(request, BUYER);

        Assert.assertSame(BUYER, dto.notificationRecipient());
        Assert.assertEquals(NotificationType.BRAND_MODEL_REQUEST_REJECTED, dto.notificationType());
        Assert.assertEquals("notification.brandModelRequest.rejected.title", dto.titleKey());
        Assert.assertEquals("notification.brandModelRequest.rejected.body", dto.bodyKey());
        Assert.assertEquals(List.of("Acme", "Phone"), dto.bodyArgs());
        Assert.assertEquals("/listing", dto.actionUrl());
        Assert.assertEquals("notification.brandModelRequest.rejected.cta", dto.actionLabelKey());
        Assert.assertNull(dto.offer());
    }

    private static User buildUser(final long id, final String displayName) {
        return User.builder()
                .id(id)
                .username("user_" + id)
                .displayName(displayName)
                .email("user_" + id + "@swappr.test")
                .password("secret")
                .joinedAt(Instant.now())
                .build();
    }

    private static Listing buildListing(final long id, final String title, final User creator) {
        return Listing.builder()
                .id(id)
                .title(title)
                .creator(creator)
                .price(new Price(new BigDecimal("500.00")))
                .condition(Condition.LIKE_NEW)
                .status(ListingStatus.ACTIVE)
                .build();
    }

    private static Offer buildOffer(
            final long id,
            final Listing offeredListing,
            final BigDecimal amount,
            final String trackingNumber
    ) {
        return Offer.builder()
                .id(id)
                .listing(LISTING)
                .offeredListing(offeredListing)
                .buyer(BUYER)
                .amount(amount)
                .trackingNumber(trackingNumber)
                .isFullPrice(false)
                .status(OfferStatus.PENDING)
                .createdAt(Instant.now())
                .build();
    }
}
