package ar.edu.itba.paw.service.notification;

import ar.edu.itba.paw.model.Listing;
import ar.edu.itba.paw.model.NotificationType;
import ar.edu.itba.paw.model.Offer;
import ar.edu.itba.paw.model.User;
import ar.edu.itba.paw.model.Brand;
import ar.edu.itba.paw.model.BrandModelRequest;
import ar.edu.itba.paw.model.Model;
import ar.edu.itba.paw.service.dto.SiteNotificationCreationDto;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
final class SiteNotificationCreationDtoFactory {

    /**
     * NOTE:
     * All methods in this class are package-private on purpose,
     * as they are only meant to be used in NotificationDispatcherServiceImpl.
     */

    static SiteNotificationCreationDto purchaseConfirmed(
            @NonNull final Listing listing,
            @NonNull final User buyer
    ) {
        final User seller = listing.getCreator();
        final String bodyKey = listing.isFree()
                ? "notification.purchaseConfirmed.free.body"
                : "notification.purchaseConfirmed.body";
        return SiteNotificationCreationDto.builder()
                .notificationRecipient(buyer)
                .notificationType(NotificationType.PURCHASE_CONFIRMED)
                .titleKey("notification.purchaseConfirmed.title")
                .bodyKey(bodyKey)
                .bodyArgs(List.of(seller.getDisplayName(), listing.getTitle()))
                .actionUrl("/account/my-offers?statusGroup=RESOLVED")
                .actionLabelKey("notification.purchaseConfirmed.cta")
                .build();
    }

