package ar.edu.itba.paw.persistence;

import ar.edu.itba.paw.model.Province;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;
import java.util.Optional;

@Repository
public class ProvinceJpaDao implements ProvinceDao {

    @PersistenceContext
    private EntityManager em;

    @Override
    public Optional<Province> getById(Long id) {
        return Optional.ofNullable(em.find(Province.class, id));
    }

    @Override
    public Optional<Province> getByName(String name) {
        return em.createQuery("FROM Province p WHERE p.name = :name", Province.class)
                .setParameter("name", name)
                .getResultStream()
                .findFirst();
    }

    @Override
    public List<Province> getAll() {
        return em.createQuery("FROM Province p ORDER BY p.id", Province.class).getResultList();
    }
}
