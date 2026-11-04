package ar.edu.itba.paw.service;

import ar.edu.itba.paw.model.*;
import ar.edu.itba.paw.service.dto.SiteNotificationCreationDto;
import ar.edu.itba.paw.service.dto.mail.ListingMailDto;
import ar.edu.itba.paw.service.dto.mail.OfferMailDto;
import ar.edu.itba.paw.service.dto.mail.UserMailDto;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.context.support.StaticMessageSource;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class NotificationDispatcherImplTest {

    private RecordingSiteNotificationService siteNotificationService;
    private RecordingEmailNotificationService emailNotificationService;
    private NotificationDispatcherImpl dispatcher;

    @Before
    public void setUp() {
        siteNotificationService = new RecordingSiteNotificationService();
        emailNotificationService = new RecordingEmailNotificationService();
        dispatcher = new NotificationDispatcherImpl(
                siteNotificationService,
                emailNotificationService,
                buildMessageSource()
        );
    }

    @Test
    public void testNotifyNewOfferCashDispatchesBothSiteAndEmailInRecipientLocale() {
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

        // Act
        dispatcher.notifyNewOffer(offer);

        // Assert — Site notification uses seller's Spanish locale and context-relative URL
        Assert.assertEquals(1, siteNotificationService.created.size());
        final SiteNotificationCreationDto siteDto = siteNotificationService.created.get(0);
        Assert.assertSame(seller, siteDto.notificationRecipient());
        Assert.assertEquals(NotificationType.NEW_OFFER, siteDto.notificationType());
        Assert.assertEquals("Nueva oferta recibida", siteDto.title());
        Assert.assertEquals("Buyer Bob ofreció US$ 250.00 por \"Sony WH-1000XM5\".", siteDto.body());
        Assert.assertEquals("/account/incoming-offers/500", siteDto.actionUrl());
        Assert.assertEquals("notification.newOffer.cta", siteDto.actionLabelKey());

        // Assert — Email notification also dispatched with detached OfferMailDto snapshot
        Assert.assertEquals(1, emailNotificationService.events.size());
        final EmailEvent emailEvent = emailNotificationService.events.get(0);
        Assert.assertEquals(NotificationType.NEW_OFFER, emailEvent.type);
        Assert.assertEquals(Long.valueOf(500L), emailEvent.offer.getId());
        Assert.assertEquals(seller.getEmail(), emailEvent.offer.getSeller().getEmail());
    }

    @Test
    public void testNotifyNewOfferTradeWithCashSupplementFormatsTradeBody() {
        // Arrange
        final User seller = buildUser(10L, "Seller Ana", Language.ENGLISH);
        final User buyer = buildUser(20L, "Buyer Bob", Language.ENGLISH);
        final Listing listing = buildListing(100L, "MacBook Pro 14", seller);
        final Listing offeredListing = buildListing(200L, "iPad Pro M2", buyer);
        final Offer offer = Offer.builder()
                .id(501L)
                .listing(listing)
                .offeredListing(offeredListing)
                .buyer(buyer)
                .amount(new BigDecimal("300.00"))
                .isFullPrice(false)
                .status(OfferStatus.PENDING)
                .createdAt(Instant.now())
                .build();

        // Act
        dispatcher.notifyNewOffer(offer);

        // Assert
        Assert.assertEquals(1, siteNotificationService.created.size());
        final SiteNotificationCreationDto siteDto = siteNotificationService.created.get(0);
        Assert.assertEquals("New offer received", siteDto.title());
        Assert.assertEquals(
                "Buyer Bob offered \"iPad Pro M2\" and US$ 300.00 for \"MacBook Pro 14\".",
                siteDto.body()
        );
        Assert.assertEquals(1, emailNotificationService.events.size());
        Assert.assertEquals(NotificationType.NEW_OFFER, emailNotificationService.events.get(0).type);
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

        // Act
        dispatcher.notifyOfferAccepted(offer);

        // Assert
        Assert.assertEquals(1, siteNotificationService.created.size());
        final SiteNotificationCreationDto siteDto = siteNotificationService.created.get(0);
        Assert.assertSame(buyer, siteDto.notificationRecipient());
        Assert.assertEquals(NotificationType.OFFER_ACCEPTED, siteDto.notificationType());
        Assert.assertEquals("/account/my-offers/502/payment", siteDto.actionUrl());
        Assert.assertEquals("notification.offerAccepted.cta", siteDto.actionLabelKey());

        Assert.assertEquals(1, emailNotificationService.events.size());
        Assert.assertEquals(NotificationType.OFFER_ACCEPTED, emailNotificationService.events.get(0).type);
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

        // Act
        dispatcher.notifyProofOfShippingUploaded(offer);

        // Assert
        Assert.assertEquals(1, siteNotificationService.created.size());
        final SiteNotificationCreationDto siteDto = siteNotificationService.created.get(0);
        Assert.assertEquals(NotificationType.PROOF_OF_SHIPPING_UPLOADED, siteDto.notificationType());
        Assert.assertEquals(
                "Seller Ana uploaded proof of shipping for \"Camera Lens\" (tracking: AR-998877).",
                siteDto.body()
        );
        Assert.assertEquals("/account/my-offers?statusGroup=PENDING_PAYMENT", siteDto.actionUrl());
        Assert.assertEquals(1, emailNotificationService.events.size());
        Assert.assertEquals(NotificationType.PROOF_OF_SHIPPING_UPLOADED, emailNotificationService.events.get(0).type);
    }

    @Test
    public void testNotifyPurchaseConfirmedDispatchesBothWhileListingSoldIsEmailOnly() {
        // Arrange
        final User seller = buildUser(10L, "Seller Ana", Language.ENGLISH);
        final User buyer = buildUser(20L, "Buyer Bob", Language.ENGLISH);
        final Listing listing = buildListing(100L, "Mechanical Keyboard", seller);

        // Act
        dispatcher.notifyListingSold(listing, buyer, "Thanks!");
        dispatcher.notifyPurchaseConfirmed(listing, buyer);

        // Assert — Only PURCHASE_CONFIRMED produces a site notification; LISTING_SOLD is email-only
        Assert.assertEquals(1, siteNotificationService.created.size());
        final SiteNotificationCreationDto siteDto = siteNotificationService.created.get(0);
        Assert.assertSame(buyer, siteDto.notificationRecipient());
        Assert.assertEquals(NotificationType.PURCHASE_CONFIRMED, siteDto.notificationType());
        Assert.assertEquals("/account/my-offers?statusGroup=RESOLVED", siteDto.actionUrl());

        // Assert — Both email notifications were sent
        Assert.assertEquals(2, emailNotificationService.events.size());
        Assert.assertEquals(NotificationType.LISTING_SOLD, emailNotificationService.events.get(0).type);
        Assert.assertEquals(NotificationType.PURCHASE_CONFIRMED, emailNotificationService.events.get(1).type);
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

        // Act
        dispatcher.notifyOfferRejected(offer);
        dispatcher.notifyOfferWithdrawn(offer);
        dispatcher.notifyProofOfPaymentUploaded(offer);

        // Assert
        Assert.assertEquals(3, siteNotificationService.created.size());
        Assert.assertEquals(NotificationType.OFFER_REJECTED, siteNotificationService.created.get(0).notificationType());
        Assert.assertEquals("/listing", siteNotificationService.created.get(0).actionUrl());

        Assert.assertEquals(NotificationType.OFFER_WITHDRAWN, siteNotificationService.created.get(1).notificationType());
        Assert.assertEquals("/listing/100", siteNotificationService.created.get(1).actionUrl());

        Assert.assertEquals(NotificationType.PROOF_OF_PAYMENT_UPLOADED, siteNotificationService.created.get(2).notificationType());
        Assert.assertEquals("/account/incoming-offers/505", siteNotificationService.created.get(2).actionUrl());

        Assert.assertEquals(3, emailNotificationService.events.size());
        Assert.assertEquals(NotificationType.OFFER_REJECTED, emailNotificationService.events.get(0).type);
        Assert.assertEquals(NotificationType.OFFER_WITHDRAWN, emailNotificationService.events.get(1).type);
        Assert.assertEquals(NotificationType.PROOF_OF_PAYMENT_UPLOADED, emailNotificationService.events.get(2).type);
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

        // Act — invoke all remaining email-only notifications
        dispatcher.notifyWelcome(user);
        dispatcher.notifyListingPublished(listing);
        dispatcher.notifyPendingTransaction(offer);
        dispatcher.notifyPasswordRecovery(user, "111222");
        dispatcher.notifyEmailVerification(user, "333444");
        dispatcher.notifyAccountSuspended(user);
        dispatcher.notifyAccountUnsuspended(user);

        // Assert — zero site notifications created, 7 emails dispatched
        Assert.assertTrue(siteNotificationService.created.isEmpty());
        Assert.assertEquals(7, emailNotificationService.events.size());
        Assert.assertEquals("111222", emailNotificationService.events.get(3).extra);
        Assert.assertEquals("333444", emailNotificationService.events.get(4).extra);
    }

    /* ---------------------------------------------------------------------------------------------- */
    /* Fixtures & Stateful Recording Stubs                                                            */
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

    private static StaticMessageSource buildMessageSource() {
        final StaticMessageSource ms = new StaticMessageSource();
        final Locale en = Locale.ENGLISH;
        final Locale es = Locale.of("es");

        ms.addMessage("currency.symbol", en, "US$");
        ms.addMessage("currency.symbol", es, "US$");
        ms.addMessage("notification.shared.tradeAndAmount", en, " and {0}");
        ms.addMessage("notification.shared.tradeAndAmount", es, " y {0}");

        ms.addMessage("notification.newOffer.title", en, "New offer received");
        ms.addMessage("notification.newOffer.body", en, "{0} offered {1} for \"{2}\".");
        ms.addMessage("notification.newOffer.body.trade", en, "{0} offered \"{1}\"{2} for \"{3}\".");

        ms.addMessage("notification.newOffer.title", es, "Nueva oferta recibida");
        ms.addMessage("notification.newOffer.body", es, "{0} ofreció {1} por \"{2}\".");
        ms.addMessage("notification.newOffer.body.trade", es, "{0} ofreció \"{1}\"{2} por \"{3}\".");

        ms.addMessage("notification.offerAccepted.title", en, "Offer accepted");
        ms.addMessage("notification.offerAccepted.body", en,
                "Your offer of {0} for \"{1}\" was accepted. Upload proof of payment or coordinate delivery with {2}.");
        ms.addMessage("notification.offerAccepted.body.trade", en,
                "Your trade offer of \"{0}\"{1} for \"{2}\" was accepted. Coordinate payment and delivery with {3}.");

        ms.addMessage("notification.offerRejected.title", en, "Offer not accepted");
        ms.addMessage("notification.offerRejected.body", en, "Your offer of {0} for \"{1}\" was not accepted.");
        ms.addMessage("notification.offerRejected.body.trade", en,
                "Your trade offer of \"{0}\"{1} for \"{2}\" was not accepted.");

        ms.addMessage("notification.offerWithdrawn.title", en, "Offer withdrawn");
        ms.addMessage("notification.offerWithdrawn.body", en, "{0} withdrew their offer of {1} for \"{2}\".");
        ms.addMessage("notification.offerWithdrawn.body.trade", en,
                "{0} withdrew their trade offer of \"{1}\"{2} for \"{3}\".");

        ms.addMessage("notification.proofOfPaymentUploaded.title", en, "Proof of payment uploaded");
        ms.addMessage("notification.proofOfPaymentUploaded.body", en,
                "{0} uploaded proof of payment for \"{1}\". Review and confirm payment to complete the sale.");

        ms.addMessage("notification.proofOfShippingUploaded.title", en, "Proof of shipping uploaded");
        ms.addMessage("notification.proofOfShippingUploaded.body", en, "{0} uploaded proof of shipping for \"{1}\".");
        ms.addMessage("notification.proofOfShippingUploaded.body.tracking", en,
                "{0} uploaded proof of shipping for \"{1}\" (tracking: {2}).");

        ms.addMessage("notification.purchaseConfirmed.title", en, "Purchase confirmed");
        ms.addMessage("notification.purchaseConfirmed.body", en, "{0} confirmed receiving payment for \"{1}\".");

        return ms;
    }

    private static final class RecordingSiteNotificationService implements SiteNotificationService {
        private final List<SiteNotificationCreationDto> created = new ArrayList<>();

        @Override
        public SiteNotification create(SiteNotificationCreationDto creationDto) {
            created.add(creationDto);
            return SiteNotification.builder()
                    .id((long) created.size())
                    .user(creationDto.notificationRecipient())
                    .type(creationDto.notificationType())
                    .title(creationDto.title())
                    .body(creationDto.body())
                    .actionUrl(creationDto.actionUrl())
                    .actionLabelKey(creationDto.actionLabelKey())
                    .createdAt(Instant.now())
                    .build();
        }

        @Override
        public java.util.Optional<SiteNotification> getById(long notificationId) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Page<SiteNotification> getRecentUnreadForUser(User recipient, int n) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Page<SiteNotification> getMatching(SiteNotificationFilter filter) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void markAsRead(long notificationId, User actor) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void markAllAsReadForUser(User recipient) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void delete(SiteNotification notification) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void delete(long notificationId, User actor) {
            throw new UnsupportedOperationException();
        }

        @Override
        public int countUnreadForUser(User user) {
            throw new UnsupportedOperationException();
        }
    }

    private record EmailEvent(NotificationType type, UserMailDto user, ListingMailDto listing, OfferMailDto offer, String extra) {}

    private static final class RecordingEmailNotificationService implements EmailNotificationService {
        private final List<EmailEvent> events = new ArrayList<>();

        @Override
        public void sendWelcomeEmail(UserMailDto user) {
            events.add(new EmailEvent(NotificationType.WELCOME, user, null, null, null));
        }

        @Override
        public void sendListingPublishedEmail(UserMailDto seller, ListingMailDto listing) {
            events.add(new EmailEvent(NotificationType.LISTING_PUBLISHED, seller, listing, null, null));
        }

        @Override
        public void sendPurchaseSellerEmail(UserMailDto seller, UserMailDto buyer, ListingMailDto listing, String message) {
            events.add(new EmailEvent(NotificationType.LISTING_SOLD, seller, listing, null, message));
        }

        @Override
        public void sendPurchaseBuyerEmail(UserMailDto buyer, UserMailDto seller, ListingMailDto listing) {
            events.add(new EmailEvent(NotificationType.PURCHASE_CONFIRMED, buyer, listing, null, null));
        }

        @Override
        public void sendNewOfferEmail(OfferMailDto offer) {
            events.add(new EmailEvent(NotificationType.NEW_OFFER, offer.getSeller(), offer.getListing(), offer, null));
        }

        @Override
        public void sendOfferRejectedEmail(OfferMailDto offer) {
            events.add(new EmailEvent(NotificationType.OFFER_REJECTED, offer.getBuyer(), offer.getListing(), offer, null));
        }

        @Override
        public void sendOfferWithdrawnEmail(OfferMailDto offer) {
            events.add(new EmailEvent(NotificationType.OFFER_WITHDRAWN, offer.getSeller(), offer.getListing(), offer, null));
        }

        @Override
        public void sendOfferPendingPaymentEmail(OfferMailDto offer) {
            events.add(new EmailEvent(NotificationType.OFFER_ACCEPTED, offer.getBuyer(), offer.getListing(), offer, null));
        }

        @Override
        public void sendPendingTransactionEmail(OfferMailDto offer) {
            events.add(new EmailEvent(NotificationType.PENDING_TRANSACTION, offer.getSeller(), offer.getListing(), offer, null));
        }

        @Override
        public void sendProofOfPaymentUploadedEmail(OfferMailDto offer) {
            events.add(new EmailEvent(NotificationType.PROOF_OF_PAYMENT_UPLOADED, offer.getSeller(), offer.getListing(), offer, null));
        }

        @Override
        public void sendProofOfShippingUploadedEmail(OfferMailDto offer) {
            events.add(new EmailEvent(NotificationType.PROOF_OF_SHIPPING_UPLOADED, offer.getBuyer(), offer.getListing(), offer, null));
        }

        @Override
        public void sendPasswordRecoveryEmail(UserMailDto user, String otpValue) {
            events.add(new EmailEvent(NotificationType.PASSWORD_RECOVERY, user, null, null, otpValue));
        }

        @Override
        public void sendVerificationEmail(UserMailDto user, String otpValue) {
            events.add(new EmailEvent(NotificationType.EMAIL_VERIFICATION, user, null, null, otpValue));
        }

        @Override
        public void sendAccountSuspendedEmail(UserMailDto user) {
            events.add(new EmailEvent(NotificationType.ACCOUNT_SUSPENDED, user, null, null, null));
        }

        @Override
        public void sendAccountUnsuspendedEmail(UserMailDto user) {
            events.add(new EmailEvent(NotificationType.ACCOUNT_UNSUSPENDED, user, null, null, null));
        }
    }
}
