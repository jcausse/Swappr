package ar.edu.itba.paw.persistence;

import ar.edu.itba.paw.model.Brand;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;
import java.util.Optional;

@Repository
public class BrandJpaDao implements BrandDao {

    @PersistenceContext
    private EntityManager em;

    @Override
    public Optional<Brand> getById(Long id) {
        return Optional.ofNullable(em.find(Brand.class, id));
    }

    @Override
    public List<Brand> getBySubcategoryId(Long subcategoryId) {
        return em.createQuery(
                "SELECT DISTINCT m.brand FROM Model m WHERE m.subcategory.id = :subcategoryId ORDER BY m.brand.name",
                Brand.class)
                .setParameter("subcategoryId", subcategoryId)
                .getResultList();
    }
}
