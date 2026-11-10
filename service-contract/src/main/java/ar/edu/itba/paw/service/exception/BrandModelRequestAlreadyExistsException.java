package ar.edu.itba.paw.service.exception;

import ar.edu.itba.paw.service.dto.BrandModelRequestCreationDto;

public class BrandModelRequestAlreadyExistsException extends RuntimeException {

    private final BrandModelRequestCreationDto request;

    public BrandModelRequestAlreadyExistsException(BrandModelRequestCreationDto request) {
        super("The requester already asked for model '" + request.modelName() + "'");
        this.request = request;
    }

    public BrandModelRequestCreationDto getRequest() {
        return request;
    }
}
