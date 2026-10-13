package ar.edu.itba.paw.persistence;

import ar.edu.itba.paw.model.File;
import ar.edu.itba.paw.model.Listing;
import ar.edu.itba.paw.model.Offer;
import ar.edu.itba.paw.model.OfferFilter;
import ar.edu.itba.paw.model.OfferRating;
import ar.edu.itba.paw.model.OfferStatus;
import ar.edu.itba.paw.model.Page;
import ar.edu.itba.paw.model.User;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Repository
public class OfferJpaDao implements OfferDao {

    @PersistenceContext
    private EntityManager em;

    @Override
    public Optional<Offer> getById(Long id) {
        final Offer offer = em.find(Offer.class, id);
        if (offer != null) {
            populateTransientFlags(List.of(offer));
        }
        return Optional.ofNullable(offer);
    }

    @Override
    public Optional<Offer> getByListingAndBuyer(Listing listing, User buyer) {
        final List<Offer> result = em.createQuery(
                "FROM Offer o WHERE o.listing.id = :listingId AND o.buyer.id = :buyerId AND o.status IN (:statuses) ORDER BY o.id DESC",
                Offer.class)
                .setParameter("listingId", listing.getId())
                .setParameter("buyerId", buyer.getId())
                .setParameter("statuses", List.of(OfferStatus.PENDING, OfferStatus.PENDING_PAYMENT))
                .getResultList();
        if (result.isEmpty()) {
            return Optional.empty();
        }
        final Offer offer = result.getFirst();
        populateTransientFlags(List.of(offer));
        return Optional.of(offer);
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
                "FROM Offer o" + whereClause + " ORDER BY o.createdAt DESC", Offer.class);
        params.forEach(query::setParameter);
        query.setFirstResult((page - 1) * pageSize);
        query.setMaxResults(pageSize);

        final List<Offer> content = query.getResultList();
        populateTransientFlags(content);
        return new Page<>(content, page, pageSize, totalCount);
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
        populateTransientFlags(List.of(offer));
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
        final String jpql = exceptOfferId != null
                ? "FROM Offer o WHERE o.listing.id = :listingId AND o.status = :status AND o.id <> :exceptOfferId"
                : "FROM Offer o WHERE o.listing.id = :listingId AND o.status = :status";
        final TypedQuery<Offer> query = em.createQuery(jpql, Offer.class)
                .setParameter("listingId", listingId)
                .setParameter("status", OfferStatus.PENDING);
        if (exceptOfferId != null) {
            query.setParameter("exceptOfferId", exceptOfferId);
        }
        final List<Offer> toReject = query.getResultList();
        for (Offer o : toReject) {
            o.setStatus(OfferStatus.REJECTED);
        }
        return toReject;
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
    public List<Offer> getAcceptedUnratedBefore(Instant cutoff) {
        final List<Offer> offers = em.createQuery(
                "FROM Offer o WHERE o.status = :status AND o.acceptedAt IS NOT NULL AND o.acceptedAt < :cutoff AND (o.sellerRating IS NULL OR o.buyerRating IS NULL)",
                Offer.class)
                .setParameter("status", OfferStatus.ACCEPTED)
                .setParameter("cutoff", cutoff)
                .getResultList();
        populateTransientFlags(offers);
        return offers;
    }

    // TODO: This should not be here. Move this to Service layer because this is business logic, not DAO logic.
    private void populateTransientFlags(List<Offer> offers) {
        if (offers == null || offers.isEmpty()) return;
        final Set<Long> listingIds = offers.stream()
                .map(o -> o.getListing().getId())
                .collect(Collectors.toSet());

        final List<Object[]> pending = em.createQuery(
                "SELECT o.listing.id, o.id, o.amount FROM Offer o WHERE o.listing.id IN :listingIds AND o.status = :status",
                Object[].class)
                .setParameter("listingIds", listingIds)
                .setParameter("status", OfferStatus.PENDING)
                .getResultList();

        for (Offer offer : offers) {
            boolean hasOther = false;
            boolean hasBetter = false;
            final Long listingId = offer.getListing().getId();
            final Long offerId = offer.getId();
            final BigDecimal amount = offer.getAmount();

            for (Object[] row : pending) {
                final Long rowListingId = (Long) row[0];
                final Long rowOfferId = (Long) row[1];
                final BigDecimal rowAmount = (BigDecimal) row[2];

                if (rowListingId.equals(listingId) && !rowOfferId.equals(offerId)) {
                    hasOther = true;
                    if (rowAmount != null && amount != null && rowAmount.compareTo(amount) > 0) {
                        hasBetter = true;
                    }
                }
            }
            offer.setHasOtherOffers(hasOther);
            offer.setHasBetterOffers(hasBetter);
        }
    }
}
