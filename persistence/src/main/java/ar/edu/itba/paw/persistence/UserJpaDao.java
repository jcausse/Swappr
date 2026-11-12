package ar.edu.itba.paw.persistence;

import ar.edu.itba.paw.model.Image;
import ar.edu.itba.paw.model.Language;
import ar.edu.itba.paw.model.OfferRating;
import ar.edu.itba.paw.model.Province;
import ar.edu.itba.paw.model.User;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Repository
public class UserJpaDao implements UserDao {

    @PersistenceContext
    private EntityManager em;

    @Override
    public Optional<User> getById(Long id) {
        return Optional.ofNullable(em.find(User.class, id));
    }

    @Override
    public Optional<User> getByUsername(String username) {
        return em.createQuery("FROM User u WHERE u.username = :username", User.class)
                .setParameter("username", username)
                .getResultStream()
                .findFirst();
    }

    @Override
    public Optional<User> getByEmail(String email) {
        return em.createQuery("FROM User u WHERE u.email = :email", User.class)
                .setParameter("email", email)
                .getResultStream()
                .findFirst();
    }

    @Override
    public User create(
            String username,
            String displayName,
            String email,
            String password,
            Image image,
            Instant joinedAt,
            Language preferredLanguage
    ) {
        final Image imageRef = image != null ? em.getReference(Image.class, image.getId()) : null;
        final User user = User.builder()
                .username(username)
                .displayName(displayName)
                .email(email)
                .password(password)
                .image(imageRef)
                .joinedAt(joinedAt)
                .preferredLanguage(preferredLanguage)
                .sellerPositiveRatings(0)
                .sellerNeutralRatings(0)
                .sellerNegativeRatings(0)
                .buyerPositiveRatings(0)
                .buyerNeutralRatings(0)
                .buyerNegativeRatings(0)
                .build();
        em.persist(user);
        return user;
    }

    @Override
    public Optional<User> update(Long userId, String displayName, String email, String password, Long imageId) {
        if (displayName == null && email == null && password == null && imageId == null) {
            return Optional.empty();
        }
        final User user = em.find(User.class, userId);
        if (user == null) {
            return Optional.empty();
        }
        if (displayName != null) {
            user.setDisplayName(displayName);
        }
        if (email != null) {
            user.setEmail(email);
        }
        if (password != null) {
            user.setPassword(password);
        }
        if (imageId != null) {
            user.setImage(em.getReference(Image.class, imageId));
        }
        return Optional.of(user);
    }

    @Override
    public Optional<User> updateLocation(Long userId, Long provinceId, String locationDetail) {
        final User user = em.find(User.class, userId);
        if (user == null) {
            return Optional.empty();
        }
        final Province province = provinceId != null ? em.getReference(Province.class, provinceId) : null;
        user.setProvince(province);
        user.setLocationDetail(locationDetail);
        return Optional.of(user);
    }

    @Override
    public Optional<User> updatePreferredLanguage(Long userId, Language preferredLanguage) {
        final User user = em.find(User.class, userId);
        if (user == null) {
            return Optional.empty();
        }
        user.setPreferredLanguage(preferredLanguage);
        return Optional.of(user);
    }

    @Override
    public Optional<User> verifyEmail(Long userId, Instant verifiedAt) {
        final User user = em.find(User.class, userId);
        if (user == null) {
            return Optional.empty();
        }
        user.setEmailVerifiedAt(verifiedAt);
        return Optional.of(user);
    }

    @Override
    public Optional<User> suspend(Long userId, Instant suspendedAt) {
        final User user = em.find(User.class, userId);
        if (user == null) {
            return Optional.empty();
        }
        user.setSuspendedAt(suspendedAt);
        return Optional.of(user);
    }

    @Override
    public Optional<User> unsuspend(Long userId) {
        final User user = em.find(User.class, userId);
        if (user == null) {
            return Optional.empty();
        }
        user.setSuspendedAt(null);
        return Optional.of(user);
    }

    @Override
    public boolean isUsernameTaken(String username) {
        return em.createQuery("SELECT COUNT(u) FROM User u WHERE u.username = :username", Long.class)
                .setParameter("username", username)
                .getSingleResult() > 0;
    }

    @Override
    public boolean isEmailTaken(String email) {
        return em.createQuery("SELECT COUNT(u) FROM User u WHERE u.email = :email", Long.class)
                .setParameter("email", email)
                .getSingleResult() > 0;
    }

    @Override
    public void incrementSellerRatingCounter(Long userId, OfferRating rating) {
        final User user = em.find(User.class, userId);
        if (user != null) {
            switch (rating) {
                case POSITIVE -> user.setSellerPositiveRatings((user.getSellerPositiveRatings() != null ? user.getSellerPositiveRatings() : 0) + 1);
                case NEUTRAL -> user.setSellerNeutralRatings((user.getSellerNeutralRatings() != null ? user.getSellerNeutralRatings() : 0) + 1);
                case NEGATIVE -> user.setSellerNegativeRatings((user.getSellerNegativeRatings() != null ? user.getSellerNegativeRatings() : 0) + 1);
            }
        }
    }

    @Override
    public void incrementBuyerRatingCounter(Long userId, OfferRating rating) {
        final User user = em.find(User.class, userId);
        if (user != null) {
            switch (rating) {
                case POSITIVE -> user.setBuyerPositiveRatings((user.getBuyerPositiveRatings() != null ? user.getBuyerPositiveRatings() : 0) + 1);
                case NEUTRAL -> user.setBuyerNeutralRatings((user.getBuyerNeutralRatings() != null ? user.getBuyerNeutralRatings() : 0) + 1);
                case NEGATIVE -> user.setBuyerNegativeRatings((user.getBuyerNegativeRatings() != null ? user.getBuyerNegativeRatings() : 0) + 1);
            }
        }
    }

    @Override
    public void incrementSellerRatingCounters(Map<Long, Long> increments, OfferRating rating) {
        incrementRatingCounters(increments, rating, true);
    }

    @Override
    public void incrementBuyerRatingCounters(Map<Long, Long> increments, OfferRating rating) {
        incrementRatingCounters(increments, rating, false);
    }

    private void incrementRatingCounters(Map<Long, Long> increments, OfferRating rating, boolean seller) {
        if (increments == null || increments.isEmpty()) return;

        final String field = switch (rating) {
            case POSITIVE -> seller ? "sellerPositiveRatings" : "buyerPositiveRatings";
            case NEUTRAL -> seller ? "sellerNeutralRatings" : "buyerNeutralRatings";
            case NEGATIVE -> seller ? "sellerNegativeRatings" : "buyerNegativeRatings";
        };

        final StringBuilder incrementsCase = new StringBuilder("CASE u.id");
        final Map<String, Object> params = new HashMap<>();
        int index = 0;
        for (final Map.Entry<Long, Long> entry : increments.entrySet()) {
            final String param = "userId" + index++;
            incrementsCase.append(" WHEN :").append(param).append(" THEN ").append(entry.getValue());
            params.put(param, entry.getKey());
        }
        incrementsCase.append(" ELSE 0 END");

        final Query update = em.createQuery(
                "UPDATE User u SET u." + field + " = COALESCE(u." + field + ", 0) + "
                        + incrementsCase + " WHERE u.id IN :ids")
                .setParameter("ids", increments.keySet());
        params.forEach(update::setParameter);
        update.executeUpdate();
    }
}
