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
    public Page<ModelInfo> getWithInfo(
            Long brandId,
            Long categoryId,
            Long subcategoryId,
            ModelSort sort,
            int page,
            int pageSize
    ) {
        final StringBuilder where = new StringBuilder(" WHERE 1=1");
        if (brandId != null) {
            where.append(" AND b.id = :brandId");
        }
        if (categoryId != null) {
            where.append(" AND s.category.id = :categoryId");
        }
        if (subcategoryId != null) {
            where.append(" AND s.id = :subcategoryId");
        }

        // The count and ID queries share the same native predicate so their filters cannot drift.
        final StringBuilder nativeWhere = new StringBuilder(" WHERE 1=1");
        if (brandId != null) nativeWhere.append(" AND m.brand_id = :brandId");
        if (categoryId != null) nativeWhere.append(" AND s.category_id = :categoryId");
        if (subcategoryId != null) nativeWhere.append(" AND m.subcategory_id = :subcategoryId");

        final String nativeFrom = " FROM models m"
                + " JOIN subcategories s ON s.subcategory_id = m.subcategory_id";

        final var countQuery = em.createNativeQuery("SELECT COUNT(*)" + nativeFrom + nativeWhere);
        if (brandId != null) countQuery.setParameter("brandId", brandId);
        if (categoryId != null) countQuery.setParameter("categoryId", categoryId);
        if (subcategoryId != null) countQuery.setParameter("subcategoryId", subcategoryId);
        final long totalCount = ((Number) countQuery.getSingleResult()).longValue();

        if (totalCount == 0) {
            return new Page<>(List.of(), page, pageSize, 0);
        }

        // 1+1 pagination: page only model IDs in a native query, then aggregate for those IDs.
        final String direction = sort.isAscending() ? "ASC" : "DESC";
        final String nativeOrderBy = sort.getKey().equals("id")
                ? "m.model_id " + direction
                : "m.model_name " + direction + ", m.model_id " + direction;
        final String orderBy = sort.getKey().equals("id")
                ? "m.id " + direction
                : "m.name " + direction + ", m.id " + direction;

        final var idQuery = em.createNativeQuery(
                "SELECT m.model_id" + nativeFrom + nativeWhere
                + " ORDER BY " + nativeOrderBy);
        if (brandId != null) idQuery.setParameter("brandId", brandId);
        if (categoryId != null) idQuery.setParameter("categoryId", categoryId);
        if (subcategoryId != null) idQuery.setParameter("subcategoryId", subcategoryId);
        idQuery.setFirstResult((page - 1) * pageSize);
        idQuery.setMaxResults(pageSize);

        final List<Long> modelIds = ((List<?>) idQuery.getResultList()).stream()
                .map(row -> ((Number) row).longValue())
                .toList();

        if (modelIds.isEmpty()) {
            return new Page<>(List.of(), page, pageSize, totalCount);
        }

        final var query = em.createQuery(
                "SELECT new ar.edu.itba.paw.model.ModelInfo("
                + "m.id, m.name, b.id, b.name, s.id, s.name, COUNT(DISTINCT l.id))"
                + " FROM Model m JOIN m.brand b JOIN m.subcategory s"
                + " LEFT JOIN Listing l ON l.model.id = m.id"
                + where
                + " AND m.id IN :modelIds"
                + " GROUP BY m.id, m.name, b.id, b.name, s.id, s.name"
                + " ORDER BY " + orderBy,
                ModelInfo.class);
        if (brandId != null) query.setParameter("brandId", brandId);
        if (categoryId != null) query.setParameter("categoryId", categoryId);
        if (subcategoryId != null) query.setParameter("subcategoryId", subcategoryId);
        query.setParameter("modelIds", modelIds);

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
        return Boolean.TRUE.equals(em.createNativeQuery(
                        "SELECT EXISTS (SELECT 1 FROM models m"
                        + " WHERE LOWER(m.model_name) = LOWER(:name) AND m.brand_id = :brandId)")
                .setParameter("name", name)
                .setParameter("brandId", brandId)
                .getSingleResult());
    }

    @Override
    public boolean existsByNameAndBrandIdAndIdNot(String name, Long brandId, Long id) {
        return Boolean.TRUE.equals(em.createNativeQuery(
                        "SELECT EXISTS (SELECT 1 FROM models m"
                        + " WHERE LOWER(m.model_name) = LOWER(:name) AND m.brand_id = :brandId"
                        + " AND m.model_id <> :id)")
                .setParameter("name", name)
                .setParameter("brandId", brandId)
                .setParameter("id", id)
                .getSingleResult());
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

    // Native EXISTS: JPQL has no select-level EXISTS, so the alternatives are a full COUNT or a
    // setMaxResults(1) query, which may trigger hidden in-memory pagination via implicit joins if
    // entities change. Native EXISTS short-circuits on the first match.
    @Override
    public boolean hasListings(Long modelId) {
        return Boolean.TRUE.equals(em.createNativeQuery(
                        "SELECT EXISTS (SELECT 1 FROM listings l WHERE l.model_id = :modelId)")
                .setParameter("modelId", modelId)
                .getSingleResult());
    }

    @Override
    public Page<ModelListingInfo> getListingInfoSummary(
        final Long brandId, final Long modelId,
        final Integer fromYear, final Integer toYear,
        final Long subcategoryId,
        final int page, final int pageSize
    ) {
        final var filters = buildListingInfoSummaryFilters(brandId, modelId, fromYear, toYear, subcategoryId);

        // The count and ID queries share the same native predicate so their filters cannot drift.
        final String nativeFrom = " FROM models m"
                + " INNER JOIN brands b ON b.brand_id = m.brand_id";

        final var countQuery = em.createNativeQuery("SELECT COUNT(*)" + nativeFrom + filters.nativeWhere());
        bindListingInfoSummaryParams(
                countQuery, subcategoryId, brandId, modelId, fromYear, toYear, ListingStatus.ACTIVE.name());
        final long totalCount = ((Number) countQuery.getSingleResult()).longValue();

        if (totalCount == 0) {
            return new Page<>(List.of(), page, pageSize, 0);
        }

        // 1+1 pagination: page only model IDs in a native query, then aggregate for those IDs.
        final var idQuery = em.createNativeQuery(
                "SELECT m.model_id" + nativeFrom + filters.nativeWhere()
                + " ORDER BY b.brand_name, m.model_name, m.model_id");
        bindListingInfoSummaryParams(
                idQuery, subcategoryId, brandId, modelId, fromYear, toYear, ListingStatus.ACTIVE.name());
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
                + "b.id, b.name, m.id, m.name, COUNT(l.condition), l.condition,"
                + " MIN(l.price.amount), MAX(l.price.amount))"
                + " FROM Listing l JOIN l.model m JOIN m.brand b"
                + " WHERE m.id IN :modelIds AND l.status = :status" + yearWhere
                + " GROUP BY b.id, b.name, m.id, m.name, l.condition"
                + " ORDER BY b.name, m.name," + CONDITION_ORDER_CASE,
                ModelListingInfo.class)
                .setParameter("modelIds", modelIds)
                .setParameter("status", ListingStatus.ACTIVE);
        if (fromYear != null) query.setParameter("yearFrom", fromYear);
        if (toYear != null) query.setParameter("yearTo", toYear);

        final List<ModelListingInfo> content = query.getResultList();

        return new Page<>(content, page, pageSize, totalCount);
    }

    // Bulk JPQL writes: they do not refresh models already in the persistence context. Re-fetch
    // (or refresh/detach) affected models before reading them later in this transaction.
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
        final String excludedClause = excludedListingId != null ? " AND l.id <> :excludedListingId" : "";
        final var query = em.createQuery(
                "SELECT new ar.edu.itba.paw.model.ModelListingInfo("
                + "b.id, b.name, m.id, m.name, COUNT(l), l.condition,"
                + " MIN(l.price.amount), MAX(l.price.amount))"
                + " FROM Listing l JOIN l.model m JOIN m.brand b"
                + " WHERE m.id = :modelId AND l.status = :status" + excludedClause
                + " GROUP BY b.id, b.name, m.id, m.name, l.condition"
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

        final ModelListingInfo first = infos.get(0);
        return Optional.of(ModelListingInfoSummary.builder()
            .brandId(first.getBrandId())
            .brandName(first.getBrandName())
            .modelId(first.getModelId())
            .modelName(first.getModelName())
            .listingInfoByCondition(infos)
            .build());
    }

    /*
     * Builds the native predicate shared by the count (1+1 step 0) and ID (1+1 step 1) queries.
     */
    private static ListingInfoSummaryFilters buildListingInfoSummaryFilters(
            final Long brandId,
            final Long modelId,
            final Integer fromYear,
            final Integer toYear,
            final Long subcategoryId
    ) {
        final var nativeSql = new StringBuilder(" WHERE m.subcategory_id = :subcategoryId");

        if (brandId != null) {
            nativeSql.append(" AND m.brand_id = :brandId");
        }
        if (modelId != null) {
            nativeSql.append(" AND m.model_id = :modelId");
        }

        nativeSql.append(" AND EXISTS (SELECT 1 FROM listings l WHERE l.model_id = m.model_id AND l.status = :status");
        if (fromYear != null) {
            nativeSql.append(" AND l.year >= :fromYear");
        }
        if (toYear != null) {
            nativeSql.append(" AND l.year <= :toYear");
        }
        nativeSql.append(")");

        return new ListingInfoSummaryFilters(nativeSql.toString());
    }

    private static void bindListingInfoSummaryParams(
            final Query query,
            final Long subcategoryId,
            final Long brandId,
            final Long modelId,
            final Integer fromYear,
            final Integer toYear,
            final Object status
    ) {
        query.setParameter("subcategoryId", subcategoryId);
        query.setParameter("status", status);
        if (brandId != null) {
            query.setParameter("brandId", brandId);
        }
        if (modelId != null) {
            query.setParameter("modelId", modelId);
        }
        if (fromYear != null) {
            query.setParameter("fromYear", fromYear);
        }
        if (toYear != null) {
            query.setParameter("toYear", toYear);
        }
    }

    private record ListingInfoSummaryFilters(String nativeWhere) {
    }
}
