package ar.edu.itba.paw.persistence;

import ar.edu.itba.paw.model.Brand;
import ar.edu.itba.paw.model.BrandInfo;
import ar.edu.itba.paw.model.BrandSort;
import ar.edu.itba.paw.model.Page;
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
    public Optional<Brand> getByName(String name) {
        return em.createQuery("FROM Brand b WHERE LOWER(b.name) = LOWER(:name)", Brand.class)
                .setParameter("name", name)
                .getResultStream()
                .findFirst();
    }

    @Override
    public boolean existsByNameAndIdNot(String name, Long id) {
        return em.createQuery(
                "SELECT COUNT(b) FROM Brand b WHERE LOWER(b.name) = LOWER(:name) AND b.id <> :id",
                Long.class)
                .setParameter("name", name)
                .setParameter("id", id)
                .getSingleResult() > 0;
    }

    @Override
    public List<Brand> getAll() {
        return em.createQuery("FROM Brand b ORDER BY b.name, b.id", Brand.class)
                .getResultList();
    }

    @Override
    public Page<BrandInfo> getWithStats(BrandSort sort, int page, int pageSize) {
        final long totalCount = em.createQuery("SELECT COUNT(b) FROM Brand b", Long.class)
                .getSingleResult();

        if (totalCount == 0) {
            return new Page<>(List.of(), page, pageSize, 0);
        }

        final String direction = sort.isAscending() ? "ASC" : "DESC";
        final String orderBy = sort.getKey().equals("id")
                ? "b.id " + direction
                : "b.name " + direction + ", b.id " + direction;
        final List<BrandInfo> content = em.createQuery(
                "SELECT b.id, b.name, COUNT(DISTINCT m.id), COUNT(DISTINCT l.id)"
                + " FROM Brand b"
                + " LEFT JOIN Model m ON m.brand.id = b.id"
                + " LEFT JOIN Listing l ON l.model.id = m.id"
                + " GROUP BY b.id, b.name"
                + " ORDER BY " + orderBy,
                Object[].class)
                .setFirstResult((page - 1) * pageSize)
                .setMaxResults(pageSize)
                .getResultStream()
                .map(row -> BrandInfo.builder()
                        .id((Long) row[0])
                        .name((String) row[1])
                        .modelCount(((Number) row[2]).longValue())
                        .hasListings(((Number) row[3]).longValue() > 0)
                        .build())
                .toList();

        return new Page<>(content, page, pageSize, totalCount);
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
    public Brand create(String name) {
        final Brand brand = Brand.builder().name(name).build();
        em.persist(brand);
        return brand;
    }

    @Override
    public boolean hasListings(Long brandId) {
        return !em.createQuery(
                "SELECT l.id FROM Listing l WHERE l.model.brand.id = :brandId",
                Long.class)
                .setParameter("brandId", brandId)
                .setMaxResults(1)
                .getResultList()
                .isEmpty();
    }
}
