package ar.edu.itba.paw.persistence;

import ar.edu.itba.paw.model.File;
import ar.edu.itba.paw.model.Listing;
import ar.edu.itba.paw.model.Offer;
import ar.edu.itba.paw.model.OfferFilter;
import ar.edu.itba.paw.model.OfferPendingCount;
import ar.edu.itba.paw.model.OfferRating;
import ar.edu.itba.paw.model.OfferStatus;
import ar.edu.itba.paw.model.OfferStatusCount;
import ar.edu.itba.paw.model.OfferStatusGroup;
import ar.edu.itba.paw.model.Page;
import ar.edu.itba.paw.model.User;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Collection;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
public class OfferJpaDao implements OfferDao {

    @PersistenceContext
    private EntityManager em;

    @Override
    public Optional<Offer> getById(Long id) {
        return Optional.ofNullable(em.find(Offer.class, id));
    }

    @Override
    public Optional<Offer> getByListingAndBuyer(Listing listing, User buyer) {
        return em.createQuery(
                "FROM Offer o WHERE o.listing.id = :listingId AND o.buyer.id = :buyerId AND o.status IN (:statuses) ORDER BY o.id DESC",
                Offer.class)
                .setParameter("listingId", listing.getId())
                .setParameter("buyerId", buyer.getId())
                .setParameter("statuses", List.of(OfferStatus.PENDING, OfferStatus.PENDING_PAYMENT))
                .getResultList().stream().findFirst();
    }

    @Override
    public Page<Offer> search(OfferFilter filter) {
        final StringBuilder whereClause = new StringBuilder(" WHERE 1=1");
        final Map<String, Object> params = new HashMap<>();

        if (filter.getBuyerId() != null) {
            whereClause.append(" AND o.buyer.id = :buyerId");
            params.put("buyerId", filter.getBuyerId());
        }

        if (filter.getSellerId() != null) {
            whereClause.append(" AND o.listing.creator.id = :sellerId");
            params.put("sellerId", filter.getSellerId());
        }

        if (filter.getStatus() != null && !filter.getStatus().isEmpty()) {
            whereClause.append(" AND o.status IN (:statuses)");
            params.put("statuses", filter.getStatus());
        }

        final TypedQuery<Long> countQuery = em.createQuery(
                "SELECT COUNT(o) FROM Offer o" + whereClause, Long.class);
        params.forEach(countQuery::setParameter);
        final long totalCount = countQuery.getSingleResult();

        final int page = filter.getPage();
        final int pageSize = filter.getPageSize();
        if (totalCount == 0) {
            return new Page<>(List.of(), page, pageSize, 0);
        }

        final TypedQuery<Offer> query = em.createQuery(
                "FROM Offer o" + whereClause + " ORDER BY o.createdAt DESC, o.id DESC", Offer.class);
        params.forEach(query::setParameter);
        query.setFirstResult((page - 1) * pageSize);
        query.setMaxResults(pageSize);

        final List<Offer> content = query.getResultList();
        return new Page<>(content, page, pageSize, totalCount);
    }

    @Override
    public Map<Long, OfferPendingCount> getPendingCounts(Collection<Long> offerIds) {
        if (offerIds == null || offerIds.isEmpty()) {
            return Map.of();
        }
        return em.createQuery(
                "SELECT new ar.edu.itba.paw.model.OfferPendingCount(o.id, " +
                    "(SELECT COUNT(p) FROM Offer p WHERE p.listing.id = o.listing.id AND p.status = :pending AND p.id <> o.id), " +
                    "(SELECT COUNT(p) FROM Offer p WHERE p.listing.id = o.listing.id AND p.status = :pending AND p.id <> o.id AND p.amount > o.amount)) " +
                    "FROM Offer o WHERE o.id IN :offerIds",
                OfferPendingCount.class)
                .setParameter("pending", OfferStatus.PENDING)
                .setParameter("offerIds", offerIds)
                .getResultStream()
                .collect(Collectors.toMap(OfferPendingCount::offerId, count -> count));
    }

