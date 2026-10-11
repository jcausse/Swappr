package ar.edu.itba.paw.persistence;

import ar.edu.itba.paw.model.OneTimePassword;
import ar.edu.itba.paw.model.User;

import java.time.Instant;
import java.util.Optional;

public interface OneTimePasswordDao {
    Optional<OneTimePassword> getByUser(User requester);

    OneTimePassword create(User requester, String otpValue, Instant createdAt);

    void deleteIfPresentByUser(User requester);

    void deleteOlderThan(Instant cutoff);
}
