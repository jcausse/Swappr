package ar.edu.itba.paw.persistence;

import ar.edu.itba.paw.model.Condition;
import ar.edu.itba.paw.model.Listing;
import ar.edu.itba.paw.model.ListingFilter;
import ar.edu.itba.paw.model.ListingStatus;
import ar.edu.itba.paw.model.Model;
import ar.edu.itba.paw.model.Page;
import ar.edu.itba.paw.model.Price;
import ar.edu.itba.paw.model.User;
import java.util.List;
import java.util.Optional;

public interface ListingDao {

    Optional<Listing> getById(Long id);

    Listing create(String title, Price price, User creator, Model model, int year,
                    Condition condition, boolean acceptsTrade, boolean acceptsShipping, String description, List<Long> imageIds);
    Page<Listing> search(ListingFilter filter);
    Page<Listing> searchForAdmin(ListingFilter filter);
    void purchase(Long id);
    void pendingTransaction(Long id);
    void updateStatus(Long id, ListingStatus status);

    void updateStatusBulk(List<Long> ids, ListingStatus status);

    Optional<Listing> update(Long id, String title, Price price, Model model, int year,
                             Condition condition, boolean acceptsTrade, boolean acceptsShipping, String description);
    boolean cancel(Long id);

    int releaseOfferedListingsForParticipant(Long userId);

    int releaseListingsHeldByBuyerPaymentOffers(Long userId);

    /**
     * Admin-cancellation edge case: bulk-releases the target listings held by PENDING_PAYMENT
     * trade offers whose offered listing is the one being canceled.
     */
    int releaseListingsHeldByPendingPaymentTradeOffersForOfferedListing(Long offeredListingId);

    int cancelAllByCreator(Long userId);

    void updateImages(Long listingId, List<Long> imageIds);

    int replaceModel(Long fromModelId, Long toModelId);

    int replaceSameNameModelsForBrands(Long fromBrandId, Long toBrandId);
}
