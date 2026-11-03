package ar.edu.itba.paw.persistence;

import ar.edu.itba.paw.model.Category;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;

@Repository
public class CategoryJpaDao implements CategoryDao {

    @PersistenceContext
    private EntityManager em;

    @Override
    public List<Category> getAll() {
        return em.createQuery("FROM Category c", Category.class).getResultList();
    }
}
