package ar.edu.itba.paw.persistence;

import ar.edu.itba.paw.model.Category;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;
import java.util.Optional;

@Repository
public class CategoryJpaDao implements CategoryDao {

    @PersistenceContext
    private EntityManager em;

    @Override
    public Optional<Category> getById(Long id) {
        return Optional.ofNullable(em.find(Category.class, id));
    }

    @Override
    public Optional<Category> getByName(String name) {
        return em.createQuery("FROM Category c WHERE c.name = :name", Category.class)
                .setParameter("name", name)
                .getResultStream()
                .findFirst();
    }

    @Override
    public List<Category> getAll() {
        return em.createQuery("FROM Category c", Category.class).getResultList();
    }

    @Override
    public Category create(String name) {
        final Category category = Category.builder().name(name).build();
        em.persist(category);
        return category;
    }
}
