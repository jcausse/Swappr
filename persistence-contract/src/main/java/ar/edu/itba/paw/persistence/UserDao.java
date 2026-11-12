package ar.edu.itba.paw.persistence;

import ar.edu.itba.paw.model.Image;
import ar.edu.itba.paw.model.User;
import ar.edu.itba.paw.model.Language;
import ar.edu.itba.paw.model.OfferRating;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;

public interface UserDao {
    Optional<User> getById(Long id);
    Optional<User> getByUsername(String username);
    Optional<User> getByEmail(String email);

    User create(
            String username,
            String displayName,
            String email,
            String password,
            Image image,
            Instant joinedAt,
            Language preferredLanguage
    );

    Optional<User> update(Long userId, String displayName, String email, String password, Long imageId);

    Optional<User> updateLocation(Long userId, Long provinceId, String locationDetail);

    Optional<User> updatePreferredLanguage(Long userId, Language preferredLanguage);
    
    Optional<User> verifyEmail(Long userId, Instant verifiedAt);

    Optional<User> suspend(Long userId, Instant suspendedAt);

    Optional<User> unsuspend(Long userId);

    boolean isUsernameTaken(String username);
    boolean isEmailTaken(String email);

    void incrementSellerRatingCounter(Long userId, OfferRating rating);
    void incrementBuyerRatingCounter(Long userId, OfferRating rating);

    void incrementSellerRatingCounters(Map<Long, Long> increments, OfferRating rating);
    void incrementBuyerRatingCounters(Map<Long, Long> increments, OfferRating rating);
}