    @Override
    public Offer create(Long listingId, User buyer, BigDecimal amount, Boolean isFullPrice, OfferStatus status, String message, Instant createdAt, Long offeredListingId) {
        final Listing listing = em.getReference(Listing.class, listingId);
        final User buyerRef = em.getReference(User.class, buyer.getId());
        final Listing offeredListing = offeredListingId != null ? em.getReference(Listing.class, offeredListingId) : null;
        final Offer offer = Offer.builder()
                .listing(listing)
                .buyer(buyerRef)
                .amount(amount)
                .isFullPrice(isFullPrice)
                .status(status)
                .message(message)
                .createdAt(createdAt)
                .offeredListing(offeredListing)
                .build();
        em.persist(offer);
        return offer;
    }

    @Override
    public boolean updateStatus(Long offerId, OfferStatus status) {
        final Offer offer = em.find(Offer.class, offerId);
        if (offer == null) return false;
        offer.setStatus(status);
        return true;
    }

    @Override
    public boolean withdraw(Long offerId, Long buyerId) {
        final Offer offer = em.find(Offer.class, offerId);
        if (offer == null || !offer.getBuyer().getId().equals(buyerId) || offer.getStatus() != OfferStatus.PENDING) {
            return false;
        }
        offer.setStatus(OfferStatus.WITHDRAWN);
        return true;
    }

    @Override
    public List<Offer> rejectPendingOffers(Long listingId, Long exceptOfferId) {
        final String pendingClause = exceptOfferId != null
                ? " FROM Offer o WHERE o.listing.id = :listingId AND o.status = :status AND o.id <> :exceptOfferId"
                : " FROM Offer o WHERE o.listing.id = :listingId AND o.status = :status";
        final TypedQuery<Long> idQuery = em.createQuery("SELECT o.id" + pendingClause, Long.class)
                .setParameter("listingId", listingId)
                .setParameter("status", OfferStatus.PENDING);
        if (exceptOfferId != null) {
            idQuery.setParameter("exceptOfferId", exceptOfferId);
        }
        final List<Long> ids = idQuery.getResultList();
        if (ids.isEmpty()) {
            return List.of();
        }

        final Query update = em.createQuery(
                        "UPDATE Offer o SET o.status = :rejected WHERE o.id IN :ids")
                .setParameter("rejected", OfferStatus.REJECTED)
                .setParameter("ids", ids);
        update.executeUpdate();

        return em.createQuery("FROM Offer o WHERE o.id IN :ids", Offer.class)
                .setParameter("ids", ids)
                .getResultList();
    }

    @Override
    public int rejectAllForParticipant(Long userId) {
        return em.createQuery(
                        "UPDATE Offer o SET o.status = :rejected"
                        + " WHERE o.status IN :cancellableStatuses"
                        + " AND (o.buyer.id = :userId"
                        + "   OR o.listing.id IN (SELECT l.id FROM Listing l WHERE l.creator.id = :userId))")
                .setParameter("rejected", OfferStatus.REJECTED)
                .setParameter("cancellableStatuses", List.of(OfferStatus.PENDING, OfferStatus.PENDING_PAYMENT))
                .setParameter("userId", userId)
                .executeUpdate();
    }

    @Override
    public int countPendingBySeller(User seller) {
        return em.createQuery("SELECT COUNT(o) FROM Offer o WHERE o.listing.creator.id = :sellerId AND o.status = :status", Long.class)
                .setParameter("sellerId", seller.getId())
                .setParameter("status", OfferStatus.PENDING)
                .getSingleResult()
                .intValue();
    }

