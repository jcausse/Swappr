package ar.edu.itba.paw.service;

import ar.edu.itba.paw.persistence.ListingDao;
import ar.edu.itba.paw.service.exception.ForbiddenException;
import ar.edu.itba.paw.service.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
@Transactional(readOnly = true)
public class ListingCancellationServiceImpl implements ListingCancellationService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ListingCancellationServiceImpl.class);

    private final ListingDao listingDao;
    private final OfferService offerService;

    @Override
    @Transactional
    public void cancel(Long id, Long currentUserId) {
        LOGGER.info("Canceling listing id={} by user {}", id, currentUserId);
        final var listing = listingDao.getById(id)
            .orElseThrow(() -> NotFoundException.createFor("Listing with ID " + id));
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
        if (listingDao.getById(id).isEmpty()) {
            throw NotFoundException.createFor("Listing with ID " + id);
        }
        offerService.rejectPendingOffersForListing(id, null);
        if (!listingDao.cancel(id)) {
            throw NotFoundException.createFor("Listing with ID " + id);
        }
        LOGGER.info("Listing canceled by Admin: id={}", id);
    }
}
