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
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Locale;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NotificationDispatcherImpl implements NotificationDispatcher {

    private static final Logger LOGGER = LoggerFactory.getLogger(NotificationDispatcherImpl.class);

    private final SiteNotificationService siteNotificationService;
    private final EmailNotificationService emailNotificationService;
    private final MessageSource messageSource;

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
            final Locale locale = buyer.getPreferredLocale();
            final String title = messageSource.getMessage("notification.purchaseConfirmed.title", null, locale);
            final String body = messageSource.getMessage(
                    "notification.purchaseConfirmed.body",
                    new Object[]{seller.getDisplayName(), listing.getTitle()},
                    locale
            );
            sendSiteNotification(
                    buyer,
                    NotificationType.PURCHASE_CONFIRMED,
                    title,
                    body,
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
            final Locale locale = seller.getPreferredLocale();
            final String title = messageSource.getMessage("notification.newOffer.title", null, locale);
            final String body = offer.getOfferedListing()
                    .map(offered -> messageSource.getMessage(
                            "notification.newOffer.body.trade",
                            new Object[]{
                                    buyer.getDisplayName(),
                                    offered.getTitle(),
                                    formatTradeSupplement(offer.getAmount(), locale),
                                    listing.getTitle()
                            },
                            locale
                    ))
                    .orElseGet(() -> messageSource.getMessage(
                            "notification.newOffer.body",
                            new Object[]{
                                    buyer.getDisplayName(),
                                    formatAmount(offer.getAmount(), locale),
                                    listing.getTitle()
                            },
                            locale
                    ));

            sendSiteNotification(
                    seller,
                    NotificationType.NEW_OFFER,
                    title,
                    body,
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
            final Locale locale = buyer.getPreferredLocale();
            final String title = messageSource.getMessage("notification.offerRejected.title", null, locale);
            final String body = offer.getOfferedListing()
                    .map(offered -> messageSource.getMessage(
                            "notification.offerRejected.body.trade",
                            new Object[]{
                                    offered.getTitle(),
                                    formatTradeSupplement(offer.getAmount(), locale),
                                    listing.getTitle()
                            },
                            locale
                    ))
                    .orElseGet(() -> messageSource.getMessage(
                            "notification.offerRejected.body",
                            new Object[]{
                                    formatAmount(offer.getAmount(), locale),
                                    listing.getTitle()
                            },
                            locale
                    ));

            sendSiteNotification(
                    buyer,
                    NotificationType.OFFER_REJECTED,
                    title,
                    body,
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
            final Locale locale = seller.getPreferredLocale();
            final String title = messageSource.getMessage("notification.offerWithdrawn.title", null, locale);
            final String body = offer.getOfferedListing()
                    .map(offered -> messageSource.getMessage(
                            "notification.offerWithdrawn.body.trade",
                            new Object[]{
                                    buyer.getDisplayName(),
                                    offered.getTitle(),
                                    formatTradeSupplement(offer.getAmount(), locale),
                                    listing.getTitle()
                            },
                            locale
                    ))
                    .orElseGet(() -> messageSource.getMessage(
                            "notification.offerWithdrawn.body",
                            new Object[]{
                                    buyer.getDisplayName(),
                                    formatAmount(offer.getAmount(), locale),
                                    listing.getTitle()
                            },
                            locale
                    ));

            sendSiteNotification(
                    seller,
                    NotificationType.OFFER_WITHDRAWN,
                    title,
                    body,
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
            final Locale locale = buyer.getPreferredLocale();
            final String title = messageSource.getMessage("notification.offerAccepted.title", null, locale);
            final String body = offer.getOfferedListing()
                    .map(offered -> messageSource.getMessage(
                            "notification.offerAccepted.body.trade",
                            new Object[]{
                                    offered.getTitle(),
                                    formatTradeSupplement(offer.getAmount(), locale),
                                    listing.getTitle(),
                                    seller.getDisplayName()
                            },
                            locale
                    ))
                    .orElseGet(() -> messageSource.getMessage(
                            "notification.offerAccepted.body",
                            new Object[]{
                                    formatAmount(offer.getAmount(), locale),
                                    listing.getTitle(),
                                    seller.getDisplayName()
                            },
                            locale
                    ));

            sendSiteNotification(
                    buyer,
                    NotificationType.OFFER_ACCEPTED,
                    title,
                    body,
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
            final Locale locale = seller.getPreferredLocale();
            final String title = messageSource.getMessage("notification.proofOfPaymentUploaded.title", null, locale);
            final String body = messageSource.getMessage(
                    "notification.proofOfPaymentUploaded.body",
                    new Object[]{buyer.getDisplayName(), listing.getTitle()},
                    locale
            );

            sendSiteNotification(
                    seller,
                    NotificationType.PROOF_OF_PAYMENT_UPLOADED,
                    title,
                    body,
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
            final Locale locale = buyer.getPreferredLocale();
            final String title = messageSource.getMessage("notification.proofOfShippingUploaded.title", null, locale);
            final String tracking = offer.getTrackingNumber() != null ? offer.getTrackingNumber().trim() : "";
            final String body = !tracking.isEmpty()
                    ? messageSource.getMessage(
                            "notification.proofOfShippingUploaded.body.tracking",
                            new Object[]{seller.getDisplayName(), listing.getTitle(), tracking},
                            locale
                    )
                    : messageSource.getMessage(
                            "notification.proofOfShippingUploaded.body",
                            new Object[]{seller.getDisplayName(), listing.getTitle()},
                            locale
                    );

            sendSiteNotification(
                    buyer,
                    NotificationType.PROOF_OF_SHIPPING_UPLOADED,
                    title,
                    body,
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
            final String title,
            final String body,
            final String actionUrl,
            final String actionLabelKey
    ) {
        siteNotificationService.create(SiteNotificationCreationDto.builder()
                .notificationRecipient(recipient)
                .notificationType(type)
                .title(title)
                .body(body)
                .actionUrl(actionUrl)
                .actionLabelKey(actionLabelKey)
                .build());
    }

    private String formatAmount(final BigDecimal amount, final Locale locale) {
        final String currencySymbol = messageSource.getMessage("currency.symbol", null, locale);
        final String numeric = amount != null ? amount.toPlainString() : "0";
        return currencySymbol + " " + numeric;
    }

    private String formatTradeSupplement(final BigDecimal amount, final Locale locale) {
        if (amount == null || amount.signum() <= 0) {
            return "";
        }
        return messageSource.getMessage(
                "notification.shared.tradeAndAmount",
                new Object[]{formatAmount(amount, locale)},
                locale
        );
    }
}
