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
    public List<ProductListingInfo> getListingInfoSummary(String brand, String model, Integer fromYear, Integer toYear, Long subcategoryId) {
        var where = " WHERE l.product.subcategory.id = :subcategoryId AND l.status = :status";
        if (brand != null) where += " AND l.product.brand = :brand";
        if (model != null) where += " AND l.product.model = :model";
        if (fromYear != null) where += " AND l.product.year >= :fromYear";
        if (toYear != null) where += " AND l.product.year <= :toYear";

//         var pageQueryStr = "SELECT l.product.brand, l.product.model FROM Listing l"
//                          + where
//                          + " GROUP BY l.product.brand, l.product.model"
//                          + " ORDER BY l.product.brand, l.product.model";
//
//         var pageQuery = em.createQuery(pageQueryStr, Object[].class)
//                 .setParameter("subcategoryId", subcategoryId)
//                 .setParameter("status", ListingStatus.ACTIVE);
//         if (brand != null) pageQuery.setParameter("brand", brand);
//         if (model != null) pageQuery.setParameter("model", model);
//         if (fromYear != null) pageQuery.setParameter("fromYear", fromYear);
//         if (toYear != null) pageQuery.setParameter("toYear", toYear);
//
//         var products = pageQuery
//                 .setFirstResult(0) // FIXME remove this!!!
//                 .setMaxResults(5)
//                 .getResultStream()
//                 .map((fields) -> new BrandModel((String)fields[0], (String)fields[1]))
//                 .toList();

        var queryStr = "SELECT l.product.brand, l.product.model, COUNT(l.condition) AS listingCount, l.condition AS condition, MIN(l.price) AS minPrice, MAX(l.price) AS maxPrice, AVG(l.price) AS avgPrice"
                     + " FROM Listing l"
                     + where
                     // + " AND l.product.brand IN (:brands) AND l.product.model IN (:models)"
                     + " GROUP BY l.condition, l.product.brand, l.product.model";

        // TODO type safe way of doing this??
        var query = em.createQuery(queryStr, Object[].class)
                .setParameter("subcategoryId", subcategoryId)
                .setParameter("status", ListingStatus.ACTIVE);
                // .setParameter("brands", products.stream().map(BrandModel::brand).toList())
                // .setParameter("models", products.stream().map(BrandModel::model).toList());
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

    private record BrandModel(String brand, String model) {}
}
