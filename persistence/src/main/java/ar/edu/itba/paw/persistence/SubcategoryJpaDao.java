package ar.edu.itba.paw.persistence;

import ar.edu.itba.paw.model.Category;
import ar.edu.itba.paw.model.Subcategory;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;
import java.util.Optional;

@Repository
public class SubcategoryJpaDao implements SubcategoryDao {

    @PersistenceContext
    private EntityManager em;

    @Override
    public Optional<Subcategory> getById(Long id) {
        return Optional.ofNullable(em.find(Subcategory.class, id));
    }

    @Override
    public Optional<Subcategory> getByName(String name) {
        return em.createQuery("FROM Subcategory s WHERE s.name = :name", Subcategory.class)
                .setParameter("name", name)
                .getResultStream()
                .findFirst();
    }

    @Override
    public List<Subcategory> getAll() {
        return em.createQuery("FROM Subcategory s", Subcategory.class).getResultList();
    }

    @Override
    public List<Subcategory> getByCategoryId(Long categoryId) {
        return em.createQuery("FROM Subcategory s WHERE s.category.id = :categoryId", Subcategory.class)
                .setParameter("categoryId", categoryId)
                .getResultList();
    }

    @Override
    public Subcategory create(String name, Long categoryId) {
        Category category = em.getReference(Category.class, categoryId);
        final Subcategory subcategory = Subcategory.builder().name(name).category(category).build();
        em.persist(subcategory);
        return subcategory;
    }
}
