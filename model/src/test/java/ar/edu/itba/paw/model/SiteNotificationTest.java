package ar.edu.itba.paw.model;

import ar.edu.itba.paw.model.notification.NotificationType;
import ar.edu.itba.paw.model.notification.SiteNotification;
import org.junit.Assert;
import org.junit.Test;

public class SiteNotificationTest {

    private Offer buildOffer(final OfferStatus status) {
        return Offer.builder()
                .id(1L)
                .status(status)
                .build();
    }

    private SiteNotification buildNotification(final NotificationType type, final Offer offer) {
        return SiteNotification.builder()
                .id(100L)
                .type(type)
                .offer(offer)
                .actionUrl("/some/action")
                .actionLabelKey("some.action.key")
                .build();
    }

    @Test
    public void testShowCallToActionWhenTypeNullReturnsTrue() {
        final SiteNotification notification = buildNotification(null, null);
        Assert.assertTrue(notification.showCallToAction());
        Assert.assertTrue(notification.isShowCallToAction());
    }

    @Test
    public void testShowCallToActionNonPaymentNotificationsReturnTrueRegardlessOfOffer() {
        for (NotificationType type : new NotificationType[]{
                NotificationType.OFFER_REJECTED,
                NotificationType.OFFER_WITHDRAWN,
                NotificationType.PURCHASE_CONFIRMED,
                NotificationType.WELCOME,
                NotificationType.LISTING_PUBLISHED,
                NotificationType.LISTING_SOLD,
                NotificationType.PASSWORD_RECOVERY,
                NotificationType.EMAIL_VERIFICATION,
                NotificationType.ACCOUNT_SUSPENDED,
                NotificationType.ACCOUNT_UNSUSPENDED
        }) {
            final SiteNotification noOffer = buildNotification(type, null);
            Assert.assertTrue("Expected true for " + type + " with null offer", noOffer.showCallToAction());

            final SiteNotification pendingOffer = buildNotification(type, buildOffer(OfferStatus.PENDING));
            Assert.assertTrue("Expected true for " + type + " with PENDING offer", pendingOffer.showCallToAction());

            final SiteNotification resolvedOffer = buildNotification(type, buildOffer(OfferStatus.ACCEPTED));
            Assert.assertTrue("Expected true for " + type + " with ACCEPTED offer", resolvedOffer.showCallToAction());
        }
    }

    @Test
    public void testShowCallToActionNewOfferRequiresPendingOffer() {
        final SiteNotification pending = buildNotification(NotificationType.NEW_OFFER, buildOffer(OfferStatus.PENDING));
        Assert.assertTrue(pending.showCallToAction());
        for (OfferStatus status : new OfferStatus[]{
                OfferStatus.PENDING_PAYMENT,
                OfferStatus.ACCEPTED,
                OfferStatus.REJECTED,
                OfferStatus.WITHDRAWN
        }) {
            final SiteNotification resolved = buildNotification(NotificationType.NEW_OFFER, buildOffer(status));
            Assert.assertFalse("Expected false for NEW_OFFER when status is " + status, resolved.showCallToAction());
        }
    }

    @Test
    public void testShowCallToActionNewOfferWithoutOfferKeepsLink() {
        final SiteNotification legacy = buildNotification(NotificationType.NEW_OFFER, null);
        Assert.assertTrue(legacy.showCallToAction());
    }

    @Test
    public void testShowCallToActionOfferAcceptedRequiresPendingPayment() {
        final SiteNotification nullOffer = buildNotification(NotificationType.OFFER_ACCEPTED, null);
        Assert.assertFalse(nullOffer.showCallToAction());

        final SiteNotification pendingPayment = buildNotification(
                NotificationType.OFFER_ACCEPTED, buildOffer(OfferStatus.PENDING_PAYMENT));
        Assert.assertTrue(pendingPayment.showCallToAction());

        for (OfferStatus status : new OfferStatus[]{
                OfferStatus.PENDING,
                OfferStatus.ACCEPTED,
                OfferStatus.REJECTED,
                OfferStatus.WITHDRAWN
        }) {
            final SiteNotification otherStatus = buildNotification(NotificationType.OFFER_ACCEPTED, buildOffer(status));
            Assert.assertFalse("Expected false for OFFER_ACCEPTED when status is " + status, otherStatus.showCallToAction());
        }
    }

    @Test
    public void testShowCallToActionProofOfPaymentUploadedRequiresPendingPayment() {
        final SiteNotification nullOffer = buildNotification(NotificationType.PROOF_OF_PAYMENT_UPLOADED, null);
        Assert.assertFalse(nullOffer.showCallToAction());

        final SiteNotification pendingPayment = buildNotification(
                NotificationType.PROOF_OF_PAYMENT_UPLOADED, buildOffer(OfferStatus.PENDING_PAYMENT));
        Assert.assertTrue(pendingPayment.showCallToAction());

        for (OfferStatus status : new OfferStatus[]{
                OfferStatus.PENDING,
                OfferStatus.ACCEPTED,
                OfferStatus.REJECTED,
                OfferStatus.WITHDRAWN
        }) {
            final SiteNotification otherStatus = buildNotification(
                    NotificationType.PROOF_OF_PAYMENT_UPLOADED, buildOffer(status));
            Assert.assertFalse("Expected false for PROOF_OF_PAYMENT_UPLOADED when status is " + status,
                    otherStatus.showCallToAction());
        }
    }

    @Test
    public void testShowCallToActionProofOfShippingUploadedRequiresPendingPayment() {
        final SiteNotification nullOffer = buildNotification(NotificationType.PROOF_OF_SHIPPING_UPLOADED, null);
        Assert.assertFalse(nullOffer.showCallToAction());

        final SiteNotification pendingPayment = buildNotification(
                NotificationType.PROOF_OF_SHIPPING_UPLOADED, buildOffer(OfferStatus.PENDING_PAYMENT));
        Assert.assertTrue(pendingPayment.showCallToAction());

        for (OfferStatus status : new OfferStatus[]{
                OfferStatus.PENDING,
                OfferStatus.ACCEPTED,
                OfferStatus.REJECTED,
                OfferStatus.WITHDRAWN
        }) {
            final SiteNotification otherStatus = buildNotification(
                    NotificationType.PROOF_OF_SHIPPING_UPLOADED, buildOffer(status));
            Assert.assertFalse("Expected false for PROOF_OF_SHIPPING_UPLOADED when status is " + status,
                    otherStatus.showCallToAction());
        }
    }

    @Test
    public void testIsShowCallToActionMatchesShowCallToAction() {
        final SiteNotification active = buildNotification(
                NotificationType.OFFER_ACCEPTED, buildOffer(OfferStatus.PENDING_PAYMENT));
        Assert.assertEquals(active.showCallToAction(), active.isShowCallToAction());

        final SiteNotification inactive = buildNotification(
                NotificationType.OFFER_ACCEPTED, buildOffer(OfferStatus.ACCEPTED));
        Assert.assertEquals(inactive.showCallToAction(), inactive.isShowCallToAction());
    }
}
