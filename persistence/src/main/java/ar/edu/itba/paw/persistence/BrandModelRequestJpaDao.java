package ar.edu.itba.paw.persistence;

import ar.edu.itba.paw.model.Brand;
import ar.edu.itba.paw.model.BrandModelRequest;
import ar.edu.itba.paw.model.BrandModelRequestType;
import ar.edu.itba.paw.model.Page;
import ar.edu.itba.paw.model.Subcategory;
import ar.edu.itba.paw.model.User;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;
import java.util.Optional;

@Repository
public class BrandModelRequestJpaDao implements BrandModelRequestDao {

    @PersistenceContext
    private EntityManager em;

    @Override
    public Optional<BrandModelRequest> getById(Long id) {
        return Optional.ofNullable(em.find(BrandModelRequest.class, id));
    }

    @Override
    public Page<BrandModelRequest> getMatching(int page, int pageSize) {
        final long totalCount = em.createQuery(
                "SELECT COUNT(r) FROM BrandModelRequest r",
                Long.class)
                .getSingleResult();

        if (totalCount == 0) {
            return new Page<>(List.of(), page, pageSize, 0);
        }

        final List<Long> requestIds = ((List<?>) em.createNativeQuery(
                "SELECT request_id FROM brand_model_requests"
                + " ORDER BY created_at DESC, request_id DESC")
                .setFirstResult((page - 1) * pageSize)
                .setMaxResults(pageSize)
                .getResultList())
                .stream()
                .map(row -> ((Number) row).longValue())
                .toList();

        if (requestIds.isEmpty()) {
            return new Page<>(List.of(), page, pageSize, totalCount);
        }

        final List<BrandModelRequest> content = em.createQuery(
                "SELECT r FROM BrandModelRequest r"
                + " LEFT JOIN FETCH r.brand"
                + " JOIN FETCH r.requester"
                + " JOIN FETCH r.subcategory s"
                + " JOIN FETCH s.category"
                + " WHERE r.id IN :ids"
                + " ORDER BY r.createdAt DESC, r.id DESC",
                BrandModelRequest.class)
                .setParameter("ids", requestIds)
                .getResultList();

        return new Page<>(content, page, pageSize, totalCount);
    }

    @Override
    public BrandModelRequest create(
            BrandModelRequestType type,
            Brand brand,
            String brandName,
            String modelName,
            Subcategory subcategory,
            User requester
    ) {
        final BrandModelRequest request = BrandModelRequest.builder()
                .type(type)
                .brand(brand != null ? em.getReference(Brand.class, brand.getId()) : null)
                .brandName(brandName)
                .modelName(modelName)
                .subcategory(em.getReference(Subcategory.class, subcategory.getId()))
                .requester(em.getReference(User.class, requester.getId()))
                .build();
        em.persist(request);
        return request;
    }

    @Override
    public boolean delete(Long id) {
        final BrandModelRequest request = em.find(BrandModelRequest.class, id);
        if (request == null) {
            return false;
        }
        em.remove(request);
        return true;
    }
}