    static SiteNotificationCreationDto newOffer(@NonNull final Offer offer) {
        final Listing listing = offer.getListing();
        final User seller = listing.getCreator();
        final User buyer = offer.getBuyer();

        final String bodyKey;
        final List<String> bodyArgs;
        if (offer.isForFreeListing()) {
            bodyKey = "notification.newOffer.free.body";
            bodyArgs = List.of(buyer.getDisplayName(), listing.getTitle());
        } else if (offer.getOfferedListing().isPresent()) {
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

        return SiteNotificationCreationDto.builder()
                .notificationRecipient(seller)
                .notificationType(NotificationType.NEW_OFFER)
                .titleKey("notification.newOffer.title")
                .bodyKey(bodyKey)
                .bodyArgs(bodyArgs)
                .actionUrl("/account/incoming-offers/" + offer.getId())
                .actionLabelKey("notification.newOffer.cta")
                .offer(offer)
                .build();
    }

    static SiteNotificationCreationDto offerRejected(@NonNull final Offer offer) {
        final Listing listing = offer.getListing();
        final User buyer = offer.getBuyer();

        final String bodyKey;
        final List<String> bodyArgs;
        if (offer.isForFreeListing()) {
            bodyKey = "notification.offerRejected.free.body";
            bodyArgs = List.of(listing.getTitle());
        } else if (offer.getOfferedListing().isPresent()) {
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

        return SiteNotificationCreationDto.builder()
                .notificationRecipient(buyer)
                .notificationType(NotificationType.OFFER_REJECTED)
                .titleKey("notification.offerRejected.title")
                .bodyKey(bodyKey)
                .bodyArgs(bodyArgs)
                .actionUrl("/listing")
                .actionLabelKey("notification.offerRejected.cta")
                .offer(offer)
                .build();
    }

    static SiteNotificationCreationDto offerWithdrawn(@NonNull final Offer offer) {
        final Listing listing = offer.getListing();
        final User seller = listing.getCreator();
        final User buyer = offer.getBuyer();

        final String bodyKey;
        final List<String> bodyArgs;
        if (offer.isForFreeListing()) {
            bodyKey = "notification.offerWithdrawn.free.body";
            bodyArgs = List.of(buyer.getDisplayName(), listing.getTitle());
        } else if (offer.getOfferedListing().isPresent()) {
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

        return SiteNotificationCreationDto.builder()
                .notificationRecipient(seller)
                .notificationType(NotificationType.OFFER_WITHDRAWN)
                .titleKey("notification.offerWithdrawn.title")
                .bodyKey(bodyKey)
                .bodyArgs(bodyArgs)
                .actionUrl("/listing/" + listing.getId())
                .actionLabelKey("notification.offerWithdrawn.cta")
                .offer(offer)
                .build();
    }

    static SiteNotificationCreationDto offerAccepted(@NonNull final Offer offer) {
        final Listing listing = offer.getListing();
        final User buyer = offer.getBuyer();
        final User seller = listing.getCreator();

        final boolean free = offer.isForFreeListing();
        final String bodyKey;
        final List<String> bodyArgs;
        if (free) {
            bodyKey = "notification.offerAccepted.free.body";
            bodyArgs = List.of(listing.getTitle(), seller.getDisplayName());
        } else if (offer.getOfferedListing().isPresent()) {
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

        return SiteNotificationCreationDto.builder()
                .notificationRecipient(buyer)
                .notificationType(NotificationType.OFFER_ACCEPTED)
                .titleKey("notification.offerAccepted.title")
                .bodyKey(bodyKey)
                .bodyArgs(bodyArgs)
                // A free listing has no payment page, so the buyer is sent to the list of offers in progress instead
                .actionUrl(free
                        ? "/account/my-offers?statusGroup=PENDING_PAYMENT"
                        : "/account/my-offers/" + offer.getId() + "/payment")
                .actionLabelKey(free ? "notification.offerAccepted.free.cta" : "notification.offerAccepted.cta")
                .offer(offer)
                .build();
    }

    static SiteNotificationCreationDto proofOfPaymentUploaded(@NonNull final Offer offer) {
        final Listing listing = offer.getListing();
        final User seller = listing.getCreator();
        final User buyer = offer.getBuyer();

        return SiteNotificationCreationDto.builder()
                .notificationRecipient(seller)
                .notificationType(NotificationType.PROOF_OF_PAYMENT_UPLOADED)
                .titleKey("notification.proofOfPaymentUploaded.title")
                .bodyKey("notification.proofOfPaymentUploaded.body")
                .bodyArgs(List.of(buyer.getDisplayName(), listing.getTitle()))
                .actionUrl("/account/incoming-offers/" + offer.getId())
                .actionLabelKey("notification.proofOfPaymentUploaded.cta")
                .offer(offer)
                .build();
    }

    static SiteNotificationCreationDto proofOfShippingUploaded(@NonNull final Offer offer) {
        final Listing listing = offer.getListing();
        final User buyer = offer.getBuyer();
        final User seller = listing.getCreator();

        final String tracking = offer.getTrackingNumber() != null ? offer.getTrackingNumber().trim() : "";
        final String bodyKey = !tracking.isEmpty()
                ? "notification.proofOfShippingUploaded.body.tracking"
                : "notification.proofOfShippingUploaded.body";
        final List<String> bodyArgs = !tracking.isEmpty()
                ? List.of(seller.getDisplayName(), listing.getTitle(), tracking)
                : List.of(seller.getDisplayName(), listing.getTitle());

        return SiteNotificationCreationDto.builder()
                .notificationRecipient(buyer)
                .notificationType(NotificationType.PROOF_OF_SHIPPING_UPLOADED)
                .titleKey("notification.proofOfShippingUploaded.title")
                .bodyKey(bodyKey)
                .bodyArgs(bodyArgs)
                .actionUrl("/account/my-offers?statusGroup=PENDING_PAYMENT")
                .actionLabelKey("notification.proofOfShippingUploaded.cta")
                .offer(offer)
                .build();
    }

    static SiteNotificationCreationDto brandModelRequestAccepted(
            @NonNull final BrandModelRequest request,
            @NonNull final Brand brand,
            @NonNull final Model model
    ) {
        return SiteNotificationCreationDto.builder()
                .notificationRecipient(request.getRequester())
                .notificationType(NotificationType.BRAND_MODEL_REQUEST_ACCEPTED)
                .titleKey("notification.brandModelRequest.accepted.title")
                .bodyKey("notification.brandModelRequest.accepted.body")
                .bodyArgs(List.of(brand.getName(), model.getName()))
                .actionUrl("/listing/new/choose-product?modelId=" + model.getId())
                .actionLabelKey("notification.brandModelRequest.accepted.cta")
                .build();
    }

    static SiteNotificationCreationDto brandModelRequestRejected(@NonNull final BrandModelRequest request) {
        return SiteNotificationCreationDto.builder()
                .notificationRecipient(request.getRequester())
                .notificationType(NotificationType.BRAND_MODEL_REQUEST_REJECTED)
                .titleKey("notification.brandModelRequest.rejected.title")
                .bodyKey("notification.brandModelRequest.rejected.body")
                .bodyArgs(List.of(request.getRequestedBrandName(), request.getModelName()))
                .actionUrl("/listing")
                .actionLabelKey("notification.brandModelRequest.rejected.cta")
                .build();
    }

    private static boolean hasCashSupplement(final BigDecimal amount) {
        return amount != null && amount.signum() > 0;
    }

    private static String formatAmount(final BigDecimal amount) {
        return amount != null ? amount.setScale(2, RoundingMode.HALF_UP).toPlainString() : "0.00";
    }
}
