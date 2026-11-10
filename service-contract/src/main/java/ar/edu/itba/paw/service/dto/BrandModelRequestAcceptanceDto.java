package ar.edu.itba.paw.service.dto;

import ar.edu.itba.paw.model.BrandModelRequestType;

public record BrandModelRequestAcceptanceDto(
    BrandModelRequestType type,
    Long brandId,
    String newBrandName,
    String modelName
) {}
