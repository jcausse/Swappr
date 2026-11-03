package ar.edu.itba.paw.persistence;

import ar.edu.itba.paw.model.Product;
import ar.edu.itba.paw.model.Subcategory;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
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
