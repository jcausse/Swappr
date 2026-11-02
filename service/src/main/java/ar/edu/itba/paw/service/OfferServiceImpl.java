package ar.edu.itba.paw.service;

import ar.edu.itba.paw.model.Listing;
import ar.edu.itba.paw.model.ListingStatus;
import ar.edu.itba.paw.model.Offer;
import ar.edu.itba.paw.model.OfferFilter;
import ar.edu.itba.paw.model.OfferStatus;
import ar.edu.itba.paw.model.OfferStatusGroup;
import ar.edu.itba.paw.model.Page;
import ar.edu.itba.paw.model.File;
import ar.edu.itba.paw.model.User;
import ar.edu.itba.paw.model.OfferRating;
import ar.edu.itba.paw.persistence.OfferDao;
import ar.edu.itba.paw.persistence.FileDao;
import ar.edu.itba.paw.persistence.UserDao;
import ar.edu.itba.paw.model.RatingRole;
import ar.edu.itba.paw.service.dto.FileDto;
import ar.edu.itba.paw.service.dto.OfferCreationDto;
import ar.edu.itba.paw.service.dto.OfferFilterDto;
import ar.edu.itba.paw.service.dto.mail.OfferMailDto;
import ar.edu.itba.paw.service.exception.BadParameterException;
import ar.edu.itba.paw.service.exception.ForbiddenException;
import ar.edu.itba.paw.service.exception.NotFoundException;

import java.time.Instant;
import java.math.BigDecimal;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Scheduled;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
@Transactional(readOnly = true)
public class OfferServiceImpl implements OfferService {

    private static final Logger LOGGER = LoggerFactory.getLogger(OfferServiceImpl.class);

    private final OfferDao offerDao;
    private final FileDao fileDao;
    private final UserDao userDao;
    private final UserService userService;
    private final ListingService listingService;
    private final MailingService mailingService;
    private final RatingService ratingService;
    private static final Duration RATING_AUTO_ASSIGN_DELAY = Duration.ofDays(14);

    @Autowired
    public OfferServiceImpl(OfferDao offerDao, FileDao fileDao, UserDao userDao,
                            UserService userService, @Lazy ListingService listingService,
                            MailingService mailingService, RatingService ratingService) {
        this.offerDao = offerDao;
        this.fileDao = fileDao;
        this.userDao = userDao;
        this.userService = userService;
        this.listingService = listingService;
        this.mailingService = mailingService;
        this.ratingService = ratingService;
    }

    @Override
    public Optional<Offer> getById(Long id) {
        return offerDao.getById(id);
    }

    @Override
    public Optional<Offer> getByListingAndBuyer(Listing listing, User buyer) {
        if (buyer == null) return Optional.empty();
        return offerDao.getByListingAndBuyer(listing, buyer);
    }

    @Override
    public Page<Offer> get(OfferFilterDto dto) {
        Objects.requireNonNull(dto, "OfferFilterDto cannot be null");

        final var filter = OfferFilter.builder()
            .page(sanitizePage(dto.page()))
            .pageSize(dto.pageSize())
            .sellerId(dto.sellerId())
            .buyerId(dto.buyerId())
            .status(parseStatusGroup(dto.statusGroup()))
            .build();

        return offerDao.search(filter);
    }

    private static int sanitizePage(final Integer page) {
        return page == null || page < 1 ? 1 : page;
    }

    private static List<OfferStatus> parseStatusGroup(final String value) {
        final var statusGroup = value == null || value.isBlank()
            ? null
            : OfferStatusGroup.fromString(value).orElse(null);

        return statusGroup == null ? OfferStatusGroup.PENDING.toStatusList() : statusGroup.toStatusList();
    }

