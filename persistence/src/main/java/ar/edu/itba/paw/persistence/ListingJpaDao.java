package ar.edu.itba.paw.persistence;

import ar.edu.itba.paw.model.Condition;
import ar.edu.itba.paw.model.Image;
import ar.edu.itba.paw.model.Listing;
import ar.edu.itba.paw.model.ListingFilter;
import ar.edu.itba.paw.model.ListingSort;
import ar.edu.itba.paw.model.ListingStatus;
import ar.edu.itba.paw.model.OfferStatus;
import ar.edu.itba.paw.model.Page;
import ar.edu.itba.paw.model.Price;
import ar.edu.itba.paw.model.Product;
import ar.edu.itba.paw.model.User;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.stream.Collectors;

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
        Product product,
        Condition condition,
        boolean acceptsTrade,
        boolean acceptsShipping,
        String description,
        List<Long> imageIds
    ) {
        final User creatorRef = em.getReference(User.class, creator.getId());
        final Product productRef = em.getReference(Product.class, product.getId());
        final List<Image> images = imageIds != null
                ? imageIds.stream().map(id -> em.getReference(Image.class, id)).collect(Collectors.toCollection(ArrayList::new))
                : new ArrayList<>();

        final Listing listing = Listing.builder()
                .title(title)
                .price(price.getAmount())
                .creator(creatorRef)
                .product(productRef)
                .condition(condition)
                .acceptsTrade(acceptsTrade)
                .acceptsShipping(acceptsShipping)
                .description(description)
                .status(ListingStatus.ACTIVE)
                .images(images)
                .build();
        em.persist(listing);
        em.flush(); // TODO: [JPA Migration Cleanup]
        return listing;
    }

    @Override
    public Page<Listing> search(ListingFilter filter) {
        final StringBuilder whereClause = new StringBuilder(" WHERE 1=1");
        final Map<String, Object> params = new HashMap<>();

        if (filter.getCategoryId() != null) {
            whereClause.append(" AND l.product.subcategory.category.id = :categoryId");
            params.put("categoryId", filter.getCategoryId());
        }
        if (filter.getSubcategoryId() != null) {
            whereClause.append(" AND l.product.subcategory.id = :subcategoryId");
            params.put("subcategoryId", filter.getSubcategoryId());
        }
        if (filter.getMinPrice() != null) {
            whereClause.append(" AND l.price >= :minPrice");
            params.put("minPrice", filter.getMinPrice());
        }
        if (filter.getMaxPrice() != null) {
            whereClause.append(" AND l.price <= :maxPrice");
            params.put("maxPrice", filter.getMaxPrice());
        }
        if (filter.getCondition() != null) {
            whereClause.append(" AND l.condition = :condition");
            params.put("condition", filter.getCondition());
        }
        if (filter.getAcceptsTrade() != null) {
            whereClause.append(" AND l.acceptsTrade = :acceptsTrade");
            params.put("acceptsTrade", filter.getAcceptsTrade());
        }
        if (filter.getAcceptsShipping() != null) {
            whereClause.append(" AND l.acceptsShipping = :acceptsShipping");
            params.put("acceptsShipping", filter.getAcceptsShipping());
        }
        if (filter.getStatus() != null) {
            whereClause.append(" AND l.status = :status");
            params.put("status", filter.getStatus());
        }
        if (filter.getCreatorId() != null) {
            whereClause.append(" AND l.creator.id = :creatorId");
            params.put("creatorId", filter.getCreatorId());
        }
        if (filter.getProvinceId() != null) {
            whereClause.append(" AND l.creator.province.id = :provinceId");
            params.put("provinceId", filter.getProvinceId());
        }
        if (filter.getQuery() != null && !filter.getQuery().isBlank()) {
            whereClause.append(" AND (LOWER(l.title) LIKE :query OR LOWER(l.description) LIKE :query)");
            params.put("query", "%" + escapeLike(filter.getQuery().trim().toLowerCase()) + "%");
        }
        if (Boolean.TRUE.equals(filter.getHasActiveOffers())) {
            whereClause.append(" AND EXISTS (SELECT 1 FROM Offer o WHERE o.listing.id = l.id AND o.status = :pendingOfferStatus)");
            params.put("pendingOfferStatus", OfferStatus.PENDING);
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

        final String orderBy = resolveOrderBy(filter.getSort());
        final TypedQuery<Listing> query = em.createQuery(
                "FROM Listing l" + whereClause + " ORDER BY " + orderBy, Listing.class);
        params.forEach(query::setParameter);
        query.setFirstResult((page - 1) * pageSize);
        query.setMaxResults(pageSize);

        return new Page<>(query.getResultList(), page, pageSize, totalCount);
    }

    private static final Map<ListingSort, String> LISTING_SORT_TO_SQL = new HashMap<>(Map.ofEntries(
            Map.entry(ListingSort.PRICE_ASC, "l.price ASC, l.id DESC"),
            Map.entry(ListingSort.PRICE_DESC, "l.price DESC, l.id DESC"),
            Map.entry(ListingSort.NAME_ASC, "l.title ASC, l.id DESC"),
            Map.entry(ListingSort.NAME_DESC, "l.title DESC, l.id DESC"),
            Map.entry(ListingSort.MOST_OFFERS, "l.pendingOffersCount DESC, l.id DESC"),
            Map.entry(ListingSort.RECENT_OFFERS, "l.recentPendingOfferAt DESC NULLS LAST, l.id DESC")
    ));

    private static final String LISTING_SORT_TO_SQL_DEFAULT = "l.id DESC";

    private static String resolveOrderBy(final ListingSort sort) {
        return LISTING_SORT_TO_SQL.getOrDefault(sort, LISTING_SORT_TO_SQL_DEFAULT);
    }

    private static String escapeLike(String input) {
        return input.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_");
    }

    @Override
    public ListingStatus purchase(Long id, Long buyerId) {
        final Listing listing = em.find(Listing.class, id);
        if (listing != null) {
            listing.setStatus(ListingStatus.SOLD);
            em.flush(); // TODO: [JPA Migration Cleanup]
        }
        return ListingStatus.SOLD;
    }

    @Override
    public ListingStatus pendingTransaction(Long id, Long buyerId) {
        final Listing listing = em.find(Listing.class, id);
        if (listing != null) {
            listing.setStatus(ListingStatus.PENDING_TRANSACTION);
            em.flush(); // TODO: [JPA Migration Cleanup]
        }
        return ListingStatus.PENDING_TRANSACTION;
    }

    @Override
    public void cancel(Long id) {
        final Listing listing = em.find(Listing.class, id);
        if (listing != null) {
            listing.setStatus(ListingStatus.CANCELED);
            em.flush(); // TODO: [JPA Migration Cleanup]
        }
    }

    @Override
    public void updateStatus(Long id, ListingStatus status) {
        final Listing listing = em.find(Listing.class, id);
        if (listing != null) {
            listing.setStatus(status);
            em.flush(); // TODO: [JPA Migration Cleanup]
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
        em.flush(); // TODO: [JPA Migration Cleanup]
    }

    @Override
    public Listing update(
        Long id,
        String title,
        Price price,
        Product product,
        Condition condition,
        boolean acceptsTrade,
        boolean acceptsShipping,
        String description
    ) {
        final Listing listing = em.find(Listing.class, id);
        if (listing == null) {
            throw new NoSuchElementException("Listing not found with id: " + id);
        }
        listing.setTitle(title);
        listing.setPrice(price.getAmount());
        listing.setProduct(em.getReference(Product.class, product.getId()));
        listing.setCondition(condition);
        listing.setAcceptsTrade(acceptsTrade);
        listing.setAcceptsShipping(acceptsShipping);
        listing.setDescription(description);
        em.flush(); // TODO: [JPA Migration Cleanup]
        return listing;
    }
}
