package ar.edu.itba.paw.service;

import ar.edu.itba.paw.model.BrandModelRequest;
import ar.edu.itba.paw.model.Page;
import ar.edu.itba.paw.service.dto.BrandModelRequestAcceptanceDto;
import ar.edu.itba.paw.service.dto.BrandModelRequestCreationDto;

public interface BrandModelRequestService {

    Page<BrandModelRequest> getRequests(int page, int pageSize);

    BrandModelRequest create(BrandModelRequestCreationDto dto);

    void accept(Long requestId, BrandModelRequestAcceptanceDto dto);

    void reject(Long requestId);
}
