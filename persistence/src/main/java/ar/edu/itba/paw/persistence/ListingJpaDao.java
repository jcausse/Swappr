package ar.edu.itba.paw.persistence;

import ar.edu.itba.paw.model.Condition;
import ar.edu.itba.paw.model.Listing;
import ar.edu.itba.paw.model.ListingFilter;
import ar.edu.itba.paw.model.ListingSort;
import ar.edu.itba.paw.model.ListingStatus;
import ar.edu.itba.paw.model.Model;
import ar.edu.itba.paw.model.OfferStatus;
import ar.edu.itba.paw.model.Page;
import ar.edu.itba.paw.model.Price;
import ar.edu.itba.paw.model.User;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public class ListingJpaDao implements ListingDao {

    @PersistenceContext
    private EntityManager em;

    @Override
    public Optional<Listing> getById(Long id) {
        return Optional.ofNullable(em.find(Listing.class, id));
    }

    @Override
    public Listing create(
        String title,
        Price price,
        User creator,
        Model model,
        int year,
        Condition condition,
        boolean acceptsTrade,
        boolean acceptsShipping,
        String description,
        List<Long> imageIds
    ) {
        final User creatorRef = em.getReference(User.class, creator.getId());
        final Model modelRef = em.getReference(Model.class, model.getId());
        final List<Long> initialImageIds = imageIds != null ? new ArrayList<>(imageIds) : new ArrayList<>();

        final Listing listing = Listing.builder()
                .title(title)
                .price(price)
                .creator(creatorRef)
                .model(modelRef)
                .year(year)
                .condition(condition)
                .acceptsTrade(acceptsTrade)
                .acceptsShipping(acceptsShipping)
                .description(description)
                .status(ListingStatus.ACTIVE)
                .imageIds(initialImageIds)
                .build();
        em.persist(listing);
        return listing;
    }

    @Override
    public Page<Listing> search(ListingFilter filter) {
        return search(filter, false);
    }

    @Override
    public Page<Listing> searchForAdmin(ListingFilter filter) {
        return search(filter, true);
    }

    private Page<Listing> search(ListingFilter filter, boolean fetchCreator) {
        final StringBuilder whereClause = new StringBuilder(" WHERE 1=1");
        final Map<String, Object> params = new HashMap<>();
        final StringBuilder nativeWhere = new StringBuilder(" WHERE 1=1");
        final Map<String, Object> nativeParams = new HashMap<>();

        if (filter.getCategoryId() != null) {
            whereClause.append(" AND l.model.subcategory.category.id = :categoryId");
            nativeWhere.append(" AND s.category_id = :categoryId");
            params.put("categoryId", filter.getCategoryId());
            nativeParams.put("categoryId", filter.getCategoryId());
        }
        if (filter.getSubcategoryId() != null) {
            whereClause.append(" AND l.model.subcategory.id = :subcategoryId");
            nativeWhere.append(" AND m.subcategory_id = :subcategoryId");
            params.put("subcategoryId", filter.getSubcategoryId());
            nativeParams.put("subcategoryId", filter.getSubcategoryId());
        }
        if (filter.getBrandId() != null) {
            whereClause.append(" AND l.model.brand.id = :brandId");
            nativeWhere.append(" AND m.brand_id = :brandId");
            params.put("brandId", filter.getBrandId());
            nativeParams.put("brandId", filter.getBrandId());
        }
        if (filter.getModelId() != null) {
            whereClause.append(" AND l.model.id = :modelId");
            nativeWhere.append(" AND m.model_id = :modelId");
            params.put("modelId", filter.getModelId());
            nativeParams.put("modelId", filter.getModelId());
        }
        if (filter.getYearMin() != null) {
            whereClause.append(" AND l.year >= :yearMin");
            nativeWhere.append(" AND l.year >= :yearMin");
            params.put("yearMin", filter.getYearMin());
            nativeParams.put("yearMin", filter.getYearMin());
        }
        if (filter.getYearMax() != null) {
            whereClause.append(" AND l.year <= :yearMax");
            nativeWhere.append(" AND l.year <= :yearMax");
            params.put("yearMax", filter.getYearMax());
            nativeParams.put("yearMax", filter.getYearMax());
        }
        if (filter.getMinPrice() != null) {
            whereClause.append(" AND l.price.amount >= :minPrice");
            nativeWhere.append(" AND l.price >= :minPrice");
            params.put("minPrice", filter.getMinPrice());
            nativeParams.put("minPrice", filter.getMinPrice());
        }
        if (filter.getMaxPrice() != null) {
            whereClause.append(" AND l.price.amount <= :maxPrice");
            nativeWhere.append(" AND l.price <= :maxPrice");
            params.put("maxPrice", filter.getMaxPrice());
            nativeParams.put("maxPrice", filter.getMaxPrice());
        }
        if (filter.getCondition() != null) {
            whereClause.append(" AND l.condition = :condition");
            nativeWhere.append(" AND l.condition = :condition");
            params.put("condition", filter.getCondition());
            nativeParams.put("condition", filter.getCondition().name());
        }
        if (filter.getAcceptsTrade() != null) {
            whereClause.append(" AND l.acceptsTrade = :acceptsTrade");
            nativeWhere.append(" AND l.accepts_trade = :acceptsTrade");
            params.put("acceptsTrade", filter.getAcceptsTrade());
            nativeParams.put("acceptsTrade", filter.getAcceptsTrade());
        }
        if (filter.getAcceptsShipping() != null) {
            whereClause.append(" AND l.acceptsShipping = :acceptsShipping");
            nativeWhere.append(" AND l.accepts_shipping = :acceptsShipping");
            params.put("acceptsShipping", filter.getAcceptsShipping());
            nativeParams.put("acceptsShipping", filter.getAcceptsShipping());
        }
        if (filter.getStatus() != null) {
            whereClause.append(" AND l.status = :status");
            nativeWhere.append(" AND l.status = :status");
            params.put("status", filter.getStatus());
            nativeParams.put("status", filter.getStatus().name());
        }
        if (filter.getCreatorId() != null) {
            whereClause.append(" AND l.creator.id = :creatorId");
            nativeWhere.append(" AND l.creator_id = :creatorId");
            params.put("creatorId", filter.getCreatorId());
            nativeParams.put("creatorId", filter.getCreatorId());
        }
        if (filter.getProvinceId() != null) {
            whereClause.append(" AND l.creator.province.id = :provinceId");
            nativeWhere.append(" AND u.province_id = :provinceId");
            params.put("provinceId", filter.getProvinceId());
            nativeParams.put("provinceId", filter.getProvinceId());
        }
        if (filter.getQuery() != null && !filter.getQuery().isBlank()) {
            whereClause.append(" AND (LOWER(l.title) LIKE :query OR LOWER(l.description) LIKE :query)");
            nativeWhere.append(" AND (LOWER(l.title) LIKE :query OR LOWER(l.description) LIKE :query)");
            final String query = "%" + escapeLike(filter.getQuery().trim().toLowerCase()) + "%";
            params.put("query", query);
            nativeParams.put("query", query);
        }
        if (Boolean.TRUE.equals(filter.getHasActiveOffers())) {
            whereClause.append(" AND EXISTS (SELECT 1 FROM Offer o WHERE o.listing.id = l.id AND o.status = :pendingOfferStatus)");
            nativeWhere.append(" AND EXISTS (SELECT 1 FROM offers o WHERE o.listing_id = l.listing_id AND o.status = :pendingOfferStatus)");
            params.put("pendingOfferStatus", OfferStatus.PENDING);
            nativeParams.put("pendingOfferStatus", OfferStatus.PENDING.name());
        }

        final TypedQuery<Long> countQuery = em.createQuery(
                "SELECT COUNT(l) FROM Listing l" + whereClause, Long.class);
        params.forEach(countQuery::setParameter);
        final long totalCount = countQuery.getSingleResult();

        final int page = filter.getPage();
        final int pageSize = filter.getPageSize();
        if (totalCount == 0) {
            return new Page<>(List.of(), page, pageSize, 0);
        }

        // 1+1 pagination: page only listing IDs in a native query, then load those entities.
        final Query idQuery = em.createNativeQuery(
                "SELECT l.listing_id FROM listings l"
                + " JOIN models m ON m.model_id = l.model_id"
                + " JOIN subcategories s ON s.subcategory_id = m.subcategory_id"
                + " JOIN users u ON u.user_id = l.creator_id"
                + nativeWhere
                + " ORDER BY " + resolveNativeOrderBy(filter.getSort()))
                .setFirstResult((page - 1) * pageSize)
                .setMaxResults(pageSize);
        nativeParams.forEach(idQuery::setParameter);

        final List<Long> ids = ((List<?>) idQuery.getResultList()).stream()
                .map(row -> ((Number) row).longValue())
                .toList();
        if (ids.isEmpty()) {
            return new Page<>(List.of(), page, pageSize, totalCount);
        }

        final String creatorFetch = fetchCreator ? " JOIN FETCH l.creator" : "";
        final List<Listing> content = em.createQuery(
                "SELECT l FROM Listing l"
                + " JOIN FETCH l.model m"
                + " JOIN FETCH m.brand"
                + " JOIN FETCH m.subcategory s"
                + " JOIN FETCH s.category"
                + creatorFetch
                + " WHERE l.id IN :ids"
                + " ORDER BY " + resolveOrderBy(filter.getSort()), Listing.class)
                .setParameter("ids", ids)
                .getResultList();

        return new Page<>(content, page, pageSize, totalCount);
    }

    private static final Map<ListingSort, String> LISTING_SORT_TO_SQL = new HashMap<>(Map.ofEntries(
            Map.entry(ListingSort.PRICE_ASC, "l.price ASC, l.listing_id DESC"),
            Map.entry(ListingSort.PRICE_DESC, "l.price DESC, l.listing_id DESC"),
            Map.entry(ListingSort.NAME_ASC, "l.title ASC, l.listing_id DESC"),
            Map.entry(ListingSort.NAME_DESC, "l.title DESC, l.listing_id DESC"),
            Map.entry(ListingSort.MOST_OFFERS,
                    "(SELECT COUNT(*) FROM offers o WHERE o.listing_id = l.listing_id AND o.status = 'PENDING') DESC, l.listing_id DESC"),
            Map.entry(ListingSort.RECENT_OFFERS,
                    "(SELECT MAX(o.created_at) FROM offers o WHERE o.listing_id = l.listing_id AND o.status = 'PENDING') DESC NULLS LAST, l.listing_id DESC")
    ));

    private static final String LISTING_SORT_TO_SQL_DEFAULT = "l.listing_id DESC";

    private static final Map<ListingSort, String> LISTING_SORT_TO_JPQL = new HashMap<>(Map.ofEntries(
            Map.entry(ListingSort.PRICE_ASC, "l.price.amount ASC, l.id DESC"),
            Map.entry(ListingSort.PRICE_DESC, "l.price.amount DESC, l.id DESC"),
            Map.entry(ListingSort.NAME_ASC, "l.title ASC, l.id DESC"),
            Map.entry(ListingSort.NAME_DESC, "l.title DESC, l.id DESC"),
            Map.entry(ListingSort.MOST_OFFERS, "l.pendingOffersCount DESC, l.id DESC"),
            Map.entry(ListingSort.RECENT_OFFERS, "l.recentPendingOfferAt DESC NULLS LAST, l.id DESC")
    ));

    private static final String LISTING_SORT_TO_JPQL_DEFAULT = "l.id DESC";

    private static String resolveNativeOrderBy(final ListingSort sort) {
        return LISTING_SORT_TO_SQL.getOrDefault(sort, LISTING_SORT_TO_SQL_DEFAULT);
    }

    private static String resolveOrderBy(final ListingSort sort) {
        return LISTING_SORT_TO_JPQL.getOrDefault(sort, LISTING_SORT_TO_JPQL_DEFAULT);
    }

    private static String escapeLike(String input) {
        return input.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_");
    }

    @Override
    public void purchase(Long id) {
        final Listing listing = em.find(Listing.class, id);
        if (listing != null) {
            listing.setStatus(ListingStatus.SOLD);
        }
    }

    @Override
    public void pendingTransaction(Long id) {
        final Listing listing = em.find(Listing.class, id);
        if (listing != null) {
            listing.setStatus(ListingStatus.PENDING_TRANSACTION);
        }
    }

    @Override
    public boolean cancel(Long id) {
        final Listing listing = em.find(Listing.class, id);
        if (listing == null) {
            return false;
        }
        listing.setStatus(ListingStatus.CANCELED);
        return true;
    }

    @Override
    public int releaseOfferedListingsForParticipant(Long userId) {
        return em.createQuery(
                        "UPDATE Listing l SET l.status = :active"
                        + " WHERE l.id IN (SELECT o.offeredListing.id FROM Offer o"
                        + "   WHERE o.offeredListing IS NOT NULL"
                        + "   AND o.status IN :cancellableStatuses"
                        + "   AND (o.buyer.id = :userId OR o.listing.creator.id = :userId))")
                .setParameter("active", ListingStatus.ACTIVE)
                .setParameter("cancellableStatuses", List.of(OfferStatus.PENDING, OfferStatus.PENDING_PAYMENT))
                .setParameter("userId", userId)
                .executeUpdate();
    }

    @Override
    public int releaseListingsHeldByBuyerPaymentOffers(Long userId) {
        return em.createQuery(
                        "UPDATE Listing l SET l.status = :active"
                        + " WHERE l.id IN (SELECT o.listing.id FROM Offer o"
                        + "   WHERE o.buyer.id = :userId AND o.status = :pendingPayment)")
                .setParameter("active", ListingStatus.ACTIVE)
                .setParameter("userId", userId)
                .setParameter("pendingPayment", OfferStatus.PENDING_PAYMENT)
                .executeUpdate();
    }

    @Override
    public int releaseListingsHeldByPendingPaymentTradeOffersForOfferedListing(Long offeredListingId) {
        return em.createQuery(
                        "UPDATE Listing l SET l.status = :active"
                        + " WHERE l.id IN (SELECT o.listing.id FROM Offer o"
                        + "   WHERE o.offeredListing.id = :offeredListingId"
                        + "   AND o.status = :pendingPayment)")
                .setParameter("active", ListingStatus.ACTIVE)
                .setParameter("offeredListingId", offeredListingId)
                .setParameter("pendingPayment", OfferStatus.PENDING_PAYMENT)
                .executeUpdate();
    }

    @Override
    public int cancelAllByCreator(Long userId) {
        return em.createQuery(
                        "UPDATE Listing l SET l.status = :canceled"
                        + " WHERE l.creator.id = :userId AND l.status IN :cancellableStatuses")
                .setParameter("canceled", ListingStatus.CANCELED)
                .setParameter("userId", userId)
                .setParameter("cancellableStatuses",
                        List.of(ListingStatus.ACTIVE, ListingStatus.PENDING_TRANSACTION, ListingStatus.OFFERED_IN_TRADE))
                .executeUpdate();
    }

    @Override
    public int replaceModel(Long fromModelId, Long toModelId) {
        final Model from = em.getReference(Model.class, fromModelId);
        final Model to = em.getReference(Model.class, toModelId);
        return em.createQuery("UPDATE Listing l SET l.model = :to WHERE l.model = :from")
                .setParameter("to", to)
                .setParameter("from", from)
                .executeUpdate();
    }

    @Override
    public int replaceSameNameModelsForBrands(Long fromBrandId, Long toBrandId) {
        // This is JPQL, not native SQL: the subquery in SET is a Hibernate extension that standard
        // JPQL does not support.
        return em.createQuery(
                "UPDATE Listing l SET l.model = ("
                + "SELECT newModel FROM Model oldModel, Model newModel"
                + " WHERE oldModel.id = l.model.id"
                + "   AND oldModel.brand.id = :fromBrandId"
                + "   AND newModel.brand.id = :toBrandId"
                + "   AND newModel.name = oldModel.name"
                + ") WHERE l.model.id IN ("
                + "SELECT oldModel.id FROM Model oldModel, Model newModel"
                + " WHERE oldModel.brand.id = :fromBrandId"
                + "   AND newModel.brand.id = :toBrandId"
                + "   AND newModel.name = oldModel.name"
                + ")")
                .setParameter("fromBrandId", fromBrandId)
                .setParameter("toBrandId", toBrandId)
                .executeUpdate();
    }

    @Override
    public void updateStatus(Long id, ListingStatus status) {
        final Listing listing = em.find(Listing.class, id);
        if (listing != null) {
            listing.setStatus(status);
        }
    }

    @Override
    public void updateStatusBulk(List<Long> ids, ListingStatus status) {
        if (ids == null || ids.isEmpty()) {
            return;
        }
        em.createQuery("UPDATE Listing l SET l.status = :status WHERE l.id IN :ids")
                .setParameter("status", status)
                .setParameter("ids", ids)
                .executeUpdate();
    }

    @Override
    public Optional<Listing> update(
        Long id,
        String title,
        Price price,
        Model model,
        int year,
        Condition condition,
        boolean acceptsTrade,
        boolean acceptsShipping,
        String description
    ) {
        final Listing listing = em.find(Listing.class, id);
        if (listing == null) {
            return Optional.empty();
        }
        listing.setTitle(title);
        listing.setPrice(price);
        listing.setModel(em.getReference(Model.class, model.getId()));
        listing.setYear(year);
        listing.setCondition(condition);
        listing.setAcceptsTrade(acceptsTrade);
        listing.setAcceptsShipping(acceptsShipping);
        listing.setDescription(description);
        return Optional.of(listing);
    }

    @Override
    public void updateImages(final Long listingId, final List<Long> imageIds) {
        final Listing listing = em.find(Listing.class, listingId);
        if (listing == null) {
            return;
        }

        listing.setImageIds(imageIds != null ? new ArrayList<>(imageIds) : new ArrayList<>());
    }
}
