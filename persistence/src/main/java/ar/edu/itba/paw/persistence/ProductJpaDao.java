package ar.edu.itba.paw.persistence;

import ar.edu.itba.paw.model.Condition;
import ar.edu.itba.paw.model.ListingStatus;
import ar.edu.itba.paw.model.Price;
import ar.edu.itba.paw.model.Product;
import ar.edu.itba.paw.model.ProductListingInfo;
import ar.edu.itba.paw.model.Subcategory;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
    public List<Product> getByCategory(Long categoryId) {
        return em.createQuery("FROM Product p WHERE p.subcategory.category.id = :categoryId", Product.class)
                .setParameter("categoryId", categoryId)
                .getResultList();
    }

    @Override
    public List<Product> getBySubcategory(Long subcategoryId) {
        return em.createQuery("FROM Product p WHERE p.subcategory.id = :subcategoryId", Product.class)
                .setParameter("subcategoryId", subcategoryId)
                .getResultList();
    }

    @Override
    public List<Product> getBySubcategoryBrandModel(Long subcategoryId, String brand, String model) {
        final StringBuilder jpql = new StringBuilder("FROM Product p WHERE p.subcategory.id = :subcategoryId");
        final Map<String, Object> params = new HashMap<>();
        params.put("subcategoryId", subcategoryId);

        if (brand != null && !brand.isBlank()) {
            jpql.append(" AND p.brand = :brand");
            params.put("brand", brand);
        }
        if (model != null && !model.isBlank()) {
            jpql.append(" AND p.model = :model");
            params.put("model", model);
        }

        final TypedQuery<Product> query = em.createQuery(jpql.toString(), Product.class);
        params.forEach(query::setParameter);
        return query.getResultList();
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
    public List<ProductListingInfo> getListingInfoSummary(String brand, String model, Integer fromYear, Integer toYear, Long subcategoryId) {
        var where = " WHERE l.product.subcategory.id = :subcategoryId AND l.status = :status";
        if (brand != null) where += " AND l.product.brand = :brand";
        if (model != null) where += " AND l.product.model = :model";
        if (fromYear != null) where += " AND l.product.year >= :fromYear";
        if (toYear != null) where += " AND l.product.year <= :toYear";

        var queryStr = "SELECT l.product.brand, l.product.model, COUNT(l.condition) AS listingCount, l.condition AS condition, MIN(l.price) AS minPrice, MAX(l.price) AS maxPrice, AVG(l.price) AS avgPrice"
                     + " FROM Listing l"
                     + where
                     + " GROUP BY l.condition, l.product.brand, l.product.model";

        // TODO type safe way of doing this??
        var query = em.createQuery(queryStr, Object[].class)
                .setParameter("subcategoryId", subcategoryId)
                .setParameter("status", ListingStatus.ACTIVE);
        if (brand != null) query.setParameter("brand", brand);
        if (model != null) query.setParameter("model", model);
        if (fromYear != null) query.setParameter("fromYear", fromYear);
        if (toYear != null) query.setParameter("toYear", toYear);

        return query.getResultStream()
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
                .sorted((a, b) -> a.getCondition().ordinal() - b.getCondition().ordinal())
                .toList();
    }

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
