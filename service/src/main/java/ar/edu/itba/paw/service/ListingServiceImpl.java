package ar.edu.itba.paw.service;

import ar.edu.itba.paw.model.Condition;
import ar.edu.itba.paw.model.Image;
import ar.edu.itba.paw.model.Listing;
import ar.edu.itba.paw.model.ListingFilter;
import ar.edu.itba.paw.model.ListingSort;
import ar.edu.itba.paw.model.Page;
import ar.edu.itba.paw.model.ListingStatus;
import ar.edu.itba.paw.model.Model;
import ar.edu.itba.paw.model.Price;
import ar.edu.itba.paw.model.User;
import ar.edu.itba.paw.persistence.ListingDao;
import ar.edu.itba.paw.service.dto.ImageData;
import ar.edu.itba.paw.service.dto.LandingListingsDto;
import ar.edu.itba.paw.service.dto.ListingCreationDto;
import ar.edu.itba.paw.service.dto.ListingFilterDto;
import ar.edu.itba.paw.service.dto.ListingUpdateDto;
import ar.edu.itba.paw.service.exception.BadParameterException;
import ar.edu.itba.paw.service.exception.ForbiddenException;
import ar.edu.itba.paw.service.exception.NotFoundException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
@Transactional(readOnly = true)
public class ListingServiceImpl implements ListingService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ListingServiceImpl.class);
    private static final int LANDING_LIST_SIZE = 12;

    private final ListingDao listingDao;

    private final UserService userService;
    private final CatalogService catalogService;
    private final ImageService imageService;
    private final NotificationDispatcherService notificationDispatcherService;
    private final OfferService offerService;

    @Override
    public Listing getById(Long id) {
        return listingDao
            .getById(id)
            .orElseThrow(() -> NotFoundException.createFor("Listing with ID " + id));
    }

    @Override
    public Page<Listing> search(ListingFilterDto dto) {
        Objects.requireNonNull(dto, "ListingFilterDto cannot be null");
        LOGGER.debug("Searching listings: query='{}', categoryId={}, status={}, sort={}, page={}",
                dto.query(), dto.categoryId(), dto.status(), dto.sort(), dto.page());

        final boolean onlyFree = Boolean.TRUE.equals(dto.onlyFree());

        // the price range is hidden in the UI while "only free" is active
        final var filter = ListingFilter.builder()
            .categoryId(dto.categoryId())
            .subcategoryId(dto.subcategoryId())
            .minPrice(onlyFree ? null : sanitizePrice(dto.minPrice()))
            .maxPrice(onlyFree ? null : sanitizePrice(dto.maxPrice()))
            .condition(dto.condition())
            .acceptsTrade(Boolean.TRUE.equals(dto.acceptsTrade()) ? Boolean.TRUE : null)
            .onlyFree(onlyFree ? Boolean.TRUE : null)
            .query(dto.query())
            .sort(dto.sort())
            .page(sanitizePage(dto.page()))
            .pageSize(dto.pageSize())
            .creatorId(dto.creatorId())
            .status(dto.status())
            .hasActiveOffers(dto.hasActiveOffers())
            .provinceId(dto.provinceId())
            .acceptsShipping(Boolean.TRUE.equals(dto.acceptsShipping()) ? Boolean.TRUE : null)
            .brandId(dto.brandId())
            .modelId(dto.modelId())
            .yearMin(dto.yearMin())
            .yearMax(dto.yearMax())
            .build();

        return listingDao.search(filter);
    }

    @Override
    public LandingListingsDto getForLanding() {
        return new LandingListingsDto(
            landingListings(ListingSort.MOST_OFFERS, true),
            landingListings(ListingSort.RECENT_OFFERS, true),
            landingListings(ListingSort.RECENT, false)
        );
    }

    private List<Listing> landingListings(final ListingSort sort, final boolean hasActiveOffers) {
        final var filter = ListingFilterDto.builder()
            .sort(sort)
            .status(ListingStatus.ACTIVE)
            .page(1)
            .pageSize(LANDING_LIST_SIZE)
            .hasActiveOffers(hasActiveOffers ? Boolean.TRUE : null)
            .build();
        return search(filter).getContent();
    }

    private static int sanitizePage(final Integer page) {
        return page == null || page < 1 ? 1 : page;
    }

    private static BigDecimal sanitizePrice(final BigDecimal value) {
        return value != null && value.signum() >= 0 ? value : null;
    }

    @Override
    @Transactional
    public Listing create(ListingCreationDto dto) {
        Objects.requireNonNull(dto, "ListingCreationDto cannot be null");
        LOGGER.debug("Creating listing: title='{}', creatorId={}, modelId={}, year={}", dto.title(), dto.creatorId(), dto.modelId(), dto.year());

        User creator = userService.getById(dto.creatorId())
                .orElseThrow(() -> new BadParameterException("Invalid creatorId"));

        Model model;
        try {
            model = catalogService.getModelById(dto.modelId());
        } catch (NotFoundException e) {
            throw BadParameterException.create("modelId", e.getMessage());
        }

        List<Long> imageIds = new ArrayList<>();
        if (dto.images() != null) {
            for (int i = 0; i < dto.images().size(); i++) {
                ImageData imageData = dto.images().get(i);
                if (imageData != null && imageData.imageBytes() != null && imageData.imageBytes().length > 0) {
                    String alt = "Image " + (i + 1) + " for listing: " + dto.title();
                    Image image = imageService.create(imageData.imageFilename(), alt, imageData.imageContentType(), imageData.imageBytes());
                    imageIds.add(image.getId());
                }
            }
        }

        if (dto.condition() == null) {
            throw BadParameterException.create("condition", "Condition is required");
        }
        final Condition condition = dto.condition();

        final Price price = new Price(dto.price());
        var listing = listingDao.create(
            dto.title(),
            price,
            creator,
            model,
            dto.year(),
            condition,
            acceptsTradeFor(price, dto.acceptsTrade()),
            dto.acceptsShipping(),
            dto.description(),
            imageIds
        );

        LOGGER.info("Listing created: id={}, title='{}', creatorId={}, price={}, images={}",
                listing.getId(), listing.getTitle(), creator.getId(), dto.price(), imageIds.size());

        notificationDispatcherService.notifyListingPublished(listing);

        return listing;
    }

    // A free listing can't be traded: there is no value to exchange against, so the request is silently dropped.
    private static boolean acceptsTradeFor(final Price price, final boolean requested) {
        return requested && price.getAmount().signum() > 0;
    }

    @Override
    @Transactional
    public Listing purchase(Long id, Long buyerId, String message) {
        LOGGER.info("Processing purchase: listingId={}, buyerId={}", id, buyerId);
        var listing = listingDao
            .getById(id)
            .orElseThrow(() -> NotFoundException.createFor("Listing with ID " + id));

        if (!listing.isPendingTransaction()) {
            LOGGER.warn("Purchase attempted on listing {} with non-PENDING_TRANSACTION status: {}", id, listing.getStatus());
            throw new BadParameterException("Listing is not in a pending transaction state");
        }

        listingDao.purchase(id, buyerId);

        final User buyer = userService.getById(buyerId)
            .orElseThrow(() -> new BadParameterException("Invalid buyerId"));
        final User seller = listing.getCreator();

        notificationDispatcherService.notifyListingSold(listing, buyer, message);
        notificationDispatcherService.notifyPurchaseConfirmed(listing, buyer);

        LOGGER.info("Listing purchased: id={}, buyerId={}, sellerId={}", id, buyerId, seller.getId());
        return listing;
    }

    @Override
    @Transactional
    public Listing pendingTransaction(Long id, Long buyerId, String message) {
        LOGGER.debug("Setting listing to pending transaction: listingId={}, buyerId={}", id, buyerId);
        var listing = listingDao
            .getById(id)
            .orElseThrow(() -> NotFoundException.createFor("Listing with ID " + id));

        listingDao.pendingTransaction(id, buyerId);

        return listing;
    }

    @Override
    @Transactional
    public Listing update(ListingUpdateDto dto, Long currentUserId) {
        Objects.requireNonNull(dto, "ListingUpdateDto cannot be null");
        LOGGER.debug("Updating listing id={} by user {}", dto.listingId(), currentUserId);

        var existing = getById(dto.listingId());

        if (!existing.isCreator(currentUserId)) {
            throw new ForbiddenException("Not authorized to update this listing");
        }
        if (existing.isCanceled()) {
            throw new ForbiddenException("Cannot edit a canceled listing");
        }

        Model model;
        try {
            model = catalogService.getModelById(dto.modelId());
        } catch (NotFoundException e) {
            throw BadParameterException.create("modelId", e.getMessage());
        }

        if (dto.condition() == null) {
            throw BadParameterException.create("condition", "Condition is required");
        }
        final Condition condition = dto.condition();

        final Price price = new Price(dto.price());
        var updated = listingDao.update(
            existing.getId(),
            dto.title(),
            price,
            model,
            dto.year(),
            condition,
            acceptsTradeFor(price, dto.acceptsTrade()),
            dto.acceptsShipping(),
            dto.description()
        );
        LOGGER.info("Listing updated: id={}, title='{}'", updated.getId(), updated.getTitle());
        return updated;
    }

    @Override
    @Transactional
    public void cancel(Long id, Long currentUserId) {
        LOGGER.info("Canceling listing id={} by user {}", id, currentUserId);
        var listing = getById(id);
        if (!listing.isCreator(currentUserId)) {
            throw new ForbiddenException("Not authorized to cancel this listing");
        }
        offerService.rejectPendingOffersForListing(id, null);
        if (!listingDao.cancel(id)) {
            throw NotFoundException.createFor("Listing with ID " + id);
        }
        LOGGER.info("Listing canceled: id={}", id);
    }

    @Override
    @Transactional
    public void cancelByAdmin(Long id) {
        LOGGER.info("Canceling listing id={} by Admin", id);
        offerService.rejectPendingOffersForListing(id, null);
        if (!listingDao.cancel(id)) {
            throw NotFoundException.createFor("Listing with ID " + id);
        }
        LOGGER.info("Listing canceled by Admin: id={}", id);
    }

    @Override
    @Transactional
    public void updateStatus(Long id, ListingStatus status) {
        LOGGER.debug("Updating listing status: id={}, newStatus={}", id, status);
        listingDao.updateStatus(id, status);
    }

    @Override
    @Transactional
    public void updateStatusBulk(List<Long> ids, ListingStatus status) {
        LOGGER.debug("Bulk updating listing status: ids={}, newStatus={}", ids, status);
        listingDao.updateStatusBulk(ids, status);
    }
}