    @Override
    @Transactional
    public Offer create(OfferCreationDto dto) {
        Objects.requireNonNull(dto, "OfferCreationDto cannot be null");

        final var listing = listingService.getById(dto.listingId());
        final var listingPrice = listing.getPrice().getAmount();

        BigDecimal amount = switch (dto.offerType()) {
            case FULL -> listingPrice;
            case CUSTOM -> dto.customAmount();
            case TRADE -> dto.tradeAmount() != null ? dto.tradeAmount() : BigDecimal.ZERO;
        };

        var isFullPrice = amount.equals(listingPrice);

        LOGGER.debug("Creating offer: listingId={}, buyerId={}, amount={}", dto.listingId(), dto.buyerId(), amount);

        if (!listing.isActive()) {
            LOGGER.warn("Offer attempted on listing {} with non-ACTIVE status: {}", dto.listingId(), listing.getStatus());
            throw BadParameterException.create("listingId", "Listing is not available for offers");
        }
        if (listing.isCreator(dto.buyerId())) {
            LOGGER.warn("User {} attempted to buy their own listing {}", dto.buyerId(), dto.listingId());
            throw BadParameterException.create("buyerId", "User cannot buy their own listing");
        }
        if (amount == null || amount.signum() < 0) {
            throw BadParameterException.create("amount", "Offer amount must be non-negative");
        }
        if (amount.compareTo(listingPrice) > 0) {
            LOGGER.warn("Offer amount {} exceeds price {} of listing {}", amount, listingPrice, dto.listingId());
            throw BadParameterException.create("amount", "Offer amount cannot exceed the listing price");
        }

        final User buyer = userService.getById(dto.buyerId())
                .orElseThrow(() -> new BadParameterException("Invalid buyerId"));

        Long offeredListingId = dto.offeredListingId();
        boolean isTrade = offeredListingId != null;

        if (isTrade) {
            if (!listing.isAcceptsTrade()) {
                LOGGER.warn("Trade offer attempted on listing {} that does not accept trades", dto.listingId());
                throw BadParameterException.create("listingId", "This listing does not accept trades");
            }
            final Listing offeredListing = listingService.getById(offeredListingId);
            if (!offeredListing.isCreator(buyer)) {
                throw BadParameterException.create("offeredListingId", "Offered listing must belong to the buyer");
            }
            if (!offeredListing.isActive()) {
                throw BadParameterException.create("offeredListingId", "Offered listing must be active");
            }
            // Set offered listing to OFFERED_IN_TRADE
            listingService.updateStatus(offeredListingId, ListingStatus.OFFERED_IN_TRADE);
        }

        final Offer offer = offerDao.create(
            dto.listingId(),
            buyer,
            amount,
            isFullPrice,
            OfferStatus.PENDING,
            dto.message(),
            Instant.now(),
            offeredListingId
        );

        LOGGER.info("Offer created: id={}, listingId={}, buyerId={}, amount={}, isTrade={}",
                offer.getId(), dto.listingId(), dto.buyerId(), amount, isTrade);

        // Send email notification to seller about the new offer
        mailingService.sendNewOfferEmail(OfferMailDto.from(offer));

        return offer;
    }

    @Override
    @Transactional
    public Offer accept(Long offerId, Long currentUserId) {
        LOGGER.debug("Accepting offer: offerId={}, by userId={}", offerId, currentUserId);
        final Offer offer = offerDao.getById(offerId)
            .orElseThrow(() -> NotFoundException.createFor("Offer with ID " + offerId));

        if (!offer.isSeller(currentUserId)) {
            LOGGER.warn("User {} unauthorized to accept offer {}", currentUserId, offerId);
            throw new ForbiddenException("Not authorized to accept this offer");
        }

        if (!offer.isPending()) {
            LOGGER.warn("Cannot accept offer {} in status {}", offerId, offer.getStatus());
            throw new BadParameterException("Offer is not pending");
        }

        // Handle trade offer: set offered listing to SOLD
        if (offer.getOfferedListingId() != null) {
            listingService.updateStatus(offer.getOfferedListingId(), ListingStatus.SOLD);
        }

        listingService.pendingTransaction(offer.getListing().getId(), offer.getBuyer().getId(), offer.getMessage());
        rejectPendingOffersForListing(offer.getListing().getId(), offerId);
        offerDao.updateStatus(offerId, OfferStatus.PENDING_PAYMENT);

        LOGGER.info("Offer accepted: offerId={}, listingId={}, buyerId={}, sellerId={}",
                offerId, offer.getListing().getId(), offer.getBuyer().getId(), currentUserId);

        final var offerMail = OfferMailDto.from(offer);
        // Send email notification to buyer about offer acceptance (pending payment)
        mailingService.sendOfferPendingPaymentEmail(offerMail);
        // Send email notification to seller about pending transaction
        mailingService.sendPendingTransactionEmail(offerMail);

        // FIXME double query
        return offerDao.getById(offerId).orElseThrow();
    }

