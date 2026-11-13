package ar.edu.itba.paw.persistence;

import ar.edu.itba.paw.model.Brand;
import ar.edu.itba.paw.model.Condition;
import ar.edu.itba.paw.model.ListingStatus;
import ar.edu.itba.paw.model.Model;
import ar.edu.itba.paw.model.ModelInfo;
import ar.edu.itba.paw.model.ModelListingInfo;
import ar.edu.itba.paw.model.ModelSort;
import ar.edu.itba.paw.model.ModelListingInfoSummary;
import ar.edu.itba.paw.model.Page;
import ar.edu.itba.paw.model.Subcategory;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public class ModelJpaDao implements ModelDao {

    private static final String CONDITION_ORDER_CASE = buildConditionOrderCase();

    private static String buildConditionOrderCase() {
        final StringBuilder orderCase = new StringBuilder(" CASE l.condition");
        for (final Condition condition : Condition.values()) {
            orderCase.append(" WHEN '")
                    .append(condition.name())
                    .append("' THEN ")
                    .append(condition.ordinal());
        }
        return orderCase.append(" END").toString();
    }

    @PersistenceContext
    private EntityManager em;

    @Override
    public Optional<Model> getById(Long id) {
        return Optional.ofNullable(em.find(Model.class, id));
    }

    @Override
    public Optional<Model> getByNameAndBrandId(String name, Long brandId) {
        return em.createQuery(
                "FROM Model m WHERE LOWER(m.name) = LOWER(:name) AND m.brand.id = :brandId",
                Model.class)
                .setParameter("name", name)
                .setParameter("brandId", brandId)
                .getResultStream()
                .findFirst();
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
    public Page<ModelInfo> getWithInfo(Long brandId, Long subcategoryId, ModelSort sort, int page, int pageSize) {
        final StringBuilder where = new StringBuilder(" WHERE 1=1");
        if (brandId != null) {
            where.append(" AND b.id = :brandId");
        }
        if (subcategoryId != null) {
            where.append(" AND s.id = :subcategoryId");
        }

        final var countQuery = em.createQuery(
                "SELECT COUNT(m) FROM Model m JOIN m.brand b JOIN m.subcategory s" + where,
                Long.class);
        if (brandId != null) countQuery.setParameter("brandId", brandId);
        if (subcategoryId != null) countQuery.setParameter("subcategoryId", subcategoryId);
        final long totalCount = countQuery.getSingleResult();

        if (totalCount == 0) {
            return new Page<>(List.of(), page, pageSize, 0);
        }

        // This query doesn't use the 1+1 pattern: it's not needed, since it only returns individual
        // fields and aggregates and not entities and doesn't paginate fetch joins, so there is no
        // risk of lazy-fetching issues, in-memory pagination, or n+1 queries
        final String direction = sort.isAscending() ? "ASC" : "DESC";
        final String orderBy = sort.getKey().equals("id")
                ? "m.id " + direction
                : "m.name " + direction + ", m.id " + direction;
        final var query = em.createQuery(
                "SELECT new ar.edu.itba.paw.model.ModelInfo("
                + "m.id, m.name, b.id, b.name, s.id, s.name, COUNT(DISTINCT l.id))"
                + " FROM Model m JOIN m.brand b JOIN m.subcategory s"
                + " LEFT JOIN Listing l ON l.model.id = m.id"
                + where
                + " GROUP BY m.id, m.name, b.id, b.name, s.id, s.name"
                + " ORDER BY " + orderBy,
                ModelInfo.class);
        if (brandId != null) query.setParameter("brandId", brandId);
        if (subcategoryId != null) query.setParameter("subcategoryId", subcategoryId);
        query.setFirstResult((page - 1) * pageSize);
        query.setMaxResults(pageSize);

        final List<ModelInfo> content = query.getResultList();

        return new Page<>(content, page, pageSize, totalCount);
    }

    @Override
    public List<Model> getAllWithBrand() {
        return em.createQuery(
                "SELECT m FROM Model m JOIN FETCH m.brand ORDER BY m.brand.name, m.name, m.id",
                Model.class)
                .getResultList();
    }

    @Override
    public boolean existsByNameAndBrandId(String name, Long brandId) {
        return em.createQuery(
                "SELECT COUNT(m) FROM Model m WHERE LOWER(m.name) = LOWER(:name) AND m.brand.id = :brandId",
                Long.class)
                .setParameter("name", name)
                .setParameter("brandId", brandId)
                .getSingleResult() > 0;
    }

    @Override
    public boolean existsByNameAndBrandIdAndIdNot(String name, Long brandId, Long id) {
        return em.createQuery(
                "SELECT COUNT(m) FROM Model m WHERE LOWER(m.name) = LOWER(:name) AND m.brand.id = :brandId AND m.id <> :id",
                Long.class)
                .setParameter("name", name)
                .setParameter("brandId", brandId)
                .setParameter("id", id)
                .getSingleResult() > 0;
    }

    @Override
    public Model create(String name, Brand brand, Subcategory subcategory) {
        final Model model = Model.builder()
                .name(name)
                .brand(em.getReference(Brand.class, brand.getId()))
                .subcategory(em.getReference(Subcategory.class, subcategory.getId()))
                .build();
        em.persist(model);
        return model;
    }

    @Override
    public Optional<Model> update(Long id, String name, Brand brand, Subcategory subcategory) {
        final Model model = em.find(Model.class, id);
        if (model == null) {
            return Optional.empty();
        }
        model.setName(name);
        model.setBrand(em.getReference(Brand.class, brand.getId()));
        model.setSubcategory(em.getReference(Subcategory.class, subcategory.getId()));
        return Optional.of(model);
    }

    @Override
    public boolean deleteModel(Long id) {
        final Model model = em.find(Model.class, id);
        if (model == null) {
            return false;
        }
        em.remove(model);
        return true;
    }

    @Override
    public boolean hasListings(Long modelId) {
        return !em.createQuery(
                "SELECT l.id FROM Listing l WHERE l.model.id = :modelId",
                Long.class)
                .setParameter("modelId", modelId)
                .setMaxResults(1)
                .getResultList()
                .isEmpty();
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

        final var countWhere = new StringBuilder(
                " WHERE m.subcategory.id = :subcategoryId"
                + " AND EXISTS (SELECT l.id FROM Listing l WHERE l.model = m AND l.status = :status");
        if (fromYear != null) countWhere.append(" AND l.year >= :fromYear");
        if (toYear != null) countWhere.append(" AND l.year <= :toYear");
        countWhere.append(")");
        if (brandId != null) countWhere.append(" AND m.brand.id = :brandId");
        if (modelId != null) countWhere.append(" AND m.id = :modelId");

        final var countQuery = em.createQuery("SELECT COUNT(m) FROM Model m" + countWhere, Long.class)
                .setParameter("subcategoryId", subcategoryId)
                .setParameter("status", ListingStatus.ACTIVE);
        if (fromYear != null) countQuery.setParameter("fromYear", fromYear);
        if (toYear != null) countQuery.setParameter("toYear", toYear);
        if (brandId != null) countQuery.setParameter("brandId", brandId);
        if (modelId != null) countQuery.setParameter("modelId", modelId);
        final long totalCount = countQuery.getSingleResult();

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
                "SELECT new ar.edu.itba.paw.model.ModelListingInfo("
                + "b, m, l.condition, COUNT(l.condition),"
                + " MIN(l.price.amount), MAX(l.price.amount), AVG(l.price.amount))"
                + " FROM Listing l JOIN l.model m JOIN m.brand b"
                + " WHERE m.id IN :modelIds AND l.status = :status" + yearWhere
                + " GROUP BY b, m, l.condition"
                + " ORDER BY b.name, m.name," + CONDITION_ORDER_CASE,
                ModelListingInfo.class)
                .setParameter("modelIds", modelIds)
                .setParameter("status", ListingStatus.ACTIVE);
        if (fromYear != null) query.setParameter("yearFrom", fromYear);
        if (toYear != null) query.setParameter("yearTo", toYear);

        final List<ModelListingInfo> content = query.getResultList();

        return new Page<>(content, page, pageSize, totalCount);
    }

    @Override
    public int replaceBrand(Long fromBrandId, Long toBrandId) {
        em.createQuery(
                "DELETE FROM Model oldModel"
                + " WHERE oldModel.brand.id = :fromBrandId"
                + "   AND EXISTS ("
                + "     SELECT newModel.id FROM Model newModel"
                + "     WHERE newModel.brand.id = :toBrandId"
                + "       AND newModel.name = oldModel.name"
                + "   )")
                .setParameter("fromBrandId", fromBrandId)
                .setParameter("toBrandId", toBrandId)
                .executeUpdate();

        final Brand from = em.getReference(Brand.class, fromBrandId);
        final Brand to = em.getReference(Brand.class, toBrandId);
        return em.createQuery("UPDATE Model m SET m.brand = :to WHERE m.brand = :from")
                .setParameter("to", to)
                .setParameter("from", from)
                .executeUpdate();
    }

    public Optional<ModelListingInfoSummary> getListingInfoSummaryForModel(final Long modelId, final Long excludedListingId) {
        final Model model = em.find(Model.class, modelId);
        if (model == null) {
            return Optional.empty();
        }

        final String excludedClause = excludedListingId != null ? " AND l.id <> :excludedListingId" : "";
        final var query = em.createQuery(
                "SELECT new ar.edu.itba.paw.model.ModelListingInfo("
                + "b, m, l.condition, COUNT(l),"
                + " MIN(l.price.amount), MAX(l.price.amount), AVG(l.price.amount))"
                + " FROM Listing l JOIN l.model m JOIN m.brand b"
                + " WHERE m.id = :modelId AND l.status = :status" + excludedClause
                + " GROUP BY b, m, l.condition"
                + " ORDER BY" + CONDITION_ORDER_CASE,
                ModelListingInfo.class)
                .setParameter("modelId", modelId)
                .setParameter("status", ListingStatus.ACTIVE);
        if (excludedListingId != null) {
            query.setParameter("excludedListingId", excludedListingId);
        }

        final List<ModelListingInfo> infos = query.getResultList();

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