    @Override
    public Map<OfferStatusGroup, Long> countByStatus(User currentUser, Boolean incoming) {
        var where = " WHERE "
                  + (incoming ? "o.listing.creator.id" : "o.buyer.id")
                  + " = :userId"
                  + " AND (NOT (o.status IN (:resolvedStatuses)) OR (o.status = :acceptedStatus AND " + (incoming ? "o.buyerRating" : "o.sellerRating") + " IS NULL))";

        var query = "SELECT new ar.edu.itba.paw.model.OfferStatusCount(o.status, COUNT(o)) FROM Offer o"
                  + where
                  + " GROUP BY o.status";

        Map<OfferStatusGroup, Long> results = new EnumMap<>(OfferStatusGroup.class);
        em.createQuery(query, OfferStatusCount.class)
                .setParameter("userId", currentUser.getId())
                .setParameter("resolvedStatuses", OfferStatusGroup.RESOLVED.toStatusList())
                .setParameter("acceptedStatus", OfferStatus.ACCEPTED)
                .getResultStream()
                .forEach((result) -> results.merge(
                        OfferStatusGroup.fromStatus(result.status()),
                        result.count(),
                        Long::sum
                ));

        return results;
    }

    @Override
    public boolean updateProofOfPaymentId(Long offerId, Long proofOfPaymentId) {
        final Offer offer = em.find(Offer.class, offerId);
        if (offer == null) return false;
        final File file = proofOfPaymentId != null ? em.getReference(File.class, proofOfPaymentId) : null;
        offer.setProofOfPayment(file);
        return true;
    }

    @Override
    public boolean updateProofOfShipping(Long offerId, Long proofOfShippingId, String trackingNumber) {
        final Offer offer = em.find(Offer.class, offerId);
        if (offer == null) return false;
        final File file = proofOfShippingId != null ? em.getReference(File.class, proofOfShippingId) : null;
        offer.setProofOfShipping(file);
        offer.setTrackingNumber(trackingNumber);
        return true;
    }

    @Override
    public boolean markAccepted(Long offerId, Instant acceptedAt) {
        final Offer offer = em.find(Offer.class, offerId);
        if (offer == null) return false;
        offer.setStatus(OfferStatus.ACCEPTED);
        offer.setAcceptedAt(acceptedAt);
        return true;
    }

    @Override
    public boolean setSellerRating(Long offerId, OfferRating rating) {
        final Offer offer = em.find(Offer.class, offerId);
        if (offer == null || offer.getSellerRating().isPresent()) return false;
        offer.setSellerRating(rating);
        return true;
    }

    @Override
    public boolean setBuyerRating(Long offerId, OfferRating rating) {
        final Offer offer = em.find(Offer.class, offerId);
        if (offer == null || offer.getBuyerRating().isPresent()) return false;
        offer.setBuyerRating(rating);
        return true;
    }

    @Override
    public int setSellerRatings(List<Long> offerIds, OfferRating rating) {
        if (offerIds == null || offerIds.isEmpty()) return 0;
        return em.createQuery(
                "UPDATE Offer o SET o.sellerRating = :rating WHERE o.id IN :ids AND o.sellerRating IS NULL")
                .setParameter("rating", rating)
                .setParameter("ids", offerIds)
                .executeUpdate();
    }

    @Override
    public int setBuyerRatings(List<Long> offerIds, OfferRating rating) {
        if (offerIds == null || offerIds.isEmpty()) return 0;
        return em.createQuery(
                "UPDATE Offer o SET o.buyerRating = :rating WHERE o.id IN :ids AND o.buyerRating IS NULL")
                .setParameter("rating", rating)
                .setParameter("ids", offerIds)
                .executeUpdate();
    }

    @Override
    public List<Offer> getAcceptedUnratedBefore(Instant cutoff) {
        return em.createQuery(
                "FROM Offer o WHERE o.status = :status AND o.acceptedAt IS NOT NULL AND o.acceptedAt < :cutoff AND (o.sellerRating IS NULL OR o.buyerRating IS NULL)",
                Offer.class)
                .setParameter("status", OfferStatus.ACCEPTED)
                .setParameter("cutoff", cutoff)
                .getResultList();
    }
}
