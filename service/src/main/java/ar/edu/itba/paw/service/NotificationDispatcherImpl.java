package ar.edu.itba.paw.service;

import ar.edu.itba.paw.model.Listing;
import ar.edu.itba.paw.model.NotificationType;
import ar.edu.itba.paw.model.Offer;
import ar.edu.itba.paw.model.User;
import ar.edu.itba.paw.service.dto.SiteNotificationCreationDto;
import ar.edu.itba.paw.service.dto.mail.ListingMailDto;
import ar.edu.itba.paw.service.dto.mail.OfferMailDto;
import ar.edu.itba.paw.service.dto.mail.UserMailDto;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NotificationDispatcherImpl implements NotificationDispatcher {

    private static final Logger LOGGER = LoggerFactory.getLogger(NotificationDispatcherImpl.class);

    private final SiteNotificationService siteNotificationService;
    private final EmailNotificationService emailNotificationService;

    @Override
    public void notifyWelcome(@NonNull User user) {
        LOGGER.debug("Dispatching {} notification for user id={}", NotificationType.WELCOME, user.getId());
        if (shouldSendEmail(user, NotificationType.WELCOME)) {
            emailNotificationService.sendWelcomeEmail(UserMailDto.from(user));
        }
    }

    @Override
    public void notifyListingPublished(@NonNull Listing listing) {
        final User seller = listing.getCreator();
        LOGGER.debug("Dispatching {} notification for listing id={}, seller id={}",
                NotificationType.LISTING_PUBLISHED, listing.getId(), seller.getId());
        if (shouldSendEmail(seller, NotificationType.LISTING_PUBLISHED)) {
            emailNotificationService.sendListingPublishedEmail(UserMailDto.from(seller), ListingMailDto.from(listing));
        }
    }

    @Override
    public void notifyListingSold(@NonNull Listing listing, @NonNull User buyer, String message) {
        final User seller = listing.getCreator();
        LOGGER.debug("Dispatching {} notification for listing id={}, seller id={}, buyer id={}",
                NotificationType.LISTING_SOLD, listing.getId(), seller.getId(), buyer.getId());
        if (shouldSendEmail(seller, NotificationType.LISTING_SOLD)) {
            emailNotificationService.sendPurchaseSellerEmail(
                    UserMailDto.from(seller),
                    UserMailDto.from(buyer),
                    ListingMailDto.from(listing),
                    message
            );
        }
    }

    @Override
    @Transactional
    public void notifyPurchaseConfirmed(@NonNull Listing listing, @NonNull User buyer) {
        final User seller = listing.getCreator();
        LOGGER.debug("Dispatching {} notification for listing id={}, buyer id={}",
                NotificationType.PURCHASE_CONFIRMED, listing.getId(), buyer.getId());

        if (shouldSendSite(buyer, NotificationType.PURCHASE_CONFIRMED)) {
            sendSiteNotification(
                    buyer,
                    NotificationType.PURCHASE_CONFIRMED,
                    "notification.purchaseConfirmed.title",
                    "notification.purchaseConfirmed.body",
                    List.of(seller.getDisplayName(), listing.getTitle()),
                    "/account/my-offers?statusGroup=RESOLVED",
                    "notification.purchaseConfirmed.cta"
            );
        }

        if (shouldSendEmail(buyer, NotificationType.PURCHASE_CONFIRMED)) {
            emailNotificationService.sendPurchaseBuyerEmail(
                    UserMailDto.from(buyer),
                    UserMailDto.from(seller),
                    ListingMailDto.from(listing)
            );
        }
    }

    @Override
    @Transactional
    public void notifyNewOffer(@NonNull Offer offer) {
        final Listing listing = offer.getListing();
        final User seller = listing.getCreator();
        final User buyer = offer.getBuyer();
        LOGGER.debug("Dispatching {} notification for offer id={}, seller id={}",
                NotificationType.NEW_OFFER, offer.getId(), seller.getId());

        if (shouldSendSite(seller, NotificationType.NEW_OFFER)) {
            final String bodyKey;
            final List<String> bodyArgs;
            if (offer.getOfferedListing().isPresent()) {
                final Listing offered = offer.getOfferedListing().get();
                if (hasCashSupplement(offer.getAmount())) {
                    bodyKey = "notification.newOffer.body.tradeWithCash";
                    bodyArgs = List.of(
                            buyer.getDisplayName(),
                            offered.getTitle(),
                            formatAmount(offer.getAmount()),
                            listing.getTitle()
                    );
                } else {
                    bodyKey = "notification.newOffer.body.trade";
                    bodyArgs = List.of(
                            buyer.getDisplayName(),
                            offered.getTitle(),
                            listing.getTitle()
                    );
                }
            } else {
                bodyKey = "notification.newOffer.body";
                bodyArgs = List.of(
                        buyer.getDisplayName(),
                        formatAmount(offer.getAmount()),
                        listing.getTitle()
                );
            }

            sendSiteNotification(
                    seller,
                    NotificationType.NEW_OFFER,
                    "notification.newOffer.title",
                    bodyKey,
                    bodyArgs,
                    "/account/incoming-offers/" + offer.getId(),
                    "notification.newOffer.cta"
            );
        }

        if (shouldSendEmail(seller, NotificationType.NEW_OFFER)) {
            emailNotificationService.sendNewOfferEmail(OfferMailDto.from(offer));
        }
    }

    @Override
    @Transactional
    public void notifyOfferRejected(@NonNull Offer offer) {
        final Listing listing = offer.getListing();
        final User buyer = offer.getBuyer();
        LOGGER.debug("Dispatching {} notification for offer id={}, buyer id={}",
                NotificationType.OFFER_REJECTED, offer.getId(), buyer.getId());

        if (shouldSendSite(buyer, NotificationType.OFFER_REJECTED)) {
            final String bodyKey;
            final List<String> bodyArgs;
            if (offer.getOfferedListing().isPresent()) {
                final Listing offered = offer.getOfferedListing().get();
                if (hasCashSupplement(offer.getAmount())) {
                    bodyKey = "notification.offerRejected.body.tradeWithCash";
                    bodyArgs = List.of(
                            offered.getTitle(),
                            formatAmount(offer.getAmount()),
                            listing.getTitle()
                    );
                } else {
                    bodyKey = "notification.offerRejected.body.trade";
                    bodyArgs = List.of(
                            offered.getTitle(),
                            listing.getTitle()
                    );
                }
            } else {
                bodyKey = "notification.offerRejected.body";
                bodyArgs = List.of(
                        formatAmount(offer.getAmount()),
                        listing.getTitle()
                );
            }

            sendSiteNotification(
                    buyer,
                    NotificationType.OFFER_REJECTED,
                    "notification.offerRejected.title",
                    bodyKey,
                    bodyArgs,
                    "/listing",
                    "notification.offerRejected.cta"
            );
        }

        if (shouldSendEmail(buyer, NotificationType.OFFER_REJECTED)) {
            emailNotificationService.sendOfferRejectedEmail(OfferMailDto.from(offer));
        }
    }

    @Override
    @Transactional
    public void notifyOfferWithdrawn(@NonNull Offer offer) {
        final Listing listing = offer.getListing();
        final User seller = listing.getCreator();
        final User buyer = offer.getBuyer();
        LOGGER.debug("Dispatching {} notification for offer id={}, seller id={}",
                NotificationType.OFFER_WITHDRAWN, offer.getId(), seller.getId());

        if (shouldSendSite(seller, NotificationType.OFFER_WITHDRAWN)) {
            final String bodyKey;
            final List<String> bodyArgs;
            if (offer.getOfferedListing().isPresent()) {
                final Listing offered = offer.getOfferedListing().get();
                if (hasCashSupplement(offer.getAmount())) {
                    bodyKey = "notification.offerWithdrawn.body.tradeWithCash";
                    bodyArgs = List.of(
                            buyer.getDisplayName(),
                            offered.getTitle(),
                            formatAmount(offer.getAmount()),
                            listing.getTitle()
                    );
                } else {
                    bodyKey = "notification.offerWithdrawn.body.trade";
                    bodyArgs = List.of(
                            buyer.getDisplayName(),
                            offered.getTitle(),
                            listing.getTitle()
                    );
                }
            } else {
                bodyKey = "notification.offerWithdrawn.body";
                bodyArgs = List.of(
                        buyer.getDisplayName(),
                        formatAmount(offer.getAmount()),
                        listing.getTitle()
                );
            }

            sendSiteNotification(
                    seller,
                    NotificationType.OFFER_WITHDRAWN,
                    "notification.offerWithdrawn.title",
                    bodyKey,
                    bodyArgs,
                    "/listing/" + listing.getId(),
                    "notification.offerWithdrawn.cta"
            );
        }

        if (shouldSendEmail(seller, NotificationType.OFFER_WITHDRAWN)) {
            emailNotificationService.sendOfferWithdrawnEmail(OfferMailDto.from(offer));
        }
    }

    @Override
    @Transactional
    public void notifyOfferAccepted(@NonNull Offer offer) {
        final Listing listing = offer.getListing();
        final User buyer = offer.getBuyer();
        final User seller = listing.getCreator();
        LOGGER.debug("Dispatching {} notification for offer id={}, buyer id={}",
                NotificationType.OFFER_ACCEPTED, offer.getId(), buyer.getId());

        if (shouldSendSite(buyer, NotificationType.OFFER_ACCEPTED)) {
            final String bodyKey;
            final List<String> bodyArgs;
            if (offer.getOfferedListing().isPresent()) {
                final Listing offered = offer.getOfferedListing().get();
                if (hasCashSupplement(offer.getAmount())) {
                    bodyKey = "notification.offerAccepted.body.tradeWithCash";
                    bodyArgs = List.of(
                            offered.getTitle(),
                            formatAmount(offer.getAmount()),
                            listing.getTitle(),
                            seller.getDisplayName()
                    );
                } else {
                    bodyKey = "notification.offerAccepted.body.trade";
                    bodyArgs = List.of(
                            offered.getTitle(),
                            listing.getTitle(),
                            seller.getDisplayName()
                    );
                }
            } else {
                bodyKey = "notification.offerAccepted.body";
                bodyArgs = List.of(
                        formatAmount(offer.getAmount()),
                        listing.getTitle(),
                        seller.getDisplayName()
                );
            }

            sendSiteNotification(
                    buyer,
                    NotificationType.OFFER_ACCEPTED,
                    "notification.offerAccepted.title",
                    bodyKey,
                    bodyArgs,
                    "/account/my-offers/" + offer.getId() + "/payment",
                    "notification.offerAccepted.cta"
            );
        }

        if (shouldSendEmail(buyer, NotificationType.OFFER_ACCEPTED)) {
            emailNotificationService.sendOfferPendingPaymentEmail(OfferMailDto.from(offer));
        }
    }

    @Override
    public void notifyPendingTransaction(@NonNull Offer offer) {
        final User seller = offer.getListing().getCreator();
        LOGGER.debug("Dispatching {} notification for offer id={}, seller id={}",
                NotificationType.PENDING_TRANSACTION, offer.getId(), seller.getId());
        if (shouldSendEmail(seller, NotificationType.PENDING_TRANSACTION)) {
            emailNotificationService.sendPendingTransactionEmail(OfferMailDto.from(offer));
        }
    }

    @Override
    @Transactional
    public void notifyProofOfPaymentUploaded(@NonNull Offer offer) {
        final Listing listing = offer.getListing();
        final User seller = listing.getCreator();
        final User buyer = offer.getBuyer();
        LOGGER.debug("Dispatching {} notification for offer id={}, seller id={}",
                NotificationType.PROOF_OF_PAYMENT_UPLOADED, offer.getId(), seller.getId());

        if (shouldSendSite(seller, NotificationType.PROOF_OF_PAYMENT_UPLOADED)) {
            sendSiteNotification(
                    seller,
                    NotificationType.PROOF_OF_PAYMENT_UPLOADED,
                    "notification.proofOfPaymentUploaded.title",
                    "notification.proofOfPaymentUploaded.body",
                    List.of(buyer.getDisplayName(), listing.getTitle()),
                    "/account/incoming-offers/" + offer.getId(),
                    "notification.proofOfPaymentUploaded.cta"
            );
        }

        if (shouldSendEmail(seller, NotificationType.PROOF_OF_PAYMENT_UPLOADED)) {
            emailNotificationService.sendProofOfPaymentUploadedEmail(OfferMailDto.from(offer));
        }
    }

    @Override
    @Transactional
    public void notifyProofOfShippingUploaded(@NonNull Offer offer) {
        final Listing listing = offer.getListing();
        final User buyer = offer.getBuyer();
        final User seller = listing.getCreator();
        LOGGER.debug("Dispatching {} notification for offer id={}, buyer id={}",
                NotificationType.PROOF_OF_SHIPPING_UPLOADED, offer.getId(), buyer.getId());

        if (shouldSendSite(buyer, NotificationType.PROOF_OF_SHIPPING_UPLOADED)) {
            final String tracking = offer.getTrackingNumber() != null ? offer.getTrackingNumber().trim() : "";
            final String bodyKey = !tracking.isEmpty()
                    ? "notification.proofOfShippingUploaded.body.tracking"
                    : "notification.proofOfShippingUploaded.body";
            final List<String> bodyArgs = !tracking.isEmpty()
                    ? List.of(seller.getDisplayName(), listing.getTitle(), tracking)
                    : List.of(seller.getDisplayName(), listing.getTitle());

            sendSiteNotification(
                    buyer,
                    NotificationType.PROOF_OF_SHIPPING_UPLOADED,
                    "notification.proofOfShippingUploaded.title",
                    bodyKey,
                    bodyArgs,
                    "/account/my-offers?statusGroup=PENDING_PAYMENT",
                    "notification.proofOfShippingUploaded.cta"
            );
        }

        if (shouldSendEmail(buyer, NotificationType.PROOF_OF_SHIPPING_UPLOADED)) {
            emailNotificationService.sendProofOfShippingUploadedEmail(OfferMailDto.from(offer));
        }
    }

    @Override
    public void notifyPasswordRecovery(@NonNull User user, @NonNull String otpValue) {
        LOGGER.debug("Dispatching {} notification for user id={}", NotificationType.PASSWORD_RECOVERY, user.getId());
        if (shouldSendEmail(user, NotificationType.PASSWORD_RECOVERY)) {
            emailNotificationService.sendPasswordRecoveryEmail(UserMailDto.from(user), otpValue);
        }
    }

    @Override
    public void notifyEmailVerification(@NonNull User user, @NonNull String otpValue) {
        LOGGER.debug("Dispatching {} notification for user id={}", NotificationType.EMAIL_VERIFICATION, user.getId());
        if (shouldSendEmail(user, NotificationType.EMAIL_VERIFICATION)) {
            emailNotificationService.sendVerificationEmail(UserMailDto.from(user), otpValue);
        }
    }

    @Override
    public void notifyAccountSuspended(@NonNull User user) {
        LOGGER.debug("Dispatching {} notification for user id={}", NotificationType.ACCOUNT_SUSPENDED, user.getId());
        if (shouldSendEmail(user, NotificationType.ACCOUNT_SUSPENDED)) {
            emailNotificationService.sendAccountSuspendedEmail(UserMailDto.from(user));
        }
    }

    @Override
    public void notifyAccountUnsuspended(@NonNull User user) {
        LOGGER.debug("Dispatching {} notification for user id={}", NotificationType.ACCOUNT_UNSUSPENDED, user.getId());
        if (shouldSendEmail(user, NotificationType.ACCOUNT_UNSUSPENDED)) {
            emailNotificationService.sendAccountUnsuspendedEmail(UserMailDto.from(user));
        }
    }

    private boolean shouldSendSite(final User recipient, final NotificationType type) {
        return recipient != null && type.supportsSite();
    }

    private boolean shouldSendEmail(final User recipient, final NotificationType type) {
        return recipient != null && type.supportsEmail();
    }

    private void sendSiteNotification(
            final User recipient,
            final NotificationType type,
            final String titleKey,
            final String bodyKey,
            final List<String> bodyArgs,
            final String actionUrl,
            final String actionLabelKey
    ) {
        siteNotificationService.create(SiteNotificationCreationDto.builder()
                .notificationRecipient(recipient)
                .notificationType(type)
                .titleKey(titleKey)
                .bodyKey(bodyKey)
                .bodyArgs(bodyArgs)
                .actionUrl(actionUrl)
                .actionLabelKey(actionLabelKey)
                .build());
    }

    private static boolean hasCashSupplement(final BigDecimal amount) {
        return amount != null && amount.signum() > 0;
    }

    private static String formatAmount(final BigDecimal amount) {
        return amount != null ? amount.setScale(2, RoundingMode.HALF_UP).toPlainString() : "0.00";
    }
}
