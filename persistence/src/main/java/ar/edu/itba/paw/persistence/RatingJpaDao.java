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
import javax.persistence.TypedQuery;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public class RatingJpaDao implements RatingDao {

    @PersistenceContext
    private EntityManager em;

    @Override
    public Optional<Rating> getById(Long id) {
        return Optional.ofNullable(em.find(Rating.class, id));
    }

    @Override
    public Page<Rating> search(RatingFilter filter) {
        final StringBuilder whereClause = new StringBuilder(" WHERE 1=1");
        final Map<String, Object> params = new HashMap<>();

        if (filter.getRatedId() != null) {
            whereClause.append(" AND r.rated.id = :ratedId");
            params.put("ratedId", filter.getRatedId());
        }

        if (filter.getRoles() != null && !filter.getRoles().isEmpty()) {
            whereClause.append(" AND r.role IN (:roles)");
            params.put("roles", filter.getRoles());
        }

        if (filter.getTypes() != null && !filter.getTypes().isEmpty()) {
            whereClause.append(" AND r.type IN (:types)");
            params.put("types", filter.getTypes());
        }

        final TypedQuery<Long> countQuery = em.createQuery(
                "SELECT COUNT(r) FROM Rating r" + whereClause, Long.class);
        params.forEach(countQuery::setParameter);
        final long totalCount = countQuery.getSingleResult();

        final int page = filter.getPage();
        final int pageSize = filter.getPageSize();
        if (totalCount == 0) {
            return new Page<>(List.of(), page, pageSize, 0);
        }

        final TypedQuery<Rating> query = em.createQuery(
                "FROM Rating r" + whereClause + " ORDER BY r.createdAt DESC", Rating.class);
        params.forEach(query::setParameter);
        query.setFirstResult((page - 1) * pageSize);
        query.setMaxResults(pageSize);

        return new Page<>(query.getResultList(), page, pageSize, totalCount);
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
        em.flush(); // TODO: [JPA Migration Cleanup]
        return rating;
    }
}
