package ar.edu.itba.paw.persistence;

import ar.edu.itba.paw.model.Offer;
import ar.edu.itba.paw.model.OfferRating;
import ar.edu.itba.paw.model.Page;
import ar.edu.itba.paw.model.Rating;
import ar.edu.itba.paw.model.RatingFilter;
import ar.edu.itba.paw.model.RatingRole;
import ar.edu.itba.paw.model.User;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class RatingJpaDao implements RatingDao {

    @PersistenceContext
    private EntityManager em;

    @Override
    public Page<Rating> search(RatingFilter filter) {
        final StringBuilder nativeWhere = new StringBuilder(" WHERE 1=1");
        final Map<String, Object> nativeParams = new HashMap<>();

        if (filter.getRatedId() != null) {
            nativeWhere.append(" AND r.rated_id = :ratedId");
            nativeParams.put("ratedId", filter.getRatedId());
        }

        if (filter.getRoles() != null && !filter.getRoles().isEmpty()) {
            nativeWhere.append(" AND r.role IN (:roles)");
            nativeParams.put("roles", filter.getRoles().stream().map(Enum::name).toList());
        }

        if (filter.getTypes() != null && !filter.getTypes().isEmpty()) {
            nativeWhere.append(" AND r.type IN (:types)");
            nativeParams.put("types", filter.getTypes().stream().map(Enum::name).toList());
        }

        final Query countQuery = em.createNativeQuery("SELECT COUNT(*) FROM ratings r" + nativeWhere);
        nativeParams.forEach(countQuery::setParameter);
        final long totalCount = ((Number) countQuery.getSingleResult()).longValue();

        final int page = filter.getPage();
        final int pageSize = filter.getPageSize();
        if (totalCount == 0) {
            return new Page<>(List.of(), page, pageSize, 0);
        }

        // 1+1 pagination: page only rating IDs in a native query, then load those entities.
        final Query idQuery = em.createNativeQuery(
                "SELECT r.rating_id FROM ratings r" + nativeWhere
                + " ORDER BY r.created_at DESC, r.rating_id DESC")
                .setFirstResult((page - 1) * pageSize)
                .setMaxResults(pageSize);
        nativeParams.forEach(idQuery::setParameter);

        final List<Long> ids = ((List<?>) idQuery.getResultList()).stream()
                .map(row -> ((Number) row).longValue())
                .toList();
        if (ids.isEmpty()) {
            return new Page<>(List.of(), page, pageSize, totalCount);
        }

        final List<Rating> content = em.createQuery(
                "SELECT r FROM Rating r"
                + " JOIN FETCH r.creator"
                + " JOIN FETCH r.offer o"
                + " JOIN FETCH o.listing"
                + " WHERE r.id IN :ids"
                + " ORDER BY r.createdAt DESC, r.id DESC", Rating.class)
                .setParameter("ids", ids)
                .getResultList();

        return new Page<>(content, page, pageSize, totalCount);
    }

    @Override
    public Rating create(Long creatorId, Long ratedId, Long offerId, RatingRole role, OfferRating type, String reviewText, Instant createdAt) {
        final User creator = em.getReference(User.class, creatorId);
        final User rated = em.getReference(User.class, ratedId);
        final Offer offer = em.getReference(Offer.class, offerId);
        final Rating rating = Rating.builder()
                .creator(creator)
                .rated(rated)
                .offer(offer)
                .role(role)
                .type(type)
                .reviewText(reviewText)
                .createdAt(createdAt)
                .build();
        em.persist(rating);
        return rating;
    }
}
