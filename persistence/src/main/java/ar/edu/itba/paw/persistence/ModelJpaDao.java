package ar.edu.itba.paw.persistence;

import ar.edu.itba.paw.model.Brand;
import ar.edu.itba.paw.model.Condition;
import ar.edu.itba.paw.model.ListingStatus;
import ar.edu.itba.paw.model.Model;
import ar.edu.itba.paw.model.ModelListingInfo;
import ar.edu.itba.paw.model.ModelListingInfoSummary;
import ar.edu.itba.paw.model.Page;
import ar.edu.itba.paw.model.Price;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public class ModelJpaDao implements ModelDao {

    @PersistenceContext
    private EntityManager em;

    @Override
    public Optional<Model> getById(Long id) {
        return Optional.ofNullable(em.find(Model.class, id));
    }

    @Override
    public List<Model> getBySubcategoryIdAndBrandId(Long subcategoryId, Long brandId) {
        return em.createQuery(
                "FROM Model m WHERE m.subcategory.id = :subcategoryId AND m.brand.id = :brandId ORDER BY m.name",
                Model.class)
                .setParameter("subcategoryId", subcategoryId)
                .setParameter("brandId", brandId)
                .getResultList();
    }

    @Override
    public Page<ModelListingInfo> getListingInfoSummary(
        final Long brandId, final Long modelId,
        final Integer fromYear, final Integer toYear,
        final Long subcategoryId,
        final int page, final int pageSize
    ) {
        final var where = new StringBuilder(
                " WHERE m.subcategory_id = ?1"
                + " AND EXISTS (SELECT 1 FROM listings l WHERE l.model_id = m.model_id AND l.status = ?2");
        final List<Object> params = new ArrayList<>();
        params.add(subcategoryId);
        params.add(ListingStatus.ACTIVE.name());

        if (brandId != null) {
            where.append(" AND m.brand_id = ?").append(params.size() + 1);
            params.add(brandId);
        }
        if (modelId != null) {
            where.append(" AND m.model_id = ?").append(params.size() + 1);
            params.add(modelId);
        }
        if (fromYear != null) {
            where.append(" AND l.year >= ?").append(params.size() + 1);
            params.add(fromYear);
        }
        if (toYear != null) {
            where.append(" AND l.year <= ?").append(params.size() + 1);
            params.add(toYear);
        }
        where.append(")");

        final var countQuery = em.createNativeQuery("SELECT COUNT(*) FROM models m" + where);
        bindParams(countQuery, params);
        final long totalCount = ((Number) countQuery.getSingleResult()).longValue();

        if (totalCount == 0) {
            return new Page<>(List.of(), page, pageSize, 0);
        }

        final var idQuery = em.createNativeQuery(
                "SELECT m.model_id FROM models m"
                + " INNER JOIN brands b ON b.brand_id = m.brand_id"
                + where
                + " ORDER BY b.brand_name, m.model_name, m.model_id");
        bindParams(idQuery, params);
        idQuery.setFirstResult((page - 1) * pageSize);
        idQuery.setMaxResults(pageSize);

        final List<Long> modelIds = ((List<?>) idQuery.getResultList()).stream()
                .map((row) -> ((Number) row).longValue())
                .toList();

        if (modelIds.isEmpty()) {
            return new Page<>(List.of(), page, pageSize, totalCount);
        }

        final var yearWhere = new StringBuilder();
        if (fromYear != null) yearWhere.append(" AND l.year >= :yearFrom");
        if (toYear != null) yearWhere.append(" AND l.year <= :yearTo");

        final var query = em.createQuery(
                "SELECT b, m, l.condition, COUNT(l.condition), MIN(l.price), MAX(l.price), AVG(l.price)"
                + " FROM Listing l JOIN l.model m JOIN m.brand b"
                + " WHERE m.id IN :modelIds AND l.status = :status" + yearWhere
                + " GROUP BY b, m, l.condition"
                + " ORDER BY b.name, m.name, l.condition",
                Object[].class)
                .setParameter("modelIds", modelIds)
                .setParameter("status", ListingStatus.ACTIVE);
        if (fromYear != null) query.setParameter("yearFrom", fromYear);
        if (toYear != null) query.setParameter("yearTo", toYear);

        final List<ModelListingInfo> content = query.getResultStream()
                .map((fields) -> ModelListingInfo.builder()
                    .brand((Brand) fields[0])
                    .model((Model) fields[1])
                    .condition((Condition) fields[2])
                    .listingCount((Long) fields[3])
                    .minPrice((Price) fields[4])
                    .maxPrice((Price) fields[5])
                    .avgPrice(new Price(new BigDecimal((Double) fields[6])))
                    .build()
                )
                .toList();

        return new Page<>(content, page, pageSize, totalCount);
    }

    @Override
    public Optional<ModelListingInfoSummary> getListingInfoSummaryForModel(final Long modelId) {
        final Model model = em.find(Model.class, modelId);
        if (model == null) {
            return Optional.empty();
        }

        final List<ModelListingInfo> infos = em.createQuery(
                "SELECT l.condition, COUNT(l), MIN(l.price.amount), MAX(l.price.amount), AVG(l.price.amount)"
                + " FROM Listing l"
                + " WHERE l.model.id = :modelId AND l.status = :status"
                + " GROUP BY l.condition",
                Object[].class)
                .setParameter("modelId", modelId)
                .setParameter("status", ListingStatus.ACTIVE)
                .getResultList()
                .stream()
                .map((fields) -> ModelListingInfo.builder()
                    .brand(model.getBrand())
                    .model(model)
                    .condition((Condition) fields[0])
                    .listingCount((Long) fields[1])
                    .minPrice(new Price((BigDecimal) fields[2]))
                    .maxPrice(new Price((BigDecimal) fields[3]))
                    .avgPrice(new Price(BigDecimal.valueOf((Double) fields[4])))
                    .build()
                )
                .toList();

        if (infos.isEmpty()) {
            return Optional.empty();
        }

        return Optional.of(ModelListingInfoSummary.builder()
            .brand(model.getBrand())
            .model(model)
            .listingInfoByCondition(infos)
            .build());
    }

    private static void bindParams(final Query query, final List<Object> params) {
        for (int i = 0; i < params.size(); i++) {
            query.setParameter(i + 1, params.get(i));
        }
    }
}