    @Override
    @Transactional
    public Offer reject(Long offerId, Long currentUserId) {
        LOGGER.debug("Rejecting offer: offerId={}, by userId={}", offerId, currentUserId);
        final Offer offer = offerDao.getById(offerId)
            .orElseThrow(() -> NotFoundException.createFor("Offer with ID " + offerId));

        if (!offer.isSeller(currentUserId)) {
            LOGGER.warn("User {} unauthorized to reject offer {}", currentUserId, offerId);
            throw new ForbiddenException("Not authorized to reject this offer");
        }

        if (offer.isPendingPayment()) {
            // Reset listing to ACTIVE
            listingService.updateStatus(offer.getListing().getId(), ListingStatus.ACTIVE);
        } else if (!offer.isPending()) {
            LOGGER.warn("Cannot reject offer {} in status {}", offerId, offer.getStatus());
            throw new BadParameterException("Offer cannot be rejected in its current state");
        }

        // Handle trade offer: reset offered listing to ACTIVE
        if (offer.getOfferedListingId() != null) {
            listingService.updateStatus(offer.getOfferedListingId(), ListingStatus.ACTIVE);
        }

        offerDao.updateStatus(offerId, OfferStatus.REJECTED);

        LOGGER.info("Offer rejected: offerId={}, listingId={}, by sellerId={}", offerId, offer.getListing().getId(), currentUserId);

        // Send email notification to buyer about offer rejection
        mailingService.sendOfferRejectedEmail(OfferMailDto.from(offer));

        // FIXME double query
        return offerDao.getById(offerId).orElseThrow();
    }

    @Override
    @Transactional
    public Offer withdraw(Long offerId, Long currentUserId) {
        LOGGER.debug("Withdrawing offer: offerId={}, by userId={}", offerId, currentUserId);
        final Offer offer = offerDao.getById(offerId)
            .orElseThrow(() -> NotFoundException.createFor("Offer with ID " + offerId));

        if (!offer.isBuyer(currentUserId)) {
            LOGGER.warn("User {} unauthorized to withdraw offer {}", currentUserId, offerId);
            throw new BadParameterException("Not authorized to withdraw this offer");
        }

        if (!offer.isPending()) {
            LOGGER.warn("Cannot withdraw offer {} in status {}", offerId, offer.getStatus());
            throw new BadParameterException("Offer is not pending");
        }

        // Handle trade offer: reset offered listing to ACTIVE
        if (offer.getOfferedListingId() != null) {
            listingService.updateStatus(offer.getOfferedListingId(), ListingStatus.ACTIVE);
        }

        offerDao.withdraw(offerId, currentUserId);

        LOGGER.info("Offer withdrawn: offerId={}, listingId={}, by buyerId={}", offerId, offer.getListing().getId(), currentUserId);

        // Send email notification to seller about offer withdrawal
        mailingService.sendOfferWithdrawnEmail(OfferMailDto.from(offer));

        // FIXME double query
        return offerDao.getById(offerId).orElseThrow();
    }

