package ar.edu.itba.paw.service;

import ar.edu.itba.paw.model.*;
import ar.edu.itba.paw.service.dto.SiteNotificationCreationDto;
import ar.edu.itba.paw.service.dto.mail.ListingMailDto;
import ar.edu.itba.paw.service.dto.mail.OfferMailDto;
import ar.edu.itba.paw.service.dto.mail.UserMailDto;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class NotificationDispatcherImplTest {

    @Mock
    private SiteNotificationService siteNotificationService;

    @Mock
    private EmailNotificationService emailNotificationService;

    private NotificationDispatcherImpl dispatcher;

    @Before
    public void setUp() {
        dispatcher = new NotificationDispatcherImpl(
                siteNotificationService,
                emailNotificationService
        );
    }

    @Test
    public void testNotifyNewOfferCashDispatchesLanguageNeutralKeysAndEmail() {
        // Arrange — seller prefers Spanish, buyer prefers English
        final User seller = buildUser(10L, "Seller Ana", Language.SPANISH);
        final User buyer = buildUser(20L, "Buyer Bob", Language.ENGLISH);
        final Listing listing = buildListing(100L, "Sony WH-1000XM5", seller);
        final Offer offer = Offer.builder()
                .id(500L)
                .listing(listing)
                .buyer(buyer)
                .amount(new BigDecimal("250.00"))
                .isFullPrice(false)
                .status(OfferStatus.PENDING)
                .createdAt(Instant.now())
                .build();

        final ArgumentCaptor<SiteNotificationCreationDto> siteCaptor =
                ArgumentCaptor.forClass(SiteNotificationCreationDto.class);
        final ArgumentCaptor<OfferMailDto> mailCaptor = ArgumentCaptor.forClass(OfferMailDto.class);
        when(siteNotificationService.create(siteCaptor.capture())).thenReturn(new SiteNotification());
        doNothing().when(emailNotificationService).sendNewOfferEmail(mailCaptor.capture());

        // Act
        dispatcher.notifyNewOffer(offer);

        // Assert — Site notification stores message keys and language-neutral args
        Assert.assertEquals(1, siteCaptor.getAllValues().size());
        final SiteNotificationCreationDto siteDto = siteCaptor.getValue();
        Assert.assertSame(seller, siteDto.notificationRecipient());
        Assert.assertEquals(NotificationType.NEW_OFFER, siteDto.notificationType());
        Assert.assertEquals("notification.newOffer.title", siteDto.titleKey());
        Assert.assertEquals("notification.newOffer.body", siteDto.bodyKey());
        Assert.assertEquals(List.of("Buyer Bob", "250.00", "Sony WH-1000XM5"), siteDto.bodyArgs());
        Assert.assertEquals("/account/incoming-offers/500", siteDto.actionUrl());
        Assert.assertEquals("notification.newOffer.cta", siteDto.actionLabelKey());

        // Assert — Email notification also dispatched with detached OfferMailDto snapshot
        Assert.assertEquals(1, mailCaptor.getAllValues().size());
        final OfferMailDto mailDto = mailCaptor.getValue();
        Assert.assertEquals(Long.valueOf(500L), mailDto.getId());
        Assert.assertEquals(seller.getEmail(), mailDto.getSeller().getEmail());
    }

    @Test
    public void testNotifyNewOfferTradeWithAndWithoutCashUsesDistinctBodyKeys() {
        // Arrange
        final User seller = buildUser(10L, "Seller Ana", Language.ENGLISH);
        final User buyer = buildUser(20L, "Buyer Bob", Language.ENGLISH);
        final Listing listing = buildListing(100L, "MacBook Pro 14", seller);
        final Listing offeredListing = buildListing(200L, "iPad Pro M2", buyer);
        final Offer tradeWithCash = Offer.builder()
                .id(501L)
                .listing(listing)
                .offeredListing(offeredListing)
                .buyer(buyer)
                .amount(new BigDecimal("300.00"))
                .isFullPrice(false)
                .status(OfferStatus.PENDING)
                .createdAt(Instant.now())
                .build();
        final Offer pureTrade = Offer.builder()
                .id(502L)
                .listing(listing)
                .offeredListing(offeredListing)
                .buyer(buyer)
                .amount(BigDecimal.ZERO)
                .isFullPrice(false)
                .status(OfferStatus.PENDING)
                .createdAt(Instant.now())
                .build();

        final ArgumentCaptor<SiteNotificationCreationDto> siteCaptor =
                ArgumentCaptor.forClass(SiteNotificationCreationDto.class);
        final ArgumentCaptor<OfferMailDto> mailCaptor = ArgumentCaptor.forClass(OfferMailDto.class);
        when(siteNotificationService.create(siteCaptor.capture())).thenReturn(new SiteNotification());
        doNothing().when(emailNotificationService).sendNewOfferEmail(mailCaptor.capture());

        // Act
        dispatcher.notifyNewOffer(tradeWithCash);
        dispatcher.notifyNewOffer(pureTrade);

        // Assert
        Assert.assertEquals(2, siteCaptor.getAllValues().size());

        final SiteNotificationCreationDto withCashDto = siteCaptor.getAllValues().get(0);
        Assert.assertEquals("notification.newOffer.title", withCashDto.titleKey());
        Assert.assertEquals("notification.newOffer.body.tradeWithCash", withCashDto.bodyKey());
        Assert.assertEquals(
                List.of("Buyer Bob", "iPad Pro M2", "300.00", "MacBook Pro 14"),
                withCashDto.bodyArgs()
        );

        final SiteNotificationCreationDto pureTradeDto = siteCaptor.getAllValues().get(1);
        Assert.assertEquals("notification.newOffer.title", pureTradeDto.titleKey());
        Assert.assertEquals("notification.newOffer.body.trade", pureTradeDto.bodyKey());
        Assert.assertEquals(
                List.of("Buyer Bob", "iPad Pro M2", "MacBook Pro 14"),
                pureTradeDto.bodyArgs()
        );

        Assert.assertEquals(2, mailCaptor.getAllValues().size());
    }

    @Test
    public void testNotifyOfferAcceptedDispatchesSiteAndEmailToBuyer() {
        // Arrange
        final User seller = buildUser(10L, "Seller Ana", Language.ENGLISH);
        final User buyer = buildUser(20L, "Buyer Bob", Language.ENGLISH);
        final Listing listing = buildListing(100L, "Nintendo Switch OLED", seller);
        final Offer offer = Offer.builder()
                .id(502L)
                .listing(listing)
                .buyer(buyer)
                .amount(new BigDecimal("280.00"))
                .isFullPrice(true)
                .status(OfferStatus.PENDING_PAYMENT)
                .createdAt(Instant.now())
                .build();

        final ArgumentCaptor<SiteNotificationCreationDto> siteCaptor =
                ArgumentCaptor.forClass(SiteNotificationCreationDto.class);
        final ArgumentCaptor<OfferMailDto> mailCaptor = ArgumentCaptor.forClass(OfferMailDto.class);
        when(siteNotificationService.create(siteCaptor.capture())).thenReturn(new SiteNotification());
        doNothing().when(emailNotificationService).sendOfferPendingPaymentEmail(mailCaptor.capture());

        // Act
        dispatcher.notifyOfferAccepted(offer);

        // Assert
        Assert.assertEquals(1, siteCaptor.getAllValues().size());
        final SiteNotificationCreationDto siteDto = siteCaptor.getValue();
        Assert.assertSame(buyer, siteDto.notificationRecipient());
        Assert.assertEquals(NotificationType.OFFER_ACCEPTED, siteDto.notificationType());
        Assert.assertEquals("notification.offerAccepted.title", siteDto.titleKey());
        Assert.assertEquals("notification.offerAccepted.body", siteDto.bodyKey());
        Assert.assertEquals(List.of("280.00", "Nintendo Switch OLED", "Seller Ana"), siteDto.bodyArgs());
        Assert.assertEquals("/account/my-offers/502/payment", siteDto.actionUrl());
        Assert.assertEquals("notification.offerAccepted.cta", siteDto.actionLabelKey());
        Assert.assertEquals(1, mailCaptor.getAllValues().size());
    }

    @Test
    public void testNotifyProofOfShippingUploadedIncludesTrackingNumberWhenPresent() {
        // Arrange
        final User seller = buildUser(10L, "Seller Ana", Language.ENGLISH);
        final User buyer = buildUser(20L, "Buyer Bob", Language.ENGLISH);
        final Listing listing = buildListing(100L, "Camera Lens", seller);
        final Offer offer = Offer.builder()
                .id(503L)
                .listing(listing)
                .buyer(buyer)
                .amount(new BigDecimal("400.00"))
                .isFullPrice(true)
                .trackingNumber("AR-998877")
                .status(OfferStatus.PENDING_PAYMENT)
                .createdAt(Instant.now())
                .build();

        final ArgumentCaptor<SiteNotificationCreationDto> siteCaptor =
                ArgumentCaptor.forClass(SiteNotificationCreationDto.class);
        final ArgumentCaptor<OfferMailDto> mailCaptor = ArgumentCaptor.forClass(OfferMailDto.class);
        when(siteNotificationService.create(siteCaptor.capture())).thenReturn(new SiteNotification());
        doNothing().when(emailNotificationService).sendProofOfShippingUploadedEmail(mailCaptor.capture());

        // Act
        dispatcher.notifyProofOfShippingUploaded(offer);

        // Assert
        Assert.assertEquals(1, siteCaptor.getAllValues().size());
        final SiteNotificationCreationDto siteDto = siteCaptor.getValue();
        Assert.assertEquals(NotificationType.PROOF_OF_SHIPPING_UPLOADED, siteDto.notificationType());
        Assert.assertEquals("notification.proofOfShippingUploaded.title", siteDto.titleKey());
        Assert.assertEquals("notification.proofOfShippingUploaded.body.tracking", siteDto.bodyKey());
        Assert.assertEquals(List.of("Seller Ana", "Camera Lens", "AR-998877"), siteDto.bodyArgs());
        Assert.assertEquals("/account/my-offers?statusGroup=PENDING_PAYMENT", siteDto.actionUrl());
        Assert.assertEquals(1, mailCaptor.getAllValues().size());
    }

    @Test
    public void testNotifyPurchaseConfirmedDispatchesBothWhileListingSoldIsEmailOnly() {
        // Arrange
        final User seller = buildUser(10L, "Seller Ana", Language.ENGLISH);
        final User buyer = buildUser(20L, "Buyer Bob", Language.ENGLISH);
        final Listing listing = buildListing(100L, "Mechanical Keyboard", seller);

        final ArgumentCaptor<SiteNotificationCreationDto> siteCaptor =
                ArgumentCaptor.forClass(SiteNotificationCreationDto.class);
        final ArgumentCaptor<ListingMailDto> soldListingCaptor = ArgumentCaptor.forClass(ListingMailDto.class);
        final ArgumentCaptor<ListingMailDto> buyerListingCaptor = ArgumentCaptor.forClass(ListingMailDto.class);

        when(siteNotificationService.create(siteCaptor.capture())).thenReturn(new SiteNotification());
        doNothing().when(emailNotificationService)
                .sendPurchaseSellerEmail(any(), any(), soldListingCaptor.capture(), eq("Thanks!"));
        doNothing().when(emailNotificationService)
                .sendPurchaseBuyerEmail(any(), any(), buyerListingCaptor.capture());

        // Act
        dispatcher.notifyListingSold(listing, buyer, "Thanks!");
        dispatcher.notifyPurchaseConfirmed(listing, buyer);

        // Assert — Only PURCHASE_CONFIRMED produces a site notification; LISTING_SOLD is email-only
        Assert.assertEquals(1, siteCaptor.getAllValues().size());
        final SiteNotificationCreationDto siteDto = siteCaptor.getValue();
        Assert.assertSame(buyer, siteDto.notificationRecipient());
        Assert.assertEquals(NotificationType.PURCHASE_CONFIRMED, siteDto.notificationType());
        Assert.assertEquals("notification.purchaseConfirmed.title", siteDto.titleKey());
        Assert.assertEquals("notification.purchaseConfirmed.body", siteDto.bodyKey());
        Assert.assertEquals(List.of("Seller Ana", "Mechanical Keyboard"), siteDto.bodyArgs());
        Assert.assertEquals("/account/my-offers?statusGroup=RESOLVED", siteDto.actionUrl());

        // Assert — Both email notifications were sent
        Assert.assertEquals(1, soldListingCaptor.getAllValues().size());
        Assert.assertEquals(1, buyerListingCaptor.getAllValues().size());
    }

    @Test
    public void testNotifyOfferRejectedWithdrawnAndProofOfPaymentDispatchBothChannels() {
        // Arrange
        final User seller = buildUser(10L, "Seller Ana", Language.ENGLISH);
        final User buyer = buildUser(20L, "Buyer Bob", Language.ENGLISH);
        final Listing listing = buildListing(100L, "Desk Monitor", seller);
        final Offer offer = Offer.builder()
                .id(505L)
                .listing(listing)
                .buyer(buyer)
                .amount(new BigDecimal("190.00"))
                .isFullPrice(false)
                .status(OfferStatus.PENDING)
                .createdAt(Instant.now())
                .build();

        final ArgumentCaptor<SiteNotificationCreationDto> siteCaptor =
                ArgumentCaptor.forClass(SiteNotificationCreationDto.class);
        final ArgumentCaptor<OfferMailDto> rejectedCaptor = ArgumentCaptor.forClass(OfferMailDto.class);
        final ArgumentCaptor<OfferMailDto> withdrawnCaptor = ArgumentCaptor.forClass(OfferMailDto.class);
        final ArgumentCaptor<OfferMailDto> paymentCaptor = ArgumentCaptor.forClass(OfferMailDto.class);

        when(siteNotificationService.create(siteCaptor.capture())).thenReturn(new SiteNotification());
        doNothing().when(emailNotificationService).sendOfferRejectedEmail(rejectedCaptor.capture());
        doNothing().when(emailNotificationService).sendOfferWithdrawnEmail(withdrawnCaptor.capture());
        doNothing().when(emailNotificationService).sendProofOfPaymentUploadedEmail(paymentCaptor.capture());

        // Act
        dispatcher.notifyOfferRejected(offer);
        dispatcher.notifyOfferWithdrawn(offer);
        dispatcher.notifyProofOfPaymentUploaded(offer);

        // Assert
        Assert.assertEquals(3, siteCaptor.getAllValues().size());

        final SiteNotificationCreationDto rejectedDto = siteCaptor.getAllValues().get(0);
        Assert.assertEquals(NotificationType.OFFER_REJECTED, rejectedDto.notificationType());
        Assert.assertEquals("notification.offerRejected.body", rejectedDto.bodyKey());
        Assert.assertEquals(List.of("190.00", "Desk Monitor"), rejectedDto.bodyArgs());
        Assert.assertEquals("/listing", rejectedDto.actionUrl());

        final SiteNotificationCreationDto withdrawnDto = siteCaptor.getAllValues().get(1);
        Assert.assertEquals(NotificationType.OFFER_WITHDRAWN, withdrawnDto.notificationType());
        Assert.assertEquals("notification.offerWithdrawn.body", withdrawnDto.bodyKey());
        Assert.assertEquals(List.of("Buyer Bob", "190.00", "Desk Monitor"), withdrawnDto.bodyArgs());
        Assert.assertEquals("/listing/100", withdrawnDto.actionUrl());

        final SiteNotificationCreationDto paymentDto = siteCaptor.getAllValues().get(2);
        Assert.assertEquals(NotificationType.PROOF_OF_PAYMENT_UPLOADED, paymentDto.notificationType());
        Assert.assertEquals("notification.proofOfPaymentUploaded.body", paymentDto.bodyKey());
        Assert.assertEquals(List.of("Buyer Bob", "Desk Monitor"), paymentDto.bodyArgs());
        Assert.assertEquals("/account/incoming-offers/505", paymentDto.actionUrl());

        Assert.assertEquals(1, rejectedCaptor.getAllValues().size());
        Assert.assertEquals(1, withdrawnCaptor.getAllValues().size());
        Assert.assertEquals(1, paymentCaptor.getAllValues().size());
    }

    @Test
    public void testEmailOnlyNotificationTypesNeverCreateSiteNotifications() {
        // Arrange
        final User user = buildUser(10L, "User Ana", Language.ENGLISH);
        final User buyer = buildUser(20L, "Buyer Bob", Language.ENGLISH);
        final Listing listing = buildListing(100L, "Guitar", user);
        final Offer offer = Offer.builder()
                .id(504L)
                .listing(listing)
                .buyer(buyer)
                .amount(new BigDecimal("150.00"))
                .isFullPrice(true)
                .status(OfferStatus.PENDING_PAYMENT)
                .createdAt(Instant.now())
                .build();

        final ArgumentCaptor<SiteNotificationCreationDto> siteCaptor =
                ArgumentCaptor.forClass(SiteNotificationCreationDto.class);
        final ArgumentCaptor<UserMailDto> welcomeCaptor = ArgumentCaptor.forClass(UserMailDto.class);
        final ArgumentCaptor<ListingMailDto> publishedCaptor = ArgumentCaptor.forClass(ListingMailDto.class);
        final ArgumentCaptor<OfferMailDto> pendingTxCaptor = ArgumentCaptor.forClass(OfferMailDto.class);
        final ArgumentCaptor<String> recoveryOtpCaptor = ArgumentCaptor.forClass(String.class);
        final ArgumentCaptor<String> verificationOtpCaptor = ArgumentCaptor.forClass(String.class);
        final ArgumentCaptor<UserMailDto> suspendedCaptor = ArgumentCaptor.forClass(UserMailDto.class);
        final ArgumentCaptor<UserMailDto> unsuspendedCaptor = ArgumentCaptor.forClass(UserMailDto.class);

        lenient().when(siteNotificationService.create(siteCaptor.capture())).thenReturn(new SiteNotification());
        doNothing().when(emailNotificationService).sendWelcomeEmail(welcomeCaptor.capture());
        doNothing().when(emailNotificationService).sendListingPublishedEmail(any(), publishedCaptor.capture());
        doNothing().when(emailNotificationService).sendPendingTransactionEmail(pendingTxCaptor.capture());
        doNothing().when(emailNotificationService).sendPasswordRecoveryEmail(any(), recoveryOtpCaptor.capture());
        doNothing().when(emailNotificationService).sendVerificationEmail(any(), verificationOtpCaptor.capture());
        doNothing().when(emailNotificationService).sendAccountSuspendedEmail(suspendedCaptor.capture());
        doNothing().when(emailNotificationService).sendAccountUnsuspendedEmail(unsuspendedCaptor.capture());

        // Act — invoke all remaining email-only notifications
        dispatcher.notifyWelcome(user);
        dispatcher.notifyListingPublished(listing);
        dispatcher.notifyPendingTransaction(offer);
        dispatcher.notifyPasswordRecovery(user, "111222");
        dispatcher.notifyEmailVerification(user, "333444");
        dispatcher.notifyAccountSuspended(user);
        dispatcher.notifyAccountUnsuspended(user);

        // Assert — zero site notifications created, all 7 emails dispatched
        Assert.assertTrue(siteCaptor.getAllValues().isEmpty());
        Assert.assertEquals(1, welcomeCaptor.getAllValues().size());
        Assert.assertEquals(1, publishedCaptor.getAllValues().size());
        Assert.assertEquals(1, pendingTxCaptor.getAllValues().size());
        Assert.assertEquals("111222", recoveryOtpCaptor.getValue());
        Assert.assertEquals("333444", verificationOtpCaptor.getValue());
        Assert.assertEquals(1, suspendedCaptor.getAllValues().size());
        Assert.assertEquals(1, unsuspendedCaptor.getAllValues().size());
    }

    /* ---------------------------------------------------------------------------------------------- */
    /* Fixtures                                                                                       */
    /* ---------------------------------------------------------------------------------------------- */

    private static User buildUser(final long id, final String displayName, final Language preferredLanguage) {
        return User.builder()
                .id(id)
                .username("user_" + id)
                .displayName(displayName)
                .email("user_" + id + "@swappr.test")
                .password("secret")
                .preferredLanguage(preferredLanguage)
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
}
