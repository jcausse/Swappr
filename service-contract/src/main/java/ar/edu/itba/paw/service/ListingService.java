package ar.edu.itba.paw.service;

import ar.edu.itba.paw.model.Listing;
import ar.edu.itba.paw.model.ListingStatus;
import ar.edu.itba.paw.model.Page;
import ar.edu.itba.paw.service.dto.LandingListingsDto;
import ar.edu.itba.paw.service.dto.ListingCreationDto;
import ar.edu.itba.paw.service.dto.ListingFilterDto;
import ar.edu.itba.paw.service.dto.ListingUpdateDto;
import java.util.List;

public interface ListingService {
    Listing getById(Long id);

    Listing create(ListingCreationDto dto);

    Page<Listing> search(ListingFilterDto filter);

    LandingListingsDto getForLanding();

    Listing purchase(Long id, Long buyerId, String message);

    Listing pendingTransaction(Long id, Long buyerId, String message);

    void updateStatus(Long id, ListingStatus status);

    void updateStatusBulk(List<Long> ids, ListingStatus status);

    Listing update(ListingUpdateDto dto, Long currentUserId);

    void cancel(Long id, Long currentUserId);

    void cancelByAdmin(Long id);
}