    @Override
    @Transactional
    public List<Offer> rejectPendingOffersForListing(Long listingId, Long exceptOfferId) {
        final List<Offer> rejected = offerDao.rejectPendingOffers(listingId, exceptOfferId);
        LOGGER.info("Rejected {} pending offers for listing id={} (except offerId={})", rejected.size(), listingId, exceptOfferId);
        // Collect offered listing IDs to bulk update
        final List<Long> offeredListingIds = rejected.stream()
                .map(Offer::getOfferedListingId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();

        if (!offeredListingIds.isEmpty()) {
            listingService.updateStatusBulk(offeredListingIds, ListingStatus.ACTIVE);
        }

        for (Offer offer : rejected) {
            mailingService.sendOfferRejectedEmail(OfferMailDto.from(offer));
        }
        return rejected;
    }

    @Override
    @Transactional
    public Offer uploadProofOfPayment(Long offerId, Long buyerId, FileDto fileDto) {
        LOGGER.debug("Uploading proof of payment: offerId={}, buyerId={}", offerId, buyerId);
        final Offer offer = offerDao.getById(offerId)
            .orElseThrow(() -> NotFoundException.createFor("Offer with ID " + offerId));

        if (!offer.isBuyer(buyerId)) {
            LOGGER.warn("User {} unauthorized to upload proof of payment for offer {}", buyerId, offerId);
            throw new BadParameterException("Only the buyer can upload proof of payment");
        }

        if (!offer.canUploadDocuments()) {
            throw new BadParameterException("Offer is not pending payment");
        }

        if (fileDto == null) {
            throw new BadParameterException("No file given");
        }

        var partialFile = fileDto.toFile().orElseThrow(() -> new BadParameterException("No file given"));
        var contentType = partialFile.getContentType().orElse("");
        var data = partialFile.getData();
        var filename = partialFile.getFilename();
        var alt = "Proof of payment for offer " + offerId;

        if (data.length == 0) {
            throw new BadParameterException("File is empty");
        }

        if (!contentType.startsWith("image/") && !"application/pdf".equals(contentType)) {
            throw new BadParameterException("Only image and PDF files are allowed");
        }

        final File file = fileDao.create(filename, alt, contentType, data);
        offerDao.updateProofOfPaymentId(offerId, file.getId());

        LOGGER.info("Proof of payment uploaded: offerId={}, buyerId={}, fileId={}", offerId, buyerId, file.getId());

        // Notify seller that proof of payment was uploaded
        mailingService.sendProofOfPaymentUploadedEmail(OfferMailDto.from(offer));

        return offerDao.getById(offerId).orElseThrow();
    }

    @Override
    @Transactional
    public Offer uploadProofOfShipping(Long offerId, Long sellerId, FileDto fileDto, String trackingNumber) {
        LOGGER.debug("Uploading proof of shipping: offerId={}, sellerId={}", offerId, sellerId);
        final Offer offer = offerDao.getById(offerId)
            .orElseThrow(() -> NotFoundException.createFor("Offer with ID " + offerId));

        // TODO remove this validation after implementing preatuhorize
        if (!offer.isSeller(sellerId)) {
            LOGGER.warn("User {} unauthorized to upload proof of shipping for offer {}", sellerId, offerId);
            throw new BadParameterException("Only the seller can upload proof of shipping");
        }

        if (!offer.canUploadDocuments()) {
            throw new BadParameterException("Offer is not pending payment");
        }

        if (fileDto == null) {
            throw new BadParameterException("No file given");
        }

        var partialFileOptional = fileDto.toFile();
        if (partialFileOptional.isEmpty() && (trackingNumber == null || trackingNumber.isBlank())) {
            throw new BadParameterException("Either a file or a tracking number must be provided");
        }

        Long proofOfShippingId = null;
        if (partialFileOptional.isPresent()) {
            var partialFile = partialFileOptional.get();
            var contentType = partialFile.getContentType().orElse("");
            var data = partialFile.getData();
            var filename = partialFile.getFilename();
            var alt = "Proof of shipping for offer " + offerId;

            if (!contentType.startsWith("image/") && !"application/pdf".equals(contentType)) {
                throw new BadParameterException("Only image and PDF files are allowed");
            }

            final File file = fileDao.create(filename, alt, contentType, data);
            proofOfShippingId = file.getId();
        }

        offerDao.updateProofOfShipping(offerId, proofOfShippingId, trackingNumber);

        LOGGER.info("Proof of shipping uploaded: offerId={}, sellerId={}, hasFile={}, trackingNumber='{}'",
                offerId, sellerId, proofOfShippingId != null, trackingNumber);

        final Offer updatedOffer = offerDao.getById(offerId).orElseThrow();

        // Notify buyer that proof of shipping was uploaded
        mailingService.sendProofOfShippingUploadedEmail(OfferMailDto.from(updatedOffer));

        return updatedOffer;
    }

    @Override
    @Transactional
    public Offer confirmPayment(Long offerId, Long sellerId) {
        LOGGER.debug("Confirming payment: offerId={}, sellerId={}", offerId, sellerId);
        final Offer offer = offerDao.getById(offerId)
            .orElseThrow(() -> NotFoundException.createFor("Offer with ID " + offerId));

        if (!offer.isSeller(sellerId)) {
            LOGGER.warn("User {} unauthorized to confirm payment for offer {}", sellerId, offerId);
            throw new BadParameterException("Only the seller can confirm payment");
        }

        if (!offer.isPendingPayment()) {
            LOGGER.warn("Cannot confirm payment for offer {} in status {}", offerId, offer.getStatus());
            throw new BadParameterException("Offer is not pending payment");
        }

        listingService.purchase(offer.getListing().getId(), offer.getBuyer().getId(), offer.getMessage());
        offerDao.markAccepted(offerId, Instant.now());

        LOGGER.info("Payment confirmed: offerId={}, listingId={}, sellerId={}", offerId, offer.getListing().getId(), sellerId);

        return offerDao.getById(offerId).orElseThrow();
    }

    @Override
    public Optional<File> getProofOfPaymentFile(Long offerId, Long currentUserId) {
        final Offer offer = offerDao.getById(offerId)
            .orElseThrow(() -> NotFoundException.createFor("Offer with ID " + offerId));

        if (!offer.isParticipant(currentUserId)) {
            throw new ForbiddenException("Not authorized to download proof of payment");
        }

        if (offer.getProofOfPaymentId() == null) {
            return Optional.empty();
        }

        return fileDao.getById(offer.getProofOfPaymentId());
    }

    @Override
    public Optional<File> getProofOfShippingFile(Long offerId, Long currentUserId) {
        final Offer offer = offerDao.getById(offerId)
            .orElseThrow(() -> NotFoundException.createFor("Offer with ID " + offerId));

        if (!offer.isParticipant(currentUserId)) {
            throw new ForbiddenException("Not authorized to download proof of shipping");
        }

        if (offer.getProofOfShippingId() == null) {
            return Optional.empty();
        }

        return fileDao.getById(offer.getProofOfShippingId());
    }

    @Override
    public int getPendingOffersCount(User seller) {
        return offerDao.countPendingBySeller(seller);
    }

    @Override
    public Map<String, Long> getOfferCountByStatus(User currentUser, Boolean incoming) {
		return offerDao.countByStatus(currentUser, incoming);
    }

    @Override
    @Transactional
    public Offer rate(Offer offer, User currentUser, OfferRating rating) {
        return rate(offer, currentUser, rating, null);
    }

    @Override
    @Transactional
    public Offer rate(Offer offer, User currentUser, OfferRating rating, String reviewText) {
        if (!offer.isAccepted()) {
            throw new BadParameterException("Only accepted offers can be rated");
        }

		final var buyer = offer.getBuyer();
		final var seller = offer.getListing().getCreator();

		final boolean isBuyer = offer.isBuyer(currentUser);

        if (!offer.isParticipant(currentUser)) {
            throw new ForbiddenException("You did not participate in this offer");
        }
        if (!offer.canBeRatedBy(currentUser)) {
            throw new BadParameterException("You have already rated this offer");
        }

        final var role = isBuyer ? RatingRole.SELLER : RatingRole.BUYER;
        final var ratedUser = isBuyer ? seller : buyer;

        if (reviewText != null && !reviewText.isBlank()) {
            ratingService.create(currentUser, ratedUser, offer, role, rating, reviewText.trim());
        }

        if (isBuyer) {
            final boolean updated = offerDao.setSellerRating(offer.getId(), rating);
            if (!updated) {
                throw new BadParameterException("You have already rated this offer");
            }
            offer.setSellerRating(rating);
            userDao.incrementSellerRatingCounter(seller.getId(), rating);
        } else {
            final boolean updated = offerDao.setBuyerRating(offer.getId(), rating);
            if (!updated) {
                throw new BadParameterException("You have already rated this offer");
            }
            offer.setBuyerRating(rating);
            userDao.incrementBuyerRatingCounter(buyer.getId(), rating);
        }

        return offer;
    }

    @Override
    @Scheduled(cron = "0 0 * * * ?")
    @Transactional
    public void autoRatePendingOffers() {
        final Instant cutoff = Instant.now().minus(RATING_AUTO_ASSIGN_DELAY);
        final List<Offer> pending = offerDao.getAcceptedUnratedBefore(cutoff);

        for (Offer offer : pending) {
            if (offer.getSellerRating().isEmpty()) {
                final boolean updated = offerDao.setSellerRating(offer.getId(), OfferRating.POSITIVE);
                if (updated) {
                    userDao.incrementSellerRatingCounter(offer.getListing().getCreator().getId(), OfferRating.POSITIVE);
                }
            }
            if (offer.getBuyerRating().isEmpty()) {
                final boolean updated = offerDao.setBuyerRating(offer.getId(), OfferRating.POSITIVE);
                if (updated) {
                    userDao.incrementBuyerRatingCounter(offer.getBuyer().getId(), OfferRating.POSITIVE);
                }
            }
        }
    }

    @Override
    @Transactional
    public void cancelByAdmin(Long offerId) {
        LOGGER.info("Canceling offer by admin: offerId={}", offerId);

        /* Get offer */
        final Offer offer = offerDao.getById(offerId)
            .orElseThrow(() -> NotFoundException.createFor("Offer with ID " + offerId));

        /* Reset listing status to ACTIVE if the current offer is holding it PENDING_TRANSACTION */
        if (offer.isPendingPayment()) {
            listingService.updateStatus(offer.getListing().getId(), ListingStatus.ACTIVE);
        } else if (!offer.isPending()) {
            LOGGER.warn("Cannot cancel offer {} in status {}", offerId, offer.getStatus());
            throw new BadParameterException("Offer cannot be canceled in its current state");
        }

        /* If there is an offered listing, reset it to ACTIVE */
        if (offer.getOfferedListingId() != null) {
            listingService.updateStatus(offer.getOfferedListingId(), ListingStatus.ACTIVE);
        }

        /* Reject the offer */
        offerDao.updateStatus(offerId, OfferStatus.REJECTED);

        LOGGER.info("Offer canceled by admin: offerId={}, listingId={}", offerId, offer.getListing().getId());

        /* Send the buyer an email telling them the offer has been rejected */
        mailingService.sendOfferRejectedEmail(OfferMailDto.from(offer));
    }
}
