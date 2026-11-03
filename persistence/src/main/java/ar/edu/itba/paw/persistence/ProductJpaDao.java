package ar.edu.itba.paw.persistence;

import ar.edu.itba.paw.model.Condition;
import ar.edu.itba.paw.model.ListingStatus;
import ar.edu.itba.paw.model.Page;
import ar.edu.itba.paw.model.Price;
import ar.edu.itba.paw.model.Product;
import ar.edu.itba.paw.model.ProductListingInfo;
import ar.edu.itba.paw.model.Subcategory;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public class ProductJpaDao implements ProductDao {

    @PersistenceContext
    private EntityManager em;

    @Override
    public Optional<Product> getById(Long id) {
        return Optional.ofNullable(em.find(Product.class, id));
    }

    @Override
    public Optional<Product> getByBrandModelYearSubcategory(String brand, String model, Integer year, Long subcategoryId) {
        return em.createQuery("FROM Product p WHERE p.brand = :brand AND p.model = :model AND p.year = :year AND p.subcategory.id = :subcategoryId", Product.class)
                .setParameter("brand", brand)
                .setParameter("model", model)
                .setParameter("year", year)
                .setParameter("subcategoryId", subcategoryId)
                .getResultStream()
                .findFirst();
    }

    @Override
    public List<String> getBrandsBySubcategory(Long subcategoryId) {
        return em.createQuery("SELECT DISTINCT p.brand FROM Product p WHERE p.subcategory.id = :subcategoryId ORDER BY p.brand", String.class)
                .setParameter("subcategoryId", subcategoryId)
                .getResultList();
    }

    @Override
    public List<String> getModelsBySubcategoryAndBrand(Long subcategoryId, String brand) {
        return em.createQuery("SELECT DISTINCT p.model FROM Product p WHERE p.subcategory.id = :subcategoryId AND p.brand = :brand ORDER BY p.model", String.class)
                .setParameter("subcategoryId", subcategoryId)
                .setParameter("brand", brand)
                .getResultList();
    }

    @Override
    public Page<ProductListingInfo> getListingInfoSummary(
        final String brand, final String model,
        final Integer fromYear, final Integer toYear,
        final Long subcategoryId,
        final int page, final int pageSize
    ) {
        final var where = new StringBuilder(
                " WHERE p.subcategory_id = ?1"
                + " AND EXISTS (SELECT 1 FROM listings l WHERE l.product_id = p.product_id AND l.status = ?2)");
        final List<Object> params = new ArrayList<>();
        params.add(subcategoryId);
        params.add(ListingStatus.ACTIVE.name());

        if (brand != null) {
            where.append(" AND p.brand = ?").append(params.size() + 1);
            params.add(brand);
        }
        if (model != null) {
            where.append(" AND p.model = ?").append(params.size() + 1);
            params.add(model);
        }
        if (fromYear != null) {
            where.append(" AND p.year >= ?").append(params.size() + 1);
            params.add(fromYear);
        }
        if (toYear != null) {
            where.append(" AND p.year <= ?").append(params.size() + 1);
            params.add(toYear);
        }

        final var countQuery = em.createNativeQuery("SELECT COUNT(*) FROM (SELECT p.brand, p.model FROM products p" + where + " GROUP BY p.brand, p.model)");
        bindParams(countQuery, params);
        final long totalCount = ((Number) countQuery.getSingleResult()).longValue();

        if (totalCount == 0) {
            return new Page<>(List.of(), page, pageSize, 0);
        }

        final var idQuery = em.createNativeQuery("SELECT p.brand, p.model FROM products p" + where + " GROUP BY p.brand, p.model ORDER BY p.brand, p.model");
        bindParams(idQuery, params);
        idQuery.setFirstResult((page - 1) * pageSize);
        idQuery.setMaxResults(pageSize);

        final var products = ((List<?>) idQuery.getResultList()).stream()
                .map((row) -> new BrandModel((String)((Object[])row)[0], (String)((Object[])row)[1]))
                .toList();

        if (products.isEmpty()) {
            return new Page<>(List.of(), page, pageSize, totalCount);
        }

        // This is UGLY!! but it works, and doesn't have any issues performance wise as "products" size is bounded to one page
        var productWhere = new StringBuilder();
        for (int i = 0; i < products.size(); i++) {
            if (i > 0) productWhere.append(" OR ");
            productWhere.append("(l.product.brand = :brand");
            productWhere.append(i);
            productWhere.append(" AND l.product.model = :model");
            productWhere.append(i);
            productWhere.append(")");
        }

        var yearWhere = new StringBuilder("");
        if (fromYear != null) yearWhere.append(" AND l.product.year >= :yearFrom");
        if (toYear != null) yearWhere.append(" AND l.product.year <= :yearTo");

        final var query = em.createQuery(
                "SELECT l.product.brand, l.product.model, COUNT(l.condition) AS listingCount, l.condition AS condition, MIN(l.price) AS minPrice, MAX(l.price) AS maxPrice, AVG(l.price) AS avgPrice"
                + " FROM Listing l"
                + " WHERE (" + productWhere + ") AND l.status = :status" + yearWhere
                + " GROUP BY l.condition, l.product.brand, l.product.model"
                + " ORDER BY l.product.brand, l.product.model, l.condition",
                Object[].class)
                .setParameter("status", ListingStatus.ACTIVE);

        for (int i = 0; i < products.size(); i++) {
            query.setParameter("brand" + i, products.get(i).brand());
            query.setParameter("model" + i, products.get(i).model());
        }
        if (fromYear != null) query.setParameter("yearFrom", fromYear);
        if (toYear != null) query.setParameter("yearTo", toYear);

        final List<ProductListingInfo> content = query.getResultStream()
                .map((fields) -> ProductListingInfo.builder()
                    .brand((String)fields[0])
                    .model((String)fields[1])
                    .listingCount((Long)fields[2])
                    .condition((Condition)fields[3])
                    .minPrice((Price)fields[4])
                    .maxPrice((Price)fields[5])
                    .avgPrice(new Price(new BigDecimal((Double)fields[6])))
                    .build()
                )
                .toList();

        return new Page<>(content, page, pageSize, totalCount);
    }

    private static void bindParams(final Query query, final List<Object> params) {
        for (int i = 0; i < params.size(); i++) {
            query.setParameter(i + 1, params.get(i));
        }
    }

    private record BrandModel(String brand, String model) {}

    @Override
    public Product create(String brand, String model, Integer year, Subcategory subcategory) {
        final Subcategory subcategoryRef = subcategory != null ? em.getReference(Subcategory.class, subcategory.getId()) : null;
        final Product product = Product.builder()
                .brand(brand)
                .model(model)
                .year(year)
                .subcategory(subcategoryRef)
                .build();
        em.persist(product);
        return product;
    }
}
