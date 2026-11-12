package ar.edu.itba.paw.service;

public interface ListingCancellationService {

    void cancel(Long id, Long currentUserId);

    void cancelByAdmin(Long id);
}
