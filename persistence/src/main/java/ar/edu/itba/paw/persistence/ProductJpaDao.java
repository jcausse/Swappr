package ar.edu.itba.paw.persistence;

import ar.edu.itba.paw.model.Product;
import ar.edu.itba.paw.model.Subcategory;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
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
    public List<Integer> getYearsBySubcategoryAndBrandAndModel(Long subcategoryId, String brand, String model) {
        return em.createQuery("SELECT DISTINCT p.year FROM Product p WHERE p.subcategory.id = :subcategoryId AND p.brand = :brand AND p.model = :model ORDER BY p.year DESC", Integer.class)
                .setParameter("subcategoryId", subcategoryId)
                .setParameter("brand", brand)
                .setParameter("model", model)
                .getResultList();
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
        em.flush(); // TODO: [JPA Migration Cleanup]
        return product;
    }
}
