package ar.edu.itba.paw.persistence;

import ar.edu.itba.paw.model.OneTimePassword;
import ar.edu.itba.paw.model.User;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.time.Instant;
import java.util.Optional;

@Repository
public class OneTimePasswordJpaDao implements OneTimePasswordDao {

    @PersistenceContext
    private EntityManager em;

    @Override
    public Optional<OneTimePassword> getByUser(User requester) {
        return Optional.ofNullable(em.find(OneTimePassword.class, requester.getId()));
    }

    @Override
    public OneTimePassword create(User requester, String otpValue, Instant createdAt) {
        /*
         * Obtaining a reference to the User first ensures that userReference is managed by the EntityManager,
         * which is needed for the OneTimePassword's @MapsId annotation to work without triggering a
         * PersistentObjectException on detached entities.
         * The line below might seem redundant on first sight, but it is needed and must not be removed.
         */
        final User userReference = em.getReference(User.class, requester.getId());
        final OneTimePassword otp = OneTimePassword.builder()
                .user(userReference)
                .otpValue(otpValue)
                .createdAt(createdAt)
                .build();
        em.persist(otp);
        em.flush(); // TODO: [JPA Migration Cleanup]
        return otp;
    }

    @Override
    public void deleteIfPresentByUser(User requester) {
        final OneTimePassword otp = em.find(OneTimePassword.class, requester.getId());
        if (otp != null) {
            em.remove(otp);
            em.flush(); // TODO: [JPA Migration Cleanup]
        }
    }

    @Override
    public void deleteOlderThan(Instant cutoff) {
        em.createQuery("DELETE FROM OneTimePassword otp WHERE otp.createdAt < :cutoff")
                .setParameter("cutoff", cutoff)
                .executeUpdate();
    }
}
