package ar.edu.itba.paw.service.notification;

import ar.edu.itba.paw.model.Listing;
import ar.edu.itba.paw.model.notification.NotificationChannel;
import ar.edu.itba.paw.model.notification.NotificationType;
import ar.edu.itba.paw.model.Offer;
import ar.edu.itba.paw.model.User;
import ar.edu.itba.paw.model.Brand;
import ar.edu.itba.paw.model.BrandModelRequest;
import ar.edu.itba.paw.model.Model;
import ar.edu.itba.paw.service.EmailNotificationService;
import ar.edu.itba.paw.service.NotificationDispatcherService;
import ar.edu.itba.paw.service.NotificationPreferenceService;
import ar.edu.itba.paw.service.SiteNotificationService;
import ar.edu.itba.paw.service.dto.mail.ListingMailDto;
import ar.edu.itba.paw.service.dto.mail.OfferMailDto;
import ar.edu.itba.paw.service.dto.mail.UserMailDto;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NotificationDispatcherServiceImpl implements NotificationDispatcherService {

    private static final Logger LOGGER = LoggerFactory.getLogger(NotificationDispatcherServiceImpl.class);

    private final SiteNotificationService siteNotificationService;
    private final EmailNotificationService emailNotificationService;
    private final NotificationPreferenceService notificationPreferenceService;

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
                    message,
                    listing.isFree()
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
            siteNotificationService.create(SiteNotificationCreationDtoFactory.purchaseConfirmed(listing, buyer));
        }

        if (shouldSendEmail(buyer, NotificationType.PURCHASE_CONFIRMED)) {
            emailNotificationService.sendPurchaseBuyerEmail(
                    UserMailDto.from(buyer),
                    UserMailDto.from(seller),
                    ListingMailDto.from(listing),
                    listing.isFree()
            );
        }
    }

    @Override
    @Transactional
    public void notifyNewOffer(@NonNull Offer offer) {
        final User seller = offer.getListing().getCreator();
        LOGGER.debug("Dispatching {} notification for offer id={}, seller id={}",
                NotificationType.NEW_OFFER, offer.getId(), seller.getId());

        if (shouldSendSite(seller, NotificationType.NEW_OFFER)) {
            siteNotificationService.create(SiteNotificationCreationDtoFactory.newOffer(offer));
        }

        if (shouldSendEmail(seller, NotificationType.NEW_OFFER)) {
            emailNotificationService.sendNewOfferEmail(OfferMailDto.from(offer));
        }
    }

    @Override
    @Transactional
    public void notifyOfferRejected(@NonNull Offer offer) {
        final User buyer = offer.getBuyer();
        LOGGER.debug("Dispatching {} notification for offer id={}, buyer id={}",
                NotificationType.OFFER_REJECTED, offer.getId(), buyer.getId());

        if (shouldSendSite(buyer, NotificationType.OFFER_REJECTED)) {
            siteNotificationService.create(SiteNotificationCreationDtoFactory.offerRejected(offer));
        }

        if (shouldSendEmail(buyer, NotificationType.OFFER_REJECTED)) {
            emailNotificationService.sendOfferRejectedEmail(OfferMailDto.from(offer));
        }
    }

    @Override
    @Transactional
    public void notifyOfferWithdrawn(@NonNull Offer offer) {
        final User seller = offer.getListing().getCreator();
        LOGGER.debug("Dispatching {} notification for offer id={}, seller id={}",
                NotificationType.OFFER_WITHDRAWN, offer.getId(), seller.getId());

        if (shouldSendSite(seller, NotificationType.OFFER_WITHDRAWN)) {
            siteNotificationService.create(SiteNotificationCreationDtoFactory.offerWithdrawn(offer));
        }

        if (shouldSendEmail(seller, NotificationType.OFFER_WITHDRAWN)) {
            emailNotificationService.sendOfferWithdrawnEmail(OfferMailDto.from(offer));
        }
    }

    @Override
    @Transactional
    public void notifyOfferAccepted(@NonNull Offer offer) {
        final User buyer = offer.getBuyer();
        LOGGER.debug("Dispatching {} notification for offer id={}, buyer id={}",
                NotificationType.OFFER_ACCEPTED, offer.getId(), buyer.getId());

        if (shouldSendSite(buyer, NotificationType.OFFER_ACCEPTED)) {
            siteNotificationService.create(SiteNotificationCreationDtoFactory.offerAccepted(offer));
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
        final User seller = offer.getListing().getCreator();
        LOGGER.debug("Dispatching {} notification for offer id={}, seller id={}",
                NotificationType.PROOF_OF_PAYMENT_UPLOADED, offer.getId(), seller.getId());

        if (shouldSendSite(seller, NotificationType.PROOF_OF_PAYMENT_UPLOADED)) {
            siteNotificationService.create(SiteNotificationCreationDtoFactory.proofOfPaymentUploaded(offer));
        }

        if (shouldSendEmail(seller, NotificationType.PROOF_OF_PAYMENT_UPLOADED)) {
            emailNotificationService.sendProofOfPaymentUploadedEmail(OfferMailDto.from(offer));
        }
    }

    @Override
    @Transactional
    public void notifyProofOfShippingUploaded(@NonNull Offer offer) {
        final User buyer = offer.getBuyer();
        LOGGER.debug("Dispatching {} notification for offer id={}, buyer id={}",
                NotificationType.PROOF_OF_SHIPPING_UPLOADED, offer.getId(), buyer.getId());

        if (shouldSendSite(buyer, NotificationType.PROOF_OF_SHIPPING_UPLOADED)) {
            siteNotificationService.create(SiteNotificationCreationDtoFactory.proofOfShippingUploaded(offer));
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

    @Override
    @Transactional
    public void notifyBrandModelRequestAccepted(
            @NonNull BrandModelRequest request,
            @NonNull List<User> recipients,
            @NonNull Brand brand,
            @NonNull Model model
    ) {
        for (final User recipient : recipients) {
            LOGGER.debug("Dispatching {} notification for request id={}, recipient id={}",
                    NotificationType.BRAND_MODEL_REQUEST_ACCEPTED, request.getId(), recipient.getId());

            if (shouldSendSite(recipient, NotificationType.BRAND_MODEL_REQUEST_ACCEPTED)) {
                siteNotificationService.create(
                        SiteNotificationCreationDtoFactory.brandModelRequestAccepted(request, recipient, brand, model));
            }

            if (shouldSendEmail(recipient, NotificationType.BRAND_MODEL_REQUEST_ACCEPTED)) {
                emailNotificationService.sendBrandModelRequestAcceptedEmail(
                        UserMailDto.from(recipient),
                        brand.getName(),
                        model.getName(),
                        model.getId()
                );
            }
        }
    }

    @Override
    @Transactional
    public void notifyBrandModelRequestRejected(
            @NonNull BrandModelRequest request,
            @NonNull List<User> recipients
    ) {
        for (final User recipient : recipients) {
            LOGGER.debug("Dispatching {} notification for request id={}, recipient id={}",
                    NotificationType.BRAND_MODEL_REQUEST_REJECTED, request.getId(), recipient.getId());

            if (shouldSendSite(recipient, NotificationType.BRAND_MODEL_REQUEST_REJECTED)) {
                siteNotificationService.create(
                        SiteNotificationCreationDtoFactory.brandModelRequestRejected(request, recipient));
            }

            if (shouldSendEmail(recipient, NotificationType.BRAND_MODEL_REQUEST_REJECTED)) {
                emailNotificationService.sendBrandModelRequestRejectedEmail(
                        UserMailDto.from(recipient),
                        request.getRequestedBrandName(),
                        request.getModelName()
                );
            }
        }
    }

    private boolean shouldSendSite(final User recipient, final NotificationType type) {
        return notificationPreferenceService.isNotificationEnabled(recipient, type, NotificationChannel.SITE);
    }

    private boolean shouldSendEmail(final User recipient, final NotificationType type) {
        return notificationPreferenceService.isNotificationEnabled(recipient, type, NotificationChannel.EMAIL);
    }
}
