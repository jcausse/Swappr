package ar.edu.itba.paw.persistence;

import ar.edu.itba.paw.model.Role;
import ar.edu.itba.paw.model.User;
import lombok.NonNull;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.ArrayList;
import java.util.List;

@Repository
public class UserRoleJpaDao implements UserRoleDao {

    @PersistenceContext
    private EntityManager em;

    @Override
    public void addRole(@NonNull User user, @NonNull Role role) {
        final User attachedUser = em.find(User.class, user.getId());
        if (attachedUser != null) {
            attachedUser.getRoles().add(role);
        }
    }

    @Override
    public List<Role> getRoles(@NonNull User user) {
        final User attachedUser = em.find(User.class, user.getId());
        if (attachedUser != null) {
            return new ArrayList<>(attachedUser.getRoles());
        }
        return new ArrayList<>();
    }

    @Override
    public boolean hasRole(@NonNull User user, @NonNull Role role) {
        if (user.getId() == null) {
            return false;
        }
        final Long count = em.createQuery(
                        "SELECT COUNT(r) FROM User u JOIN u.roles r WHERE u.id = :userId AND r = :role",
                        Long.class
                )
                .setParameter("userId", user.getId())
                .setParameter("role", role)
                .getSingleResult();
        return count != null && count > 0;
    }
}
