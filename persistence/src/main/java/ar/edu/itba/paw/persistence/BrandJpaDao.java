package ar.edu.itba.paw.persistence;

import ar.edu.itba.paw.model.Brand;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Repository
public class BrandJpaDao implements BrandDao {

    @PersistenceContext
    private EntityManager em;

    @Override
    public Optional<Brand> getById(Long id) {
        return Optional.ofNullable(em.find(Brand.class, id));
    }

    @Override
    public Optional<Brand> getByName(String name) {
        return em.createQuery("FROM Brand b WHERE b.name = :name", Brand.class)
                .setParameter("name", name)
                .getResultStream()
                .findFirst();
    }

    @Override
    public List<Brand> getAll() {
        return em.createQuery("FROM Brand b ORDER BY b.name, b.id", Brand.class)
                .getResultList();
    }

    @Override
    public List<Brand> getBySubcategoryId(Long subcategoryId) {
        return em.createQuery(
                "SELECT DISTINCT m.brand FROM Model m WHERE m.subcategory.id = :subcategoryId ORDER BY m.brand.name",
                Brand.class)
                .setParameter("subcategoryId", subcategoryId)
                .getResultList();
    }

    @Override
    public Optional<Brand> rename(Long id, String name) {
        final Brand brand = em.find(Brand.class, id);
        if (brand == null) {
            return Optional.empty();
        }
        brand.setName(name);
        return Optional.of(brand);
    }

    @Override
    public boolean delete(Long id) {
        final Brand brand = em.find(Brand.class, id);
        if (brand == null) {
            return false;
        }
        em.remove(brand);
        return true;
    }

    @Override
    public Map<Long, Long> getModelCountsByBrand() {
        final Map<Long, Long> counts = new HashMap<>();
        final List<Object[]> rows = em.createQuery(
                "SELECT m.brand.id, COUNT(m.id) FROM Model m GROUP BY m.brand.id",
                Object[].class).getResultList();
        for (final Object[] row : rows) {
            counts.put((Long) row[0], (Long) row[1]);
        }
        return counts;
    }

    @Override
    public Set<Long> getBrandIdsWithListings() {
        return new HashSet<>(em.createQuery(
                "SELECT DISTINCT l.model.brand.id FROM Listing l",
                Long.class).getResultList());
    }
}
