package ar.edu.itba.paw.persistence;

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
    public List<Subcategory> getByCategoryId(Long categoryId) {
        return em.createQuery("FROM Subcategory s WHERE s.category.id = :categoryId", Subcategory.class)
                .setParameter("categoryId", categoryId)
                .getResultList();
    }
}
